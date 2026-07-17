# 部署运行脚本说明

## 分工

| 环境 | 前端协议 | 怎么启动 |
|------|----------|----------|
| **本地开发** | **HTTP** | `start-test.bat` 或 `npm run dev` → http://localhost:5173 |
| **云服务器** | **HTTPS**（自签） | `./scripts/deploy.sh start` → https://服务器IP:5173 |

`deploy.sh` **只给云服务器用**，会自动：`VITE_HTTPS=1` + 切换 Nginx 云配置。

---

## 云服务器（Linux）

前置：已安装 Docker、JDK 21+、Node.js 18+，已从 Gitee 拉取代码。

```bash
cd /path/to/creditbank
chmod +x scripts/deploy.sh
./scripts/deploy.sh start
```

| 命令 | 作用 |
|------|------|
| `./scripts/deploy.sh start` | 启动 MySQL/Redis/Nginx + 后端 + 前端 HTTPS |
| `./scripts/deploy.sh stop` | 停前后端（容器保留） |
| `./scripts/deploy.sh stop-all` | 前后端 + Docker 全停 |
| `./scripts/deploy.sh restart` | 重启应用 |
| `./scripts/deploy.sh status` / `logs` | 状态 / 日志 |

可选：

```bash
MODE=dev ./scripts/deploy.sh start
SKIP_BUILD=1 ./scripts/deploy.sh start
HOST_IP=你的公网IP ./scripts/deploy.sh start
```

安全组放行：`80`、`5173`（按需 `8080`）。  
浏览器提示证书不安全时，点「继续访问」即可用摄像头/麦克风。

仓库已含 `application-local.yml`（DeepSeek / TRTC），一般无需再改密钥。

---

## 本机 Windows（HTTP）

```powershell
# 方式一
.\start-test.bat

# 方式二
cd docker
docker compose up -d
cd ..\backend
.\mvnw.cmd spring-boot:run
# 另开终端
cd ..\frontend
npm run dev
```

访问：**http://localhost:5173**
