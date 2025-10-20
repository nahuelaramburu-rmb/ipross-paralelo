import React from 'react';
import HeaderWrapper, { STATUS_BAR_IOS } from './HeaderWrapper';
import TopNavigationButton from '../TopNavigationButton';
import TopNavigationTitle from '../TopNavigationTitle';
import Icon from 'react-native-vector-icons/Ionicons';
import strings from '../../constants/Strings';
import { useNavigation } from '@react-navigation/native';
import { moderateScale } from '../../lib/size-normalizer';
import * as Colors from '../../constants/Colors';
import { Platform, StyleSheet } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

const AppointmentScreenHeader = () => {
    const navigation = useNavigation();
    const insets = useSafeAreaInsets();

    return (
        <HeaderWrapper style={[styles.shadow, { top: insets.top }]}>
            <TopNavigationTitle title={strings.header.appointment} />
            <TopNavigationButton
                image={<Icon name='md-add' color={Colors.primaryText} size={moderateScale(24)} />}
                type='RIGHT'
                action={() => navigation.navigate('NewAppointment')}
            />
            <TopNavigationButton
                image={<Icon name='ios-arrow-back' color={Colors.primaryText} size={moderateScale(24)} />}
                type='LEFT'
                action={() => navigation.navigate('BeneficiaryInformation')}
            />
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

export default React.memo(AppointmentScreenHeader);
