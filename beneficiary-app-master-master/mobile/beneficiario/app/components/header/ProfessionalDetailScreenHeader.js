import React, { useRef } from 'react';
import { moderateScale } from '../../lib/size-normalizer';
import Icon from 'react-native-vector-icons/Ionicons';
import * as Colors from '../../constants/Colors';
import TopNavigationButton from '../TopNavigationButton';
import TopNavigationTitle from '../TopNavigationTitle';
import HeaderWrapper, { STATUS_BAR_IOS } from './HeaderWrapper';
import { Platform, StyleSheet } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import strings from '../../constants/Strings';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

const ProfessionalDetailScreenHeader = ({ pop }) => {
    const navigation = useNavigation();
    const insets = useSafeAreaInsets();

    return (
        <HeaderWrapper style={[styles.transparent, { top: insets.top }]}>
            <TopNavigationButton
                image={<Icon name='ios-arrow-back' color={Colors.white} size={moderateScale(24)} />}
                type='LEFT'
                action={() => navigation.navigate('Professional')}
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

export default React.memo(ProfessionalDetailScreenHeader);
