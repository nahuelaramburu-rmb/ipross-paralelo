import PropTypes from 'prop-types';
import React, { Component } from 'react';
import { StyleSheet, Text, View, Dimensions } from 'react-native';

import { codeSalt } from '../../configs/api';
import * as Colors from '../../constants/Colors';

import Hashids from 'hashids';

import strings from '../../constants/Strings';

import { font_styles } from '../../lib/default-styles';
import { generator, strtohex } from '../../lib/otp';
import * as Progress from 'react-native-progress';

const { width } = Dimensions.get('window');
import base32 from 'base-32';
import { moderateScale, verticalScale } from '../../lib/size-normalizer';
import { bold } from '../../constants/Fonts';
import { SafeAreaView } from 'react-native-safe-area-context';

const OTP_DURATION = 120;

export default class TokenCodeGeneratorScreen extends Component {
    static propTypes = {
        navigation: PropTypes.shape({
            state: PropTypes.shape({
                params: PropTypes.shape({
                    idNumber: PropTypes.number.isRequired,
                }),
            }),
        }).isRequired,
    };

    constructor(props) {
        super(props);
        this.state = {
            otp: {
                value: null,
                secondsBeforeExpiration: 0,
                countdown: 0,
            },
        };

        this.interval = null;
        this.hashids = new Hashids(codeSalt['TOKEN_KEY']);
        this.otpSecret = base32
            .encode(
                strtohex(
                    this.hashids.encode(props.route.params.idNumber, props.route.params.idNumber)
                ).toUpperCase()
            )
            .replace(new RegExp('=', 'g'), '');
        this._printValues = this._printValues.bind(this);
        this._getCountdownPercentage = this._getCountdownPercentage.bind(this);
    }

    componentDidMount() {
        const { otp, secondsBeforeExpiration } = generator({
            name: 'otp generator',
            secret: this.otpSecret,
            timeSlice: OTP_DURATION,
        });
        this.setState({
            ...this.state,
            otp: { value: otp, secondsBeforeExpiration: secondsBeforeExpiration },
        });
        this._printValues();
    }

    _printValues() {
        let remaining = this.state.otp.secondsBeforeExpiration;
        this.interval = setInterval(() => {
            if (remaining > 0) {
                remaining = remaining - 1;
                this.setState({
                    ...this.state,
                    otp: { ...this.state.otp, secondsBeforeExpiration: remaining },
                });
            } else {
                const obj = generator({
                    name: 'otp generator',
                    secret: this.otpSecret,
                    timeSlice: OTP_DURATION,
                });
                this.setState({
                    ...this.state,
                    otp: { value: obj.otp, secondsBeforeExpiration: obj.secondsBeforeExpiration },
                });
                remaining = obj.secondsBeforeExpiration;
            }
        }, 1000);
    }

    componentWillUnmount() {
        if (this.interval !== null) clearInterval(this.interval);
    }

    _getCountdownPercentage() {
        const completedPercentage = this.state.otp.secondsBeforeExpiration / OTP_DURATION;
        return completedPercentage;
    }

    render() {
        let mainView = null;
        mainView = (
            <>
                <View style={styles.messageContainer}>
                    <Text style={[font_styles.title_3, styles.centerText]}>
                        {strings.codeGenerator.token_message}
                    </Text>
                    <Text style={[font_styles.primary_text_bold, styles.centerText, { color: Colors.error }]}>
                        {strings.codeGenerator.qr_hour_message}
                    </Text>
                </View>
                <View style={styles.otpContainer}>
                    <Text style={styles.otpText}>{this.state.otp.value}</Text>
                </View>
                <View style={styles.expirationContainer}>
                    <Text
                        style={[
                            font_styles.title_3,
                            { color: Colors.secondaryText, paddingVertical: verticalScale(18) },
                        ]}>
                        {strings.codeGenerator.expiration_in}
                        <Text style={{ fontFamily: bold }}>
                            {' '}
                            {this.state.otp.secondsBeforeExpiration}
                        </Text>{' '}
                        segundos.
                    </Text>
                    <Progress.Bar
                        progress={this._getCountdownPercentage()}
                        width={moderateScale(250)}
                        color={Colors.accent}
                        borderColor={Colors.darkDividerLine}
                    />
                </View>
            </>
        );

        return (
            <SafeAreaView style={styles.safeArea}>
                <View style={styles.container}>{mainView}</View>
            </SafeAreaView>
        );
    }
}

const styles = StyleSheet.create({
    safeArea: {
        flex: 1,
    },
    container: {
        flex: 1,
        justifyContent: 'space-around',
        alignItems: 'center',
        backgroundColor: Colors.appBackground,
    },
    messageContainer: {
        flex: 0.3,
        alignItems: 'center',
        justifyContent: 'space-around',
        paddingHorizontal: verticalScale(24),
    },
    centerText: {
        textAlign: 'center',
    },
    otpContainer: {
        flex: 0.4,
        alignItems: 'center',
        justifyContent: 'center',
        width: width,
    },
    otpText: {
        fontSize: moderateScale(50, 1),
        fontFamily: bold,
        textAlign: 'center',
        letterSpacing: 8,
        color: Colors.accent,
    },
    expirationContainer: {
        flex: 0.3,
        alignItems: 'center',
        justifyContent: 'center',
        width: width,
        flexDirection: 'column',
    },
});
