import React from 'react';
import { moderateScale } from '../../lib/size-normalizer';
import Icon from 'react-native-vector-icons/Ionicons';
import strings from '../../constants/Strings';
import * as Colors from '../../constants/Colors';
import TopNavigationButton from '../TopNavigationButton';
import TopNavigationTitle from '../TopNavigationTitle';
import HeaderWrapper, { STATUS_BAR_IOS } from './HeaderWrapper';
import { StyleSheet, Platform, StatusBar } from 'react-native';
import { useNavigation } from '@react-navigation/core';

const ProfessionalMapsScreenHeader = ({pop}) => {
    
    const navigation = useNavigation();

    let leftButton = null;

    leftButton = (
        <TopNavigationButton
            type='LEFT'
            testID='back-nav-button'
            image={<Icon name='ios-arrow-back' color={Colors.primaryText} size={moderateScale(24)} />}
            // action={() => navigation.navigate('Professional')}
            action={pop}
        />
    );
    
    return (
        <HeaderWrapper style={styles.shadow}>
            {leftButton}
            <TopNavigationTitle title={strings.header.professional_maps} />
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

export default React.memo(ProfessionalMapsScreenHeader);
