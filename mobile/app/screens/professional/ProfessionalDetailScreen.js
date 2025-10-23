import React, { Component } from 'react';
import {
    Text,
    View,
    StyleSheet,
    Dimensions,
    FlatList,
    ScrollView,
    ActivityIndicator,
    Platform,
    RefreshControl,
} from 'react-native';
import { connect } from 'react-redux';
import { verticalScale, moderateScale } from '../../lib/size-normalizer';
import * as Colors from '../../constants/Colors';
import { font_styles } from '../../lib/default-styles';
import { getStatusColor } from '../../lib/utils';

import ImageCard from '../../components/ImageCard';
import Icon from 'react-native-vector-icons/Fontisto';
import strings from '../../constants/Strings';
import ExtraDimensions from 'react-native-extra-dimensions-android';
import { SafeAreaView, useSafeAreaInsets } from 'react-native-safe-area-context';
import TitleCard from '../../components/TitleCard';
import { getMedicalCenters, getPractitioner } from '../../actions/professionalAction';
import ProfessionalDetailItemScreen from './ProfessionalDetailItemScreen';
import { useAnimatableHeader } from '../../hooks/utils';

const { width, height: iosHeight } = Dimensions.get('screen');
const height = Platform.OS === 'ios' ? iosHeight : ExtraDimensions.getRealWindowHeight();

const ProfessionalDetailScreenWithInsets = (props) => {
    const insets = useSafeAreaInsets();
    return <ProfessionalDetailScreen {...props} insets={insets} />;
};

class ProfessionalDetailScreen extends Component {
    constructor(props) {
        super(props);
        this.state = {};

        // this._renderMedicalItem = this._renderMedicalItem.bind(this);
    }

    componentDidMount() {
        const practitionerLink = this.props.route.params?.practitionerLink ?? null;
        const practitionerId = this.props.route.params?.practitionerId ?? null;

        if (practitionerId !== null) {
            this.props.getPractitioner(practitionerId);
            // this.props.getMedicalCenters(practitionerId);
        }
    }

    render() {
        const { practitioner, practitionerLoading, medicalCenters, insets } = this.props;

        return (
            <SafeAreaView style={{ flex: 1, paddingTop: -insets.top }}>
                <View style={styles.container}>
                    <ScrollView
                        refreshControl={<RefreshControl refreshing={false} onRefresh={() => {}} title='' />}
                        contentContainerStyle={{ flexGrow: 1 }}
                        scrollEventThrottle={16}
                        alwaysBounceVertical={false}
                        horizontal={false}>
                        <View style={styles.header}>
                            <Text style={[font_styles.title_1, { color: Colors.white }]}>
                                Consultorios Medicos
                            </Text>
                            <TitleCard
                                title={`Matricula Nro ${
                                    practitioner.practitionerCode == undefined
                                        ? ''
                                        : practitioner.practitionerCode
                                }`}
                                subtitle={practitioner.status == undefined ? '' : practitioner.status.name}
                                subtitleStyle={{
                                    color: getStatusColor(
                                        practitioner.status == undefined ? '' : practitioner.status.name
                                    ),
                                }}
                                style={styles.statusCard}
                            />
                        </View>
                        <View style={styles.validationInfoContainer}>
                            <View style={styles.basicInfoContainer}>
                                <ImageCard
                                    header={strings.validationStatus.practitioner.toUpperCase()}
                                    title={`${
                                        practitioner.lastName == undefined ? '' : practitioner.lastName
                                    } ${practitioner.name == undefined ? '' : practitioner.name}`}
                                    style={{
                                        width: '100%',
                                        marginTop: verticalScale(5),
                                    }}
                                    image={
                                        <Icon
                                            name='doctor'
                                            size={moderateScale(28)}
                                            color={Colors.logoText}
                                        />
                                    }
                                />
                            </View>
                            <View style={styles.chargeTotalContainer}>
                                <View style={styles.statusContainer}>
                                    <Text style={[font_styles.title_3_bold, { fontSize: 20 }]}>
                                        Especialidad/es
                                    </Text>
                                    {practitioner._embedded?.medicalSpecialties?.map((it) => (
                                        <Text
                                            numberOfLines={2}
                                            key={it.id}
                                            style={[
                                                font_styles.secondary_text,
                                                { color: Colors.primaryText },
                                            ]}>
                                            {`${it.name}`} &nbsp;
                                        </Text>
                                    ))}
                                </View>
                            </View>
                        </View>
                        <ScrollView
                            contentContainerStyle={{ flex: 0.5, marginTop: moderateScale(50) }}
                            refreshControl={
                                <RefreshControl refreshing={false} onRefresh={() => {}} title='' />
                            }
                            scrollEventThrottle={16}
                            alwaysBounceVertical={false}
                            horizontal={false}>
                            <ProfessionalDetailItemScreen />
                        </ScrollView>
                    </ScrollView>
                </View>
            </SafeAreaView>
        );
    }
}

const styles = StyleSheet.create({
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
    statusCard: {
        position: 'absolute',
        height: moderateScale(height * 0.1),
        width: moderateScale(width * 0.7),
        bottom: -moderateScale(height * 0.1) / 2,
    },
    statusContainer: {
        flex: 1,
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'space-around',
        padding: moderateScale(8),
    },
    validationInfoContainer: {
        width: width,
        zIndex: -10,
        marginTop: verticalScale(5),
    },
    medicalItemContainer: {
        width: width,
        alignItems: 'center',
        justifyContent: 'center',
    },
    medicalItem: {
        width: width - moderateScale(20),
        padding: moderateScale(12),
        flex: 1,
        borderRadius: moderateScale(6),
        backgroundColor: Colors.white,
        flexDirection: 'column',
        justifyContent: 'flex-start',
        alignItems: 'flex-start',
        elevation: 1,
        marginBottom: moderateScale(10),
        shadowColor: '#000',
        shadowOffset: {
            width: 0,
            height: 1,
        },
        shadowOpacity: 0.18,
        shadowRadius: 1.0,
    },
    medicalItemDescription: {
        width: '100%',
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'flex-start',
    },
    basicInfoContainer: {
        marginTop: verticalScale(height * 0.05) / 2 + moderateScale(10),
        flexDirection: 'row',
        justifyContent: 'center',
        alignItems: 'center',
        paddingHorizontal: moderateScale(14),
        marginBottom: 5,
    },
    chargeTotalContainer: {
        shadowColor: Colors.primaryText,
        marginTop: 5,
        shadowOffset: {
            width: 0,
            height: 1,
        },
        shadowOpacity: 0.18,
        shadowRadius: 1.0,
        elevation: 1,
        borderRadius: moderateScale(6),
        backgroundColor: Colors.white,
        width: moderateScale(width * 0.8),
        alignSelf: 'center',
        marginBottom: -70,
    },
    divider: {
        borderBottomColor: Colors.lightDividerLine,
        borderBottomWidth: 0.5,
        width: '100%',
        marginBottom: verticalScale(12),
    },
});

function mapStateToProps(state) {
    return {
        practitioner: state.professional.selectedPractitioner.item._embedded?.practitioners[0] ?? {},
        medicalCenters: state.professional.medicalCenters.items ?? [],
        practitionerLoading: state.professional.selectedPractitioner.loading,
    };
}

export default connect(mapStateToProps, {
    getPractitioner,
    getMedicalCenters,
})(ProfessionalDetailScreenWithInsets);
