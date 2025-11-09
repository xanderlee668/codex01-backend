# Codex01 Backend

基于 Spring Boot、MySQL 和 JWT 的 RESTful 后端服务，实现用户注册/登录与任务管理能力，供 iOS 客户端后续对接。

## 功能概述

- 用户注册：邮箱唯一性校验、BCrypt 密码加密、注册成功后返回访问令牌。
- 用户登录：用户名/密码认证，通过 JWT 为后续请求提供授权头。
- 任务管理：基于用户的任务列表查询、创建、查看详情、更新、删除。
- 统一异常响应：验证失败与业务异常提供结构化 JSON 反馈。
- CORS 全局配置，方便移动端或 Web 端跨域访问。
- Actuator 健康检查端点，便于部署环境探活。

## 项目结构

```
src/
  main/java/com/codex/backend/
    config/        # JWT 与 CORS 配置
    domain/        # JPA 实体
    repository/    # 数据访问层
    security/      # JWT & Spring Security 相关组件
    service/       # 业务逻辑与映射
    web/           # REST 控制器与异常处理
  main/resources/
    application.yml
```

## 开发环境准备

1. 安装 Java 17、Maven 3.9+。
2. 准备 MySQL 数据库，并在 `src/main/resources/application.yml` 中配置连接信息（默认使用数据库 `codex`，用户名 `codex`、密码 `codexpass`）。
3. 执行数据库中用户创建示例：

```sql
CREATE DATABASE IF NOT EXISTS codex;
CREATE USER 'codex'@'%' IDENTIFIED BY 'codexpass';
GRANT ALL PRIVILEGES ON codex.* TO 'codex'@'%';
FLUSH PRIVILEGES;
```

## 运行

```bash
mvn spring-boot:run
```

应用启动后主要接口：

- `POST /api/auth/register`：注册用户。
- `POST /api/auth/login`：登录并获取 JWT。
- `GET /api/tasks`：获取当前用户任务列表，支持 `completed`、`due_from`、`due_to`、`keyword` 等查询参数。
- `POST /api/tasks`：创建任务。
- `GET /api/tasks/{id}`：获取单个任务。
- `PUT /api/tasks/{id}`：更新任务。
- `DELETE /api/tasks/{id}`：删除任务。
- `GET /api/tasks/summary`：统计任务数量、完成情况与提醒数。
- `GET /api/users/me`：获取当前登录用户资料。
- `PUT /api/users/me`：更新当前登录用户昵称。

请求需要在 `Authorization` 头中携带 `Bearer <token>`。

## 测试

```bash
mvn test
```

## Docker 部署

提供了 `Dockerfile` 与 `docker-compose.yml` 便于在云服务器部署：

1. 构建镜像：

   ```bash
   docker build -t codex01-backend .
   ```

2. 使用 Docker Compose 一键启动应用与 MySQL：

   ```bash
   docker compose up -d
   ```

   Compose 文件包含数据库初始化环境变量，默认暴露应用 8080 端口。

部署前请修改 `application.yml`、环境变量及 `jwt.secret` 等敏感配置。
