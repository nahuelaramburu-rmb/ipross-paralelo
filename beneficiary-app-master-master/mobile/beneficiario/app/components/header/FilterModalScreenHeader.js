import React from 'react';
import { Platform, StatusBar, StyleSheet } from 'react-native';
import { moderateScale } from '../../lib/size-normalizer';
import Icon from 'react-native-vector-icons/Ionicons';
import strings from '../../constants/Strings';
import * as Colors from '../../constants/Colors';
import TopNavigationButton from '../TopNavigationButton';
import TopNavigationTitle from '../TopNavigationTitle';
import HeaderWrapper, { STATUS_BAR_IOS } from './HeaderWrapper';
import { useRoute } from '@react-navigation/native';
import MaterialIcon from 'react-native-vector-icons/MaterialCommunityIcons';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

const FilterModalScreenHeader = ({ pop }) => {
    const route = useRoute();
    const insets = useSafeAreaInsets();

    const filtersApplied = route.params?.filtersApplied ?? null;
    const clearFilters = route.params?.clearFilters ?? null;
    let rightButton = null;

    if (filtersApplied && filtersApplied > 0) {
        rightButton = (
            <TopNavigationButton
                type='RIGHT'
                image={
                    <MaterialIcon name='filter-remove' size={moderateScale(24)} color={Colors.primaryText} />
                }
                action={clearFilters}
            />
        );
    }

    return (
        <HeaderWrapper style={[styles.shadow, { top: insets.top }]}>
            <TopNavigationButton
                image={<Icon name='ios-close' size={moderateScale(24)} color={Colors.primaryText} />}
                type='LEFT'
                action={pop}
            />
            {rightButton}
            <TopNavigationTitle title={strings.header.filters} />
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
    },
});

export default React.memo(FilterModalScreenHeader);
