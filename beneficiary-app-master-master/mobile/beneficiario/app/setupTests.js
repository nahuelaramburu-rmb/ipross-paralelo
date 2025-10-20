/* eslint-env jest */
import mockRNCNetInfo from '@react-native-community/netinfo/jest/netinfo-mock.js';

jest.mock('react-native-device-info', () => {
    return {};
});
/*
jest.mock('react-native-gesture-handler', () => {
    const React = require('react');
    const RealModule = require.requireActual('react-native-gesture-handler');
    const Touchable = require('react-native/Libraries/Components/Touchable/TouchableOpacity');
    const MyModule = {
        ...RealModule,
        RectButton: (props) => {
            return <Touchable {...props}>{props.children}</Touchable>;
        },
    };
    return MyModule;
});
*/
jest.mock('react-native-image-picker');

jest.mock('react-native-config', () => ({
    LOGIN_KEY: 'a',
    LOGIN_SECRET: 'a',
    REPORTSERVER_KEY: 'a',
    REPORTSERVER_SECRET: 'a',
    TOKEN_KEY: 'a',
    QR_SECRET: 'a',
    QR_IV_SECRET: 'a',
    HOST: 'undefined',
}));

jest.mock('@react-native-community/netinfo', () => mockRNCNetInfo);

jest.mock('react-native-sensitive-info', () => ({
    setItem: () => {},
    deleteItem: () => {},
}));

jest.mock('./lib/size-normalizer.js', () => ({
    scale: jest.fn(),
    verticalScale: jest.fn(),
    moderateScale: jest.fn(),
}));

jest.mock('./lib/default-styles.js', () => ({
    font_styles: {},
}));

jest.mock('@react-native-firebase/app', () => ({
    messaging: jest.fn(() => ({
        hasPermission: jest.fn(() => Promise.resolve(true)),
        subscribeToTopic: jest.fn(),
        unsubscribeFromTopic: jest.fn(),
        requestPermission: jest.fn(() => Promise.resolve(true)),
        getToken: jest.fn(() => Promise.resolve('myMockToken')),
    })),
    notifications: jest.fn(() => ({
        onNotification: jest.fn(),
        onNotificationDisplayed: jest.fn(),
    })),
    analytics: jest.fn(() => ({
        logEvent: jest.fn(),
        setUserProperties: jest.fn(),
        setUserId: jest.fn(),
        setCurrentScreen: jest.fn(),
    })),
}));

jest.mock('./lib/NavigationService.js', () => ({
    navigate: jest.fn(),
    reset: jest.fn(),
}));
