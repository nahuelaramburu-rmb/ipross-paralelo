import React from 'react';
import HeaderWrapper, { STATUS_BAR_IOS } from './HeaderWrapper';
import TopNavigationTitle from '../TopNavigationTitle';
import strings from '../../constants/Strings';
import { moderateScale } from '../../lib/size-normalizer';
import * as Colors from '../../constants/Colors';
import TopNavigationButton from '../TopNavigationButton';
import Icon from 'react-native-vector-icons/Ionicons';
import { Platform, StyleSheet } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

const AppoitnmentDetailScreenHeader = ({ scene, pop }) => {
    const insets = useSafeAreaInsets();
    return (
        <HeaderWrapper style={[styles.transparent, { top: insets.top }]}>
            <TopNavigationButton
                image={<Icon name='ios-arrow-back' color={Colors.white} size={moderateScale(24)} />}
                type='LEFT'
                action={pop}
            />
            <TopNavigationTitle title={null} />
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

export default React.memo(AppoitnmentDetailScreenHeader);
