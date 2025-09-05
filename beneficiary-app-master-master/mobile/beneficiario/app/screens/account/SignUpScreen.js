import React, { useState, useEffect, useRef, PureComponent } from 'react';
import { StyleSheet, View } from 'react-native';
import { connect } from 'react-redux';
import Icon from 'react-native-vector-icons/Ionicons';
import moment from 'moment';
import * as Colors from '../../constants/Colors';
import strings from '../../constants/Strings';
import TextField from '../../components/TextField';
import { moderateScale, verticalScale } from '../../lib/size-normalizer';
import AuthenticationWrapper from '../../components/AuthenticationWrapper';
import { useDispatch, useSelector } from 'react-redux';
import {
    validateData as validate,
    confirmAccount as confirm,
    clearValidationError,
} from '../../actions/profileAction';
import Dropdown, { Option } from '../../components/dropdown';
import * as Yup from 'yup';
import { useFormik } from 'formik';
import DatePicker from '../../components/DatePicker';
import PropTypes from 'prop-types';
import { regular } from '../../constants/Fonts';

const UserDataSchema = Yup.object().shape({
    beneficiaryId: Yup.string()
        .matches(/^\d{2}-[0-9]*\/\d{2}$/g, strings.errors.invalid_beneficiary_code)
        .required(strings.errors.required),
    idType: Yup.string().required(strings.errors.required),
    idNumber: Yup.string().required(strings.errors.required),
    birthDate: Yup.date().required(strings.errors.required),
});

const AccountSchema = Yup.object().shape({
    email: Yup.string().email(strings.errors.email_not_valid).required(strings.errors.required),
    password: Yup.string().required(strings.errors.required),
    password2: Yup.string()
        .oneOf([Yup.ref('password'), null], strings.errors.passwords_must_match)
        .required(strings.errors.required),
});

const DataValidationForm = ({
    navigation,
    handleFormChange,
    handleIdentityValueChange,
    handleIdentityTypeChange,
}) => {
    const beneficiaryIdRef = useRef(null);
    const idTypeRef = useRef(null);
    const idNumberRef = useRef(null);
    const birthDateRef = useRef(null);

    const dispatch = useDispatch();

    const validateData = (values) => {
        const obj = {
            beneficiaryCode: values.beneficiaryId,
            birthDate: moment(values.birthDate, 'DD-MM-YYYY').format('YYYY-MM-DD'),
            idType: values.idType.toUpperCase(),
            idNumber: values.idNumber,
        };

        dispatch(validate(obj))
            .then(() => {
                handleIdentityValueChange(values.idNumber);
                handleIdentityTypeChange(values.idType);
                handleFormChange(1);
            })
            .catch(() => null);
    };

    const { handleChange, handleBlur, handleSubmit, errors, touched, values, setFieldValue } = useFormik({
        validationSchema: UserDataSchema,
        initialValues: { beneficiaryId: '', idType: '', idNumber: '', birthDate: '' },
        onSubmit: validateData,
    });

    useEffect(() => {
        navigation.setParams({ onValidate: handleSubmit });
    }, [handleSubmit, navigation]);

    const handleIdTypeChange = (text) => {
        setFieldValue('idType', text);
    };

    const handleDatePickerChange = (date) => {
        setFieldValue('birthDate', date);
    };

    return (
        <View style={[styles.form]}>
            <View style={styles.inputContainer}>
                <TextField
                    touched={touched.beneficiaryId}
                    ref={beneficiaryIdRef}
                    leftIcon='ios-person-circle-outline'
                    placeholder={strings.signUp.beneficiary_number}
                    value={values.beneficiaryId}
                    autoCorrect={false}
                    enablesReturnKeyAutomatically={true}
                    onChangeText={handleChange('beneficiaryId')}
                    returnKeyType='next'
                    onBlur={handleBlur('beneficiaryId')}
                    onSubmitEditing={() => idTypeRef.current?.focus()}
                    autoCapitalize={'none'}
                    keyboardType={'default'}
                    error={errors.beneficiaryId}
                />
            </View>
            <View style={styles.inputContainer}>
                <Dropdown
                    leftIcon='ios-create'
                    onChangeText={handleIdTypeChange}
                    onBlur={() => idNumberRef.current?.focus()}
                    ref={idTypeRef}
                    placeholder={strings.signUp.id_type}
                    error={errors.idType}
                    touched={touched.idType}>
                    <Option id='dni'>Documento Nacional de Identidad</Option>
                    <Option id='pas'>Pasaporte</Option>
                    <Option id='lc'>Libreta Cívica</Option>
                    <Option id='le'>Libreta de Enrolamiento</Option>
                </Dropdown>
            </View>
            <View style={styles.inputContainer}>
                <TextField
                    touched={touched.idNumber}
                    ref={idNumberRef}
                    leftIcon='ios-finger-print'
                    placeholder={strings.signUp.id_number}
                    value={values.idNumber}
                    autoCorrect={false}
                    enablesReturnKeyAutomatically={true}
                    onChangeText={handleChange('idNumber')}
                    returnKeyType='next'
                    onBlur={handleBlur('idNumber')}
                    onSubmitEditing={() => birthDateRef.current?.focus()}
                    autoCapitalize='none'
                    keyboardType='numeric'
                    error={errors.idNumber}
                />
            </View>
            <View style={styles.inputContainer}>
                <DatePicker
                    leftIcon='ios-calendar'
                    touched={touched.birthDate}
                    ref={birthDateRef}
                    placeholder={strings.signUp.date_of_birth}
                    onChangeDate={handleDatePickerChange}
                    value={values.birthDate}
                    error={errors.birthDate}
                />
            </View>
        </View>
    );
};

const AccountCreationForm = ({ identityValue, navigation, identityType }) => {
    const [isSecuryEntryPassword, setIsSecurityPassword] = useState(true);
    const [isSecuryEntryPassword2, setIsSecurityPassword2] = useState(true);

    const emailRef = useRef();
    const passwordRef = useRef();
    const password2Ref = useRef();

    useEffect(() => {
        passwordRef?.current?.setNativeProps({
            style: { fontFamily: regular },
        });
        password2Ref?.current?.setNativeProps({
            style: { fontFamily: regular },
        });
    }, [isSecuryEntryPassword, isSecuryEntryPassword2]);

    const dispatch = useDispatch();

    const { validatedData } = useSelector((state) => ({
        validatedData: state.profile.account.validated_data,
    }));

    const confirmAccount = (values) => {
        const account = {
            role: {
                name: 'beneficiary',
            },
            user: {
                username: identityValue,
                password: values.password,
                email: values.email,
                resource_id: validatedData.resourceId,
                profile: {
                    name: validatedData.name,
                    last_name: validatedData.lastName,
                    id_number: parseInt(identityValue),
                    id_type: identityType.toUpperCase(),
                },
            },
        };

        dispatch(confirm(account));
    };

    const { handleChange, handleBlur, handleSubmit, errors, touched, values } = useFormik({
        validationSchema: AccountSchema,
        initialValues: { email: '', password: '', password2: '' },
        onSubmit: confirmAccount,
    });

    useEffect(() => {
        navigation.setParams({ onConfirm: handleSubmit });
    }, [handleSubmit, navigation]);

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
        <View style={[styles.form]}>
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
                    returnKeyType='next'
                    onBlur={handleBlur('email')}
                    onSubmitEditing={() => passwordRef.current?.focus()}
                    autoCapitalize='none'
                    keyboardType='email-address'
                    error={errors.email}
                />
            </View>
            <View style={styles.inputContainer}>
                <TextField
                    disabled={true}
                    leftIcon='ios-person'
                    placeholder={strings.signUp.id_number}
                    value={identityValue}
                    autoCorrect={false}
                    enablesReturnKeyAutomatically={true}
                    returnKeyType='next'
                    autoCapitalize='none'
                />
            </View>
            <View style={styles.inputContainer}>
                <TextField
                    touched={touched.password}
                    ref={passwordRef}
                    leftIcon='ios-lock-closed'
                    placeholder={strings.common.password}
                    value={values.password}
                    autoCorrect={false}
                    enablesReturnKeyAutomatically={true}
                    onChangeText={handleChange('password')}
                    returnKeyType='next'
                    onBlur={handleBlur('password')}
                    onSubmitEditing={() => password2Ref.current?.focus()}
                    autoCapitalize='none'
                    keyboardType='default'
                    error={errors.password}
                    secureTextEntry={isSecuryEntryPassword}
                    rightIcon={renderAccessory('1')}
                />
            </View>
            <View style={styles.inputContainer}>
                <TextField
                    touched={touched.password2}
                    ref={password2Ref}
                    leftIcon='ios-lock-closed'
                    placeholder={strings.common.password_again}
                    value={values.password2}
                    autoCorrect={false}
                    enablesReturnKeyAutomatically={true}
                    onChangeText={handleChange('password2')}
                    returnKeyType='next'
                    onBlur={handleBlur('password2')}
                    autoCapitalize='none'
                    keyboardType='default'
                    error={errors.password2}
                    secureTextEntry={isSecuryEntryPassword2}
                    rightIcon={renderAccessory('2')}
                />
            </View>
        </View>
    );
};

class SignUpScreen extends PureComponent {
    constructor(props) {
        super(props);
        this.state = {
            identityValue: '',
            identityType: '',
            currentPosition: 0,
        };

        this._handleFormChange = this._handleFormChange.bind(this);
        this._handleIdentityValueChange = this._handleIdentityValueChange.bind(this);
        this._handleIdentityTypeChange = this._handleIdentityTypeChange.bind(this);
    }

    componentDidUpdate(prevProps, prevState) {
        if (this.props.validation_error !== prevProps.validation_error && this.props.validation_error) {
            this.props.clearValidationError();
        }

        if (prevProps.loading_data_validation !== this.props.loading_data_validation) {
            this.props.navigation.setParams({
                isValidatingData: this.props.loading_data_validation,
            });
        }

        if (prevProps.loading_account_creation !== this.props.loading_account_creation) {
            this.props.navigation.setParams({
                isConfirmingAccount: this.props.loading_account_creation,
            });
        }
    }

    _handleFormChange(form) {
        this.setState({ currentPosition: form });
    }

    _handleIdentityValueChange(identityValue) {
        this.setState({ identityValue });
    }

    _handleIdentityTypeChange(identityType) {
        this.setState({ identityType });
    }

    render() {
        return (
            <AuthenticationWrapper>
                <View style={styles.container}>
                    {this.state.currentPosition === 0 ? (
                        <DataValidationForm
                            navigation={this.props.navigation}
                            handleFormChange={this._handleFormChange}
                            handleIdentityValueChange={this._handleIdentityValueChange}
                            handleIdentityTypeChange={this._handleIdentityTypeChange}
                            identityType={this.state.identityType}
                            identityValue={this.state.identityValue}
                        />
                    ) : (
                        <AccountCreationForm
                            identityValue={this.state.identityValue}
                            navigation={this.props.navigation}
                            identityType={this.state.identityType}
                        />
                    )}
                </View>
            </AuthenticationWrapper>
        );
    }
}

DataValidationForm.propTypes = {
    navigation: PropTypes.object,
    handleFormChange: PropTypes.func,
    handleIdentityValueChange: PropTypes.func,
    handleIdentityTypeChange: PropTypes.func,
};

AccountCreationForm.propTypes = {
    identityValue: PropTypes.string,
    navigation: PropTypes.object,
    identityType: PropTypes.string,
};

SignUpScreen.propTypes = {
    clearValidationError: PropTypes.func,
    loading_data_validation: PropTypes.bool,
    loading_account_creation: PropTypes.bool,
    validated_data: PropTypes.object,
    error: PropTypes.object,
    validation_error: PropTypes.boolean,
    navigation: PropTypes.object,
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
        flex: 0.7,
        width: '100%',
        flexDirection: 'column',
        justifyContent: 'center',
    },
    icon: {
        marginRight: moderateScale(12),
        alignItems: 'center',
    },
    inputContainer: {
        marginBottom: verticalScale(16, 0.25),
    },
});

function mapStateToProps(state) {
    return {
        loading_data_validation: state.profile.account.validating_data,
        loading_account_creation: state.profile.account.creating_account,
        validated_data: state.profile.account.validated_data,
        error: state.error,
        validation_error: state.profile.account.error,
    };
}

export default connect(mapStateToProps, {
    clearValidationError,
})(SignUpScreen);
