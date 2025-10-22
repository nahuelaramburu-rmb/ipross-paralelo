import React, { forwardRef, useState, useEffect, useRef, memo } from 'react';
import { View, TextInput, StyleSheet, Animated, Text } from 'react-native';
import Icon from 'react-native-vector-icons/Ionicons';
import * as Colors from '../constants/Colors';
import { moderateScale, verticalScale } from '../lib/size-normalizer';
import { regular } from '../constants/Fonts';
import PropTypes from 'prop-types';
import { font_styles } from '../lib/default-styles';

const INPUT_HEIGHT = verticalScale(52, 0.25);
const INPUT_PADDING = moderateScale(8, 0.25);

const ErrorMessage = ({ message }) => {
    const translateY = useRef(new Animated.Value(-16)).current;

    useEffect(() => {
        Animated.timing(translateY, {
            duration: 300,
            toValue: 0,
            useNativeDriver: true,
        }).start();
    }, [translateY]);

    const opacity = translateY.interpolate({
        inputRange: [-16, 0],
        outputRange: [0, 1],
    });

    return (
        <Animated.View style={[styles.errorContainer, { transform: [{ translateY }], opacity }]}>
            <Text style={[font_styles.subtitle, styles.error]}>{message}</Text>
        </Animated.View>
    );
};

const TextField = forwardRef(
    (
        {
            touched,
            leftIcon,
            rightIcon = null,
            value: initialValue,
            onChangeText,
            secureTextEntry = false,
            error = undefined,
            editable = true,
            disabled = false,
            ...props
        },
        ref
    ) => {
        const [value, setValue] = useState(initialValue);
        const [inputHeight, setInputHeight] = useState(INPUT_HEIGHT);

        useEffect(() => {
            setValue(initialValue);
        }, [initialValue]);

        const handleTextChange = (text) => {
            setValue(text);
            if (onChangeText) onChangeText(text);
        };

        const handleContentSizeChange = (event) => {
            if (event && event.nativeEvent && event.nativeEvent.contentSize) {
                if (event.nativeEvent.contentSize.height < INPUT_HEIGHT) {
                    setInputHeight(INPUT_HEIGHT);
                } else {
                    setInputHeight(event.nativeEvent.contentSize.height + INPUT_PADDING * 2);
                }
            }
        };

        let color = Colors.logoTextInactive;
        const iconColor = !disabled ? Colors.logoTextInactive : Colors.logoTextInactive;

        if (disabled) {
            color = Colors.logoTextInactive;
        } else {
            if (touched && error) {
                color = 'rgb(213, 0, 0)';
            }
        }

        return (
            <View>
                <View style={[styles.container, { borderColor: color, height: inputHeight }]}>
                    <View style={styles.iconContainer}>
                        {typeof leftIcon === 'string' ? (
                            <Icon name={leftIcon} size={moderateScale(20)} color={iconColor} />
                        ) : (
                            leftIcon
                        )}
                    </View>
                    <View style={styles.textInputContainer}>
                        <TextInput
                            ref={ref}
                            value={value}
                            editable={!disabled && editable}
                            onChangeText={handleTextChange}
                            onContentSizeChange={handleContentSizeChange}
                            underlineColorAndroid='transparent'
                            style={[styles.textInput, disabled && styles.disabledTextInput]}
                            secureTextEntry={secureTextEntry}
                            {...props}
                        />
                    </View>
                    <View style={styles.iconContainer}>
                        {typeof rightIcon === 'string' ? (
                            <Icon name={rightIcon} size={moderateScale(20)} color={Colors.logoTextInactive} />
                        ) : (
                            rightIcon
                        )}
                    </View>
                    <View style={[styles.extraBorder, { backgroundColor: color }]} />
                </View>
                {error && touched && <ErrorMessage message={error} />}
            </View>
        );
    }
);

const styles = StyleSheet.create({
    container: {
        minHeight: INPUT_HEIGHT,
        borderWidth: StyleSheet.hairlineWidth,
        flexDirection: 'row',
        borderRadius: moderateScale(6),
        padding: INPUT_PADDING,
        alignItems: 'center',
        justifyContent: 'center',
    },
    iconContainer: {
        padding: moderateScale(8),
        alignItems: 'center',
        justifyContent: 'center',
    },
    textInputContainer: {
        flex: 1,
    },
    textInput: {
        justifyContent: 'center',
        fontFamily: regular,
        color: Colors.primaryText,
        fontSize: moderateScale(16),
        padding: 0,
        margin: 0,
        borderBottomWidth: 0,
    },
    disabledTextInput: {
        color: Colors.darkDividerLine,
    },
    extraBorder: {
        position: 'absolute',
        bottom: 0,
        width: '99%',
        height: 1,
        margin: 'auto',
    },
    errorContainer: {
        marginTop: verticalScale(4),
        minHeight: verticalScale(16),
        paddingLeft: moderateScale(4),
    },
    error: {
        color: 'rgb(213, 0, 0)',
    },
});

TextField.propTypes = {
    touched: PropTypes.bool,
    leftIcon: PropTypes.oneOfType([PropTypes.string, PropTypes.element]),
    rightIcon: PropTypes.oneOfType([PropTypes.string, PropTypes.element]),
    value: PropTypes.string,
    onChangeText: PropTypes.func,
    secureTextEntry: PropTypes.bool,
    error: PropTypes.string,
    editable: PropTypes.bool,
    disabled: PropTypes.bool,
};

ErrorMessage.propTypes = {
    message: PropTypes.string,
};

export default memo(TextField);
