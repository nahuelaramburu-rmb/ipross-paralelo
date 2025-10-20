import React from 'react';
import { Text, View, StyleSheet, Dimensions } from 'react-native';
import { moderateScale } from '../../lib/size-normalizer';
import * as Colors from '../../constants/Colors';
import { font_styles } from '../../lib/default-styles';
import moment from 'moment';
import { getStatusColor } from '../../lib/utils';
import { useNavigation } from '@react-navigation/native';
import { RectButton } from 'react-native-gesture-handler';
import strings from '../../constants/Strings';

const { width } = Dimensions.get('screen');

const procedureTypes = {
    CertificateProcedure: 'Certificado',
    DisabilityProcedure: 'Discapacidad',
    CUDProcedure: 'CUD',
    UnknownAuthorizationProcedure: 'Desconocimiento de Atención',
};

const CERTIFICATE_PROCEDURE = 'CertificateProcedure';

const OpenProcedureCard = React.memo(({ item }) => {
    const navigation = useNavigation();

    return (
        <View style={styles.container}>
            <View style={[styles.card, { borderColor: getStatusColor(item.status.name) }]}>
                <RectButton
                    onPress={() =>
                        navigation.navigate('ProcedureDetail', {
                            procedureLink: item._links.self.href,
                            procedureId: item.id,
                        })
                    }
                    style={styles.button}
                    testID='open-procedure-card-button'>
                    <View style={styles.cardHeader}>
                        <Text style={font_styles.title_3}>
                            {strings.openedProcedure.procedure} {item.id}
                        </Text>
                    </View>
                    <View style={styles.cardBody}>
                        <View style={styles.row}>
                            <Text style={font_styles.primary_text}>{strings.openedProcedure.date}: </Text>
                            <Text style={font_styles.primary_text_bold}>
                                {moment(item.createdAt).format('DD/MM/YYYY')}
                            </Text>
                        </View>
                        <View style={styles.row}>
                            <Text style={font_styles.primary_text}>{strings.openedProcedure.status}: </Text>
                            <Text
                                style={[
                                    font_styles.primary_text_bold,
                                    { color: getStatusColor(item.status.name) },
                                ]}>
                                {item.status.name}
                            </Text>
                        </View>
                        <View style={styles.row}>
                            <Text style={font_styles.primary_text}>
                                {strings.openedProcedure.beneficiary}:{' '}
                            </Text>
                            <Text
                                style={
                                    font_styles.primary_text_bold
                                }>{`${item.beneficiary.name} ${item.beneficiary.lastName}`}</Text>
                        </View>
                        <View style={styles.row}>
                            <Text numberOfLines={1} ellipsizeMode='tail'>
                                <Text style={font_styles.primary_text}>
                                    {strings.openedProcedure.procedure_type}:{' '}
                                </Text>
                                <Text style={font_styles.primary_text_bold}>
                                    {procedureTypes[item.type]}{' '}
                                    {item.type === CERTIFICATE_PROCEDURE
                                        ? `(${item.certificateType.name})`
                                        : null}
                                </Text>
                            </Text>
                        </View>
                    </View>
                </RectButton>
            </View>
        </View>
    );
});

const styles = StyleSheet.create({
    container: {
        flex: 1,
        width: width,
        alignItems: 'center',
        justifyContent: 'center',
        paddingVertical: moderateScale(8),
        paddingHorizontal: moderateScale(14),
    },
    card: {
        backgroundColor: Colors.white,
        borderRadius: moderateScale(10),
        width: '100%',
        height: '100%',
        borderWidth: moderateScale(1),
        borderStyle: 'solid',
        elevation: 5,
    },
    cardHeader: {
        paddingVertical: moderateScale(6),
    },
    cardBody: {
        flex: 1,
        justifyContent: 'space-around',
        flexDirection: 'column',
    },
    row: {
        flexDirection: 'row',
    },
    button: {
        flex: 1,
        paddingVertical: moderateScale(12),
        paddingHorizontal: moderateScale(18),
    },
});

export default OpenProcedureCard;
