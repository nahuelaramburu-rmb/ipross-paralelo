import React, { useEffect, useRef, useState } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { useDispatch, useSelector, shallowEqual } from 'react-redux';
import Icon from 'react-native-vector-icons/Ionicons';
import * as Colors from '../../constants/Colors';
import strings from '../../constants/Strings';
import { moderateScale, verticalScale } from '../../lib/size-normalizer';
import { font_styles } from '../../lib/default-styles';
import AuthenticationWrapper from '../../components/AuthenticationWrapper';
import { TouchableOpacity } from 'react-native-gesture-handler';
import { login } from '../../actions/profileAction';
import Button from '../../components/Button';
import TextField from '../../components/TextField';
import * as Yup from 'yup';
import { useFormik } from 'formik';
import PropTypes from 'prop-types';
import { lightItalic, regular } from '../../constants/Fonts';

const LoginSchema = Yup.object().shape({
    username: Yup.string().required(strings.errors.required),
    password: Yup.string().required(strings.errors.required),
});

const LoginScreen = ({ navigation }) => {
    const dispatch = useDispatch();
    const passwordRef = useRef(null);
    const [isSecureEntry, setIsSecurityEntry] = useState(true);

    const { loading } = useSelector(
        (state) => ({
            loading: state.profile.login.loading,
        }),
        shallowEqual
    );

    useEffect(() => {
        passwordRef?.current?.setNativeProps({
            style: { fontFamily: regular },
        });
    }, [isSecureEntry]);

    const { handleChange, handleBlur, handleSubmit, errors, touched, values } = useFormik({
        validationSchema: LoginSchema,
        initialValues: { username: '', password: '' },
        onSubmit: () => dispatch(login(values)),
    });

    const onAccessoryPress = () => {
        setIsSecurityEntry(!isSecureEntry);
    };

    const renderAccessory = () => {
        let name = isSecureEntry ? 'ios-eye' : 'ios-eye-off';

        return (
            <Icon
                size={moderateScale(20)}
                name={name}
                color={Colors.logoTextInactive}
                onPress={onAccessoryPress}
                suppressHighlighting
            />
        );
    };

    const forgotPassword = () => {
        navigation.navigate('ForgotPassword');
    };

    const register = () => {
        navigation.navigate('SignUpStackScreens');
    };

    return (
        <AuthenticationWrapper>
            <View style={styles.container}>
                <View style={styles.loginForm}>
                    <View style={styles.inputContainer}>
                        <TextField
                            touched={touched.username}
                            value={values.username}
                            leftIcon='ios-person'
                            placeholder={strings.login.user}
                            autoCorrect={false}
                            enablesReturnKeyAutomatically={true}
                            onChangeText={handleChange('username')}
                            onSubmitEditing={() => passwordRef.current?.focus()}
                            returnKeyType='next'
                            onBlur={handleBlur('username')}
                            autoCapitalize={'none'}
                            keyboardType={'numeric'}
                            error={errors.username}
                        />
                    </View>
                    <View style={styles.inputContainer}>
                        <TextField
                            touched={touched.password}
                            ref={passwordRef}
                            leftIcon='ios-lock-closed'
                            rightIcon={renderAccessory()}
                            placeholder={strings.login.password}
                            value={values.password}
                            autoCorrect={false}
                            enablesReturnKeyAutomatically={true}
                            onChangeText={handleChange('password')}
                            returnKeyType='done'
                            onBlur={handleBlur('password')}
                            autoCapitalize={'none'}
                            keyboardType={'default'}
                            error={errors.password}
                            secureTextEntry={isSecureEntry}
                        />
                    </View>
                    <TouchableOpacity style={styles.forgotPasswordButton} onPress={forgotPassword}>
                        <Text style={[font_styles.secondary_text, { color: Colors.accent }]}>
                            {strings.login.forgot_password}
                        </Text>
                    </TouchableOpacity>
                </View>
                <View style={styles.buttonContainer}>
                    <Button
                        title={strings.login.log_in}
                        loading={loading}
                        block={true}
                        raised={true}
                        type='solid'
                        onPress={handleSubmit}
                        style={styles.loginButton}
                    />
                    <Button
                        title={strings.login.create_account}
                        type='outline'
                        block={true}
                        raised={true}
                        onPress={register}
                    />
                </View>
            </View>
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
    loginForm: {
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
    forgotPasswordButton: {
        alignSelf: 'flex-end',
    },
    loginButton: {
        marginBottom: verticalScale(8),
    },
});

LoginScreen.propTypes = {
    navigation: PropTypes.object,
};

export default LoginScreen;
