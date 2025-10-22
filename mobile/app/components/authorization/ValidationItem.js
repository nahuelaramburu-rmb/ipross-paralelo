import PropTypes from 'prop-types';
import React, { memo } from 'react';
import { View, Text, StyleSheet } from 'react-native';
import * as Colors from '../../constants/Colors';
import moment from 'moment';
import { verticalScale, moderateScale } from '../../lib/size-normalizer';
import { font_styles } from '../../lib/default-styles';
import { getStatusColor, posibleStatuses } from '../../lib/utils';
import { ListItem } from '../list';
import Icon from 'react-native-vector-icons/Ionicons';
import { bold } from '../../constants/Fonts';
import strings from '../../constants/Strings';

const statusIconMappings = {
    [posibleStatuses.PENDING_APPROVAL]: 'ios-alarm-outline',
    [posibleStatuses.APPROVED]: 'ios-checkmark-circle-outline',
    [posibleStatuses.PARTIALLY_APPROVED]: 'ios-warning-outline',
    [posibleStatuses.REJECTED]: 'ios-alert-circle-outline',
    [posibleStatuses.CANCELLED]: 'ios-close-circle-outline',
};

const AuthorizationItem = ({ item, itemPressed }) => {
    const statusColor = getStatusColor(item.status.name);

    const icon = (
        <Icon
            name={statusIconMappings[item.status.name] || 'ios-help-circle'}
            size={moderateScale(24)}
            color={statusColor}
        />
    );

    return (
        <ListItem rightIcon='ios-chevron-forward-outline' leftIcon={icon} onPress={() => itemPressed(item)}>
            <View style={styles.itemBody}>
                <Text style={[font_styles.primary_text, { marginBottom: verticalScale(8) }]}>
                    Dr. {item.practitioner.name} {item.practitioner.lastName}
                </Text>

                <View style={styles.itemSubtitle}>
                    <Text numberOfLines={1} ellipsizeMode='tail'>
                        <Text style={[font_styles.secondary_text, { color: Colors.primaryText }]}>
                            {moment(item.createdAt).format('D/M/YYYY HH:mm')}
                        </Text>
                        <Text style={[font_styles.secondary_text, { color: Colors.primaryText }]}> - </Text>
                        <Text style={[font_styles.secondary_text, { color: statusColor }]}>
                            {item.status.name}
                        </Text>
                    </Text>
                </View>
                {item.refundableItems && (
                    <View style={[styles.itemSubtitle, { marginTop: verticalScale(8) }]}>
                        <Text
                            style={[
                                font_styles.secondary_text,
                                { color: Colors.primaryText, fontFamily: bold },
                            ]}>
                            {strings.validationStatus.refundable.toUpperCase()}
                        </Text>
                    </View>
                )}
            </View>
        </ListItem>
    );
};

AuthorizationItem.propTypes = {
    item: PropTypes.object,
    itemPressed: PropTypes.func,
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

export default memo(AuthorizationItem);
