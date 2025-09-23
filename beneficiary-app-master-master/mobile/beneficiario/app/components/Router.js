import React, { useCallback, useEffect, useRef } from 'react';
import { useDispatch } from 'react-redux';
import { NavigationContainer } from '@react-navigation/native';
import { createStackNavigator, CardStyleInterpolators } from '@react-navigation/stack';
import { createDrawerNavigator } from '@react-navigation/drawer';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';

import { moderateScale } from '../lib/size-normalizer';
import Splash from '../screens/SplashScreen';
import LoginScreen from '../screens/account/LoginScreen';
import RenewPasswordScreen from '../screens/account/RenewPasswordScreen';
import SignUpScreen from '../screens/account/SignUpScreen';
import BeneficiaryInformationScreen from '../screens/user/BeneficiaryInformationScreen';
import CoinsuranceChargeScreen from '../screens/charge/CoinsuranceChargeScreen';
import Drawer from '../components/Drawer';
import TopNavigation from '../components/TopNavigation';
import TabBar from './TabBar';
import { QrCodeGeneratorScreen, TokenCodeGeneratorScreen } from '../screens/user';
import FamilyManagementScreen from '../screens/user/FamilyManagementScreen';
import FilterModal from '../screens/FilterModalScreen';
import ForgotPasswordScreen from '../screens/account/ForgotPasswordScreen';
import ProcedureScreen from '../screens/procedure/ProcedureScreen';
import NewProcedureScreen from '../screens/procedure/NewProcedureScreen';
import ProcedureDetailScreen from '../screens/procedure/ProcedureDetailScreen';
import ProcedureMessagesScreen from '../screens/procedure/ProcedureMessagesScreen';
import BatchScreen from '../screens/discapacity/BatchScreen';
import BatchDetailScreen from '../screens/discapacity/BatchDetailScreen';
import AppointmentScreen from '../screens/appointment/AppointmentScreen';
import NewAppointmentScreen from '../screens/appointment/NewAppointmentScreen';
import AppointmentDetail from './appointment/AppointmentDetail';
import PrescriptionDetail from './prescription/PrescriptionDetail';
import ProfessionalScreen from '../screens/professional/ProfessionalScreen';
import ProfessionalListScreen from '../screens/professional/ProfessionalListScreen';
import ProfessionalDetailScreen from '../screens/professional/ProfessionalDetailScreen';
import ProfessionalMapsScreen from '../screens/professional/ProfessionalMapsScreenTemp';
import {
    ValidationScreen,
    AuthorizationRatingScreen,
    AuthorizationUnawarenessScreen,
    PreAuthorizationScreen,
    ValidationStatusScreen,
    PreAuthorizationDetailScreen,
} from '../screens/validation';
import SignUpHelpModal from '../screens/SignUpHelpModal';
import { navigationRef } from '../lib/NavigationService';
import { updateScene } from '../actions/routerAction';

const MainTab = createBottomTabNavigator();
const MainAppStack = createStackNavigator();
const AppDrawer = createDrawerNavigator();
const SignUpStack = createStackNavigator();
const AuthStack = createStackNavigator();
const RootStack = createStackNavigator();

const MainTabScreens = () => {
    return (
        <MainTab.Navigator
            initialRouteName='BeneficiaryInformation'
            tabBar={(props) => <TabBar {...props} />}>
            <MainTab.Screen name='BeneficiaryInformation' component={BeneficiaryInformationScreen} />
            <MainTab.Screen name='Authorizations' component={ValidationScreen} />
            <MainTab.Screen name='CoinsuranceCharges' component={CoinsuranceChargeScreen} />
            <MainTab.Screen name='Procedures' component={ProcedureScreen} />
        </MainTab.Navigator>
    );
};

const MainAppStackScreens = () => {
    return (
        <MainAppStack.Navigator
            initialRouteName='MainTabScreens'
            mode='card'
            headerMode='screen'
            screenOptions={{
                cardStyleInterpolator: CardStyleInterpolators.forHorizontalIOS,
                headerShown: true,
                header: (props) => <TopNavigation {...props} />,
            }}>
            <MainAppStack.Screen name='MainTabScreens' component={MainTabScreens} />
            <MainAppStack.Screen name='QrToken' component={QrCodeGeneratorScreen} />
            <MainAppStack.Screen name='OTPToken' component={TokenCodeGeneratorScreen} />
            <MainAppStack.Screen name='FamilyManagement' component={FamilyManagementScreen} />
            <MainAppStack.Screen name='NewProcedure' component={NewProcedureScreen} />
            <MainAppStack.Screen name='ProcedureDetail' component={ProcedureDetailScreen} />
            <MainAppStack.Screen name='ProcedureMessages' component={ProcedureMessagesScreen} />
            <MainAppStack.Screen name='PrescriptionDetail' component={PrescriptionDetail} />
            <MainAppStack.Screen name='ValidationStatus' component={ValidationStatusScreen} />
            <MainAppStack.Screen name='AuthorizationRating' component={AuthorizationRatingScreen} />
            <MainAppStack.Screen name='AuthorizationUnawareness' component={AuthorizationUnawarenessScreen} />
            <MainAppStack.Screen name='PreAuthorizations' component={PreAuthorizationScreen} />
            <MainAppStack.Screen name='PreAuthorizationDetail' component={PreAuthorizationDetailScreen} />
            <MainAppStack.Screen name='Batch' component={BatchScreen} />
            <MainAppStack.Screen name='BatchDetail' component={BatchDetailScreen} />
            <MainAppStack.Screen name='Appointment' component={AppointmentScreen} />
            <MainAppStack.Screen name='NewAppointment' component={NewAppointmentScreen} />
            <MainAppStack.Screen name='AppointmentDetail' component={AppointmentDetail} />
            <MainAppStack.Screen name='Professional' component={ProfessionalScreen} />
            <MainAppStack.Screen name='ProfessionalList' component={ProfessionalListScreen} />
            <MainAppStack.Screen name='ProfessionalDetail' component={ProfessionalDetailScreen} />
            <MainAppStack.Screen name='ProfessionalMaps' component={ProfessionalMapsScreen} />

            <MainAppStack.Screen
                name='FilterModal'
                component={FilterModal}
                options={{ cardStyleInterpolator: CardStyleInterpolators.forVerticalIOS }}
            />
        </MainAppStack.Navigator>
    );
};

const AppDrawerScreens = () => {
    return (
        <AppDrawer.Navigator
            initialRouteName='MainAppStackScreens'
            drawerPosition='left'
            drawerType='front'
            drawerStyle={{ width: moderateScale(250) }}
            drawerContent={(props) => <Drawer {...props} />}
            screenOptions={{
                headerShown: false,
                swipeEnabled: false,
            }}>
            <AppDrawer.Screen name='MainAppStackScreens' component={MainAppStackScreens} />
        </AppDrawer.Navigator>
    );
};

const SignUpStackScreens = () => {
    return (
        <SignUpStack.Navigator initialRouteName='SignUp' mode='modal' headerMode='float'>
            <SignUpStack.Screen
                name='SignUp'
                component={SignUpScreen}
                options={{
                    header: (props) => <TopNavigation {...props} />,
                }}
            />
            <SignUpStack.Screen
                name='SignUpHelpModal'
                component={SignUpHelpModal}
                options={{
                    header: (props) => <TopNavigation {...props} />,
                }}
            />
        </SignUpStack.Navigator>
    );
};

const AuthStackScreens = () => {
    return (
        <AuthStack.Navigator
            initialRouteName='Login'
            mode='card'
            headerMode='screen'
            screenOptions={{
                cardStyleInterpolator: CardStyleInterpolators.forHorizontalIOS,
            }}>
            <AuthStack.Screen
                name='Login'
                component={LoginScreen}
                options={{
                    header: (props) => <TopNavigation {...props} />,
                }}
            />
            <AuthStack.Screen
                name='PasswordReset'
                component={RenewPasswordScreen}
                options={{ headerShown: false }}
            />
            <AuthStack.Screen
                name='SignUpStackScreens'
                component={SignUpStackScreens}
                options={{ headerShown: false }}
            />
            <AuthStack.Screen
                name='ForgotPassword'
                component={ForgotPasswordScreen}
                options={{
                    header: (props) => <TopNavigation {...props} />,
                }}
            />
        </AuthStack.Navigator>
    );
};

const AppContainer = ({ status }) => {
    const routeNameRef = useRef();
    const dispatch = useDispatch();
    const isLoggedOut = status === 'logged_out';
    const isLoading = status === 'initializing';

    useEffect(() => {
        const state = navigationRef.current.getRootState();
        routeNameRef.current = getActiveRouteName(state);
    }, [getActiveRouteName]);

    const handleStateChange = (state) => {
        const previousRouteName = routeNameRef.current;
        const currentRouteName = getActiveRouteName(state);

        if (previousRouteName !== currentRouteName) {
            dispatch(updateScene(currentRouteName));
        }

        routeNameRef.current = currentRouteName;
    };

    const getActiveRouteName = useCallback((state) => {
        const route = state.routes[state.index];

        if (route.state) {
            return getActiveRouteName(route.state);
        }

        return route.name;
    }, []);

    return (
        <NavigationContainer onStateChange={handleStateChange} ref={navigationRef}>
            <RootStack.Navigator screenOptions={{ headerShown: false }}>
                {isLoading ? (
                    <RootStack.Screen name='Splash' component={Splash} />
                ) : isLoggedOut ? (
                    <RootStack.Screen name='AuthStackScreens' component={AuthStackScreens} />
                ) : (
                    <RootStack.Screen name='AppDrawerScreens' component={AppDrawerScreens} />
                )}
            </RootStack.Navigator>
        </NavigationContainer>
    );
};

export default AppContainer;
