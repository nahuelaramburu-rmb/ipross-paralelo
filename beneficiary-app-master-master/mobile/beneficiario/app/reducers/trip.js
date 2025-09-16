/* @flow */

import { FETCHING_TRIPS, FETCH_TRIPS_SUCCESS } from '../constants/ActionTypes';

const initialState = {
    trips: {
        // TRIPS DEL USUARIO SELECCIONADO EN REDUCER DE PROFILE (RELATIVES)
        items: {},
        loading: false,
    },
};

export default (state = initialState, action) => {
    switch (action.type) {
        case FETCHING_TRIPS:
            return {
                ...state,
                trips: {
                    ...state.trips,
                    loading: true,
                },
            };
        case FETCH_TRIPS_SUCCESS:
            return {
                ...state,
                trips: {
                    ...state.trips,
                    loading: false,
                    items: { ...action.trips },
                },
            };
        default:
            return state;
    }
};