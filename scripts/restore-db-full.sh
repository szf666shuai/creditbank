#!/usr/bin/env bash
# 用仓库内完整库备份覆盖当前 MySQL（云服务器 / 本机 Docker 通用）
# 用法：./scripts/restore-db-full.sh
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DUMP="$ROOT/docker/mysql/full/credit_bank_full.sql"
CONTAINER="${MYSQL_CONTAINER:-credit-bank-mysql}"
ROOT_PASS="${MYSQL_ROOT_PASSWORD:-root123456}"

[[ -f "$DUMP" ]] || { echo "[ERROR] 找不到备份: $DUMP"; exit 1; }

if ! docker ps --format '{{.Names}}' | grep -qx "$CONTAINER"; then
  echo "[ERROR] 容器未运行: $CONTAINER（先 docker compose up -d mysql）"
  exit 1
fi

echo "[INFO] 将用本地备份覆盖库 credit_bank → 容器 $CONTAINER"
echo "[WARN] 云上现有数据会被替换为备份内容"
if [[ "${FORCE:-0}" != "1" ]]; then
  read -r -p "确认继续？[y/N] " ans
  [[ "${ans:-}" =~ ^[Yy]$ ]] || { echo "已取消"; exit 0; }
fi

docker exec -i "$CONTAINER" mysql -uroot -p"$ROOT_PASS" < "$DUMP"
# 清首页等 Redis 缓存，避免旧数据
if docker ps --format '{{.Names}}' | grep -qx credit-bank-redis; then
  docker exec credit-bank-redis redis-cli -a redis123456 FLUSHDB >/dev/null 2>&1 || true
fi
echo "[OK] 恢复完成。刷新前端即可。"
