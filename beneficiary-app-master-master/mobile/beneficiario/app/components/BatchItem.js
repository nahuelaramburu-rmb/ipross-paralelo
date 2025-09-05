import React, { useCallback, memo } from 'react';
import { View, StyleSheet, Text } from 'react-native';
import { verticalScale, moderateScale } from '../lib/size-normalizer';
import * as Colors from '../constants/Colors';
import moment from 'moment';
import { font_styles } from '../lib/default-styles';
import { useNavigation } from '@react-navigation/native';
import { getStatusColor, posibleStatuses } from '../lib/utils';
import ListItem from './list/ListItem';
import Icon from 'react-native-vector-icons/Ionicons';
import PropTypes from 'prop-types';

const statusIconMapping = {
    [posibleStatuses.EXPIRED]: 'ios-remove-circle-outline',
    [posibleStatuses.CANCELLED]: 'ios-close',
    [posibleStatuses.PENDING]: 'ios-alarm-outline',
    [posibleStatuses.ACTIVE]: 'ios-checkmark-circle-outline',
};

const BatchItem = ({ item }) => {
    const navigation = useNavigation();

    const { id, dateFrom, dateTo, _links, status } = item;

    const goToDetail = useCallback(() => {
        navigation.navigate('BatchDetail', {
            batchLink: _links.self.href,
            batchId: id,
        });
    }, [navigation, id, _links]);

    return (
        <ListItem
            rightIcon='ios-chevron-forward-outline'
            leftIcon={
                <Icon
                    name={statusIconMapping[status.name]}
                    size={moderateScale(24)}
                    color={getStatusColor(status.name)}
                />
            }
            onPress={goToDetail}>
            <View style={styles.itemBody}>
                <View style={styles.itemTitle}>
                    <Text style={[font_styles.primary_text]}>{`Módulo N° ${id} - `}</Text>
                    <Text style={[font_styles.primary_text, { color: getStatusColor(status.name) }]}>
                        {status.name}
                    </Text>
                </View>
                <View style={styles.itemSubtitle}>
                    <Text numberOfLines={1} ellipsizeMode='tail'>
                        <Text style={[font_styles.secondary_text, { color: Colors.primaryText }]}>
                            {moment(dateFrom).format('D/M/YYYY')}
                        </Text>
                        <Text style={[font_styles.secondary_text, { color: Colors.primaryText }]}> - </Text>
                        <Text style={[font_styles.secondary_text, { color: Colors.primaryText }]}>
                            {moment(dateTo).format('D/M/YYYY')}
                        </Text>
                    </Text>
                </View>
            </View>
        </ListItem>
    );
};

BatchItem.propTypes = {
    item: PropTypes.object,
};

const styles = StyleSheet.create({
    itemBody: {
        flex: 1,
        flexDirection: 'column',
    },
    itemTitle: {
        flexDirection: 'row',
        marginBottom: verticalScale(8),
    },
    itemSubtitle: {
        flex: 1,
        flexDirection: 'row',
        flexWrap: 'wrap',
        alignItems: 'center',
    },
});

export default memo(BatchItem);
