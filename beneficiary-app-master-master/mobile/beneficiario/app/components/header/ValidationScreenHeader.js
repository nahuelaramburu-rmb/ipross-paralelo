import React from 'react';
import { moderateScale } from '../../lib/size-normalizer';
import Icon from 'react-native-vector-icons/Ionicons';
import strings from '../../constants/Strings';
import * as Colors from '../../constants/Colors';
import TopNavigationTitle from '../TopNavigationTitle';
import HeaderWrapper, { STATUS_BAR_IOS } from './HeaderWrapper';
import { useRoute } from '@react-navigation/native';
import TopNavigationBudgeButton from '../TopNavigationBudgeButton';
import { Platform, StyleSheet } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

const ValidationScreenHeader = ({}) => {
    const insets = useSafeAreaInsets();
    const tabRoute = useRoute();
    const screenRoute = tabRoute.state?.routes[tabRoute.state.index] ?? {};

    const animatedValueHeight = screenRoute.params?.animatedValueHeight ?? null;
    const openModal = screenRoute.params?.openModal ?? null;
    const animatedValueOpacity = screenRoute.params?.animatedValueOpacity ?? null;
    const count = screenRoute.params?.filtersApplied ?? null;
    let animatedStyle = null;

    const rightButton = (
        <TopNavigationBudgeButton
            image={<Icon name='ios-options' size={moderateScale(24)} color={Colors.primaryText} />}
            type='RIGHT'
            count={count}
            action={openModal}
        />
    );

    if (animatedValueHeight && animatedValueOpacity) {
        animatedStyle = {
            transform: [{ translateY: animatedValueHeight }],
            opacity: animatedValueOpacity,
        };
    }

    return (
        <HeaderWrapper animationStyle={animatedStyle} style={[styles.header, { top: insets.top }]}>
            {rightButton}
            <TopNavigationTitle title={strings.header.validations} />
        </HeaderWrapper>
    );
};

const styles = StyleSheet.create({
    header: {
        position: 'absolute',
        top: Platform.OS === 'ios' ? STATUS_BAR_IOS : 0,
        right: 0,
        left: 0,
    },
});

export default React.memo(ValidationScreenHeader);
