import React from 'react';
import { moderateScale } from '../../lib/size-normalizer';
import Icon from 'react-native-vector-icons/Ionicons';
import strings from '../../constants/Strings';
import * as Colors from '../../constants/Colors';
import TopNavigationButton from '../TopNavigationButton';
import TopNavigationTitle from '../TopNavigationTitle';
import HeaderWrapper, { STATUS_BAR_IOS } from './HeaderWrapper';
import { StyleSheet, Platform } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

const FamilyManagementScreenHeader = ({ pop }) => {
    const insets = useSafeAreaInsets();
    return (
        <HeaderWrapper style={[styles.transparent, { top: insets.top }]}>
            <TopNavigationButton
                type='LEFT'
                image={<Icon name='ios-arrow-back' size={moderateScale(24)} color={Colors.white} />}
                action={() => pop()}
            />
            <TopNavigationTitle title={strings.header.family} textColor={Colors.white} />
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

export default React.memo(FamilyManagementScreenHeader);
