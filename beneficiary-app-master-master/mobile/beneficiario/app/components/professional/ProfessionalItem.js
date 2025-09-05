import React, { useCallback, memo } from 'react';
import { View, StyleSheet, Text } from 'react-native';
import { verticalScale, moderateScale } from '../../lib/size-normalizer';
import * as Colors from '../../constants/Colors';
import { font_styles } from '../../lib/default-styles';
import { useNavigation } from '@react-navigation/native';
import { getStatusColor, posibleStatuses } from '../../lib/utils';
import ListItem from '../list/ListItem';
import Icon from 'react-native-vector-icons/Ionicons';
import PropTypes from 'prop-types';
import { getMedicalCenters, getPractitioner } from '../../actions/professionalAction';
import { useDispatch } from 'react-redux';

const ProfessionalItem = ({ item }) => {
    const navigation = useNavigation();

    const dispatch = useDispatch();

    const color = getStatusColor(posibleStatuses.CONFIRMADO);

    const medico = item;

    const goToDetail = useCallback(() => {
        dispatch(getPractitioner(medico.idNumber));

        dispatch(getMedicalCenters(medico.id));

        navigation.navigate('ProfessionalDetail', {
            practitionerLink: medico._links.medicalCenters.href,
            practitionerId: medico.idNumber,
        });
    }, [medico.idNumber, medico._links, getPractitioner, getMedicalCenters]);

    return (
        <ListItem
            rightIcon='ios-chevron-forward-outline'
            leftIcon={
                <Icon
                    name='ios-person-outline'
                    size={moderateScale(24)}
                    color={getStatusColor(posibleStatuses.PARTIALLY_APPROVED)}
                />
            }
            onPress={goToDetail}>
            <View style={styles.itemBody}>
                <Text
                    style={[font_styles.primary_text, { marginBottom: verticalScale(2), fontWeight: '600' }]}>
                    Dr. {medico.name} {medico.lastName}
                </Text>
                <Text numberOfLines={1} ellipsizeMode='tail'>
                    <Text
                        style={[
                            font_styles.secondary_text,
                            { color: Colors.primaryText, fontWeight: '500' },
                        ]}>
                        Matricula Nro:
                    </Text>
                    <Text style={[font_styles.secondary_text, { color: Colors.primaryText }]}> </Text>
                    <Text style={[font_styles.secondary_text, { color: Colors.primaryText }]}>
                        {medico.practitionerCode}
                    </Text>
                </Text>

                <View style={styles.itemSubtitle}>
                    <Text
                        style={[
                            font_styles.secondary_text,
                            { color: Colors.primaryText, fontWeight: '500' },
                        ]}>
                        Especialidad: &nbsp;
                    </Text>
                    {medico._embedded.medicalSpecialties.map((it) => (
                        <Text
                            numberOfLines={2}
                            key={it.id}
                            style={[font_styles.secondary_text_bold, { color: Colors.primaryText }]}>
                            {`${it.name}`} &nbsp;
                        </Text>
                    ))}
                    <Text numberOfLines={1} ellipsizeMode='tail'>
                        <Text
                            style={[
                                font_styles.secondary_text,
                                { color: Colors.primaryText, fontWeight: '500' },
                            ]}>
                            Estado Profesional
                        </Text>
                        <Text style={[font_styles.secondary_text, { color: Colors.primaryText }]}> - </Text>
                        <Text style={[font_styles.secondary_text, { color: color, fontWeight: '700' }]}>
                            {medico.status.name}
                        </Text>
                    </Text>
                </View>
            </View>
        </ListItem>
    );
};

ProfessionalItem.propTypes = {
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

export default memo(ProfessionalItem);
