import React from 'react';
import { StyleSheet } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import PreAuthorizationList from '../../components/authorization/PreAuthorizationList';

const PreAuthorizationScreen = () => {
    return (
        <SafeAreaView style={styles.safeArea}>
            <PreAuthorizationList />
        </SafeAreaView>
    );
};

const styles = StyleSheet.create({
    safeArea: {
        flex: 1,
    },
});

export default PreAuthorizationScreen;
