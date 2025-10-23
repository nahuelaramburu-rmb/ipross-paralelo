import React from 'react';
import HeaderWrapper from './HeaderWrapper';
import TopNavigationTitle from '../TopNavigationTitle';
import TopNavigationButton from '../TopNavigationButton';
import Icon from 'react-native-vector-icons/Ionicons';
import strings from '../../constants/Strings';
import { moderateScale } from '../../lib/size-normalizer';
import * as Colors from '../../constants/Colors';
import { Linking, StyleSheet } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

const BeneficiaryInformationScreenHeader = ({ openDrawer }) => {
    const insets = useSafeAreaInsets();

    const initiateWhatsApp = () => {
        let url = 'whatsapp://send?text=' + '' + '&phone=54' + 2920475511;
        Linking.openURL(url)
            .then((data) => {
                console.log('WhatsApp Opened');
            })
            .catch(() => {
                alert('Debe Tener Instalada la App Whatsapp!');
            });
    };

    return (
        <HeaderWrapper style={[styles.shadow, { top: insets.top }]}>
            <TopNavigationTitle title={strings.header.credential} />
            <TopNavigationButton
                type='LEFT'
                image={<Icon name='ios-menu-outline' size={moderateScale(24)} color={Colors.primaryText} />}
                action={() => openDrawer()}
            />
            <TopNavigationButton
                type='RIGHT'
                image={<Icon name='logo-whatsapp' size={moderateScale(24)} color={Colors.primaryText} />}
                action={() => initiateWhatsApp()}
            />
        </HeaderWrapper>
    );
};

const styles = StyleSheet.create({
    shadow: {
        position: 'absolute',
        borderBottomColor: 'rgba(0, 0, 0, 0.03)',
        shadowOffset: {
            width: 0,
            height: 1,
        },
        shadowOpacity: 0.18,
        shadowRadius: 1.0,
        elevation: 1,
        right: 0,
        left: 0,
    },
});

export default React.memo(BeneficiaryInformationScreenHeader);
