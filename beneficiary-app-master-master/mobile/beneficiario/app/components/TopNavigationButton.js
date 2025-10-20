import React from 'react';
import { StyleSheet, View } from 'react-native';
import PropTypes from 'prop-types';
import { BorderlessButton } from 'react-native-gesture-handler';
import { moderateScale } from '../lib/size-normalizer';

const TopNavigationButton = ({ image, action, testID, style }) => {
    return (
        <BorderlessButton style={[styles.container, style]} onPress={action}>
            <View testID={testID}>{image}</View>
        </BorderlessButton>
    );
};

const styles = StyleSheet.create({
    container: {
        height: moderateScale(24),
        width: moderateScale(24),
        alignItems: 'center',
        justifyContent: 'center',
    },
});

TopNavigationButton.propTypes = {
    type: PropTypes.string,
};

export default React.memo(TopNavigationButton);
