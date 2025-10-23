import PropTypes from 'prop-types';
import React, { Component } from 'react';
import { StatusBar, View, Dimensions, Platform } from 'react-native';
import * as Colors from '../constants/Colors';
import DropdownAlert from 'react-native-dropdownalert';
import { DropDownHolder } from './DropDownHolder';
import Icon from 'react-native-vector-icons/Ionicons';
import { connect } from 'react-redux';

import { moderateScale } from '../lib/size-normalizer';

const { height } = Dimensions.get('screen');

class DropdownAlertMessage extends Component {
    static propTypes = {
        currentScene: PropTypes.string.isRequired,
    };

    constructor(props) {
        super(props);
        this.state = {
            color: Colors.primary,
            barStyle: 'default',
        };
        this._getInactiveColor = this._getInactiveColor.bind(this);
        this._renderImage = this._renderImage.bind(this);
    }

    componentDidUpdate(prevProps) {
        if (prevProps.currentScene !== this.props.currentScene) {
            this._getInactiveColor();
        }
    }

    componentDidMount() {
        this._getInactiveColor();
    }

    _getInactiveColor() {
        const oldAndroidDevice = Platform.OS === 'android' && Platform.Version <= 22;
        switch (this.props.currentScene) {
            case 'PrescriptionDetail':
            case 'ValidationStatus':
                this.setState({ color: Colors.accent });
                break;
            case 'SignUpHelpModal':
            case 'ForgotPassword':
            case 'PasswordReset':
            case 'SignUp':
            case 'Login':
                this.setState({ color: 'transparent', barStyle: 'dark-content' });
                break;
            case 'FamilyManagement':
            case 'TripMapModal':
                this.setState({ color: 'transparent' });
                break;
            default:
                this.setState({
                    color: oldAndroidDevice ? Colors.primaryText : Colors.white,
                    barStyle: 'dark-content',
                });
                break;
        }
    }

    _renderImage(prop, state) {
        switch (state.type) {
            case 'error':
                return (
                    <View style={{ alignSelf: 'center' }}>
                        <Icon name='ios-alert' size={moderateScale(30)} color={Colors.white} />
                    </View>
                );
            case 'success':
                return (
                    <View style={{ alignSelf: 'center' }}>
                        <Icon name='ios-checkmark-circle' size={moderateScale(30)} color={Colors.white} />
                    </View>
                );
            case 'info':
                return (
                    <View style={{ alignSelf: 'center' }}>
                        <Icon name='ios-information-circle' size={moderateScale(30)} color={Colors.white} />
                    </View>
                );
            default:
                break;
        }
    }

    render() {
        return (
            <React.Fragment>
                <StatusBar
                    translucent={true}
                    backgroundColor={this.state.color}
                    barStyle={this.state.barStyle}
                />
                <DropdownAlert
                    ref={(ref) => DropDownHolder.setDropDown(ref)}
                    closeInterval={4000}
                    updateStatusBar={true}
                    translucent={true}
                    wrapperStyle={{ top: 0, position: 'absolute' }}
                    inactiveStatusBarStyle={this.state.barStyle}
                    titleTextProps={{ fontFamily: 'Lato-Regular' }}
                    messageTextProps={{ fontFamily: 'Lato-Regular' }}
                    renderImage={(prop, state) => this._renderImage(prop, state)}
                    inactiveStatusBarBackgroundColor={this.state.color}
                />
            </React.Fragment>
        );
    }
}

function mapStateToProps(state) {
    return {
        currentScene: state.router.currentScene,
    };
}

export default connect(mapStateToProps, null)(DropdownAlertMessage);
