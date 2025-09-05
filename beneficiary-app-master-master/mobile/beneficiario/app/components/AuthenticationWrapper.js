import React, { useRef } from 'react';
import { StyleSheet, View, Image, Dimensions, Platform } from 'react-native';
import images from '../configs/images';
import { moderateScale, verticalScale } from '../lib/size-normalizer';
import ExtraDimensions from 'react-native-extra-dimensions-android';
import { KeyboardAwareScrollView } from 'react-native-keyboard-aware-scroll-view';
import PropTypes from 'prop-types';
import * as Colors from '../constants/Colors';
import BackgroundSvg, { originalWidth, originalHeight } from '../components/BackgroundSvg';

const { height: iosHeight, width } = Dimensions.get('window');
const height =
    Platform.OS === 'ios'
        ? iosHeight
        : ExtraDimensions.getRealWindowHeight() - ExtraDimensions.getSoftMenuBarHeight();

const backgroundAspectRatio = originalWidth / originalHeight;

const AuthenticationWrapper = ({ children, ...props }) => {
    const viewRef = useRef(null);

    return (
        <KeyboardAwareScrollView
            ref={viewRef}
            scrollEnabled={false}
            contentContainerStyle={styles.keyboardAwareScrollView}
            onKeyboardDidHide={() => viewRef.current?.scrollToPosition(0, 0)}
            {...props}>
            <View style={styles.backgroundContainer}>
                <BackgroundSvg
                    width={width}
                    height={'100%'}
                    style={{ aspectRatio: backgroundAspectRatio, transform: [{ rotate: '-180deg' }] }}
                />
            </View>

            <View style={styles.imageContainer}>
                <View style={styles.topImage}>
                    <Image source={images.iprossLogo} style={styles.image} />
                </View>
            </View>

            <View style={styles.contentContainer}>{children}</View>
        </KeyboardAwareScrollView>
    );
};

AuthenticationWrapper.propTypes = {
    children: PropTypes.element,
};

export default AuthenticationWrapper;

const styles = StyleSheet.create({
    keyboardAwareScrollView: {
        minHeight: height,
        flexGrow: 1,
        backgroundColor: Colors.white,
    },
    imageContainer: {
        height: height * 0.4,
        alignItems: 'center',
        justifyContent: 'flex-end',
    },
    contentContainer: {
        flex: 1,
        paddingHorizontal: moderateScale(24),
    },
    topImage: {
        width: width * 0.8,
        height: verticalScale(200),
    },
    image: {
        ...StyleSheet.absoluteFillObject,
        height: undefined,
        width: undefined,
        resizeMode: 'contain',
    },
    backgroundContainer: {
        ...StyleSheet.absoluteFillObject,
        alignItems: 'center',
        justifyContent: 'center',
    },
});
