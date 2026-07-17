#!/usr/bin/env bash
# =============================================================================
# 星秩存册 · 一键部署 / 启停脚本（本机 Linux / 云服务器通用）
#
# 用法：
#   chmod +x scripts/deploy.sh
#   ./scripts/deploy.sh start      # 启动 Docker + 后端 + 前端
#   ./scripts/deploy.sh stop       # 停止后端、前端（Docker 容器默认保留）
#   ./scripts/deploy.sh stop-all   # 连同 Docker 一起停
#   ./scripts/deploy.sh restart
#   ./scripts/deploy.sh status
#   ./scripts/deploy.sh logs       # 查看后端/前端最近日志
#
# 可选环境变量：
#   MODE=dev|prod          默认 prod（jar 后台跑）；dev 用 spring-boot:run
#   SKIP_NPM_INSTALL=1     跳过 npm install
#   SKIP_BUILD=1           已有 jar 时跳过重新打包
#   HOST_IP=x.x.x.x        打印访问地址时使用（不填则自动探测）
# =============================================================================

set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DOCKER_DIR="$ROOT/docker"
BACKEND_DIR="$ROOT/backend"
FRONTEND_DIR="$ROOT/frontend"
LOG_DIR="$ROOT/logs"
PID_DIR="$LOG_DIR"
BACKEND_PID="$PID_DIR/backend.pid"
FRONTEND_PID="$PID_DIR/frontend.pid"
BACKEND_LOG="$LOG_DIR/backend.log"
FRONTEND_LOG="$LOG_DIR/frontend.log"

MODE="${MODE:-prod}"
SKIP_NPM_INSTALL="${SKIP_NPM_INSTALL:-0}"
SKIP_BUILD="${SKIP_BUILD:-0}"

mkdir -p "$LOG_DIR"

RED=$'\033[31m'
GREEN=$'\033[32m'
YELLOW=$'\033[33m'
CYAN=$'\033[36m'
RESET=$'\033[0m'

info()  { echo "${CYAN}[INFO]${RESET} $*" >&2; }
ok()    { echo "${GREEN}[OK]${RESET} $*" >&2; }
warn()  { echo "${YELLOW}[WARN]${RESET} $*" >&2; }
fail()  { echo "${RED}[ERROR]${RESET} $*" >&2; exit 1; }

detect_host_ip() {
  if [[ -n "${HOST_IP:-}" ]]; then
    echo "$HOST_IP"
    return
  fi
  # 优先取默认路由网卡 IP（云服务器内网/公网网卡通常在此）
  local ip
  ip="$(ip -4 route get 1.1.1.1 2>/dev/null | awk '{for(i=1;i<=NF;i++) if($i=="src"){print $(i+1); exit}}' || true)"
  if [[ -z "$ip" ]]; then
    ip="$(hostname -I 2>/dev/null | awk '{print $1}' || true)"
  fi
  echo "${ip:-127.0.0.1}"
}

need_cmd() {
  command -v "$1" >/dev/null 2>&1 || fail "未找到命令：$1，请先安装"
}

compose() {
  if docker compose version >/dev/null 2>&1; then
    (cd "$DOCKER_DIR" && docker compose "$@")
  elif command -v docker-compose >/dev/null 2>&1; then
    (cd "$DOCKER_DIR" && docker-compose "$@")
  else
    fail "未找到 docker compose / docker-compose"
  fi
}

check_prereq() {
  need_cmd docker
  need_cmd java
  need_cmd node
  need_cmd npm
  docker info >/dev/null 2>&1 || fail "Docker 未运行，请先启动 Docker 服务：sudo systemctl start docker"
  local java_ver
  java_ver="$(java -version 2>&1 | head -n1 || true)"
  info "Java: $java_ver"
  info "Node: $(node -v)  npm: $(npm -v)"
}

wait_mysql() {
  info "等待 MySQL 健康检查..."
  local i=0
  while (( i < 60 )); do
    local status
    status="$(docker inspect --format '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' credit-bank-mysql 2>/dev/null || echo missing)"
    if [[ "$status" == "healthy" || "$status" == "running" ]]; then
      # healthy 最理想；部分环境下无 health 字段时 running 也继续
      if [[ "$status" == "healthy" ]]; then
        ok "MySQL 已就绪"
        return
      fi
    fi
    sleep 2
    i=$((i + 1))
    echo -ne "\r       等待中... $((i * 2))s"
  done
  echo
  fail "MySQL 未能在 120 秒内就绪，请执行：cd docker && docker compose logs mysql"
}

is_port_listen() {
  local port="$1"
  if command -v ss >/dev/null 2>&1; then
    ss -ltn "sport = :$port" 2>/dev/null | grep -q ":$port"
  else
    netstat -ltn 2>/dev/null | grep -q ":$port "
  fi
}

http_ok() {
  local url="$1"
  if command -v curl >/dev/null 2>&1; then
    # -k：开发环境 Vite 自签 HTTPS 证书
    curl -kfsS --max-time 5 "$url" >/dev/null 2>&1
  else
    return 1
  fi
}

pid_running() {
  local pidfile="$1"
  [[ -f "$pidfile" ]] || return 1
  local pid
  pid="$(cat "$pidfile" 2>/dev/null || true)"
  [[ -n "$pid" ]] || return 1
  kill -0 "$pid" 2>/dev/null
}

stop_pidfile() {
  local name="$1"
  local pidfile="$2"
  if pid_running "$pidfile"; then
    local pid
    pid="$(cat "$pidfile")"
    info "停止 $name (PID $pid)..."
    kill "$pid" 2>/dev/null || true
    for _ in $(seq 1 20); do
      kill -0 "$pid" 2>/dev/null || break
      sleep 0.5
    done
    if kill -0 "$pid" 2>/dev/null; then
      warn "$name 未退出，强制结束"
      kill -9 "$pid" 2>/dev/null || true
    fi
    rm -f "$pidfile"
    ok "$name 已停止"
  else
    rm -f "$pidfile"
    info "$name 未在运行"
  fi
}

start_docker() {
  info "应用云服务器 Nginx 配置（反代 HTTPS 前端）..."
  if [[ -f "$DOCKER_DIR/nginx/conf.d/default.cloud.conf" ]]; then
    cp "$DOCKER_DIR/nginx/conf.d/default.cloud.conf" "$DOCKER_DIR/nginx/conf.d/default.conf"
  fi
  info "启动 Docker 服务（MySQL / Redis / Nginx）..."
  compose up -d
  wait_mysql
  # Redis 也顺便等一下
  local rstatus
  rstatus="$(docker inspect --format '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' credit-bank-redis 2>/dev/null || echo missing)"
  info "Redis 状态: $rstatus"
}

# 解析后端构建命令：优先 ./mvnw，否则系统 mvn
resolve_mvn() {
  cd "$BACKEND_DIR"
  if [[ -f ./mvnw ]]; then
    chmod +x ./mvnw 2>/dev/null || true
    echo "./mvnw"
  elif command -v mvn >/dev/null 2>&1; then
    warn "未找到 backend/mvnw，改用系统 mvn"
    echo "mvn"
  else
    fail "未找到 backend/mvnw，且系统未安装 mvn。请安装 Maven 或补齐 backend/mvnw 后重试"
  fi
}

build_backend() {
  cd "$BACKEND_DIR"
  local jar
  jar="$(ls -1 target/credit-bank-platform-*.jar 2>/dev/null | grep -v '\.original$' | head -n1 || true)"
  if [[ "$SKIP_BUILD" == "1" && -n "$jar" ]]; then
    info "跳过打包，使用已有 jar: $jar"
    echo "$jar"
    return
  fi
  info "打包后端（首次可能较久）..."
  local mvn_cmd
  mvn_cmd="$(resolve_mvn)"
  "$mvn_cmd" -q -DskipTests package
  jar="$(ls -1 target/credit-bank-platform-*.jar 2>/dev/null | grep -v '\.original$' | head -n1 || true)"
  [[ -n "$jar" ]] || fail "打包完成但未找到 jar"
  echo "$jar"
}

start_backend() {
  if http_ok "http://127.0.0.1:8080/api/health"; then
    ok "后端已在 8080 运行，跳过启动"
    return
  fi
  if is_port_listen 8080; then
    fail "端口 8080 已被占用，且健康检查失败。请先释放端口或执行 stop"
  fi

  cd "$BACKEND_DIR"
  if [[ "$MODE" == "dev" ]]; then
    info "以 dev 模式启动后端（spring-boot:run）..."
    local mvn_cmd
    mvn_cmd="$(resolve_mvn)"
    nohup "$mvn_cmd" spring-boot:run >"$BACKEND_LOG" 2>&1 &
    echo $! >"$BACKEND_PID"
  else
    local jar
    jar="$(build_backend)"
    info "启动后端 jar: $jar"
    nohup java -jar "$jar" >"$BACKEND_LOG" 2>&1 &
    echo $! >"$BACKEND_PID"
  fi

  info "等待后端就绪..."
  local i=0
  while (( i < 90 )); do
    if http_ok "http://127.0.0.1:8080/api/health"; then
      ok "后端已就绪 → http://127.0.0.1:8080/api/health"
      return
    fi
    if ! pid_running "$BACKEND_PID"; then
      echo
      tail -n 40 "$BACKEND_LOG" || true
      fail "后端进程已退出，请查看 $BACKEND_LOG"
    fi
    sleep 2
    i=$((i + 1))
    echo -ne "\r       等待中... $((i * 2))s"
  done
  echo
  tail -n 40 "$BACKEND_LOG" || true
  fail "后端启动超时，请查看 $BACKEND_LOG"
}

start_frontend() {
  if is_port_listen 5173 && http_ok "https://127.0.0.1:5173"; then
    ok "前端已在 5173 运行，跳过启动"
    return
  fi
  if is_port_listen 5173; then
    fail "端口 5173 已被占用。请先释放或执行 stop"
  fi

  cd "$FRONTEND_DIR"
  if [[ ! -d node_modules && "$SKIP_NPM_INSTALL" != "1" ]]; then
    info "安装前端依赖（npm install）..."
    npm install
  elif [[ ! -d node_modules ]]; then
    fail "缺少 frontend/node_modules，请先 npm install 或去掉 SKIP_NPM_INSTALL=1"
  fi

  info "启动前端 HTTPS（云服务器模式，VITE_HTTPS=1）..."
  # 仅云服务器启用自签 HTTPS；本地请直接 npm run dev（HTTP）
  nohup env VITE_HTTPS=1 npm run dev -- --host 0.0.0.0 --port 5173 >"$FRONTEND_LOG" 2>&1 &
  echo $! >"$FRONTEND_PID"

  local i=0
  while (( i < 30 )); do
    if http_ok "https://127.0.0.1:5173"; then
      ok "前端已就绪（HTTPS）"
      return
    fi
    if ! pid_running "$FRONTEND_PID"; then
      # npm 脚本可能产生子进程，pid 不一定是 vite；再探端口
      if is_port_listen 5173; then
        ok "前端端口已监听"
        return
      fi
      echo
      tail -n 40 "$FRONTEND_LOG" || true
      fail "前端进程异常，请查看 $FRONTEND_LOG"
    fi
    sleep 1
    i=$((i + 1))
  done
  warn "前端健康检查超时，请查看 $FRONTEND_LOG（有时首次编译较慢）"
}

print_summary() {
  local ip
  ip="$(detect_host_ip)"
  cat <<EOF

========================================
  星秩存册 已启动
========================================
  本机前端:  https://127.0.0.1:5173
  云访问:    https://${ip}:5173
  经 Nginx:  http://${ip}/   （反代到前端 HTTPS；音视频请优先直连 :5173）
  后端健康:  http://${ip}:8080/api/health
  账号密码:  student1 / enterprise1 / admin   密码均为 admin123

  首次用自签证书时，浏览器会提示「不安全」，点「继续访问 / 高级 → 继续」即可
  日志目录:  $LOG_DIR
  停止服务:  ./scripts/deploy.sh stop

  云服务器请放行安全组端口: 80 / 5173 / 8080（按需）
  说明: 本脚本仅用于云服务器；本地开发请用 HTTP（npm run dev / start-test.bat）
========================================
EOF
}

cmd_start() {
  check_prereq
  start_docker
  start_backend
  start_frontend
  print_summary
}

cmd_stop() {
  stop_pidfile "前端" "$FRONTEND_PID"
  # vite 常由 npm 拉起，额外按端口清理
  if is_port_listen 5173; then
    if command -v fuser >/dev/null 2>&1; then
      warn "释放 5173 端口..."
      fuser -k 5173/tcp >/dev/null 2>&1 || true
    elif command -v lsof >/dev/null 2>&1; then
      local p
      p="$(lsof -t -i:5173 2>/dev/null || true)"
      [[ -n "$p" ]] && kill $p 2>/dev/null || true
    fi
  fi
  stop_pidfile "后端" "$BACKEND_PID"
  if is_port_listen 8080; then
    if command -v fuser >/dev/null 2>&1; then
      warn "释放 8080 端口..."
      fuser -k 8080/tcp >/dev/null 2>&1 || true
    elif command -v lsof >/dev/null 2>&1; then
      local p
      p="$(lsof -t -i:8080 2>/dev/null || true)"
      [[ -n "$p" ]] && kill $p 2>/dev/null || true
    fi
  fi
  ok "应用进程已停止（Docker 仍在运行）。若要停容器：./scripts/deploy.sh stop-all"
}

cmd_stop_all() {
  cmd_stop
  info "停止 Docker 容器..."
  compose down || true
  ok "全部已停止"
}

cmd_status() {
  echo "---- Docker ----"
  compose ps || true
  echo
  echo "---- 进程 / 端口 ----"
  if pid_running "$BACKEND_PID"; then
    ok "后端 PID $(cat "$BACKEND_PID")"
  else
    warn "后端 PID 文件不存在或进程已退出"
  fi
  if http_ok "http://127.0.0.1:8080/api/health"; then
    ok "后端健康检查通过"
  else
    warn "后端健康检查失败"
  fi
  if is_port_listen 5173; then
    ok "前端端口 5173 在监听"
  else
    warn "前端端口 5173 未监听"
  fi
  echo
  echo "日志: $BACKEND_LOG / $FRONTEND_LOG"
}

cmd_logs() {
  echo "======== backend (tail) ========"
  tail -n 50 "$BACKEND_LOG" 2>/dev/null || echo "(无日志)"
  echo
  echo "======== frontend (tail) ========"
  tail -n 50 "$FRONTEND_LOG" 2>/dev/null || echo "(无日志)"
}

cmd_restart() {
  cmd_stop
  cmd_start
}

usage() {
  cat <<EOF
用法: $0 {start|stop|stop-all|restart|status|logs}

示例:
  ./scripts/deploy.sh start
  MODE=dev ./scripts/deploy.sh start          # 开发模式（不打 jar）
  SKIP_BUILD=1 ./scripts/deploy.sh start      # 复用已有 jar
  HOST_IP=1.2.3.4 ./scripts/deploy.sh start   # 指定打印的访问 IP
EOF
}

main() {
  local action="${1:-}"
  case "$action" in
    start)     cmd_start ;;
    stop)      cmd_stop ;;
    stop-all)  cmd_stop_all ;;
    restart)   cmd_restart ;;
    status)    cmd_status ;;
    logs)      cmd_logs ;;
    -h|--help|help|"") usage; [[ -n "$action" ]] || exit 1 ;;
    *) fail "未知命令: $action（执行 $0 help 查看用法）" ;;
  esac
}

main "$@"
