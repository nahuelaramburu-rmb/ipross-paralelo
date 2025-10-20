/* @flow */

import {
    FETCHING_PROCEDURES,
    FETCH_PROCEDURES_SUCCESS,
    FETCHING_CERTIFICATE_TYPES,
    FETCH_CERTIFICATE_TYPES_SUCCESS,
    FETCHING_PROCEDURE_BY_ID,
    FETCH_PROCEDURE_BY_ID_SUCCESS,
    UPDATE_PROCEDURE_SUCCESS,
    ADD_MESSAGE_TO_PROCEDURE_SUCCESS,
    FETCH_SELECTED_PROCEDURE_MESSAGES_SUCCESS,
    ERROR,
    FETCHING_MORE_PROCEDURES,
    FETCH_MORE_PROCEDURES_SUCCESS,
} from '../constants/ActionTypes';

const initialState = {
    procedures: {
        // TODOS LOS PROCEDURES DE TODO EL GRUPO FAMILIAR
        opened: {},
        closed: {},
        loading: false,
        loadingMore: false,
    },
    certificateTypes: {
        items: [],
        loading: false,
    },
    selectedProcedure: {
        loading: false,
        item: {},
    },
};

export default (state = initialState, action) => {
    switch (action.type) {
        case FETCHING_PROCEDURES:
            return {
                ...state,
                procedures: {
                    ...state.procedures,
                    loading: true,
                },
            };
        case FETCH_PROCEDURES_SUCCESS:
            return {
                ...state,
                procedures: {
                    ...state.procedures,
                    loading: false,
                    opened: action.procedures.content.opened,
                    closed: action.procedures.content.closed,
                },
            };
        case FETCHING_MORE_PROCEDURES:
            return {
                ...state,
                procedures: {
                    ...state.procedures,
                    loadingMore: true,
                },
            };
        case FETCH_MORE_PROCEDURES_SUCCESS:
            return handleFetchMoreProceduresSuccess(state, action);
        case FETCHING_CERTIFICATE_TYPES:
            return {
                ...state,
                certificateTypes: {
                    ...state.certificateTypes,
                    loading: true,
                },
            };
        case FETCH_CERTIFICATE_TYPES_SUCCESS:
            return {
                ...state,
                certificateTypes: {
                    ...state.certificateTypes,
                    loading: false,
                    items: action.certificateTypes,
                },
            };
        case FETCHING_PROCEDURE_BY_ID:
            return {
                ...state,
                selectedProcedure: {
                    ...state.selectedProcedure,
                    loading: true,
                },
            };
        case FETCH_PROCEDURE_BY_ID_SUCCESS:
            return {
                ...state,
                selectedProcedure: {
                    ...state.selectedProcedure,
                    loading: false,
                    item: action.procedure,
                    files: action.files,
                },
            };
        case UPDATE_PROCEDURE_SUCCESS:
            return {
                ...state,
                selectedProcedure: {
                    loading: false,
                    item: {},
                    files: [],
                },
            };
        case ADD_MESSAGE_TO_PROCEDURE_SUCCESS:
            return {
                ...state,
                selectedProcedure: {
                    ...state.selectedProcedure,
                    item: {
                        ...state.selectedProcedure.item,
                        messages: [...state.selectedProcedure.item.messages, action.message],
                    },
                },
            };
        case FETCH_SELECTED_PROCEDURE_MESSAGES_SUCCESS:
            return {
                ...state,
                selectedProcedure: {
                    ...state.selectedProcedure,
                    item: {
                        ...state.selectedProcedure.item,
                        messages: action.messages,
                    },
                },
            };
        case ERROR:
            return {
                ...state,
                certificateTypes: {
                    ...state.certificateTypes,
                    loading: false,
                },
                procedures: {
                    ...state.procedures,
                    loading: false,
                },
                selectedProcedure: {
                    ...state.selectedProcedure,
                    loading: false,
                },
            };
        default:
            return state;
    }
};

function handleFetchMoreProceduresSuccess(state, action) {
    const searchedProcedure = action.whichProcedure;
    const newProcedures = action.procedures._embedded.procedures;
    const newLinks = action.procedures._links;

    let _embedded = [...state.procedures[searchedProcedure]._embedded.procedures, ...newProcedures];
    let _links = newLinks;
    let proc = { ...state.procedures[searchedProcedure], _links, _embedded: { procedures: _embedded } };

    return {
        ...state,
        procedures: {
            ...state.procedures,
            [searchedProcedure]: proc,
            loadingMore: false,
        },
    };
}
