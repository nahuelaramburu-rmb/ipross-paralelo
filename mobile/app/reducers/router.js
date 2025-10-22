/* @flow */

import { UPDATING_CURRENT_SCENE } from '../constants/ActionTypes';

//import apiConfig from '../configs/api';

const initialState = {
    currentScene: 'Login',
};

export default (state = initialState, action) => {
    switch (action.type) {
        case UPDATING_CURRENT_SCENE:
            return {
                ...state,
                currentScene: action.scene,
            };
        default:
            return state;
    }
};
