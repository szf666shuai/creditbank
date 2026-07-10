# 终身学习学分银行平台 · 学习管理系统

## 项目简介

本平台由一系列机构加盟，运用积分管理手段提升学员学习积极性。积分商城支持获取、转换、增长、消耗等功能；学习管理系统涵盖档案管理、成果管理、论坛、报名签到、招聘求职、区块链校验、诚信评定等模块。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Java 21、Spring Boot 3.4、MyBatis-Plus、Redis |
| 前端 | Vue 3、TypeScript、Vite、Element Plus |
| 数据库 | MySQL 8.0 |
| 中间件 | Redis 7、Nginx |
| 部署 | Docker Compose |

## 项目结构

```
├── backend/          # Spring Boot 后端
├── frontend/         # Vue3 前端
├── docker/           # Docker 编排（MySQL、Redis、Nginx）
└── docs/             # 项目文档
```

## 快速启动

### 1. 环境要求

| 工具 | 版本要求 | 说明 |
|------|----------|------|
| JDK | 21+ | 已检测到本机 Java 21 |
| Node.js | 18+ | 前端开发 |
| Docker Desktop | 最新版 | 运行 MySQL / Redis / Nginx |
| Git | 2.x | 版本管理 |

> Maven 无需单独安装，后端目录已包含 Maven Wrapper（`mvnw.cmd`）。

### 2. 启动基础服务（MySQL + Redis + Nginx）

```powershell
cd docker
docker compose up -d
```

服务端口：

- MySQL：`localhost:3307`（用户 `creditbank` / 密码 `creditbank123`，库名 `credit_bank`）
- Redis：`localhost:6379`（密码 `redis123456`）
- Nginx：`localhost:80`

### 3. 启动后端

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

验证：访问 http://localhost:8080/api/health

### 4. 启动前端

```powershell
cd frontend
npm install
npm run dev
```

访问：http://localhost:5173

## 数据库账号（开发环境）

| 服务 | 账号 | 密码 | 库/DB |
|------|------|------|-------|
| MySQL root | root | root123456 | credit_bank |
| MySQL 应用 | creditbank | creditbank123 | credit_bank |
| Redis | - | redis123456 | db0 |

## 团队成员环境配置

1. 克隆仓库后，按上述步骤启动 Docker 服务
2. 使用 IntelliJ IDEA 2020+ 打开 `backend` 目录，导入 Maven 项目
3. 使用 HBuilderX 或 VS Code 打开 `frontend` 目录
4. 数据库管理工具推荐 Navicat / SQLyog 连接本地 MySQL

## 模块分工建议

| 模块 | 后端包路径建议 | 前端目录建议 |
|------|----------------|--------------|
| 积分商城 | `module.credit` | `views/credit/` |
| 学习档案 | `module.archive` | `views/archive/` |
| 学习成果 | `module.achievement` | `views/achievement/` |
| 论坛 | `module.forum` | `views/forum/` |
| 报名签到 | `module.activity` | `views/activity/` |
| 招聘求职 | `module.job` | `views/job/` |
| 区块链校验 | `module.blockchain` | `views/blockchain/` |
| 诚信评定 | `module.integrity` | `views/integrity/` |
| 系统管理 | `module.system` | `views/system/` |

## 常见问题

**Q: Docker 启动 MySQL 失败？**  
A: 检查 3306 端口是否被占用，或执行 `docker compose down -v` 后重新启动。

**Q: 后端连接数据库失败？**  
A: 确认 Docker 中 MySQL 容器健康（`docker compose ps`），等待初始化脚本执行完毕（约 30 秒）。

**Q: 前端代理 404？**  
A: 确认后端已在 8080 端口运行，且 `vite.config.ts` 中 proxy 配置正确。
