import React, { PureComponent } from 'react';
import { ActivityIndicator, StyleSheet, View, Dimensions, Text, AppState } from 'react-native';
import { connect } from 'react-redux';
import * as Colors from '../../constants/Colors';
import ProcedureForm from '../../components/procedure/ProcedureForm';
import get from 'lodash/get';
import { DropDownHolder } from '../../components/DropDownHolder';
import strings from '../../constants/Strings';
import { getStatusColor, posibleStatuses } from '../../lib/utils';
import { getProcedureById, updateProcedure } from '../../actions/procedureAction';
import { SafeAreaView } from 'react-native-safe-area-context';
import TitleCard from '../../components/TitleCard';
import { moderateScale, verticalScale } from '../../lib/size-normalizer';
import { font_styles } from '../../lib/default-styles';
import { KeyboardAwareScrollView } from 'react-native-keyboard-aware-scroll-view';

const { height } = Dimensions.get('window');

class ProcedureDetailScreen extends PureComponent {
    constructor(props) {
        super(props);
        this.state = {
            loading: false,
            editing: false,
            appState: AppState.currentState,
        };

        this._isEditable = this._isEditable.bind(this);
        this._editProcedure = this._editProcedure.bind(this);
        this._onCancel = this._onCancel.bind(this);
        this._onConfirm = this._onConfirm.bind(this);
        this._seeMessages = this._seeMessages.bind(this);
        this._handleAppStateChange = this._handleAppStateChange.bind(this);
    }

    componentDidMount() {
        const { route, navigation, getProcedureById, selectedProcedure } = this.props;
        const procedureLink = route.params?.procedureLink ?? null;
        const procedureId = route.params?.procedureId ?? null;
        const procedureType = route.params?.procedureType ?? null;
        if (procedureLink && procedureId !== selectedProcedure.id) getProcedureById({ link: procedureLink });
        else if (!procedureLink && procedureId !== selectedProcedure.id) {
            getProcedureById({ procedureId, procedureType }); // VENGO DESDE UNA NOTIFICACION
        } else if (this._isEditable(selectedProcedure)) {
            navigation.setParams({
                editProcedure: this._editProcedure,
                editing: false,
                seeMessages: this._seeMessages,
            });
        } else navigation.setParams({ seeMessages: this._seeMessages });

        this.props.navigation.setParams({ searchingProcedure: this.props.selectedProcedureLoading });
        // AppState.addEventListener('change', this._handleAppStateChange);
    }

    componentDidUpdate(prevProps) {
        const type = this.props.route.params?.type ?? null;
        if (
            prevProps.selectedProcedure !== this.props.selectedProcedure &&
            this._isEditable(this.props.selectedProcedure)
        ) {
            this.props.navigation.setParams({ editProcedure: this._editProcedure, editing: false });
        }

        if (prevProps.selectedProcedure !== this.props.selectedProcedure) {
            this.props.navigation.setParams({ seeMessages: this._seeMessages });
        }

        if (prevProps.selectedProcedureLoading !== this.props.selectedProcedureLoading) {
            this.props.navigation.setParams({ searchingProcedure: this.props.selectedProcedureLoading });
        }

        if (prevProps.selectedProcedure !== this.props.selectedProcedure && type === 'NEW_MESSAGE') {
            // ESTE IF ESTA PARA QUE SI HAY UN CAMBIO DE SELECTED PROCEDURE (ES DECIR QUE FUI A BUSCARLO AL SERVER) Y A SU VEZ EXISTE UN PARAM 'NEW_MESSEGE'
            // SIGNIFICA QUE VENGO DE UNA NOTIFICACION DE NEW PROCEDURE MESSEGE ENTONCES TRANSICIONO A LA PANTALLA PARA VERLO
            this.props.navigation.navigate('ProcedureMessages', {
                messages: this.props.selectedProcedure.messages,
            });
        }

        // ESTE IF ESTA POR SI RECIBO UNA NOTIFICACION DE NEW_MESSAGE O UPDATE_PROCEDURE Y ESTOY PARA VIENDO UN PROCEDURE
        // NECESITO DETECTAR QUE TAPPEARON LA NOTIFICACION Y LLEVARLO AL PROCEDURE DE LA NOTIFICACION TAPPEADA
        const procedureId = this.props.route.params?.procedureId ?? null;
        const procedureType = this.props.route.params?.procedureType ?? null;
        if (
            parseInt(prevProps.route.params?.procedureId) !== parseInt(procedureId) &&
            procedureType !== prevProps.route.params?.procedureType &&
            parseInt(procedureId) !== this.props.selectedProcedure.id
        ) {
            this.props.getProcedureById({ procedureId, procedureType });
        }
    }

    // componentWillUnmount() {
    //     AppState.removeEventListener('change', this._handleAppStateChange);
    // }

    _handleAppStateChange(nextAppState) {
        const { appState } = this.state;
        const { route } = this.props;
        const procedureLink = route.params?.procedureLink ?? null;
        const procedureId = route.params?.procedureId ?? null;
        const procedureType = route.params?.procedureType ?? null;
        if (appState.match(/inactive|background/) && nextAppState === 'active') {
            if (procedureLink) {
                this.props.getProcedureById({ link: procedureLink });
            } else {
                this.props.getProcedureById({ procedureId, procedureType });
            }
        }

        this.setState({ appState: nextAppState });
    }

    _editProcedure() {
        this.props.navigation.setParams({ editing: true, onCancel: this._onCancel });
        this.setState({ editing: true });
    }

    _onCancel() {
        this.setState({ editing: false });
        this.props.navigation.setParams({ editing: false });
    }

    _isEditable(selectedProcedure) {
        return [posibleStatuses.REVIEWING].indexOf(get(selectedProcedure, 'status.name', '')) > -1;
    }

    _onConfirm(data, images) {
        const { updateProcedure, selectedProcedure, selectedProcedureFiles, navigation } = this.props;
        this.setState({ loading: true });
        const deletedFiles = selectedProcedureFiles
            .filter((oldImg) => images.findIndex((img) => img.name === oldImg.name) === -1)
            .map((delImg) => delImg.name);
        const addedFiles = images.filter(
            (img) => selectedProcedureFiles.findIndex((oldImg) => img.name === oldImg.name) === -1
        );
        updateProcedure(selectedProcedure._links.self.href, data, addedFiles, deletedFiles)
            .then(() => {
                this.setState({ loading: false }, () => {
                    setTimeout(() => navigation.navigate('Procedures'), 100);
                    DropDownHolder.alert(
                        'success',
                        'Éxito',
                        strings.procedureDetail.procedure_update_success
                    );
                });
            })
            .catch(() => this.setState({ loading: false }));
    }

    _seeMessages() {
        const { navigation, selectedProcedure } = this.props;
        navigation.navigate('ProcedureMessages', { messages: selectedProcedure.messages });
    }

    render() {
        const { loading, editing } = this.state;
        const { selectedProcedureLoading, selectedProcedure, selectedProcedureFiles } = this.props;
        let mainView;

        if (selectedProcedureLoading) {
            mainView = (
                <View style={{ flex: 1, alignItems: 'center', justifyContent: 'center' }}>
                    <ActivityIndicator size='large' color={Colors.primaryText} />
                </View>
            );
        } else {
            mainView = (
                <React.Fragment>
                    <Text style={[font_styles.title_2_header]}>
                        {strings.procedureDetail.procedure_id} {selectedProcedure.id}
                    </Text>
                    <TitleCard
                        title={strings.prescriptionDetail.general_status}
                        subtitle={get(selectedProcedure, 'status.name', '')}
                        subtitleStyle={{
                            color: getStatusColor(get(selectedProcedure, 'status.name', '')),
                        }}
                        style={[styles.statusCard]}
                    />
                    <ProcedureForm
                        beneficiary={get(selectedProcedure, 'beneficiary.id')}
                        procedureType={selectedProcedure.type}
                        description={selectedProcedure.description}
                        certificateType={get(selectedProcedure, 'certificateType.id')}
                        files={selectedProcedureFiles}
                        disabled={true}
                        loading={loading}
                        onConfirm={this._onConfirm}
                        editing={editing}
                    />
                </React.Fragment>
            );
        }

        return (
            <SafeAreaView style={{ flex: 1 }}>
                <KeyboardAwareScrollView
                    style={styles.container}
                    contentContainerStyle={styles.keyboardAwareContent}
                    keyboardShouldPersistTaps='always'
                    showVerticalScrollIndicator={false}
                    scrollEnabled={true}>
                    {mainView}
                </KeyboardAwareScrollView>
            </SafeAreaView>
        );
    }
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
    },
    scrollViewcontainer: {
        flexGrow: 1,
    },
    statusCard: {
        height: height * 0.1,
        marginVertical: verticalScale(16),
    },
    keyboardAwareContent: {
        flexGrow: 1,
        padding: moderateScale(16),
    },
});

function mapStateToProps(state) {
    return {
        selectedProcedure: state.procedure.selectedProcedure.item,
        selectedProcedureLoading: state.procedure.selectedProcedure.loading,
        selectedProcedureFiles: state.procedure.selectedProcedure.files,
    };
}

export default connect(mapStateToProps, {
    getProcedureById,
    updateProcedure,
})(ProcedureDetailScreen);
