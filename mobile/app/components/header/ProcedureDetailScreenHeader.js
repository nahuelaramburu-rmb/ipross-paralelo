import React from 'react';
import { moderateScale } from '../../lib/size-normalizer';
import Icon from 'react-native-vector-icons/Ionicons';
import strings from '../../constants/Strings';
import * as Colors from '../../constants/Colors';
import TopNavigationButton from '../TopNavigationButton';
import TopNavigationTitle from '../TopNavigationTitle';
import HeaderWrapper, { STATUS_BAR_IOS } from './HeaderWrapper';
import { useRoute } from '@react-navigation/native';
import { Platform, StyleSheet, ActivityIndicator } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

const ProcedureDetailScreenHeader = ({ pop }) => {
    const route = useRoute();
    const insets = useSafeAreaInsets();
    let leftButton = null;
    let rightButtons = [];

    const editing = route.params?.editing ?? null;
    const onCancel = route.params?.onCancel ?? null;
    const editProcedure = route.params?.editProcedure ?? null;
    const onConfirm = route.params?.onConfirm ?? null;
    const seeMessages = route.params?.seeMessages ?? null;
    const searchingProcedure = route.params?.searchingProcedure ?? null;
    const loadingUpdate = route.params?.loadingUpdate ?? null;

    if (searchingProcedure !== null && !searchingProcedure) {
        rightButtons.push(
            <TopNavigationButton
                type='RIGHT'
                key='messages'
                testID='message-nav-button'
                image={
                    <Icon
                        name='ios-chatbubbles-outline'
                        color={Colors.primaryText}
                        size={moderateScale(24)}
                    />
                }
                action={seeMessages}
            />
        );

        if (editing !== null && !editing) {
            leftButton = (
                <TopNavigationButton
                    type='LEFT'
                    testID='back-nav-button'
                    image={<Icon name='ios-arrow-back' color={Colors.primaryText} size={moderateScale(24)} />}
                    action={pop}
                />
            );
            rightButtons.push(
                <TopNavigationButton
                    key='edit'
                    type='RIGHT'
                    testID='edit-nav-button'
                    image={
                        <Icon name='ios-create-outline' color={Colors.primaryText} size={moderateScale(24)} />
                    }
                    action={editProcedure}
                />
            );
        }
        if (editing !== null && editing) {
            if (!loadingUpdate) {
                rightButtons.push(
                    <TopNavigationButton
                        type='RIGHT'
                        key='confirm'
                        testID='confirm-edit-nav-button'
                        image={
                            <Icon name='ios-checkmark' color={Colors.primaryText} size={moderateScale(24)} />
                        }
                        action={onConfirm}
                    />
                );
            } else {
                rightButtons.push(
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
                    testID='dismiss-nav-button'
                    image={<Icon name='ios-close' color={Colors.primaryText} size={moderateScale(24)} />}
                    action={onCancel}
                />
            );
        }
        if (!editing) {
            leftButton = (
                <TopNavigationButton
                    type='LEFT'
                    testID='back-nav-button'
                    image={<Icon name='ios-arrow-back' color={Colors.primaryText} size={moderateScale(24)} />}
                    action={pop}
                />
            );
        }
    }

    return (
        <HeaderWrapper style={[styles.shadow, { top: insets.top }]}>
            <TopNavigationTitle title={strings.header.procedure_detail} />
            {leftButton}
            {rightButtons}
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

export default React.memo(ProcedureDetailScreenHeader);
