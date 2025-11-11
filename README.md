# Codex01 Backend

基于 Spring Boot 3、Spring Security + JWT 与 JPA 实现的 RESTful 后端，用于为 iOS 项目 [codex01](https://github.com/xanderlee668/codex01) 提供真实接口。

- ✅ 所有端点统一挂载在 `/api` 前缀下，返回 JSON 且字段均为 `snake_case`。
- ✅ 登录成功返回 JWT，后续请求需携带 `Authorization: Bearer <token>`。
- ✅ 与前端 `APIClient`、`SampleData` 一致的数据模型，开箱即用完成登录、注册、会话恢复、雪板列表查询/发布、收藏、站内信与行程管理。

## 快速开始

```bash
mvn spring-boot:run
```

应用启动后会自动创建示例账号（`admin@admin.com` / `12345678`）以及演示用的 Listing、收藏、站内信会话与行程数据，方便直接联调。

## 主要端点

### 鉴权模块 `/api/auth`

| 方法 | 路径 | 说明 |
| ---- | ---- | ---- |
| `POST` | `/api/auth/register` | 注册新用户并直接返回 token + user 结构。 |
| `POST` | `/api/auth/login` | 校验邮箱+密码后返回 token + user。 |
| `GET` | `/api/auth/me` | 使用 JWT 恢复当前会话，返回 user 结构。 |

#### 响应结构

```json
{
  "token": "<JWT>",
  "user": {
    "user_id": "uuid",
    "email": "user@example.com",
    "display_name": "Snow Rider",
    "location": "London",
    "bio": "热爱单板",
    "rating": 4.8,
    "deals_count": 12
  }
}
```

- 注册请求体：`{"email":"user@example.com","password":"123456","display_name":"Snow Rider"}`
- 登录请求体：`{"email":"user@example.com","password":"123456"}`
- `GET /api/auth/me` 仅返回 `{"user": { ... 同上 ... }}`。

### 雪板列表模块 `/api/listings`

| 方法 | 路径 | 说明 |
| ---- | ---- | ---- |
| `GET` | `/api/listings` | 返回所有雪板 Listing，字段与前端枚举、节点完全一致。需 JWT。 |
| `POST` | `/api/listings` | 发布雪板 Listing，后端自动写入 seller 信息并返回完整实体。需 JWT。 |

#### Listing 响应示例

```json
{
  "listing_id": "uuid",
  "title": "Burton Custom X",
  "description": "轻度使用，附送固定器",
  "condition": "like_new",
  "price": 450.0,
  "location": "London",
  "trade_option": "face_to_face",
  "is_favorite": false,
  "image_url": "https://.../board.jpg",
  "seller": {
    "seller_id": "uuid",
    "display_name": "Admin Rider",
    "rating": 4.9,
    "deals_count": 32
  }
}
```

#### 发布请求示例

```json
{
  "title": "Jones Mountain Twin",
  "description": "保养良好，含原装滑雪包",
  "condition": "good",
  "price": 380.0,
  "location": "Innsbruck",
  "trade_option": "courier",
  "is_favorite": false,
  "image_url": "https://.../mountain-twin.jpg"
}
```

若校验失败、枚举取值不合法或未携带 JWT，接口会返回如下错误结构：

```json
{
  "message": "错误提示",
  "errors": {
    "price": "must be greater than or equal to 0.0"
  }
}
```

### 收藏模块 `/api/favorites`

| 方法 | 路径 | 说明 |
| ---- | ---- | ---- |
| `GET` | `/api/favorites` | 返回当前用户收藏的 Listing 列表，时间倒序。 |
| `POST` | `/api/favorites/{listing_id}` | 将指定 Listing 加入收藏并返回收藏记录。 |
| `DELETE` | `/api/favorites/{listing_id}` | 取消收藏，返回 204。 |

```json
{
  "favorite_id": "uuid",
  "added_at": "2024-05-20T10:00:00Z",
  "listing": { ... 同 Listing 响应 ... }
}
```

### 站内信模块 `/api/messages`

| 方法 | 路径 | 说明 |
| ---- | ---- | ---- |
| `GET` | `/api/messages` | 列出当前用户参与的所有站内信线程。 |
| `GET` | `/api/messages/{thread_id}` | 查看单个线程详情。 |
| `POST` | `/api/messages` | 新建会话并发送第一条消息，需传入 `listing_id` 与 `message`。 |
| `POST` | `/api/messages/{thread_id}/messages` | 在指定线程中发送消息。 |

```json
{
  "thread_id": "uuid",
  "listing": { ... },
  "buyer": {"user_id": "uuid", "display_name": "Mountain Lover", "rating": 4.6, "deals_count": 18},
  "seller": {"user_id": "uuid", "display_name": "Admin Rider", "rating": 4.9, "deals_count": 32},
  "messages": [
    {"message_id": "uuid", "sender_id": "uuid", "content": "你好，这块板子还在吗？", "sent_at": "2024-05-20T10:00:00Z"}
  ],
  "unread_count": 0,
  "archived": false,
  "updated_at": "2024-05-20T10:05:00Z"
}
```

### 行程模块 `/api/trips`

| 方法 | 路径 | 说明 |
| ---- | ---- | ---- |
| `GET` | `/api/trips` | 返回行程列表，包含成员、报名请求与群聊。 |
| `POST` | `/api/trips` | 创建行程（`title`、`destination`、`description`、`start_at`、`end_at`、可选 `status`）。 |
| `POST` | `/api/trips/{trip_id}/requests` | 提交加入请求，需 `message` 字段。 |
| `POST` | `/api/trips/{trip_id}/requests/{request_id}/approve` | 组织者审批请求。 |
| `POST` | `/api/trips/{trip_id}/messages` | 行程群聊发送消息。 |

```json
{
  "trip_id": "uuid",
  "title": "阿尔卑斯周末团",
  "destination": "Zermatt",
  "description": "轻松两日行程，适合中级及以上滑手",
  "start_at": "2024-05-24T08:00:00Z",
  "end_at": "2024-05-26T18:00:00Z",
  "status": "upcoming",
  "organizer": {"user_id": "uuid", "display_name": "Admin Rider", "role": "organizer"},
  "participants": [
    {"user_id": "uuid", "display_name": "Mountain Lover", "role": "member"}
  ],
  "pending_requests": [
    {"request_id": "uuid", "status": "pending", "message": "期待加入一起出发", "applicant": {"user_id": "uuid", "display_name": "Alps Rookie", "role": "member"}}
  ],
  "messages": [
    {"message_id": "uuid", "content": "欢迎加入，本周五集合！", "sender": {"user_id": "uuid", "display_name": "Admin Rider", "role": "organizer"}}
  ]
}
```

- `status` 支持 `planned/upcoming/active/completed` 以及兼容前端旧枚举值 `planning/ongoing/complete`，留空或空字符串默认 `planned`。
- 创建成功返回 201，并附带最新行程详情结构，可直接更新前端列表。

#### 创建行程请求示例

```json
{
  "title": "阿尔卑斯周末团",
  "destination": "Zermatt",
  "description": "轻松两日行程，适合中级及以上滑手",
  "start_at": "2024-05-24T08:00:00Z",
  "end_at": "2024-05-26T18:00:00Z",
  "status": "upcoming" // 可省略或留空，默认 planned；同时兼容 planning/ongoing/complete 等别名
}
```

## 数据模型

- **User**：邮箱、密码哈希、展示昵称、所在地、个人简介、评分与成交次数。
- **Listing**：标题、描述、成色(`new/like_new/good/worn`)、价格、所在地、交易方式(`face_to_face/courier/hybrid`)、收藏状态、图片地址与卖家信息。
- **Favorite**：关联用户与 Listing，并记录收藏时间。
- **MessageThread / Message**：站内信会话及消息内容。
- **Trip / TripParticipant / TripJoinRequest / TripMessage**：行程详情、成员、报名请求与群聊消息。
