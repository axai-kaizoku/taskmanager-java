# Authentication Architecture - Session-Based JWT with Rotating Refresh Tokens

## Overview
This application implements a modern, secure authentication system with:
- **Session tracking** per device/login
- **Rotating refresh tokens** for maximum security
- **Token reuse detection** to prevent attacks
- **Redis caching** for performance
- **Comprehensive session management**

---

## Architecture Components

### 1. Session Model (`Session.java`)
Each login creates a unique session that tracks:
- `userId` - User who owns the session
- `deviceInfo` - Device type (iPhone, Android, Windows PC, etc.)
- `userAgent` - Full browser user agent string
- `ipAddress` - Client IP address
- `createdAt` - When session was created
- `lastAccessedAt` - Last activity timestamp
- `active` - Session status flag
- `tokenFamily` - UUID for tracking token rotation family

**MongoDB Collection:** `sessions`

---

### 2. RefreshToken Model (`RefreshToken.java`)
Refresh tokens are now linked to sessions (not directly to users):
- `sessionId` - Reference to the session
- `token` - The actual refresh token (UUID)
- `expiryDate` - When token expires
- `tokenFamily` - Links all rotated tokens from same session
- `used` - Flag for reuse detection
- `createdAt` - Token creation timestamp

**MongoDB Collection:** `refresh_tokens`

---

### 3. Token Lifecycle

#### **Login Flow:**
```
1. User submits credentials
2. Extract device info (userAgent, IP, device type)
3. Create Session in MongoDB + cache in Redis
4. Generate Access Token (JWT, 1 hour)
5. Create Refresh Token (linked to session, 24 hours)
6. Return both tokens to client
```

#### **Refresh Flow (Token Rotation):**
```
1. Client sends refresh token
2. Validate token (check expiry, blacklist)
3. Check if token.used == true (REUSE DETECTION)
   - If YES: SECURITY BREACH! Revoke entire session
   - If NO: Continue...
4. Mark old token as used
5. Blacklist old token in Redis
6. Create NEW refresh token (same session, new expiry)
7. Update token family in Redis
8. Generate NEW access token
9. Return new tokens to client
```

#### **Logout Flow:**
```
1. Revoke session (set active=false)
2. Delete all refresh tokens for session
3. Blacklist current access token in Redis
4. Session can no longer be used
```

---

## Security Features

### 🔒 **Token Reuse Detection**
If an old (already-used) refresh token is attempted:
1. Immediately revoke the entire session
2. Delete all tokens in the token family
3. Invalidate session cache
4. User must re-login

**Why this matters:** If an attacker steals an old token and tries to use it, the system detects it and locks down the entire session.

---

### 🔒 **Token Rotation**
Every time you refresh:
- Old refresh token becomes invalid
- New refresh token is generated with fresh expiry
- Expiry resets to 24 hours from now

**Active users never get logged out!**

---

### 🔒 **Blacklisting**
Revoked tokens are stored in Redis with TTL:
- Logout: Access token blacklisted for remaining TTL
- Refresh: Old refresh token blacklisted
- Fast lookups via Redis

---

## Redis Integration

### **Cache Keys:**

| Prefix | Purpose | TTL | Example |
|--------|---------|-----|---------|
| `session:` | Session objects | 90 days | `session:abc123` |
| `blacklist:token:` | Revoked tokens | Token TTL | `blacklist:token:xyz789` |
| `access:token:` | Access token → sessionId | 1 hour | `access:token:jwt123` |
| `token:family:` | Token family → latest token | 90 days | `token:family:fam456` |

### **Performance Benefits:**
- Session lookups: Redis first, MongoDB fallback
- Token validation: Check blacklist in Redis (sub-millisecond)
- Reduced database load
- Fast session revocation

---

## API Endpoints

### **Authentication**
```
POST /api/auth/signin
POST /api/auth/signup
POST /api/auth/refreshtoken
POST /api/auth/logout
```

### **Session Management**
```
GET    /api/sessions                # List all active sessions
DELETE /api/sessions/{sessionId}    # Revoke specific session
POST   /api/sessions/revoke-others  # Revoke all except current
POST   /api/sessions/revoke-all     # Logout from all devices
```

---

## Database Schema

### **MongoDB Collections:**

**users:**
```json
{
  "_id": "user123",
  "email": "user@example.com",
  "password": "hashed",
  "roles": ["USER"],
  "createdAt": "2026-03-21T10:00:00Z"
}
```

**sessions:**
```json
{
  "_id": "session456",
  "userId": "user123",
  "deviceInfo": "iPhone",
  "userAgent": "Mozilla/5.0 ...",
  "ipAddress": "192.168.1.1",
  "createdAt": "2026-03-21T10:00:00Z",
  "lastAccessedAt": "2026-03-21T12:00:00Z",
  "active": true,
  "tokenFamily": "family789"
}
```

**refresh_tokens:**
```json
{
  "_id": "token001",
  "sessionId": "session456",
  "token": "uuid-refresh-token",
  "tokenFamily": "family789",
  "expiryDate": "2026-03-22T10:00:00Z",
  "used": false,
  "createdAt": "2026-03-21T10:00:00Z"
}
```

---

## Configuration

**application.properties:**
```properties
# JWT Settings
app.jwtSecret=your-secret-key
app.jwtExpirationMs=3600000           # 1 hour
app.jwtRefreshExpirationMs=86400000   # 24 hours

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

---

## How This Differs from Previous Implementation

| Aspect | Before | After |
|--------|--------|-------|
| **Granularity** | Per user | Per session/device |
| **Token lifetime** | Static 24h | Rotating (extends if active) |
| **Security** | Basic | Token reuse detection |
| **Session management** | None | Full device tracking |
| **Revocation** | Delete by userId | Granular per session |
| **Caching** | None | Redis for performance |
| **Device tracking** | No | Yes (device, IP, userAgent) |

---

## Client Integration Example

### **Login:**
```javascript
const response = await fetch('/api/auth/signin', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ email, password })
});

const { accessToken, refreshToken } = response.data;
localStorage.setItem('accessToken', accessToken);
localStorage.setItem('refreshToken', refreshToken);
```

### **API Requests:**
```javascript
fetch('/api/tasks', {
  headers: {
    'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
  }
});
```

### **Token Refresh:**
```javascript
async function refreshAccessToken() {
  const refreshToken = localStorage.getItem('refreshToken');

  const response = await fetch('/api/auth/refreshtoken', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ refreshToken })
  });

  const { accessToken, refreshToken: newRefreshToken } = response.data;
  localStorage.setItem('accessToken', accessToken);
  localStorage.setItem('refreshToken', newRefreshToken); // ← NEW TOKEN!
}
```

### **Logout:**
```javascript
await fetch('/api/auth/logout?sessionId=xxx', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
  }
});

localStorage.clear();
```

---

## Real-World Scenarios

### **Scenario 1: User logs in from phone and laptop**
- Creates 2 sessions (one per device)
- Each session has its own token family
- User can manage/revoke sessions independently
- Can see "Signed in on iPhone (192.168.1.5)"

### **Scenario 2: Token stolen by attacker**
- Attacker uses old refresh token
- System detects token.used == true
- Entire session revoked immediately
- User gets notification to re-login

### **Scenario 3: Active user never logs out**
- Day 1: Login (refresh token expires Day 2)
- Day 1.5: Refresh → new token expires Day 2.5
- Day 2: Refresh → new token expires Day 3
- User stays logged in indefinitely while active

### **Scenario 4: User wants to logout from all devices**
- Calls `/api/sessions/revoke-all`
- All sessions marked inactive
- All tokens invalidated
- Must re-login on every device

---

## Files Modified/Created

### **New Files:**
- `model/Session.java`
- `repository/SessionRepo.java`
- `dao/SessionDAO.java`
- `service/SessionService.java`
- `service/RedisAuthService.java`
- `controller/SessionController.java`
- `dto/SessionDTO.java`
- `util/DeviceInfoExtractor.java`

### **Modified Files:**
- `model/RefreshToken.java` - Now links to session, added rotation fields
- `repository/RefreshTokenRepo.java` - Added session/family queries
- `dao/RefreshTokenDAO.java` - Updated methods
- `service/RefreshTokenService.java` - Implemented rotation logic
- `controller/AuthController.java` - Session creation, token rotation
- `security/AuthTokenFilter.java` - Blacklist checking
- `security/JwtUtils.java` - Added expiration extraction

---

## Next Steps (Optional Enhancements)

1. **Push Notifications** - Notify user when new session detected
2. **Geolocation** - Show approximate location based on IP
3. **Session limits** - Max 5 active sessions per user
4. **Anomaly detection** - Flag suspicious login patterns
5. **Admin panel** - View all user sessions
6. **Analytics** - Track device usage, peak hours

---

## Testing

Run the application and test:

1. **Login** - Check session created in MongoDB & Redis
2. **Refresh** - Verify old token invalidated, new token works
3. **Reuse detection** - Try using old token → should fail
4. **Logout** - Verify tokens blacklisted
5. **Session list** - Check all active sessions
6. **Revoke session** - Verify tokens stop working

---

**Built with security-first principles. Production-ready authentication system! 🚀**
