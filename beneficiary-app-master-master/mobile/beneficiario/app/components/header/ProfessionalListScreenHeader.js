import React from 'react';
import HeaderWrapper, { STATUS_BAR_IOS } from './HeaderWrapper';
import TopNavigationTitle from '../TopNavigationTitle';
import strings from '../../constants/Strings';
import { useRoute } from '@react-navigation/native';
import { Platform, StyleSheet } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import TopNavigationButton from '../TopNavigationButton';
import Icon from 'react-native-vector-icons/Ionicons';
import * as Colors from '../../constants/Colors';
import { moderateScale } from '../../lib/size-normalizer';

const ProfessionalListScreenHeader = ({ pop }) => {
    const route = useRoute();
    const insets = useSafeAreaInsets();
    return (
        <HeaderWrapper
            style={[styles.header, styles.shadow, { top: insets.top }]}>
            <TopNavigationTitle title={strings.header.professional_list} />
            <TopNavigationButton
                image={<Icon name='ios-arrow-back' color={Colors.primaryText} size={moderateScale(24)} />}
                type='LEFT'
                action={pop}
            />
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
    shadow: {
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

export default React.memo(ProfessionalListScreenHeader);
