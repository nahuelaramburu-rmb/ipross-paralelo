import {
    FETCHING_TYPE_SPECIALTY,
    FETCH_TYPE_SPECIALTY_SUCCESS,
    FETCHING_ESPECIALTY,
    FETCH_ESPECIALTY_SUCCESS,
    FETCHING_TOWN,
    FETCH_TOWN_SUCCESS,
    FETCHING_PROFESSIONAL,
    FETCH_PROFESSIONAL_SUCCESS,
    FETCH_PROFESSIONALS_SUCCESS,
    FETCHING_PROFESSIONALS,
    FETCHING_MORE_PROFESSIONALS,
    FETCH_MORE_PROFESSIONALS_SUCCESS,
    FETCHING_MEDICAL_CENTER,
    FETCH_MEDICAL_CENTER_SUCCESS,
    FETCHING_MEDICAL_COORDINATES,
    FETCH_MEDICAL_COORDINATES_SUCCESS,
    ERROR,
} from '../constants/ActionTypes';

import { apiUrls } from '../configs/api';

export const getTypesSpecialty = (isRefresh) => (dispatch, getState) =>
    _getTypesSpecialty(isRefresh, dispatch, getState);

const _getTypesSpecialty = async (isRefresh = false, dispatch, getState) => {
    if (!isRefresh) dispatch({ type: FETCHING_TYPE_SPECIALTY });

    const token = getState().profile.token;

    try {
        const response = await fetch(encodeURI(apiUrls['api'] + `general/medical-specialty-types`), {
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

        dispatch({ type: FETCH_TYPE_SPECIALTY_SUCCESS, typesSpecialty: json, isRefresh: isRefresh });
    } catch (err) {
        console.log(err);
        dispatch({ type: ERROR, error: err });
    }
};

export const getMedicalSpecialties = (typeSpecialtyId) => (dispatch, getState) =>
    _getMedicalSpecialties(typeSpecialtyId, dispatch, getState);
const _getMedicalSpecialties = async (typeSpecialtyId, dispatch, getState) => {
    const token = getState().profile.token;

    dispatch({ type: FETCHING_ESPECIALTY });

    try {
        const response = await fetch(
            encodeURI(apiUrls['api'] + `general/medical-specialty-types/${typeSpecialtyId}`),
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

        dispatch({ type: FETCH_ESPECIALTY_SUCCESS, specialties: json });
    } catch (err) {
        console.log(err);
        dispatch({ type: ERROR, error: { error: err } });
    }
};

export const getTowns = (isRefresh) => (dispatch, getState) => _getTowns(isRefresh, dispatch, getState);

const _getTowns = async (isRefresh = false, dispatch, getState) => {
    if (!isRefresh) dispatch({ type: FETCHING_TOWN });

    const token = getState().profile.token;

    try {
        const response = await fetch(encodeURI(apiUrls['api'] + `regions/provinces/15`), {
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

        dispatch({ type: FETCH_TOWN_SUCCESS, towns: json, isRefresh: isRefresh });
    } catch (err) {
        console.log(err);
        dispatch({ type: ERROR, error: err });
    }
};

export const getProfessionalSpecialty = (specialtyId, townId, isRefresh = false, link = null) => (
    dispatch,
    getState
) => _getProfessionalSpecialty(specialtyId, townId, isRefresh, link, dispatch, getState);
const _getProfessionalSpecialty = async (specialtyId, townId, isRefresh, link, dispatch, getState) => {
    const token = getState().profile.token;

    if (!isRefresh && !link) dispatch({ type: FETCHING_PROFESSIONALS });

    if (!isRefresh && link) dispatch({ type: FETCHING_MORE_PROFESSIONALS });

    const url = link
        ? link
        : apiUrls['api'] +
          `practitioners?page=1&size=30&search=medicalSpecialties:{id=${specialtyId}},address.city:{id=${townId}},status:{name=HABILITADO}`;

    try {
        const response = await fetch(encodeURI(url), {
            method: 'GET',
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json',
                Authorization: token.token_type + ' ' + token.access,
            },
        });

        const json = await response.json();

        if (!response.ok) {
            false;
            throw json;
        }

        if (link) {
            return dispatch({
                type: FETCH_MORE_PROFESSIONALS_SUCCESS,
                practitioners: json,
            });
        }

        return dispatch({
            type: FETCH_PROFESSIONALS_SUCCESS,
            practitioners: json,
        });
    } catch (err) {
        console.log(err);
        dispatch({ type: ERROR, error: { error: err } });
    }
};

export const getPractitioner = (idPractitioner) => (dispatch, getState) =>
    _getPractitioner(idPractitioner, dispatch, getState);
const _getPractitioner = async (idPractitioner, dispatch, getState) => {
    dispatch({ type: FETCHING_PROFESSIONAL });

    const token = getState().profile.token;

    let url = apiUrls['api'] + `practitioners?page=1&size=30&search=idNumber:${idPractitioner}`;

    try {
        const response = await fetch(encodeURI(url), {
            method: 'GET',
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json',
                Authorization: token.token_type + ' ' + token.access,
            },
        });

        const json = await response.json();

        if (!response.ok) {
            dispatch({ type: ERROR, error: json });
            return;
        }

        dispatch({ type: FETCH_PROFESSIONAL_SUCCESS, practitioner: json });
    } catch (err) {
        console.log(err);
        dispatch({ type: ERROR, error: { error: err } });
    }
};

export const getMedicalCenters = (idPractitioner) => (dispatch, getState) =>
    _getMedicalCenters(idPractitioner, dispatch, getState);
const _getMedicalCenters = async (idPractitioner, dispatch, getState) => {
    dispatch({ type: FETCHING_MEDICAL_CENTER });

    const token = getState().profile.token;

    let url = apiUrls['api'] + `practitioners/${idPractitioner}/medical-centers`;

    try {
        const response = await fetch(encodeURI(url), {
            method: 'GET',
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json',
                Authorization: token.token_type + ' ' + token.access,
            },
        });

        const json = await response.json();

        if (!response.ok) {
            dispatch({ type: ERROR, error: json });
            return;
        }

        dispatch({ type: FETCH_MEDICAL_CENTER_SUCCESS, medicalCenters: json });
    } catch (err) {
        console.log(err);
        dispatch({ type: ERROR, error: { error: err } });
    }
};

export const getMedicalCoordinates = (city, street, streetNumber) => (dispatch, getState) =>
    _getMedicalCoordinates(city, street, streetNumber, dispatch, getState);
const _getMedicalCoordinates = async (city, street, streetNumber, dispatch) => {
    dispatch({ type: FETCHING_MEDICAL_COORDINATES });

    let url = apiUrls['api-maps'] + `${streetNumber}+${street},${city},+AR`;

    try {
        const response = await fetch(encodeURI(url), {
            method: 'GET',
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json',
            },
        });

        const json = await response.json();

        if (!response.ok) {
            dispatch({ type: ERROR, error: json });
            return;
        }

        dispatch({ type: FETCH_MEDICAL_COORDINATES_SUCCESS, medicalCoordinates: json });
    } catch (err) {
        console.log(err);
        dispatch({ type: ERROR, error: { error: err } });
    }
};
