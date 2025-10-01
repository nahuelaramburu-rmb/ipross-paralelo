# 📋 **Documentación de Endpoints - App Beneficiario IPROSS**

## 🔐 **1. Autenticación**

### **POST** `/identity-service/v1/oauth/token`
**Descripción**: Login de usuario con credenciales
```json
// Request
{
  "username": "12345678",
  "password": "contraseña123",
  "grant_type": "password"
}

// Response (200)
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 3600,
  "scope": "read write"
}

// Response Error (401)
{
  "error": "invalid_credentials",
  "error_description": "Las credenciales proporcionadas no son válidas"
}
```

### **POST** `/identity-service/v1/oauth/token`
**Descripción**: Refresh token para renovar sesión
```json
// Request
{
  "grant_type": "refresh_token",
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}

// Response (200)
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 3600
}
```

### **POST** `/identity-service/v1/accounts/forgot-password`
**Descripción**: Solicitar restablecimiento de contraseña
```json
// Request
{
  "username": "12345678",
  "email": "usuario@email.com"
}

// Response (200)
{
  "message": "Se ha enviado un enlace de restablecimiento a su email",
  "success": true
}
```

### **POST** `/identity-service/v1/accounts/register`
**Descripción**: Registro de nuevo usuario
```json
// Request
{
  "username": "12345678",
  "email": "usuario@email.com",
  "password": "contraseña123",
  "firstName": "Juan",
  "lastName": "Pérez",
  "phone": "+5491123456789"
}

// Response (201)
{
  "id": "uuid-usuario",
  "username": "12345678",
  "email": "usuario@email.com",
  "emailVerified": false,
  "message": "Cuenta creada exitosamente. Verifique su email."
}
```

## 👤 **2. Información del Beneficiario**

### **GET** `/identity-service/v1/users/me`
**Descripción**: Obtener datos del usuario autenticado
```json
// Headers
{
  "Authorization": "Bearer {access_token}"
}

// Response (200)
{
  "id": "uuid-usuario",
  "username": "12345678",
  "email": "usuario@email.com",
  "firstName": "Juan",
  "lastName": "Pérez",
  "phone": "+5491123456789",
  "birthDate": "1985-03-15",
  "address": {
    "street": "Av. San Martín 123",
    "city": "Neuquén",
    "province": "Neuquén",
    "zipCode": "8300"
  },
  "plans": [
    {
      "id": 1,
      "name": "Plan Básico",
      "insurancePlanType": {
        "name": "AMBULATORIO"
      },
      "expirationDate": "2024-12-31"
    }
  ],
  "_links": {
    "self": {
      "href": "/api/users/uuid-usuario"
    }
  }
}
```

### **GET** `/identity-service/v1/users/{userId}/relatives`
**Descripción**: Obtener familiares a cargo del usuario
```json
// Headers
{
  "Authorization": "Bearer {access_token}"
}

// Response (200)
{
  "_embedded": {
    "relatives": [
      {
        "id": "uuid-familiar",
        "firstName": "María",
        "lastName": "Pérez",
        "relationship": "CONYUGE",
        "birthDate": "1987-06-20",
        "documentNumber": "87654321",
        "_links": {
          "self": {
            "href": "/api/users/uuid-familiar"
          }
        }
      }
    ]
  }
}
```

## 🏥 **3. Autorizaciones Médicas**

### **GET** `/validation-api/v1/authorizations`
**Descripción**: Obtener lista de autorizaciones médicas
```json
// Query Parameters
// ?beneficiaryId={id}&page=1&size=20&createdAt[gte]=2024-01-01&createdAt[lte]=2024-12-31

// Headers
{
  "Authorization": "Bearer {access_token}"
}

// Response (200)
{
  "_embedded": {
    "authorizations": [
      {
        "id": "uuid-autorizacion",
        "authorizationNumber": "AUT-2024-001",
        "createdAt": "2024-03-15T10:30:00Z",
        "status": "APPROVED",
        "medicalCenter": "Hospital Castro Rendón",
        "professional": "Dr. Juan García",
        "specialty": "Cardiología",
        "procedure": "Consulta Cardiológica",
        "observations": "Control post operatorio",
        "amount": 15000.50,
        "_links": {
          "self": {
            "href": "/api/authorizations/uuid-autorizacion"
          }
        }
      }
    ]
  },
  "_links": {
    "next": {
      "href": "/api/authorizations?page=2&size=20"
    }
  },
  "page": {
    "size": 20,
    "totalElements": 45,
    "totalPages": 3,
    "number": 0
  }
}
```

### **GET** `/validation-api/v1/authorizations/{id}`
**Descripción**: Obtener detalle de una autorización específica
```json
// Headers
{
  "Authorization": "Bearer {access_token}"
}

// Response (200)
{
  "id": "uuid-autorizacion",
  "authorizationNumber": "AUT-2024-001",
  "createdAt": "2024-03-15T10:30:00Z",
  "status": "APPROVED",
  "medicalCenter": {
    "name": "Hospital Castro Rendón",
    "address": "Buenos Aires 450, Neuquén"
  },
  "professional": {
    "name": "Dr. Juan García",
    "specialtyType": "Cardiología",
    "license": "12345"
  },
  "procedure": "Consulta Cardiológica",
  "observations": "Control post operatorio",
  "amount": 15000.50,
  "beneficiary": {
    "firstName": "Juan",
    "lastName": "Pérez",
    "documentNumber": "12345678"
  }
}
```

### **POST** `/validation-api/v1/authorizations/{id}/rate`
**Descripción**: Calificar una autorización médica
```json
// Request
{
  "rating": 5,
  "comment": "Excelente atención médica"
}

// Response (200)
{
  "message": "Calificación registrada exitosamente",
  "success": true
}
```

## 💊 **4. Recetas Médicas**

### **GET** `/validation-api/v1/prescriptions`
**Descripción**: Obtener lista de recetas médicas
```json
// Query Parameters
// ?beneficiaryId={id}&page=1&size=20&createdAt[gte]=2024-01-01

// Headers
{
  "Authorization": "Bearer {access_token}"
}

// Response (200)
{
  "_embedded": {
    "prescriptions": [
      {
        "id": "uuid-receta",
        "prescriptionNumber": "REC-2024-001",
        "createdAt": "2024-03-15T14:20:00Z",
        "status": "DISPENSED",
        "pharmacy": "Farmacia Central",
        "professional": "Dr. Ana López",
        "medications": [
          {
            "name": "Ibuprofeno 600mg",
            "quantity": 30,
            "instructions": "Tomar 1 cada 8 horas"
          }
        ],
        "totalAmount": 8500.00
      }
    ]
  }
}
```

## 📋 **5. Trámites**

### **GET** `/validation-api/v1/procedures`
**Descripción**: Obtener lista de trámites
```json
// Query Parameters
// ?page=1&size=20&groups=opened-status:{name=EN REVISION},closed-status!{name=EN REVISION}

// Headers
{
  "Authorization": "Bearer {access_token}"
}

// Response (200)
{
  "_embedded": {
    "procedures": [
      {
        "id": "uuid-tramite",
        "type": "CUDProcedure",
        "title": "Solicitud CUD",
        "status": "EN_REVISION",
        "createdAt": "2024-03-10T09:00:00Z",
        "lastUpdate": "2024-03-15T16:30:00Z",
        "description": "Solicitud de Certificado Único de Discapacidad"
      }
    ]
  }
}
```

### **POST** `/validation-api/v1/procedures`
**Descripción**: Crear nuevo trámite
```json
// Request
{
  "type": "CertificateProcedure",
  "certificateTypeId": 1,
  "description": "Solicitud de certificado médico",
  "attachments": [
    {
      "fileName": "estudios.pdf",
      "fileData": "base64encodeddata..."
    }
  ]
}

// Response (201)
{
  "id": "uuid-tramite",
  "type": "CertificateProcedure",
  "status": "CREATED",
  "createdAt": "2024-03-16T10:00:00Z",
  "message": "Trámite creado exitosamente"
}
```

### **GET** `/validation-api/v1/procedures/{id}/messages`
**Descripción**: Obtener mensajes de un trámite
```json
// Headers
{
  "Authorization": "Bearer {access_token}"
}

// Response (200)
{
  "_embedded": {
    "messages": [
      {
        "id": "uuid-mensaje",
        "content": "Se requiere documentación adicional",
        "sender": "SISTEMA",
        "createdAt": "2024-03-15T11:00:00Z",
        "attachments": []
      }
    ]
  }
}
```

### **POST** `/validation-api/v1/procedures/{id}/messages`
**Descripción**: Agregar mensaje a un trámite
```json
// Request
{
  "content": "Adjunto la documentación solicitada",
  "attachments": [
    {
      "fileName": "documento.pdf",
      "fileData": "base64encodeddata..."
    }
  ]
}

// Response (201)
{
  "message": "Mensaje agregado exitosamente",
  "success": true
}
```

### **GET** `/validation-api/v1/general/certificate-types`
**Descripción**: Obtener tipos de certificados disponibles
```json
// Headers
{
  "Authorization": "Bearer {access_token}"
}

// Response (200)
{
  "_embedded": {
    "certificateTypes": [
      {
        "id": 1,
        "name": "Certificado Médico",
        "description": "Certificado médico general"
      },
      {
        "id": 2,
        "name": "Certificado de Aptitud Física",
        "description": "Certificado para actividades deportivas"
      }
    ]
  }
}
```

## 🏥 **6. Profesionales**

### **GET** `/validation-api/v1/general/medical-specialty-types`
**Descripción**: Obtener tipos de especialidades médicas
```json
// Headers
{
  "Authorization": "Bearer {access_token}"
}

// Response (200)
{
  "_embedded": {
    "specialtyTypes": [
      {
        "id": 1,
        "name": "CLINICAS",
        "description": "Especialidades Clínicas"
      },
      {
        "id": 2,
        "name": "QUIRURGICAS",
        "description": "Especialidades Quirúrgicas"
      }
    ]
  }
}
```

### **GET** `/validation-api/v1/general/medical-specialty-types/{id}`
**Descripción**: Obtener especialidades de un tipo específico
```json
// Headers
{
  "Authorization": "Bearer {access_token}"
}

// Response (200)
{
  "id": 1,
  "name": "CLINICAS",
  "specialties": [
    {
      "id": 1,
      "name": "Cardiología"
    },
    {
      "id": 2,
      "name": "Dermatología"
    }
  ]
}
```

### **GET** `/validation-api/v1/professionals`
**Descripción**: Buscar profesionales
```json
// Query Parameters
// ?specialtyId={id}&townId={id}&page=1&size=20

// Headers
{
  "Authorization": "Bearer {access_token}"
}

// Response (200)
{
  "_embedded": {
    "professionals": [
      {
        "id": "uuid-profesional",
        "firstName": "Juan",
        "lastName": "García",
        "specialty": "Cardiología",
        "license": "12345",
        "medicalCenter": {
          "name": "Hospital Castro Rendón",
          "address": "Buenos Aires 450",
          "phone": "0299-4444444"
        },
        "schedule": {
          "monday": "08:00-12:00",
          "tuesday": "14:00-18:00"
        }
      }
    ]
  }
}
```

### **GET** `/validation-api/v1/regions/provinces/15`
**Descripción**: Obtener localidades de Neuquén
```json
// Headers
{
  "Authorization": "Bearer {access_token}"
}

// Response (200)
{
  "id": 15,
  "name": "Neuquén",
  "towns": [
    {
      "id": 1,
      "name": "Neuquén Capital"
    },
    {
      "id": 2,
      "name": "Plottier"
    }
  ]
}
```

## 📅 **7. Turnos**

### **POST** `/api-turnos/apiv1/login`
**Descripción**: Login específico para sistema de turnos
```json
// Request
{
  "username": "api_user",
  "password": "api_password"
}

// Response (200)
{
  "access_token": "token_turnos",
  "token_type": "Bearer",
  "expires_in": 3600
}
```

### **GET** `/api-turnos/apiv1/applicants/search`
**Descripción**: Buscar solicitante de turnos
```json
// Query Parameters
// ?documento={documento}

// Headers
{
  "Authorization": "Bearer {token_turnos}"
}

// Response (200)
{
  "id": "uuid-solicitante",
  "documento": "12345678",
  "apellidos": "Pérez",
  "nombres": "Juan"
}
```

### **POST** `/api-turnos/apiv1/applicants`
**Descripción**: Registrar nuevo solicitante
```json
// Request
{
  "documento": "12345678",
  "apellidos": "Pérez",
  "nombres": "Juan",
  "telefono": "2994444444",
  "email": "juan@email.com"
}

// Response (201)
{
  "id": "uuid-solicitante",
  "message": "Solicitante registrado exitosamente"
}
```

### **GET** `/api-turnos/apiv1/appointments`
**Descripción**: Obtener turnos del usuario
```json
// Query Parameters
// ?applicantId={id}&from=2024-01-01&to=2024-12-31

// Headers
{
  "Authorization": "Bearer {token_turnos}"
}

// Response (200)
{
  "_embedded": {
    "appointments": [
      {
        "id": "uuid-turno",
        "date": "2024-03-20",
        "time": "10:30",
        "professional": "Dr. García",
        "specialty": "Cardiología",
        "location": "Hospital Castro Rendón",
        "status": "CONFIRMED"
      }
    ]
  }
}
```

### **POST** `/api-turnos/apiv1/appointments`
**Descripción**: Crear nuevo turno
```json
// Request
{
  "applicantId": "uuid-solicitante",
  "professionalId": "uuid-profesional",
  "date": "2024-03-25",
  "time": "15:00",
  "observations": "Primera consulta"
}

// Response (201)
{
  "id": "uuid-turno",
  "confirmationCode": "ABC123",
  "message": "Turno creado exitosamente"
}
```

### **GET** `/api-turnos/apiv1/delegations`
**Descripción**: Obtener delegaciones disponibles
```json
// Headers
{
  "Authorization": "Bearer {token_turnos}"
}

// Response (200)
{
  "_embedded": {
    "delegations": [
      {
        "id": 1,
        "name": "Delegación Neuquén",
        "address": "Av. Argentina 123"
      }
    ]
  }
}
```

### **GET** `/api-turnos/apiv1/sectors`
**Descripción**: Obtener sectores de una delegación
```json
// Query Parameters
// ?delegationId={id}

// Headers
{
  "Authorization": "Bearer {token_turnos}"
}

// Response (200)
{
  "_embedded": {
    "sectors": [
      {
        "id": 1,
        "name": "Cardiología",
        "delegationId": 1
      }
    ]
  }
}
```

### **GET** `/api-turnos/apiv1/appointments/enabled`
**Descripción**: Verificar si se pueden crear turnos
```json
// Headers
{
  "Authorization": "Bearer {token_turnos}"
}

// Response (200)
{
  "enabled": true,
  "message": "Sistema de turnos habilitado"
}
```

## 💰 **8. Coseguros**

### **GET** `/validation-api/v1/coinsurance-charges`
**Descripción**: Obtener lista de coseguros
```json
// Query Parameters
// ?beneficiaryId={id}&page=1&size=20&date[gte]=2024-01-01

// Headers
{
  "Authorization": "Bearer {access_token}"
}

// Response (200)
{
  "_embedded": {
    "charges": [
      {
        "id": "uuid-coseguro",
        "date": "2024-03-15",
        "amount": 5000.00,
        "concept": "Consulta médica",
        "professional": "Dr. García",
        "status": "PENDING",
        "dueDate": "2024-04-15"
      }
    ]
  }
}
```

## 🩺 **9. Discapacidad (CUD)**

### **GET** `/validation-api/v1/batches`
**Descripción**: Obtener lotes de CUD
```json
// Headers
{
  "Authorization": "Bearer {access_token}"
}

// Response (200)
{
  "_embedded": {
    "batches": [
      {
        "id": "uuid-lote",
        "batchNumber": "LOTE-2024-001",
        "createdAt": "2024-03-10T10:00:00Z",
        "status": "IN_PROCESS",
        "itemsCount": 5
      }
    ]
  }
}
```

### **GET** `/validation-api/v1/batches/{id}`
**Descripción**: Obtener detalle de un lote CUD
```json
// Headers
{
  "Authorization": "Bearer {access_token}"
}

// Response (200)
{
  "id": "uuid-lote",
  "batchNumber": "LOTE-2024-001",
  "createdAt": "2024-03-10T10:00:00Z",
  "status": "IN_PROCESS",
  "items": [
    {
      "id": "uuid-item",
      "description": "Evaluación médica",
      "status": "PENDING",
      "professional": "Dr. López"
    }
  ]
}
```

## 🔧 **10. Utilidades**

### **GET** `/validation-api/v1/storage/reports`
**Descripción**: Obtener archivos asociados a autorizaciones
```json
// Query Parameters
// ?authorizationId={id}

// Headers
{
  "Authorization": "Bearer {access_token}"
}

// Response (200)
{
  "_embedded": {
    "files": [
      {
        "id": "uuid-archivo",
        "fileName": "reporte_medico.pdf",
        "fileUrl": "https://storage.ipross.com/files/uuid-archivo",
        "uploadDate": "2024-03-15T10:00:00Z"
      }
    ]
  }
}
```

### **GET** `/validation-api/v1/preauthorizations`
**Descripción**: Obtener pre-autorizaciones
```json
// Query Parameters
// ?beneficiaryId={id}&page=1&size=20

// Headers
{
  "Authorization": "Bearer {access_token}"
}

// Response (200)
{
  "_embedded": {
    "preauthorizations": [
      {
        "id": "uuid-preaut",
        "preAuthorizationNumber": "PRE-2024-001",
        "createdAt": "2024-03-15T10:00:00Z",
        "status": "PENDING",
        "procedure": "Resonancia Magnética",
        "professional": "Dr. García"
      }
    ]
  }
}
```

### **GET** `/validation-api/v1/preauthorizations/{id}`
**Descripción**: Obtener detalle de pre-autorización
```json
// Headers
{
  "Authorization": "Bearer {access_token}"
}

// Response (200)
{
  "id": "uuid-preaut",
  "preAuthorizationNumber": "PRE-2024-001",
  "createdAt": "2024-03-15T10:00:00Z",
  "status": "PENDING",
  "procedure": "Resonancia Magnética",
  "professional": {
    "name": "Dr. García",
    "specialty": "Radiología"
  },
  "medicalCenter": "Centro de Diagnóstico",
  "observations": "Estudio solicitado por traumatología"
}
```

## 📱 **11. Tokens y QR**

### **POST** `/identity-service/v1/users/generate-token`
**Descripción**: Generar token OTP
```json
// Headers
{
  "Authorization": "Bearer {access_token}"
}

// Response (200)
{
  "token": "123456",
  "expiresAt": "2024-03-16T11:05:00Z",
  "qrCode": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA..."
}
```

### **GET** `/identity-service/v1/users/generate-qr`
**Descripción**: Generar código QR con datos del beneficiario
```json
// Headers
{
  "Authorization": "Bearer {access_token}"
}

// Response (200)
{
  "qrData": "encrypted_beneficiary_data",
  "qrImage": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...",
  "expiresAt": "2024-03-16T11:00:00Z"
}
```

## 🔔 **12. Notificaciones**

### **POST** `/notification-service/v1/devices/register`
**Descripción**: Registrar dispositivo para notificaciones push
```json
// Request
{
  "deviceToken": "firebase_device_token",
  "platform": "android",
  "userId": "uuid-usuario"
}

// Response (200)
{
  "message": "Dispositivo registrado exitosamente",
  "success": true
}
```

### **GET** `/notification-service/v1/notifications`
**Descripción**: Obtener notificaciones del usuario
```json
// Headers
{
  "Authorization": "Bearer {access_token}"
}

// Response (200)
{
  "_embedded": {
    "notifications": [
      {
        "id": "uuid-notif",
        "title": "Nueva autorización",
        "message": "Su autorización ha sido aprobada",
        "createdAt": "2024-03-15T10:00:00Z",
        "read": false,
        "type": "AUTHORIZATION"
      }
    ]
  }
}
```

---

## 🔒 **Autenticación General**

Todos los endpoints (excepto login, registro y forgot-password) requieren el header:
```
Authorization: Bearer {access_token}
```

## 📄 **Códigos de Error Comunes**

- **400**: Bad Request - Datos de entrada inválidos
- **401**: Unauthorized - Token inválido o expirado
- **403**: Forbidden - Sin permisos para acceder al recurso
- **404**: Not Found - Recurso no encontrado
- **422**: Unprocessable Entity - Datos de entrada con formato incorrecto
- **429**: Too Many Requests - Límite de requests excedido
- **500**: Internal Server Error - Error interno del servidor

## 🌐 **URLs Base**

- **Identity Service**: `https://{host}/identity-service/v1/`
- **Validation API**: `https://{host}/validation-api/v1/`
- **Notification Service**: `https://{host}/notification-service/v1/`
- **Report Server**: `http://{host}/report-server/v1/`
- **API Turnos**: `https://{turnos_host}`

## 📋 **Notas Importantes**

1. **Paginación**: Los endpoints que retornan listas implementan paginación con los parámetros `page` y `size`
2. **Filtros**: Muchos endpoints soportan filtros por fecha usando el formato `field[gte]` y `field[lte]`
3. **HATEOAS**: Las respuestas incluyen enlaces de navegación en el campo `_links`
4. **Formato de Fechas**: Todas las fechas se manejan en formato ISO 8601 (UTC)
5. **Archivos**: Los archivos se envían codificados en base64 en el campo `fileData`
6. **Tokens**: Los tokens de acceso tienen una duración limitada y deben renovarse usando el refresh token

---

**Última actualización**: Octubre 2025  
**Versión de API**: v1  
**Contacto**: Equipo de desarrollo IPROSS