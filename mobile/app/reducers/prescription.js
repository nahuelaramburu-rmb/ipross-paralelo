import {
    FETCHING_PRESCRIPTIONS,
    FETCH_PRESCRIPTIONS_SUCCESS,
    FETCHING_MORE_PRESCRIPTIONS,
    FETCH_MORE_PRESCRIPTIONS_SUCCESS,
    ERROR,
    SELECT_PRESCRIPTION,
    FETCHING_PRESCRIPTION,
    FETCH_PRESCRIPTION_SUCCESS,
} from '../constants/ActionTypes';
import { combineReducers } from 'redux';

const prescriptionsInitialState = {
    items: {},
    loading: false,
    loadingMore: false,
};

const selectedPrescriptionInitialState = {
    item: {},
    loading: false,
};

const prescriptionList = (state = prescriptionsInitialState, action) => {
    switch (action.type) {
        case FETCHING_PRESCRIPTIONS:
            return {
                ...state,
                loading: true,
            };
        case FETCH_PRESCRIPTIONS_SUCCESS:
            return {
                ...state,
                loading: false,
                items: action.prescriptions,
            };
        case FETCH_PRESCRIPTION_SUCCESS:
            return handleFetchPrescriptionSuccess(state, action);
        case FETCHING_MORE_PRESCRIPTIONS:
            return {
                ...state,
                loadingMore: true,
            };
        case FETCH_MORE_PRESCRIPTIONS_SUCCESS:
            return handleMorePrescriptionsSuccess(state, action);
        case ERROR:
            return {
                ...state,
                loading: false,
            };
        default:
            return state;
    }
};

function handleMorePrescriptionsSuccess(state, action) {
    return {
        ...state,
        loadingMore: false,
        items: {
            ...state.items,
            _links: action.prescriptions._links,
            _embedded: {
                ...state.items._embeddded,
                prescriptions: [
                    ...state.items._embedded.prescriptions,
                    ...action.prescriptions._embedded.prescriptions,
                ],
            },
        },
    };
}

function handleFetchPrescriptionSuccess(state, action) {
    if (!state.items._embedded) return state;

    const alreadyLoadedPrescr = [...state.items._embedded.prescriptions];
    const indx = alreadyLoadedPrescr.findIndex((it) => it.id === action.prescription.id);
    if (indx > -1) return state;
    alreadyLoadedPrescr.unshift(action.prescription);

    return {
        ...state,
        items: {
            ...state.items,
            _embedded: {
                ...state.items._embedded,
                prescriptions: alreadyLoadedPrescr,
            },
        },
    };
}

const selectedPrescription = (state = selectedPrescriptionInitialState, action) => {
    switch (action.type) {
        case SELECT_PRESCRIPTION:
            return {
                ...state,
                item: action.prescription,
            };
        case FETCHING_PRESCRIPTION:
            return {
                ...state,
                loading: true,
            };
        case FETCH_PRESCRIPTION_SUCCESS:
            return {
                ...state,
                item: action.prescription,
                loading: false,
            };
        default:
            return state;
    }
};

const prescriptions = combineReducers({
    prescriptionList,
    selectedPrescription,
});

export default prescriptions;
