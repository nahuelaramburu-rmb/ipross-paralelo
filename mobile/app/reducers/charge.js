/* @flow */

import {
    FETCHING_CHARGES,
    FETCH_CHARGES_SUCCESS,
    CLEAR_CHARGES_STATUS,
    FETCH_MORE_CHARGES_SUCCESS,
    FETCHING_MORE_CHARGES,
    ERROR,
} from '../constants/ActionTypes';

import moment from 'moment';

const initialState = {
    charges: {
        // COSEGUROS DEL USUARIO SELECCIONADO EN REDUCER DE PROFILE (RELATIVES)
        items: {},
        status: null,
        currentMonthCharge: null,
        loading: false,
        loadingMore: false,
    },
};

export default (state = initialState, action) => {
    switch (action.type) {
        case FETCHING_CHARGES:
            return {
                ...state,
                charges: {
                    ...state.charges,
                    loading: true,
                },
            };
        case FETCH_CHARGES_SUCCESS:
            const currentDate = moment();
            const chargeItem = action.charges._embedded.charges.find(
                (charge) => charge.month === currentDate.month() + 1 && charge.year === currentDate.year()
            );
            return {
                ...state,
                charges: {
                    ...state.charges,
                    loading: false,
                    items: action.charges,
                    status: action.isRefresh ? 'charges_updated' : 'charges_fetched',
                    currentMonthCharge: typeof chargeItem === 'undefined' ? null : chargeItem.chargeTotal,
                },
            };
        case FETCHING_MORE_CHARGES:
            return {
                ...state,
                charges: {
                    ...state.charges,
                    loadingMore: true,
                    status: null,
                },
            };
        case FETCH_MORE_CHARGES_SUCCESS:
            let _embedded = [
                ...state.charges.items._embedded.charges.concat(action.charges._embedded.charges),
            ];
            let _links = action.charges._links;
            let val = { _links, _embedded: { charges: _embedded } };
            return {
                ...state,
                charges: {
                    ...state.charges,
                    items: val,
                    status: null,
                    loadingMore: false,
                },
            };
        case CLEAR_CHARGES_STATUS:
            return {
                ...state,
                charges: {
                    ...state.charges,
                    status: null,
                },
            };
        case ERROR:
            return {
                ...state,
                charges: {
                    ...state.charges,
                    loading: false,
                },
            };
        default:
            return state;
    }
};
