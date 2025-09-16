import profile from '../profile';
import * as ActionTypes from '../../constants/ActionTypes';
import { DropDownHolder } from '../../components/DropDownHolder';

const initialState = {
    userData: null,
    status: 'initializing',
    credentials: null,
    token: null,
    login: {
        loading: false,
    },
    update_user_data: {
        loading: false,
    },
    account: {
        creating_account: false,
        validating_data: false,
        validated_data: null,
        error: false,
    },
    password_renew: {
        loading: false,
        success: false,
    },
    forgot_password: {
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

describe('profile reducer tests', () => {
    it('should return the initial state', () => {
        expect(profile(undefined, {})).toEqual(initialState);
    });

    it('should handle LOGGED_IN with profile', () => {
        const loggedInAction = {
            type: ActionTypes.LOGGED_IN,
            profile: {
                userData: {
                    name: 'name',
                    lastName: 'lastname',
                },
                credentials: {
                    username: 'username',
                    password: 'password',
                },
                token: {
                    access: 'abc',
                    token_type: 'Bearer',
                },
            },
            selectedUser: null,
        };

        const expectedState = {
            ...initialState,
            userData: {
                name: 'name',
                lastName: 'lastname',
            },
            status: 'logged_in',
            credentials: {
                username: 'username',
                password: 'password',
            },
            token: {
                access: 'abc',
                token_type: 'Bearer',
            },
            relatives: {
                ...initialState.relatives,
                selectedUser: {
                    name: 'name',
                    lastName: 'lastname',
                },
            },
        };

        expect(profile(undefined, loggedInAction)).toEqual(expectedState);
    });

    it('should handle LOGGED_IN without profile', () => {
        const loggedInAction = {
            type: ActionTypes.LOGGED_IN,
            profile: null,
            selectedUser: null,
            validatedData: null,
        };

        const expectedState = {
            ...initialState,
            status: 'logged_out',
        };

        expect(profile(undefined, loggedInAction)).toEqual(expectedState);
    });

    it('should handle LOGGED_IN with profile and selectedUser', () => {
        const loggedInAction = {
            type: ActionTypes.LOGGED_IN,
            profile: {
                userData: {
                    name: 'name',
                    lastName: 'lastname',
                },
                credentials: {
                    username: 'username',
                    password: 'password',
                },
                token: {
                    access: 'abc',
                    token_type: 'Bearer',
                },
            },
            selectedUser: {
                name: 'selectedUserName',
                lastName: 'selectedUserLastName',
            },
        };

        const expectedState = {
            ...initialState,
            userData: {
                name: 'name',
                lastName: 'lastname',
            },
            status: 'logged_in',
            credentials: {
                username: 'username',
                password: 'password',
            },
            token: {
                access: 'abc',
                token_type: 'Bearer',
            },
            relatives: {
                ...initialState.relatives,
                selectedUser: {
                    name: 'selectedUserName',
                    lastName: 'selectedUserLastName',
                },
            },
        };

        expect(profile(undefined, loggedInAction)).toEqual(expectedState);
    });

    it('should handle LOGGING_IN', () => {
        const loggingInAction = {
            type: ActionTypes.LOGGING_IN,
        };

        const expectedState = {
            ...initialState,
            login: {
                ...initialState.login,
                loading: true,
            },
        };

        expect(profile(undefined, loggingInAction)).toEqual(expectedState);
    });

    it('should handle RENEW_PASSWORD', () => {
        const renewPasswordAction = {
            type: ActionTypes.RENEW_PASSWORD,
            profile: {
                credentials: {
                    username: 'username',
                    password: 'password',
                },
                token: {
                    access: 'AAA',
                    token_type: 'Bearer',
                    expire_in: 3600,
                },
                userData: {
                    name: 'name',
                    lastName: 'lastName',
                },
            },
        };

        const expectedState = {
            ...initialState,
            credentials: {
                username: 'username',
                password: 'password',
            },
            token: {
                access: 'AAA',
                token_type: 'Bearer',
                expire_in: 3600,
            },
            userData: {
                name: 'name',
                lastName: 'lastName',
            },
            login: {
                ...initialState.login,
                loading: false,
            },
        };

        expect(profile(undefined, renewPasswordAction)).toEqual(expectedState);
    });

    it('should handle CONFIRMING_TEMPORARY_PASSWORD', () => {
        const confirmingPasswordAction = {
            type: ActionTypes.CONFIRMING_TEMPORARY_PASSWORD,
        };

        const expectedState = {
            ...initialState,
            password_renew: {
                ...initialState.password_renew,
                loading: true,
            },
        };

        expect(profile(undefined, confirmingPasswordAction)).toEqual(expectedState);
    });

    it('should handle UPDATE_TEMPORARY_PASSWORD_SUCCESS', () => {
        const updateTemporaryPasswordActionSuccess = {
            type: ActionTypes.UPDATE_TEMPORARY_PASSWORD_SUCCESS,
        };

        const expectedState = {
            ...initialState,
            password_renew: {
                ...initialState.password_renew,
                loading: false,
                success: true,
            },
        };

        expect(profile(undefined, updateTemporaryPasswordActionSuccess)).toEqual(expectedState);
    });

    it('should handle CREATING_ACCOUNT', () => {
        const creatingAccountAction = {
            type: ActionTypes.CREATING_ACCOUNT,
        };

        const expectedState = {
            ...initialState,
            account: {
                ...initialState.account,
                creating_account: true,
            },
        };

        expect(profile(undefined, creatingAccountAction)).toEqual(expectedState);
    });

    it('should handle ACCOUNT_CREATED', () => {
        const accountCreatedAction = {
            type: ActionTypes.ACCOUNT_CREATED,
        };

        DropDownHolder.alert = jest.fn();

        const expectedState = {
            ...initialState,
            account: {
                ...initialState.account,
                creating_account: false,
                validated_data: null,
            },
        };

        expect(profile(undefined, accountCreatedAction)).toEqual(expectedState);
        expect(DropDownHolder.alert).toHaveBeenCalledTimes(1);
    });

    it('should handle LOGGED_OUT with error', () => {
        const loggedOutAction = {
            type: ActionTypes.LOGGED_OUT,
            error: { error: 'Network Request Failed' },
        };

        DropDownHolder.alert = jest.fn();

        const expectedState = {
            ...initialState,
            status: 'logged_out',
            userData: null,
        };

        expect(profile(undefined, loggedOutAction)).toEqual(expectedState);
        expect(DropDownHolder.alert).toHaveBeenCalledTimes(1);
    });

    it('should handle LOGGED_OUT without error', () => {
        const loggedOutAction = {
            type: ActionTypes.LOGGED_OUT,
            error: null,
        };

        DropDownHolder.alert = jest.fn();

        const expectedState = {
            ...initialState,
            status: 'logged_out',
            userData: null,
        };

        expect(profile(undefined, loggedOutAction)).toEqual(expectedState);
        expect(DropDownHolder.alert).not.toHaveBeenCalledTimes(1);
    });

    it('should handle SENDING_OTP_EMAIL', () => {
        const sendingOtpEmailAction = {
            type: ActionTypes.SENDING_OTP_EMAIL,
        };

        const expectedState = {
            ...initialState,
            forgot_password: {
                ...initialState.forgot_password,
                email_loading: true,
            },
        };

        expect(profile(undefined, sendingOtpEmailAction)).toEqual(expectedState);
    });

    it('should handle SEND_OTP_EMAIL_SUCCESS', () => {
        const sendingOtpEmailActionSuccess = {
            type: ActionTypes.SEND_OTP_EMAIL_SUCCESS,
            email: 'email@email.com',
        };

        const expectedState = {
            ...initialState,
            forgot_password: {
                ...initialState.forgot_password,
                email_loading: false,
                email_sent: true,
                email: 'email@email.com',
            },
        };

        expect(profile(undefined, sendingOtpEmailActionSuccess)).toEqual(expectedState);
    });

    it('should handle CONFIRMING_PASSWORD_FORGOT_PASSWORD', () => {
        const confirmingForgotPasswordAction = {
            type: ActionTypes.CONFIRMING_PASSWORD_FORGOT_PASSWORD,
        };

        const expectedState = {
            ...initialState,
            forgot_password: {
                ...initialState.forgot_password,
                forgot_password_loading: true,
            },
        };

        expect(profile(undefined, confirmingForgotPasswordAction)).toEqual(expectedState);
    });

    it('should handle CONFIRM_PASSWORD_FORGOT_PASSWORD_SUCCESS', () => {
        const confirmForgotPasswordActionSuccess = {
            type: ActionTypes.CONFIRM_PASSWORD_FORGOT_PASSWORD_SUCCESS,
        };

        DropDownHolder.alert = jest.fn();

        const expectedState = {
            ...initialState,
            forgot_password: {
                ...initialState.forgot_password,
                forgot_password_loading: false,
                email: '',
                email_sent: false,
            },
        };

        expect(profile(undefined, confirmForgotPasswordActionSuccess)).toEqual(expectedState);
        expect(DropDownHolder.alert).toHaveBeenCalledTimes(1);
    });

    it('should handle FETCH_RELATIVES_SUCCESS', () => {
        const fetchRelativesSuccessAction = {
            type: ActionTypes.FETCH_RELATIVES_SUCCESS,
            relatives: [{ name: 'relative1' }, { name: 'relative2' }],
        };

        const expectedState = {
            ...initialState,
            relatives: {
                ...initialState.relatives,
                items: [{ name: 'relative1' }, { name: 'relative2' }],
                loading: false,
            },
        };

        expect(profile(undefined, fetchRelativesSuccessAction)).toEqual(expectedState);
    });

    it('should handle FETCHING_RELATIVES', () => {
        const fetchingRelativesAction = {
            type: ActionTypes.FETCHING_RELATIVES,
        };

        const expectedState = {
            ...initialState,
            relatives: {
                ...initialState.relatives,
                loading: true,
            },
        };

        expect(profile(undefined, fetchingRelativesAction)).toEqual(expectedState);
    });

    it('should handle CHANGE_USER', () => {
        const changeUserAction = {
            type: ActionTypes.CHANGE_USER,
            relative: { name: 'name', lastName: 'lastName' },
        };

        const expectedState = {
            ...initialState,
            relatives: {
                ...initialState.relatives,
                selectedUser: { name: 'name', lastName: 'lastName' },
            },
        };

        expect(profile(undefined, changeUserAction)).toEqual(expectedState);
    });

    it('should handle VALIDATING_DATA', () => {
        const validatingDataAction = {
            type: ActionTypes.VALIDATING_DATA,
        };

        const expectedState = {
            ...initialState,
            account: {
                ...initialState.account,
                validating_data: true,
            },
        };

        expect(profile(undefined, validatingDataAction)).toEqual(expectedState);
    });

    it('should handle VALIDATION_DATA_SUCCESS', () => {
        const validationDataSuccessAction = {
            type: ActionTypes.VALIDATION_DATA_SUCCESS,
            data: { name: 'name', lastName: 'lastName', resourceId: 'ABC' },
        };

        const expectedState = {
            ...initialState,
            account: {
                ...initialState.account,
                validating_data: false,
                validated_data: { name: 'name', lastName: 'lastName', resourceId: 'ABC' },
                error: false,
            },
        };

        expect(profile(undefined, validationDataSuccessAction)).toEqual(expectedState);
    });

    it('should handle VALIDATION_DATA_ERROR', () => {
        const validationDataErrorAction = {
            type: ActionTypes.VALIDATION_DATA_ERROR,
        };

        const expectedState = {
            ...initialState,
            account: {
                ...initialState.account,
                validating_data: false,
                error: true,
            },
        };

        expect(profile(undefined, validationDataErrorAction)).toEqual(expectedState);
    });

    it('should handle CLEAR_VALIDATION_ERROR', () => {
        const clearValidationErrorAction = {
            type: ActionTypes.CLEAR_VALIDATION_ERROR,
        };

        const expectedState = {
            ...initialState,
            account: {
                ...initialState.account,
                error: false,
            },
        };

        expect(profile(undefined, clearValidationErrorAction)).toEqual(expectedState);
    });

    it('should handle UPDATING_SELECTED_USER_DATA', () => {
        const updatingSelectedUserDataAction = {
            type: ActionTypes.UPDATING_SELECTED_USER_DATA,
        };

        const expectedState = {
            ...initialState,
            update_user_data: {
                ...initialState.update_user_data,
                loading: true,
            },
        };

        expect(profile(undefined, updatingSelectedUserDataAction)).toEqual(expectedState);
    });

    it('should handle SELECTED_USER_DATA_UPDATED', () => {
        const selectedUserDataUpdatedAction = {
            type: ActionTypes.SELECTED_USER_DATA_UPDATED,
            data: { name: 'name', lastName: 'lastName' },
        };

        const expectedState = {
            ...initialState,
            update_user_data: {
                ...initialState.update_user_data,
                loading: false,
            },
            relatives: {
                ...initialState.relatives,
                selectedUser: { name: 'name', lastName: 'lastName' },
            },
        };

        expect(profile(undefined, selectedUserDataUpdatedAction)).toEqual(expectedState);
    });

    it('should handle ERROR', () => {
        const errorAction = {
            type: ActionTypes.ERROR,
        };

        const expectedState = {
            ...initialState,
            update_user_data: {
                ...initialState.update_user_data,
                loading: false,
            },
            login: {
                ...initialState.login,
                loading: false,
            },
            password_renew: {
                ...initialState.password_renew,
                loading: false,
            },
            account: {
                ...initialState.account,
                loading: false,
                validating_data: false,
                creating_account: false,
            },
            forgot_password: {
                ...initialState.forgot_password,
                email_loading: false,
                forgot_password_loading: false,
            },
        };

        expect(profile(undefined, errorAction)).toEqual(expectedState);
    });
});
