import { StyleSheet } from 'react-native';
import { moderateScale } from './size-normalizer';
import * as Colors from '../constants/Colors';
import { semibold, regular, bold } from '../constants/Fonts';

export const font_styles = StyleSheet.create({
    title_1: {
        fontFamily: semibold,
        fontSize: moderateScale(21, 1),
        color: Colors.logoText,
    },
    title_1_bold: {
        fontFamily: bold,
        fontSize: moderateScale(21, 1),
        color: Colors.logoText,
    },
    title_2: {
        fontFamily: semibold,
        fontSize: moderateScale(20, 1),
        color: Colors.logoText,
    },
    headline: {
        fontFamily: semibold,
        fontSize: moderateScale(20, 0.5),
        color: Colors.logoText,
    },
    title_2_header: {
        fontFamily: semibold,
        fontSize: moderateScale(18, 1),
        color: Colors.logoText,
    },
    title_3: {
        color: Colors.primaryText,
        fontFamily: regular,
        fontSize: moderateScale(17, 0.75),
    },
    title_3_bold: {
        fontFamily: bold,
        fontSize: moderateScale(17, 0.75),
        color: Colors.primaryText,
    },
    primary_text: {
        fontSize: moderateScale(16),
        color: Colors.primaryText,
        fontFamily: regular,
    },
    primary_text_bold: {
        fontSize: moderateScale(16),
        color: Colors.primaryText,
        fontFamily: bold,
    },
    secondary_text: {
        fontSize: moderateScale(14),
        color: Colors.secondaryText,
        fontFamily: regular,
    },
    secondary_text_bold: {
        fontSize: moderateScale(14),
        color: Colors.secondaryText,
        fontFamily: bold,
    },
    subtitle: {
        fontFamily: bold,
        fontSize: moderateScale(12, 0.75),
        color: Colors.secondaryText,
    },
});
