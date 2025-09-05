import React, { memo, useCallback } from 'react';
import { View, StyleSheet, Platform, Text } from 'react-native';
import { shallowEqual, useDispatch, useSelector } from 'react-redux';
import * as Colors from '../../constants/Colors';
import { verticalScale, moderateScale } from '../../lib/size-normalizer';
import { font_styles } from '../../lib/default-styles';
import strings from '../../constants/Strings';
import Icon from 'react-native-vector-icons/Ionicons';
import moment from 'moment';
import { getPrescriptions } from '../../actions/prescriptionAction';
import { getStatusColor, posibleStatuses } from '../../lib/utils';
import { useNavigation } from '@react-navigation/native';
import { NAV_BAR_HEIGHT_ANDROID, NAV_BAR_HEIGHT_IOS } from '../header/HeaderWrapper';
import List, { ListItem } from '../list';
import PropTypes from 'prop-types';

const TAB_BAR_HEIGHT = verticalScale(50);

const iconStatusMappings = {
    [posibleStatuses.PENDING]: 'ios-alarm-outline',
    [posibleStatuses.APPROVED_FEM]: 'ios-checkmark-circle-outline',
    [posibleStatuses.REJECTED_FEM]: 'ios-alert-circle-outline',
    [posibleStatuses.CANCELLED_FEM]: 'ios-close-circle-outline',
    [posibleStatuses.CONSUMED_FEM]: 'ios-cart-outline',
    [posibleStatuses.EXPIRED_FEM]: 'ios-alarm-outline',
};

const PrescriptionItem = ({ item: prescriptionItem }) => {
    const navigation = useNavigation();

    const color = getStatusColor(prescriptionItem.status.name);

    const icon = (
        <Icon
            name={iconStatusMappings[prescriptionItem.status.name] || 'ios-help-circle'}
            size={moderateScale(24)}
            color={color}
        />
    );

    const handleOnPress = () => {
        navigation.navigate('PrescriptionDetail', {
            prescriptionId: prescriptionItem.id,
        });
    };

    return (
        <ListItem rightIcon='ios-chevron-forward-outline' leftIcon={icon} onPress={handleOnPress}>
            <View style={styles.itemBody}>
                <Text style={[font_styles.primary_text, { marginBottom: verticalScale(8) }]}>
                    Dr. {prescriptionItem.practitioner.name} {prescriptionItem.practitioner.lastName}
                </Text>
                <View style={styles.itemSubtitle}>
                    <Text numberOfLines={1} ellipsizeMode='tail'>
                        <Text style={[font_styles.secondary_text, { color: Colors.primaryText }]}>
                            {moment(prescriptionItem.createdAt).format('D/M/YYYY HH:mm')}
                        </Text>
                        <Text style={[font_styles.secondary_text, { color: Colors.primaryText }]}> - </Text>
                        <Text style={[font_styles.secondary_text, { color: color }]}>
                            {prescriptionItem.status.name}
                        </Text>
                    </Text>
                </View>
            </View>
        </ListItem>
    );
};

const MemorizedPrescriptionItem = PrescriptionItem;

const PrescriptionList = ({ filters, onScroll }) => {
    const dispatch = useDispatch();

    const { prescriptions, isLoadingPrescriptions, isLoadingMore, prescriptionsLink } = useSelector(
        (state) => ({
            prescriptions: state.prescription.prescriptionList.items._embedded?.prescriptions ?? [],
            isLoadingPrescriptions: state.prescription.prescriptionList.loading,
            isLoadingMore: state.prescription.prescriptionList.loadingMore,
            prescriptionsLink: state.prescription.prescriptionList.items._links ?? {},
        }),
        shallowEqual
    );

    const searchPrescriptions = useCallback(
        (isRefresh = false, filters = null, link = null) => {
            return dispatch(getPrescriptions(isRefresh, filters, link));
        },
        [dispatch]
    );
    
    return (
        <List
            filters={filters}
            onScroll={onScroll}
            loading={isLoadingPrescriptions}
            loadingMore={isLoadingMore}
            renderItem={({ item }) => <MemorizedPrescriptionItem item={item} />}
            data={prescriptions}
            getData={searchPrescriptions}
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
        paddingTop: Platform.OS === 'ios' ? 0 : NAV_BAR_HEIGHT_ANDROID + TAB_BAR_HEIGHT,
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

PrescriptionItem.propTypes = {
    item: PropTypes.object,
    onScroll: PropTypes.object,
};

PrescriptionList.propTypes = {
    filters: PropTypes.array,
    onScroll: PropTypes.object,
};

export default PrescriptionList;
