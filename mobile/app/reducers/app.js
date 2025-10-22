/* @flow */

import {} from '../constants/ActionTypes';

//import apiConfig from '../configs/api';

const initialState = {
    status: 'initializing',
};

export default (state = initialState, action) => {
    switch (action.type) {
        default:
            return state;
    }
};
