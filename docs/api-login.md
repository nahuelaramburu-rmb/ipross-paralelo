# 🔐 API de Autenticación - Identity Service

## Base URL
```
http://168.181.187.5:81/identity-service/v1
```

---

## 📍 POST /auth/login

Autentica a un usuario con email y contraseña.

### Endpoint
```
POST http://168.181.187.5:81/identity-service/v1/auth/login
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
    "status": "success",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
        "id": "12345",
        "email": "miguel@gmail.com",
        "nombre": "Miguel García",
        "numero_afiliado": "02-12345/01",
        "_links": {
            "self": {
                "href": "/api/beneficiaries/12345"
            },
            "plans": {
                "href": "/api/beneficiaries/12345/plans"
            },
            "charges": {
                "href": "/api/beneficiaries/12345/charges"
            }
        }
    }
}
```

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
            `${API_BASE_URL}/auth/login`,
            {
                email: email,
                password: password,
            }
        );
        
        return response.data;
    } catch (error) {
        console.error('Error de login:', error);
        throw error;
    }
};
```
