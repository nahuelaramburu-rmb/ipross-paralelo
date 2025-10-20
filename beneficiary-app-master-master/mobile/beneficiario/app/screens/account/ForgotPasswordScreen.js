import React, { useRef, useEffect, useState, useCallback } from 'react';
import { shallowEqual, useSelector, useDispatch } from 'react-redux';
import { StyleSheet, View } from 'react-native';
import Button from '../../components/Button';
import Icon from 'react-native-vector-icons/Ionicons';
import * as Colors from '../../constants/Colors';
import strings from '../../constants/Strings';
import { DropDownHolder } from '../../components/DropDownHolder';
import { moderateScale, verticalScale } from '../../lib/size-normalizer';
import { sendOTPEmail, confirmNewPassword } from '../../actions/profileAction';
import * as Yup from 'yup';
import { useFormik } from 'formik';
import TextField from '../../components/TextField';
import PropTypes from 'prop-types';
import AuthenticationWrapper from '../../components/AuthenticationWrapper';
import { regular } from '../../constants/Fonts';

const EmailSchema = Yup.object().shape({
    email: Yup.string().email(strings.errors.email_not_valid).required(strings.errors.required),
});

const OtpSchema = Yup.object().shape({
    otp: Yup.string().required(strings.errors.required),
    password: Yup.string().required(strings.errors.required),
    password2: Yup.string()
        .oneOf([Yup.ref('password'), null], strings.errors.passwords_must_match)
        .required(strings.errors.required),
});

const EmailForm = ({ onEmailSubmit }) => {
    const emailRef = useRef(null);

    const { sendingEmail } = useSelector(
        (state) => ({
            sendingEmail: state.profile.forgot_password.email_loading,
        }),
        shallowEqual
    );

    const { handleChange, handleBlur, handleSubmit, errors, touched, values } = useFormik({
        validationSchema: EmailSchema,
        initialValues: { email: '' },
        onSubmit: onEmailSubmit,
    });

    useEffect(() => {
        if (emailRef.current) emailRef.current.focus();
    }, []);

    return (
        <>
            <View style={styles.form}>
                <View style={styles.inputContainer}>
                    <TextField
                        touched={touched.email}
                        ref={emailRef}
                        leftIcon='ios-mail'
                        placeholder={strings.signUp.email}
                        value={values.email}
                        autoCorrect={false}
                        enablesReturnKeyAutomatically={true}
                        onChangeText={handleChange('email')}
                        returnKeyType='done'
                        onBlur={handleBlur('email')}
                        autoCapitalize='none'
                        keyboardType='email-address'
                        error={errors.email}
                    />
                </View>
            </View>
            <View style={styles.buttonContainer}>
                <Button
                    title={strings.common.confirm}
                    loading={sendingEmail}
                    block={true}
                    raised={true}
                    type='solid'
                    onPress={handleSubmit}
                />
            </View>
        </>
    );
};

const OtpForm = ({ onOtpSubmit }) => {
    const [isSecuryEntryPassword, setIsSecurityPassword] = useState(true);
    const [isSecuryEntryPassword2, setIsSecurityPassword2] = useState(true);

    const otpRef = useRef(null);
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

    const { confirmingPassword } = useSelector(
        (state) => ({
            confirmingPassword: state.profile.forgot_password.forgot_password_loading,
        }),
        shallowEqual
    );

    const { handleChange, handleBlur, handleSubmit, errors, touched, values } = useFormik({
        validationSchema: OtpSchema,
        initialValues: { otp: '', password: '', password2: '' },
        onSubmit: onOtpSubmit,
    });

    useEffect(() => {
        if (otpRef.current) otpRef.current.focus();
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
                <View style={styles.inputContainer}>
                    <TextField
                        touched={touched.otp}
                        ref={otpRef}
                        leftIcon='ios-lock-closed'
                        placeholder={strings.forgotPassword.otp_code}
                        value={values.otp}
                        autoCorrect={false}
                        onSubmitEditing={() => passwordRef.current?.focus()}
                        enablesReturnKeyAutomatically={true}
                        onChangeText={handleChange('otp')}
                        returnKeyType='next'
                        onBlur={handleBlur('otp')}
                        autoCapitalize='none'
                        keyboardType='numeric'
                        error={errors.otp}
                    />
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
                    loading={confirmingPassword}
                    block={true}
                    raised={true}
                    type='solid'
                    onPress={handleSubmit}
                />
            </View>
        </>
    );
};

const ForgotPasswordScreen = () => {
    const dispatch = useDispatch();
    const [emailSended, setEmailSended] = useState(false);

    const sendOtpEmail = useCallback(
        (values) => {
            dispatch(sendOTPEmail(values))
                .then(() => {
                    setEmailSended(true);
                    DropDownHolder.alert(
                        'success',
                        strings.forgotPassword.email_success_message_title,
                        strings.forgotPassword.email_success_message_info
                    );
                })
                .catch(() => null);
        },
        [dispatch]
    );

    const confirmPassword = useCallback(
        (values) => {
            const obj = {
                restore_otp: values.otp,
                new_password: values.password,
            };

            dispatch(confirmNewPassword(obj));
        },
        [dispatch]
    );

    let view = null;

    if (!emailSended) view = <EmailForm onEmailSubmit={sendOtpEmail} />;
    else view = <OtpForm onOtpSubmit={confirmPassword} />;

    return (
        <AuthenticationWrapper>
            <View style={styles.container}>{view}</View>
        </AuthenticationWrapper>
    );
};

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
    inputContainer: {
        marginBottom: verticalScale(16, 0.25),
    },
});

EmailForm.propTypes = {
    onEmailSubmit: PropTypes.func,
};

OtpForm.propTypes = {
    onOtpSubmit: PropTypes.func,
};

export default ForgotPasswordScreen;
