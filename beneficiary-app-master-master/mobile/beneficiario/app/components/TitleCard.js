import React, { memo } from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { font_styles } from '../lib/default-styles';
import { moderateScale } from '../lib/size-normalizer';
import * as Colors from '../constants/Colors';

const TitleCard = ({ subtitle, style, title, subtitleStyle }) => {
    return (
        <View style={[styles.statusCard, style]}>
            <View style={styles.statusContainer}>
                <Text style={[font_styles.title_3, { fontSize: moderateScale(16, 0.25) }]}>{title}</Text>
                <Text
                    numberOfLines={1}
                    ellipsizeMode='tail'
                    style={[font_styles.title_3_bold, subtitleStyle]}>
                    {subtitle}
                </Text>
            </View>
        </View>
    );
};

export default memo(TitleCard);

const styles = StyleSheet.create({
    statusCard: {
        backgroundColor: Colors.white,
        elevation: 1,
        borderRadius: moderateScale(6),
        zIndex: 10,
        shadowColor: '#000',
        shadowOffset: {
            width: 0,
            height: 1,
        },
        shadowOpacity: 0.18,
        shadowRadius: 1.0,
    },
    statusContainer: {
        flex: 1,
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'space-around',
        padding: moderateScale(8),
    },
});
