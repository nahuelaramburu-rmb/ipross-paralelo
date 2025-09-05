import React, { memo,  useEffect, useRef } from 'react';
import { View, StyleSheet, ActivityIndicator } from 'react-native';
import { useSelector, shallowEqual, useDispatch } from 'react-redux';
import { verticalScale } from '../../lib/size-normalizer';
import { useNavigation } from '@react-navigation/native';
import strings from '../../constants/Strings';
import * as Yup from 'yup';
import { useFormik } from 'formik';
import Button from '../Button';
import PropTypes from 'prop-types';
import { getMedicalSpecialties, getProfessionalSpecialty, getTowns, getTypesSpecialty } from '../../actions/professionalAction';
import { Colors } from 'react-native/Libraries/NewAppScreen';
import DropdownOther, { Option } from '../../components/dropdownother';


const ProfessionalSchema = Yup.object().shape({
    tipoEspecialidad: Yup.number().required(strings.errors.required),
    especialidad: Yup.number().required(strings.errors.required),
    localidad: Yup.number().required(strings.errors.required),
});

const ProfessionalForm = ({
    disabled,
    tipoEspecialidad = null,
    especialidad = null,
    localidad = null,
    nuevoTowns=[],
}) => {
    const dispatch = useDispatch();
    
    const navigation = useNavigation();

    const tipoEspecialidadRef = useRef(null);
    const especialidadRef = useRef(null);
    const localidadRef = useRef(null);

    const { typesSpecialty, specialties,specialtiesLoading,typesSpecialtyLoading ,towns,townsLoading} = useSelector(
        (state) => ({
            typesSpecialty: state.professional.typesSpecialty.items ?? [],
            typesSpecialtyLoading: state.professional.typesSpecialty.loading,
            specialties: state.professional.specialties.items ?? [],
            specialtiesLoading: state.professional.specialties.loading,
            towns: state.professional.towns.items ?? [],
            townsLoading: state.professional.towns.loading,

        }),
        shallowEqual
    );

    const { handleChange, handleBlur, handleSubmit, errors, touched, values, setFieldValue } = useFormik({
        validationSchema: ProfessionalSchema,
        initialValues: {
            tipoEspecialidad: tipoEspecialidad,
            especialidad: especialidad,
            localidad:localidad
        }
    });


    useEffect(() => {
        dispatch(getTypesSpecialty());
    }, [dispatch]);


    const handleSearch = () => {

        const data = {
            tipoEspecialidad:  values.tipoEspecialidad ,
            especialidad: values.especialidad,
            localidad: values.localidad,
        };

        dispatch(getProfessionalSpecialty(data.especialidad,data.localidad));

        navigation.navigate('ProfessionalList',{
            idSpecialty: data.especialidad,
            idTown:data.localidad            
        });
 
    }    

    const handletipoEspecialidadChange = (text) => {
        setFieldValue('tipoEspecialidad', text);
        dispatch(getMedicalSpecialties(text));
    };

    const handleEspecialidadChange = (text) => {
        setFieldValue('especialidad', text);
        
        dispatch(getTowns());

    };
    
    const handleLocalidadChange = (text) => {
        setFieldValue('localidad', text);
    };

    return (
        <View style={styles.container}>
                <View style={styles.inputContainer}>
                    <DropdownOther
                        value={values.tipoEspecialidad}
                        leftIcon='ios-medical'
                        onChangeText={handletipoEspecialidadChange }
                        ref={tipoEspecialidadRef}
                        onBlur={() => especialidadRef.current?.focus()}
                        disabled={disabled}
                        rightIcon={
                            typesSpecialtyLoading ? (
                                <ActivityIndicator size='small' color={Colors.primaryText} />
                            ) : null
                        }
                        placeholder={strings.professionalForm.specialty_type}
                        error={errors.tipoEspecialidad}
                        touched={touched.tipoEspecialidad}>
                       {!!typesSpecialty && typesSpecialty.map(it =>(<Option key={it.id} id={it.id}>{`${it.name}`}</Option>))} 
                    </DropdownOther>
                </View>        
            {/* Especialidad */}
            <View style={styles.inputContainer}>
                <DropdownOther
                    value={values.especialidad}
                    leftIcon='ios-medkit-outline'
                    rightIcon={
                        specialtiesLoading ? (
                                <ActivityIndicator size='small' color={Colors.primaryText} />
                            ) : null
                        }                    
                    onChangeText={handleEspecialidadChange}
                    onBlur={() => {localidadRef.current?.focus(); }}
                    ref={especialidadRef}
                    disabled={disabled}
                    placeholder={strings.professionalForm.specialty}
                    error={errors.especialidad}
                    touched={touched.especialidad}>
                    {specialties.map(it =>(<Option key={it.id} id={it.id}>{`${it.name}`}</Option>))}
                </DropdownOther>
            </View>
            
            {/* Localidades */}
            <View style={styles.inputContainer}>
                <DropdownOther
                    value={values.localidades}
                    leftIcon='ios-home-outline'
                    rightIcon={
                        townsLoading ? (
                                <ActivityIndicator size='small' color={Colors.primaryText} />
                            ) : null
                        }                    
                    onChangeText={handleLocalidadChange}
                    ref={localidadRef}
                    disabled={disabled}
                    placeholder={strings.professionalForm.city}
                    error={errors.localidad}
                    touched={touched.localidad}>
                    {towns.map(it =>(<Option key={it.id} id={it.id}>{`${it.name}`}</Option>))}
                </DropdownOther>
            </View>
           
            <View style={styles.containerButton}>
                <Button
                    title={strings.professionalForm.search}
                    block={true}
                    raised={true}
                    type='solid'
                    onPress={handleSearch}
                />
            </View>
        </View>
    );
};

ProfessionalForm.propTypes = {
    loading: PropTypes.bool,
    tipoEspecialidad: PropTypes.number,
    especialidad: PropTypes.number,
    localidad: PropTypes.number,

};

const styles = StyleSheet.create({
    container: {
        flex: 1,
        alignContent:'center',
        flexDirection: 'column',
        
    },
    mainViewContainer: {
        flex: 1,
        flexDirection: 'column',
        justifyContent: 'space-between',
    },
    inputContainer: {
        marginBottom: verticalScale(16, 0.25),
    },
    containerButton: {
        borderBottomColor: Colors.lightDividerLine,
        borderBottomWidth: 0.1,
        width: '100%',
        marginTop: verticalScale(20),
    },    
});

export default memo(ProfessionalForm);