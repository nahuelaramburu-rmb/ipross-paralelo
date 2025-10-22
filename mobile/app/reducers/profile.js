/* @flow */

import {
    LOGGED_IN,
    LOGGING_IN,
    CONFIRMING_TEMPORARY_PASSWORD,
    ACCOUNT_CREATED,
    CREATING_ACCOUNT,
    LOGGED_OUT,
    FETCH_RELATIVES_SUCCESS,
    FETCHING_RELATIVES,
    CHANGE_USER,
    ERROR,
    RENEW_PASSWORD,
    VALIDATION_DATA_SUCCESS,
    VALIDATING_DATA,
    VALIDATION_DATA_ERROR,
    CLEAR_VALIDATION_ERROR,
    SENDING_OTP_EMAIL,
    SEND_OTP_EMAIL_SUCCESS,
    UPDATING_SELECTED_USER_DATA,
    SELECTED_USER_DATA_UPDATED,
    UPDATE_TEMPORARY_PASSWORD_SUCCESS,
    CONFIRMING_PASSWORD_FORGOT_PASSWORD,
    CONFIRM_PASSWORD_FORGOT_PASSWORD_SUCCESS,
} from '../constants/ActionTypes';
import { DropDownHolder } from '../components/DropDownHolder';
import errorCodes from '../constants/ErrorCodes';
import strings from '../constants/Strings';

const initialState = {
    userData: null,
    status: 'initializing',
    credentials: null,
    token: null,
    login: {
        loading: false,
    },
    update_user_data: {
        // UPDATE USER DATA AL LEVANTAR LA APP
        loading: false,
    },
    account: {
        // CREACIÓN DE CUENTAS
        creating_account: false,
        validating_data: false,
        validated_data: null,
        error: false,
    },
    password_renew: {
        // CAMBIO DE CONTRASEÑA EN PRIMER LOGIN CUANDO LA CUENTA FUE GENERADA DESDE LA WEB
        loading: false,
        success: false,
    },
    forgot_password: {
        // MANEJO DEL OLVIDO DE CONTRASEÑA
        email_loading: false,
        email_sent: false,
        email: '',
        forgot_password_loading: false,
    },
    relatives: {
        items: [],
        selectedUser: null,
        loading: false,
    },
};

export default (state = initialState, action) => {
    switch (action.type) {
        case LOGGED_IN:
            if (action.profile !== null) {
                return {
                    ...state,
                    credentials: action.profile.credentials,
                    token: action.profile.token,
                    status: 'logged_in',
                    userData: action.profile.userData,
                    relatives: {
                        ...state.relatives,
                        selectedUser: !action.selectedUser ? action.profile.userData : action.selectedUser,
                    },
                    login: {
                        ...state.login,
                        loading: false,
                    },
                };
            } else {
                return {
                    ...state,
                    status: 'logged_out',
                };
            }
        case LOGGING_IN:
            return {
                ...state,
                login: {
                    ...state.login,
                    loading: true,
                },
            };
        case RENEW_PASSWORD:
            return {
                ...state,
                credentials: action.profile.credentials,
                token: action.profile.token,
                userData: action.profile.userData,
                login: {
                    ...state.login,
                    loading: false,
                },
            };
        case CONFIRMING_TEMPORARY_PASSWORD:
            return {
                ...state,
                password_renew: {
                    ...state.password_renew,
                    loading: true,
                },
            };
        case UPDATE_TEMPORARY_PASSWORD_SUCCESS:
            return {
                ...state,
                password_renew: {
                    ...state.password_renew,
                    loading: false,
                    success: true,
                },
            };
        case CREATING_ACCOUNT:
            return {
                ...state,
                account: {
                    ...state.account,
                    creating_account: true,
                },
            };
        case ACCOUNT_CREATED:
            DropDownHolder.alert('success', 'Éxito', strings.login.account_created);
            return {
                ...state,
                account: {
                    ...state.account,
                    creating_account: false,
                    validated_data: null,
                },
            };
        case LOGGED_OUT:
            if (action.error !== null) {
                if (typeof action.error.type !== 'undefined') {
                    DropDownHolder.alert(
                        'error',
                        'Error',
                        typeof errorCodes[action.error.type] !== 'undefined'
                            ? __DEV__
                                ? `${action.error.type} - ${errorCodes[action.error.type]}`
                                : errorCodes[action.error.type]
                            : errorCodes['default']
                    );
                } else DropDownHolder.alert('error', 'Error', errorCodes['default']);
            }
            return {
                ...state,
                status: 'logged_out',
            };
        case SENDING_OTP_EMAIL:
            return {
                ...state,
                forgot_password: {
                    ...state.forgot_password,
                    email_loading: true,
                },
            };
        case SEND_OTP_EMAIL_SUCCESS:
            return {
                ...state,
                forgot_password: {
                    ...state.forgot_password,
                    email_loading: false,
                    email_sent: true,
                    email: action.email,
                },
            };
        case CONFIRMING_PASSWORD_FORGOT_PASSWORD:
            return {
                ...state,
                forgot_password: {
                    ...state.forgot_password,
                    forgot_password_loading: true,
                },
            };
        case CONFIRM_PASSWORD_FORGOT_PASSWORD_SUCCESS:
            DropDownHolder.alert(
                'success',
                strings.forgotPassword.forgot_password_success_title,
                strings.forgotPassword.forgot_password_success_info
            );
            return {
                ...state,
                forgot_password: {
                    ...state.forgot_password,
                    forgot_password_loading: false,
                    email: '',
                    email_sent: false,
                },
            };
        case FETCH_RELATIVES_SUCCESS:
            return {
                ...state,
                relatives: {
                    ...state.relatives,
                    items: action.relatives,
                    loading: false,
                },
            };
        case FETCHING_RELATIVES:
            return {
                ...state,
                relatives: {
                    ...state.relatives,
                    loading: true,
                },
            };
        case CHANGE_USER:
            return {
                ...state,
                relatives: {
                    ...state.relatives,
                    selectedUser: action.relative,
                },
            };
        case VALIDATING_DATA:
            return {
                ...state,
                account: {
                    ...state.account,
                    validating_data: true,
                },
            };
        case VALIDATION_DATA_SUCCESS:
            return {
                ...state,
                account: {
                    ...state.account,
                    validating_data: false,
                    validated_data: action.data,
                    error: false,
                },
            };
        case VALIDATION_DATA_ERROR:
            return {
                ...state,
                account: {
                    ...state.account,
                    validating_data: false,
                    error: true,
                },
            };
        case CLEAR_VALIDATION_ERROR:
            return {
                ...state,
                account: {
                    ...state.account,
                    error: false,
                },
            };
        case UPDATING_SELECTED_USER_DATA:
            return {
                ...state,
                update_user_data: {
                    ...state.update_user_data,
                    loading: true,
                },
            };
        case SELECTED_USER_DATA_UPDATED:
            return {
                ...state,
                update_user_data: {
                    ...state.update_user_data,
                    loading: false,
                },
                relatives: {
                    ...state.relatives,
                    selectedUser: action.data,
                },
            };
        case ERROR:
            return {
                ...state,
                update_user_data: {
                    ...state.update_user_data,
                    loading: false,
                },
                login: {
                    ...state.login,
                    loading: false,
                },
                password_renew: {
                    ...state.password_renew,
                    loading: false,
                },
                account: {
                    ...state.account,
                    validating_data: false,
                    creating_account: false,
                },
                forgot_password: {
                    ...state.forgot_password,
                    email_loading: false,
                    forgot_password_loading: false,
                },
            };
        default:
            return state;
    }
};
