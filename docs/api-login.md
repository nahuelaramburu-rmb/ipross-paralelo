# 🔐 API de Autenticación - Identity Service

## Base URL
```
https://168.181.187.5:882/identity-service/v1
```

---

## 📍 POST /auth/login

Autentica a un usuario con email y contraseña.

### Endpoint
```
POST https://168.181.187.5:882/identity-service/v1/auth/login
```

### Headers
```
Content-Type: application/json
```

### Request Body
```json
{
    "email": "miguel@gmail.com",
    "password": "Password123"
}
```

### Response Success (200 OK)
```json
{
  "message": {
    "access_token": "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiQkVORUZJQ0lBUlkiLCJvcGVyYXRpb25zIjpbImJhdGNoZXM6cmVhZCIsImJlbmVmaWNpYXJpZXM6cmVhZCIsImJlbmVmaWNpYXJ5X2J1ZGdldHM6cmVhZCIsImRldmljZXM6Y3JlYXRlIiwiZGV2aWNlczp1cGRhdGUiLCJmaWxlczpyZWFkIiwibWVkaWNhbF9hdXRob3JpemF0aW9uczpyZWFkIiwibm90aWZpY2F0aW9uczpyZWFkIiwibm90aWZpY2F0aW9uczp1cGRhdGUiLCJwcmVfYXV0aG9yaXphdGlvbnM6cmVhZCIsInByZXNjcmlwdGlvbnM6cmVhZCIsInByb2NlZHVyZXM6Y3JlYXRlIiwicHJvY2VkdXJlczpyZWFkIiwicmVwb3J0czpyZWFkIl0sInRva2VuIHR5cGUiOiIiLCJzdWIiOiJtaWd1ZWxAZ21haWwuY29tIiwiaWF0IjoxNzYwNDY3MDE5LCJleHAiOjE3NjA0NzA2MTl9.1QQmuNcAAn2nev2HWYOtwe0i1Xjkedmx8tMfiiVWhAc",
    "refresh_token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtaWd1ZWxAZ21haWwuY29tIiwiaWF0IjoxNzYwNDY3MDE5LCJleHAiOjE3NjA0ODg2MTl9.BJhnddR4_TZdh6lsn8muUu9m7wktopuOIZS9a31WgpU"
  }
}
```

**Nota:** El token JWT incluye información sobre:
- `role`: "BENEFICIARY"
- `operations`: Permisos del usuario (lectura de beneficiarios, notificaciones, etc.)
- `sub`: Email del usuario (miguel@gmail.com)
- `iat`: Timestamp de emisión
- `exp`: Timestamp de expiración (1 hora para access_token, 6 horas para refresh_token)

### Response Error (401 Unauthorized)
```json
{
    "status": "error",
    "message": "Credenciales inválidas"
}
```

### Response Error (404 Not Found)
```json
{
    "status": "error",
    "message": "Usuario no encontrado"
}
```

---

## Notas de Implementación

- **Campo de autenticación**: Se cambió de `numero_afiliado` a `email`
- **Variable de entorno**: `API_BASE_URL` definida en `.env`
- **Token JWT**: Se incluye en las respuestas exitosas para autenticación posterior
- **Links HATEOAS**: El backend proporciona URLs dinámicas para recursos relacionados

---

## Ejemplo de uso en React Native

```javascript
import axios from 'axios';
import { API_BASE_URL } from '@env';

const login = async (email, password) => {
    try {
        const response = await axios.post(
            `${API_BASE_URL}/identity-service/v1/auth/login`,
            {
                email: email,
                password: password,
            }
        );
        
        // El backend retorna { message: { access_token, refresh_token } }
        const { access_token, refresh_token } = response.data.message;
        
        return {
            access_token,
            refresh_token
        };
    } catch (error) {
        console.error('Error de login:', error);
        throw error;
    }
};
```
