import React, { useEffect, useRef,useCallback,useState} from 'react';
import { View, StyleSheet, ActivityIndicator, Platform } from 'react-native';
import { useSelector, shallowEqual, useDispatch } from 'react-redux';

import * as Colors from '../../constants/Colors';
import { moderateScale, verticalScale } from '../../lib/size-normalizer';
import { useNavigation } from '@react-navigation/native';
import { DropDownHolder } from '../DropDownHolder';
import strings from '../../constants/Strings';
import TextField from '../../components/TextField';
import Dropdown, { Option } from '../../components/dropdown';
import * as Yup from 'yup';
import { useFormik } from 'formik';
import PropTypes from 'prop-types';
import { getAppointmentEnabled, getDelegations, getDelegationSector } from '../../actions/appointmentAction';
import moment from 'moment';


const TurnoSchema = Yup.object().shape({
    delegation: Yup.number().required(strings.errors.required),
    sector_id: Yup.number().required(strings.errors.required),
    turno: Yup.number().required(strings.errors.required),

});

const AppointmentForm = ({
    disabled,
    editing,
    loading,
    onConfirm,
    delegation = '',
    sector_id = '',
    turno = '',
    fecha = '',
    hora = '',
    box_id = '',
    applicant_id ='',

}) => {
    const dispatch = useDispatch();
    const navigation = useNavigation();
    const delegationRef = useRef(null);
    const sector_idRef = useRef(null);
    const turnoRef = useRef(null);
   
    const [delegacion, setDelegacion] = useState('');

    const { delegations, loadingDelegation, sectors,sectorsLoading,appointments_enabled,turnosLoading,afiliado_id } = useSelector(
        (state) => ({
            delegations: state.appointment.delegations.items.data ?? [],
            loadingDelegation: state.appointment.delegations.loading,
            sectors: state.appointment.sectors.items.data ?? [],
            sectorsLoading: state.appointment.sectors.loading,
            appointments_enabled: state.appointment.appointments_enabled.items.data ?? [],
            turnosLoading: state.appointment.appointments_enabled.loading,
            afiliado_id: state.appointment.applicant.id ?? null,
        }), shallowEqual );
       
        const handleConfirm = (values) => {
            
            const data = {
                sector_id:  values.sector_id ,
                fecha: values.fecha,
                hora: values.hora,
                box_id: values.box_id,
                applicant_id: afiliado_id
            };

            if (onConfirm) onConfirm(data);
        };
        const { handleChange, handleBlur, handleSubmit, errors, touched, values, setFieldValue } = useFormik({
            validationSchema: TurnoSchema,
            initialValues: {
                sector_id:sector_id,
                turno: turno,
                fecha: fecha,
                hora: hora,
                box_id: box_id,
                applicant_id:afiliado_id,
            },
            onSubmit: handleConfirm,
        });
        
        useEffect(() => {
            navigation.setParams({ loadingUpdate: loading });
        }, [loading, navigation]);
        
        useEffect(() => {
            navigation.setParams({ onConfirm: handleSubmit, loading: false });
        }, [navigation, handleSubmit]);
        
        const todasDelegaciones = useCallback(
            (isRefresh = false) => {
                return dispatch(getDelegations(isRefresh));
            },
            [dispatch]
        );        
        useEffect(() => {
            todasDelegaciones();
        }, []);


        const handleDelegationChange = (text) => {
            setFieldValue('delegation', text);
            setDelegacion(text);
            dispatch(getDelegationSector(text));
        };
        
        const handleSectorChange = (text) => {
            setFieldValue('sector_id', text);
            dispatch(getAppointmentEnabled(delegacion,text));
        };
        
        const handleTurnoChange = (text) => {
            
            setFieldValue('turno', text);

            const datosTurno=appointments_enabled.filter((it) => (it.order_key===text));
            
            setFieldValue('fecha' , datosTurno[0].fecha);
            setFieldValue('hora'  , datosTurno[0].hora);
            setFieldValue('box_id', datosTurno[0].box);
        };        

        return (
            <View style={styles.container}>
                <View style={styles.inputContainer}>
                    <Dropdown
                        value={values.delegation}
                        leftIcon='ios-home'
                        onChangeText={handleDelegationChange}
                        ref={delegationRef}
                        onBlur={() => sector_idRef.current?.focus()}
                        disabled={disabled}
                        rightIcon={
                            loadingDelegation ? (
                                <ActivityIndicator size='small' color={Colors.primaryText} />
                            ) : null
                        }
                        placeholder={strings.openedAppointments.delegation}
                        error={errors.delegation}
                        touched={touched.delegation}>
                        {delegations.map(it =>(<Option key={it.id} id={it.id}>{`${it.nombre}`}</Option>))} 
                    </Dropdown>
                </View>
                <View style={styles.inputContainer}>
                    <Dropdown
                        value={values.sector_id}
                        leftIcon='ios-people-sharp'
                        onChangeText={handleSectorChange}
                        onBlur={() => turnoRef.current?.focus()}
                        ref={sector_idRef}
                        disabled={disabled}
                        rightIcon={
                            sectorsLoading ? (
                                <ActivityIndicator size='small' color={Colors.primaryText} />
                            ) : null
                        }
                        placeholder={strings.openedAppointments.sector}
                        error={errors.sector}
                        touched={touched.sector}>
                        {sectors.map(it =>(<Option key={it.id} id={it.id}>{`${it.nombre}`}</Option>))}
                    </Dropdown>
                </View>
                <View style={styles.inputContainer}>
                    <Dropdown
                        value={values.turno}
                        leftIcon='ios-calendar-sharp'
                        onChangeText={handleTurnoChange}
                        ref={turnoRef}
                        disabled={disabled}
                        rightIcon={
                            turnosLoading ? (
                                <ActivityIndicator size='small' color={Colors.primaryText} />
                            ) : null
                        }                        
                        placeholder={strings.openedAppointments.turno}
                        error={errors.turno}
                        touched={touched.turno}>
                        {appointments_enabled.map(it =>(
                            <Option key={it.fecha} id={it.order_key}>
                                {`${moment(it.fecha).format('DD-MM-YYYY')} ${it.hora}`}
                            </Option>))}
                    </Dropdown>
                </View>

                <View style={{flex:2}}>

                </View>

        </View>
    );
};

AppointmentForm.propTypes = {
    disabled: PropTypes.bool,
    editing: PropTypes.bool,
    loading: PropTypes.bool,
    delegation: PropTypes.oneOfType([PropTypes.number, PropTypes.string]),
    sector_id: PropTypes.oneOfType([PropTypes.number, PropTypes.string]),
    turno: PropTypes.oneOfType([PropTypes.number, PropTypes.string]),
    fecha: PropTypes.string,
    hora: PropTypes.string,
    box_id: PropTypes.string, 
    applicant_id: PropTypes.string, 
    onConfirm: PropTypes.func,
};

export default AppointmentForm;

const styles = StyleSheet.create({
    container: {
        flex: 1,
        flexDirection: 'column',
        justifyContent: 'space-between',
    },
    mainViewContainer: {
        flex: 2,
        flexDirection: 'column',
        justifyContent: 'space-between',
    },
    inputContainer: {
        marginBottom: verticalScale(16, 0.25),
    },
});
