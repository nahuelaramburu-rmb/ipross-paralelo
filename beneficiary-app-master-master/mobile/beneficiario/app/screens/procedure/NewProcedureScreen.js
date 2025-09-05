import React, { PureComponent } from 'react';
import { StyleSheet } from 'react-native';
import { connect } from 'react-redux';
import ProcedureForm from '../../components/procedure/ProcedureForm';
import { DropDownHolder } from '../../components/DropDownHolder';
import strings from '../../constants/Strings';
import { createProcedure, getCertificateTypes, getProcedures } from '../../actions/procedureAction';
import { findRelatives } from '../../actions/profileAction';
import { SafeAreaView } from 'react-native-safe-area-context';
import { KeyboardAwareScrollView } from 'react-native-keyboard-aware-scroll-view';
import { moderateScale, verticalScale } from '../../lib/size-normalizer';

class NewProcedureScreen extends PureComponent {
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

    _onConfirm(data, images) {
        const { createProcedure } = this.props;
        this.setState({ loading: true });
        createProcedure(data, images)
            .then(() => {
                this.setState({ loading: false });
                this.props.getProcedures();
                this.props.navigation.navigate('Procedures');
                DropDownHolder.alert('success', 'Éxito', strings.newProcedure.procedure_creation_success);
            })
            .catch(() => {
                this.setState({ loading: false });
            });
    }
    
        
    render() {
        const { loading } = this.state;

        return (
            <SafeAreaView style={{ flex: 1 }}>
                <KeyboardAwareScrollView
                    style={styles.container}
                    contentContainerStyle={styles.keyboardAwareContent}
                    keyboardShouldPersistTaps='always'
                    showVerticalScrollIndicator={false}
                    scrollEnabled={true}>
                    <ProcedureForm
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
        relatives: state.profile.relatives.items,
        relativesLoading: state.profile.relatives.loading,
        ownerUser: state.profile.userData,
        certificateTypes: state.procedure.certificateTypes.items,
        certificateTypesLoading: state.procedure.certificateTypes.loading,
    };
}

export default connect(mapStateToProps, {
    createProcedure,
    findRelatives,
    getCertificateTypes,
    getProcedures,
})(NewProcedureScreen);
