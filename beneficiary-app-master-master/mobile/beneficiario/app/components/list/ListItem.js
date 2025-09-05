import React, { memo } from 'react';
import { StyleSheet, View } from 'react-native';
import { RectButton } from 'react-native-gesture-handler';
import Icon from 'react-native-vector-icons/Ionicons';
import { moderateScale } from '../../lib/size-normalizer';
import * as Colors from '../../constants/Colors';
import PropTypes from 'prop-types';

const ListItem = ({ onPress, leftIcon, rightIcon, children }) => {
    const leftIconContent =
        typeof leftIcon === 'string' ? (
            <Icon name={leftIcon} size={moderateScale(20)} color={Colors.accent} />
        ) : (
            leftIcon
        );

    const rightIconContent =
        typeof rightIcon === 'string' ? (
            <Icon
                name={rightIcon}
                size={moderateScale(24)}
                color={Colors.primaryText}
                style={styles.iconOpacity}
            />
        ) : (
            rightIcon
        );

    return (
        <RectButton onPress={onPress} style={styles.rectButton}>
            <View style={styles.container}>
                {leftIconContent}
                <View style={styles.mainContent}>{children}</View>
                {rightIconContent}
            </View>
        </RectButton>
    );
};

ListItem.propTypes = {
    onPress: PropTypes.func,
    leftIcon: PropTypes.oneOfType([PropTypes.element, PropTypes.string]),
    rightIcon: PropTypes.oneOfType([PropTypes.element, PropTypes.string]),
    children: PropTypes.node,
};

const styles = StyleSheet.create({
    rectButton: {
        backgroundColor: '#ffffff00',
        flex: 1,
    },
    container: {
        borderBottomWidth: 0.5,
        borderBottomColor: Colors.lightDividerLine,
        justifyContent: 'space-between',
        alignItems: 'center',
        width: '100%',
        flexDirection: 'row',
        flexGrow: 1,
        flexShrink: 0,
        paddingHorizontal: moderateScale(18),
        paddingVertical: moderateScale(12),
    },
    mainContent: {
        alignItems: 'flex-start',
        flex: 1,
        flexDirection: 'column',
        marginLeft: moderateScale(18),
    },
    iconOpacity: {
        opacity: 0.4,
    },
});

export default memo(ListItem);
