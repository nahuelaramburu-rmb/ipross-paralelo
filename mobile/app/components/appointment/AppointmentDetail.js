import React, { useEffect } from 'react';
import {
    Text,
    View,
    StyleSheet,
    Dimensions,
    ScrollView,
    ActivityIndicator,
    Platform,
    Alert,
} from 'react-native';
import { useSelector, useDispatch, shallowEqual } from 'react-redux';
import { moderateScale, verticalScale } from '../../lib/size-normalizer';
import * as Colors from '../../constants/Colors';
import ImageCard from '../ImageCard';
import { DropDownHolder } from '../../components/DropDownHolder';
import strings from '../../constants/Strings';
import Icon from 'react-native-vector-icons/Fontisto';
import { font_styles } from '../../lib/default-styles';
import { getStatusColor } from '../../lib/utils';
import ExtraDimensions from 'react-native-extra-dimensions-android';
import get from 'lodash/get';
import { SafeAreaView, useSafeAreaInsets } from 'react-native-safe-area-context';
import TitleCard from '../TitleCard';
import PropTypes from 'prop-types';
import { cancelAppointment, getAppointmentById, getAppointments } from '../../actions/appointmentAction';
import Button from '../Button';

const { width, height: iosHeight } = Dimensions.get('screen');

const height = Platform.OS === 'ios' ? iosHeight : ExtraDimensions.getRealWindowHeight();

const AppointmentDetail = ({ route }) => {
    const turnoId = route.params?.turnoId;
    const insets = useSafeAreaInsets();
    const dispatch = useDispatch();

    const { turno, selectedAppointmentLoading, profile } = useSelector(
        (state) => ({
            turno: state.appointment.selectedAppointment.item,
            selectedAppointmentLoading: state.appointment.selectedAppointment.loading,
            profile: state.profile ?? [],
        }),
        shallowEqual
    );

    const ejectCancel = () => {
        dispatch(cancelAppointment(turno.token));
        DropDownHolder.alert('success', 'Éxito', 'Cancelación Exitosa');
        dispatch(getAppointmentById(turnoId));
        dispatch(getAppointments());
    };

    const handleCancel = () => {
        Alert.alert(
            'Cancelar Turno',
            '¿Desea Cancelar su Turno?',
            [
                {
                    text: 'NO',
                    style: 'cancel',
                },
                {
                    text: 'SI',
                    onPress: () => ejectCancel(),
                    style: 'default',
                },
            ],
            {
                cancelable: false,
            }
        );
    };

    useEffect(() => {
        dispatch(getAppointmentById(turnoId));
    }, [dispatch, turnoId]);

    if (selectedAppointmentLoading) {
        return (
            <SafeAreaView style={[styles.safeAreaView, { paddingTop: insets.top }]}>
                <View style={[styles.container, { alignContent: 'center' }]}>
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
                            {`Turno N° ${turno.id}`}
                        </Text>
                        <TitleCard
                            title={strings.openedAppointments.status_general}
                            subtitle={get(turno, 'status_description', '')}
                            subtitleStyle={{
                                color: getStatusColor(turno.status_description),
                            }}
                            style={[styles.statusCard, styles.titleCard]}
                        />
                    </View>
                    <View style={styles.prescriptionInfoContainer}>
                        <View style={styles.cardsContainer}>
                            <ImageCard
                                header={strings.openedAppointments.delegation.toUpperCase()}
                                title={get(turno, 'delegation', '')}
                                style={{ width: '48%' }}
                                image={<Icon name='home' size={moderateScale(28)} color={Colors.logoText} />}
                            />
                            <ImageCard
                                header={strings.prescriptionDetail.beneficiary.toUpperCase()}
                                title={`${profile.userData.lastName} ${profile.userData.name}`}
                                style={{ width: '48%' }}
                                image={
                                    <Icon name='person' size={moderateScale(28)} color={Colors.logoText} />
                                }
                            />
                        </View>
                        <View style={[styles.statusCard]}>
                            <View style={[styles.statusContainer]}>
                                <Text style={[font_styles.title_3_bold, { marginBottom: verticalScale(12) }]}>
                                    Datos Turno
                                </Text>
                                <View style={styles.divider} />
                                <View style={styles.mainInfoContainer}>
                                    <Text
                                        numberOfLines={1}
                                        ellipsizeMode='tail'
                                        style={{ marginVertical: moderateScale(4) }}>
                                        <Text style={font_styles.primary_text}>
                                            {strings.openedAppointments.sector}{' '}
                                        </Text>
                                        <Text style={font_styles.primary_text_bold}>
                                            {get(turno, 'sector', '')}
                                        </Text>
                                    </Text>
                                    <Text style={{ marginVertical: moderateScale(4) }}>
                                        <Text style={font_styles.primary_text}>
                                            {strings.openedAppointments.delegation_address}:{' '}
                                        </Text>
                                        <Text style={font_styles.primary_text_bold}>
                                            ? get(turno, 'delegation_address', '') : 'Sin datos'}
                                        </Text>
                                    </Text>
                                    <Text
                                        numberOfLines={1}
                                        ellipsizeMode='tail'
                                        style={{ marginVertical: moderateScale(4) }}>
                                        <Text style={font_styles.primary_text}>Día/Hora : </Text>
                                        <Text style={font_styles.primary_text_bold}>
                                            {turno.fecha} - {turno.hora} hs.
                                        </Text>
                                    </Text>
                                    <Text
                                        numberOfLines={1}
                                        ellipsizeMode='tail'
                                        style={{ marginVertical: moderateScale(4) }}>
                                        <Text style={font_styles.primary_text}>
                                            {strings.openedAppointments.attendance_description}:{' '}
                                        </Text>
                                        <Text
                                            style={[
                                                font_styles.primary_text_bold,
                                                { color: getStatusColor(turno.attendance_descripcion) },
                                            ]}>
                                            {turno.attendance_descripcion}
                                        </Text>
                                    </Text>
                                    {turno.status_id === 1 && (
                                        <View style={styles.containerButton}>
                                            <Button
                                                title={strings.openedAppointments.cancel}
                                                block={true}
                                                raised={true}
                                                type='solid'
                                                onPress={handleCancel}
                                            />
                                        </View>
                                    )}
                                </View>
                            </View>
                        </View>
                    </View>
                </ScrollView>
            </View>
        </SafeAreaView>
    );
};

AppointmentDetail.propTypes = {
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
    containerButton: {
        borderBottomColor: Colors.lightDividerLine,
        borderBottomWidth: 0.5,
        width: '100%',
        marginTop: verticalScale(20),
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

export default React.memo(AppointmentDetail);
