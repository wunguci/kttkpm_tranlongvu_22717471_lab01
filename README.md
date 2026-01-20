# Email Queue Service

Producer nhận email requests qua API và đẩy vào queue.
Consumer lấy từ queue và xử lý ngay lập tức.

## Run
```bash
docker-compose up --build
docker compose down -v

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
