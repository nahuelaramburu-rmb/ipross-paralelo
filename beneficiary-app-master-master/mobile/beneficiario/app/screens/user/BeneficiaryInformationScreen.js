import PropTypes from 'prop-types';
import React, { PureComponent } from 'react';
import { connect } from 'react-redux';
import { StyleSheet, View, Dimensions, ScrollView, Text, ActivityIndicator, Platform } from 'react-native';
import Icon from 'react-native-vector-icons/Ionicons';
import Modal from 'react-native-modal';
import * as Colors from '../../constants/Colors';
import strings from '../../constants/Strings';
import { verticalScale, moderateScale } from '../../lib/size-normalizer';
import { font_styles } from '../../lib/default-styles';
import ImageCard from '../../components/ImageCard';
import { bold } from '../../constants/Fonts';
import moment from 'moment';
import 'moment/locale/es';

import Credential from '../../components/Credential';
import ActionButton from 'react-native-action-button';
import DecoratedCard from '../../components/DecoratedCard';
import SimpleCard from '../../components/SimpleCard';
import { SafeAreaView } from 'react-native-safe-area-context';
import { NAV_BAR_HEIGHT_IOS, NAV_BAR_HEIGHT_ANDROID } from '../../components/header/HeaderWrapper';

const { width, height } = Dimensions.get('window');

const PlanModalContent = ({ plans }) => {
    const renderPlanItem = ({ item }) => {
        return (
            <View key={item.id} style={styles.modalPlanItemContainer}>
                <Text numberOfLines={1} ellipsizeMode='tail' style={styles.modalPlanTitle}>
                    <Text style={[font_styles.primary_text]}>{item.name}</Text>
                    <Text style={[font_styles.primary_text_bold]}> - {item.insurancePlanType.name}</Text>
                </Text>

                <Text style={[font_styles.primary_text, { marginLeft: '1%' }]}>
                    {item.expirationDate
                        ? `Hasta ${moment(item.expirationDate).format('DD/MM/YYYY')}`
                        : 'Sin Vencimiento'}
                </Text>
            </View>
        );
    };

    return (
        <View style={styles.modalContainer}>
            <View style={styles.modalTitleContainer}>
                <Text style={[font_styles.title_3, { textAlign: 'center' }]}>
                    {strings.beneficiaryInformation.beneficiary_plans}
                </Text>
            </View>

            {plans.map((item) => renderPlanItem({ item }))}
        </View>
    );
};

class BeneficiaryInformationScreen extends PureComponent {
    static propTypes = {
        beneficiary: PropTypes.object,
        update_data_loading: PropTypes.bool.isRequired,
        selectedUserCharge: PropTypes.number,
    };

    constructor(props) {
        super(props);
        this.state = {
            isPlanModalVisible: false,
        };

        this._generateCode = this._generateCode.bind(this);
        this._renderButtonImage = this._renderButtonImage.bind(this);
        this._getCapitalizedText = this._getCapitalizedText.bind(this);
        this._handlePlanCardClick = this._handlePlanCardClick.bind(this);
    }

    _generateCode(type) {
        const { navigation, beneficiary } = this.props;
        const screen = type === 'qr' ? 'QrToken' : 'OTPToken';
        navigation.navigate(screen, {
            idNumber: beneficiary.idNumber,
            idType: beneficiary.idType.id,
        });
    }

    _renderButtonImage(enabled) {
        const color = enabled ? Colors.primaryText : Colors.disabledTextButton;
        return <Icon name='ios-add' size={moderateScale(24)} color={color} />;
    }

    _getCapitalizedText(text) {
        if (typeof text !== 'undefined') {
            const textLowerCase = text.toLowerCase();
            return textLowerCase.charAt(0).toUpperCase() + textLowerCase.slice(1);
        }
        return '';
    }

    _handlePlanCardClick() {
        this.setState({ isPlanModalVisible: true });
    }

    render() {
        if (!this.props.beneficiary) return null;
        const { isPlanModalVisible } = this.state;
        const { selectedUserCharge, beneficiary, update_data_loading } = this.props;
        const enabledStatusName = 'con cobertura';
        const beneficiaryStatus = beneficiary.status.name;
        const statusColor =
            beneficiaryStatus.toLowerCase() !== enabledStatusName ? Colors.error : Colors.statusApproved;
        const statusIcon =
            beneficiaryStatus.toLowerCase() !== enabledStatusName ? (
                <Icon name='ios-close-circle' size={moderateScale(24)} color={statusColor} />
            ) : (
                <Icon name='ios-checkmark-circle' size={moderateScale(24)} color={statusColor} />
            );
        const paymentMethodName = beneficiary.paymentMethod.name;
        const now = moment()
            .locale('es')
            .format('MMMM YYYY')
            .replace(/^\w/, (c) => c.toUpperCase());
        return (
            <SafeAreaView style={{ flex: 1 }}>
                <View style={styles.container}>
                    {!update_data_loading ? (
                        <React.Fragment>
                            <View style={styles.credentialContainer}>
                                <Credential style={styles.credential} beneficiaryInfo={beneficiary} />
                            </View>
                            <Text style={[font_styles.title_2_header, styles.generalInformationTitle]}>
                                {strings.beneficiaryInformation.beneficiary_info}
                            </Text>
                            <View style={{ flex: 1 }}>
                                <ScrollView
                                    scrollEventThrottle={16}
                                    style={{ flex: 1, width: width }}
                                    contentContainerStyle={styles.scrollViewContent}>
                                    <DecoratedCard
                                        image={statusIcon}
                                        color={statusColor}
                                        value={this._getCapitalizedText(beneficiaryStatus)}
                                        style={styles.decoratedCard}
                                    />
                                    <SimpleCard
                                        title={now}
                                        header={strings.beneficiaryInformation.charge_acum_prefix}
                                        value={
                                            selectedUserCharge !== null
                                                ? `$${selectedUserCharge}`
                                                : `$${strings.beneficiaryInformation.no_charges}`
                                        }
                                        style={styles.simpleCard}
                                    />
                                    <View style={styles.cardsRow}>
                                        <ImageCard
                                            onPress={this._handlePlanCardClick}
                                            header={strings.beneficiaryInformation.plan_prefix}
                                            title={beneficiary._embedded.insurancePlans[0].name}
                                            style={styles.imageCard}
                                            image={
                                                <Icon
                                                    name='ios-book-outline'
                                                    size={moderateScale(28)}
                                                    color={Colors.logoText}
                                                />
                                            }
                                        />
                                        <ImageCard
                                            header={strings.beneficiaryInformation.charge_type_prefix}
                                            title={paymentMethodName}
                                            style={styles.imageCard}
                                            image={
                                                <Icon
                                                    name='ios-wallet-outline'
                                                    size={moderateScale(28)}
                                                    color={Colors.logoText}
                                                />
                                            }
                                        />
                                    </View>
                                    <View style={styles.cardsRow}>
                                        <ImageCard
                                            header={strings.beneficiaryInformation.relationship_prefix}
                                            title={beneficiary.relationshipType.name}
                                            style={styles.imageCard}
                                            image={
                                                <Icon
                                                    name='ios-people-outline'
                                                    size={moderateScale(28)}
                                                    color={Colors.logoText}
                                                />
                                            }
                                        />
                                        <ImageCard
                                            header={strings.beneficiaryInformation.category}
                                            title={
                                                beneficiary.beneficiaryCategory
                                                    ? beneficiary.beneficiaryCategory.name
                                                    : '-'
                                            }
                                            style={styles.imageCard}
                                            image={
                                                <Icon
                                                    name='ios-pricetag-outline'
                                                    size={moderateScale(28)}
                                                    color={Colors.logoText}
                                                />
                                            }
                                        />
                                    </View>
                                </ScrollView>
                            </View>
                            <ActionButton
                                buttonColor={
                                    beneficiaryStatus.toLowerCase() !== enabledStatusName
                                        ? Colors.lightDividerLine
                                        : Colors.primary
                                }
                                active={false}
                                position={'right'}
                                fixNativeFeedbackRadius={true}
                                size={moderateScale(56)}
                                offsetX={15}
                                renderIcon={() =>
                                    this._renderButtonImage(
                                        beneficiaryStatus.toLowerCase() === enabledStatusName
                                    )
                                }>
                                {beneficiaryStatus.toLowerCase() === enabledStatusName && (
                                    <ActionButton.Item
                                        buttonColor={Colors.primary}
                                        title='Código QR'
                                        size={moderateScale(46)}
                                        textContainerStyle={styles.actionButtonText}
                                        textStyle={[
                                            font_styles.secondary_text,
                                            { color: Colors.primaryText },
                                        ]}
                                        onPress={() => this._generateCode('qr')}>
                                        <Icon name='ios-qr-code-outline' style={styles.actionButtonIcon} />
                                    </ActionButton.Item>
                                )}
                                {beneficiaryStatus.toLowerCase() === enabledStatusName && (
                                    <ActionButton.Item
                                        buttonColor={Colors.primary}
                                        title='Código Token'
                                        size={moderateScale(46)}
                                        textContainerStyle={styles.actionButtonText}
                                        textStyle={[
                                            font_styles.secondary_text,
                                            { color: Colors.primaryText },
                                        ]}
                                        onPress={() => this._generateCode('token')}>
                                        <Icon name='ios-key-outline' style={styles.actionButtonIcon} />
                                    </ActionButton.Item>
                                )}
                            </ActionButton>
                            <Modal
                                isVisible={isPlanModalVisible}
                                useNativeDriver={true}
                                onBackButtonPress={() => this.setState({ isPlanModalVisible: false })}
                                onBackdropPress={() => this.setState({ isPlanModalVisible: false })}
                                animationInTiming={500}
                                animationOutTiming={500}
                                backdropTransitionInTiming={500}
                                backdropTransitionOutTiming={500}
                                deviceHeight={height}
                                hideModalContentWhileAnimating={true}
                                animationOut={'slideOutDown'}
                                animationIn={'slideInUp'}>
                                <PlanModalContent plans={beneficiary._embedded.insurancePlans} />
                            </Modal>
                        </React.Fragment>
                    ) : (
                        <View style={styles.loadingContainer}>
                            <ActivityIndicator color={Colors.primaryText} size={'large'} />
                        </View>
                    )}
                </View>
            </SafeAreaView>
        );
    }
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        justifyContent: 'flex-start',
        alignItems: 'flex-start',
        backgroundColor: Colors.appBackground,
        paddingTop: Platform.OS === 'ios' ? NAV_BAR_HEIGHT_IOS : NAV_BAR_HEIGHT_ANDROID,
    },
    compoundCard: {
        width: width - 80,
        justifyContent: 'center',
        alignItems: 'center',
        backgroundColor: Colors.white,
        alignSelf: 'center',
        flexDirection: 'column',
        flex: 1,
        shadowColor: Colors.primaryText,
        shadowOffset: {
            width: 0,
            height: 1,
        },
        shadowOpacity: 0.18,
        shadowRadius: 1.0,
        elevation: 1,
        marginBottom: '2%',
    },
    title: {
        marginTop: '1%',
        marginBottom: '2%',
        fontFamily: bold,
        fontSize: moderateScale(12),
        color: Colors.secondaryText,
    },
    credential: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
        marginTop: '1%',
    },
    mainInformation: {
        flex: 0.35,
        width: width,
    },
    actionButtonIcon: {
        fontSize: moderateScale(20),
        color: Colors.primar,
    },
    modalContainer: {
        backgroundColor: 'white',
        width: width - moderateScale(30),
        alignSelf: 'center',
        borderRadius: moderateScale(4),
        borderColor: 'rgba(0, 0, 0, 0.1)',
    },
    modalTitleContainer: {
        width: '100%',
        paddingVertical: moderateScale(10),
        borderBottomColor: Colors.lightDividerLine,
        borderBottomWidth: 0.5,
        justifyContent: 'center',
    },
    modalPlanItemContainer: {
        paddingVertical: verticalScale(12),
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'space-between',
        paddingHorizontal: moderateScale(16),
    },
    modalPlanTitle: {
        flexDirection: 'row',
        flexShrink: 1,
    },
    credentialContainer: {
        width: width,
        height: height * 0.3,
        alignItems: 'center',
        justifyContent: 'center',
    },
    generalInformationTitle: {
        paddingLeft: moderateScale(16),
        marginBottom: moderateScale(10),
        marginTop: verticalScale(16),
    },
    scrollViewContent: {
        flexGrow: 1,
        alignItems: 'center',
        justifyContent: 'space-around',
        paddingHorizontal: moderateScale(16),
    },
    decoratedCard: {
        marginBottom: verticalScale(10),
        width: '100%',
        height: verticalScale(60),
    },
    simpleCard: {
        width: '100%',
        height: verticalScale(60),
        marginBottom: verticalScale(10),
    },
    cardsRow: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        width: '100%',
        marginBottom: verticalScale(10),
    },
    imageCard: {
        width: '48%',
    },
    actionButtonText: {
        height: verticalScale(21, 0.75),
        paddingVertical: 0,
    },
    loadingContainer: {
        flex: 1,
        width: width,
        alignItems: 'center',
        justifyContent: 'center',
    },
});

function mapStateToProps(state) {
    return {
        beneficiary: state.profile.relatives.selectedUser,
        update_data_loading: state.profile.update_user_data.loading,
        selectedUserCharge: state.charge.charges.currentMonthCharge,
    };
}

export default connect(mapStateToProps, null)(BeneficiaryInformationScreen);
