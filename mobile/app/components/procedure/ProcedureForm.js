import React, { useEffect, useRef } from 'react';
import { View, StyleSheet, ActivityIndicator, Platform } from 'react-native';
import { useSelector, shallowEqual, useDispatch } from 'react-redux';

import * as Colors from '../../constants/Colors';
import { ProcedureImageContainer, ProcedureImage } from './ProcedureImage';
import { moderateScale, verticalScale } from '../../lib/size-normalizer';
import { useNavigation } from '@react-navigation/native';
import * as ImagePicker from 'expo-image-picker';
import { DropDownHolder } from '../DropDownHolder';
import strings from '../../constants/Strings';
import { getCertificateTypes } from '../../actions/procedureAction';
import { findRelatives } from '../../actions/profileAction';
import TextField from '../../components/TextField';
import Dropdown, { Option } from '../../components/dropdown';
import * as Yup from 'yup';
import { useFormik } from 'formik';
import Button from '../Button';
import PropTypes from 'prop-types';

const getFileExtension = (mimeTypeOrUri) => {
    // Si es un tipo MIME como 'image/jpeg', extraer la extensión
    if (mimeTypeOrUri && mimeTypeOrUri.includes('/')) {
        return mimeTypeOrUri.split('/').pop();
    }
    // Si es una URI, extraer la extensión del nombre del archivo
    if (mimeTypeOrUri && mimeTypeOrUri.includes('.')) {
        return mimeTypeOrUri.split('.').pop();
    }
    // Por defecto, asumir jpg
    return 'jpg';
};

const ProcedureSchema = Yup.object().shape({
    beneficiary: Yup.number().required(strings.errors.required),
    procedureType: Yup.string().required(strings.errors.required),
    description: Yup.string().required(strings.errors.required),
    certificateType: Yup.string().when('procedureType', {
        is: (type) => type === 'CertificateProcedure',
        then: Yup.string().required(strings.errors.required).typeError(strings.errors.required),
    }),
    images: Yup.array().min(1, strings.errors.required),
});

const ProcedureForm = ({
    disabled,
    editing,
    loading,
    onConfirm,
    beneficiary = '',
    procedureType = '',
    description = '',
    certificateType = '',
    files = [],
}) => {
    const dispatch = useDispatch();
    const navigation = useNavigation();
    const beneficiaryRef = useRef(null);
    const procedureTypeRef = useRef(null);
    const certificateTypeRef = useRef(null);
    const descriptionRef = useRef(null);

    const { relatives, loadingRelatives, ownerUser, certificateTypes, certificateTypesLoading } = useSelector(
        (state) => ({
            relatives: state.profile.relatives.items,
            loadingRelatives: state.profile.relatives.loading,
            ownerUser: state.profile.userData,
            certificateTypes: state.procedure.certificateTypes.items,
            certificateTypesLoading: state.procedure.certificateTypes.loading,
        }),
        shallowEqual
    );

    const handleConfirm = (values) => {
        const data = {
            procedureType: values.procedureType,
            description: values.description,
            beneficiary: { id: values.beneficiary },
        };

        if (values.certificateType) data['certificateType'] = { id: values.certificateType };

        if (onConfirm) onConfirm(data, values.images);
    };

    const { handleChange, handleBlur, handleSubmit, errors, touched, values, setFieldValue } = useFormik({
        validationSchema: ProcedureSchema,
        initialValues: {
            beneficiary: beneficiary,
            procedureType: procedureType,
            description: description,
            certificateType: certificateType,
            images: files,
        },
        onSubmit: handleConfirm,
    });

    useEffect(() => {
        navigation.setParams({ loadingUpdate: loading });
    }, [loading, navigation]);

    useEffect(() => {
        navigation.setParams({ onConfirm: handleSubmit, loading: false });
    }, [navigation, handleSubmit]);

    useEffect(() => {
        setFieldValue('certificateType', certificateType);
    }, [values.procedureType, certificateType, setFieldValue]);

    useEffect(() => {
        dispatch(findRelatives());
    }, [dispatch]);

    useEffect(() => {
        if (!editing) {
            // SI CANCELA LA EDICION RESTAURO EL STADO PARA REFLEJAR QUE NADA CAMBIÓ (ES DECIR QUE NO FUE CONTRA LA API)
            setFieldValue('images', files);
        }
    }, [editing, setFieldValue, files]);

    useEffect(() => {
        if (certificateTypes.length === 0 && values.procedureType === 'CertificateProcedure') {
            dispatch(getCertificateTypes());
        }
    }, [values, dispatch, certificateTypes]);

    const handleBeneficiaryChange = (text) => {
        setFieldValue('beneficiary', text);
    };

    const handleProcedureTypeChange = (text) => {
        setFieldValue('procedureType', text);
    };

    const handleCertificateTypeChange = (text) => {
        setFieldValue('certificateType', text);
    };

    const pickAnImage = async () => {
        try {
            // Solicitar permisos para la galería
            const permissionResult = await ImagePicker.requestMediaLibraryPermissionsAsync();
            
            if (permissionResult.granted === false) {
                DropDownHolder.alert('error', 'Error', 'Se requiere permiso para acceder a la galería');
                return;
            }

            // Lanzar el selector de imágenes
            const result = await ImagePicker.launchImageLibraryAsync({
                mediaTypes: ImagePicker.MediaTypeOptions.Images,
                quality: 0.8,
                allowsEditing: false,
            });

            if (result.canceled) {
                console.log('User cancelled image picker');
                return;
            }

            const response = result.assets[0];
            let source;
            if (Platform.OS === 'android') {
                source = { uri: response.uri, isStatic: true };
            } else {
                source = { uri: response.uri.replace('file://', ''), isStatic: true };
            }

            const ext = getFileExtension(response.mimeType || response.uri);
            const filename = `Documento-${new Date().getTime()}.${ext}`;

            setFieldValue('images', [
                ...values.images,
                { id: new Date().getTime(), uri: source.uri, type: response.mimeType || 'image/jpeg', name: filename },
            ]);
        } catch (error) {
            console.log('Error en pickAnImage:', error);
            DropDownHolder.alert('error', 'Error', 'No se pudo seleccionar la imagen');
        }
    };

    const onRemoveImage = (name) => {
        const imagesCopy = [...values.images];
        const indx = imagesCopy.findIndex((img) => img.name === name);
        if (indx === -1) return;

        imagesCopy.splice(indx, 1);
        setFieldValue('images', imagesCopy);
    };

    return (
        <View style={styles.container}>
            <View style={styles.inputContainer}>
                <Dropdown
                    value={values.beneficiary}
                    leftIcon='ios-people'
                    onChangeText={handleBeneficiaryChange}
                    ref={beneficiaryRef}
                    onBlur={() => procedureTypeRef.current?.focus()}
                    disabled={disabled}
                    rightIcon={
                        loadingRelatives ? (
                            <ActivityIndicator size='small' color={Colors.primaryText} />
                        ) : null
                    }
                    placeholder={strings.procedureForm.beneficiary}
                    error={errors.beneficiary}
                    touched={touched.beneficiary}>
                    {[ownerUser, ...relatives].map((it) => (
                        <Option key={it.id} id={it.id}>{`${it.name} ${it.lastName}`}</Option>
                    ))}
                </Dropdown>
            </View>
            <View style={styles.inputContainer}>
                <Dropdown
                    value={values.procedureType}
                    leftIcon='document-text'
                    onChangeText={handleProcedureTypeChange}
                    onBlur={() => {
                        if (!certificateTypeRef.current) descriptionRef.current?.focus();
                        else certificateTypeRef.current?.focus();
                    }}
                    ref={procedureTypeRef}
                    disabled={disabled}
                    placeholder={strings.procedureForm.procedure_type}
                    error={errors.procedureType}
                    touched={touched.procedureType}>
                    <Option id='CUDProcedure'>{'CUD'}</Option>
                    <Option id='DisabilityProcedure'>{'Discapacidad'}</Option>
                    <Option id='CertificateProcedure'>{'Certificados'}</Option>
                    {disabled && (
                        <Option id='UnknownAuthorizationProcedure'>{'Desconocimiento de Atención'}</Option>
                    )}
                </Dropdown>
            </View>
            {values.procedureType === 'CertificateProcedure' && (
                <View style={styles.inputContainer}>
                    <Dropdown
                        value={values.certificateType}
                        leftIcon='document-attach'
                        rightIcon={
                            certificateTypesLoading ? (
                                <ActivityIndicator size='small' color={Colors.primaryText} />
                            ) : null
                        }
                        onChangeText={handleCertificateTypeChange}
                        onBlur={() => descriptionRef.current?.focus()}
                        ref={certificateTypeRef}
                        disabled={disabled}
                        placeholder={strings.procedureForm.certificate_type}
                        error={errors.certificateType}
                        touched={touched.certificateType}>
                        {certificateTypes.map((it) => (
                            <Option key={it.id} id={it.id}>
                                {it.name}
                            </Option>
                        ))}
                    </Dropdown>
                </View>
            )}
            <View style={styles.inputContainer}>
                <TextField
                    maxLength={240}
                    multiline={true}
                    disabled={disabled}
                    touched={touched.description}
                    ref={descriptionRef}
                    leftIcon='ios-chatbubble'
                    placeholder={strings.procedureForm.description}
                    value={values.description}
                    autoCorrect={false}
                    blurOnSubmit={true}
                    enablesReturnKeyAutomatically={true}
                    onChangeText={handleChange('description')}
                    returnKeyType='done'
                    onBlur={handleBlur('description')}
                    autoCapitalize={'none'}
                    keyboardType={'default'}
                    error={errors.description}
                />
            </View>
            <ProcedureImageContainer>
                {values.images.map((img) => (
                    <ProcedureImage
                        image={img}
                        key={img.id || img.name}
                        style={{ marginVertical: moderateScale(8) }}
                        editing={editing}
                        onRemove={onRemoveImage}
                    />
                ))}
            </ProcedureImageContainer>
            {editing && (
                <Button
                    title={strings.procedureForm.add_image}
                    block={true}
                    raised={true}
                    type='solid'
                    onPress={pickAnImage}
                />
            )}
        </View>
    );
};

ProcedureForm.propTypes = {
    disabled: PropTypes.bool,
    editing: PropTypes.bool,
    loading: PropTypes.bool,
    onConfirm: PropTypes.func,
    beneficiary: PropTypes.oneOfType([PropTypes.number, PropTypes.string]),
    procedureType: PropTypes.string,
    description: PropTypes.string,
    certificateType: PropTypes.oneOfType([PropTypes.number, PropTypes.string]),
    files: PropTypes.array,
};

export default ProcedureForm;

const styles = StyleSheet.create({
    container: {
        flex: 1,
        flexDirection: 'column',
        justifyContent: 'space-between',
    },
    mainViewContainer: {
        flex: 1,
        flexDirection: 'column',
        justifyContent: 'space-between',
    },
    inputContainer: {
        marginBottom: verticalScale(16, 0.25),
    },
});
