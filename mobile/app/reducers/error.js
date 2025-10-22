/* @flow */

import { ERROR, CLEAR_ERROR, ERROR_REPORTERED } from '../constants/ActionTypes';

import { DropDownHolder } from '../components/DropDownHolder';
import errorCodes from '../constants/ErrorCodes';

const initialState = {
    data: [],
    status: null,
    notify: false,
};

/*
    action.error instanceof Error -> error de runtime (ej: cannot read prop of undefined) -> no quiero mostrar ese error, muestro default
    !action.error instanceof Error && action.error.message -> es un error de request (400, 500, etc) -> muestro el error que viene del server
    !action.error instanceof Error && !action.error.message -> es un error generado por mi a proposito (ej: falta el campo x en tal objeto)
*/

export default (state = initialState, action) => {
    switch (action.type) {
        case ERROR:
            return handleError(state, action);
        case ERROR_REPORTERED:
            return {
                ...state,
                data: [],
            };
        case CLEAR_ERROR:
            return {
                ...state,
                data: {},
                status: null,
                notify: false,
            };
        default:
            return state;
    }
};

function determineErrorType(error) {
    if (error instanceof Error) return 'runtimeError';
    if (!(error instanceof Error) && error.message) return 'requestError';
    if (typeof error === 'string') return 'generatedError';
}

function handleError(state, action) {
    const errorType = determineErrorType(action.error);
    const batchedErrors = [...state.data];
    if (errorType !== 'generatedError') batchedErrors.push(action.error);
    if ((typeof action.notify !== 'undefined' && action.notify) || typeof action.notify === 'undefined') {
        if (errorType === 'runtimeError') DropDownHolder.alert('error', 'Error', errorCodes['default']);
        if (errorType === 'generatedError') DropDownHolder.alert('error', 'Error', action.error);
        if (errorType === 'requestError') DropDownHolder.alert('error', 'Error', action.error.message);
    }
    return {
        ...state,
        data: batchedErrors,
        status: typeof action.status !== 'undefined' ? action.status : null,
        notify: typeof action.notify !== 'undefined' ? action.notify : false,
    };
}
