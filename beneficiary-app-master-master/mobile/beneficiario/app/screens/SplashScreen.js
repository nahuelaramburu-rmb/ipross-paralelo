import React from 'react';
import { StyleSheet, View, Dimensions, Image } from 'react-native';
import images from '../configs/images';
import BackgroundSvg, { originalWidth, originalHeight } from '../components/BackgroundSvg';
import RnLogo from '../images/rn-logo.svg';

const { width } = Dimensions.get('window');

const backgroundAspectRatio = originalWidth / originalHeight;
const iprossLogoAspectRatio = 3126 / 1426;
const rnLogoAspectRatio = 346.5 / 274.9;

const Splash = () => {
    return (
        <View style={styles.container}>
            <View style={styles.backgroundContainer}>
                <BackgroundSvg
                    width={width}
                    height={'100%'}
                    style={{ aspectRatio: backgroundAspectRatio, transform: [{ rotate: '-180deg' }] }}
                />
            </View>

            <View style={{ aspectRatio: iprossLogoAspectRatio, width: width * 0.7 }}>
                <Image source={images.iprossLogo} style={[styles.image]} />
            </View>
            <View style={{ aspectRatio: rnLogoAspectRatio, width: width * 0.3 }}>
                <RnLogo style={styles.image} />
            </View>
        </View>
    );
};

export default Splash;

const styles = StyleSheet.create({
    container: {
        flex: 1,
        justifyContent: 'space-evenly',
        alignItems: 'center',
    },
    image: {
        ...StyleSheet.absoluteFillObject,
        height: undefined,
        width: undefined,
    },
    backgroundContainer: {
        ...StyleSheet.absoluteFillObject,
        alignItems: 'center',
        justifyContent: 'center',
    },
});
