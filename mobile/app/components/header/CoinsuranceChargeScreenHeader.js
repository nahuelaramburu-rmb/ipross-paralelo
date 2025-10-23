import React from 'react';
import strings from '../../constants/Strings';
import TopNavigationTitle from '../TopNavigationTitle';
import HeaderWrapper, { STATUS_BAR_IOS } from './HeaderWrapper';
import { Platform, StyleSheet } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

const CoinsuranceChangeScreenHeader = () => {
    const insets = useSafeAreaInsets();
    return (
        <HeaderWrapper style={[styles.shadow, { top: insets.top }]}>
            <TopNavigationTitle title={strings.header.coinsurance_charges} />
        </HeaderWrapper>
    );
};

const styles = StyleSheet.create({
    shadow: {
        top: Platform.OS === 'ios' ? STATUS_BAR_IOS : 0,
        borderBottomColor: 'rgba(0, 0, 0, 0.03)',
        shadowOffset: {
            width: 0,
            height: 1,
        },
        shadowOpacity: 0.18,
        shadowRadius: 1.0,
        elevation: 1,
        position: 'absolute',
        right: 0,
        left: 0,
    },
});

export default React.memo(CoinsuranceChangeScreenHeader);
