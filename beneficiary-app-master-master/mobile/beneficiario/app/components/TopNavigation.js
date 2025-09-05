import React, { PureComponent } from 'react';
import { connect } from 'react-redux';

import ProcedureScreenHeader from './header/ProcedureScreenHeader';
import BeneficiaryInformationScreenHeader from './header/BeneficiaryInformationScreenHeader';
import CommonHeader from './header/CommonHeader';
import QrTokenGeneratorScreenHeader from './header/QrCodeGeneratorScreenHeader';
import TokenCodeGeneratorScreenHeader from './header/TokenCodeGeneratorScreenHeader';
import FamilyManagementScreenHeader from './header/FamilyManagementScreenHeader';
import CoinsuranceChargeScreenHeader from './header/CoinsuranceChargeScreenHeader';
import ValidationStatusScreenHeader from './header/ValidationStatusScreenHeader';
import ProcedureDetailScreenHeader from './header/ProcedureDetailScreenHeader';
import NewProcedureScreenHeader from './header/NewProcedureScreenHeader';
import FilterModalScreenHeader from './header/FilterModalScreenHeader';
import ErrorBoundaryHeader from './header/ErrorBoundaryHeader';
import ValidationScreenHeader from './header/ValidationScreenHeader';
import ProcedureMessagesScreenHeader from './header/ProcedureMessagesScreenHeader';
import BatchScreenHeader from './header/BatchScreenHeader';
import BatchDetailScreenHeader from './header/BatchDetailScreenHeader';
import PrescriptionDetailScreenHeader from './header/PrescriptionDetailScreenHeader';
import SignUpHeader from './header/SignUpHeader';
import SignUpHelpModalHeader from './header/SignUpHelpModalHeader';
import AuthorizationRatingScreenHeader from './header/AuthorizationRatingScreenHeader';
import AuthorizationUnawarenessScreenHeader from './header/AuthorizationUnawarenessScreenHeader';
import PreAuthorizationScreenHeader from './header/PreAuthorizationScreenHeader';
import PreAuthorizationDetailScreenHeader from './header/PreAuthorizationDetailScreenHeader';
import AppointmentScreenHeader from './header/AppointmentScreenHeader';
import NewAppointmentScreenHeader from './header/NewAppointmentScreenHeader';
import AppoitnmentDetailScreenHeader from './header/AppoitnmentDetailScreenHeader';
import ProfessionalScreenHeader from './header/ProfessionalScreenHeader';
import ProfessionalListScreenHeader from './header/ProfessionalListScreenHeader';
import ProfessionalDetailScreenHeader from './header/ProfessionalDetailScreenHeader';
import ProfessionalMapsScreenHeader from './header/ProfessionalMapsScreenHeader';


class TopNavigation extends PureComponent {
    constructor(props) {
        super(props);
        this.state = {
            currentScene: props.current_scene,
        };

        this._pop = this._pop.bind(this);
        this._goBack = this._goBack.bind(this);
        this._openDrawer = this._openDrawer.bind(this);
        this._getHeader = this._getHeader.bind(this);
    }

    componentDidUpdate(prevProps) {
        if (this.props.current_scene !== prevProps.current_scene) {
            this.setState({ currentScene: this.props.current_scene });
        }
    }

    _pop() {
        this.props.navigation.pop();
    }

    _goBack() {
        this.props.navigation.goBack();
    }

    _openDrawer() {
        this.props.navigation.openDrawer();
    }

    _getHeader() {
        const { currentScene } = this.state;
        
        switch (currentScene) {
            case 'error-boundary':
                return <ErrorBoundaryHeader />;
            case 'Appointment':
                    return <AppointmentScreenHeader goBack={this._goBack} />;                
            case 'NewAppointment':
                return <NewAppointmentScreenHeader pop={this._pop} />;
            case 'AppointmentDetail':
                return <AppoitnmentDetailScreenHeader pop={this._pop} />;
            case 'BeneficiaryInformation':
                return <BeneficiaryInformationScreenHeader openDrawer={this._openDrawer} />;
            case 'SignUpHelpModal':
                return <SignUpHelpModalHeader pop={this._pop} />;
            case 'SignUp':
                return <SignUpHeader pop={this._pop} />;
            case 'ForgotPassword':
                return <CommonHeader pop={this._pop} />;
            case 'OTPToken':
                return <TokenCodeGeneratorScreenHeader pop={this._pop} />;
            case 'QrToken':
                return <QrTokenGeneratorScreenHeader pop={this._pop} />;
            case 'FamilyManagement':
                return <FamilyManagementScreenHeader pop={this._pop} />;
            case 'CoinsuranceCharges':
                return <CoinsuranceChargeScreenHeader />;
            case 'Authorizations':
                return <ValidationScreenHeader />;
            case 'ValidationStatus':
                return <ValidationStatusScreenHeader pop={this._pop} />;
            case 'Procedures':
                return <ProcedureScreenHeader goBack={this._goBack} />;
            case 'ProcedureDetail':
                return <ProcedureDetailScreenHeader pop={this._pop} />;
            case 'NewProcedure':
                return <NewProcedureScreenHeader pop={this._pop} />;
            case 'ProcedureMessages':
                return <ProcedureMessagesScreenHeader pop={this._pop} />;
            case 'FilterModal':
                return <FilterModalScreenHeader pop={this._pop} />;
            case 'Batch':
                return <BatchScreenHeader pop={this._pop} />;
            case 'BatchDetail':
                return <BatchDetailScreenHeader pop={this._pop} />;
            case 'PrescriptionDetail':
                return <PrescriptionDetailScreenHeader pop={this._pop} />;
            case 'AuthorizationRating':
                return <AuthorizationRatingScreenHeader pop={this._pop} />;
            case 'AuthorizationUnawareness':
                return <AuthorizationUnawarenessScreenHeader pop={this._pop} />;
            case 'PreAuthorizations':
                return <PreAuthorizationScreenHeader pop={this._pop} />;
            case 'PreAuthorizationDetail':
                return <PreAuthorizationDetailScreenHeader pop={this._pop} />;
            case 'Professional':
                return <ProfessionalScreenHeader pop={this._pop} />;      
            case 'ProfessionalList':
                return <ProfessionalListScreenHeader pop={this._pop} />;      
            case 'ProfessionalDetail':
                return <ProfessionalDetailScreenHeader pop={this._pop} />;      
            case 'ProfessionalMaps':
                return <ProfessionalMapsScreenHeader pop={this._pop} />;      
                
            case 'Login':
                return null;
        }
    }

    render() {
        return <React.Fragment>{this._getHeader()}</React.Fragment>;
    }
}

function mapStateToProps(state) {
    return {
        current_scene: state.router.currentScene,
    };
}

export default connect(mapStateToProps, null)(TopNavigation);
