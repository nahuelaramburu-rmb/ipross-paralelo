import React, { useCallback, useEffect, memo } from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { useDispatch, useSelector, shallowEqual } from 'react-redux';
import List, { ListItem } from '../list';
import { getPreAuthorizations } from '../../actions/validationAction';
import { font_styles } from '../../lib/default-styles';
import { moderateScale, verticalScale } from '../../lib/size-normalizer';
import { getStatusColor, posibleStatuses } from '../../lib/utils';
import PropTypes from 'prop-types';
import { useNavigation } from '@react-navigation/native';
import { useAnimatableHeader } from '../../hooks/utils';
import Icon from 'react-native-vector-icons/Ionicons';
import strings from '../../constants/Strings';
import moment from 'moment';

const statusIconMappings = {
    [posibleStatuses.CONSUMED_FEM]: 'ios-cart-outline',
    [posibleStatuses.EXPIRED_FEM]: 'ios-alarm-outline',
    [posibleStatuses.ANNULLED_FEM]: 'ios-close-circle-outline',
    [posibleStatuses.ACTIVE_FEM]: 'ios-checkmark-circle-outline',
};

const PreAuthorizationItem = ({ item }) => {
    const navigation = useNavigation();
    const statusColor = getStatusColor(item.status.name);

    const goToDetail = useCallback(() => {
        navigation.navigate('PreAuthorizationDetail', { preAuthorizationId: item.id });
    }, [navigation, item]);

    const icon = (
        <Icon
            name={statusIconMappings[item.status.name] || 'ios-help-circle'}
            size={moderateScale(24)}
            color={statusColor}
        />
    );

    return (
        <ListItem rightIcon='ios-chevron-forward-outline' leftIcon={icon} onPress={goToDetail}>
            <View style={styles.itemBody}>
                <Text
                    style={[font_styles.primary_text, { marginBottom: verticalScale(8) }]}
                    numberOfLines={1}>
                    N°. {item.id} - {item.beneficiary.name} {item.beneficiary.lastName}
                </Text>
                <View style={styles.itemSubtitle}>
                    <Text numberOfLines={1}>
                        <Text
                            style={[font_styles.secondary_text, { color: getStatusColor(item.status.name) }]}>
                            {item.status.name}
                        </Text>
                        {' - '}
                        <Text style={[font_styles.subtitle]} numberOfLines={1}>
                            {strings.preAuthorizationList.expiration.toUpperCase()}:
                        </Text>{' '}
                        <Text style={font_styles.secondary_text}>
                            {moment(item.expirationDate).format('D/M/YYYY')}
                        </Text>
                    </Text>
                </View>
            </View>
        </ListItem>
    );
};

const MemorizedPreAuthorizationItem = memo(PreAuthorizationItem);

const PreAuthorizationList = () => {
    const dispatch = useDispatch();
    const { onScroll } = useAnimatableHeader();

    const {
        preAuthorizations,
        isLoadingPreAuthorizations,
        isLoadingMorePreAuthorizations,
        preAuthorizationLinks,
    } = useSelector(
        (state) => ({
            preAuthorizations:
                state.validation.preAuthorizationList.preAuthorizations._embedded?.preAuthorizations ?? [],
            preAuthorizationLinks: state.validation.preAuthorizationList.preAuthorizations._links ?? {},
            isLoadingPreAuthorizations: state.validation.preAuthorizationList.loading,
            isLoadingMorePreAuthorizations: state.validation.preAuthorizationList.loadingMore,
        }),
        shallowEqual
    );

    const searchPreAuthorizations = useCallback(
        (isRefresh = false, filters = null, link = null) => {
            return dispatch(getPreAuthorizations(isRefresh, filters, link));
        },
        [dispatch]
    );

    useEffect(() => {
        searchPreAuthorizations();
    }, [searchPreAuthorizations]);

    return (
        <List
            onScroll={onScroll}
            loading={isLoadingPreAuthorizations}
            loadingMore={isLoadingMorePreAuthorizations}
            renderItem={({ item }) => <MemorizedPreAuthorizationItem item={item} />}
            data={preAuthorizations}
            getData={searchPreAuthorizations}
            links={preAuthorizationLinks}
        />
    );
};

PreAuthorizationItem.propTypes = {
    item: PropTypes.object,
};

const styles = StyleSheet.create({
    itemBody: {
        flex: 1,
        flexDirection: 'column',
    },
    itemSubtitle: {
        flex: 1,
        flexDirection: 'row',
        flexWrap: 'wrap',
        alignItems: 'center',
    },
});

export default PreAuthorizationList;
