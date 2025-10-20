import React, { PureComponent } from 'react';
import { StyleSheet } from 'react-native';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { DropDownHolder } from '../../components/DropDownHolder';
import strings from '../../constants/Strings';

import { SafeAreaView } from 'react-native-safe-area-context';
import { KeyboardAwareScrollView } from 'react-native-keyboard-aware-scroll-view';
import { moderateScale, verticalScale } from '../../lib/size-normalizer';
import AppointmentForm from '../../components/appointment/AppointmentForm';
import {
    createAppointment,
    getDelegations,
    getAppointmentEnabled,
    getDelegationSector,
    getAppointments,
} from '../../actions/appointmentAction';

class NewAppointmentScreen extends PureComponent {
    constructor(props) {
        super(props);
        this.state = {
            loading: false,
        };
        this._onConfirm = this._onConfirm.bind(this);
    }

    componentDidUpdate(prevProps, prevState) {
        if (prevState.loading !== this.state.loading) {
            this.props.navigation.setParams({
                loadingCreation: this.state.loading,
            });
        }
    }

    _onConfirm(data) {
        const { createAppointment } = this.props;
        this.setState({ loading: true });
        createAppointment(data)
            .then(() => {
                this.setState({ loading: false });
                this.props.getAppointments();
                this.props.navigation.navigate('Appointment');
                DropDownHolder.alert('success', 'Éxito', strings.newAppointment.appointment_creation_success);
            })
            .catch(() => {
                this.setState({ loading: false });
            });
    }

    render() {
        const { loading } = this.state;

        return (
            <SafeAreaView style={styles.container}>
                <KeyboardAwareScrollView
                    style={styles.container}
                    contentContainerStyle={styles.keyboardAwareContent}
                    keyboardShouldPersistTaps='always'
                    showVerticalScrollIndicator={false}
                    scrollEnabled={true}>
                    <AppointmentForm
                        disabled={false}
                        onConfirm={this._onConfirm}
                        creationLoading={loading}
                        editing={true}
                    />
                </KeyboardAwareScrollView>
            </SafeAreaView>
        );
    }
}

NewAppointmentScreen.propTypes = {
    navigation: PropTypes.shape({
        setParams: PropTypes.func.isRequired,
        navigate: PropTypes.func.isRequired,
    }).isRequired,
    createAppointment: PropTypes.func.isRequired,
    getAppointments: PropTypes.func.isRequired,
};

const styles = StyleSheet.create({
    container: {
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
});

function mapStateToProps(state) {
    return {
        delegations: state.appointment.delegations.items.data ?? [],
        loadingDelegation: state.appointment.delegations.loading,
        sectors: state.appointment.sectors.items.data ?? [],
        sectorsLoading: state.appointment.sectors.loading,
        appointments_enabled: state.appointment.appointments_enabled.items.data ?? [],
        turnosLoading: state.appointment.appointments_enabled.loading,
        afiliado_id: state.appointment.applicant_id,
    };
}

export default connect(mapStateToProps, {
    createAppointment,
    getAppointmentEnabled,
    getDelegations,
    getDelegationSector,
    getAppointments,
})(NewAppointmentScreen);
