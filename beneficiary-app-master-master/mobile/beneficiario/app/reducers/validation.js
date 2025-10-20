/* @flow */

import {
    FETCHING_VALIDATIONS,
    FETCH_VALIDATIONS_SUCCESS,
    FETCHING_MORE_VALIDATIONS,
    FETCH_MORE_VALIDATION_SUCCESS,
    SELECT_VALIDATION,
    FETCHING_VALIDATION_ASSOCIATED_FILES,
    FETCH_VALIDATION_ASSOCIATED_FILES_SUCCESS,
    FETCHING_VALIDATION,
    FETCH_VALIDATION_SUCCESS,
    RANKED_AUTHORIZATION_SUCCESS,
    FETCH_AUTHORIZATION_PROCEDURE_SUCCESS,
    ERROR,
    FETCHING_PREAUTHORIZATIONS,
    FETCHING_PREAUTHORIZATION,
    FETCH_MORE_PREAUTHORIZATIONS_SUCCESS,
    FETCHING_MORE_PREAUTHORIZATIONS,
    FETCH_PREAUTHORIZATIONS_SUCCESS,
    FETCH_PREAUTHORIZATION_SUCCESS,
} from '../constants/ActionTypes';
import { combineReducers } from 'redux';

const authorizationListInitialState = {
    loading: false,
    validations: {},
    loadingMore: false,
};

const selectedAuthorizationInitialState = {
    item: {},
    associated_files: [],
    procedure: undefined,
    loading_files: false,
    loading: false,
};

const preAuthorizationListInitialState = {
    loading: false,
    preAuthorizations: {},
    loadingMore: false,
};

const preAuthorizationDetailsInitialState = {
    preAuthorizationsById: {},
    loading: false,
};

const preAuthorizationDetails = (state = preAuthorizationDetailsInitialState, action) => {
    switch (action.type) {
        case FETCHING_PREAUTHORIZATION:
            return handleFetching(state, action);
        case FETCH_PREAUTHORIZATION_SUCCESS:
            return handleFetchPreAuthorizationSuccess(state, action);
        case ERROR:
            return handleError(state, action);
        default:
            return state;
    }
};

const preAuthorizationList = (state = preAuthorizationListInitialState, action) => {
    switch (action.type) {
        case FETCHING_PREAUTHORIZATIONS:
            return handleFetching(state, action);
        case FETCHING_MORE_PREAUTHORIZATIONS:
            return handleFetchingMore(state, action);
        case FETCH_MORE_PREAUTHORIZATIONS_SUCCESS:
            return handleFetchMorePreAuthorizationsSuccess(state, action);
        case FETCH_PREAUTHORIZATIONS_SUCCESS:
            return handleFetchPreAuthorizationsSuccess(state, action);
        case ERROR:
            return handleError(state, action);
        default:
            return state;
    }
};

const selectedAuthorization = (state = selectedAuthorizationInitialState, action) => {
    switch (action.type) {
        case SELECT_VALIDATION:
            return handleSelectAuthorization(state, action);
        case FETCHING_VALIDATION_ASSOCIATED_FILES:
            return handleFetchingAuthorizationFiles(state, action);
        case FETCH_VALIDATION_ASSOCIATED_FILES_SUCCESS:
            return handleFetchAuthorizationFilesSuccess(state, action);
        case FETCHING_VALIDATION:
            return handleFetching(state, action);
        case FETCH_VALIDATION_SUCCESS:
            return handleFetchAuthorizationSuccess(state, action);
        case RANKED_AUTHORIZATION_SUCCESS:
            return handleRankedAuthorizationSuccess(state, action);
        case FETCH_AUTHORIZATION_PROCEDURE_SUCCESS:
            return handleFetchAuthorizationProcedureSuccess(state, action);
        case ERROR:
            return handleError(state, action);
        default:
            return state;
    }
};

const authorizationList = (state = authorizationListInitialState, action) => {
    switch (action.type) {
        case FETCHING_VALIDATIONS:
            return handleFetching(state, action);
        case FETCH_VALIDATIONS_SUCCESS:
            return handleFetchAuthorizationsSuccess(state, action);
        case FETCHING_MORE_VALIDATIONS:
            return handleFetchingMore(state, action);
        case FETCH_MORE_VALIDATION_SUCCESS:
            return handleFetchMoreAuthorizationsSuccess(state, action);
        case RANKED_AUTHORIZATION_SUCCESS:
            return handleRankedAuthorizationSuccess(state, action);
        case ERROR:
            return handleError(state, action);
        default:
            return state;
    }
};

function handleError(state, action) {
    return {
        ...state,
        loading: false,
        loadingMore: false,
    };
}

function handleSelectAuthorization(state, action) {
    return {
        ...state,
        item: action.selectedAuthorization,
        procedure: undefined,
    };
}

function handleFetching(state, action) {
    return {
        ...state,
        loading: true,
    };
}

function handleFetchingMore(state, action) {
    return {
        ...state,
        loadingMore: true,
    };
}

function handleFetchingAuthorizationFiles(state, action) {
    return {
        ...state,
        loading_files: true,
    };
}

function handleFetchAuthorizationFilesSuccess(state, action) {
    return {
        ...state,
        associated_files: action.files,
        loading_files: false,
    };
}

function handleFetchAuthorizationSuccess(state, action) {
    return {
        ...state,
        item: action.validation,
        loading: false,
    };
}

function handleRankedAuthorizationSuccess(state, action) {
    if (typeof state.validations === 'undefined') {
        return {
            ...state,
            item: {
                ...state.item,
                ...action.authorization,
            },
        };
    }

    const validationsCp = state.validations._embedded ? [...state.validations._embedded.authorizations] : [];
    const indx = validationsCp.findIndex((it) => it.id === action.authorization.id);
    if (indx > -1) validationsCp.splice(indx, 1, action.authorization);
    return {
        ...state,
        validations: {
            ...state.validations,
            _embedded: {
                authorizations: validationsCp,
            },
        },
    };
}

function handleFetchAuthorizationsSuccess(state, action) {
    return {
        ...state,
        loading: false,
        validations: action.validations,
        status: action.isRefresh ? 'validations_updated' : 'validations_fetched',
    };
}

function handleFetchMoreAuthorizationsSuccess(state, action) {
    let _embedded = [
        ...state.validations._embedded.authorizations,
        ...action.validations._embedded.authorizations,
    ];
    let _links = action.validations._links;
    let val = { _links, _embedded: { authorizations: _embedded } };
    return {
        ...state,
        validations: val,
        loadingMore: false,
        status: null,
    };
}

function handleFetchMorePreAuthorizationsSuccess(state, action) {
    let _embedded = [
        ...state.preAuthorizations._embedded.preAuthorizations,
        ...action.preAuthorizations._embedded.preAuthorizations,
    ];
    let _links = action.preAuthorizations._links;
    let preVal = { _links, _embedded: { preAuthorizations: _embedded } };
    return {
        ...state,
        preAuthorizations: preVal,
        loadingMore: false,
    };
}

function handleFetchPreAuthorizationsSuccess(state, action) {
    return {
        ...state,
        loading: false,
        preAuthorizations: action.preAuthorizations,
    };
}

function handleFetchAuthorizationProcedureSuccess(state, action) {
    return {
        ...state,
        procedure: action.procedure,
    };
}

function handleFetchPreAuthorizationSuccess(state, action) {
    return {
        ...state,
        loading: false,
        preAuthorizationsById: {
            ...state.preAuthorizationsById,
            [action.preAuthorizationId]: action.preAuthorization,
        },
    };
}

const validation = combineReducers({
    preAuthorizationList,
    authorizationList,
    selectedAuthorization,
    preAuthorizationDetails,
});

export default validation;
