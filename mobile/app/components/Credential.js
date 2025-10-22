import PropTypes from 'prop-types';
import React, { memo } from 'react';
import { StyleSheet, Text, View, Dimensions, Image } from 'react-native';
import * as Colors from '../constants/Colors';
import images from '../configs/images';
import { font_styles } from '../lib/default-styles';
import { moderateScale, verticalScale } from '../lib/size-normalizer';
import BackgroundSvg from './BackgroundSvg';
import RnLogo from '../images/rn-logo.svg';
import CredentialUser from '../images/credential_user.svg';

const { width, height } = Dimensions.get('window');

const iprossLogoAspectRatio = 3126 / 1426;
const rnLogoAspectRatio = 346.5 / 274.9;

const Credential = ({ style, beneficiaryInfo }) => {
    return (
        <View style={style}>
            <View style={[styles.container]}>
                <View style={styles.credentialContainer}>
                    <View style={styles.credentialOverlay}>
                        <BackgroundSvg
                            width={width}
                            height={height}
                            style={{ transform: [{ rotate: '-180deg' }] }}
                        />
                    </View>
                    <View style={styles.credentialContentContainer}>
                        <View style={styles.credentialHeader}>
                            <View style={styles.iprossLogoContainer}>
                                <Image
                                    source={images.iprossLogo}
                                    style={[styles.iprossLogo, { aspectRatio: iprossLogoAspectRatio }]}
                                />
                            </View>
                            <View style={[{ aspectRatio: rnLogoAspectRatio }, styles.center]}>
                                <RnLogo style={styles.rnLogo} />
                            </View>
                        </View>
                        <View style={styles.credentialContent}>
                            <View style={styles.userContainer}>
                                <View style={styles.userCircle}>
                                    <View style={styles.userInnerCircle}>
                                        <CredentialUser fill={Colors.light3} />
                                    </View>
                                </View>
                            </View>
                            <View style={styles.mainInformation}>
                                <View style={styles.mainInformationContainer}>
                                    <Text style={[font_styles.title_3_bold, styles.text]}>
                                        {`${beneficiaryInfo.lastName}, ${beneficiaryInfo.name}`}
                                    </Text>
                                    <Text
                                        style={[
                                            font_styles.secondary_text,
                                            styles.text,
                                            { color: Colors.logoText },
                                        ]}>
                                        {beneficiaryInfo.beneficiaryCode}
                                    </Text>
                                    <Text
                                        style={[
                                            font_styles.secondary_text,
                                            styles.text,
                                            { color: Colors.logoText },
                                        ]}>
                                        {`${beneficiaryInfo.idType.alias}  ${beneficiaryInfo.idNumber}`}
                                    </Text>
                                </View>
                            </View>
                        </View>
                    </View>
                </View>
            </View>
        </View>
    );
};

Credential.propTypes = {
    style: PropTypes.object,
    beneficiaryInfo: PropTypes.shape({
        gender: PropTypes.string.isRequired,
        name: PropTypes.string.isRequired,
        lastName: PropTypes.string.isRequired,
        idType: PropTypes.object.isRequired,
        idNumber: PropTypes.number.isRequired,
        beneficiaryCode: PropTypes.string.isRequired,
    }).isRequired,
};

const styles = StyleSheet.create({
    container: {
        flex: 1,
        paddingVertical: verticalScale(8),
    },
    credentialContainer: {
        width: width - moderateScale(50),
        height: '100%',
        shadowColor: '#000',
        shadowOffset: {
            width: 0,
            height: 2,
        },
        shadowOpacity: 0.23,
        shadowRadius: 2.62,
        elevation: 4,
        borderRadius: 18,
    },
    credentialOverlay: {
        ...StyleSheet.absoluteFillObject,
        overflow: 'hidden',
        justifyContent: 'center',
        borderRadius: 18,
    },
    credentialContentContainer: {
        flex: 1,
        alignItems: 'center',
        justifyContent: 'center',
        flexDirection: 'column',
    },
    credentialHeader: {
        flex: 1,
        width: '100%',
        justifyContent: 'space-between',
        flexDirection: 'row',
        paddingHorizontal: moderateScale(12),
    },
    credentialContent: {
        flex: 2,
        width: '100%',
        flexDirection: 'row',
    },
    userContainer: {
        flex: 0.4,
        alignItems: 'center',
        justifyContent: 'center',
    },
    userCircle: {
        width: moderateScale(74, 1),
        height: moderateScale(74, 1),
        borderRadius: moderateScale(37, 1),
        overflow: 'hidden',
        backgroundColor: 'white',
        justifyContent: 'center',
        alignItems: 'center',
    },
    userInnerCircle: {
        width: moderateScale(68, 1),
        height: moderateScale(68, 1),
        borderRadius: moderateScale(34, 1),
        borderWidth: 3,
        borderColor: Colors.accentTertiary,
    },
    text: {
        paddingVertical: verticalScale(2),
    },
    iprossLogoContainer: {
        justifyContent: 'center',
        marginLeft: moderateScale(10),
    },
    iprossLogo: {
        height: '90%',
    },
    rnLogo: {
        height: '70%',
    },
    center: {
        justifyContent: 'center',
    },
    mainInformation: {
        flex: 0.7,
        justifyContent: 'center',
        alignItems: 'flex-start',
    },
    mainInformationContainer: {
        flexDirection: 'column',
        justifyContent: 'center',
    },
});

export default memo(Credential);
