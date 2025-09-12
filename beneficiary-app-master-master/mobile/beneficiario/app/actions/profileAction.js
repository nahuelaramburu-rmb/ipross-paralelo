import AsyncStorage from '@react-native-async-storage/async-storage';

import {
    LOGGED_IN,
    LOGGING_IN,
    CONFIRMING_TEMPORARY_PASSWORD,
    CREATING_ACCOUNT,
    ACCOUNT_CREATED,
    LOGGED_OUT,
    FETCH_RELATIVES_SUCCESS,
    FETCHING_RELATIVES,
    CHANGE_USER,
    ERROR,
    RENEW_PASSWORD,
    VALIDATING_DATA,
    VALIDATION_DATA_SUCCESS,
    VALIDATION_DATA_ERROR,
    CLEAR_VALIDATION_ERROR,
    CONFIRMING_PASSWORD_FORGOT_PASSWORD,
    CONFIRM_PASSWORD_FORGOT_PASSWORD_SUCCESS,
    UPDATING_SELECTED_USER_DATA,
    SELECTED_USER_DATA_UPDATED,
    UPDATE_TEMPORARY_PASSWORD_SUCCESS,
    SENDING_OTP_EMAIL,
    SEND_OTP_EMAIL_SUCCESS,
    USER_DEVICE_REGISTED,
} from '../constants/ActionTypes';
import { apiUrls, loginKeys } from '../configs/api';
import findIndex from 'lodash/findIndex';
import NetInfo from '@react-native-community/netinfo';

import SInfo from 'react-native-sensitive-info';
import { Platform } from 'react-native';
import Firebase from '@react-native-firebase/app';

import { authenticateUser, getUserData, hasToUpdateApp } from '../lib/authentication';
import base64 from 'base-64';
import { navigate, reset } from '../lib/NavigationService';
import DeviceInfo from 'react-native-device-info';
import strings from '../constants/Strings';
import { DropDownHolder } from '../components/DropDownHolder';

export const checkUpdationNedded = () => (dispatch) => _checkUpdationNedded(dispatch);
const _checkUpdationNedded = async (dispatch) => {
    try {
        const updationNeeded = await hasToUpdateApp();
        return updationNeeded;
    } catch (err) {
        dispatch({ type: ERROR, error: err, notify: true });
        throw err;
    }
};

// LOGIN METHODS (MÉTODOS RELACIONADOS AL LOGIN)

export const updateUserData = () => (dispatch, getState) => _updateUserData(dispatch, getState);
const _updateUserData = async (dispatch, getState) => {
    dispatch({ type: UPDATING_SELECTED_USER_DATA });

    if (getState().profile.relatives.selectedUser === null) return;

    const url = getState().profile.relatives.selectedUser._links.self;
    const token = getState().profile.token;

    try {
        const response = await fetch(url.href, {
            method: 'GET',
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json',
                Authorization: `${token.token_type} ${token.access}`,
            },
        });

        const json = await response.json();

        if (!response.ok) {
            throw json;
        }

        dispatch({ type: SELECTED_USER_DATA_UPDATED, data: json });
    } catch (err) {
        console.log(err);
        dispatch({ type: ERROR, error: err });
    }
};

export const logout = (responseError = null) => (dispatch) => _logout(responseError, dispatch);
const _logout = async (responseError, dispatch) => {
    try {
        await Firebase.messaging().deleteToken();
        await AsyncStorage.clear();
        await SInfo.deleteItem('refresh_token', {});
        dispatch({ type: LOGGED_OUT, error: responseError });
    } catch (err) {
        console.log('error logging out', err);
        NetInfo.fetch().then((state) => {
            const isConnected = state.isConnected;
            if (!isConnected && !responseError) {
                /**
                 * SI LLEGO ACA ES PORQUE EL USUARIO CLICKEO EN LA UI LOGOUT Y NO TENIA CONEXION
                 */
                DropDownHolder.alert(
                    'error',
                    'Error',
                    'No se puede cerrar sesión. Revise su conexión y reintente nuevamente'
                );
            }
        });
        return;
    }
};

export const getSavedData = () => (dispatch) => _getSavedData(dispatch);
const _getSavedData = async (dispatch) => {
    try {
        let profile = await AsyncStorage.getItem('profile');
        let selectedUser = await AsyncStorage.getItem('selectedUser');
        if (profile === null) return dispatch({ type: LOGGED_IN, profile: null, selectedUser: null });
        profile = JSON.parse(profile);
        selectedUser = JSON.parse(selectedUser);
        dispatch({ type: LOGGED_IN, profile, selectedUser });
    } catch (err) {
        console.log(err);
        dispatch({ type: ERROR, error: { error: err }, notify: false });
        _logout(null, dispatch);
    }
};

export const login = (credentials) => (dispatch) => _login(credentials, dispatch);
const _login = async (credentials, dispatch) => {
    dispatch({ type: LOGGING_IN });

    const authResponse = await authenticateUser(credentials);

    let hasToChangePass = false;
    if (
        authResponse.error &&
        authResponse.error.message &&
        authResponse.error.message.includes('FORCE_CHANGE_PASSWORD')
    )
        hasToChangePass = true;

    if (authResponse.error && !hasToChangePass) {
        if (authResponse.error.message.includes('EMAIL_VERIFICATION_REQUIRED')) {
            authResponse.error.message = strings.login.email_verification_required;
        }
        dispatch({ type: ERROR, error: authResponse.error });
        return;
    }

    let userDataResponse = null;

    if (!hasToChangePass) {
        const token = authResponse.token_type + ' ' + authResponse.access_token;
        userDataResponse = await getUserData(token);

        if (userDataResponse.error) {
            console.log(userDataResponse.error);
            dispatch({ type: ERROR, error: userDataResponse.error });
            return;
        }
    }

    let profile = {
        credentials: credentials,
        token: {
            access: authResponse.access_token || null,
            refresh: authResponse.refresh_token || null,
            expires_in: authResponse.expires_in || null,
            token_type: authResponse.token_type || null,
        },
        userData: userDataResponse,
    };

    if (!hasToChangePass) {
        try {
            await AsyncStorage.setItem('profile', JSON.stringify(profile));
            dispatch({ type: LOGGED_IN, profile: profile, selectedUser: null });
        } catch (err) {
            console.log(err);
            return;
        }
    } else {
        navigate('PasswordReset');
        dispatch({ type: RENEW_PASSWORD, profile: profile });
    }
};

export function _isExpired(token) {
    // CONTROLA VENCIMIENTO DE TOKEN
    const rtData = base64.decode(token.split('.')[1]);
    if (Date.now() > JSON.parse(rtData).exp * 1000) return true;
    return false;
}

// ACCOUNT METHODS (MÉTODOS RELACIONADOS A LA CREACIÓN DE CUENTA)

export const updatePassword = (new_password) => (dispatch, getState) =>
    _updatePassword(new_password, dispatch, getState);
const _updatePassword = async (new_password, dispatch, getState) => {
    // ENDPOINT PARA CAMBIAR LA CONTRASEÑA DE LAS CUENTAS CREADAS DESDE EL ADMIN EN SU PRIMER LOGIN

    dispatch({ type: CONFIRMING_TEMPORARY_PASSWORD });

    const profile = getState().profile;

    const body = {
        password: profile.credentials.password,
        new_password: new_password.new_password,
    };

    try {
        const response = await fetch(
            apiUrls['identity-service'] + `users/password?username=${profile.credentials.username}`,
            {
                method: 'PUT',
                headers: {
                    Accept: 'application/json',
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(body),
            }
        );

        let json = null;

        if (response.status !== 204) json = await response.json();

        if (!response.ok) {
            throw json;
        }

        dispatch({ type: UPDATE_TEMPORARY_PASSWORD_SUCCESS });
    } catch (err) {
        console.log(err);
        dispatch({ type: ERROR, error: err });
        throw err;
    }
};

export const validateData = (obj) => (dispatch) => _validateData(obj, dispatch);
const _validateData = async (obj, dispatch) => {
    // ENDPOINT INTERMEDIO PARA VALIDAR LA DATA INGRESADO POR EL USUARIO Y CONSTATAR QUE EXISTE EN EL SISTEMA PARA PODER CREARLE UNA CUENTA
    dispatch({ type: VALIDATING_DATA });

    try {
        const response = await fetch(
            `${apiUrls['api']}beneficiaries/verification?beneficiaryCode=${obj.beneficiaryCode}&birthDate=${obj.birthDate}&idType=${obj.idType}&idNumber=${obj.idNumber}`,
            {
                method: 'GET',
                headers: {
                    Accept: 'application/json',
                    'Content-Type': 'application/json',
                    Authorization: 'Basic ' + base64.encode(loginKeys['key'] + ':' + loginKeys['secret']),
                },
            }
        );

        const json = await response.json();

        if (!response.ok) {
            throw json;
        }
        dispatch({ type: VALIDATION_DATA_SUCCESS, data: json });
    } catch (err) {
        console.log(err);
        dispatch({ type: VALIDATION_DATA_ERROR });
        dispatch({ type: ERROR, error: err });
        throw err;
    }
};

export const confirmAccount = (account) => (dispatch, getState) =>
    _confirmAccount(account, dispatch, getState);
const _confirmAccount = async (account, dispatch, getState) => {
    // ENDPOINT FINAL PARA CREAR LA CUENTA AL USUARIO UNA VEZ CONSTATADA SU INFORMACION
    dispatch({ type: CREATING_ACCOUNT });

    try {
        const response = await fetch(apiUrls['identity-service'] + 'users', {
            method: 'POST',
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json',
                Authorization: 'Basic ' + base64.encode(loginKeys['key'] + ':' + loginKeys['secret']),
            },
            body: JSON.stringify(account),
        });

        let json = null;

        if (response.status !== 204) json = await response.json();

        if (!response.ok) {
            throw json;
        }

        dispatch({ type: ACCOUNT_CREATED, account });
        navigate('Login');
    } catch (err) {
        console.log(err);
        dispatch({ type: ERROR, error: err });
    }
};

export const sendOTPEmail = (email) => (dispatch) => _sendOTPEmail(email, dispatch);
const _sendOTPEmail = async (email, dispatch) => {
    // ENDPOINT PARA ENVIAR MAIL CON CÓDIGO PARA HACER UPDATE DEL PASSWORD EN CASO DE QUE SE LO OLVIDE
    dispatch({ type: SENDING_OTP_EMAIL });

    try {
        const response = await fetch(`${apiUrls['identity-service']}users/forgot?email=${email.email}`, {
            method: 'GET',
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json',
            },
        });

        let json = null;

        if (response.status !== 204) json = await response.json();

        if (!response.ok) {
            throw json;
        }

        dispatch({ type: SEND_OTP_EMAIL_SUCCESS, email: email.email });
    } catch (err) {
        console.log(err);
        dispatch({ type: ERROR, error: err });
        throw err;
    }
};

export const confirmNewPassword = (obj) => (dispatch, getState) =>
    _confirmNewPassword(obj, dispatch, getState);
const _confirmNewPassword = async (obj, dispatch, getState) => {
    // ENDPOINT PARA CONFIRMAR LA NUEVA CONTRASEÑA EN CASO DE QUE SE LO OLVIDE
    dispatch({ type: CONFIRMING_PASSWORD_FORGOT_PASSWORD });
    const email = getState().profile.forgot_password.email;

    const body = { ...obj, email };

    try {
        const response = await fetch(`${apiUrls['identity-service']}users/forgot`, {
            method: 'PUT',
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(body),
        });

        let json = null;

        if (response.status !== 204) json = await response.json();

        if (!response.ok) {
            throw json;
        }

        dispatch({ type: CONFIRM_PASSWORD_FORGOT_PASSWORD_SUCCESS });
        reset({
            index: 0,
            routes: [{ name: 'Login' }],
        });
    } catch (err) {
        console.log(err);
        dispatch({ type: ERROR, error: err });
    }
};

// COMMON METHODS (MÉTODOS RELACIONADOS A OPERACIONES SOBRE LOS PERFILES)

export const findRelatives = () => (dispatch, getState) => _findRelatives(dispatch, getState);
const _findRelatives = async (dispatch, getState) => {
    // BUSCA LOS PARIENTES RELACIONADOS AL TITULAR DE LA CUENTA

    dispatch({ type: FETCHING_RELATIVES });

    const relativesLink = getState().profile.userData._links.relatives.href;
    const token = getState().profile.token;

    try {
        const response = await fetch(relativesLink, {
            method: 'GET',
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json',
                Authorization: token.token_type + ' ' + token.access,
            },
        });

        const json = await response.json();

        if (!response.ok) {
            throw json;
        }
        
        await AsyncStorage.setItem('relatives', JSON.stringify(json._embedded.relatives));

        dispatch({ type: FETCH_RELATIVES_SUCCESS, relatives: json._embedded.relatives });
    } catch (err) {
        console.log(err);
        const relatives = await AsyncStorage.getItem('relatives');
        if (relatives) dispatch({ type: FETCH_RELATIVES_SUCCESS, relatives: JSON.parse(relatives) });
        dispatch({ type: ERROR, error: err });
    }
};

export const changeUser = (userId) => (dispatch, getState) => _changeUser(userId, dispatch, getState);
const _changeUser = async (userId, dispatch, getState) => {
    // CAMBIA EL USUARIO CON EL QUE SE ESTÁ OPERANDO EN LA APP
    const relatives = getState().profile.relatives.items;
    const owner = getState().profile.userData;
    let isOwner = false;
    const indx = findIndex(relatives, (rel) => {
        return rel.id === userId;
    });

    if (indx === -1) isOwner = true;

    try {
        await AsyncStorage.setItem(
            'selectedUser',
            isOwner ? JSON.stringify(owner) : JSON.stringify(relatives[indx])
        );
        dispatch({ type: CHANGE_USER, relative: isOwner ? owner : relatives[indx] });
    } catch (error) {
        console.log(error);
    }
};

export const clearValidationError = () => (dispatch) => _clearValidationError(dispatch);
const _clearValidationError = (dispatch) => {
    dispatch({ type: CLEAR_VALIDATION_ERROR });
};

export const registerUserDevice = (deviceToken) => (dispatch, getState) =>
    _registerUserDevice(deviceToken, dispatch, getState);
const _registerUserDevice = async (deviceToken, dispatch, getState) => {
    console.log('REGISTER USER DEVICE');
    const token = getState().profile.token;

    const data = {
        token: deviceToken,
        platform: Platform.OS,
    };

    try {
        const brand = await DeviceInfo.getBrand();
        data['brand'] = brand;
        const os_name = await DeviceInfo.getSystemName();
        data['os_name'] = os_name;
        const os_version = await DeviceInfo.getSystemVersion();
        data['os_version'] = os_version;
        const carrier = await DeviceInfo.getCarrier();
        data['carrier'] = carrier;
        const manufacturer = await DeviceInfo.getManufacturer();
        data['manufacturer'] = manufacturer;
        const model = await DeviceInfo.getModel();
        data['model'] = model;
        const mac = await DeviceInfo.getMacAddress();
        data['mac'] = mac;

        const response = await fetch(`${apiUrls['notification-service']}devices`, {
            method: 'POST',
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json',
                Authorization: token.token_type + ' ' + token.access,
            },
            body: JSON.stringify(data),
        });

        const json = await response.json();

        if (!response.ok) {
            throw json;
        }

        /* guardo token para saber si ya fui contra el server de notifications (puede que haya habido un error al momento de hacer la request, 
            entonces cada vez que la app levanta lo intento hasta que pueda) */
        await AsyncStorage.setItem('fcmToken', deviceToken);

        dispatch({ type: USER_DEVICE_REGISTED });
    } catch (error) {
        console.log(error);
        dispatch({ type: ERROR, error: error });
    }
};
