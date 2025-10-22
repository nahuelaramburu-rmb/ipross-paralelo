import React, { useCallback, useEffect, useState } from 'react';
import { shallowEqual, useDispatch, useSelector } from 'react-redux';
import { GiftedChat, InputToolbar } from 'react-native-gifted-chat';
import { ActivityIndicator, StyleSheet, View, Text } from 'react-native';
import get from 'lodash/get';
import * as Colors from '../../constants/Colors';
import Icon from 'react-native-vector-icons/Ionicons';
import { moderateScale } from '../../lib/size-normalizer';
import { TouchableOpacity } from 'react-native-gesture-handler';
import strings from '../../constants/Strings';
import { posibleStatuses } from '../../lib/utils';
import { addMessageToProcedure, refreshCurrentProcedureMessages } from '../../actions/procedureAction';
import { SafeAreaView } from 'react-native-safe-area-context';
import PropTypes from 'prop-types';
import { useFetchPolling } from '../../hooks/utils';
import { KeyboardAwareScrollView } from 'react-native-keyboard-aware-scroll-view';
import { font_styles } from '../../lib/default-styles';

const ProcedureMessagesScreen = ({ route }) => {
    const [messages, setMessages] = useState([]);
    const [isLoading, setIsLoading] = useState(false);
    const dispatch = useDispatch();

    const { loggedUserCredentials, selectedProcedure } = useSelector(
        (state) => ({
            loggedUserCredentials: state.profile.credentials,
            selectedProcedure: state.procedure.selectedProcedure.item,
        }),
        shallowEqual
    );

    const handlePolling = useCallback(() => {
        if (isLoading) return;
        dispatch(refreshCurrentProcedureMessages(selectedProcedure._links.self.href));
    }, [dispatch, selectedProcedure, isLoading]);

    useFetchPolling(handlePolling);

    const processMessages = useCallback((messages) => {
        const processedMessages = messages
            .map((mes, indx) => ({
                _id: indx,
                text: mes.text,
                createdAt: mes.sentAt,
                user: {
                    _id: mes.from,
                    name: mes.from,
                },
            }))
            .sort((mes1, mes2) => new Date(mes2.createdAt) - new Date(mes1.createdAt));

        setMessages(processedMessages);
    }, []);

    useEffect(() => {
        const messages = route.params?.messages ?? [];
        processMessages(messages);
    }, [processMessages, route]);

    useEffect(() => {
        processMessages(selectedProcedure.messages);
    }, [selectedProcedure.messages, processMessages]);

    const refreshMessages = () => {
        if (isLoading) return;
        setIsLoading(true);
        dispatch(refreshCurrentProcedureMessages(selectedProcedure._links.self.href))
            .then(() => setIsLoading(false))
            .catch(() => setIsLoading(false));
    };

    const renderActions = () => {
        return (
            <TouchableOpacity style={styles.actionButtonContainer} onPress={refreshMessages}>
                <Icon
                    name='ios-refresh-outline'
                    size={moderateScale(20)}
                    color={Colors.accent}
                    style={{ marginRight: moderateScale(6) }}
                />
            </TouchableOpacity>
        );
    };

    const renderFooter = () => {
        if (!isLoading) return null;

        return <ActivityIndicator size='small' color={Colors.primaryText} />;
    };

    const onSend = (messages) => {
        if (isLoading) return;
        setIsLoading(true);
        dispatch(addMessageToProcedure(selectedProcedure._links.self.href, { text: messages[0].text }))
            .then(() => setIsLoading(false))
            .catch(() => setIsLoading(false));
    };

    const canEdit = () => {
        return [posibleStatuses.REVIEWING].indexOf(get(selectedProcedure, 'status.name', '')) > -1;
    };

    const renderEmptyChat = () => {
        return (
            <View style={styles.emptyListContainer}>
                <Text style={[font_styles.secondary_text]}>
                    {strings.procedureMessages.there_arent_messages}
                </Text>
            </View>
        );
    };

    return (
        <SafeAreaView style={styles.safeArea}>
            <GiftedChat
                messages={messages}
                placeholder={strings.procedureMessages.placeholder}
                renderActions={renderActions}
                renderChatFooter={renderFooter}
                onSend={(messages) => onSend(messages)}
                textInputProps={{ multiline: false }}
                renderUsernameOnMessage={true}
                renderChatEmpty={renderEmptyChat}
                renderInputToolbar={
                    canEdit()
                        ? (props) => (
                              <KeyboardAwareScrollView
                                  style={styles.safeArea}
                                  contentContainerStyle={{ height: 44 }}>
                                  <InputToolbar {...props} />
                              </KeyboardAwareScrollView>
                          )
                        : () => null
                }
                user={{ _id: loggedUserCredentials.username }}
            />
        </SafeAreaView>
    );
};

ProcedureMessagesScreen.propTypes = {
    route: PropTypes.object,
};

const styles = StyleSheet.create({
    safeArea: {
        flex: 1,
    },
    actionButtonContainer: {
        alignItems: 'center',
        justifyContent: 'center',
        height: '100%',
        marginLeft: moderateScale(8),
    },
    emptyListContainer: {
        flex: 1,
        alignItems: 'center',
        justifyContent: 'center',
        transform: [{ rotateX: '-180deg' }],
    },
});

export default ProcedureMessagesScreen;
