import { combineReducers } from 'redux';
import { LOGGED_OUT } from '../constants/ActionTypes';

import app from './app';
import profile from './profile';
import router from './router';
import validation from './validation';
import charge from './charge';
import error from './error';
import batch from './batch';
import trip from './trip';
import procedure from './procedure';
import prescription from './prescription';
import appointment from './appointment';
import professional from './professional';

const appReducer = combineReducers({
    app,
    profile,
    router,
    validation,
    error,
    charge,
    batch,
    trip,
    procedure,
    prescription,
    appointment,
    professional
});

const rootReducer = (state, action) => {
    if (action.type === LOGGED_OUT) {
        state = undefined;
    }
    return appReducer(state, action);
};

export default rootReducer;
