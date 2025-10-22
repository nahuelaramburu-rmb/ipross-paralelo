import React, { Component } from 'react';
import {
    Text,
    View,
    StyleSheet,
    Dimensions,
    FlatList,
    ScrollView,
    ActivityIndicator,
    Platform,
} from 'react-native';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { verticalScale, moderateScale } from '../../lib/size-normalizer';
import * as Colors from '../../constants/Colors';
import { font_styles } from '../../lib/default-styles';
import { getStatusColor } from '../../lib/utils';
import ImageCard from '../../components/ImageCard';
import Icon from 'react-native-vector-icons/Fontisto';
import strings from '../../constants/Strings';
import ExtraDimensions from 'react-native-extra-dimensions-android';
import { searchValidationAssociatedFiles, getValidation } from '../../actions/validationAction';
import { SafeAreaView, useSafeAreaInsets } from 'react-native-safe-area-context';
import TitleCard from '../../components/TitleCard';

const { width, height: iosHeight } = Dimensions.get('screen');
const height = Platform.OS === 'ios' ? iosHeight : ExtraDimensions.getRealWindowHeight();

const ValidationStatusScreenWithInsets = (props) => {
    const insets = useSafeAreaInsets();
    return <ValidationStatusScreen {...props} insets={insets} />;
};

class ValidationStatusScreen extends Component {
    constructor(props) {
        super(props);
        this.state = {
            validation: props.validation,
        };

        this._renderMedicalItem = this._renderMedicalItem.bind(this);
    }

    componentDidMount() {
        const { route } = this.props;
        if (route.params?.valId) this.props.getValidation(route.params?.valId);
        //if (Object.keys(validation).length > 0) this.props.searchValidationAssociatedFiles(validation.id);
    }

    static getDerivedStateFromProps(nextProps, prevState) {
        if (nextProps.validation !== prevState.validation) {
            return { validation: nextProps.validation };
        }
        return null;
    }

    _renderMedicalItem(it) {
        const { item } = it;
        return (
            <View style={styles.medicalItemContainer} key={it.index}>
                <View style={styles.medicalItem}>
                    <Text
                        numberOfLines={1}
                        ellipsizeMode='tail'
                        style={[font_styles.title_3_bold, { marginBottom: verticalScale(12) }]}>
                        {item.nomenclator.medicalPractice.name}
                    </Text>
                    <View style={styles.divider} />
                    <View
                        style={{
                            flex: 1,
                            alignItems: 'flex-start',
                            justifyContent: 'space-around',
                        }}>
                        <Text
                            numberOfLines={2}
                            ellipsizeMode='tail'
                            style={{ marginVertical: moderateScale(4) }}>
                            <Text style={font_styles.primary_text}>{strings.validationStatus.status}: </Text>
                            <Text
                                style={[
                                    font_styles.primary_text_bold,
                                    { color: getStatusColor(item.status.name) },
                                ]}>
                                {item.status.name}
                            </Text>
                        </Text>
                        <Text
                            numberOfLines={2}
                            ellipsizeMode='tail'
                            style={{ marginVertical: moderateScale(4) }}>
                            <Text style={font_styles.primary_text}>
                                {strings.validationStatus.quantity}:{' '}
                            </Text>
                            <Text style={[font_styles.primary_text_bold]}>{item.quantity}</Text>
                        </Text>
                        <Text
                            numberOfLines={2}
                            ellipsizeMode='tail'
                            style={{ marginVertical: moderateScale(4) }}>
                            <Text style={font_styles.primary_text}>
                                {strings.validationStatus.unit_price}:{' '}
                            </Text>
                            <Text style={[font_styles.primary_text_bold]}>{`$${item.chargeUnitPrice}`}</Text>
                        </Text>
                        <Text
                            numberOfLines={2}
                            ellipsizeMode='tail'
                            style={{ marginVertical: moderateScale(4) }}>
                            <Text style={font_styles.primary_text}>
                                {strings.validationStatus.subtotal}:{' '}
                            </Text>
                            <Text style={[font_styles.primary_text_bold]}>{`$${item.chargeSubtotal}`}</Text>
                        </Text>
                        <Text
                            numberOfLines={2}
                            ellipsizeMode='tail'
                            style={{ marginVertical: moderateScale(4) }}>
                            <Text style={font_styles.primary_text}>
                                {strings.validationStatus.refundable}:{' '}
                            </Text>
                            <Text style={[font_styles.primary_text_bold]}>
                                {item.refundable ? 'Si' : 'No'}
                            </Text>
                        </Text>
                        {item.resolution !== null ? (
                            <View style={[styles.medicalItemDescription]}>
                                <Text
                                    numberOfLines={6}
                                    ellipsizeMode='tail'
                                    style={{ marginVertical: moderateScale(4) }}>
                                    <Text style={font_styles.primary_text}>
                                        {strings.validationStatus.resolution}:{' '}
                                    </Text>
                                    <Text style={[font_styles.primary_text_bold]}>{item.resolution}</Text>
                                </Text>
                            </View>
                        ) : null}
                    </View>
                </View>
            </View>
        );
    }

    render() {
        const { validation } = this.state;
        const { validation_loading, insets } = this.props;

        if (Object.keys(validation).length === 0 && !validation_loading) return null;
        else if (Object.keys(validation).length === 0 && validation_loading) {
            return (
                <View style={{ flex: 1, alignItems: 'center', justifyContent: 'center' }}>
                    <ActivityIndicator size='large' color={Colors.primaryText} />
                </View>
            );
        }
        return (
            <SafeAreaView style={{ flex: 1, paddingTop: -insets.top }}>
                <View style={styles.container}>
                    <ScrollView
                        contentContainerStyle={{ flexGrow: 1 }}
                        scrollEventThrottle={16}
                        alwaysBounceVertical={false}>
                        <View style={styles.header}>
                            <Text style={[font_styles.title_1, { color: Colors.white }]}>
                                {`Atención N° ${validation.id}`}
                            </Text>
                            <TitleCard
                                title={strings.validationStatus.general_status}
                                subtitle={validation.status.name}
                                subtitleStyle={{ color: getStatusColor(validation.status.name) }}
                                style={styles.statusCard}
                            />
                        </View>
                        <View style={styles.validationInfoContainer}>
                            <View style={styles.basicInfoContainer}>
                                <ImageCard
                                    header={strings.validationStatus.practitioner.toUpperCase()}
                                    title={`${validation.practitioner.name} ${validation.practitioner.lastName}`}
                                    style={{ width: '48%' }}
                                    image={
                                        <Icon
                                            name='doctor'
                                            size={moderateScale(28)}
                                            color={Colors.logoText}
                                        />
                                    }
                                />
                                <ImageCard
                                    header={strings.validationStatus.beneficiary.toUpperCase()}
                                    title={`${validation.beneficiary.name} ${validation.beneficiary.lastName}`}
                                    style={{ width: '48%' }}
                                    image={
                                        <Icon
                                            name='person'
                                            size={moderateScale(28)}
                                            color={Colors.logoText}
                                        />
                                    }
                                />
                            </View>
                            <View style={styles.chargeTotalContainer}>
                                <View style={styles.statusContainer}>
                                    {/* <Text
                                        style={[font_styles.title_3, { fontSize: moderateScale(16, 0.25) }]}>
                                        {strings.validationStatus.charge_total}
                                    </Text> */}
                                    <Text style={[font_styles.title_3_bold, {}]}>
                                        {`$${validation.chargeTotal}`}
                                    </Text>
                                </View>
                            </View>
                            <FlatList
                                contentContainerStyle={{ flexGrow: 1 }}
                                showsHorizontalScrollIndicator={false}
                                snapToInterval={width}
                                decelerationRate='fast'
                                horizontal={true}
                                scrollEventThrottle={16}
                                keyExtractor={(item, index) => item.id.toString()}
                                data={validation._embedded.authorizationItems}
                                renderItem={this._renderMedicalItem}
                            />
                        </View>
                    </ScrollView>
                </View>
            </SafeAreaView>
        );
    }
}

ValidationStatusScreen.propTypes = {
    validation: PropTypes.object,
    route: PropTypes.object,
    getValidation: PropTypes.func,
    validation_loading: PropTypes.bool,
    insets: PropTypes.object,
};

const styles = StyleSheet.create({
    container: {
        flex: 1,
        justifyContent: 'flex-start',
        alignItems: 'center',
        flexDirection: 'column',
        backgroundColor: Colors.appBackground,
    },
    header: {
        height: moderateScale(height * 0.25),
        backgroundColor: Colors.accent,
        width: width,
        justifyContent: 'center',
        alignItems: 'center',
        flexDirection: 'column',
    },
    statusCard: {
        position: 'absolute',
        height: moderateScale(height * 0.1),
        width: moderateScale(width * 0.7),
        bottom: -moderateScale(height * 0.1) / 2,
    },
    statusContainer: {
        flex: 1,
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'space-around',
        padding: moderateScale(8),
    },
    validationInfoContainer: {
        width: width,
        zIndex: -10,
    },
    medicalItemContainer: {
        width: width,
        alignItems: 'center',
        justifyContent: 'center',
    },
    medicalItem: {
        width: width - moderateScale(20),
        padding: moderateScale(12),
        flex: 1,
        borderRadius: moderateScale(6),
        backgroundColor: Colors.white,
        flexDirection: 'column',
        justifyContent: 'flex-start',
        alignItems: 'flex-start',
        elevation: 1,
        marginBottom: moderateScale(10),
        shadowColor: '#000',
        shadowOffset: {
            width: 0,
            height: 1,
        },
        shadowOpacity: 0.18,
        shadowRadius: 1.0,
    },
    medicalItemDescription: {
        width: '100%',
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'flex-start',
    },
    basicInfoContainer: {
        marginTop: verticalScale(height * 0.1) / 2 + moderateScale(10),
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        width: '100%',
        paddingHorizontal: moderateScale(14),
        marginBottom: verticalScale(10),
    },
    chargeTotalContainer: {
        height: moderateScale(height * 0.1),
        shadowColor: Colors.primaryText,
        shadowOffset: {
            width: 0,
            height: 1,
        },
        shadowOpacity: 0.18,
        shadowRadius: 1.0,
        elevation: 1,
        borderRadius: moderateScale(6),
        backgroundColor: Colors.white,
        width: moderateScale(width * 0.8),
        alignSelf: 'center',
        marginBottom: verticalScale(10),
    },
    divider: {
        borderBottomColor: Colors.lightDividerLine,
        borderBottomWidth: 0.5,
        width: '100%',
        marginBottom: verticalScale(12),
    },
});

function mapStateToProps(state) {
    return {
        validation: state.validation.selectedAuthorization.item,
        validation_loading: state.validation.selectedAuthorization.loading,
        loading_files: state.validation.selectedAuthorization.loading_files,
        files: state.validation.selectedAuthorization.associated_files,
    };
}

export default connect(mapStateToProps, {
    searchValidationAssociatedFiles,
    getValidation,
})(ValidationStatusScreenWithInsets);
