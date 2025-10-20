import PropTypes from 'prop-types';
import React, { Component } from 'react';
import { StyleSheet, Text, View, ActivityIndicator, Dimensions, NativeModules } from 'react-native';
import { codeSalt } from '../../configs/api';
import * as Colors from '../../constants/Colors';
import QRCode from 'react-native-qrcode-svg';
import strings from '../../constants/Strings';
import { font_styles } from '../../lib/default-styles';
import { verticalScale, moderateScale } from '../../lib/size-normalizer';
import moment from 'moment';
import { uuidv4 } from '../../lib/utils';
import { SafeAreaView } from 'react-native-safe-area-context';
import { toHex } from '../../lib/utils';

var AesCrypto = NativeModules.Aes;

const { width, height } = Dimensions.get('window');

export default class QrCodeGeneratorScreen extends Component {
    static propTypes = {
        navigation: PropTypes.shape({
            state: PropTypes.shape({
                params: PropTypes.shape({
                    idNumber: PropTypes.number.isRequired,
                    idType: PropTypes.number.isRequired,
                }),
            }),
        }).isRequired,
    };

    constructor(props) {
        super(props);
        this.state = {
            generatingCode: false,
            idQR: null,
        };
        this._getScreenSize = this._getScreenSize.bind(this);
        this._generateCode = this._generateCode.bind(this);
    }

    componentDidMount() {
        this._generateCode();
    }

    async _generateCode() {
        this.setState({ generatingCode: true });
        const { route } = this.props;
        const toEncode = {
            beneficiary: {
                idNumber: route.params.idNumber,
                idType: {
                    id: route.params.idType,
                },
            },
            encryptedQrKey: uuidv4(),
            timestamp: Date.now(),
            type: 'beneficiary',
        };

        AesCrypto.encrypt(JSON.stringify(toEncode), toHex(codeSalt['QR_SECRET']), toHex(codeSalt['QR_IV']))
            .then((cipher) => {
                console.log(cipher);
                setTimeout(() => {
                    this.setState({ idQR: cipher, generatingCode: false });
                }, 2000);
            })
            .catch((err) => {
                console.log(err);
                setTimeout(() => {
                    this.setState({ generatingCode: false });
                }, 2000);
            });
    }

    _getScreenSize() {
        if (height < 640) return 185;
        if (height >= 640 && height < 900) return 250;
        if (height >= 900) return 320;
    }

    render() {
        let mainView = null;
        if (this.state.generatingCode) {
            mainView = (
                <>
                    <ActivityIndicator size='large' color={Colors.primaryText} />
                    <Text
                        style={[
                            font_styles.secondary_text,
                            { color: Colors.secondaryText, marginTop: verticalScale(6) },
                        ]}>
                        {strings.codeGenerator.generating_qr_code}
                    </Text>
                </>
            );
        } else {
            mainView = (
                <>
                    <View style={styles.messagesContainer}>
                        <Text style={[font_styles.title_3, styles.centerText]}>
                            {strings.codeGenerator.qr_message}
                        </Text>
                        <Text
                            style={[
                                font_styles.primary_text_bold,
                                styles.centerText,
                                { color: Colors.error },
                            ]}>
                            {strings.codeGenerator.qr_hour_message}
                        </Text>
                    </View>
                    <View style={styles.mainViewContainer}>
                        <View style={styles.qrContainer}>
                            {this.state.idQR !== null ? (
                                <QRCode
                                    value={this.state.idQR}
                                    size={this._getScreenSize()}
                                    color={Colors.primaryText}
                                    backgroundColor={Colors.appBackground}
                                />
                            ) : null}
                        </View>
                        <View style={styles.expirationContainer}>
                            <Text
                                style={[
                                    font_styles.title_3_bold,
                                    styles.centerText,
                                    { color: Colors.secondaryText },
                                ]}>
                                {strings.codeGenerator.expiration}
                            </Text>
                            <Text style={[font_styles.title_3, { color: Colors.secondaryText }]}>
                                {`: Hoy (${moment().format('D/M/YYYY')}) a las ${moment()
                                    .add(600, 'seconds')
                                    .format('HH:mm')}hs.`}
                            </Text>
                        </View>
                    </View>
                </>
            );
        }
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
        justifyContent: 'center',
        alignItems: 'center',
        backgroundColor: Colors.appBackground,
        flexDirection: 'column',
    },
    messagesContainer: {
        flex: 0.3,
        alignItems: 'center',
        justifyContent: 'space-around',
        paddingHorizontal: verticalScale(24),
    },
    mainViewContainer: {
        flex: 0.7,
        alignItems: 'center',
        justifyContent: 'center',
        width: width,
    },
    expirationContainer: {
        flex: 0.4,
        alignItems: 'center',
        justifyContent: 'center',
        flexDirection: 'row',
        width: width,
        paddingHorizontal: moderateScale(6),
    },
    qrContainer: {
        margin: '2%',
        alignItems: 'center',
        justifyContent: 'center',
        flex: 1,
        width: width,
    },
    centerText: {
        textAlign: 'center',
    },
});
