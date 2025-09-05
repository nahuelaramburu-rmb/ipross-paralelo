import React from 'react';
import { View, Text, StyleSheet, TouchableNativeFeedback, Platform, TouchableHighlight } from 'react-native';
import { verticalScale, moderateScale } from '../../lib/size-normalizer';
import { font_styles } from '../../lib/default-styles';
import PropTypes from 'prop-types';

export const OPTION_HEIGHT = verticalScale(36);

const Touchable =
    Platform.OS === 'android' && Platform.Version >= 21 ? TouchableNativeFeedback : TouchableHighlight;

const Option = ({ id, children, onItemSelect, index, selected }) => {
    const selectItem = () => {
        onItemSelect({ id: id, position: index, label: children });
    };

    const textColor = selected ? 'rgba(0, 0, 0, .87)' : 'rgba(0, 0, 0, .54)';

    const touchableProps =
        Platform.OS === 'android' && Platform.Version >= 21
            ? { background: TouchableNativeFeedback.SelectableBackground() }
            : {};

    return (
        <Touchable onPress={selectItem} {...touchableProps}>
            <View style={styles.container}>
                <Text style={[font_styles.primary_text, { color: textColor }]} numberOfLines={1}>
                    {children}
                </Text>
            </View>
        </Touchable>
    );
};

Option.propTypes = {
    id: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
    children: PropTypes.node,
    onItemSelect: PropTypes.func,
    index: PropTypes.number,
    selected: PropTypes.bool,
};

const styles = StyleSheet.create({
    rectButton: {
        backgroundColor: '#ffffff00',
    },
    container: {
        height: OPTION_HEIGHT,
        justifyContent: 'center',
        paddingLeft: moderateScale(8),
    },
});

export default Option;
