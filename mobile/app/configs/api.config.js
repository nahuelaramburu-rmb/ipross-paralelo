/**
 * Configuración de API
 * 
 * Define las URLs base para los servicios backend de IPROSS
 */

// Host principal - puede ser sobreescrito por variable de entorno
const DEFAULT_HOST = '168.181.187.5';
const API_HOST = process.env.API_HOST || DEFAULT_HOST;

// Protocolo HTTP (sin SSL en desarrollo)
const PROTOCOL = 'http://';

// URLs base para cada servicio
export const API_CONFIG = {
  // Servicio de identidad (autenticación)
  IDENTITY_SERVICE: {
    BASE_URL: `${PROTOCOL}${API_HOST}:81/identity-service/v1`,
    ENDPOINTS: {
      LOGIN: '/auth/login',
      REFRESH: '/auth/refresh',
    }
  },
  
  // Servicio de validación (datos de beneficiarios)
  VALIDATION_API: {
    BASE_URL: `${PROTOCOL}${API_HOST}:82/validation-api/v1`,
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
    idNumber: 36447582,
    password: 'Password123',
    beneficiaryData: {
      id: 61,
      name: 'jose maria',
      lastName: 'dominguez',
      idNumber: 36447582,
      beneficiaryCode: '03-30529552/05',
      gender: 'MASCULINO',
      birthDate: '1991-05-12',
      status: {
        name: 'CON COBERTURA',
        id: 8
      },
      beneficiaryCategory: {
        name: 'estado_1',
        id: 6
      }
    }
  }
};

export default API_CONFIG;
