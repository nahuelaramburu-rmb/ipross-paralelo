import { createStore } from 'redux';

// Reducer simple para pruebas
const simpleReducer = (state = { test: 'OK' }, action) => {
    switch (action.type) {
        default:
            return state;
    }
};

export default function configureStore() {
    return createStore(simpleReducer);
}