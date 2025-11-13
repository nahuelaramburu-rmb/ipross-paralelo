/**
 * Configuración de API
 * 
 * Define las URLs base para los servicios backend de IPROSS
 */

// URL base del servidor (Railway Production)
const BASE_URL = 'https://backend-ipross-production.up.railway.app';

// URLs base para cada servicio
export const API_CONFIG = {
  // Servicio de identidad (autenticación)
  IDENTITY_SERVICE: {
    BASE_URL: `${BASE_URL}/identity-service/v1`,
    ENDPOINTS: {
      LOGIN: '/auth/login',
      REFRESH: '/auth/refresh',
    }
  },
  
  // Servicio de validación (datos de beneficiarios)
  VALIDATION_API: {
    BASE_URL: `${BASE_URL}/validation-api/v1`,
    ENDPOINTS: {
      BENEFICIARY_AUTH: '/beneficiaries/auth',
      AUTHORIZATIONS: '/authorizations',
      PROCEDURES: '/procedures',
      RELATIVES: (id) => `/beneficiaries/${id}/relatives`,
    }
  },

  // Configuración de timeout
  TIMEOUT: 10000, // 10 segundos

  // Headers por defecto
  DEFAULT_HEADERS: {
    'Accept': 'application/json',
    'Content-Type': 'application/json',
  }
};

// Datos de fallback si la API no está disponible
export const FALLBACK_DATA = {
  USER: {
    idNumber: 35467201,
    password: 'Ramones162',
    beneficiaryData: {
      id: 6,
      name: 'patricio',
      lastName: 'aguirre',
      idNumber: 35467201,
      deleted: false,
      user: {
        username: 'patricio',
        email: 'patricionguirre@gmail.com',
        role: 'BENEFICIARY',
        tenant: 'default'
      },
      status: {
        name: 'CON COBERTURA',
        id: 1
      }
    }
  }
};

export default API_CONFIG;
