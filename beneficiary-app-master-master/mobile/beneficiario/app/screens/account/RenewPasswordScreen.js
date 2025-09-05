import React, { useCallback, useEffect, useRef, useState } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { shallowEqual, useDispatch, useSelector } from 'react-redux';
import Icon from 'react-native-vector-icons/Ionicons';
import * as Colors from '../../constants/Colors';
import strings from '../../constants/Strings';
import { DropDownHolder } from '../../components/DropDownHolder';
import { moderateScale, verticalScale } from '../../lib/size-normalizer';
import AuthenticationWrapper from '../../components/AuthenticationWrapper';
import { reset } from '../../lib/NavigationService';
import { updatePassword } from '../../actions/profileAction';
import * as Yup from 'yup';
import { useFormik } from 'formik';
import Button from '../../components/Button';
import TextField from '../../components/TextField';
import PropTypes from 'prop-types';
import { font_styles } from '../../lib/default-styles';
import { regular } from '../../constants/Fonts';

const RenewPasswordSchema = Yup.object().shape({
    password: Yup.string().required(strings.errors.required),
    password2: Yup.string()
        .oneOf([Yup.ref('password'), null], strings.errors.passwords_must_match)
        .required(strings.errors.required),
});

const RenewPasswordForm = ({ onSubmit }) => {
    const [isSecuryEntryPassword, setIsSecurityPassword] = useState(true);
    const [isSecuryEntryPassword2, setIsSecurityPassword2] = useState(true);

    const passwordRef = useRef(null);
    const password2Ref = useRef(null);

    useEffect(() => {
        passwordRef?.current?.setNativeProps({
            style: { fontFamily: regular },
        });
        password2Ref?.current?.setNativeProps({
            style: { fontFamily: regular },
        });
    }, [isSecuryEntryPassword, isSecuryEntryPassword2]);

    const { handleChange, handleBlur, handleSubmit, errors, touched, values } = useFormik({
        validationSchema: RenewPasswordSchema,
        initialValues: { password: '', password2: '' },
        onSubmit: onSubmit,
    });

    const { changingPassword } = useSelector(
        (state) => ({
            changingPassword: state.profile.password_renew.loading,
        }),
        shallowEqual
    );

    useEffect(() => {
        passwordRef.current?.focus();
    }, []);

    const onAccessoryPress = (input) => {
        if (input === '1') setIsSecurityPassword((value) => !value);
        else setIsSecurityPassword2((value) => !value);
    };

    const renderAccessory = (input) => {
        let secureTextEntry = input === '1' ? isSecuryEntryPassword : isSecuryEntryPassword2;

        let name = secureTextEntry ? 'ios-eye' : 'ios-eye-off';

        return (
            <Icon
                size={moderateScale(20)}
                name={name}
                color={Colors.logoTextInactive}
                onPress={() => onAccessoryPress(input)}
                suppressHighlighting
            />
        );
    };

    return (
        <>
            <View style={styles.form}>
                <View style={styles.textContainer}>
                    <Text style={[font_styles.secondary_text_bold, styles.text]}>
                        {strings.renewPassword.change_password}
                    </Text>
                </View>
                <View style={styles.inputContainer}>
                    <TextField
                        touched={touched.password}
                        ref={passwordRef}
                        rightIcon={renderAccessory('1')}
                        leftIcon='ios-lock-closed'
                        placeholder={strings.renewPassword.new_password}
                        onSubmitEditing={() => password2Ref.current?.focus()}
                        value={values.password}
                        autoCorrect={false}
                        enablesReturnKeyAutomatically={true}
                        onChangeText={handleChange('password')}
                        returnKeyType='next'
                        onBlur={handleBlur('password')}
                        autoCapitalize='none'
                        keyboardType='default'
                        error={errors.password}
                        secureTextEntry={isSecuryEntryPassword}
                    />
                </View>
                <View style={styles.inputContainer}>
                    <TextField
                        touched={touched.password2}
                        ref={password2Ref}
                        rightIcon={renderAccessory('2')}
                        leftIcon='ios-lock-closed'
                        placeholder={strings.renewPassword.new_password}
                        value={values.password2}
                        autoCorrect={false}
                        enablesReturnKeyAutomatically={true}
                        onChangeText={handleChange('password2')}
                        onSubmitEditing={() => password2Ref.current?.blur()}
                        returnKeyType='done'
                        onBlur={handleBlur('password2')}
                        autoCapitalize='none'
                        keyboardType='default'
                        error={errors.password2}
                        secureTextEntry={isSecuryEntryPassword2}
                    />
                </View>
            </View>
            <View style={styles.buttonContainer}>
                <Button
                    title={strings.common.confirm}
                    loading={changingPassword}
                    block={true}
                    raised={true}
                    type='solid'
                    onPress={handleSubmit}
                />
            </View>
        </>
    );
};

const RenewPasswordScreen = () => {
    const dispatch = useDispatch();

    const onFormSubmit = useCallback(
        (values) => {
            dispatch(updatePassword({ new_password: values.password }))
                .then(() => {
                    DropDownHolder.alert(
                        'success',
                        strings.general.info,
                        strings.renewPassword.update_password_success,
                        2000
                    );

                    reset({
                        index: 0,
                        routes: [{ name: 'Login' }],
                    });
                })
                .catch(() => null);
        },
        [dispatch]
    );

    return (
        <AuthenticationWrapper>
            <View style={styles.container}>
                <RenewPasswordForm onSubmit={onFormSubmit} />
            </View>
        </AuthenticationWrapper>
    );
};

RenewPasswordForm.propTypes = {
    onSubmit: PropTypes.func,
};

export default RenewPasswordScreen;

const styles = StyleSheet.create({
    container: {
        flex: 1,
        alignItems: 'center',
        flexDirection: 'column',
        justifyContent: 'space-around',
        paddingBottom: verticalScale(12),
    },
    form: {
        flex: 0.6,
        width: '100%',
        flexDirection: 'column',
        justifyContent: 'center',
    },
    buttonContainer: {
        width: '100%',
        alignItems: 'center',
        justifyContent: 'space-around',
        flexDirection: 'column',
    },
    textContainer: {
        position: 'absolute',
        alignContent: 'center',
        alignItems: 'center',
        top: 0,
    },
    text: {
        textAlign: 'center',
        paddingHorizontal: moderateScale(32),
    },
    inputContainer: {
        marginBottom: verticalScale(16, 0.25),
    },
});
