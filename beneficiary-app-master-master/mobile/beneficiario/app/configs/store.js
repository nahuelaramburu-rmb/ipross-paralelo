import { applyMiddleware, createStore } from 'redux';
import { createLogger } from 'redux-logger';
import thunk from 'redux-thunk';

import { logout, _isExpired } from '../actions/profileAction';
import { updateAccess } from '../lib/authentication';
import { LOGGED_IN } from '../constants/ActionTypes';

import rootReducer from '../reducers';
import { useAsyncStorage } from '@react-native-async-storage/async-storage';

var tokenPromise = null;

function authMiddleware({ dispatch, getState }) {
    return (next) => async (action) => {
        if (typeof action === 'function') {
            let state = getState();
            if (state.profile.token !== null && state.profile.token.access !== null) {
                if (_isExpired(state.profile.token.access)) {
                    if (tokenPromise === null) {
                        return (tokenPromise = updateAccess()
                            .then((value) => {
                                tokenPromise = null;
                                dispatch({ type: LOGGED_IN, ...value });
                                next(action);
                            })
                            .catch((err) => {
                                console.log('error refreshing token', err);
                                dispatch(logout(err));
                            }));
                    } else {
                        return tokenPromise.then(() => {
                            tokenPromise = null;
                            next(action);
                        });
                    }
                }
            }
        }
        return next(action);
    };
}

const middleware = [authMiddleware, thunk];

//Se  carga el Logger Redux
if (__DEV__) middleware.push(createLogger());

const createStoreWithMiddleware = applyMiddleware(...middleware)(createStore);

export default function configureStore(initialState) {
    const store = createStoreWithMiddleware(rootReducer, initialState);
    return store;
}
