# 🔐 Endpoint de Login - Identity Service

## POST `/identity-service/v1/auth/login`

Endpoint para autenticación de usuarios en la aplicación móvil de IPROSS.

---

## 📡 Request

### URL Completa
```
http://168.181.187.5:81/identity-service/v1/auth/login
```

### Método
```
POST
```

### Headers
```json
{
  "Content-Type": "application/json"
}
```

### Body
```json
{
  "email": "miguel@gmail.com",
  "password": "Password123"
}
```

#### Parámetros del Body

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `email` | string | ✅ | Email del usuario registrado |
| `password` | string | ✅ | Contraseña del usuario |

---

## 📥 Response

### Success (200 OK)

```json
{
  "message": {
    "access_token": "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiQkVORUZJQ0lBUlkiLCJvcGVyYXRpb25zIjpbImJhdGNoZXM6cmVhZCIsImJlbmVmaWNpYXJpZXM6cmVhZCIsImJlbmVmaWNpYXJ5X2J1ZGdldHM6cmVhZCIsImRldmljZXM6Y3JlYXRlIiwiZGV2aWNlczp1cGRhdGUiLCJmaWxlczpyZWFkIiwibWVkaWNhbF9hdXRob3JpemF0aW9uczpyZWFkIiwibm90aWZpY2F0aW9uczpyZWFkIiwibm90aWZpY2F0aW9uczp1cGRhdGUiLCJwcmVfYXV0aG9yaXphdGlvbnM6cmVhZCIsInByZXNjcmlwdGlvbnM6cmVhZCIsInByb2NlZHVyZXM6Y3JlYXRlIiwicHJvY2VkdXJlczpyZWFkIiwicmVwb3J0czpyZWFkIl0sInRva2VuIHR5cGUiOiIiLCJzdWIiOiJtaWd1ZWxAZ21haWwuY29tIiwiaWF0IjoxNzYwMjk4NDEzLCJleHAiOjE3NjAzMDIwMTN9.whayLEzhkBY6rFpwMD51qjSAhV2xhtHMCi1MWPJnCqc",
    "refresh_token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtaWd1ZWxAZ21haWwuY29tIiwiaWF0IjoxNzYwMjk4NDEzLCJleHAiOjE3NjAzMjAwMTN9.zR21xWHDr39-TynjwFF-B4oiv3GQZiQjnFXgc3qN1wM"
  }
}
```

#### Estructura de los Tokens JWT

**Access Token** contiene:
- `role`: "BENEFICIARY"
- `operations`: Array de permisos del usuario
- `sub`: Email del usuario
- `iat`: Timestamp de emisión
- `exp`: Timestamp de expiración (1 hora)

**Refresh Token** contiene:
- `sub`: Email del usuario
- `iat`: Timestamp de emisión
- `exp`: Timestamp de expiración (6 horas)
```

### Error (401 Unauthorized)

```json
{
  "status": "error",
  "message": "Credenciales incorrectas"
}
```

### Error (404 Not Found)

```json
{
  "status": "error",
  "message": "Usuario no encontrado"
}
```

### Error (500 Internal Server Error)

```json
{
  "status": "error",
  "message": "Error en el servidor"
}
```

---

## 🔧 Ejemplo de Uso

### cURL
```bash
curl -X POST http://168.181.187.5:81/identity-service/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "miguel@gmail.com",
    "password": "Password123"
  }'
```

### JavaScript (Axios)
```javascript
import axios from 'axios';

const login = async (email, password) => {
  try {
    const response = await axios.post(
      'http://168.181.187.5:81/identity-service/v1/auth/login',
      {
        email: email,
        password: password
      }
    );
    
    console.log('Login exitoso:', response.data);
    return response.data;
  } catch (error) {
    console.error('Error de login:', error.response?.data || error.message);
    throw error;
  }
};
```

---

## 📝 Notas

1. **Cambio de autenticación**: Anteriormente se usaba `numero_afiliado`, ahora se usa `email`
2. **Token JWT**: El token debe guardarse de forma segura y enviarse en requests subsiguientes
3. **Links HATEOAS**: Los `_links` proporcionan URLs dinámicas para otros endpoints
4. **Timeout**: El token tiene una duración de 24 horas
5. **Refresh**: Implementar refresh token para renovar sesión sin re-login

---

## 🔄 Migración desde el endpoint anterior

### Antes (OAuth con numero_afiliado)
```javascript
POST /oauth/token
Body: numero_afiliado=12345&password=pass123&grant_type=password
```

### Ahora (JSON con email)
```javascript
POST /identity-service/v1/auth/login
Body: { "email": "user@email.com", "password": "pass123" }
```

---

**Última actualización**: 12 de octubre de 2025
**Versión API**: v1
**Base URL**: `http://168.181.187.5:81`
