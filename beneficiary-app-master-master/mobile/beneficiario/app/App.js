import React, { PureComponent } from 'react';
import { Alert, AppState, Linking, BackHandler, Platform } from 'react-native';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { connect } from 'react-redux';
import DropdownAlertMessage from './components/DropdownAlertMessage';
import ErrorBoundary from './components/ErrorBoundary';
import AppContainer from './components/Router';
import { navigate } from './lib/NavigationService';
import Firebase from '@react-native-firebase/app';
import PushNotification from 'react-native-push-notification';
import AsyncStorage from '@react-native-async-storage/async-storage';
import strings from './constants/Strings';
import { enableScreens } from 'react-native-screens';
import {
    getSavedData,
    updateUserData,
    registerUserDevice,
    checkUpdationNedded,
} from './actions/profileAction';
import { loginApiTurnos } from './actions/appointmentAction'

import { updateScene } from './actions/routerAction';
import { getValidations } from './actions/validationAction';
import { getUserCharges } from './actions/chargeAction';
import { reportErrors, sendError } from './actions/errorAction';
import { getPrescriptions } from './actions/prescriptionAction';

import moment from 'moment';
import 'moment/locale/es-us';
import NoInternetConnection from './components/NoInternetConnection';
moment.locale('es-us');

enableScreens();

const notificationHandler = (notification) => {
    const { data } = notification;
    switch (data.type) {
        case 'VALIDATION_UPDATE':
            navigate('ValidationStatus', {
                valId: data.authorizationId,
            });
            break;
        case 'PROCEDURE_NEW_MESSAGE':
            navigate('ProcedureDetail', {
                procedureId: data.procedureId,
                procedureType: data.procedureType,
                type: 'NEW_MESSAGE',
                key: 'APage' + Math.random() * 10000, // EL KEY ESTÁ PARA QUE ME UPDATEE LOS PARAMS EN EL COMPONENTE DESTINO
            });
            break;
        case 'PROCEDURE_UPDATE':
            navigate('ProcedureDetail', {
                procedureId: data.procedureId,
                procedureType: data.procedureType,
            });
            break;
        case 'NEW_PRESCRIPTION':
            navigate('PrescriptionDetail', {
                prescriptionId: data.prescriptionId,
            });
            break;
        case 'CAMPAIGN':
            setTimeout(() => {
                Linking.openURL(data.url).catch((err) => console.error('An error occurred', err));
            }, 500);
            break;
        default:
            break;
    }
};

PushNotification.configure({
    onNotification: function (notification) {
        // Solo handleo notis que se generan local (es decir, aquellas que se generan en foreground)
        if (notification.foreground) notificationHandler(notification);
    },
});

class App extends PureComponent {
    constructor(props) {
        super(props);
        this.state = {
            status: 'initializing',
            appState: AppState.currentState,
            checkingUpdation: false,
        };

        this.notificationListener = null;
        this.messageListener = null;
        this.notificationOpenedListener = null;
        this.updateTokenListener = null;

        this._handleAppStateChange = this._handleAppStateChange.bind(this);
        this._handleBackButton = this._handleBackButton.bind(this);
        this._checkUpdation = this._checkUpdation.bind(this);
        this._showUpdationModal = this._showUpdationModal.bind(this);
        this._createLocalNotification = this._createLocalNotification.bind(this);
    }

    async componentDidMount() {
        AppState.addEventListener('change', this._handleAppStateChange);
        BackHandler.addEventListener('hardwareBackPress', this._handleBackButton);
        if (this.state.status === 'initializing') {
            const updationNedded = await this._checkUpdation();
            if (!updationNedded) this.props.getSavedData();
        }
    }

    async _checkUpdation() {
        if (this.state.checkingUpdation) return;
        try {
            this.setState({ checkingUpdation: true });
            const updationNedded = await this.props.checkUpdationNedded();
            if (updationNedded) this._showUpdationModal();
            return updationNedded;
        } catch (err) {
            console.log(err);
            return false;
        } finally {
            this.setState({ checkingUpdation: false });
        }
    }

    _showUpdationModal() {
        Alert.alert(
            strings.general.updation_needed,
            strings.general.updation_needed_description,
            [
                {
                    text: strings.general.go_to_playstore,
                    onPress: () => {
                        const url =
                            Platform.OS === 'android'
                                ? 'market://details?id=com.capacidad.beneficiaryapp'
                                : 'itms-apps://itunes.apple.com/us/app/apple-store/id1511226661?';
                        Linking.openURL(url).catch((err) => console.error('An error occurred', err));
                    },
                },
            ],
            { cancelable: false }
        );
    }

    componentDidUpdate(prevProps, prevState) {
        if (prevState.status !== this.state.status && this.state.status === 'logged_in') {
            this._checkFirebasePermission();
            this._createNotificationListeners();
            this._createFirebaseChannel(); // To display notifications
            this._createUpdateTokenListener();
            this.props.getValidations(false);
            this.props.getUserCharges(false);
            this.props.getPrescriptions({});
        }
    }

    componentWillUnmount() {
        AppState.removeEventListener('change', this._handleAppStateChange);
        BackHandler.removeEventListener('hardwareBackPress', this._handleBackButton);
        if (this.notificationListener) this.notificationListener();
        if (this.notificationOpenedListener) this.notificationOpenedListener();
        if (this.updateTokenListener) this.updateTokenListener();
    }

    static getDerivedStateFromProps(props, state) {
        if (props.status !== state.status) return { status: props.status };

        return null;
    }

    _handleBackButton() {
        if (this.props.currentScene === 'BeneficiaryInformation' || this.props.currentScene === 'Login') {
            BackHandler.exitApp();
            return true;
        }
    }

    async _handleAppStateChange(nextAppState) {
        if (this.state.appState.match(/inactive|background/) && nextAppState === 'active') {
            this._checkUpdation();
            if (this.state.status === 'initializing') return;
            if (this.state.status === 'logged_in') {
                this.props.updateUserData();
            }
        }

        this.setState({ appState: nextAppState });
    }

    async _getFirebaseToken() {
        /* solo devuelvo el token cuando no lo tengo en local storage, eso quiere decir que no registré el device en el notification service */
        let fcmToken = await AsyncStorage.getItem('fcmToken');
        if (!fcmToken) {
            fcmToken = await Firebase.messaging().getToken();
            return fcmToken;
        }
    }

    async _requestFirebasePermission() {
        try {
            await Firebase.messaging().requestPermission();
            return await this._getFirebaseToken();
        } catch (error) {
            console.log('permission rejected');
        }
    }

    async _checkFirebasePermission() {
        const enabled = await Firebase.messaging().hasPermission();
        let token = null;
        if (enabled) {
            token = await this._getFirebaseToken();
        } else {
            token = await this._requestFirebasePermission();
        }

        /* Veo si ya fui contra el server de notifications, en caso de que no, voy */
        if (typeof token !== 'undefined') this.props.registerUserDevice(token);
    }

    async _createNotificationListeners() {
        this.notificationOpenedListener = Firebase.messaging().onNotificationOpenedApp((notificationOpen) => {
            // SALE POR ACA CUANDO TAPPEO EL BANNER DE LA NOTIFICACIÓN Y LA APP ESTABA FOREGROUND O BACKGROUND
            notificationHandler(notificationOpen);
        });

        const notificationOpen = await Firebase.messaging().getInitialNotification();
        if (notificationOpen) {
            // SALE POR ACA CUANDO TAPPEO EL BANNER DE LA NOTIFICACIÓN Y LA APP ESTABA KILLED
            notificationHandler(notificationOpen);
        }

        this.messageListener = Firebase.messaging().onMessage((message) => {
            // SALE POR ACA CUANDO LA APP ESTÁ EN FOREGROUND Y EN EL MESSAGE VIENE SOLO DATA (NO NOTIFICATION)
            this._createLocalNotification(message);
        });
    }

    _createLocalNotification(message) {
        const { notification, data } = message;
        PushNotification.localNotification({
            mesageId: message.messageId,
            title: notification.title,
            message: notification.body || '',
            channelId: 'notification-channel',
            color: '#000000',
            userInfo: data,
        });
    }

    _createFirebaseChannel() {
        PushNotification.createChannel(
            {
                channelId: 'notification-channel',
                channelName: 'Notification Channel',
                channelDescription: 'Channel for displaying notifications',
                importance: 4,
            },
            (created) => console.log(`createChannel returned '${created}'`)
        );
    }

    _createUpdateTokenListener() {
        this.updateTokenListener = Firebase.messaging().onTokenRefresh((fcmToken) => {
            this.props.registerUserDevice(fcmToken);
        });
    }

    render() {
        const { status } = this.state;
        return (
            <SafeAreaProvider>
                <ErrorBoundary sendError={(err) => this.props.sendError(err)}>
                    <NoInternetConnection />
                    <AppContainer status={status} />
                </ErrorBoundary>
                <DropdownAlertMessage />
            </SafeAreaProvider>
        );
    }
}

function mapStateToProps(state) {
    return {
        status: state.profile.status,
        currentScene: state.router.currentScene,
    };
}

export default connect(mapStateToProps, {
    reportErrors,
    getSavedData,
    updateScene,
    getValidations,
    updateUserData,
    loginApiTurnos,
    getUserCharges,
    sendError,
    registerUserDevice,
    getPrescriptions,
    checkUpdationNedded,
})(App);
