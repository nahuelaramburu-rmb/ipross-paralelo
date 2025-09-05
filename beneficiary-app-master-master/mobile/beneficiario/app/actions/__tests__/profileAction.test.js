import * as profileActions from '../profileAction';
import * as actionTypes from '../../constants/ActionTypes';
import * as authenticator from '../../lib/authentication';
import configureMockStore from 'redux-mock-store';
import fetchMock from 'fetch-mock';
import thunk from 'redux-thunk';
import NavigationService from '../../lib/NavigationService';
import SInfo from 'react-native-sensitive-info';
import AsyncStorage from '@react-native-community/async-storage';
import { apiUrls } from '../../configs/api';

const middlewares = [thunk];
const mockStore = configureMockStore(middlewares);

jest.mock('@react-native-community/async-storage', () => ({
    setItem: () => null,
}));

describe('Login tests', () => {
    afterEach(() => {
        jest.resetAllMocks();
        fetchMock.restore();
    });

    it('simulate authenticate error with bad credentials', () => {
        const expectedError = {
            error: {
                message: 'Bad credentials',
                path: '/oauth',
                status: '400',
                timestamp: '1557153997665',
                type: 'InvalidGrant',
            },
        };

        const expectedAction = [
            { type: actionTypes.LOGGING_IN },
            { type: actionTypes.ERROR, ...expectedError },
        ];

        const credentials = {
            username: 'mb11223344',
            password: 'Mb11223344',
        };

        authenticator.authenticateUser = jest.fn();
        authenticator.authenticateUser.mockReturnValue(expectedError);

        const store = mockStore();
        return store.dispatch(profileActions.login(credentials)).then(() => {
            expect(store.getActions()).toEqual(expectedAction);
        });
    });

    it('simulate authentication with FORCE_CHANGE_PASSWORD', () => {
        const mockedError = {
            error: {
                message: 'User is unconfirmed with challenge: FORCE_CHANGE_PASSWORD',
                path: '/oauth',
                status: '401',
                timestamp: '1557153997665',
                type: 'Unauthorized',
            },
        };

        const expectedProfile = {
            credentials: { username: 'mb11223344', password: 'Mb11223344' },
            token: {
                access: null,
                refresh: null,
                expires_in: null,
                token_type: null,
            },
            userData: null,
        };

        const expectedAction = [
            { type: actionTypes.LOGGING_IN },
            { type: actionTypes.RENEW_PASSWORD, profile: expectedProfile },
        ];

        const credentials = {
            username: 'mb11223344',
            password: 'Mb11223344',
        };

        NavigationService.navigate = jest.fn();
        authenticator.authenticateUser = jest.fn();
        authenticator.authenticateUser.mockReturnValue(mockedError);

        const store = mockStore();
        return store.dispatch(profileActions.login(credentials)).then(() => {
            expect(NavigationService.navigate).toHaveBeenCalledTimes(1);
            expect(store.getActions()).toEqual(expectedAction);
        });
    });

    it('should do user login', () => {
        const expectedAction = [
            { type: actionTypes.LOGGING_IN },
            {
                type: actionTypes.LOGGED_IN,
                profile: {
                    credentials: { username: 'mb11223344', password: 'Mb11223344' },
                    token: { access: 'AAA', refresh: 'BBB', expires_in: 123, token_type: 'AAA' },
                    userData: { name: 'AAA', lastName: 'BBB' },
                },
                selectedUser: null,
            },
        ];

        const accessData = { access_token: 'AAA', refresh_token: 'BBB', expires_in: 123, token_type: 'AAA' };
        const userData = { name: 'AAA', lastName: 'BBB' };

        const credentials = {
            username: 'mb11223344',
            password: 'Mb11223344',
        };

        authenticator.authenticateUser = jest.fn().mockImplementation(() => accessData);

        authenticator.getUserData = jest.fn().mockImplementation(() => userData);

        const store = mockStore();
        return store.dispatch(profileActions.login(credentials)).then(() => {
            expect(store.getActions()).toEqual(expectedAction);
        });
    });

    it('Update temporary password correctly', () => {
        fetchMock.putOnce(apiUrls['api'] + `users/password?username=tenchon`, {
            body: {},
            status: 204,
        });

        const new_password = { new_password: 'abc' };

        const expectedActions = [
            { type: actionTypes.CONFIRMING_TEMPORARY_PASSWORD },
            { type: actionTypes.UPDATE_TEMPORARY_PASSWORD_SUCCESS },
        ];

        const store = mockStore({
            profile: {
                credentials: {
                    username: 'tenchon',
                    password: 'prueba123',
                },
            },
        });

        return store.dispatch(profileActions.updatePassword(new_password)).then(() => {
            expect(store.getActions()).toEqual(expectedActions);
        });
    });

    it('Update temporary password with internet connection error', () => {
        fetchMock.putOnce(apiUrls['api'] + `users/password?username=tenchon`, {
            throws: 'Network request failed',
        });

        const new_password = { new_password: 'abc' };

        const expectedActions = [
            { type: actionTypes.CONFIRMING_TEMPORARY_PASSWORD },
            { type: actionTypes.ERROR, error: { error: 'Network request failed' } },
        ];

        const store = mockStore({
            profile: {
                credentials: {
                    username: 'tenchon',
                    password: 'prueba123',
                },
            },
        });

        return store.dispatch(profileActions.updatePassword(new_password)).then(() => {
            expect(store.getActions()).toEqual(expectedActions);
        });
    });

    it('Update temporary password with server error', () => {
        fetchMock.putOnce(apiUrls['api'] + `users/password?username=tenchon`, {
            status: 400,
            body: {
                status: 400,
                message: 'Password already in use',
                type: 'InvalidPassword',
                path: '/v1/users/password',
                timestamp: 1557174340242,
            },
        });

        const new_password = { new_password: 'prueba123' };

        const expectedActions = [
            { type: actionTypes.CONFIRMING_TEMPORARY_PASSWORD },
            {
                type: actionTypes.ERROR,
                error: {
                    status: 400,
                    message: 'Password already in use',
                    type: 'InvalidPassword',
                    path: '/v1/users/password',
                    timestamp: 1557174340242,
                },
            },
        ];

        const store = mockStore({
            profile: {
                credentials: {
                    username: 'tenchon',
                    password: 'prueba123',
                },
            },
        });

        return store.dispatch(profileActions.updatePassword(new_password)).then(() => {
            expect(store.getActions()).toEqual(expectedActions);
        });
    });

    it('Send OTP email for restoring password successful', () => {
        fetchMock.getOnce(`${apiUrls['api']}users/forgot?email=test@test.com`, {
            status: 204,
            body: {},
        });

        const email = { email: 'test@test.com' };

        const expectedActions = [
            { type: actionTypes.SENDING_OTP_EMAIL },
            { type: actionTypes.SEND_OTP_EMAIL_SUCCESS, email: 'test@test.com' },
        ];

        const store = mockStore();
        return store.dispatch(profileActions.sendOTPEmail(email)).then(() => {
            expect(store.getActions()).toEqual(expectedActions);
        });
    });

    it('Send OTP email for restoring password with internet connection error', () => {
        fetchMock.getOnce(`${apiUrls['api']}users/forgot?email=test@test.com`, {
            throws: 'Network request failed',
        });

        const email = { email: 'test@test.com' };

        const expectedActions = [
            { type: actionTypes.SENDING_OTP_EMAIL },
            { type: actionTypes.ERROR, error: { error: 'Network request failed' } },
        ];

        const store = mockStore();
        return store.dispatch(profileActions.sendOTPEmail(email)).then(() => {
            expect(store.getActions()).toEqual(expectedActions);
        });
    });

    it('Send OTP email for restoring password with server error', () => {
        fetchMock.getOnce(`${apiUrls['api']}users/forgot?email=test@test.com`, {
            status: 404,
            body: {
                status: 404,
                message: 'User with email: test@test.com does not exist',
                type: 'ObjectNotFound',
                path: '/v1/users/forgot',
                timestamp: 1557238609869,
            },
        });

        const email = { email: 'test@test.com' };

        const expectedActions = [
            { type: actionTypes.SENDING_OTP_EMAIL },
            {
                type: actionTypes.ERROR,
                error: {
                    status: 404,
                    message: 'User with email: test@test.com does not exist',
                    type: 'ObjectNotFound',
                    path: '/v1/users/forgot',
                    timestamp: 1557238609869,
                },
            },
        ];

        const store = mockStore();
        return store.dispatch(profileActions.sendOTPEmail(email)).then(() => {
            expect(store.getActions()).toEqual(expectedActions);
        });
    });

    it('Confirm new password on forgot password successfully', () => {
        const store = mockStore({
            profile: {
                forgot_password: {
                    email: 'test@test.com',
                },
            },
        });

        const obj = { otp_restore: '123456', new_password: 'prueba123' };

        fetchMock.putOnce(`${apiUrls['api']}users/forgot`, {
            status: 204,
            body: {},
        });

        NavigationService.reset = jest.fn();

        const expectedActions = [
            { type: actionTypes.CONFIRMING_PASSWORD_FORGOT_PASSWORD },
            { type: actionTypes.CONFIRM_PASSWORD_FORGOT_PASSWORD_SUCCESS },
        ];

        return store.dispatch(profileActions.confirmNewPassword(obj)).then(() => {
            expect(NavigationService.reset).toHaveBeenCalledTimes(1);
            expect(store.getActions()).toEqual(expectedActions);
        });
    });

    it('Confirm new password on forgot password with internet connection error', () => {
        const store = mockStore({
            profile: {
                forgot_password: {
                    email: 'test@test.com',
                },
            },
        });

        const obj = { otp_restore: '123456', new_password: 'prueba123' };

        fetchMock.putOnce(`${apiUrls['api']}users/forgot`, {
            throws: 'Network request failed',
        });

        const expectedActions = [
            { type: actionTypes.CONFIRMING_PASSWORD_FORGOT_PASSWORD },
            { type: actionTypes.ERROR, error: { error: 'Network request failed' } },
        ];

        return store.dispatch(profileActions.confirmNewPassword(obj)).then(() => {
            expect(store.getActions()).toEqual(expectedActions);
        });
    });

    it('Confirm new password on forgot password with server error', () => {
        const store = mockStore({
            profile: {
                forgot_password: {
                    email: 'test@test.com',
                },
            },
        });

        const obj = { otp_restore: '123456', new_password: 'prueba123' };

        fetchMock.putOnce(`${apiUrls['api']}users/forgot`, {
            status: 404,
            body: {
                status: 404,
                message: 'User does not exist with email: test@test.com and OTP: 123456',
                type: 'ObjectNotFound',
                path: '/v1/users/forgot',
                timestamp: 1557242556278,
            },
        });

        const expectedActions = [
            { type: actionTypes.CONFIRMING_PASSWORD_FORGOT_PASSWORD },
            {
                type: actionTypes.ERROR,
                error: {
                    status: 404,
                    message: 'User does not exist with email: test@test.com and OTP: 123456',
                    type: 'ObjectNotFound',
                    path: '/v1/users/forgot',
                    timestamp: 1557242556278,
                },
            },
        ];

        return store.dispatch(profileActions.confirmNewPassword(obj)).then(() => {
            expect(store.getActions()).toEqual(expectedActions);
        });
    });

    it('Validate user data on account creation successfully', () => {
        const userData = {
            beneficiaryCode: '13-1222133/01',
            birthDate: '10/10/2010',
            idType: 'DNI',
            idNumber: 111111,
        };

        fetchMock.getOnce(
            `${apiUrls['api']}beneficiaries/verification?beneficiaryCode=13-1222133/01&birthDate=10/10/2010&idType=DNI&idNumber=111111`,
            {
                status: 200,
                body: {
                    birthDate: '1925-05-20',
                    gender: 'MASCULINO',
                    id: 902,
                    idNumber: 1000111,
                    idType: { name: 'Documento Nacional de Identidad', id: 1, alias: 'DNI' },
                    lastName: 'Lopez',
                    name: 'Hector',
                    resourceId: '4c5ecb23-3450-4688-a9dd-a158270cf9c6',
                    tenantId: '990de2f6-13a0-43b3-87c9-aebdad9c2de8',
                    workIdNumber: null,
                },
            }
        );

        const expectedActions = [
            { type: actionTypes.VALIDATING_DATA },
            {
                type: actionTypes.VALIDATION_DATA_SUCCESS,
                data: {
                    birthDate: '1925-05-20',
                    gender: 'MASCULINO',
                    id: 902,
                    idNumber: 1000111,
                    idType: { name: 'Documento Nacional de Identidad', id: 1, alias: 'DNI' },
                    lastName: 'Lopez',
                    name: 'Hector',
                    resourceId: '4c5ecb23-3450-4688-a9dd-a158270cf9c6',
                    tenantId: '990de2f6-13a0-43b3-87c9-aebdad9c2de8',
                    workIdNumber: null,
                },
            },
        ];

        const store = mockStore();
        return store.dispatch(profileActions.validateData(userData)).then(() => {
            expect(store.getActions()).toEqual(expectedActions);
        });
    });

    it('Validate user data on account creation with internet connection error', () => {
        const userData = {
            beneficiaryCode: '13-1222133/01',
            birthDate: '10/10/2010',
            idType: 'DNI',
            idNumber: 111111,
        };

        fetchMock.getOnce(
            `${apiUrls['api']}beneficiaries/verification?beneficiaryCode=13-1222133/01&birthDate=10/10/2010&idType=DNI&idNumber=111111`,
            {
                throws: 'Network request failed',
            }
        );

        const expectedActions = [
            { type: actionTypes.VALIDATING_DATA },
            { type: actionTypes.VALIDATION_DATA_ERROR },
            { type: actionTypes.ERROR, error: { error: 'Network request failed' } },
        ];

        const store = mockStore();
        return store.dispatch(profileActions.validateData(userData)).then(() => {
            expect(store.getActions()).toEqual(expectedActions);
        });
    });

    it('Validate user data on account creation with server error', () => {
        const userData = {
            beneficiaryCode: '13-1222133/011',
            birthDate: '10/10/2010',
            idType: 'DNI',
            idNumber: 111111,
        };

        fetchMock.getOnce(
            `${apiUrls['api']}beneficiaries/verification?beneficiaryCode=13-1222133/011&birthDate=10/10/2010&idType=DNI&idNumber=111111`,
            {
                status: 404,
                body: {
                    message:
                        'Object not found with beneficiaryCode: 13-41200396/011 idNumber: 1000111 birthDate: 1925-05-20 and specified IdType',
                    path: '/v1/beneficiaries/verification',
                    status: 404,
                    timestamp: '2019-05-07T16:15:42.1044734',
                    type: 'ObjectNotFound',
                },
            }
        );

        const expectedActions = [
            { type: actionTypes.VALIDATING_DATA },
            { type: actionTypes.VALIDATION_DATA_ERROR },
            {
                type: actionTypes.ERROR,
                error: {
                    message:
                        'Object not found with beneficiaryCode: 13-41200396/011 idNumber: 1000111 birthDate: 1925-05-20 and specified IdType',
                    path: '/v1/beneficiaries/verification',
                    status: 404,
                    timestamp: '2019-05-07T16:15:42.1044734',
                    type: 'ObjectNotFound',
                },
            },
        ];

        const store = mockStore();
        return store.dispatch(profileActions.validateData(userData)).then(() => {
            expect(store.getActions()).toEqual(expectedActions);
        });
    });

    it('Confirm user creation successfully', () => {
        const account = {
            role: {
                name: 'beneficiary',
            },
            user: {
                username: 'test',
                password: 'tEst1ng@',
                email: 'tinchosampietro@hotmail.com',
                resource_id: 'c296782b-932f-4b18-a075-bae6149b920f',
                profile: {
                    name: 'Martin',
                    last_name: 'Sampietro',
                    id_number: '37781296',
                    id_type: 'DNI',
                },
            },
        };

        fetchMock.postOnce(apiUrls['api'] + 'users', {
            status: 204,
            body: {},
        });

        NavigationService.navigate = jest.fn();

        const expectedActions = [
            { type: actionTypes.CREATING_ACCOUNT },
            { type: actionTypes.ACCOUNT_CREATED, account },
        ];

        const store = mockStore();
        return store.dispatch(profileActions.confirmAccount(account)).then(() => {
            expect(NavigationService.navigate).toHaveBeenCalledTimes(1);
            expect(store.getActions()).toEqual(expectedActions);
        });
    });

    it('Confirm user creation with internet server error', () => {
        const account = {
            role: {
                name: 'beneficiary',
            },
            user: {
                username: 'test',
                password: 'tEst1ng@',
                email: 'tinchosampietro@hotmail.com',
                resource_id: 'c296782b-932f-4b18-a075-bae6149b920f',
                profile: {
                    name: 'Martin',
                    last_name: 'Sampietro',
                    id_number: '37781296',
                    id_type: 'DNI',
                },
            },
        };

        fetchMock.postOnce(apiUrls['api'] + 'users', {
            throws: 'Network request failed',
        });

        NavigationService.navigate = jest.fn();

        const expectedActions = [
            { type: actionTypes.CREATING_ACCOUNT },
            { type: actionTypes.ERROR, error: { error: 'Network request failed' } },
        ];

        const store = mockStore();
        return store.dispatch(profileActions.confirmAccount(account)).then(() => {
            expect(NavigationService.navigate).not.toHaveBeenCalledTimes(1);
            expect(store.getActions()).toEqual(expectedActions);
        });
    });

    it('Confirm user creation with server error', () => {
        const account = {
            role: {
                name: 'beneficiary',
            },
            user: {
                username: 'test',
                password: 'tEst1ng@',
                email: 'tinchosampietro@hotmail.com',
                resource_id: 'c296782b-932f-4b18-a075-bae6149b920f',
                profile: {
                    name: 'Martin',
                    last_name: 'Sampietro',
                    id_number: '37781296',
                    id_type: 'DNI',
                },
            },
        };

        fetchMock.postOnce(apiUrls['api'] + 'users', {
            status: 409,
            body: {
                message: 'Could not execute statement, fields already in use',
                path: '/v1/users',
                status: 409,
                timestamp: 1557259366468,
                type: 'ObjectAlreadyExists',
            },
        });

        NavigationService.navigate = jest.fn();
        AsyncStorage.setItem = jest.fn();

        const expectedActions = [
            { type: actionTypes.CREATING_ACCOUNT },
            {
                type: actionTypes.ERROR,
                error: {
                    message: 'Could not execute statement, fields already in use',
                    path: '/v1/users',
                    status: 409,
                    timestamp: 1557259366468,
                    type: 'ObjectAlreadyExists',
                },
            },
        ];

        const store = mockStore();
        return store.dispatch(profileActions.confirmAccount(account)).then(() => {
            expect(NavigationService.navigate).not.toHaveBeenCalled();
            expect(AsyncStorage.setItem).not.toHaveBeenCalled();
            expect(store.getActions()).toEqual(expectedActions);
        });
    });

    it('Logout successfully', () => {
        AsyncStorage.clear = jest.fn();
        SInfo.deleteItem = jest.fn();
        NavigationService.navigate = jest.fn();

        const expectedActions = [{ type: actionTypes.LOGGED_OUT, error: null }];

        const store = mockStore();

        return store.dispatch(profileActions.logout(null)).then(() => {
            expect(AsyncStorage.clear).toHaveBeenCalledTimes(1);
            expect(NavigationService.navigate).toHaveBeenCalledTimes(1);
            expect(SInfo.deleteItem).toHaveBeenCalledTimes(1);
            expect(store.getActions()).toEqual(expectedActions);
        });
    });

    it('Get saved data successfully with AsyncStorage empty', () => {
        AsyncStorage.getItem = jest.fn();
        AsyncStorage.getItem.mockReturnValue(null);

        const expectedActions = [{ type: actionTypes.LOGGED_IN, profile: null, selectedUser: null }];

        const store = mockStore();

        return store.dispatch(profileActions.getSavedData()).then(() => {
            expect(store.getActions()).toEqual(expectedActions);
        });
    });

    it('Get saved data successfully with profile saved on AsyncStorage', () => {
        const profile = { name: 'name', lastName: 'lastname' };
        const selectedUser = { name: 'selected_user', lastName: 'selected_user' };
        AsyncStorage.getItem = jest.fn().mockImplementationOnce(() => JSON.stringify(profile));

        authenticator.updateAccess = jest.fn();
        authenticator.updateAccess.mockReturnValue({ profile: profile, selectedUser: selectedUser });

        const expectedActions = [
            { type: actionTypes.LOGGED_IN, profile: profile, selectedUser: selectedUser },
        ];

        const store = mockStore();

        return store.dispatch(profileActions.getSavedData()).then(() => {
            expect(authenticator.updateAccess).toHaveBeenCalledTimes(1);
            expect(store.getActions()).toEqual(expectedActions);
        });
    });
});
