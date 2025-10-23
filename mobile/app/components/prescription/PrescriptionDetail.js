import React, { useEffect } from 'react';
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
import { useSelector, useDispatch, shallowEqual } from 'react-redux';
import { moderateScale, verticalScale } from '../../lib/size-normalizer';
import * as Colors from '../../constants/Colors';
import ImageCard from '../ImageCard';
import strings from '../../constants/Strings';
import Icon from 'react-native-vector-icons/Fontisto';
import { font_styles } from '../../lib/default-styles';
import { getStatusColor } from '../../lib/utils';
import ExtraDimensions from 'react-native-extra-dimensions-android';
import moment from 'moment';
import { selectPrescription } from '../../actions/prescriptionAction';
import get from 'lodash/get';
import { SafeAreaView, useSafeAreaInsets } from 'react-native-safe-area-context';
import TitleCard from '../TitleCard';
import PropTypes from 'prop-types';

const { width, height: iosHeight } = Dimensions.get('screen');
const height = Platform.OS === 'ios' ? iosHeight : ExtraDimensions.getRealWindowHeight();

const PrescriptionItem = ({ item, index }) => {
    return (
        <View style={{ paddingHorizontal: moderateScale(4) }}>
            <View style={styles.prescriptionItem}>
                <Text style={[font_styles.title_3_bold, { marginVertical: moderateScale(4) }]}>
                    Medicamento {index + 1}
                </Text>
                <Text numberOfLines={2} ellipsizeMode='tail' style={{ marginVertical: moderateScale(4) }}>
                    <Text style={font_styles.primary_text}>{strings.prescriptionDetail.medicine}: </Text>
                    <Text style={font_styles.primary_text_bold}>{item.medicine.product}</Text> -{' '}
                    <Text style={font_styles.secondary_text}>{item.medicine.presentation}</Text>
                </Text>
                {item.dailyDosage && (
                    <Text numberOfLines={1} ellipsizeMode='tail' style={{ marginVertical: moderateScale(4) }}>
                        <Text style={font_styles.primary_text}>
                            {strings.prescriptionDetail.dosis_quantity}:{' '}
                        </Text>
                        <Text style={font_styles.primary_text_bold}>{item.dailyDosage}</Text>
                    </Text>
                )}
                <Text numberOfLines={1} ellipsizeMode='tail' style={{ marginVertical: moderateScale(4) }}>
                    <Text style={font_styles.primary_text}>
                        {strings.prescriptionDetail.package_quantity}:{' '}
                    </Text>
                    <Text style={font_styles.primary_text_bold}>{item.quantity}</Text>
                </Text>
                <Text numberOfLines={2} ellipsizeMode='tail' style={{ marginVertical: moderateScale(4) }}>
                    <Text style={font_styles.primary_text}>
                        {strings.prescriptionDetail.treatment_days}:{' '}
                    </Text>
                    <Text style={font_styles.primary_text_bold}>{item.treatmentDays}</Text>
                </Text>
            </View>
        </View>
    );
};

const PrescriptionDetail = ({ route }) => {
    const prescriptionId = route.params?.prescriptionId;
    const insets = useSafeAreaInsets();
    const dispatch = useDispatch();

    const { selectedPrescription, selectedPrescriptionLoading } = useSelector(
        (state) => ({
            selectedPrescription: state.prescription.selectedPrescription.item,
            selectedPrescriptionLoading: state.prescription.selectedPrescription.loading,
        }),
        shallowEqual
    );

    useEffect(() => {
        dispatch(selectPrescription(prescriptionId));
    }, [dispatch, prescriptionId]);

    const renderPrescriptionItem = ({ item, index }) => {
        return <PrescriptionItem item={item} index={index} />;
    };

    if (selectedPrescriptionLoading) {
        return (
            <SafeAreaView style={styles.safeAreaView}>
                <View style={[styles.container, { alignItems: 'center' }]}>
                    <ActivityIndicator size='large' color={Colors.primaryText} />
                </View>
            </SafeAreaView>
        );
    }

    return (
        <SafeAreaView style={[styles.safeAreaView, { paddingTop: -insets.top }]}>
            <View style={styles.container}>
                <ScrollView
                    contentContainerStyle={{ flexGrow: 1 }}
                    scrollEventThrottle={16}
                    alwaysBounceVertical={false}
                    nestedScrollEnabled={true}>
                    <View style={styles.header}>
                        <Text style={[font_styles.title_1, { color: Colors.white }]}>
                            {`Receta N° ${selectedPrescription.id}`}
                        </Text>
                        <TitleCard
                            title={strings.prescriptionDetail.general_status}
                            subtitle={get(selectedPrescription, 'status.name', '')}
                            subtitleStyle={{
                                color: getStatusColor(get(selectedPrescription, 'status.name', '')),
                            }}
                            style={[styles.statusCard, styles.titleCard]}
                        />
                    </View>
                    <View style={styles.prescriptionInfoContainer}>
                        <View style={styles.cardsContainer}>
                            <ImageCard
                                header={strings.prescriptionDetail.practitioner.toUpperCase()}
                                title={`${get(selectedPrescription, 'practitioner.name', '')} ${get(
                                    selectedPrescription,
                                    'practitioner.lastName',
                                    ''
                                )}`}
                                style={{ width: '48%' }}
                                image={
                                    <Icon name='doctor' size={moderateScale(28)} color={Colors.logoText} />
                                }
                            />
                            <ImageCard
                                header={strings.prescriptionDetail.beneficiary.toUpperCase()}
                                title={`${get(selectedPrescription, 'beneficiary.name', '')} ${get(
                                    selectedPrescription,
                                    'beneficiary.lastName',
                                    ''
                                )}`}
                                style={{ width: '48%' }}
                                image={
                                    <Icon name='person' size={moderateScale(28)} color={Colors.logoText} />
                                }
                            />
                        </View>
                        <View style={[styles.statusCard]}>
                            <View style={[styles.statusContainer]}>
                                <Text style={[font_styles.title_3_bold, { marginBottom: verticalScale(12) }]}>
                                    {strings.prescriptionDetail.prescriptions}
                                </Text>
                                <View style={styles.divider} />
                                <View style={styles.mainInfoContainer}>
                                    <Text
                                        numberOfLines={1}
                                        ellipsizeMode='tail'
                                        style={{ marginVertical: moderateScale(4) }}>
                                        <Text style={font_styles.primary_text}>
                                            {strings.prescriptionDetail.prescription}{' '}
                                        </Text>
                                        <Text style={font_styles.primary_text_bold}>
                                            {get(selectedPrescription, 'id', '')}
                                        </Text>
                                    </Text>
                                    <Text style={{ marginVertical: moderateScale(4) }}>
                                        <Text style={font_styles.primary_text}>
                                            {strings.prescriptionDetail.prescription_number}:{' '}
                                        </Text>
                                        <Text
                                            style={[
                                                font_styles.primary_text_bold,
                                                { color: Colors.statusApproved },
                                            ]}>
                                            {get(selectedPrescription, 'exchangeId', []).join(' - ')}
                                        </Text>
                                    </Text>
                                    <Text
                                        numberOfLines={1}
                                        ellipsizeMode='tail'
                                        style={{ marginVertical: moderateScale(4) }}>
                                        <Text style={font_styles.primary_text}>
                                            {strings.prescriptionDetail.created_date}:{' '}
                                        </Text>
                                        <Text style={font_styles.primary_text_bold}>
                                            {selectedPrescription.createdAt &&
                                                moment(selectedPrescription.createdAt).format(
                                                    'D/M/YYYY HH:mm'
                                                )}
                                        </Text>
                                    </Text>
                                    <Text
                                        numberOfLines={1}
                                        ellipsizeMode='tail'
                                        style={{ marginVertical: moderateScale(4) }}>
                                        <Text style={font_styles.primary_text}>
                                            {strings.prescriptionDetail.is_preauthorized}:{' '}
                                        </Text>
                                        <Text style={font_styles.primary_text_bold}>
                                            {selectedPrescription.preAuthorized ? 'Sí' : 'No'}
                                        </Text>
                                    </Text>
                                    <Text
                                        numberOfLines={6}
                                        ellipsizeMode='tail'
                                        style={{ marginVertical: moderateScale(4) }}>
                                        <Text style={font_styles.primary_text}>
                                            {strings.prescriptionDetail.observations}:{' '}
                                        </Text>
                                        <Text style={font_styles.primary_text_bold}>
                                            {get(selectedPrescription, 'observations', '')}
                                        </Text>
                                    </Text>

                                    <FlatList
                                        scrollEventThrottle={16}
                                        style={styles.prescriptionItemsFlatList}
                                        contentContainerStyle={styles.prescriptionItemsFlatListContante}
                                        scrollEnabled={true}
                                        snapToInterval={width - moderateScale(14) * 4}
                                        decelerationRate='fast'
                                        data={get(selectedPrescription, '_embedded.prescriptionItems', [])}
                                        horizontal={true}
                                        keyExtractor={(item, index) => index.toString()}
                                        renderItem={renderPrescriptionItem}
                                    />
                                </View>
                            </View>
                        </View>
                    </View>
                </ScrollView>
            </View>
        </SafeAreaView>
    );
};

PrescriptionItem.propTypes = {
    item: PropTypes.object,
    index: PropTypes.number,
};

PrescriptionDetail.propTypes = {
    route: PropTypes.object,
};

const styles = StyleSheet.create({
    safeAreaView: {
        flex: 1,
    },
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
    cardsContainer: {
        marginTop: verticalScale(height * 0.1) / 2 + moderateScale(10),
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: verticalScale(10),
    },
    statusCard: {
        backgroundColor: Colors.white,
        elevation: 1,
        borderRadius: moderateScale(6),
        flex: 1,
    },
    titleCard: {
        position: 'absolute',
        height: moderateScale(height * 0.1),
        width: moderateScale(width * 0.7),
        bottom: -moderateScale(height * 0.1) / 2,
    },
    statusContainer: {
        flex: 1,
        flexDirection: 'column',
        alignItems: 'flex-start',
        justifyContent: 'flex-start',
        padding: moderateScale(14),
    },
    divider: {
        borderBottomColor: Colors.lightDividerLine,
        borderBottomWidth: 0.5,
        width: '100%',
        marginBottom: verticalScale(12),
    },
    mainInfoContainer: {
        flex: 1,
        flexDirection: 'column',
        alignItems: 'flex-start',
        justifyContent: 'space-between',
        width: '100%',
    },
    prescriptionItemContainer: {
        flex: 1,
        width: '100%',
    },

    prescriptionInfoContainer: {
        zIndex: -10,
        paddingHorizontal: moderateScale(14),
        paddingBottom: verticalScale(10),
        width: width,
    },
    prescriptionItem: {
        width: width - moderateScale(14) * 4 - moderateScale(4) * 2,
        padding: moderateScale(12),
        justifyContent: 'space-between',
        borderRadius: moderateScale(6),
        borderWidth: 1,
        borderColor: Colors.darkDividerLine,
    },
    prescriptionItemsFlatListContante: {
        flexGrow: 1,
        alignItems: 'center',
        justifyContent: 'center',
    },
    prescriptionItemsFlatList: {
        marginVertical: verticalScale(4),
        flex: 1,
    },
});

export default React.memo(PrescriptionDetail);
