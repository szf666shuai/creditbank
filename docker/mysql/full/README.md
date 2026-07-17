# 完整库备份（含结构 + 数据）

来源：本地 Docker `credit-bank-mysql` 导出。  
云服务器拉代码后执行：

```bash
cd /home/ubuntu/creditbank
git fetch gitee && git reset --hard gitee/master
chmod +x scripts/restore-db-full.sh
FORCE=1 ./scripts/restore-db-full.sh
```

或一条命令：

```bash
docker exec -i credit-bank-mysql mysql -uroot -proot123456 < docker/mysql/full/credit_bank_full.sql
docker exec credit-bank-redis redis-cli -a redis123456 FLUSHDB
```

**注意**：会覆盖云上当前 `credit_bank` 全部数据。
