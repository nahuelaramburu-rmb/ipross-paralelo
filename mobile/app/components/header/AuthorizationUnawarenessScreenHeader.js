import React from 'react';
import { StyleSheet, Platform, ActivityIndicator } from 'react-native';
import HeaderWrapper, { STATUS_BAR_IOS } from './HeaderWrapper';
import TopNavigationButton from '../TopNavigationButton';
import TopNavigationTitle from '../TopNavigationTitle';
import Icon from 'react-native-vector-icons/Ionicons';
import strings from '../../constants/Strings';
import { moderateScale } from '../../lib/size-normalizer';
import * as Colors from '../../constants/Colors';
import { useRoute } from '@react-navigation/native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

const AuthorizationUnawarenessScreenHeader = ({ pop }) => {
    const insets = useSafeAreaInsets();
    const route = useRoute();

    const rightOperation = [];

    const onConfirm = route.params?.onConfirm ?? null;
    const isLoading = route.params?.isLoading ?? false;
    const cannotRate = route.params?.cannotRate ?? null;

    if (!isLoading) {
        if (!cannotRate && onConfirm) {
            rightOperation.push(
                <TopNavigationButton
                    key='check'
                    image={<Icon name='ios-checkmark' color={Colors.primaryText} size={moderateScale(24)} />}
                    type='RIGHT'
                    action={onConfirm}
                />
            );
        }
    } else {
        rightOperation.push(
            <TopNavigationButton
                type='RIGHT'
                key='loading'
                image={<ActivityIndicator size='small' color={Colors.primaryText} />}
                action={null}
            />
        );
    }

    return (
        <HeaderWrapper style={[styles.shadow, { top: insets.top }]}>
            <TopNavigationButton
                image={<Icon name='ios-arrow-back' color={Colors.primaryText} size={moderateScale(24)} />}
                type='LEFT'
                action={pop}
            />
            {rightOperation}
            <TopNavigationTitle title={strings.header.unawareness_title} />
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

export default React.memo(AuthorizationUnawarenessScreenHeader);
