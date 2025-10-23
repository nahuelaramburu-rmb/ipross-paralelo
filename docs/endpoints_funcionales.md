# Documentación de Endpoints para la App IPROSS Beneficiario

## Base URLs
```
Identity Service: https://{HOST}/identity-service/v1/
Validation API: https://{HOST}/validation-api/v1/

```

## 1. Autenticación

### Login
**Endpoint:** `POST /identity-service/v1/auth/login`

**Headers:**
```
Accept: application/json
Content-Type: application/x-www-form-urlencoded

```

**Body (form-data):**
```
idNumber: {string} - Usuario/email del beneficiario
password: {string} - Contraseña del beneficiario

```

**Respuesta exitosa (200):**
```json
{
    "message": {
        "access_token": "access_token",
        "refresh_token": "refresh_token"
    }
}
```

**Respuesta de error (400/401):**
```json

{
    "path": "path",
    "code": "code",
    "message": "message",
    "type": "type",
    "timestamp": 1761055433785,
    "status": 401
}
```


-------------------------------------------------------------------------------------------------------------------------------
-------------------------------------------------------------------------------------------------------------------------------



### Refresh Token (provee un nuevo access token , apartir del refresh token enviado)

**Endpoint:** `POST /identity-service/v1/auth/refresh`

Authorization: Bearer {ACCESS_TOKEN}


**Body (form-data):**
```
none
```

**Respuesta exitosa (200):**
{
    "message": {
        "access_token": "access_token",
        "refresh_token": "refresh_token"
    }
}

**Respuesta de error (400/401):**

{
    "path": "/oauth",
    "error": "Unauthorized",
    "message": "Full authentication is required to access this resource",
    "type": "InsufficientAuthentication",
    "timestamp": 1761055741447,
    "status": 401
}{
    "path": "/oauth",
    "error": "Unauthorized",
    "message": "Full authentication is required to access this resource",
    "type": "InsufficientAuthentication",
    "timestamp": 1761055741447,
    "status": 401
}



-------------------------------------------------------------------------------------------------------------------------------
-------------------------------------------------------------------------------------------------------------------------------

## 2. Datos del Usuario

### Obtener información del beneficiario autenticado
**Endpoint:** `GET /validation-api/v1/beneficiaries/auth`


**Headers:**
```
Accept: application/json
Content-Type: application/json

Authorization: Bearer {ACCESS_TOKEN}
```

**Respuesta exitosa (200):**
```json
{
    "status": {
        "name": "CON COBERTURA",
        "id": 8
    },
    "createdAt": "2025-10-14T12:28:40.211888",
    "resourceId": "6f665511-6a8c-47ec-b215-3cd91a616bee",
    "lastName": "dominguez",
    "idType": {
        "alias": "DNI",
        "name": "Documento Nacional de Identidad",
        "id": 1
    },
    "relationshipType": {
        "name": "Titular",
        "id": 1
    },
    "createdBy": "anonymousUser",
    "modifiedBy": null,
    "birthDate": "1991-05-12",
    "gender": "MASCULINO",
    "idNumber": 36447582,
    "workIdNumber": 25364475825,
    "beneficiaryCode": "03-30529552/05",
    "beneficiaryCategory": {
        "name": "estado_1",
        "id": 6
    },
    "company": null,
    "paymentMethod": {
        "name": "Voluntario",
        "id": 2
    },
    "activeBatch": false,
    "name": "jose maria",
    "id": 61,
    "_links": {
        "budgets": {
            "href": "http://localhost:8080/validation-api/v1/budgets/beneficiaries?page=1&size=15&beneficiaryId=61&search="
        },
        "prescriptions": {
            "href": "http://localhost:8080/validation-api/v1/prescriptions?page=1&size=15&search=beneficiary%3A%7Bid%3D61%7D"
        },
        "relatives": {
            "href": "http://localhost:8080/validation-api/v1/beneficiaries/61/relatives"
        },
        "self": {
            "href": "http://localhost:8080/validation-api/v1/beneficiaries/61"
        },
        "expirations": {
            "href": "http://localhost:8080/validation-api/v1/beneficiaries/61/expirations?page=1&size=15"
        },
        "contactInfo": {
            "href": "http://localhost:8080/validation-api/v1/beneficiaries/61/contact-info"
        },
        "authorizations": {
            "href": "http://localhost:8080/validation-api/v1/authorizations?beneficiaryId=61&page=1&size=15"
        },
        "charges": {
            "href": "http://localhost:8080/validation-api/v1/beneficiaries/61/charges?page=1&size=15"
        },
        "companyCharges": {
            "href": "http://localhost:8080/validation-api/v1/beneficiaries/61/company-charges?page=1&size=15"
        },
        "batches": {
            "href": "http://localhost:8080/validation-api/v1/batches?page=1&size=15&search=beneficiary%3A%7Bid%3D61%7D"
        }
    },
    "_embedded": {
        "tradeUnions": [],
        "icd10Diseases": [],
        "insurancePlans": [
            {
                "expirationDate": "2026-10-08",
                "insurancePlanType": {
                    "name": "Especial",
                    "id": 2
                },
                "name": "plan X",
                "priority": 8,
                "id": 101,
                "beneficiaryInsurancePlanPriority": 1,
                "beneficiaryInsurancePlanId": 708942,
                "_links": {
                    "self": {
                        "href": "http://localhost:8080/validation-api/v1/beneficiaries/insurance-plans/708942"
                    }
                }
            },
            {
                "expirationDate": "2026-04-23",
                "insurancePlanType": {
                    "name": "Especial",
                    "id": 2
                },
                "name": "Plan Especial3",
                "priority": 3,
                "id": 95,
                "beneficiaryInsurancePlanPriority": 2,
                "beneficiaryInsurancePlanId": 708943,
                "_links": {
                    "self": {
                        "href": "http://localhost:8080/validation-api/v1/beneficiaries/insurance-plans/708943"
                    }
                }
            }
        ]
    }
}



**Respuesta de error (400/401):**

(SE DEBE CORREGIR ESTA RESPONSE , NO DEBE EXPONERSE NADA DE ESTA INFO)

{
    "path": "/validation-api/v1/beneficiaries/auth",
    "code": "Invalid or Malformed JWT",
    "message": "Invalid or Malformed JWT",
    "type": "InvalidToken",
    "timestamp": 1761056353976,
    "status": 401
}





```


-------------------------------------------------------------------------------------------------------------------------------
-------------------------------------------------------------------------------------------------------------------------------


## 3. Autorizaciones

(nota , se provee la data de este user como prueba , nuestro user test no tiene cargadas autorizaciones,)
(nota , no se debe permitir acceder a un id de otro user , por mas que se envie un jwt válido , solo debe corresponder a 1 solo user )

### Obtener autorizaciones del beneficiario
**Endpoint:** `GET /validation-api/v1/authorizations?beneficiaryId=7&page=1&size=10`

**Headers:**
```
Authorization: Bearer {ACCESS_TOKEN}

```

**Query Parameters:**
```
beneficiaryId: number
page: number (opcional, default 1)
size: number (opcional, default 10)
```

**Respuesta:**
```json
{
    "_links": {
        "first": {
            "href": "http://localhost:8080/validation-api/v1/authorizations?page=1&size=10&beneficiaryId=7"
        },
        "next": {
            "href": "http://localhost:8080/validation-api/v1/authorizations?page=2&size=10&beneficiaryId=7"
        },
        "last": {
            "href": "http://localhost:8080/validation-api/v1/authorizations?page=14&size=10&beneficiaryId=7"
        },
        "self": {
            "href": "http://localhost:8080/validation-api/v1/authorizations?page=1&size=10&beneficiaryId=7"
        }
    },
    "pageSize": 10,
    "totalElements": 137,
    "totalPages": 14,
    "_embedded": {
        "authorizations": [
            {
                "status": {
                    "name": "RECHAZADO",
                    "id": 6
                },
                "createdAt": "2020-07-21T17:49:10.441865",
                "city": {
                    "name": "Cipolletti",
                    "id": 69
                },
                "chargeTotal": 0.00,
                "failures": [],
                "paymentMethod": {
                    "name": "Voluntario",
                    "id": 2
                },
                "authorizationCondition": {
                    "name": "BENEFICIARIO EN TRANSITO",
                    "id": 2
                },
                "preMedicalAuthorization": null,
                "rating": null,
                "authorizationType": {
                    "name": "NUMERO DE DOCUMENTO",
                    "id": 2
                },
                "audited": false,
                "refundableItems": false,
                "id": 6306,
                "_links": {
                    "self": {
                        "href": "http://localhost:8080/validation-api/v1/authorizations/6306"
                    }
                }
            },
            {
                "status": {
                    "name": "CANCELADO",
                    "id": 10
                },
                "createdAt": "2020-07-21T17:48:15.350248",
                "city": {
                    "name": "Cipolletti",
                    "id": 69
                },
                "chargeTotal": 0.00,
                "failures": [],
                "paymentMethod": {
                    "name": "Voluntario",
                    "id": 2
                },
                "authorizationCondition": {
                    "name": "BENEFICIARIO EN TRANSITO",
                    "id": 2
                },
                "preMedicalAuthorization": null,
                "rating": null,
                "authorizationType": {
                    "name": "NUMERO DE DOCUMENTO",
                    "id": 2
                },
                "audited": false,
                "refundableItems": false,
                "id": 6305,
                "_links": {
                    "self": {
                        "href": "http://localhost:8080/validation-api/v1/authorizations/6305"
                    }
                }
            },
            {
                "status": {
                    "name": "RECHAZADO",
                    "id": 6
                },
                "createdAt": "2020-07-21T17:47:59.045454",
                "city": {
                    "name": "Cipolletti",
                    "id": 69
                },
                "chargeTotal": 0.00,
                "failures": [],
                "paymentMethod": {
                    "name": "Voluntario",
                    "id": 2
                },
                "authorizationCondition": {
                    "name": "BENEFICIARIO EN TRANSITO",
                    "id": 2
                },
                "preMedicalAuthorization": null,
                "rating": null,
                "authorizationType": {
                    "name": "NUMERO DE DOCUMENTO",
                    "id": 2
                },
                "audited": false,
                "refundableItems": false,
                "id": 6304,
                "_links": {
                    "self": {
                        "href": "http://localhost:8080/validation-api/v1/authorizations/6304"
                    }
                }
            },
            {
                "status": {
                    "name": "CANCELADO",
                    "id": 10
                },
                "createdAt": "2020-07-21T17:47:44.662623",
                "city": {
                    "name": "Cipolletti",
                    "id": 69
                },
                "chargeTotal": 0.00,
                "failures": [],
                "paymentMethod": {
                    "name": "Voluntario",
                    "id": 2
                },
                "authorizationCondition": {
                    "name": "BENEFICIARIO EN TRANSITO",
                    "id": 2
                },
                "preMedicalAuthorization": null,
                "rating": null,
                "authorizationType": {
                    "name": "NUMERO DE DOCUMENTO",
                    "id": 2
                },
                "audited": false,
                "refundableItems": false,
                "id": 6303,
                "_links": {
                    "self": {
                        "href": "http://localhost:8080/validation-api/v1/authorizations/6303"
                    }
                }
            },
            {
                "status": {
                    "name": "CANCELADO",
                    "id": 10
                },
                "createdAt": "2020-07-21T17:47:28.236992",
                "city": {
                    "name": "Cipolletti",
                    "id": 69
                },
                "chargeTotal": 0.00,
                "failures": [],
                "paymentMethod": {
                    "name": "Voluntario",
                    "id": 2
                },
                "authorizationCondition": {
                    "name": "BENEFICIARIO EN TRANSITO",
                    "id": 2
                },
                "preMedicalAuthorization": null,
                "rating": null,
                "authorizationType": {
                    "name": "NUMERO DE DOCUMENTO",
                    "id": 2
                },
                "audited": false,
                "refundableItems": false,
                "id": 6302,
                "_links": {
                    "self": {
                        "href": "http://localhost:8080/validation-api/v1/authorizations/6302"
                    }
                }
            },
            {
                "status": {
                    "name": "CANCELADO",
                    "id": 10
                },
                "createdAt": "2020-06-22T14:29:29.937891",
                "city": {
                    "name": "Cipolletti",
                    "id": 69
                },
                "chargeTotal": 0.00,
                "failures": [],
                "paymentMethod": {
                    "name": "Voluntario",
                    "id": 2
                },
                "authorizationCondition": {
                    "name": "BENEFICIARIO EN TRANSITO",
                    "id": 2
                },
                "preMedicalAuthorization": null,
                "rating": null,
                "authorizationType": {
                    "name": "NUMERO DE DOCUMENTO",
                    "id": 2
                },
                "audited": false,
                "refundableItems": false,
                "id": 5990,
                "_links": {
                    "self": {
                        "href": "http://localhost:8080/validation-api/v1/authorizations/5990"
                    }
                }
            },
            {
                "status": {
                    "name": "APROBADO",
                    "id": 4
                },
                "createdAt": "2020-06-22T14:28:56.945406",
                "city": {
                    "name": "Cipolletti",
                    "id": 69
                },
                "chargeTotal": 150.00,
                "failures": [],
                "paymentMethod": {
                    "name": "Voluntario",
                    "id": 2
                },
                "authorizationCondition": {
                    "name": "BENEFICIARIO EN TRANSITO",
                    "id": 2
                },
                "preMedicalAuthorization": null,
                "rating": null,
                "authorizationType": {
                    "name": "NUMERO DE DOCUMENTO",
                    "id": 2
                },
                "audited": false,
                "refundableItems": false,
                "id": 5970,
                "_links": {
                    "self": {
                        "href": "http://localhost:8080/validation-api/v1/authorizations/5970"
                    }
                }
            },
            {
                "status": {
                    "name": "APROBADO",
                    "id": 4
                },
                "createdAt": "2020-06-22T14:28:48.73929",
                "city": {
                    "name": "Cipolletti",
                    "id": 69
                },
                "chargeTotal": 150.00,
                "failures": [],
                "paymentMethod": {
                    "name": "Voluntario",
                    "id": 2
                },
                "authorizationCondition": {
                    "name": "BENEFICIARIO EN TRANSITO",
                    "id": 2
                },
                "preMedicalAuthorization": null,
                "rating": null,
                "authorizationType": {
                    "name": "NUMERO DE DOCUMENTO",
                    "id": 2
                },
                "audited": false,
                "refundableItems": false,
                "id": 5965,
                "_links": {
                    "self": {
                        "href": "http://localhost:8080/validation-api/v1/authorizations/5965"
                    }
                }
            },
            {
                "status": {
                    "name": "CANCELADO",
                    "id": 10
                },
                "createdAt": "2020-06-22T14:27:55.303743",
                "city": {
                    "name": "Cipolletti",
                    "id": 69
                },
                "chargeTotal": 0.00,
                "failures": [],
                "paymentMethod": {
                    "name": "Voluntario",
                    "id": 2
                },
                "authorizationCondition": {
                    "name": "BENEFICIARIO EN TRANSITO",
                    "id": 2
                },
                "preMedicalAuthorization": null,
                "rating": null,
                "authorizationType": {
                    "name": "NUMERO DE DOCUMENTO",
                    "id": 2
                },
                "audited": false,
                "refundableItems": false,
                "id": 5933,
                "_links": {
                    "self": {
                        "href": "http://localhost:8080/validation-api/v1/authorizations/5933"
                    }
                }
            },
            {
                "status": {
                    "name": "APROBADO",
                    "id": 4
                },
                "createdAt": "2020-06-22T14:26:29.903348",
                "city": {
                    "name": "Cipolletti",
                    "id": 69
                },
                "chargeTotal": 150.00,
                "failures": [],
                "paymentMethod": {
                    "name": "Voluntario",
                    "id": 2
                },
                "authorizationCondition": {
                    "name": "BENEFICIARIO EN TRANSITO",
                    "id": 2
                },
                "preMedicalAuthorization": null,
                "rating": null,
                "authorizationType": {
                    "name": "NUMERO DE DOCUMENTO",
                    "id": 2
                },
                "audited": false,
                "refundableItems": false,
                "id": 5882,
                "_links": {
                    "self": {
                        "href": "http://localhost:8080/validation-api/v1/authorizations/5882"
                    }
                }
            }
        ]
    }
}


**Respuesta de error (400/401):**

(nota , se debe ocultar esta información , solo es válida en entorno de desarrollo)

{
    "path": "/validation-api/v1/authorizations",
    "code": "Invalid or Malformed JWT",
    "message": "Invalid or Malformed JWT",
    "type": "InvalidToken",
    "timestamp": 1761056757902,
    "status": 401
}

```

-------------------------------------------------------------------------------------------------------------------------------
-------------------------------------------------------------------------------------------------------------------------------


## 5. Trámites

### Obtener trámites del beneficiario
**Endpoint:** `GET /validation-api/v1/procedures?beneficiaryId=1&page=1&size=10`

(nota , se provee el id de este  beneficiary como prueba , nuestro user test no tiene cargadas procedures,)
(nota , no se debe permitir acceder a un id de otro user , por mas que se envie un jwt válido , solo debe corresponder a 1 solo user )

**Headers:**
```
Authorization: Bearer {ACCESS_TOKEN}
```

**Query Parameters:**
```
beneficiaryId: number
page: number (opcional, default 1)
size: number (opcional, default 10)
```


**Respuesta:**
```json
{
    "content": [
        {
            "status": {
                "name": "APROBADO",
                "id": 20
            },
            "createdAt": "2019-10-28T17:20:48.097113",
            "fileCount": 1,
            "closedAt": "2019-10-28T17:46:05.391845",
            "type": "DisabilityProcedure",
            "description": "correccion",
            "expiration": null
        },
        {
            "status": {
                "name": "VENCIDO",
                "id": 22
            },
            "createdAt": "2019-12-30T12:58:20.066651",
            "fileCount": 1,
            "closedAt": "2019-12-30T13:04:59.764025",
            "type": "CertificateProcedure",
            "description": "Adjunto blabla",
            "expiration": "2020-12-30"
        },
        {
            "status": {
                "name": "RECHAZADO",
                "id": 21
            },
            "createdAt": "2020-12-09T13:23:24.921363",
            "fileCount": 0,
            "closedAt": "2020-12-09T20:04:04.67817",
            "type": "UnknownAuthorizationProcedure",
            "description": "\nN° de Validación: 8261\nPrestador: Pedro Perez\nConsultorio: Consultorio A\n\nDescripción: no me hice esta atencion\n",
            "expiration": null
        },
        {
            "status": {
                "name": "RECHAZADO",
                "id": 21
            },
            "createdAt": "2019-10-28T17:16:40.411836",
            "fileCount": 3,
            "closedAt": "2019-10-28T17:17:29.396485",
            "type": "DisabilityProcedure",
            "description": "adjunto documentacion discapacidad",
            "expiration": null
        },
        {
            "status": {
                "name": "APROBADO",
                "id": 20
            },
            "createdAt": "2020-12-08T19:03:49.521674",
            "fileCount": 0,
            "closedAt": "2020-12-08T19:04:51.605275",
            "type": "UnknownAuthorizationProcedure",
            "description": "\nN° de Validación: 8255\nPrestador: Pedro Perez\nConsultorio: Consultorio A\n\nDescripción: desconozco\n",
            "expiration": null
        },
        {
            "status": {
                "name": "APROBADO",
                "id": 20
            },
            "createdAt": "2021-01-13T22:08:42.894771",
            "fileCount": 1,
            "closedAt": "2021-01-13T22:09:47.246829",
            "type": "CertificateProcedure",
            "description": "tt",
            "expiration": null
        },
        {
            "status": {
                "name": "APROBADO",
                "id": 20
            },
            "createdAt": "2019-10-29T13:32:01.184392",
            "fileCount": 1,
            "closedAt": "2019-10-29T13:37:52.993339",
            "type": "CUDProcedure",
            "description": "adjunto cud",
            "expiration": null
        },
        {
            "status": {
                "name": "RECHAZADO",
                "id": 21
            },
            "createdAt": "2021-01-13T22:22:44.505517",
            "fileCount": 1,
            "closedAt": "2021-02-09T10:32:32.524876",
            "type": "CertificateProcedure",
            "description": "ttt",
            "expiration": null
        },
        {
            "status": {
                "name": "APROBADO",
                "id": 20
            },
            "createdAt": "2020-12-08T20:16:07.70604",
            "fileCount": 0,
            "closedAt": "2020-12-08T20:18:01.507666",
            "type": "UnknownAuthorizationProcedure",
            "description": "\nN° de Validación: 8256\nPrestador: Pedro Perez\nConsultorio: Consultorio A\n\nDescripción: desconozco\n",
            "expiration": null
        },
        {
            "status": {
                "name": "RECHAZADO",
                "id": 21
            },
            "createdAt": "2020-12-02T11:15:20.613623",
            "fileCount": 0,
            "closedAt": "2020-12-08T17:31:18.612208",
            "type": "UnknownAuthorizationProcedure",
            "description": "\nN°deValidación:8102\nPrestador:PedroPerez\nConsultorio:ConsultorioA\n\nDescripción:porquesi\n",
            "expiration": null
        }
    ],
    "pageable": {
        "pageNumber": 1,
        "pageSize": 10,
        "sort": {
            "sorted": true,
            "unsorted": false,
            "empty": false
        },
        "offset": 10,
        "unpaged": false,
        "paged": true
    },
    "totalElements": 34,
    "totalPages": 4,
    "last": false,
    "size": 10,
    "number": 1,
    "sort": {
        "sorted": true,
        "unsorted": false,
        "empty": false
    },
    "numberOfElements": 10,
    "first": false,
    "empty": false
}


{
    "path": "/validation-api/v1/procedures",
    "code": "Invalid or Malformed JWT",
    "message": "Invalid or Malformed JWT",
    "type": "InvalidToken",
    "timestamp": 1761057490237,
    "status": 401
}


**Respuesta de error (400/401):**

(nota , se debe ocultar esta información , solo es válida en entorno de desarrollo)

{
    "path": "/validation-api/v1/procedures",
    "code": "Invalid or Malformed JWT",
    "message": "Invalid or Malformed JWT",
    "type": "InvalidToken",
    "timestamp": 1761057490237,
    "status": 401
}


```



-------------------------------------------------------------------------------------------------------------------------------
-------------------------------------------------------------------------------------------------------------------------------

### Crear nuevo trámite
**Endpoint:** `POST /validation-api/v1/procedures`

(nota , se deben proveer las claves de aws , para poder almacenar los archivos de imagenes,
sin eso , este endpoint no esta funcional)


tenemos 4 tipos distintos de trámites:

/cud
/disability
/unknown-authorization
/certificate


body para cud  y disability

**Body:**
```
form-data:

file : "url de imagen",
file : "url de imagen",
file : "url de imagen"

body:
{
  "description": string,
  "beneficiary": {
    "id": number
  }
}


body para /certificate

body:
{
  "description": string,
  "beneficiary": {
    "id": number
  },
  "certificateType": {
    "id": number
  }
}

body para /unknown-authorization

body:
{
  "description": string,
  "beneficiary": {
    "id": number
  },
  "medicalAuthorization": {
    "id": number
  }
}


```


-------------------------------------------------------------------------------------------------------------------------------
-------------------------------------------------------------------------------------------------------------------------------







-------------------------------------------------------------------------------------------------------------------------------
-------------------------------------------------------------------------------------------------------------------------------
-------------------------------------------------------------------------------------------------------------------------------

## Códigos de Error Comunes

- **401 Unauthorized:** Token expirado o inválido
- **403 Forbidden:** Sin permisos para el recurso
- **404 Not Found:** Recurso no encontrado
- **422 Unprocessable Entity:** Datos de entrada inválidos
- **500 Internal Server Error:** Error del servidor

## Notas de Implementación

1. Todos los endpoints requieren autenticación excepto el login
2. Los tokens tienen una duración de 1 hora
3. Implementar refresh automático del token cuando expire
4. Manejar errores de red y mostrar mensajes apropiados al usuario
5. Implementar cache local para datos que no cambien frecuentemente
