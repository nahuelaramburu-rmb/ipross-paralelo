import React from 'react';
import { Platform } from 'react-native';
import HeaderWrapper, { STATUS_BAR_IOS } from './HeaderWrapper';
import TopNavigationButton from '../TopNavigationButton';
import Icon from 'react-native-vector-icons/Ionicons';
import { moderateScale } from '../../lib/size-normalizer';
import * as Colors from '../../constants/Colors';
import { StyleSheet } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

const CommonHeader = ({ pop }) => {
    const insets = useSafeAreaInsets();
    return (
        <HeaderWrapper style={[styles.transparent, { top: insets.top }]}>
            <TopNavigationButton
                type='LEFT'
                image={<Icon name='ios-arrow-back' size={moderateScale(24)} color={Colors.primaryText} />}
                action={pop}
            />
        </HeaderWrapper>
    );
};

const styles = StyleSheet.create({
    transparent: {
        backgroundColor: '#ffffff00',
        position: 'absolute',
        top: Platform.OS === 'ios' ? STATUS_BAR_IOS : 0,
        left: 0,
        right: 0,
    },
});

export default React.memo(CommonHeader);
