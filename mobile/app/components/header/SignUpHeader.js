import React from 'react';
import HeaderWrapper, { STATUS_BAR_IOS } from './HeaderWrapper';
import TopNavigationButton from '../TopNavigationButton';
import Icon from 'react-native-vector-icons/Ionicons';
import { moderateScale } from '../../lib/size-normalizer';
import * as Colors from '../../constants/Colors';
import { StatusBar, StyleSheet, Platform, ActivityIndicator } from 'react-native';
import { useNavigation, useRoute } from '@react-navigation/native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

const SignUpHeader = ({ pop }) => {
    const navigation = useNavigation();
    const route = useRoute();
    const insets = useSafeAreaInsets();

    const onValidate = route.params?.onValidate ?? null;
    const onConfirm = route.params?.onConfirm ?? null;
    const isValidatingData = route.params?.isValidatingData ?? false;
    const isConfirmingAccount = route.params?.isConfirmingAccount ?? false;
    const rightButtons = [];

    rightButtons.push(
        <TopNavigationButton
            type='RIGHT'
            key='info'
            image={
                <Icon
                    name='ios-information-circle-outline'
                    size={moderateScale(24)}
                    color={Colors.primaryText}
                />
            }
            action={() => navigation.navigate('SignUpHelpModal')}
        />
    );

    if (onConfirm) {
        if (isConfirmingAccount) {
            rightButtons.push(
                <TopNavigationButton
                    type='RIGHT'
                    key='loading'
                    image={<ActivityIndicator size='small' color={Colors.primaryText} />}
                    action={null}
                />
            );
        } else {
            rightButtons.push(
                <TopNavigationButton
                    key='forward'
                    image={
                        <Icon name={'ios-checkmark'} color={Colors.primaryText} size={moderateScale(24)} />
                    }
                    action={onConfirm}
                    type='RIGHT'
                />
            );
        }
    } else {
        if (onValidate) {
            if (isValidatingData) {
                rightButtons.push(
                    <TopNavigationButton
                        type='RIGHT'
                        key='loading'
                        image={<ActivityIndicator size='small' color={Colors.primaryText} />}
                        action={null}
                    />
                );
            } else {
                rightButtons.push(
                    <TopNavigationButton
                        key='forward'
                        image={
                            <Icon
                                name={'ios-arrow-forward'}
                                color={Colors.primaryText}
                                size={moderateScale(24)}
                            />
                        }
                        action={onValidate}
                        type='RIGHT'
                    />
                );
            }
        }
    }

    return (
        <HeaderWrapper style={[styles.transparent, { top: insets.top }]}>
            <TopNavigationButton
                type='LEFT'
                image={<Icon name='ios-arrow-back' size={moderateScale(24)} color={Colors.primaryText} />}
                action={() => pop()}
            />
            {rightButtons}
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

export default React.memo(SignUpHeader);
