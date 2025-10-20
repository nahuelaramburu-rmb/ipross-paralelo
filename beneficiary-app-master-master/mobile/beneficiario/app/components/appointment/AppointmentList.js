import React, { memo, useCallback, useEffect } from 'react';
import { View, StyleSheet, Platform, Text } from 'react-native';
import { shallowEqual, useDispatch, useSelector } from 'react-redux';
import * as Colors from '../../constants/Colors';
import { verticalScale, moderateScale } from '../../lib/size-normalizer';
import { font_styles } from '../../lib/default-styles';
import Icon from 'react-native-vector-icons/Ionicons';
import { getStatusColor, posibleStatuses } from '../../lib/utils';
import { useNavigation } from '@react-navigation/native';
import List, { ListItem } from '../list';
import PropTypes from 'prop-types';
import { NAV_BAR_HEIGHT_ANDROID, NAV_BAR_HEIGHT_IOS } from '../header/HeaderWrapper';
import { getAppointments } from '../../actions/appointmentAction';

const TAB_BAR_HEIGHT = verticalScale(50);

const iconStatusMappings = {
    [posibleStatuses.PENDING]: 'ios-alarm-outline',
    [posibleStatuses.CONFIRMADO]: 'ios-checkmark-circle-outline',
    [posibleStatuses.REJECTED_FEM]: 'ios-alert-circle-outline',
    [posibleStatuses.CANCELADO]: 'ios-close-circle-outline',
    [posibleStatuses.CONSUMED_FEM]: 'ios-cart-outline',
    [posibleStatuses.EXPIRED_FEM]: 'ios-alarm-outline',
};

const TurnoItem = ({ item: turnoItem }) => {
    const navigation = useNavigation();
    const color = getStatusColor(turnoItem.status_description);

    const icon = (
        <Icon
            name={iconStatusMappings[turnoItem.status_description] || 'ios-help-circle'}
            size={moderateScale(24)}
            color={color}
        />
    );

    const handleOnPress = () => {
        navigation.navigate('AppointmentDetail', {
            turnoId: turnoItem.id,
        });
    };

    return (
        <ListItem rightIcon='ios-chevron-forward-outline' leftIcon={icon} onPress={handleOnPress}>
            <View>
                <Text style={[font_styles.primary_text_bold, { color: Colors.logoText }]}>
                    Delegación: {turnoItem.delegation}
                </Text>
                <Text style={[font_styles.primary_text, { color: Colors.logoText }]}>
                    Sector: {turnoItem.sector}
                </Text>
                <Text style={font_styles.primary_text}>Día: {turnoItem.fecha}</Text>
                <Text style={font_styles.primary_text}>Hora: {turnoItem.hora} hs.</Text>

                <View style={styles.itemSubtitle}>
                    <Text numberOfLines={1} ellipsizeMode='tail'>
                        <Text style={[font_styles.secondary_text, { color: Colors.secondaryText }]}>
                            ESTADO
                        </Text>
                        <Text style={[font_styles.primary_text, { color: Colors.primaryText }]}> - </Text>
                        <Text style={[font_styles.primary_text, { color: color }]}>
                            {turnoItem.status_description}
                        </Text>
                    </Text>
                </View>
            </View>
        </ListItem>
    );
};

const MemorizedTurnoItem = memo(TurnoItem);

const AppointmentList = ({ filters, onScroll = true }) => {
    const dispatch = useDispatch();

    const { turnos, isLoadingTurnos, prescriptionsLink } = useSelector(
        (state) => ({
            turnos: state.appointment.appointments.items.data ?? [],
            isLoadingTurnos: state.appointment.appointments.loading,
            prescriptionsLink: {},
        }),
        shallowEqual
    );

    const searchTurnos = useCallback(
        (isRefresh = false) => {
            return dispatch(getAppointments(isRefresh));
        },
        [dispatch]
    );

    useEffect(() => {
        searchTurnos();
    }, [searchTurnos]);

    return (
        <List
            onScroll={onScroll}
            loading={isLoadingTurnos}
            renderItem={({ item }) => <MemorizedTurnoItem item={item} />}
            data={turnos}
            getData={searchTurnos}
            links={prescriptionsLink}
            contentInset={{ top: NAV_BAR_HEIGHT_IOS + TAB_BAR_HEIGHT }}
            contentOffset={{ x: 0, y: -(NAV_BAR_HEIGHT_IOS + TAB_BAR_HEIGHT) }}
            progressViewOffset={NAV_BAR_HEIGHT_ANDROID + TAB_BAR_HEIGHT} // only works on android
            contentContainerStyle={styles.flatlistWrapper}
            loadingContainerStyle={styles.loaderContainer}
        />
    );
};

const styles = StyleSheet.create({
    flatlistWrapper: {
        flexGrow: 1,
    },
    loaderContainer: {
        flex: 1,
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        paddingTop:
            Platform.OS === 'ios'
                ? NAV_BAR_HEIGHT_IOS + TAB_BAR_HEIGHT
                : NAV_BAR_HEIGHT_ANDROID + TAB_BAR_HEIGHT,
    },
});

AppointmentList.propTypes = {
    filters: PropTypes.array,
    onScroll: PropTypes.object,
};

export default memo(AppointmentList);
