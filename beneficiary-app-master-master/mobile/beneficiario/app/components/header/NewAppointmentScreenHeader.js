import React from 'react';
import { moderateScale } from '../../lib/size-normalizer';
import Icon from 'react-native-vector-icons/Ionicons';
import strings from '../../constants/Strings';
import * as Colors from '../../constants/Colors';
import TopNavigationButton from '../TopNavigationButton';
import TopNavigationTitle from '../TopNavigationTitle';
import HeaderWrapper, { STATUS_BAR_IOS } from './HeaderWrapper';
import { useRoute } from '@react-navigation/native';
import { StyleSheet, Platform, StatusBar, ActivityIndicator } from 'react-native';

const NewAppointmentScreenHeader = ({ pop }) => {
    const route = useRoute();

    let rightButton = null;
    let leftButton = null;
    const onConfirm = route.params?.onConfirm ?? null;
    const loading = route.params?.loading ?? null;
    const loadingCreation = route.params?.loadingCreation ?? null;
    
    if (loading !== null && !loading) {
        if (!loadingCreation) {
            rightButton = (
                <TopNavigationButton
                    type='RIGHT'
                    testID='create-procedure-nav-button'
                    image={<Icon name='ios-checkmark' color={Colors.primaryText} size={moderateScale(24)} />}
                    action={onConfirm}
                />
            );
        } else {
            rightButton = (
                <TopNavigationButton
                    type='RIGHT'
                    key='loading'
                    image={<ActivityIndicator size='small' color={Colors.primaryText} />}
                    action={null}
                />
            );
        }

        leftButton = (
            <TopNavigationButton
                type='LEFT'
                testID='back-nav-button'
                image={<Icon name='ios-arrow-back' color={Colors.primaryText} size={moderateScale(24)} />}
                action={pop}
            />
        );
    }

    return (
        <HeaderWrapper style={styles.shadow}>
            {leftButton}
            <TopNavigationTitle title={strings.header.new_appointment} />
            {rightButton}
        </HeaderWrapper>
    );
};

const styles = StyleSheet.create({
    shadow: {
        top: Platform.OS === 'ios' ? STATUS_BAR_IOS : StatusBar.currentHeight,
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

export default React.memo(NewAppointmentScreenHeader);
