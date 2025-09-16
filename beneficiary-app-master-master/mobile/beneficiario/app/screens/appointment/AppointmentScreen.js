import React, { useEffect } from 'react';
import { View, StyleSheet } from 'react-native';
import { useDispatch } from 'react-redux';
import { SafeAreaView } from 'react-native-safe-area-context';
import { loginApiTurnos } from '../../actions/appointmentAction';
import AppointmentList from '../../components/appointment/AppointmentList';

const AppointmentScreen = () => {
    const dispatch = useDispatch();

    useEffect(() => {
        dispatch(loginApiTurnos());
    }, [dispatch]);

    let view = null;

    view = (
        <React.Fragment>
            <AppointmentList />
        </React.Fragment>
    );

    return (
        <SafeAreaView style={styles.container}>
            <View style={styles.safeArea}>{view}</View>
        </SafeAreaView>
    );
};

const styles = StyleSheet.create({
    container: {
        flex: 1,
    },
    safeArea: {
        flex: 1,
    },
});

export default AppointmentScreen;
