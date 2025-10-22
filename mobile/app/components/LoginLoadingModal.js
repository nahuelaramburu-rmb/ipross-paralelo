import React, { useEffect, useRef } from 'react';
import { Modal, View, StyleSheet, Animated, Easing, Dimensions } from 'react-native';
import PropTypes from 'prop-types';
import images from '../configs/images';
import * as Colors from '../constants/Colors';

const { width } = Dimensions.get('window');

const LoginLoadingModal = ({ visible }) => {
    const scaleAnim = useRef(new Animated.Value(0.3)).current;
    const fadeAnim = useRef(new Animated.Value(0)).current;
    const rotateAnim = useRef(new Animated.Value(0)).current;

    useEffect(() => {
        if (visible) {
            // Resetear animaciones
            scaleAnim.setValue(0.3);
            fadeAnim.setValue(0);
            rotateAnim.setValue(0);

            // Secuencia de animación: fade in + scale up + rotate
            Animated.sequence([
                Animated.parallel([
                    Animated.timing(fadeAnim, {
                        toValue: 1,
                        duration: 400,
                        useNativeDriver: true,
                    }),
                    Animated.spring(scaleAnim, {
                        toValue: 1,
                        tension: 50,
                        friction: 7,
                        useNativeDriver: true,
                    }),
                ]),
                Animated.loop(
                    Animated.timing(rotateAnim, {
                        toValue: 1,
                        duration: 2000,
                        easing: Easing.linear,
                        useNativeDriver: true,
                    })
                ),
            ]).start();
        }
    }, [visible, scaleAnim, fadeAnim, rotateAnim]);

    const spin = rotateAnim.interpolate({
        inputRange: [0, 1],
        outputRange: ['0deg', '360deg'],
    });

    return (
        <Modal
            transparent={true}
            visible={visible}
            animationType="none"
            statusBarTranslucent={true}
        >
            <View style={styles.overlay}>
                <View style={styles.container}>
                    <Animated.Image
                        source={images.iprossLogo}
                        style={[
                            styles.logo,
                            {
                                opacity: fadeAnim,
                                transform: [
                                    { scale: scaleAnim },
                                    { rotate: spin }
                                ],
                            },
                        ]}
                        resizeMode="contain"
                    />
                </View>
            </View>
        </Modal>
    );
};

LoginLoadingModal.propTypes = {
    visible: PropTypes.bool.isRequired,
};

const styles = StyleSheet.create({
    overlay: {
        flex: 1,
        backgroundColor: 'rgba(255, 255, 255, 0.95)',
        justifyContent: 'center',
        alignItems: 'center',
    },
    container: {
        width: width * 0.6,
        height: width * 0.6,
        justifyContent: 'center',
        alignItems: 'center',
        backgroundColor: Colors.white,
        borderRadius: 20,
        shadowColor: '#000',
        shadowOffset: {
            width: 0,
            height: 4,
        },
        shadowOpacity: 0.15,
        shadowRadius: 8,
        elevation: 8,
    },
    logo: {
        width: width * 0.5,
        height: width * 0.5,
    },
});

export default LoginLoadingModal;
