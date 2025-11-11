import SInfo from 'react-native-sensitive-info';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { getVersion } from 'react-native-device-info';
import { API_CONFIG, FALLBACK_DATA } from '../configs/api.config';

export const authenticateUser = async (credentials) => _authenticateUser(credentials);
export const updateAccessToken = async () => _updateAccessToken();
export const getUserData = async (token) => _getUserData(token);
export const updateAccess = async (profile) => _updateAccess(profile);
export const hasToUpdateApp = async () => _hasToUpdateApp();

const _authenticateUser = async (credentials) => {
    const body = {
        idNumber: credentials.username, // username ahora es el número de documento
        password: credentials.password,
    };

    try {
        const response = await fetch(API_CONFIG.IDENTITY_SERVICE.BASE_URL + API_CONFIG.IDENTITY_SERVICE.ENDPOINTS.LOGIN, {
            method: 'POST',
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(body),
            timeout: 10000,
        });

        const json = await response.json();

        if (!response.ok) {
            // Si el servidor responde con error (401, 404, etc), devolver ese error
            console.log('❌ Login falló:', json);
            return { error: json };
        }

        // La respuesta viene en json.message según el formato nuevo
        const tokenData = json.message;
        
        if (!tokenData || !tokenData.access_token) {
            console.log('❌ Respuesta sin token válido');
            return { error: { message: 'Invalid response from server' } };
        }
        
        await SInfo.setItem('refresh_token', tokenData.refresh_token, {});

        let authResponse = {
            access_token: tokenData.access_token,
            refresh_token: tokenData.refresh_token,
            token_type: 'Bearer',
        };

        console.log('✅ Login exitoso');
        return authResponse;
    } catch (err) {
        console.log('❌ Error de red en authenticateUser:', err);
        
        // SOLO permitir modo offline si es exactamente el usuario de fallback Y hay error de red
        // (NO si las credenciales son incorrectas)
        if (
            err.message && 
            (err.message.includes('Network request failed') || err.message.includes('timeout')) &&
            credentials.username == FALLBACK_DATA.USER.idNumber && 
            credentials.password === FALLBACK_DATA.USER.password
        ) {
            console.log('🔓 Error de conexión detectado - usando login offline con FALLBACK_DATA');
            return {
                access_token: 'offline_token_' + Date.now(),
                refresh_token: 'offline_refresh_' + Date.now(),
                token_type: 'Bearer',
                expires_in: 86400,
                offline_mode: true
            };
        }
        
        // Para cualquier otro error, devolver el error
        return { error: { message: err.message || 'Error de conexión' } };
    }
};

const _updateAccessToken = async () => {
    try {
        const refreshToken = await SInfo.getItem('refresh_token', {});

        const response = await fetch(API_CONFIG.IDENTITY_SERVICE.BASE_URL + API_CONFIG.IDENTITY_SERVICE.ENDPOINTS.REFRESH, {
            method: 'POST',
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json',
                Authorization: `Bearer ${refreshToken}`,
            },
            timeout: 10000,
        });

        const json = await response.json();

        if (!response.ok) {
            console.log('❌ Refresh token failed:', json);
            throw new Error(json.message || 'Refresh token failed');
        }

        // La respuesta viene en json.message según el formato nuevo
        const tokenData = json.message;
        
        if (!tokenData || !tokenData.access_token) {
            console.log('❌ Respuesta de refresh sin token válido');
            throw new Error('Invalid refresh response from server');
        }

        await SInfo.setItem('refresh_token', tokenData.refresh_token, {});

        console.log('✅ Token refreshed successfully');
        return {
            access_token: tokenData.access_token,
            refresh_token: tokenData.refresh_token,
            token_type: 'Bearer',
        };
        
    } catch (err) {
        console.log('❌ Error refreshing token:', err);
        throw err;
    }
};

const _getUserData = async (token) => {
    // Si es token offline, devolver datos de fallback
    if (token && token.startsWith('offline_token_')) {
        console.log('🔓 getUserData offline - retornando FALLBACK_DATA');
        return FALLBACK_DATA.USER.beneficiaryData;
    }

    try {
        const response = await fetch(API_CONFIG.VALIDATION_API.BASE_URL + API_CONFIG.VALIDATION_API.ENDPOINTS.BENEFICIARY_AUTH, {
            method: 'GET',
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json',
                Authorization: token,
            },
            timeout: 10000,
        });

        const json = await response.json();

        if (!response.ok) {
            console.log('❌ Error obteniendo datos de usuario:', json);
            return { error: json };
        }

        console.log('✅ Datos de usuario obtenidos exitosamente');
        return json;
    } catch (err) {
        console.log('❌ Error de red en getUserData:', err);
        return { error: { message: err.message || 'Error de conexión' } };
    }
};

const _updateAccess = async () => {
    // SE ENCARGA DE REFRESCAR TOKEN EN CUANTO SE ENCUENTRE VENCIDO
    try {
        const tokenResponse = await updateAccessToken();

        if (tokenResponse.error) {
            return tokenResponse;
        }

        let profile = await AsyncStorage.getItem('profile');

        if (profile) profile = JSON.parse(profile);

        let newProfile = {
            ...(profile || {}),
            token: {
                ...(profile?.token ?? {}),
                access: tokenResponse.access_token,
                expires_in: tokenResponse.expires_in,
                token_type: tokenResponse.token_type,
            },
        };

        await AsyncStorage.setItem('profile', JSON.stringify(newProfile));
        return { profile: newProfile };
    } catch (err) {
        console.log(err);
        throw err;
    }
};

const _hasToUpdateApp = async () => {
    try {
        const response = await fetch(apiUrls['general-api'] + 'mobile/versions', {
            method: 'GET',
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json',
            },
        });

        const json = await response.json();

        if (!response.ok) {
            throw json;
        }

        const mobileVersionSupported = Number.parseInt(
            json.data.beneficiaryApp
                .split('.')
                .map((item) => (item.length === 1 ? `0${item}` : item))
                .join(''),
            10
        );
        const currentInstalledVersion = Number.parseInt(
            getVersion()
                .split('.')
                .map((item) => (item.length === 1 ? `0${item}` : item))
                .join(''),
            10
        );

        return currentInstalledVersion < mobileVersionSupported;
    } catch (err) {
        console.log(err);
        throw err;
    }
};
