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

const BatchDetailScreenHeader = ({ pop }) => {
    const insets = useSafeAreaInsets();
    return (
        <HeaderWrapper style={[styles.shadow, { top: insets.top }]}>
            <TopNavigationButton
                image={<Icon name='ios-arrow-back' color={Colors.primaryText} size={moderateScale(24)} />}
                type='LEFT'
                action={pop}
            />
            <TopNavigationTitle title={strings.header.batch_detail} />
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

export default React.memo(BatchDetailScreenHeader);
