# 部署运行脚本说明

## 云服务器（Linux，推荐）

前置：已安装 Docker、JDK 21+、Node.js 18+，已从 Gitee 拉取代码。

```bash
cd /path/to/creditbank
chmod +x scripts/deploy.sh
./scripts/deploy.sh start
```

常用命令：

| 命令 | 作用 |
|------|------|
| `./scripts/deploy.sh start` | 启动 MySQL/Redis/Nginx + 后端 + 前端 |
| `./scripts/deploy.sh stop` | 停前后端（容器保留） |
| `./scripts/deploy.sh stop-all` | 前后端 + Docker 全停 |
| `./scripts/deploy.sh restart` | 重启应用 |
| `./scripts/deploy.sh status` | 查看状态 |
| `./scripts/deploy.sh logs` | 看最近日志 |

可选环境变量：

```bash
MODE=dev ./scripts/deploy.sh start       # 不打 jar，用 spring-boot:run
SKIP_BUILD=1 ./scripts/deploy.sh start   # 复用已有 jar
HOST_IP=你的公网IP ./scripts/deploy.sh start
```

安全组 / 防火墙需放行：`80`、`5173`（按需 `8080`）。

配置说明：仓库已含 `application-local.yml`（DeepSeek / TRTC），一般无需在服务器再改密钥。数据库账号与 `docker-compose.yml` 一致（MySQL `3307`，Redis `6379`）。

## 本机 Windows

继续用根目录 `start-test.bat`，或：

```powershell
cd docker
docker compose up -d
cd ..\backend
.\mvnw.cmd spring-boot:run
# 另开终端
cd ..\frontend
npm run dev
```
