import SInfo from 'react-native-sensitive-info';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { getVersion } from 'react-native-device-info';
import { FALLBACK_DATA } from '../configs/api.config';

export const authenticateUser = async (credentials) => _authenticateUser(credentials);
export const updateAccessToken = async () => _updateAccessToken();
export const getUserData = async (token) => _getUserData(token);
export const updateAccess = async (profile) => _updateAccess(profile);
export const hasToUpdateApp = async () => _hasToUpdateApp();

import { apiUrls, loginKeys } from '../configs/api';

const _authenticateUser = async (credentials) => {
    // Verificar si es login offline con credenciales de fallback
    if (
        credentials.username == FALLBACK_DATA.USER.idNumber && 
        credentials.password === FALLBACK_DATA.USER.password
    ) {
        console.log('🔓 Login offline detectado - usando FALLBACK_DATA');
        return {
            access_token: 'offline_token_' + Date.now(),
            refresh_token: 'offline_refresh_' + Date.now(),
            token_type: 'Bearer',
            expires_in: 86400,
            offline_mode: true
        };
    }

    const body = {
        username: credentials.username,
        password: credentials.password,
        grant_type: 'password',
        client_id: loginKeys['key'],
        client_secret: loginKeys['secret'],
    };

    let formData = [];
    for (let property in body) {
        var encodedKey = encodeURIComponent(property);
        var encodedValue = encodeURIComponent(body[property]);
        formData.push(encodedKey + '=' + encodedValue);
    }
    formData = formData.join('&');

    try {
        const response = await fetch(apiUrls['identity-service'] + 'oauth/token', {
            method: 'POST',
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: formData,
        });

        const json = await response.json();

        if (!response.ok) {
            return { error: json };
        }

        await SInfo.setItem('refresh_token', json.refresh_token, {});

        let authResponse = {
            ...json,
        };

        return authResponse;
    } catch (err) {
        console.log('❌ Error en authenticateUser:', err);
        return { error: err };
    }
};

const _updateAccessToken = async () => {
    try {
        const refreshToken = await SInfo.getItem('refresh_token', {});

        const body = {
            grant_type: 'refresh_token',
            client_id: loginKeys['key'],
            client_secret: loginKeys['secret'],
            refresh_token: refreshToken,
        };

        let formData = [];
        for (let property in body) {
            var encodedKey = encodeURIComponent(property);
            var encodedValue = encodeURIComponent(body[property]);
            formData.push(encodedKey + '=' + encodedValue);
        }
        formData = formData.join('&');

        const response = await fetch(apiUrls['identity-service'] + 'oauth/token', {
            method: 'POST',
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: formData,
        });

        const json = await response.json();

        if (!response.ok) throw json;

        await SInfo.setItem('refresh_token', json.refresh_token, {});

        return json;
    } catch (err) {
        console.log(err);
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
        const response = await fetch(apiUrls['api'] + 'beneficiaries/auth', {
            method: 'GET',
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json',
                Authorization: token,
            },
        });

        const json = await response.json();

        if (!response.ok) {
            return { error: json };
        }

        return json;
    } catch (err) {
        console.log('❌ Error en getUserData:', err);
        return { error: err };
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
