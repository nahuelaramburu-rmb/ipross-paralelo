import React, { memo, useEffect } from 'react';
import { View, StyleSheet } from 'react-native';
import { useDispatch, useSelector } from 'react-redux';
import { SafeAreaView } from 'react-native-safe-area-context';
import { loginApiTurnos } from '../../actions/appointmentAction';
import AppointmentList from '../../components/appointment/AppointmentList';
import { useAnimatableHeader } from '../../hooks/utils';

const AppointmentScreen = () => {
    const dispatch = useDispatch();    

    const { interpolatedHeight, onScroll } = useAnimatableHeader();

    useEffect(() => {
        dispatch(loginApiTurnos());
    }, []);    
    
    let view = null;
    
    view = (
        <React.Fragment>
            
            <AppointmentList onScroll={onScroll}/>

        </React.Fragment>
    );

    return (
        <SafeAreaView style={{ flex: 1}}>
            <View style={styles.safeArea}>{view}</View>
        </SafeAreaView>
    );
};

const styles = StyleSheet.create({
    safeArea: {
        flex: 1,
    },
});

export default AppointmentScreen;
