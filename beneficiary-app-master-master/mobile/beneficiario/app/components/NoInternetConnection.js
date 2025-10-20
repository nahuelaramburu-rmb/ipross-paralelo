import React, { useEffect, useState, useRef } from 'react';
import { StyleSheet, Easing, Platform, Text, Animated } from 'react-native';
import NetInfo from '@react-native-community/netinfo';
import { NAV_BAR_HEIGHT_IOS, NAV_BAR_HEIGHT_ANDROID } from './header/HeaderWrapper';
import { verticalScale } from '../lib/size-normalizer';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { font_styles } from '../lib/default-styles';
import * as Colors from '../constants/Colors';
import { useSelector, shallowEqual } from 'react-redux';

const NAV_HEIGHT = Platform.select({ ios: NAV_BAR_HEIGHT_IOS, android: NAV_BAR_HEIGHT_ANDROID });
const EASING = Easing.ease;

const NoInternetConnection = () => {
    const [isConnected, setIsConnected] = useState(true);
    const animationBounce = useRef(new Animated.Value(0)).current;
    const insets = useSafeAreaInsets();

    const { appState } = useSelector(
        (state) => ({
            appState: state.profile.status,
        }),
        shallowEqual
    );

    useEffect(() => {
        const unsubscribe = NetInfo.addEventListener((state) => {
            setIsConnected(state.isConnected);
        });
        return unsubscribe;
    }, []);

    useEffect(() => {
        if (!isConnected) {
            Animated.timing(animationBounce, {
                toValue: 1,
                duration: 500,
                easing: EASING,
                useNativeDriver: true,
            }).start();
        }

        return () => {
            Animated.timing(animationBounce, {
                toValue: 0,
                duration: 500,
                easing: EASING,
                useNativeDriver: true,
            }).start();
        };
    }, [isConnected, animationBounce]);

    if (appState !== 'logged_in') return null;

    return (
        <Animated.View
            style={[
                styles.container,
                { top: insets.top + NAV_HEIGHT, transform: [{ scale: animationBounce }] },
            ]}>
            <Text style={[font_styles.secondary_text_bold, styles.text]}>No hay conexión a internet</Text>
        </Animated.View>
    );
};

const styles = StyleSheet.create({
    container: {
        ...StyleSheet.absoluteFillObject,
        height: verticalScale(24),
        backgroundColor: Colors.error,
        zIndex: 100,
        alignItems: 'center',
        justifyContent: 'center',
        opacity: 0.9,
    },
    text: {
        color: Colors.white,
    },
});

export default NoInternetConnection;
