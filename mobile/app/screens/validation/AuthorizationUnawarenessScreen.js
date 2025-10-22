import React, { useCallback, useEffect } from 'react';
import { View, Text, StyleSheet, ActivityIndicator } from 'react-native';
import * as Colors from '../../constants/Colors';
import { useSelector, shallowEqual, useDispatch } from 'react-redux';
import { moderateScale, verticalScale } from '../../lib/size-normalizer';
import { font_styles } from '../../lib/default-styles';
import { KeyboardAwareScrollView } from 'react-native-keyboard-aware-scroll-view';
import TextField from '../../components/TextField';
import { regular } from '../../constants/Fonts';
import strings from '../../constants/Strings';
import {
    createProcedure,
    getAuthorizationRelatedProcedure,
    selectProcedure,
} from '../../actions/procedureAction';
import { useFetchLoading } from '../../hooks/utils';
import { DropDownHolder } from '../../components/DropDownHolder';
import Button from '../../components/Button';
import { SafeAreaView } from 'react-native-safe-area-context';
import * as Yup from 'yup';
import { useFormik } from 'formik';
import PropTypes from 'prop-types';
import { useFocusEffect } from '@react-navigation/native';

const UnawarenessSchema = Yup.object().shape({
    description: Yup.string().required(strings.errors.required),
});

const UNWARENESS_PROCEDURE_TYPE = 'UnknownAuthorizationProcedure';

const AuthorizationUnawarenessProcedureCreated = ({ navigation }) => {
    const dispatch = useDispatch();

    const { authorizationProcedure } = useSelector(
        (state) => ({
            authorizationProcedure: state.validation.selectedAuthorization.procedure,
        }),
        shallowEqual
    );

    const handleSeeProcedure = () => {
        dispatch(selectProcedure(authorizationProcedure));
        navigation.navigate('ProcedureDetail', {
            procedureType: UNWARENESS_PROCEDURE_TYPE,
            procedureId: authorizationProcedure.id,
        });
    };

    return (
        <View style={[styles.container, styles.loadingContainer]}>
            <View style={styles.buttonContainer}>
                <Text style={[font_styles.primary_text]}>
                    {strings.authorizationUnawareness.there_is_already_a_procedure}
                </Text>
                <Button
                    title={'Ver Trámite'}
                    block={true}
                    raised={true}
                    type='solid'
                    onPress={handleSeeProcedure}
                />
            </View>
        </View>
    );
};

const AuthorizationUnawarenessForm = ({ navigation }) => {
    const dispatch = useDispatch();

    const { authorization } = useSelector(
        (state) => ({
            authorization: state.validation.selectedAuthorization.item,
        }),
        shallowEqual
    );

    const handleAsyncOperation = useCallback((data) => dispatch(createProcedure(data)), [dispatch]);

    const [execFn, isLoading] = useFetchLoading(handleAsyncOperation);

    const handleConfirm = (values) => {
        const descriptionText = `
            N° de Validación: ${authorization.id}
            Prestador: ${authorization.practitioner.name} ${authorization.practitioner.lastName}
            Consultorio: ${authorization.medicalCenter.name}
        
            Descripción: ${values.description}
        `.replace(/ {2,}/g, '');

        const obj = {
            medicalAuthorization: {
                id: authorization.id,
            },
            description: descriptionText,
            beneficiary: {
                id: authorization.beneficiary.id,
            },
            procedureType: UNWARENESS_PROCEDURE_TYPE,
        };

        execFn(obj)
            .then((procedureLink) => {
                const linkSplitted = procedureLink.split('/');
                const newProcedureId = linkSplitted[linkSplitted.length - 1];
                DropDownHolder.alert(
                    'success',
                    'Éxito',
                    strings.authorizationUnawareness.procedure_creation_successul_message
                );
                navigation.navigate('ProcedureDetail', {
                    procedureLink: procedureLink,
                    procedureId: newProcedureId,
                });
            })
            .catch((err) => console.log(err));
    };

    const { handleChange, handleBlur, handleSubmit, errors, touched, values } = useFormik({
        validationSchema: UnawarenessSchema,
        initialValues: { description: '' },
        onSubmit: handleConfirm,
    });

    useEffect(() => {
        navigation.setParams({
            onConfirm: handleSubmit,
            isLoading: isLoading,
        });
    }, [handleSubmit, navigation, isLoading]);

    return (
        <View style={styles.container}>
            <KeyboardAwareScrollView
                scrollEnabled={false}
                showsVerticalScrollIndicator={false}
                contentContainerStyle={styles.keyboardScrollView}>
                <View style={styles.textContainer}>
                    <Text style={[font_styles.primary_text, styles.infoText]}>
                        {strings.authorizationUnawareness.unawareness_description}
                    </Text>
                </View>
                <View style={styles.cardContainer}>
                    <View style={styles.authorizationInfoContainer}>
                        <Text style={[font_styles.title_3_bold, styles.cardTitle]}>
                            {strings.authorizationUnawareness.authorization_to_cancel}
                        </Text>
                        <View style={[styles.divider, { marginBottom: verticalScale(12) }]} />
                        <Text
                            numberOfLines={2}
                            ellipsizeMode='tail'
                            style={{ marginVertical: moderateScale(8) }}>
                            <Text style={font_styles.primary_text}>
                                {strings.authorizationUnawareness.nro_authorization}:{' '}
                            </Text>
                            <Text style={[font_styles.primary_text_bold]}>{authorization.id}</Text>
                        </Text>
                        <Text
                            numberOfLines={2}
                            ellipsizeMode='tail'
                            style={{ marginVertical: moderateScale(8) }}>
                            <Text style={font_styles.primary_text}>
                                {strings.authorizationUnawareness.practitioner}:{' '}
                            </Text>
                            <Text style={[font_styles.primary_text_bold]}>
                                {authorization.practitioner.name + ' ' + authorization.practitioner.lastName}
                            </Text>
                        </Text>
                        <Text
                            numberOfLines={2}
                            ellipsizeMode='tail'
                            style={{ marginVertical: moderateScale(8) }}>
                            <Text style={font_styles.primary_text}>
                                {strings.authorizationUnawareness.medical_center}:{' '}
                            </Text>
                            <Text style={[font_styles.primary_text_bold]}>
                                {authorization.medicalCenter.name}
                            </Text>
                        </Text>
                    </View>
                </View>
                <TextField
                    maxLength={160}
                    multiline={true}
                    touched={touched.description}
                    leftIcon='ios-chatbubble'
                    placeholder={strings.authorizationUnawareness.description}
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
            </KeyboardAwareScrollView>
        </View>
    );
};

const AuthorizationUnawarenessScreen = ({ navigation }) => {
    const dispatch = useDispatch();

    const handleAsyncOperation = useCallback(
        (authorizationId) => dispatch(getAuthorizationRelatedProcedure(authorizationId)),
        [dispatch]
    );

    const [execFn, isLoading] = useFetchLoading(handleAsyncOperation);

    const { authorization, authorizationProcedure } = useSelector(
        (state) => ({
            authorization: state.validation.selectedAuthorization.item,
            authorizationProcedure: state.validation.selectedAuthorization.procedure,
        }),
        shallowEqual
    );

    const fetchProcedure = useCallback(() => {
        execFn(authorization.id);
    }, [execFn, authorization]);

    useFocusEffect(fetchProcedure);

    if (authorizationProcedure) {
        return (
            <SafeAreaView style={styles.safeArea}>
                <AuthorizationUnawarenessProcedureCreated navigation={navigation} />
            </SafeAreaView>
        );
    }

    if (authorizationProcedure === null && !isLoading) {
        return (
            <SafeAreaView style={styles.safeArea}>
                <AuthorizationUnawarenessForm navigation={navigation} />
            </SafeAreaView>
        );
    }

    return (
        <SafeAreaView style={styles.safeArea}>
            <View style={styles.loadingContainer}>
                <ActivityIndicator size='large' color={Colors.primaryText} />
            </View>
        </SafeAreaView>
    );
};

AuthorizationUnawarenessProcedureCreated.propTypes = {
    navigation: PropTypes.object,
};

AuthorizationUnawarenessForm.propTypes = {
    navigation: PropTypes.object,
};

AuthorizationUnawarenessScreen.propTypes = {
    navigation: PropTypes.object,
};

const styles = StyleSheet.create({
    safeArea: {
        flex: 1,
    },
    container: {
        flex: 1,
        backgroundColor: Colors.appBackground,
        paddingHorizontal: moderateScale(16),
        justifyContent: 'flex-start',
    },
    authorizationInfoContainer: {
        width: '100%',
        padding: moderateScale(12),
        borderRadius: moderateScale(6),
        backgroundColor: Colors.white,
        paddingVertical: verticalScale(12),
        elevation: 1,
        shadowColor: '#000',
        shadowOffset: {
            width: 0,
            height: 1,
        },
        shadowOpacity: 0.18,
        shadowRadius: 1.0,
    },
    cardTitle: {
        marginBottom: verticalScale(12),
    },
    divider: {
        borderBottomColor: Colors.lightDividerLine,
        borderBottomWidth: 0.5,
        width: '100%',
    },
    textInputLabel: {
        fontFamily: regular,
        paddingTop: '1%',
    },
    textContainer: {
        paddingVertical: verticalScale(28),
    },
    cardContainer: {
        paddingBottom: verticalScale(28),
    },
    keyboardScrollView: {
        flexGrow: 1,
    },
    infoText: {
        textAlign: 'center',
    },
    loadingContainer: {
        flex: 1,
        alignItems: 'center',
        justifyContent: 'center',
    },
    buttonContainer: {
        flex: 0.2,
        flexDirection: 'column',
        justifyContent: 'space-around',
        alignItems: 'center',
        width: '100%',
    },
});

export default AuthorizationUnawarenessScreen;
