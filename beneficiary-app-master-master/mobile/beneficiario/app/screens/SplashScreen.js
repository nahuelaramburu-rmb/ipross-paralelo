import React, { useEffect, useRef } from 'react';
import { StyleSheet, View, Dimensions, Image, Animated } from 'react-native';
import images from '../configs/images';

const { width } = Dimensions.get('window');

const iprossLogoAspectRatio = 3126 / 1426;

const Splash = () => {
    const scaleAnim = useRef(new Animated.Value(0.8)).current;
    const opacityAnim = useRef(new Animated.Value(0)).current;
    const rotateAnim = useRef(new Animated.Value(0)).current;

    useEffect(() => {
        // Animación de entrada del logo
        Animated.parallel([
            Animated.timing(scaleAnim, {
                toValue: 1,
                duration: 1000,
                useNativeDriver: true,
            }),
            Animated.timing(opacityAnim, {
                toValue: 1,
                duration: 800,
                useNativeDriver: true,
            }),
        ]).start();

        // Animación de pulso continua
        Animated.loop(
            Animated.sequence([
                Animated.timing(scaleAnim, {
                    toValue: 1.05,
                    duration: 1500,
                    useNativeDriver: true,
                }),
                Animated.timing(scaleAnim, {
                    toValue: 1,
                    duration: 1500,
                    useNativeDriver: true,
                }),
            ])
        ).start();
    }, []);

    return (
        <View style={styles.container}>
            <Animated.View 
                style={{
                    aspectRatio: iprossLogoAspectRatio,
                    width: width * 0.7,
                    transform: [{ scale: scaleAnim }],
                    opacity: opacityAnim,
                }}
            >
                <Image 
                    source={images.iprossLogo} 
                    style={styles.image}
                    resizeMode="contain"
                />
            </Animated.View>
        </View>
    );
};

export default Splash;

const styles = StyleSheet.create({
    container: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
        backgroundColor: '#f5f5f5', // Fondo gris en lugar de verde
    },
    image: {
        width: '100%',
        height: '100%',
    },
});
