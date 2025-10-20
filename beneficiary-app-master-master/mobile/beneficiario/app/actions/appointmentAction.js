import AsyncStorage from '@react-native-async-storage/async-storage';

import {
    APPOINTMENT_LOGGED_IN,
    APPOINTMENT_LOGGING_IN,
    FETCHING_APPOINTMENTS,
    FETCH_APPOINTMENTS_SUCCESS,
    FETCHING_APPLICANT,
    FETCH_APPLICANT_SUCCESS,
    FETCHING_DELEGATIONS,
    FETCH_DELEGATIONS_SUCCESS,
    FETCHING_SECTORS,
    FETCH_SECTORS_SUCCESS,
    FETCHING_APPOINTMENTS_ENABLED,
    FETCH_APPOINTMENTS_ENABLED_SUCCESS,
    APPOINTMENT_CREATE,
    CREATE_APPOINTMENT_SUCCESS,
    APPOINTMENT_CONFIRM,
    FETCHING_APPOINTMENT_BY_ID,
    FETCH_APPOINTMENT_BY_ID_SUCCESS,
    ERROR,
} from '../constants/ActionTypes';

import { apiUrls } from '../configs/api';

import { userLoginAppointment } from '../lib/loginAppointment';

export const loginApiTurnos = () => (dispatch, getState) => _login(dispatch, getState);

export const searchApplicant = () => (dispatch, getState) => _searchApplicant(dispatch, getState);

const _login = async (dispatch, getState) => {
    dispatch({ type: APPOINTMENT_LOGGING_IN });

    const authResponse = await userLoginAppointment();

    let userapilogin = {
        token: {
            access: authResponse.access_token || null,
            refresh: authResponse.refresh_token || null,
            expires_in: authResponse.expires_in || null,
            token_type: authResponse.token_type || null,
        },
    };

    try {
        await AsyncStorage.setItem('userapilogin', JSON.stringify(userapilogin));

        dispatch({ type: APPOINTMENT_LOGGED_IN, userapilogin: userapilogin });

        dispatch(searchApplicant());
    } catch (err) {
        console.log(err);
        dispatch({ type: ERROR, error: err });
    }
};

const _searchApplicant = async (dispatch, getState) => {
    dispatch({ type: FETCHING_APPLICANT });

    const token = getState().appointment.token;

    const profile = getState().profile;

    try {
        const response = await fetch(
            `${apiUrls['api-turnos']}/apiv1/applicants/search?documento=${profile.credentials.username}`,
            {
                method: 'GET',
                headers: {
                    Accept: 'application/json',
                    'Content-Type': 'application/json',
                    Authorization: token.token_type + ' ' + token.access,
                },
            }
        );

        const json = await response.json();

        if (!response.ok) {
            throw json;
        }

        dispatch({ type: FETCH_APPLICANT_SUCCESS, applicant: json });

        dispatch(getAppointments());
    } catch (err) {
        if (err.status === 404) dispatch(registerApplicant());
    }
};

export const registerApplicant = () => (dispatch, getState) => _registerApplicant(dispatch, getState);
const _registerApplicant = async (dispatch, getState) => {
    const token = getState().appointment.token;

    const profile = getState().profile;

    const data = {
        documento: profile.credentials.username,
        apellidos: profile.userData.lastName,
        nombres: profile.userData.name,
        email: 'a@gmail.com',
        celular: '111111111',
    };
    try {
        const response = await fetch(apiUrls['api-turnos'] + '/apiv1/applicants', {
            method: 'POST',
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json',
                Authorization: token.token_type + ' ' + token.access,
            },
            body: JSON.stringify(data),
        });

        let json = null;

        if (response.status !== 201) json = await response.json();

        if (!response.ok) {
            throw json;
        }
    } catch (err) {
        console.log(err);
        dispatch({ type: ERROR, error: { error: err } });
    }
};

export const getAppointments =
    (isRefresh = false) =>
    (dispatch, getState) =>
        _getAppointments(isRefresh, dispatch, getState);

const _getAppointments = async (isRefresh = false, dispatch, getState) => {
    if (!isRefresh) dispatch({ type: FETCHING_APPOINTMENTS });

    const token = getState().appointment.token;

    const id = getState().appointment.applicant.id;

    try {
        const response = await fetch(
            encodeURI(
                apiUrls['api-turnos'] +
                    `/apiv1/applicants/${id}/turns?page=1&per_page=5&sort_by=fecha&sort_type=desc`
            ),
            {
                method: 'GET',
                headers: {
                    Accept: 'application/json',
                    'Content-Type': 'application/json',
                    Authorization: token.token_type + ' ' + token.access,
                },
            }
        );

        const json = await response.json();

        if (!response.ok) {
            throw json;
        }

        dispatch({ type: FETCH_APPOINTMENTS_SUCCESS, appointments: json });
    } catch (err) {
        console.log(err);
        dispatch({ type: ERROR, error: { error: err } });
    }
};

export const getAppointmentById = (appointmentId) => (dispatch, getState) =>
    _getAppointmentById(appointmentId, dispatch, getState);
const _getAppointmentById = async (appointmentId, dispatch, getState) => {
    const token = getState().appointment.token;

    dispatch({ type: FETCHING_APPOINTMENT_BY_ID });

    try {
        const response = await fetch(
            encodeURI(apiUrls['api-turnos'] + `/apiv1/turns/search?turn_id=${appointmentId}`),
            {
                method: 'GET',
                headers: {
                    Accept: 'application/json',
                    'Content-Type': 'application/json',
                    Authorization: token.token_type + ' ' + token.access,
                },
            }
        );

        const json = await response.json();

        if (!response.ok) {
            throw json;
        }

        dispatch({ type: FETCH_APPOINTMENT_BY_ID_SUCCESS, appointments: json });
    } catch (err) {
        console.log(err);
        dispatch({ type: ERROR, error: err });
    }
};

export const getDelegations = (isRefresh) => (dispatch, getState) =>
    _getDelegations(isRefresh, dispatch, getState);

const _getDelegations = async (isRefresh, dispatch, getState) => {
    if (!isRefresh) dispatch({ type: FETCHING_DELEGATIONS });

    const token = getState().appointment.token;

    try {
        const response = await fetch(encodeURI(apiUrls['api-turnos'] + '/apiv1/delegations'), {
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

        await AsyncStorage.setItem('delegations', JSON.stringify(json.data));

        dispatch({ type: FETCH_DELEGATIONS_SUCCESS, delegations: json, isRefresh: isRefresh });
    } catch (err) {
        console.log(err);
        dispatch({ type: ERROR, error: { error: err } });
    }
};

export const getDelegationSector = (delegationId) => (dispatch, getState) =>
    _getDelegationSector(delegationId, dispatch, getState);
const _getDelegationSector = async (delegationId, dispatch, getState) => {
    const token = getState().appointment.token;

    dispatch({ type: FETCHING_SECTORS });

    try {
        const response = await fetch(
            apiUrls['api-turnos'] + `/apiv1/sectors/search?delegation_id=${delegationId}`,
            {
                method: 'GET',
                headers: {
                    Accept: 'application/json',
                    'Content-Type': 'application/json',
                    Authorization: token.token_type + ' ' + token.access,
                },
            }
        );

        const json = await response.json();

        if (!response.ok) {
            false;
            throw json;
        }

        dispatch({ type: FETCH_SECTORS_SUCCESS, sectors: json });
    } catch (err) {
        console.log(err);
        dispatch({ type: ERROR, error: { error: err } });
    }
};

export const getAppointmentEnabled = (delegationId, sectorId) => (dispatch, getState) =>
    _getAppointmentEnabled(delegationId, sectorId, dispatch, getState);
const _getAppointmentEnabled = async (delegationId, sectorId, dispatch, getState) => {
    const token = getState().appointment.token;

    dispatch({ type: FETCHING_APPOINTMENTS_ENABLED });

    try {
        const response = await fetch(
            apiUrls['api-turnos'] + `/apiv1/turns?delegation_id=${delegationId}&sector_id=${sectorId}`,
            {
                method: 'GET',
                headers: {
                    Accept: 'application/json',
                    'Content-Type': 'application/json',
                    Authorization: token.token_type + ' ' + token.access,
                },
            }
        );

        const json = await response.json();

        dispatch({ type: FETCH_APPOINTMENTS_ENABLED_SUCCESS, turnos: json });
    } catch (err) {
        console.log('Error: ' + err);
        dispatch({ type: ERROR, error: { error: err } });
    }
};

export const createAppointment = (data) => (dispatch, getState) =>
    _createAppointment(data, dispatch, getState);

const _createAppointment = async (data, dispatch, getState) => {
    const token = getState().appointment.token;

    dispatch({ type: APPOINTMENT_CREATE });

    try {
        const response = await fetch(apiUrls['api-turnos'] + '/apiv1/turns', {
            method: 'POST',
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json',
                Authorization: token.token_type + ' ' + token.access,
            },
            body: JSON.stringify(data),
        });

        let json = null;

        if (response.status !== 201) json = await response.json();

        if (!response.ok) {
            throw json;
        }

        dispatch({
            type: CREATE_APPOINTMENT_SUCCESS,
            creating_appointment: false,
            appointment_created: json,
        });

        dispatch(confirmAppointment());

        return response.headers.get('location');
    } catch (err) {
        console.log(err);
        dispatch({ type: ERROR, error: err });
        throw err;
    }
};

export const confirmAppointment = () => (dispatch, getState) => _confirmAppointment(dispatch, getState);

const _confirmAppointment = async (dispatch, getState) => {
    const token = getState().appointment.token;

    const token_turno = getState().appointment.appointments_created.items.data.token;

    dispatch({ type: APPOINTMENT_CONFIRM });

    const data = {
        token: token_turno,
    };

    try {
        const response = await fetch(apiUrls['api-turnos'] + '/apiv1/turns/confirm', {
            method: 'POST',
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json',
                Authorization: token.token_type + ' ' + token.access,
            },
            body: JSON.stringify(data),
        });

        let json = null;

        if (response.status !== 201) json = await response.json();

        if (!response.ok) {
            throw json;
        }
    } catch (err) {
        console.log(err);
        dispatch({ type: ERROR, error: err });
        throw err;
    }
};

export const cancelAppointment = (token_turno) => (dispatch, getState) =>
    _cancelAppointment(token_turno, dispatch, getState);

const _cancelAppointment = async (token_turno, dispatch, getState) => {
    const token = getState().appointment.token;

    // dispatch({ type: APPOINTMENT_CONFIRM});

    const data = {
        token: token_turno,
    };

    try {
        const response = await fetch(apiUrls['api-turnos'] + '/apiv1/turns/cancel', {
            method: 'POST',
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json',
                Authorization: token.token_type + ' ' + token.access,
            },
            body: JSON.stringify(data),
        });

        let json = null;

        if (response.status !== 201) json = await response.json();

        if (!response.ok) {
            throw json;
        }

        return response.headers.get('location');
    } catch (err) {
        console.log(err);
        dispatch({ type: ERROR, error: err });
        throw err;
    }
};
