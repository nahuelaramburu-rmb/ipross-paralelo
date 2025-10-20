import React, { PureComponent } from 'react';
import { ActivityIndicator, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { KeyboardAwareScrollView } from 'react-native-keyboard-aware-scroll-view';
import { moderateScale, verticalScale } from '../../lib/size-normalizer';
import MapView, { Marker } from 'react-native-maps';
import * as Colors from '../../constants/Colors';
import { connect } from 'react-redux';

class ProfessionalMapsScreen extends PureComponent {
    constructor(props) {
        super(props);
        this.state = {};
    }

    render() {
        const { medicalCoordinates, medicalCoordinatesLoading } = this.props;

        //Coordenada ipross Central Viedma

        let latitud = -40.8135745;
        let longitud = -62.9905272;
        let description = 'Delegacion Viedma';

        if (medicalCoordinatesLoading) {
            return (
                <SafeAreaView style={styles.safeAreaView}>
                    <View style={[styles.container, { alignContent: 'center' }]}>
                        <ActivityIndicator size='large' color={Colors.primaryText} />
                    </View>
                </SafeAreaView>
            );
        } else {
            //Set coordinates

            if (
                medicalCoordinates.results.length > 0 &&
                typeof medicalCoordinates.results[0].geometry !== 'undefined'
            ) {
                latitud = medicalCoordinates.results[0].geometry.location.lat;
                longitud = medicalCoordinates.results[0].geometry.location.lng;
                description = medicalCoordinates.results[0].formatted_address;
            }
        }
        return (
            <SafeAreaView style={{ flex: 1 }}>
                <KeyboardAwareScrollView
                    style={styles.container}
                    contentContainerStyle={styles.keyboardAwareContent}
                    keyboardShouldPersistTaps='always'
                    showVerticalScrollIndicator={false}
                    scrollEnabled={true}>
                    <MapView
                        style={styles.map}
                        zoomEnabled={true}
                        scrollEnabled={true}
                        showsScale={true}
                        showsMyLocationButton={true}
                        region={{
                            latitude: latitud,
                            longitude: longitud,
                            latitudeDelta: 0.015,
                            longitudeDelta: 0.0121,
                        }}>
                        <Marker
                            coordinate={{
                                latitude: latitud,
                                longitude: longitud,
                            }}
                            title={'Consultorio / Centro Médico'}
                            description={description}
                        />
                    </MapView>
                </KeyboardAwareScrollView>
            </SafeAreaView>
        );
    }
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
    },
    map: {
        ...StyleSheet.absoluteFillObject,
    },
    safeAreaView: {
        flex: 1,
    },
    procedureTitle: {
        paddingLeft: moderateScale(16),
        marginTop: verticalScale(16),
    },
    keyboardAwareContent: {
        flexGrow: 1,
        padding: moderateScale(16),
    },
    actionButtonIcon: {
        fontSize: moderateScale(20),
        color: Colors.primar,
    },
    actionButtonText: {
        height: verticalScale(21, 0.75),
        paddingVertical: 0,
    },
});

function mapStateToProps(state) {
    return {
        medicalCoordinates: state.professional.medicalCoordinates.items ?? [],
        medicalCoordinatesLoading: state.professional.medicalCoordinates.loading ?? [],
    };
}

export default connect(mapStateToProps)(ProfessionalMapsScreen);
