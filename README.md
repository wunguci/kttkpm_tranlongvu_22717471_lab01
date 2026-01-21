# Lab 01 - Trần Long Vũ - 22717471

## 1. Email Queue Service (Message Queue)

Producer nhận email requests qua API và đẩy vào queue.
Consumer lấy từ queue và xử lý ngay lập tức.

```
+----------+        +------------+        +-----------+
| Producer | -----> | RabbitMQ   | -----> | Consumer  |
+----------+        +------------+        +-----------+
                         Queue
```

### Run

```bash
cd message_queue
docker-compose up --build
```

### Stop

```bash
docker compose down -v
```

### Test Message Queue

# Welcome email

curl -X POST http://localhost:3000/send-email \
 -H "Content-Type: application/json" \
 -d '{
"to": "user@example.com",
"subject": "Welcome to our service",
"body": "Thank you for signing up!",
"type": "welcome"
}'

# Password reset

curl -X POST http://localhost:3000/send-email \
 -H "Content-Type: application/json" \
 -d '{
"to": "john@example.com",
"subject": "Reset your password",
"body": "Click here to reset password",
"type": "password_reset"
}'

# Newsletter

curl -X POST http://localhost:3000/send-email \
 -H "Content-Type: application/json" \
 -d '{
"to": "subscribers@example.com",
"subject": "Monthly Newsletter",
"body": "Here are the latest updates",
"type": "newsletter"
}'

# Valid email - Success

curl -X POST http://localhost:3000/send-email \
 -H "Content-Type: application/json" \
 -d '{
"to": "user@gmail.com",
"subject": "Valid Email",
"body": "This will succeed"
}'

# Invalid format - Fail → DLQ

curl -X POST http://localhost:3000/send-email \
 -H "Content-Type: application/json" \
 -d '{
"to": "invalid-email-format",
"subject": "Invalid Email",
"body": "This will fail"
}'

# Blocked domain - Fail → DLQ

curl -X POST http://localhost:3000/send-email \
 -H "Content-Type: application/json" \
 -d '{
"to": "user@spam.com",
"subject": "Blocked Domain",
"body": "This will fail"
}'

```

---

## 2. Dịch vụ Xác thực JWT (OAuth2 Resource Server)

Ứng dụng Spring Boot triển khai xác thực JWT với mã hóa RSA.
Hỗ trợ Access Token (15 phút) và Refresh Token (30 ngày).

```

┌──────────┐ Đăng nhập ┌──────────┐
│ Client │ ───────────────> │ Auth │
│ │ <─────────────── │ Server │
└──────────┘ Access+Refresh └──────────┘
│ │
│ Truy cập Tài nguyên Bảo mật│
├─────────────────────────────┤
│ Bearer Token trong Header │
│ │
│ Token hết hạn? Làm mới │
└─────────────────────────────┘

````
# Ứng dụng chạy tại http://localhost:8080
````

### Tài khoản Demo

| Tên đăng nhập | Mật khẩu   | Vai trò       |
| ------------- | ---------- | ------------- |
| user          | password   | USER          |
| admin         | admin123   | USER, ADMIN   |
| manager       | manager123 | USER, MANAGER |

### Các API Endpoint

#### 1. Endpoint Công khai (Không cần xác thực)

```bash
# Chào công khai
curl http://localhost:8080/api/public/hello
```

#### 2. Đăng nhập (Lấy Token)

```bash
# Đăng nhập với tài khoản User
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user",
    "password": "password"
  }'

# Kết quả trả về:
# {
#   "accessToken": "eyJhbGci...",
#   "refreshToken": "eyJhbGci...",
#   "tokenType": "Bearer",
#   "expiresIn": 900000
# }

# Đăng nhập với tài khoản Admin
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

#### 3. Truy cập Tài nguyên Bảo mật

```bash
# Hồ sơ người dùng (Yêu cầu vai trò USER hoặc ADMIN)
curl http://localhost:8080/api/user/profile \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"

# Bảng điều khiển User (Yêu cầu vai trò USER)
curl http://localhost:8080/api/user/dashboard \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"

# Bảng điều khiển Admin (Yêu cầu vai trò ADMIN)
curl http://localhost:8080/api/admin/dashboard \
  -H "Authorization: Bearer ADMIN_ACCESS_TOKEN"

# Danh sách người dùng Admin (Yêu cầu vai trò ADMIN)
curl http://localhost:8080/api/admin/users \
  -H "Authorization: Bearer ADMIN_ACCESS_TOKEN"

# Bất kỳ tài nguyên bảo mật nào
curl http://localhost:8080/api/protected \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

#### 4. Làm mới Access Token

```bash
# Khi access token hết hạn, dùng refresh token để lấy token mới
curl -X POST http://localhost:8080/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "YOUR_REFRESH_TOKEN"
  }'

# Kết quả:
# {
#   "accessToken": "new_eyJhbGci...",
#   "tokenType": "Bearer",
#   "expiresIn": 900000
# }
```

#### 5. Xác thực Token

```bash
# Kiểm tra token có hợp lệ không
curl -X POST "http://localhost:8080/auth/validate?token=YOUR_TOKEN"

# Kết quả:
# {
#   "valid": true,
#   "username": "user",
#   "type": "access"
# }
```


