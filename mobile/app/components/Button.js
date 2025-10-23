import React, { cloneElement, memo } from 'react';
import { View, Text, StyleSheet, ActivityIndicator } from 'react-native';
import { BorderlessButton } from 'react-native-gesture-handler';
import { bold } from '../constants/Fonts';
import * as Colors from '../constants/Colors';
import { moderateScale } from '../lib/size-normalizer';
import PropTypes from 'prop-types';

// type -> SOLID, GHOST, OUTLINE

const styles = StyleSheet.create({
    borderlessButton: {
        flex: 1,
        backgroundColor: '#ffffff00',
    },
    borderlessButtonContent: {
        flex: 1,
        paddingHorizontal: moderateScale(36),
        justifyContent: 'center',
        alignItems: 'center',
        flexDirection: 'row',
    },
    buttonBase: {
        height: 50,
        borderRadius: 25,
    },
    buttonBlock: {
        width: '100%',
    },
    buttonSolid: {
        backgroundColor: Colors.primary,
    },
    buttonSolidDisabled: {
        backgroundColor: Colors.disabledBackgroundButton,
    },
    buttonOutline: {
        backgroundColor: '#ffffff',
        borderWidth: 1,
        borderStyle: 'solid',
        borderColor: Colors.primary,
    },
    buttonOutlineDisabled: {
        backgroundColor: '#ffffff00',
        borderWidth: 1,
        borderStyle: 'solid',
        borderColor: Colors.disabledBackgroundButton,
    },
    buttonGhost: {
        backgroundColor: '#ffffff00',
    },
    textBase: {
        fontFamily: bold,
        fontSize: 18,
    },
    textSolid: {
        color: Colors.primaryText,
    },
    textSolidDisabled: {
        color: Colors.disabledTextButton,
    },
    textOutline: {
        color: Colors.primaryText,
    },
    textOutlineDisabled: {
        color: Colors.disabledTextButton,
    },
    rightIcon: {
        paddingLeft: moderateScale(16),
    },
    leftIcon: {
        paddingRight: moderateScale(16),
    },
    shadow: {
        shadowColor: '#000',
        shadowOffset: {
            width: 0,
            height: 1,
        },
        shadowOpacity: 0.2,
        shadowRadius: 1.41,
        elevation: 2,
    },
});

const buttonTypes = {
    solid: {
        buttonStyles: styles.buttonSolid,
        textStyles: styles.textSolid,
    },
    outline: {
        buttonStyles: styles.buttonOutline,
        textStyles: styles.textOutline,
    },
    ghost: {
        buttonStyles: styles.buttonGhost,
        textStyles: styles.textOutline,
    },
};

const loaderColors = {
    solid: Colors.primaryText,
    outline: Colors.primaryText,
    ghost: Colors.primaryText,
};

const disabledStyles = {
    solid: {
        buttonStyles: styles.buttonSolidDisabled,
        textStyles: styles.textSolidDisabled,
    },
    outline: {
        buttonStyles: styles.buttonOutlineDisabled,
        textStyles: styles.textOutlineDisabled,
    },
    ghost: {
        buttonStyles: styles.buttonGhost,
        textStyles: styles.textOutlineDisabled,
    },
};

const Button = ({
    title,
    disabled = false,
    loading = false,
    type = 'solid',
    block = false,
    onPress,
    leftIcon = null,
    rightIcon = null,
    raised = false,
    style,
    Comp = BorderlessButton,
}) => {
    const buttonStyles = [styles.buttonBase];
    const textStyles = [styles.textBase];
    const loadingColor = loaderColors[type];

    if (block) buttonStyles.push(styles.buttonBlock);
    if (raised && !disabled) buttonStyles.push(styles.shadow);

    if (!disabled) {
        buttonStyles.push(buttonTypes[type].buttonStyles || buttonTypes['solid'].buttonStyles);
        textStyles.push(buttonTypes[type].textStyles || buttonTypes['solid'].textStyles);
    } else {
        buttonStyles.push(disabledStyles[type].buttonStyles || disabledStyles['solid'].buttonStyles);
        textStyles.push(disabledStyles[type].textStyles || disabledStyles['solid'].textStyles);
    }

    return (
        <View style={[buttonStyles, style]}>
            <Comp style={styles.borderlessButton} onPress={!loading ? onPress : null} enabled={!disabled}>
                <View style={styles.borderlessButtonContent}>
                    {loading ? (
                        <ActivityIndicator size='small' color={loadingColor} />
                    ) : (
                        <>
                            {leftIcon && cloneElement(leftIcon, { style: styles.leftIcon })}
                            <Text style={textStyles}>{title}</Text>
                            {rightIcon && cloneElement(rightIcon, { style: styles.rightIcon })}
                        </>
                    )}
                </View>
            </Comp>
        </View>
    );
};

Button.propTypes = {
    title: PropTypes.string,
    disabled: PropTypes.bool,
    loading: PropTypes.bool,
    type: PropTypes.oneOf(['solid', 'outline', 'ghost', undefined]),
    block: PropTypes.bool,
    onPress: PropTypes.oneOfType([PropTypes.func, PropTypes.bool]),
    leftIcon: PropTypes.element,
    rightIcon: PropTypes.element,
    raised: PropTypes.bool,
    style: PropTypes.object,
    Comp: PropTypes.element,
};

export default memo(Button);
