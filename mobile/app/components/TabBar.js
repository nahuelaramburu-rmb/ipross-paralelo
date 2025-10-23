import PropTypes from 'prop-types';
import React from 'react';
import { View, Text, StyleSheet, Dimensions } from 'react-native';
import * as Colors from '../constants/Colors';
import Icon from 'react-native-vector-icons/Ionicons';
import strings from '../constants/Strings';
import { moderateScale, verticalScale } from '../lib/size-normalizer';
import { BorderlessButton } from 'react-native-gesture-handler';
import { semibold } from '../constants/Fonts';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

const { width } = Dimensions.get('window');

const TAB_LENGTH = 3;

export default (props) => {
    const insets = useSafeAreaInsets();
    return <TabBar {...props} insets={insets} />;
};

class TabBar extends React.Component {
    static propTypes = {
        navigation: PropTypes.object.isRequired,
    };

    constructor(props) {
        super(props);
        this.state = {
            tabs: props.state.routes,
            activeTab: props.state.routes[props.state.index],
        };

        this._determineOffset = this._determineOffset.bind(this);
    }

    _determineOffset(tabIndex) {
        if (TAB_LENGTH === 1) return 0;

        return (width / TAB_LENGTH) * tabIndex;
    }

    _changeTab(tab) {
        const { navigate } = this.props.navigation;
        this.setState({ activeTab: tab });
        navigate(tab.name);
    }

    _renderTab(tab, index) {
        const { activeTab } = this.state;
        switch (tab.name) {
            case 'BeneficiaryInformation':
                return (
                    <BorderlessButton
                        key={index}
                        onPress={() => this._changeTab(tab)}
                        style={styles.tabButton}>
                        <View style={styles.tabButtonContainer}>
                            <Icon
                                style={styles.tabIcon}
                                size={moderateScale(22, 0.75)}
                                name={
                                    activeTab.name === 'BeneficiaryInformation'
                                        ? 'ios-card'
                                        : 'ios-card-outline'
                                }
                                color={
                                    activeTab.name === 'BeneficiaryInformation'
                                        ? Colors.primaryText
                                        : Colors.secondaryText
                                }
                            />
                            <Text
                                numberOfLines={1}
                                style={{
                                    fontSize: moderateScale(10),
                                    bottom: verticalScale(4),
                                    fontFamily: semibold,
                                    color:
                                        activeTab.name === 'BeneficiaryInformation'
                                            ? Colors.primaryText
                                            : Colors.secondaryText,
                                }}>
                                {strings.tabBar.credential}
                            </Text>
                        </View>
                    </BorderlessButton>
                );
            case 'Authorizations':
                return (
                    <BorderlessButton
                        key={index}
                        onPress={() => this._changeTab(tab)}
                        style={styles.tabButton}>
                        <View style={styles.tabButtonContainer}>
                            <Icon
                                style={styles.tabIcon}
                                size={moderateScale(22, 0.75)}
                                name={activeTab.name === 'Authorizations' ? 'ios-heart' : 'ios-heart-outline'}
                                color={
                                    activeTab.name === 'Authorizations'
                                        ? Colors.primaryText
                                        : Colors.secondaryText
                                }
                            />
                            <Text
                                numberOfLines={1}
                                style={{
                                    fontSize: moderateScale(10),
                                    bottom: verticalScale(4),
                                    fontFamily: semibold,
                                    color:
                                        activeTab.name === 'Authorizations'
                                            ? Colors.primaryText
                                            : Colors.secondaryText,
                                }}>
                                {strings.tabBar.my_attentions}
                            </Text>
                        </View>
                    </BorderlessButton>
                );
            case 'CoinsuranceCharges':
                return (
                    <BorderlessButton
                        key={index}
                        onPress={() => this._changeTab(tab)}
                        style={styles.tabButton}>
                        <View style={styles.tabButtonContainer}>
                            <Icon
                                style={styles.tabIcon}
                                size={moderateScale(22, 0.75)}
                                name={
                                    activeTab.name === 'CoinsuranceCharges'
                                        ? 'ios-wallet'
                                        : 'ios-wallet-outline'
                                }
                                color={
                                    activeTab.name === 'CoinsuranceCharges'
                                        ? Colors.primaryText
                                        : Colors.secondaryText
                                }
                            />
                            <Text
                                numberOfLines={1}
                                style={{
                                    fontSize: moderateScale(10),
                                    bottom: verticalScale(4),
                                    fontFamily: semibold,
                                    color:
                                        activeTab.name === 'CoinsuranceCharges'
                                            ? Colors.primaryText
                                            : Colors.secondaryText,
                                }}>
                                {strings.tabBar.coinsurance_values}
                            </Text>
                        </View>
                    </BorderlessButton>
                );
            case 'Procedures':
                return (
                    <BorderlessButton
                        key={index}
                        onPress={() => this._changeTab(tab)}
                        style={styles.tabButton}>
                        <View style={styles.tabButtonContainer}>
                            <Icon
                                style={styles.tabIcon}
                                size={moderateScale(22, 0.75)}
                                name={
                                    activeTab.name === 'Procedures' ? 'ios-document' : 'ios-document-outline'
                                }
                                color={
                                    activeTab.name === 'Procedures'
                                        ? Colors.primaryText
                                        : Colors.secondaryText
                                }
                            />
                            <Text
                                numberOfLines={1}
                                style={{
                                    fontSize: moderateScale(10),
                                    bottom: verticalScale(4),
                                    fontFamily: semibold,
                                    color:
                                        activeTab.name === 'Procedures'
                                            ? Colors.primaryText
                                            : Colors.secondaryText,
                                }}>
                                {strings.tabBar.procedures}
                            </Text>
                        </View>
                    </BorderlessButton>
                );
            default:
                break;
        }
    }

    render() {
        const { tabs } = this.state;
        const { insets } = this.props;
        if (tabs === null) return null;
        return (
            <View style={[styles.tabBar, { bottom: insets.bottom }]}>
                {tabs.map((tab, index) => this._renderTab(tab, index))}
            </View>
        );
    }
}

const styles = StyleSheet.create({
    tabBar: {
        height: moderateScale(48, 1),
        backgroundColor: Colors.tabBarBackground,
        flexDirection: 'row',
        justifyContent: 'space-evenly',
        alignItems: 'center',
        shadowColor: Colors.primaryText,
        borderTopWidth: 0.5,
        borderTopColor: 'rgba(0, 0, 0, 0.1)',
        shadowOffset: {
            width: 0,
            height: 1,
        },
        shadowOpacity: 0.22,
        shadowRadius: 2.22,

        elevation: 3,
    },
    tabButton: {
        height: '100%',
        flex: 1,
        justifyContent: 'center',
    },
    tabButtonContainer: {
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        flex: 0.5,
    },
    tabIcon: {
        marginBottom: '1%',
    },
});
