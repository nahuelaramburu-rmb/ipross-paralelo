import {
    FETCHING_PROCEDURES,
    FETCH_PROCEDURES_SUCCESS,
    FETCHING_CERTIFICATE_TYPES,
    FETCH_CERTIFICATE_TYPES_SUCCESS,
    CREATING_PROCEDURE,
    CREATE_PROCEDURE_SUCCESS,
    FETCHING_PROCEDURE_BY_ID,
    FETCH_PROCEDURE_BY_ID_SUCCESS,
    UPDATING_PROCEDURE,
    UPDATE_PROCEDURE_SUCCESS,
    ADDING_MESSAGE_TO_PROCEDURE,
    ADD_MESSAGE_TO_PROCEDURE_SUCCESS,
    FETCHING_SELECTED_PROCEDURE_MESSAGES,
    FETCH_SELECTED_PROCEDURE_MESSAGES_SUCCESS,
    FETCHING_MORE_PROCEDURES,
    FETCH_MORE_PROCEDURES_SUCCESS,
    ERROR,
    FETCH_AUTHORIZATION_PROCEDURE_SUCCESS,
} from '../constants/ActionTypes';
import { apiUrls } from '../configs/api';

const procedureTranslate = {
    CUDProcedure: 'cud',
    DisabilityProcedure: 'disability',
    CertificateProcedure: 'certificate',
    UnknownAuthorizationProcedure: 'unknown-authorization',
};

export const getProcedures = (isRefresh) => (dispatch, getState) =>
    _getProcedures(isRefresh, dispatch, getState);
const _getProcedures = async (isRefresh = false, dispatch, getState) => {
    if (!isRefresh) dispatch({ type: FETCHING_PROCEDURES });
    const token = getState().profile.token;

    try {
        const response = await fetch(
            encodeURI(
                apiUrls['api'] +
                    'procedures?page=1&size=20&groups=opened-status:{name=EN REVISION},closed-status!{name=EN REVISION},'
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

        dispatch({ type: FETCH_PROCEDURES_SUCCESS, procedures: json });
    } catch (err) {
        console.log(err);
        dispatch({ type: ERROR, error: err });
    }
};

export const searchNextPage = (whichProcedure) => (dispatch, getState) =>
    _searchNextPage(whichProcedure, dispatch, getState);
const _searchNextPage = async (whichProcedure, dispatch, getState) => {
    const nextPage = getState().procedure.procedures[whichProcedure]._links?.next ?? undefined;
    if (typeof nextPage === 'undefined') return;
    dispatch({ type: FETCHING_MORE_PROCEDURES });
    const token = getState().profile.token;

    try {
        const response = await fetch(nextPage.href, {
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

        dispatch({ type: FETCH_MORE_PROCEDURES_SUCCESS, procedures: json, whichProcedure });
    } catch (err) {
        console.log(err);
        dispatch({ type: ERROR, error: { error: err } });
    }
};

export const getCertificateTypes = () => (dispatch, getState) => _getCertificateTypes(dispatch, getState);
const _getCertificateTypes = async (dispatch, getState) => {
    const token = getState().profile.token;
    dispatch({ type: FETCHING_CERTIFICATE_TYPES });

    try {
        const response = await fetch(apiUrls['api'] + 'procedures/certificate/certificate-types', {
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

        dispatch({ type: FETCH_CERTIFICATE_TYPES_SUCCESS, certificateTypes: json });
    } catch (err) {
        console.log(err);
        dispatch({ type: ERROR, error: err });
    }
};

export const createProcedure =
    (data, images = []) =>
    (dispatch, getState) =>
        _createProcedure(data, images, dispatch, getState);
const _createProcedure = async (data, images, dispatch, getState) => {
    const token = getState().profile.token;
    dispatch({ type: CREATING_PROCEDURE });

    let formData = new FormData();
    images.forEach((image) => {
        formData.append('file', image);
    });

    formData.append('body', JSON.stringify(data));

    try {
        const response = await fetch(
            apiUrls['api'] + `procedures/${procedureTranslate[data.procedureType]}`,
            {
                method: 'POST',
                body: formData,
                headers: {
                    Authorization: token.token_type + ' ' + token.access,
                },
            }
        );

        let json = null;

        if (response.status !== 201) json = await response.json();

        if (!response.ok) {
            throw json;
        }

        dispatch({ type: CREATE_PROCEDURE_SUCCESS });

        return response.headers.get('location');
    } catch (err) {
        console.log('Error Aaaaaaaaaaaaaaaa');
        console.log(err);

        dispatch({ type: ERROR, error: err });
        throw err;
    }
};

export const getProcedureById =
    ({ link, procedureId, procedureType }) =>
    (dispatch, getState) =>
        _getProcedureById({ link, procedureId, procedureType }, dispatch, getState);
const _getProcedureById = async ({ link, procedureId, procedureType }, dispatch, getState) => {
    const token = getState().profile.token;
    dispatch({ type: FETCHING_PROCEDURE_BY_ID });

    let url = link;
    if (!url) url = getProcedureUrl(procedureId, procedureType);

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

        dispatch(getProcedureFiles(json));
    } catch (err) {
        console.log(err);
        dispatch({ type: ERROR, error: err });
    }
};

function getProcedureUrl(procedureId, procedureType) {
    return `${apiUrls['api']}procedures/${procedureTranslate[procedureType]}/${procedureId}`;
}

export const getProcedureFiles = (procedure) => (dispatch, getState) =>
    _getProcedureFiles(procedure, dispatch, getState);
const _getProcedureFiles = async (procedure, dispatch, getState) => {
    const token = getState().profile.token;

    try {
        const response = await fetch(procedure._links.files.href, {
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

        dispatch({ type: FETCH_PROCEDURE_BY_ID_SUCCESS, procedure: procedure, files: json });
    } catch (err) {
        console.log(err);
        dispatch({ type: ERROR, error: err });
    }
};

export const updateProcedure = (link, data, images, deletedFiles) => (dispatch, getState) =>
    _updateProcedure(link, data, images, deletedFiles, dispatch, getState);
const _updateProcedure = async (link, data, images, deletedFiles, dispatch, getState) => {
    const token = getState().profile.token;
    dispatch({ type: UPDATING_PROCEDURE });

    let formData = new FormData();
    images.forEach((image) => {
        formData.append('file', image);
    });

    formData.append('body', JSON.stringify({}));
    formData.append('filesToRemove', deletedFiles.join(','));

    try {
        const response = await fetch(link, {
            method: 'PATCH',
            body: formData,
            headers: {
                'Content-Type': 'multipart/form-data',
                Authorization: token.token_type + ' ' + token.access,
            },
        });

        let json = null;

        if (response.status !== 201) json = await response.json();

        if (!response.ok) {
            throw json;
        }

        dispatch({ type: UPDATE_PROCEDURE_SUCCESS });
    } catch (err) {
        console.log(err);
        dispatch({ type: ERROR, error: err });
        throw err;
    }
};

export const addMessageToProcedure = (link, message) => (dispatch, getState) =>
    _addMessageToProcedure(link, message, dispatch, getState);
const _addMessageToProcedure = async (link, message, dispatch, getState) => {
    const token = getState().profile.token;
    dispatch({ type: ADDING_MESSAGE_TO_PROCEDURE });

    try {
        const response = await fetch(`${link}/messages`, {
            method: 'PUT',
            body: JSON.stringify(message),
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json',
                Authorization: token.token_type + ' ' + token.access,
            },
        });

        let json = null;

        if (response.status !== 201) json = await response.json();

        if (!response.ok) {
            throw json;
        }

        dispatch({ type: ADD_MESSAGE_TO_PROCEDURE_SUCCESS, message: json });
    } catch (err) {
        console.log(err);
        dispatch({ type: ERROR, error: err });
        throw err;
    }
};

export const refreshCurrentProcedureMessages = (link) => (dispatch, getState) =>
    _refreshCurrentProcedureMessages(link, dispatch, getState);
const _refreshCurrentProcedureMessages = async (link, dispatch, getState) => {
    const token = getState().profile.token;
    dispatch({ type: FETCHING_SELECTED_PROCEDURE_MESSAGES });

    try {
        const response = await fetch(`${link}/messages`, {
            method: 'GET',
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json',
                Authorization: token.token_type + ' ' + token.access,
            },
        });

        let json = null;

        if (response.status !== 201) json = await response.json();

        if (!response.ok) {
            throw json;
        }

        dispatch({ type: FETCH_SELECTED_PROCEDURE_MESSAGES_SUCCESS, messages: json });
    } catch (err) {
        console.log(err);
        dispatch({ type: ERROR, error: err });
        throw err;
    }
};

export const getAuthorizationRelatedProcedure = (authorizationId) => (dispatch, getState) =>
    _getAuthorizationRelatedProcedure(authorizationId, dispatch, getState);
const _getAuthorizationRelatedProcedure = async (authorizationId, dispatch, getState) => {
    const token = getState().profile.token;

    try {
        const response = await fetch(
            apiUrls['api'] + `procedures/unknown-authorization?authorizationId=${authorizationId}`,
            {
                method: 'GET',
                headers: {
                    Accept: 'application/json',
                    'Content-Type': 'application/json',
                    Authorization: token.token_type + ' ' + token.access,
                },
            }
        );

        let json = null;

        if (response.status !== 201) json = await response.json();

        if (response.status === 404) {
            return dispatch({ type: FETCH_AUTHORIZATION_PROCEDURE_SUCCESS, procedure: null });
        }

        if (!response.ok) {
            throw json;
        }

        dispatch({ type: FETCH_AUTHORIZATION_PROCEDURE_SUCCESS, procedure: json });
    } catch (err) {
        console.log(err);
        dispatch({ type: ERROR, error: err });
        throw err;
    }
};

export const selectProcedure = (procedure) => (dispatch) => _selectProcedure(procedure, dispatch);
const _selectProcedure = (procedure, dispatch) => {
    dispatch(getProcedureFiles(procedure));
};
