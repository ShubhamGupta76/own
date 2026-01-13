# API Gateway Service

## Overview
API Gateway is the single entry point for all DevBlocker microservices. It handles routing, JWT authentication, CORS, and request tracing.

## Port
- **8080** - API Gateway

## Responsibilities
1. **Single Entry Point** - All client requests go through the gateway
2. **Request Routing** - Routes requests to appropriate microservices
3. **JWT Validation** - Centralized JWT token validation
4. **Security** - Rejects invalid/missing JWT tokens
5. **CORS Handling** - Manages cross-origin requests
6. **Request Tracing** - Adds X-Request-Id header for tracing

## Architecture

### Request Flow
```
Client Request
    ↓
API Gateway (Port 8080)
    ↓
RequestLoggingFilter (Adds X-Request-Id)
    ↓
JwtAuthenticationFilter (Validates JWT)
    ↓
Route to Microservice
    ↓
Response back to Client
```

## Routing Configuration

| Gateway Path | Target Service | Port |
|--------------|----------------|------|
| `/auth/**` | Auth Service | 8081 |
| `/users/**` | User Service | 8082 |
| `/teams/**` | Team Service | 8083 |
| `/channels/**` | Channel Service | 8084 |
| `/chat/**` | Chat Service | 8085 |
| `/meetings/**` | Meeting Service | 8086 |
| `/files/**` | File Service | 8087 |
| `/notifications/**` | Notification Service | 8088 |
| `/tasks/**` | Task Service | 8090 |

## Security

### Public Endpoints (No JWT Required)
- `/auth/**` - All authentication endpoints

### Secured Endpoints (JWT Required)
- All other routes require valid JWT token in `Authorization: Bearer <token>` header

### JWT Validation
- Validates token signature
- Checks token expiration
- Extracts claims: `userId`, `email`, `role`, `organizationId`
- Adds user info to request headers for downstream services:
  - `X-User-Id`
  - `X-User-Email`
  - `X-User-Role`
  - `X-Organization-Id`

## CORS Configuration

Allowed Origins:
- `http://localhost:3000` (React)
- `http://localhost:4200` (Angular)

Allowed Headers:
- `Authorization`
- `Content-Type`
- `X-Request-Id`

## Request Tracing

Every request gets a unique `X-Request-Id` header:
- Generated if not present
- Passed to downstream services
- Included in response headers
- Logged for debugging

## Example Usage

### Public Request (No Auth)
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@devblocker.com","password":"Admin@123"}'
```

### Secured Request (With JWT)
```bash
curl -X GET http://localhost:8080/users \
  -H "Authorization: Bearer <jwt_token>"
```

## Configuration

### application.yml
- Route definitions
- CORS settings
- JWT secret (must match Auth Service)
- Logging levels

### JWT Secret
Must match the secret used in Auth Service:
```yaml
jwt:
  secret: DevBlockerSecretKeyForJWTTokenGeneration123456789012345678901234567890
```

## Dependencies

- Spring Cloud Gateway (Reactive)
- Spring Security (WebFlux)
- JWT (jjwt)
- Lombok

## Running the Gateway

1. Ensure all microservices are running
2. Start API Gateway:
   ```bash
   mvn spring-boot:run
   ```
3. Gateway will start on port 8080

## Testing

### Test Public Endpoint
```bash
curl http://localhost:8080/auth/login
```

### Test Secured Endpoint (Should Fail)
```bash
curl http://localhost:8080/users
# Returns 401 Unauthorized
```

### Test with Valid JWT
```bash
# 1. Get JWT token
TOKEN=$(curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@devblocker.com","password":"Admin@123"}' \
  | jq -r '.token')

# 2. Use token
curl http://localhost:8080/users \
  -H "Authorization: Bearer $TOKEN"
```

## Postman Collection Update

Update Postman environment to use gateway:
```json
{
  "gateway_url": "http://localhost:8080"
}
```

Then update all requests to use:
- `{{gateway_url}}/auth/login` instead of `{{auth_url}}/api/auth/login`
- `{{gateway_url}}/users` instead of `{{user_url}}/api/users`
- etc.

## Troubleshooting

### 401 Unauthorized
- Check JWT token is valid
- Verify JWT secret matches Auth Service
- Check token hasn't expired

### 503 Service Unavailable
- Verify target microservice is running
- Check service port is correct
- Review service logs

### CORS Errors
- Verify origin is in allowed list
- Check CORS configuration in application.yml

## Notes

- Gateway does NOT store any data (stateless)
- Gateway does NOT replace service-level security
- Each service still validates JWT independently
- Gateway adds convenience headers for services

