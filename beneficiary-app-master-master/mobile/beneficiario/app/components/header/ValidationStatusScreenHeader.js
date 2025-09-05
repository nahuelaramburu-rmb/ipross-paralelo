import React, { useRef } from 'react';
import { moderateScale } from '../../lib/size-normalizer';
import Icon from 'react-native-vector-icons/Ionicons';
import * as Colors from '../../constants/Colors';
import TopNavigationButton from '../TopNavigationButton';
import TopNavigationTitle from '../TopNavigationTitle';
import HeaderWrapper, { STATUS_BAR_IOS } from './HeaderWrapper';
import { Platform, StyleSheet } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import Menu, { MenuDivider, MenuItem } from '../menu';
import { font_styles } from '../../lib/default-styles';
import strings from '../../constants/Strings';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { shallowEqual, useSelector } from 'react-redux';
import { posibleStatuses } from '../../lib/utils';

const ValidationStatusScreenHeader = ({ pop }) => {
    const navigation = useNavigation();
    const menuRef = useRef();
    const insets = useSafeAreaInsets();

    const { selectedAuthorization } = useSelector(
        (state) => ({
            selectedAuthorization: state.validation.selectedAuthorization.item,
        }),
        shallowEqual
    );

    const handleOpenMenu = () => {
        menuRef.current.openModal();
    };

    const handleAuthorizationRatingPress = () => {
        menuRef.current.hideModal();
        setTimeout(() => navigation.navigate('AuthorizationRating'), 400);
    };

    const handleAuthorizationUnawarenessPress = () => {
        menuRef.current.hideModal();
        setTimeout(() => navigation.navigate('AuthorizationUnawareness'), 400);
    };

    const { status } = selectedAuthorization;

    return (
        <HeaderWrapper style={[styles.transparent, { top: insets.top }]}>
            <TopNavigationButton
                image={<Icon name='ios-arrow-back' color={Colors.white} size={moderateScale(24)} />}
                type='LEFT'
                action={pop}
            />
            <Menu
                ref={menuRef}
                type='RIGHT'
                button={
                    <TopNavigationButton
                        image={
                            <Icon
                                name='ios-ellipsis-vertical'
                                color={Colors.white}
                                size={moderateScale(24)}
                            />
                        }
                        action={handleOpenMenu}
                    />
                }>
                {status && status.name === posibleStatuses.APPROVED && (
                    <>
                        <MenuItem
                            textStyle={font_styles.primary_text}
                            onPress={handleAuthorizationRatingPress}>
                            {strings.validationStatus.rate_validation}
                        </MenuItem>
                        <MenuDivider />
                    </>
                )}
                <MenuItem textStyle={font_styles.primary_text} onPress={handleAuthorizationUnawarenessPress}>
                    {strings.validationStatus.disclaim_validation}
                </MenuItem>
            </Menu>

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

export default React.memo(ValidationStatusScreenHeader);
