/**
 * Servicio de API
 * 
 * Maneja todas las llamadas HTTP a los servicios backend
 */

import { API_CONFIG, FALLBACK_DATA } from '../configs/api.config';

class ApiService {
  constructor() {
    this.accessToken = null;
    this.refreshToken = null;
  }

  /**
   * Realiza una petición HTTP con manejo de errores
   */
  async request(url, options = {}) {
    const config = {
      ...options,
      headers: {
        ...API_CONFIG.DEFAULT_HEADERS,
        ...options.headers,
      },
    };

    // Agregar token de autenticación si existe
    if (this.accessToken && !options.skipAuth) {
      config.headers['Authorization'] = `Bearer ${this.accessToken}`;
    }

    try {
      const controller = new AbortController();
      const timeoutId = setTimeout(() => controller.abort(), API_CONFIG.TIMEOUT);

      const response = await fetch(url, {
        ...config,
        signal: controller.signal,
      });

      clearTimeout(timeoutId);

      // Manejo de respuestas no exitosas
      if (!response.ok) {
        const error = await response.json().catch(() => ({
          message: 'Error en la petición',
          status: response.status,
        }));
        
        // Si el token expiró, intentar refrescar
        if (response.status === 401 && this.refreshToken && !options.skipRefresh) {
          const refreshed = await this.refreshAccessToken();
          if (refreshed) {
            // Reintentar la petición original con el nuevo token
            return this.request(url, options);
          }
        }

        throw error;
      }

      return await response.json();
    } catch (error) {
      console.error('API Request Error:', error);
      throw error;
    }
  }

  /**
   * Login de usuario
   */
  async login(idNumber, password) {
    const url = `${API_CONFIG.IDENTITY_SERVICE.BASE_URL}${API_CONFIG.IDENTITY_SERVICE.ENDPOINTS.LOGIN}`;
    
    try {
      // Crear FormData para x-www-form-urlencoded
      const formData = new URLSearchParams();
      formData.append('idNumber', idNumber);
      formData.append('password', password);

      const response = await this.request(url, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: formData.toString(),
        skipAuth: true,
      });

      // Guardar tokens
      if (response.message?.access_token) {
        this.accessToken = response.message.access_token;
        this.refreshToken = response.message.refresh_token;
      }

      return {
        success: true,
        tokens: response.message,
      };
    } catch (error) {
      console.error('Login Error:', error);
      
      // Usar datos de fallback si hay error de conexión
      if (idNumber === FALLBACK_DATA.USER.idNumber && 
          password === FALLBACK_DATA.USER.password) {
        return {
          success: true,
          fallback: true,
          userData: FALLBACK_DATA.USER.beneficiaryData,
        };
      }

      return {
        success: false,
        error: error.message || 'Error de autenticación',
      };
    }
  }

  /**
   * Refrescar access token
   */
  async refreshAccessToken() {
    if (!this.refreshToken) return false;

    const url = `${API_CONFIG.IDENTITY_SERVICE.BASE_URL}${API_CONFIG.IDENTITY_SERVICE.ENDPOINTS.REFRESH}`;
    
    try {
      const response = await this.request(url, {
        method: 'POST',
        skipRefresh: true, // Evitar bucle infinito
      });

      if (response.message?.access_token) {
        this.accessToken = response.message.access_token;
        this.refreshToken = response.message.refresh_token;
        return true;
      }

      return false;
    } catch (error) {
      console.error('Refresh Token Error:', error);
      this.logout();
      return false;
    }
  }

  /**
   * Obtener datos del beneficiario autenticado
   */
  async getBeneficiaryData() {
    const url = `${API_CONFIG.VALIDATION_API.BASE_URL}${API_CONFIG.VALIDATION_API.ENDPOINTS.BENEFICIARY_AUTH}`;
    
    try {
      const response = await this.request(url, {
        method: 'GET',
      });

      return {
        success: true,
        data: response,
      };
    } catch (error) {
      console.error('Get Beneficiary Error:', error);
      
      // Usar datos de fallback
      return {
        success: true,
        fallback: true,
        data: FALLBACK_DATA.USER.beneficiaryData,
      };
    }
  }

  /**
   * Obtener autorizaciones
   */
  async getAuthorizations(beneficiaryId, page = 1, size = 10) {
    const url = `${API_CONFIG.VALIDATION_API.BASE_URL}${API_CONFIG.VALIDATION_API.ENDPOINTS.AUTHORIZATIONS}?beneficiaryId=${beneficiaryId}&page=${page}&size=${size}`;
    
    try {
      const response = await this.request(url, {
        method: 'GET',
      });

      return {
        success: true,
        data: response,
      };
    } catch (error) {
      console.error('Get Authorizations Error:', error);
      
      // Retornar array vacío como fallback
      return {
        success: true,
        fallback: true,
        data: {
          _embedded: { authorizations: [] },
          totalElements: 0,
          totalPages: 0,
        },
      };
    }
  }

  /**
   * Obtener trámites
   */
  async getProcedures(beneficiaryId, page = 1, size = 10) {
    const url = `${API_CONFIG.VALIDATION_API.BASE_URL}${API_CONFIG.VALIDATION_API.ENDPOINTS.PROCEDURES}?beneficiaryId=${beneficiaryId}&page=${page}&size=${size}`;
    
    try {
      const response = await this.request(url, {
        method: 'GET',
      });

      return {
        success: true,
        data: response,
      };
    } catch (error) {
      console.error('Get Procedures Error:', error);
      
      // Retornar array vacío como fallback
      return {
        success: true,
        fallback: true,
        data: {
          content: [],
          totalElements: 0,
          totalPages: 0,
        },
      };
    }
  }

  /**
   * Obtener familiares
   */
  async getRelatives(beneficiaryId) {
    const url = `${API_CONFIG.VALIDATION_API.BASE_URL}${API_CONFIG.VALIDATION_API.ENDPOINTS.RELATIVES(beneficiaryId)}`;
    
    try {
      const response = await this.request(url, {
        method: 'GET',
      });

      return {
        success: true,
        data: response,
      };
    } catch (error) {
      console.error('Get Relatives Error:', error);
      
      // Retornar array vacío como fallback
      return {
        success: true,
        fallback: true,
        data: [],
      };
    }
  }

  /**
   * Crear trámite
   */
  async createProcedure(type, data, files = []) {
    const url = `${API_CONFIG.VALIDATION_API.BASE_URL}${API_CONFIG.VALIDATION_API.ENDPOINTS.PROCEDURES}/${type}`;
    
    try {
      const formData = new FormData();
      
      // Agregar archivos
      files.forEach((file, index) => {
        formData.append('file', file);
      });

      // Agregar datos como JSON
      formData.append('body', JSON.stringify(data));

      const response = await this.request(url, {
        method: 'POST',
        headers: {
          // No setear Content-Type, FormData lo hace automáticamente
        },
        body: formData,
      });

      return {
        success: true,
        data: response,
      };
    } catch (error) {
      console.error('Create Procedure Error:', error);
      
      return {
        success: false,
        error: error.message || 'Error al crear trámite',
      };
    }
  }

  /**
   * Cerrar sesión
   */
  logout() {
    this.accessToken = null;
    this.refreshToken = null;
  }

  /**
   * Verificar si hay sesión activa
   */
  isAuthenticated() {
    return !!this.accessToken;
  }
}

// Exportar instancia única (singleton)
export default new ApiService();
