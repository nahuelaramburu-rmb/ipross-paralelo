import PropTypes from 'prop-types';
import React, { Component } from 'react';
import { View, Text, StyleSheet, Platform, Linking, Alert, ScrollView } from 'react-native';
import { connect } from 'react-redux';
import * as Colors from '../constants/Colors';
import strings from '../constants/Strings';
import { moderateScale, verticalScale } from '../lib/size-normalizer';
import Icon from 'react-native-vector-icons/Ionicons';
import { TouchableOpacity } from 'react-native-gesture-handler';
import DeviceInfo from 'react-native-device-info';
import { font_styles } from '../lib/default-styles';
import { apiUrls } from '../configs/api';
import { logout } from '../actions/profileAction';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

export default (props) => {
    const insets = useSafeAreaInsets();
    return <ConnectedComp {...props} insets={insets} />;
};

class Drawer extends Component {
    static propTypes = {
        navigation: PropTypes.object.isRequired,
        selectedUser: PropTypes.shape({
            name: PropTypes.string.isRequired,
            lastName: PropTypes.string.isRequired,
            relationshipType: PropTypes.object.isRequired,
        }),
        logout: PropTypes.func.isRequired,
    };

    constructor(props) {
        super(props);

        this._itemPress = this._itemPress.bind(this);
        this._logout = this._logout.bind(this);
        this._call = this._call.bind(this);
        this._getAppVersion = this._getAppVersion.bind(this);
        this._openAppointment = this._openAppointment.bind(this);
        this._openProfessional = this._openProfessional.bind(this);

        this.state = {
            menuItems: [
                {
                    name: strings.drawer.opt1,
                    icon: 'ios-people-outline',
                    action: () => {
                        props.navigation.navigate('FamilyManagement');
                    },
                    key: 'familyManagement',
                },
                {
                    name: strings.drawer.opt6,
                    icon: 'ios-file-tray-full-outline',
                    action: () => props.navigation.navigate('PreAuthorizations'),
                    key: 'preAuthorizations',
                },
                {
                    name: strings.drawer.opt5,
                    icon: 'ios-medkit-outline',
                    action: () => props.navigation.navigate('Batch'),
                    key: 'batch',
                },
                {
                    name: strings.drawer.opt2,
                    icon: 'ios-call-outline',
                    action: this._call,
                    key: 'callCenter',
                },
                {
                    name: strings.drawer.opt4,
                    icon: 'ios-time-outline',
                    action: () => props.navigation.navigate('Appointment'),
                    key: 'appointment',
                },
                {
                    name: strings.drawer.opt7,
                    icon: 'ios-medical-outline',
                    action: () => props.navigation.navigate('Professional'),
                    key: 'professional',
                },
            ],
            app_version: null,
        };
    }

    _itemPress(i) {
        this.state.menuItems[i].action();
    }

    componentDidMount() {
        this._getAppVersion();
    }

    _openAppointment() {
        Linking.canOpenURL(apiUrls['ipross-appointment']).then((supported) => {
            if (supported) {
                Linking.openURL(apiUrls['ipross-appointment']);
            }
        });
    }

    _openProfessional() {
        Linking.canOpenURL(apiUrls['general-api']).then((supported) => {
            if (supported) {
                Linking.openURL(apiUrls['general-api']);
            }
        });
    }
    _call() {
        let phoneNumber = '08003334776';
        if (Platform.OS !== 'android') phoneNumber = `telprompt:${phoneNumber}`;
        else phoneNumber = `tel:${phoneNumber}`;
        Linking.canOpenURL(phoneNumber)
            .then((supported) => {
                if (!supported) {
                    Alert.alert(strings.general.error, `${strings.drawer.not_supported} ${phoneNumber}`);
                } else {
                    return Linking.openURL(phoneNumber);
                }
            })
            .catch((err) => console.log(err));
    }

    _logout() {
        this.props.navigation.closeDrawer();
        this.props.logout();
    }

    async _getAppVersion() {
        const version = await DeviceInfo.getVersion();
        this.setState({ app_version: version });
    }

    render() {
        const { app_version } = this.state;
        const { selectedUser, insets } = this.props;
        if (!selectedUser) return null;

        const menuItems = this.state.menuItems.map((item, i) => {
            return (
                <TouchableOpacity
                    key={i}
                    style={[styles.menuItemContainer]}
                    onPress={() => this._itemPress(i)}>
                    <Icon name={item.icon} size={moderateScale(26)} color={Colors.accent} />
                    <Text style={styles.menuItemText}>{item.name}</Text>
                </TouchableOpacity>
            );
        });

        return (
            <View style={[styles.container, { paddingTop: insets.top, paddingBottom: insets.bottom }]}>
                <View style={styles.drawerTop}>
                    <Text style={[font_styles.primary_text, styles.nameLastNameLabel]}>
                        {`${selectedUser.lastName}, ${selectedUser.name}`}
                    </Text>
                    <Text style={[font_styles.subtitle, styles.relationshipTypeLabel]}>
                        {selectedUser.relationshipType.name.toUpperCase()}
                    </Text>
                </View>
                <ScrollView
                    scrollEventThrottle={16}
                    style={styles.menuContainer}
                    contentContainerStyle={styles.scrollView}
                    showVerticalScrollIndicator={false}>
                    {menuItems}
                    <Text style={[font_styles.secondary_text, styles.appVersionLabel]}>
                        {`v${app_version}`}
                    </Text>
                </ScrollView>
                <TouchableOpacity
                    style={[styles.menuItemContainer, styles.logoutContainer]}
                    onPress={this._logout}>
                    <Icon name={'ios-log-out-outline'} size={moderateScale(26)} color={Colors.error} />
                    <View style={styles.textContainer}>
                        <Text style={[font_styles.title_3, styles.logoutLabel]}>
                            {strings.drawer.log_out}
                        </Text>
                    </View>
                </TouchableOpacity>
            </View>
        );
    }
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        justifyContent: 'flex-start',
        alignItems: 'flex-start',
    },
    drawerTop: {
        width: '100%',
        backgroundColor: Colors.primary,
        justifyContent: 'center',
        alignItems: 'center',
        flex: 0.2,
        flexDirection: 'column',
    },
    menuContainer: {
        flex: 1,
        width: '100%',
    },
    scrollView: {
        flex: 1,
    },
    textContainer: {
        flex: 1,
    },
    menuItemContainer: {
        width: '100%',
        flexDirection: 'row',
        alignItems: 'center',
        height: verticalScale(48),
        paddingHorizontal: moderateScale(12),
        borderBottomColor: Colors.lightDividerLine,
        borderBottomWidth: 0.5,
    },
    logoutContainer: {
        borderTopWidth: 0.5,
        borderTopColor: Colors.lightDividerLine,
        width: '100%',
        flexDirection: 'row',
        alignItems: 'center',
    },
    menuItemText: {
        fontSize: moderateScale(16),
        marginLeft: moderateScale(12),
        color: Colors.primaryText,
    },
    appVersionLabel: {
        position: 'absolute',
        bottom: verticalScale(12),
        right: moderateScale(12),
        color: Colors.lightDividerLine,
    },
    logoutLabel: {
        paddingLeft: moderateScale(12),
        color: Colors.error,
    },
    nameLastNameLabel: {
        color: Colors.white,
        paddingVertical: '1%',
    },
    relationshipTypeLabel: {
        color: Colors.white,
        paddingVertical: '1%',
    },
});

function mapStateToProps(state) {
    return {
        selectedUser: state.profile.relatives.selectedUser,
    };
}

const ConnectedComp = connect(mapStateToProps, {
    logout,
})(Drawer);
