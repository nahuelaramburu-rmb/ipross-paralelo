import React from 'react';
import { StatusBar, Platform } from 'react-native';
import HeaderWrapper from './HeaderWrapper';
import TopNavigationTitle from '../TopNavigationTitle';
import * as Colors from '../../constants/Colors';

const ErrorBoundaryHeader = ({ scene }) => {
    const oldAndroidDevice = Platform.OS === 'android' && Platform.Version <= 22;
    StatusBar.setBackgroundColor(oldAndroidDevice ? Colors.Colors.primaryText : Colors.white);
    StatusBar.setBarStyle('dark-content');
    StatusBar.setTranslucent(true);

    return (
        <HeaderWrapper scene={scene}>
            <TopNavigationTitle title={null} />;
        </HeaderWrapper>
    );
};

export default React.memo(ErrorBoundaryHeader);
