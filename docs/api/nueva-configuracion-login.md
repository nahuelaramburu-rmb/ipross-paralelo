# API IPROSS - Documentación de Endpoints (Actualizada)

## 🌐 URL Base de Producción
```
BASE_URL = "https://backend-ipross-production.up.railway.app"
```

## 🔐 Autenticación

### 1. Login de Usuario
**Endpoint:** `POST /identity-service/v1/auth/login`

**URL Completa:** `https://backend-ipross-production.up.railway.app/identity-service/v1/auth/login`

**Request:**
```json
{
  "idNumber": "35467201",
  "password": "Ramones162"
}
```

**Headers:**
```
Content-Type: application/json
```

**Response exitoso (200):**
```json
{
  "message": {
    "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

**Response error (401):**
```json
{
  "status": "error",
  "message": "Credenciales inválidas"
}
```

### 2. Renovar Token (Refresh)
**Endpoint:** `POST /identity-service/v1/auth/refresh`

**URL Completa:** `https://backend-ipross-production.up.railway.app/identity-service/v1/auth/refresh`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer {refresh_token}
```

**Response exitoso (200):**
```json
{
  "message": {
    "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

## 👤 Beneficiarios

### 3. Obtener Datos del Beneficiario Autenticado
**Endpoint:** `GET /validation-api/v1/beneficiaries/auth`

**URL Completa:** `https://backend-ipross-production.up.railway.app/validation-api/v1/beneficiaries/auth`

**Headers:**
```
Authorization: Bearer {access_token}
```

**Response exitoso (200):**
```json
{
  "id": "6",
  "idNumber": "35467201",
  "name": "patricio",
  "lastName": "aguirre",
  "deleted": false,
  "user": {
    "username": "patricio",
    "email": "patricionguirre@gmail.com",
    "role": "BENEFICIARY",
    "tenant": "default"
  }
}
```

## 🚀 Ejemplos de Uso

### JavaScript/Fetch
```javascript
const BASE_URL = "https://backend-ipross-production.up.railway.app";

// 1. Login
const loginResponse = await fetch(`${BASE_URL}/identity-service/v1/auth/login`, {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    idNumber: "35467201",
    password: "Ramones162"
  })
});

const loginData = await loginResponse.json();
const accessToken = loginData.message.access_token;

// 2. Obtener beneficiario
const beneficiaryResponse = await fetch(`${BASE_URL}/validation-api/v1/beneficiaries/auth`, {
  headers: {
    'Authorization': `Bearer ${accessToken}`
  }
});

const beneficiaryData = await beneficiaryResponse.json();
console.log(beneficiaryData);
```

### cURL
```bash
# Login
curl -X POST "https://backend-ipross-production.up.railway.app/identity-service/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"idNumber":"35467201","password":"Ramones162"}'

# Beneficiario
curl -X GET "https://backend-ipross-production.up.railway.app/validation-api/v1/beneficiaries/auth" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### Axios
```javascript
import axios from 'axios';

const api = axios.create({
  baseURL: 'https://backend-ipross-production.up.railway.app'
});

// Login
const login = async () => {
  const response = await api.post('/identity-service/v1/auth/login', {
    idNumber: "35467201",
    password: "Ramones162"
  });
  
  return response.data.message.access_token;
};

// Beneficiario
const getBeneficiary = async (token) => {
  const response = await api.get('/validation-api/v1/beneficiaries/auth', {
    headers: {
      Authorization: `Bearer ${token}`
    }
  });
  
  return response.data;
};
```

## 🔧 Códigos de Estado HTTP

| Código | Descripción |
|--------|-------------|
| 200 | Exitoso |
| 400 | Solicitud inválida (faltan campos) |
| 401 | No autorizado (credenciales/token inválido) |
| 404 | Recurso no encontrado |
| 500 | Error interno del servidor |

## 🔑 Información de Tokens JWT

- **Access Token**: Válido por 1 hora
- **Refresh Token**: Válido por 7 días
- **Algoritmo**: HS256
- **Payload del Access Token**:
  ```json
  {
    "sub": "3662",
    "username": "patricio",
    "email": "patricionguirre@gmail.com",
    "profile_id": "6",
    "tenant": "default",
    "iat": 1762807891,
    "exp": 1762811491
  }
  ```

## 🌡️ Health Check

### Verificar Estado del Servidor
**Endpoint:** `GET /`

**URL Completa:** `https://backend-ipross-production.up.railway.app/`

**Response:**
```json
{
  "message": "API IPROSS funcionando",
  "port": "8080",
  "timestamp": "2025-11-10T20:48:56.718Z",
  "environment": "development"
}
```

## 🛡️ Seguridad

- Todas las contraseñas se comparan con bcrypt (hash seguro)
- JWT firmados con secreto seguro
- CORS habilitado para requests cross-origin
- Conexión a base de datos con pool limitado (10 conexiones máximo)
- Validación de entrada en todos los endpoints

## 🔄 Flujo Completo de Autenticación

1. **Frontend** → `POST /identity-service/v1/auth/login` con credenciales
2. **Backend** → Valida contra BD PostgreSQL y retorna tokens JWT
3. **Frontend** → Usa `access_token` en header `Authorization: Bearer {token}`
4. **Backend** → Valida JWT y retorna datos del usuario
5. **Frontend** → Cuando expira, usa `refresh_token` para obtener nuevos tokens

## 📱 Cambios Implementados en la App Mobile

### 1. Archivo `.env`
```diff
- API_BASE_URL=https://favourites-boost-remind-lid.trycloudflare.com
+ API_BASE_URL=https://backend-ipross-production.up.railway.app
```

### 2. Archivo `app/configs/api.config.js`
```javascript
// URL base del servidor (Railway Production)
const BASE_URL = 'https://backend-ipross-production.up.railway.app';

export const API_CONFIG = {
  IDENTITY_SERVICE: {
    BASE_URL: `${BASE_URL}/identity-service/v1`,
    ENDPOINTS: {
      LOGIN: '/auth/login',
      REFRESH: '/auth/refresh',
    }
  },
  VALIDATION_API: {
    BASE_URL: `${BASE_URL}/validation-api/v1`,
    ENDPOINTS: {
      BENEFICIARY_AUTH: '/beneficiaries/auth',
      AUTHORIZATIONS: '/authorizations',
      PROCEDURES: '/procedures',
      RELATIVES: (id) => `/beneficiaries/${id}/relatives`,
    }
  },
};
```

### 3. Archivo `app/lib/authentication.js`

**Credenciales actualizadas:**
```javascript
export const FALLBACK_DATA = {
  USER: {
    idNumber: 35467201,
    password: 'Ramones162',
    // ... datos del beneficiario
  }
};
```

**Login implementado:**
```javascript
// POST /identity-service/v1/auth/login
const response = await fetch(API_CONFIG.IDENTITY_SERVICE.BASE_URL + API_CONFIG.IDENTITY_SERVICE.ENDPOINTS.LOGIN, {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    idNumber: credentials.username,
    password: credentials.password,
  }),
});
```

**Refresh Token implementado:**
```javascript
// POST /identity-service/v1/auth/refresh
const response = await fetch(API_CONFIG.IDENTITY_SERVICE.BASE_URL + API_CONFIG.IDENTITY_SERVICE.ENDPOINTS.REFRESH, {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    Authorization: `Bearer ${refreshToken}`,
  },
});
```

**Get User Data implementado:**
```javascript
// GET /validation-api/v1/beneficiaries/auth
const response = await fetch(API_CONFIG.VALIDATION_API.BASE_URL + API_CONFIG.VALIDATION_API.ENDPOINTS.BENEFICIARY_AUTH, {
  method: 'GET',
  headers: {
    Authorization: token, // Bearer {access_token}
  },
});
```

## ✅ Estados de Implementación

| Endpoint | Estado | Descripción |
|----------|--------|-------------|
| `POST /identity-service/v1/auth/login` | ✅ Implementado | Login con idNumber/password |
| `POST /identity-service/v1/auth/refresh` | ✅ Implementado | Renovación de tokens |
| `GET /validation-api/v1/beneficiaries/auth` | ✅ Implementado | Datos del beneficiario autenticado |
| `GET /validation-api/v1/authorizations` | ⏳ Pendiente | Autorizations médicas |
| `GET /validation-api/v1/procedures` | ⏳ Pendiente | Procedimientos |

## 🧪 Credenciales de Prueba

```
DNI: 35467201
Password: Ramones162
```

**Datos que retorna:**
- ID: 6
- Nombre: patricio aguirre
- Email: patricionguirre@gmail.com
- Role: BENEFICIARY
