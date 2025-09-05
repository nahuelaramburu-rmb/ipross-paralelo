import React from 'react';
import { Provider } from 'react-redux';

import App from '../App';

import { setCustomText } from 'react-native-global-props';
import { regular } from '../constants/Fonts';

const RootContainer = (props) => {
    return (
        <Provider store={props.store}>
            <App />
        </Provider>
    );
};

const customTextProps = {
    style: {
        fontFamily: regular,
    },
};
setCustomText(customTextProps);
export default RootContainer;
