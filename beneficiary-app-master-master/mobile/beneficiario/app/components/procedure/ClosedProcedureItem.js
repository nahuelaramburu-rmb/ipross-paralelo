import React, { useCallback } from 'react';
import { View, Text, StyleSheet, Dimensions } from 'react-native';
import { moderateScale, verticalScale } from '../../lib/size-normalizer';
import moment from 'moment';
import * as Colors from '../../constants/Colors';
import { getStatusColor, posibleStatuses } from '../../lib/utils';
import Icon from 'react-native-vector-icons/Ionicons';
import { font_styles } from '../../lib/default-styles';
import { useNavigation } from '@react-navigation/native';
import strings from '../../constants/Strings';
import { ListItem } from '../list';
import PropTypes from 'prop-types';

const { width } = Dimensions.get('screen');

const statusIconMappings = {
    [posibleStatuses.REVIEWING]: 'ios-alarm-outline',
    [posibleStatuses.APPROVED]: 'ios-checkmark-circle-outline',
    [posibleStatuses.REJECTED]: 'ios-alert-circle-outline',
    [posibleStatuses.EXPIRED]: 'ios-alarm-outline',
};

const ClosedProcedureItem = React.memo(({ item }) => {
    const navigation = useNavigation();

    const color = getStatusColor(item.status.name);

    const icon = (
        <Icon
            name={statusIconMappings[item.status.name] || 'ios-help-circle'}
            size={moderateScale(24)}
            color={color}
        />
    );

    const goToDetail = useCallback(() => {
        navigation.navigate('ProcedureDetail', {
            procedureLink: item._links.self.href,
            procedureId: item.id,
        });
    }, [navigation, item]);

    return (
        <ListItem rightIcon='ios-chevron-forward-outline' leftIcon={icon} onPress={goToDetail}>
            <View style={styles.itemBody}>
                <Text
                    style={[font_styles.primary_text, { marginBottom: verticalScale(8) }]}
                    numberOfLines={1}>
                    {strings.closedProcedureItem.procedure} {item.id}
                </Text>
                <View style={styles.itemSubtitle}>
                    <Text numberOfLines={1} ellipsizeMode='tail'>
                        <Text style={[font_styles.secondary_text, { color: Colors.primaryText }]}>
                            {moment(item.createdAt).format('D/M/YYYY')}
                        </Text>
                        <Text style={[font_styles.secondary_text, { color: Colors.primaryText }]}> - </Text>
                        <Text style={[font_styles.secondary_text, { color }]}>{item.status.name}</Text>
                    </Text>
                </View>
            </View>
        </ListItem>
    );
});

ClosedProcedureItem.propTypes = {
    item: PropTypes.object,
};

const styles = StyleSheet.create({
    container: {
        borderBottomWidth: 0.5,
        borderBottomColor: Colors.lightDividerLine,
        justifyContent: 'space-between',
        alignItems: 'center',
        width: width,
        flexDirection: 'row',
        flexGrow: 1,
        flexShrink: 0,
        paddingHorizontal: moderateScale(16),
        paddingVertical: moderateScale(10),
    },
});

export default ClosedProcedureItem;
