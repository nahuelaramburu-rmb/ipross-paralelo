import {
    FETCHING_VALIDATIONS,
    ERROR,
    FETCH_VALIDATIONS_SUCCESS,
    FETCHING_MORE_VALIDATIONS,
    FETCH_MORE_VALIDATION_SUCCESS,
    SELECT_VALIDATION,
    FETCHING_VALIDATION_ASSOCIATED_FILES,
    FETCH_VALIDATION_ASSOCIATED_FILES_SUCCESS,
    FETCHING_VALIDATION,
    FETCH_VALIDATION_SUCCESS,
    RANKED_AUTHORIZATION_SUCCESS,
    FETCHING_PREAUTHORIZATIONS,
    FETCHING_MORE_PREAUTHORIZATIONS,
    FETCH_MORE_PREAUTHORIZATIONS_SUCCESS,
    FETCH_PREAUTHORIZATIONS_SUCCESS,
    FETCHING_PREAUTHORIZATION,
    FETCH_PREAUTHORIZATION_SUCCESS,
} from '../constants/ActionTypes';
import { apiUrls } from '../configs/api';

export const getValidations = (isRefresh = false, queryFilter = null, link = null) => (dispatch, getState) =>
    _getValidations(isRefresh, queryFilter, link, dispatch, getState);
const _getValidations = async (isRefresh, queryFilter, link, dispatch, getState) => {
    const token = getState().profile.token;

    if (!isRefresh && !link) dispatch({ type: FETCHING_VALIDATIONS });

    if (!isRefresh && link) dispatch({ type: FETCHING_MORE_VALIDATIONS });

    const selectedUser = getState().profile.relatives.selectedUser;

    let url = link
        ? link
        : apiUrls['api'] + 'authorizations?beneficiaryId=' + selectedUser.id + '&page=1&size=20';
    if (queryFilter && !link) url += `&${queryFilter}`;

    try {
        const response = await fetch(url, {
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

        if (link) {
            return dispatch({
                type: FETCH_MORE_VALIDATION_SUCCESS,
                validations: json,
            });
        }

        return dispatch({
            type: FETCH_VALIDATIONS_SUCCESS,
            validations: json,
            isRefresh: isRefresh,
        });
    } catch (err) {
        console.log(err);
        dispatch({ type: ERROR, error: err });
        throw err;
    }
};

export const selectValidation = (validationId) => (dispatch, getState) =>
    _selectValidation(validationId, dispatch, getState);
const _selectValidation = (validationId, dispatch, getState) => {
    const validations = getState().validation.authorizationList.validations._embedded?.authorizations ?? [];
    const valIndx = validations.findIndex((val) => val.id === validationId);
    const selectedVal = validations[valIndx] ?? {};
    dispatch({ type: SELECT_VALIDATION, selectedAuthorization: selectedVal });
};

export const searchValidationAssociatedFiles = (validationId) => (dispatch, getState) =>
    _searchValidationAssociatedFiles(validationId, dispatch, getState);
const _searchValidationAssociatedFiles = async (validationId, dispatch, getState) => {
    const token = getState().profile.token;
    dispatch({ type: FETCHING_VALIDATION_ASSOCIATED_FILES });

    try {
        const response = await fetch(apiUrls['api'] + `storage/reports?authorizationId=${validationId}`, {
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

        dispatch({ type: FETCH_VALIDATION_ASSOCIATED_FILES_SUCCESS, files: json });
    } catch (err) {
        console.log(err);
        dispatch({ type: ERROR, error: { error: err } });
    }
};

export const getValidation = (id) => (dispatch, getState) => _getValidation(id, dispatch, getState);
const _getValidation = async (id, dispatch, getState) => {
    dispatch({ type: FETCHING_VALIDATION });

    const token = getState().profile.token;

    let url = apiUrls['api'] + 'authorizations/' + id;

    try {
        const response = await fetch(url, {
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

        dispatch({ type: FETCH_VALIDATION_SUCCESS, validation: json });
    } catch (err) {
        console.log(err);
        dispatch({ type: ERROR, error: { error: err } });
    }
};

export const rankAuthorization = (id, ranks) => (dispatch, getState) =>
    _rankAuthorization(id, ranks, dispatch, getState);
const _rankAuthorization = async (id, ranks, dispatch, getState) => {
    const token = getState().profile.token;

    let url = apiUrls['api'] + `authorizations/${id}/rating`;

    try {
        const response = await fetch(url, {
            method: 'PUT',
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json',
                Authorization: token.token_type + ' ' + token.access,
            },
            body: JSON.stringify(ranks),
        });

        const json = await response.json();

        if (!response.ok) {
            throw json;
        }

        dispatch({ type: RANKED_AUTHORIZATION_SUCCESS, authorization: json });
    } catch (err) {
        console.log(err);
        dispatch({ type: ERROR, error: err });
        throw err;
    }
};

export const getPreAuthorizations = (isRefresh = false, queryFilter = null, link = null) => (
    dispatch,
    getState
) => _getPreAuthorizations(isRefresh, queryFilter, link, dispatch, getState);
const _getPreAuthorizations = async (isRefresh, queryFilter, link, dispatch, getState) => {
    const token = getState().profile.token;
    if (!isRefresh && !link) dispatch({ type: FETCHING_PREAUTHORIZATIONS });

    if (!isRefresh && link) dispatch({ type: FETCHING_MORE_PREAUTHORIZATIONS });

    let url = link ? link : apiUrls['api'] + 'pre-authorizations?page=1&size=35';
    if (queryFilter && !link) url += `&${queryFilter}`;

    try {
        const response = await fetch(url, {
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

        if (link) {
            return dispatch({
                type: FETCH_MORE_PREAUTHORIZATIONS_SUCCESS,
                preAuthorizations: json,
                isRefresh: isRefresh,
            });
        }

        return dispatch({
            type: FETCH_PREAUTHORIZATIONS_SUCCESS,
            preAuthorizations: json,
            isRefresh: isRefresh,
        });
    } catch (err) {
        console.log(err);
        dispatch({ type: ERROR, error: err });
        throw err;
    }
};

export const getPreAuthorizationById = (preAuthId, isRefresh = false) => (dispatch, getState) =>
    _getPreAuthorizationById(preAuthId, isRefresh, dispatch, getState);
const _getPreAuthorizationById = async (preAuthId, isRefresh, dispatch, getState) => {
    const token = getState().profile.token;

    if (!isRefresh) dispatch({ type: FETCHING_PREAUTHORIZATION });

    const url = apiUrls['api'] + `pre-authorizations/${preAuthId}`;

    try {
        const response = await fetch(url, {
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

        return dispatch({
            type: FETCH_PREAUTHORIZATION_SUCCESS,
            preAuthorization: json,
            preAuthorizationId: preAuthId,
        });
    } catch (err) {
        console.log(err);
        dispatch({ type: ERROR, error: err });
        throw err;
    }
};
