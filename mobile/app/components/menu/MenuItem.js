import React from 'react';
import { Platform, StyleSheet, Text, TouchableHighlight, TouchableNativeFeedback, View } from 'react-native';
import { moderateScale } from '../../lib/size-normalizer';
import PropTypes from 'prop-types';

const Touchable =
    Platform.OS === 'android' && Platform.Version >= 21 ? TouchableNativeFeedback : TouchableHighlight;

const MenuItem = ({
    children,
    disabled,
    disabledTextColor,
    ellipsizeMode,
    onPress,
    style,
    textStyle,
    ...props
}) => {
    const touchableProps =
        Platform.OS === 'android' && Platform.Version >= 21
            ? { background: TouchableNativeFeedback.SelectableBackground() }
            : {};

    return (
        <Touchable disabled={disabled} onPress={onPress} {...touchableProps} {...props}>
            <View style={[styles.container, style]}>
                <Text
                    ellipsizeMode={ellipsizeMode}
                    numberOfLines={1}
                    style={[styles.title, disabled && { color: disabledTextColor }, textStyle]}>
                    {children}
                </Text>
            </View>
        </Touchable>
    );
};

MenuItem.defaultProps = {
    disabled: false,
    disabledTextColor: '#bdbdbd',
    ellipsizeMode: Platform.OS === 'ios' ? 'clip' : 'tail',
    underlayColor: '#e0e0e0',
};

const styles = StyleSheet.create({
    container: {
        height: moderateScale(48),
        justifyContent: 'center',
        maxWidth: 248,
        minWidth: 124,
    },
    title: {
        fontSize: 14,
        fontWeight: '400',
        paddingHorizontal: moderateScale(16),
        textAlign: 'left',
    },
});

MenuItem.propTypes = {
    children: PropTypes.node,
    disabled: PropTypes.bool,
    disabledTextColor: PropTypes.string,
    ellipsizeMode: PropTypes.string,
    onPress: PropTypes.func,
    style: PropTypes.object,
    textStyle: PropTypes.object,
};

export default MenuItem;
