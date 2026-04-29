# GraphQL Authentication — Spring Boot 3 + PostgreSQL

**Authentication only (không có Authorization). Dùng `@RequireLogin` annotation thay cho check thủ công.**

---

## Cấu trúc project

```
spring.graphql/
├── src/
│   ├── main/
│   │   ├── java/com/spring/graphql/
│   │   │   │
│   │   │   ├── annotation/
│   │   │   │   └── RequireLogin.java               ← ⭐ Custom annotation
│   │   │   │
│   │   │   ├── aspect/
│   │   │   │   └── RequireLoginAspect.java          ← ⭐ AOP xử lý @RequireLogin
│   │   │   │
│   │   │   ├── config/
│   │   │   │   ├── GraphQLContextInterceptor.java   ← Inject User vào GraphQL context
│   │   │   │   └── SecurityConfig.java             ← Permit all, stateless, JWT filter
│   │   │   │
│   │   │   ├── dto/
│   │   │   │   ├── AuthPayload.java                 ← Response sau login/register
│   │   │   │   └── Inputs.java                     ← LoginInput, RegisterInput, ...
│   │   │   │
│   │   │   ├── exception/
│   │   │   │   ├── AuthException.java              ← Sai password, token hết hạn, ...
│   │   │   │   ├── GraphQLExceptionHandler.java    ← Map exception → GraphQL error
│   │   │   │   └── UnauthorizedException.java      ← Chưa đăng nhập
│   │   │   │
│   │   │   ├── model/
│   │   │   │   ├── Post.java
│   │   │   │   ├── RefreshToken.java
│   │   │   │   └── User.java
│   │   │   │
│   │   │   ├── repository/
│   │   │   │   ├── PostRepository.java
│   │   │   │   ├── RefreshTokenRepository.java
│   │   │   │   └── UserRepository.java
│   │   │   │
│   │   │   ├── resolver/
│   │   │   │   ├── AuthResolver.java               ← login, register, refreshToken, logout
│   │   │   │   └── UserResolver.java               ← me, myPosts, allPosts, createPost
│   │   │   │
│   │   │   ├── security/
│   │   │   │   ├── AuthChecker.java                ← Helper kiểm tra login (dùng trong code)
│   │   │   │   └── JwtAuthFilter.java              ← Extract token → set SecurityContext
│   │   │   │
│   │   │   ├── service/
│   │   │   │   ├── AuthService.java                ← login, register, refresh, logout
│   │   │   │   ├── JwtService.java                 ← Generate + validate JWT
│   │   │   │   ├── PostService.java
│   │   │   │   └── UserDetailsServiceImpl.java     ← Load user từ DB cho Spring Security
│   │   │   │
│   │   │   ├── Application.java                    ← Entry point
│   │   │   └── DataInitializer.java                ← Tạo user test khi startup
│   │   │
│   │   └── resources/
│   │       ├── application.properties              ← PostgreSQL + JWT config
│   │       └── graphql/
│   │           └── schema.graphqls                 ← GraphQL schema
│   │
│   └── test/
│       └── java/com/spring/graphql/
│           └── ApplicationTests.java
│
└── pom.xml
```

---

## Luồng hoạt động

```
HTTP Request + "Authorization: Bearer <token>"
        │
        ▼
┌──────────────────────────────────────────────┐
│ JwtAuthFilter                                │
│                                              │
│  Có token hợp lệ?                            │
│  → YES : set SecurityContext(username)       │
│  → NO  : SecurityContext = anonymous         │
└─────────────────────┬────────────────────────┘
                      │
                      ▼
┌──────────────────────────────────────────────┐
│ GraphQLContextInterceptor                    │
│                                              │
│  SecurityContext có username?                │
│  → YES : load User từ PostgreSQL             │
│          ctx["CURRENT_USER"] = user          │
│  → NO  : ctx["CURRENT_USER"] = null          │
└─────────────────────┬────────────────────────┘
                      │
                      ▼
┌──────────────────────────────────────────────┐
│ RequireLoginAspect  (AOP)                    │
│                                              │
│  Method có @RequireLogin?                    │
│  → YES : ctx["CURRENT_USER"] != null?        │
│            → tiếp tục chạy ✅               │
│          ctx["CURRENT_USER"] == null?        │
│            → throw UnauthorizedException ❌  │
│  → NO  : bỏ qua (public method)             │
└──────────────┬───────────────┬───────────────┘
               │               │
               ▼ (pass)        ▼ (fail)
        Resolver chạy    UnauthorizedException
                               │
                        GraphQLExceptionHandler
                               │
                        { "code": "UNAUTHENTICATED" }
```

---

## Cài đặt & chạy

### Yêu cầu
- Java 21
- Maven 3.9+
- PostgreSQL 14+

### Bước 1 — Khởi động PostgreSQL

```bash
# Dùng Docker (nhanh nhất)
docker run --name pg \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=graphql_db \
  -p 5432:5432 \
  -d postgres:16
```

Hoặc tạo database thủ công:
```sql
CREATE DATABASE graphql_db;
```

### Bước 2 — Cấu hình `application.properties`

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/graphql_db
spring.datasource.username=postgres
spring.datasource.password=postgres
```

### Bước 3 — Chạy project

```bash
./mvnw spring-boot:run
```

Khi startup, `DataInitializer` tự tạo 2 user test trong PostgreSQL:

| username | password    |
|----------|-------------|
| alice    | password123 |
| bob      | password123 |

---

## GraphQL API

| Operation      | Type     | Auth cần? | Mô tả                          |
|----------------|----------|-----------|--------------------------------|
| `login`        | Mutation | ❌ Public  | Đăng nhập, trả về JWT          |
| `register`     | Mutation | ❌ Public  | Đăng ký tài khoản mới          |
| `refreshToken` | Mutation | ❌ Public  | Lấy access token mới           |
| `publicInfo`   | Query    | ❌ Public  | Thông tin công khai            |
| `logout`       | Mutation | ✅ Login   | Đăng xuất, xóa refresh token  |
| `me`           | Query    | ✅ Login   | Thông tin user đang đăng nhập  |
| `myPosts`      | Query    | ✅ Login   | Bài viết của mình              |
| `allPosts`     | Query    | ✅ Login   | Tất cả bài viết                |
| `createPost`   | Mutation | ✅ Login   | Tạo bài viết mới               |

---

## Test với GraphiQL

Truy cập: **http://localhost:8080/graphiql**

### 1. Đăng nhập
```graphql
mutation {
  login(input: {
    username: "alice"
    password: "password123"
  }) {
    accessToken
    refreshToken
    expiresIn
    user { id username email }
  }
}
```

### 2. Thêm Authorization header
Trong GraphiQL → **HTTP Headers**:
```json
{ "Authorization": "Bearer <accessToken>" }
```

### 3. Lấy thông tin user hiện tại
```graphql
query {
  me {
    id username email createdAt
  }
}
```

### 4. Tạo bài viết
```graphql
mutation {
  createPost(input: {
    title: "Bài viết đầu tiên"
    content: "Nội dung..."
  }) {
    id title
    author { username }
    createdAt
  }
}
```

### 5. Không có token → lỗi
```graphql
query {
  me { username }
}
```
```json
{
  "errors": [{
    "message": "Bạn cần đăng nhập để thực hiện thao tác này",
    "extensions": {
      "code": "UNAUTHENTICATED"
    }
  }]
}
```

### 6. Refresh token
```graphql
mutation {
  refreshToken(input: {
    refreshToken: "<refreshToken>"
  }) {
    accessToken
    expiresIn
  }
}
```

### 7. Đăng xuất
```graphql
mutation {
  logout
}
```

---

## Test với curl

```bash
# 1. Login — lấy token
curl -s -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"mutation{login(input:{username:\"alice\",password:\"password123\"}){accessToken refreshToken}}"}' \
  | jq .

# 2. Gọi protected query (thay <TOKEN>)
curl -s -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{"query":"query{me{id username email}}"}' \
  | jq .

# 3. Không có token → UNAUTHENTICATED
curl -s -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"query{me{username}}"}' \
  | jq .
```

---

## Cơ chế @RequireLogin

Thay vì kiểm tra thủ công trong từng method:
```java
// ❌ Trước — phải nhớ gọi mỗi method, dễ quên
public User me(GraphQLContext ctx) {
    User user = authChecker.requireLogin(ctx);
    return user;
}
```

Chỉ cần đặt annotation:
```java
// ✅ Sau — AOP tự động check trước khi method chạy
@QueryMapping
@RequireLogin
public User me(GraphQLContext ctx) {
    return ctx.get(CURRENT_USER_KEY);
}
```

`RequireLoginAspect` intercept tất cả method có `@RequireLogin`, kiểm tra `GraphQLContext`, và throw `UnauthorizedException` nếu chưa login — trước khi method body chạy.

