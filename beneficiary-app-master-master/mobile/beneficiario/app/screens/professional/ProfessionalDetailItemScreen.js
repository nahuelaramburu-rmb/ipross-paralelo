import React, { useCallback } from 'react';
import { Text, View, Dimensions, Platform, TouchableOpacity } from 'react-native';
import { shallowEqual, useDispatch, useSelector } from 'react-redux';
import List from '../../components/list';
import { useAnimatableHeader } from '../../hooks/utils';
import { StyleSheet } from 'react-native';
import { font_styles } from '../../lib/default-styles';
import * as Colors from '../../constants/Colors';
import { verticalScale, moderateScale } from '../../lib/size-normalizer';
import Icon from 'react-native-vector-icons/Ionicons';
import { useNavigation } from '@react-navigation/core';

import { getMedicalCoordinates } from '../../actions/professionalAction';

const { width, height } = Dimensions.get('screen');

const ProfessionalDetailItemScreen = () => {
    const { onScroll } = useAnimatableHeader();

    const navigation = useNavigation();

    const dispatch = useDispatch();

    const { medicalCenters, medicalCentersLoading, medicalCentersLink, medicalCoordinates } = useSelector(
        (state) => ({
            medicalCenters: state.professional.medicalCenters.items ?? [],
            medicalCentersLoading: state.professional.medicalCenters.loading,
            medicalCentersLink: state.professional.items._link ?? {},
            medicalCoordinates: state.professional.medicalCoordinates.items ?? [],
        }),
        shallowEqual
    );

    const goToMap = useCallback((city, street, streetNumber) => {
        dispatch(getMedicalCoordinates(city, street, streetNumber));

        navigation.navigate('ProfessionalMaps');
    }, []);

    const renderItem = ({ item, index }) => {
        return (
            <View style={styles.container}>
                <View style={{ ...styles.itemCard, marginTop: 10 }}>
                    <View style={styles.cardHeader}>
                        <View
                            style={{
                                flex: 1,
                                alignItems: 'flex-start',
                                flexDirection: 'row',
                                justifyContent: 'space-between',
                                padding: verticalScale(5),
                            }}>
                            <Text
                                numberOfLines={0}
                                ellipsizeMode='tail'
                                style={[
                                    font_styles.title_3_bold,
                                    {
                                        marginBottom: verticalScale(12),
                                        padding: 5,
                                        color: Colors.accent,
                                    },
                                ]}>
                                {item.name}
                            </Text>
                            <TouchableOpacity
                                style={{ padding: moderateScale(5) }}
                                onPress={() =>
                                    goToMap(
                                        item.address.city.name,
                                        item.address.street,
                                        item.address.streetNumber
                                    )
                                }>
                                <Icon name='search' size={moderateScale(28)} color={Colors.logoText} />
                            </TouchableOpacity>
                        </View>
                    </View>
                    <View style={styles.cardDetail}>
                        <View style={{ flexDirection: 'row', display: 'flex', alignItems: 'center' }}>
                            <Text style={[font_styles.primary_text]}>{`Dirección: `}</Text>
                            <Text numberOfLines={0} style={[font_styles.primary_text_bold]}>
                                {item.address.street == undefined ? 'Sin Datos' : item.address.street}
                            </Text>
                        </View>
                        <View style={{ flexDirection: 'row', display: 'flex', alignItems: 'center' }}>
                            <Text style={[font_styles.primary_text]}>Nro.: </Text>
                            <Text style={[font_styles.primary_text_bold]}>
                                {item.address.streetNumber == undefined ? '0' : item.address.streetNumber}
                            </Text>
                        </View>
                    </View>
                    <View style={styles.cardDetail}>
                        <View style={{ flexDirection: 'row', display: 'flex', alignItems: 'center' }}>
                            <Text style={[font_styles.primary_text]}>{`Localidad: `}</Text>
                            <Text numberOfLines={0} style={[font_styles.primary_text_bold]}>
                                {item.address.city.name == undefined ? 'Sin Datos' : item.address.city.name}
                            </Text>
                        </View>
                    </View>
                    <View style={styles.cardDetail}>
                        <View style={{ flexDirection: 'row', display: 'flex', alignItems: 'center' }}>
                            <Text style={[font_styles.primary_text]}>C.P: </Text>
                            <Text style={[font_styles.primary_text_bold]}>
                                {item.address.city.postalCode == undefined
                                    ? 'Sin Datos'
                                    : item.address.city.postalCode}
                            </Text>
                        </View>
                    </View>
                </View>
            </View>
        );
    };

    return (
        <List
            contentOffset={{ x: -10, y: -10 }}
            onScroll={onScroll}
            loading={medicalCentersLoading}
            loadingMore={false}
            renderItem={renderItem}
            data={medicalCenters._embedded ? medicalCenters._embedded.medicalCenters : []}
            links={medicalCentersLink}
        />
    );
};

const styles = StyleSheet.create({
    container: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
    },
    itemCard: {
        flex: 1,
        backgroundColor: Colors.white,
        borderRadius: moderateScale(10),
        width: width - moderateScale(30),
        elevation: 1,
        shadowColor: Colors.primaryText,
        shadowOffset: {
            width: 0,
            height: 1,
        },
        shadowOpacity: 0.18,
        shadowRadius: 1.0,
        marginBottom: 5,
    },
    cardHeader: {
        justifyContent: 'flex-start',
        flexDirection: 'row',
    },
    cardDetail: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        marginBottom: 5,
        padding: verticalScale(2),
        margin: verticalScale(5),
    },
    divider: {
        borderBottomColor: Colors.lightDividerLine,
        borderBottomWidth: 0.5,
        width: '100%',
        marginBottom: verticalScale(12),
    },
    safeArea: {
        flex: 1,
    },
});

export default ProfessionalDetailItemScreen;
