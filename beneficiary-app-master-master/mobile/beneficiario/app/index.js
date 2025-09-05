import 'react-native-gesture-handler';
import React from 'react';
import { AppRegistry } from 'react-native';
import Firebase from '@react-native-firebase/app';
import '@react-native-firebase/messaging';

import configureStore from './configs/store';
import RootContainer from './containers/RootContainer';

const store = configureStore();

Firebase.messaging().setBackgroundMessageHandler(async (remoteMessage) => {
    // SALE POR ACA CUANDO LA APP ESTÁ EN BACKGROUND O KILLED Y EN EL MESSAGE VIENE SOLO DATA (NO NOTIFICATION) -> ANDROID
    const { data } = remoteMessage;
    console.log(remoteMessage);
    
    return Promise.resolve();
});

function HeadlessCheck({ isHeadless }) {
    if (isHeadless) {
        return null;
    }

    return <RootContainer key='root' store={store} />;
}

AppRegistry.registerComponent('iprossvem', () => HeadlessCheck);
