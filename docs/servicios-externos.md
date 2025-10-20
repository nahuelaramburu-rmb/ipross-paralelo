# 🌐 **Servicios Externos Consumidos por Pantallas - App IPROSS**

## 📋 **Resumen General**

La aplicación móvil de beneficiarios IPROSS consume varios servicios externos además del backend principal. A continuación se detalla cada pantalla que utiliza servicios externos y qué tipo de integración realiza.

---

## 🏥 **1. Sistema de Turnos Médicos (API Externa)**

### **Servicio**: `api-turnos`
**URL Base**: Configurada en `Config.APITURNOS`
**Autenticación**: Sistema independiente con credenciales específicas

### **Pantallas que lo consumen**:

#### **AppointmentScreen** 📅
- **Archivo**: `app/screens/appointment/AppointmentScreen.js`
- **Funcionalidad**: Lista de turnos del usuario
- **Endpoints utilizados**:
  - `GET /apiv1/turns/search` - Buscar turnos existentes
  - `GET /apiv1/applicants/search` - Buscar datos del solicitante

#### **NewAppointmentScreen** 📝
- **Archivo**: `app/screens/appointment/NewAppointmentScreen.js`
- **Funcionalidad**: Crear nuevos turnos médicos
- **Endpoints utilizados**:
  - `GET /apiv1/delegations` - Obtener delegaciones disponibles
  - `GET /apiv1/sectors/search` - Obtener sectores por delegación
  - `GET /apiv1/turns` - Obtener turnos disponibles
  - `POST /apiv1/turns` - Crear nuevo turno
  - `POST /apiv1/turns/confirm` - Confirmar turno
  - `POST /apiv1/turns/cancel` - Cancelar turno

#### **AppointmentDetail** 🔍
- **Archivo**: `app/components/appointment/AppointmentDetail.js`
- **Funcionalidad**: Detalle de un turno específico
- **Endpoints utilizados**:
  - `GET /apiv1/turns/search?turn_id={id}` - Obtener detalle del turno

### **Proceso de autenticación**:
```javascript
// Archivo: app/lib/loginAppointment.js
POST /apiv1/login
Body: {
  "email": "api_user_credentials",
  "password": "api_password_credentials"
}
```

### **Registro de solicitante**:
```javascript
// Si el usuario no existe en el sistema de turnos
POST /apiv1/applicants
Body: {
  "documento": "user_document",
  "apellidos": "user_lastname", 
  "nombres": "user_firstname"
}
```

---

## 🗺️ **2. Servicio de Geocodificación (Google Maps API)**

### **Servicio**: `api-maps`
**URL Base**: Configurada en `Config.API_GEOCODING`
**API Key**: Configurada en `Config.API_GEOCODING_KEY`

### **Pantallas que lo consumen**:

#### **ProfessionalMapsScreen** 🗺️
- **Archivo**: `app/screens/professional/ProfessionalMapsScreen.js`
- **Funcionalidad**: Mostrar ubicación de profesionales en mapa
- **Estado**: ⚠️ **ACTUALMENTE DESHABILITADA** (usando ProfessionalMapsScreenTemp)
- **Componente**: Utiliza `react-native-maps` con `MapView` y `Marker`
- **Endpoint utilizado**:
  - `GET /?key={api_key}&address={address}` - Geocodificar direcciones

### **ProfessionalMapsScreenTemp** 🚧
- **Archivo**: `app/screens/professional/ProfessionalMapsScreenTemp.js`
- **Funcionalidad**: Pantalla temporal que reemplaza los mapas
- **Estado**: Funcionalidad temporalmente deshabilitada
- **Mensaje**: "Esta función será restaurada próximamente una vez que se actualicen las dependencias"

### **Proceso de geocodificación**:
```javascript
// Archivo: app/actions/professionalAction.js
const getMedicalCoordinates = async (streetNumber, street, city) => {
  const url = `${apiUrls['api-maps']}${streetNumber}+${street},${city},+AR`;
  // Llamada para obtener coordenadas de Google Maps
}
```

---

## 📊 **3. Servidor de Reportes (Report Server)**

### **Servicio**: `report-server`
**URL Base**: `http://{host}/report-server/v1/`
**Autenticación**: Basic Auth con credenciales específicas

### **Pantallas que lo consumen**:

#### **Sistema de Reportes de Errores** 📈
- **Archivo**: `app/actions/errorAction.js`
- **Funcionalidad**: Envío automático de reportes de errores
- **Endpoint utilizado**:
  - `POST /report` - Enviar reporte de errores de la aplicación

### **Datos enviados**:
```javascript
{
  "device_info": {
    "brand": "Samsung",
    "os_name": "Android", 
    "os_version": "11",
    "carrier": "Movistar",
    "manufacturer": "Samsung",
    "model": "Galaxy S21",
    "mac": "device_mac_address"
  },
  "user": {
    "user_remote_id": "uuid",
    "name": "Juan",
    "surname": "Pérez", 
    "username": "12345678"
  },
  "app": {
    "app_code_identifier": "BeAppVEM"
  },
  "report": {
    "app_version": "1.5.7",
    "app_api_level": 30,
    "app_last_update_time": 1234567890,
    "errors": [...]
  }
}
```

---

## 🔔 **4. Servicio de Notificaciones Push (Firebase)**

### **Servicio**: Firebase Cloud Messaging
**SDK**: `@react-native-firebase/app` y `@react-native-firebase/messaging`

### **Pantallas que lo consumen**:

#### **Sistema Global de Notificaciones** 🔔
- **Archivo**: `app/actions/profileAction.js`
- **Funcionalidad**: Registro de dispositivos para push notifications
- **Integración**: Firebase Messaging

### **Proceso de registro**:
```javascript
// Registro del dispositivo para notificaciones
POST /notification-service/v1/devices
Body: {
  "deviceToken": "firebase_token",
  "platform": "android/ios",
  "userId": "user_uuid"
}
```

### **Funciones utilizadas**:
- `Firebase.messaging().deleteToken()` - Eliminar token al logout
- Registro automático de token al login
- Manejo de notificaciones en background y foreground

---

## 🏥 **5. API de Validación (Backend Principal)**

### **Servicio**: `validation-api`
**URL Base**: `https://{host}/validation-api/v1/`
**Autenticación**: Bearer Token OAuth2

### **Pantallas que lo consumen**:
- **ValidationScreen** - Autorizaciones médicas
- **ProcedureScreen** - Trámites y solicitudes
- **BeneficiaryInformationScreen** - Datos del beneficiario
- **CoinsuranceChargeScreen** - Coseguros
- **ProfessionalScreen** - Búsqueda de profesionales
- **BatchScreen** - Lotes de discapacidad

---

## 🔐 **6. Servicio de Identidad (Identity Service)**

### **Servicio**: `identity-service`
**URL Base**: `https://{host}/identity-service/v1/`
**Autenticación**: OAuth2 con client credentials

### **Pantallas que lo consumen**:
- **LoginScreen** - Autenticación de usuarios
- **SignUpScreen** - Registro de nuevos usuarios
- **ForgotPasswordScreen** - Recuperación de contraseñas
- **QrCodeGeneratorScreen** - Generación de códigos QR
- **TokenCodeGeneratorScreen** - Generación de tokens OTP

---

## 📋 **Configuración de Variables de Entorno**

```javascript
// Archivo: app/configs/api.js
const host = Config.HOST;                    // Host principal
const turnos = Config.APITURNOS;            // Host de turnos
const maps = Config.API_GEOCODING;          // API de geocodificación
const mapsKey = Config.API_GEOCODING_KEY;   // Key de Google Maps

// Credenciales de servicios externos
const loginIpross = {
  user_api: Config.USER_API_IPROSS,
  passw_api: Config.PASSW_API_IPROSS,
};

const reportServerKeys = {
  key: Config.REPORTSERVER_KEY,
  secret: Config.REPORTSERVER_SECRET,
};
```

---

## ⚠️ **Estados Actuales de Servicios Externos**

| Servicio | Estado | Observaciones |
|----------|--------|---------------|
| **API Turnos** | ✅ **Activo** | Funcionando correctamente |
| **Google Maps** | 🚧 **Deshabilitado** | Reemplazado por pantalla temporal |
| **Report Server** | ✅ **Activo** | Envío automático de errores |
| **Firebase Push** | ✅ **Activo** | Notificaciones push funcionando |
| **Identity Service** | ✅ **Activo** | Autenticación y tokens |
| **Validation API** | ✅ **Activo** | Backend principal |

---

## 🔧 **Dependencias Externas**

### **React Native Maps** (Temporalmente deshabilitado)
```json
{
  "react-native-maps": "Usado para ProfessionalMapsScreen"
}
```

### **Firebase**
```json
{
  "@react-native-firebase/app": "^23.3.1",
  "@react-native-firebase/messaging": "^23.3.1"
}
```

### **Device Info**
```json
{
  "react-native-device-info": "^14.0.4"
}
```

---

## 🚀 **Recomendaciones**

1. **Mapas**: Restaurar funcionalidad de ProfessionalMapsScreen una vez actualizadas las dependencias
2. **Monitoreo**: Implementar health checks para servicios externos
3. **Fallbacks**: Agregar pantallas de fallback cuando servicios externos no estén disponibles
4. **Cache**: Implementar cache local para datos de turnos y profesionales
5. **Retry Logic**: Agregar lógica de reintentos para llamadas a servicios externos

---

**Última actualización**: Octubre 2025  
**Estado de servicios**: Revisión mensual recomendada