import React from 'react';
import HeaderWrapper, { STATUS_BAR_IOS } from './HeaderWrapper';
import TopNavigationButton from '../TopNavigationButton';
import Icon from 'react-native-vector-icons/Ionicons';
import { moderateScale } from '../../lib/size-normalizer';
import * as Colors from '../../constants/Colors';
import { StatusBar, StyleSheet, Platform } from 'react-native';

const SignUpModalHeader = ({ pop }) => {
    return (
        <HeaderWrapper style={styles.transparent}>
            <TopNavigationButton
                type='LEFT'
                image={<Icon name='ios-close' size={moderateScale(24)} color={Colors.primaryText} />}
                action={() => pop()}
            />
        </HeaderWrapper>
    );
};

const styles = StyleSheet.create({
    transparent: {
        backgroundColor: '#ffffff00',
        position: 'absolute',
        top: Platform.OS === 'ios' ? STATUS_BAR_IOS : StatusBar.currentHeight,
        left: 0,
        right: 0,
    },
});

export default React.memo(SignUpModalHeader);
