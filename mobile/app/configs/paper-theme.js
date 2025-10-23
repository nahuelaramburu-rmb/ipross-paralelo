import React from 'react';
import { Provider as PaperProvider } from 'react-native-paper';
import * as Colors from './constants/Colors';

const theme = {
    colors: {
        primary: Colors.primary, // #B2D235
        accent: Colors.accent, // #B2D235
        background: Colors.appBackground, // #FAFAFA
        surface: Colors.white, // #FFFFFF
        text: Colors.primaryText, // #252525
        disabled: Colors.disabledBackgroundButton, // #e4e5e8
        placeholder: Colors.secondaryText, // #757575
        backdrop: 'rgba(0, 0, 0, 0.5)',
    },
};

export const withPaperProvider = (Component) => {
    return function WithPaperProviderComponent(props) {
        return (
            <PaperProvider theme={theme}>
                <Component {...props} />
            </PaperProvider>
        );
    };
};

export { theme };
