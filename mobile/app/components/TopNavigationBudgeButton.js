import React from 'react';
import { StyleSheet, View, Text } from 'react-native';
import { BorderlessButton } from 'react-native-gesture-handler';
import PropTypes from 'prop-types';
import { moderateScale } from '../lib/size-normalizer';
import { bold } from '../constants/Fonts';
import * as Colors from '../constants/Colors';

const TopNavigationBudgeButton = ({ image, action, count }) => {
    const getBudge = () => {
        if (!count || count <= 0) return null;
        return (
            <View style={styles.budge}>
                <Text style={styles.budgeText}>{count}</Text>
            </View>
        );
    };

    return (
        <BorderlessButton style={styles.container} onPress={action}>
            {image}
            {getBudge()}
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
    budge: {
        position: 'absolute',
        top: -4,
        right: -4,
        minWidth: moderateScale(14),
        height: moderateScale(14),
        borderRadius: moderateScale(7),
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: 'red',
        borderColor: Colors.white,
        borderWidth: 0.2,
    },
    budgeText: {
        fontFamily: bold,
        color: Colors.white,
        fontSize: moderateScale(10),
    },
});

TopNavigationBudgeButton.propTypes = {
    type: PropTypes.string.isRequired,
};

export default React.memo(TopNavigationBudgeButton);
