# Vista Principal Home de Afiliado

## Descripción General
La pantalla `BeneficiaryInformationScreen` es la vista principal (home) de la aplicación móvil para afiliados de IPROSS. Muestra información del beneficiario seleccionado, su estado de cobertura, coseguros acumulados y permite generar códigos QR/Token para identificación.

## Ubicación
- **Archivo principal**: `app/screens/user/BeneficiaryInformationScreen.js`
- **Ruta de navegación**: Tab inicial del `MainTabNavigator` con nombre `BeneficiaryInformation`

## Componentes Principales

### 1. Componentes Hijos Directos

#### `Credential` 
- **Ubicación**: `app/components/Credential.js`
- **Propósito**: Muestra la credencial digital del afiliado con diseño tipo tarjeta
- **Props**: `beneficiaryInfo` (objeto con datos del beneficiario)
- **Subcomponentes**:
  - `BackgroundSvg`: Fondo decorativo SVG
  - `RnLogo`: Logo de Río Negro en formato SVG
  - `CredentialUser`: Ícono de usuario en formato SVG

#### `DecoratedCard`
- **Ubicación**: `app/components/DecoratedCard.js`
- **Propósito**: Tarjeta animada que muestra el estado de cobertura del afiliado
- **Props**: 
  - `image`: Ícono de estado (✓ para activo, ✗ para inactivo)
  - `color`: Color del estado
  - `value`: Texto del estado ("Con cobertura" / "Sin cobertura")
- **Animaciones**: Slide-in desde la izquierda con duración aleatoria

#### `SimpleCard`
- **Ubicación**: `app/components/SimpleCard.js`
- **Propósito**: Tarjeta que muestra el coseguro acumulado del mes actual
- **Props**:
  - `header`: "Coseguro acumulado"
  - `title`: Mes y año actual
  - `value`: Monto del coseguro o "Sin cargos"
- **Animaciones**: Slide-in desde la izquierda con duración aleatoria

#### `ImageCard`
- **Ubicación**: `app/components/ImageCard.js`
- **Propósito**: Tarjeta con imagen que muestra información de planes
- **Props**:
  - `image`: Ícono de etiqueta de precio
  - `header`: "Planes"
  - `title`: Nombre del plan principal
- **Interacción**: Al tocar abre modal con lista completa de planes
- **Animaciones**: Slide-in desde la izquierda con duración aleatoria

#### `ActionButton`
- **Librería**: `react-native-action-button`
- **Propósito**: Botón flotante para generar códigos QR y Token
- **Items**:
  - **Código QR**: Navega a `QrCodeGeneratorScreen`
  - **Código Token**: Navega a `TokenCodeGeneratorScreen`
- **Condición**: Solo visible si el afiliado tiene cobertura activa

### 2. Modales

#### `PlanModalContent`
- **Tipo**: Componente funcional interno
- **Propósito**: Muestra lista completa de planes del beneficiario
- **Datos mostrados**:
  - Nombre del plan
  - Tipo de plan
  - Fecha de vencimiento (si aplica)

## Estado del Componente

### State Local
```javascript
{
    isPlanModalVisible: false  // Controla visibilidad del modal de planes
}
```

### Props desde Redux
```javascript
{
    beneficiary: object,           // Datos del beneficiario seleccionado
    update_data_loading: boolean,  // Estado de carga al actualizar datos
    selectedUserCharge: number     // Coseguro acumulado del mes actual
}
```

## Endpoints y APIs

### 1. Datos del Beneficiario
- **Redux State**: `state.profile.relatives.selectedUser`
- **Origen**: Se obtiene del login y se actualiza con `updateUserData()`
- **Endpoint**: `selectedUser._links.self.href`
- **Método**: GET
- **Autenticación**: Bearer Token
- **Backend**: Identity Service

### 2. Coseguros Acumulados
- **Redux State**: `state.charge.charges.currentMonthCharge`
- **Action**: `getUserCharges()` en `chargeAction.js`
- **Endpoint**: `selectedUser._links.charges.href`
- **Método**: GET
- **Autenticación**: Bearer Token
- **Backend**: Validation API
- **Lógica**: Filtra el coseguro del mes y año actual

### 3. Actualización de Datos del Usuario
- **Action**: `updateUserData()` en `profileAction.js`
- **Endpoint**: `selectedUser._links.self.href`
- **Método**: GET
- **Autenticación**: Bearer Token
- **Backend**: Identity Service

## Endpoints Faltantes Identificados

### 1. Endpoint de Planes del Beneficiario
- **Estado actual**: Se accede desde `beneficiary.plans` (parece estar embebido)
- **Posible mejora**: Endpoint específico para obtener planes actualizados
- **Sugerencia**: `GET /api/beneficiaries/{id}/plans`
- **Backend sugerido**: Identity Service

### 2. Endpoint de Estado de Cobertura en Tiempo Real
- **Estado actual**: Se obtiene de `beneficiary.status.name`
- **Limitación**: No se actualiza en tiempo real
- **Sugerencia**: `GET /api/beneficiaries/{id}/coverage-status`
- **Backend sugerido**: Validation API

### 3. Endpoint de Resumen del Dashboard
- **Estado actual**: Múltiples llamadas separadas
- **Mejora sugerida**: Endpoint consolidado
- **Sugerencia**: `GET /api/dashboard/summary`
- **Datos incluidos**:
  - Estado de cobertura
  - Coseguro del mes
  - Resumen de planes
  - Notificaciones pendientes
- **Backend sugerido**: API Gateway que consolide múltiples servicios

## Navegación

### Pantallas Accesibles desde Home
1. **QrCodeGeneratorScreen** - Generación de código QR
2. **TokenCodeGeneratorScreen** - Generación de código Token
3. **Modal de Planes** - Lista completa de planes del beneficiario

### Integración con TabBar
- **Tab**: "Inicio" (primera pestaña)
- **Ícono**: Casa/Home
- **Navegación**: Bottom Tab Navigator

## Consideraciones de Rendimiento

### Optimizaciones Implementadas
- Uso de `PureComponent` para evitar re-renders innecesarios
- Lazy loading de componentes animados
- Memoización de componentes funcionales internos

### Mejoras Sugeridas
- Implementar cache local para datos del beneficiario
- Lazy loading del modal de planes
- Optimizar imágenes SVG para mejor rendimiento

## Dependencias Clave
- `react-redux`: Conexión con store global
- `react-native-action-button`: Botón flotante
- `react-native-modal`: Modal de planes
- `react-native-vector-icons`: Iconografía
- `moment`: Manejo de fechas
- `react-native-safe-area-context`: Áreas seguras

## Estilos y Theming
- Utiliza constantes de colores de `constants/Colors.js`
- Estilos de fuente de `lib/default-styles.js`
- Normalización de tamaños con `lib/size-normalizer.js`
- Diseño responsive adaptable a diferentes tamaños de pantalla