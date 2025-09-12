import PropTypes from 'prop-types';
import React, { Component } from 'react';
import { connect } from 'react-redux';
import { StyleSheet, Text, View, Dimensions, ActivityIndicator, SectionList, Platform } from 'react-native';
import * as Colors from '../../constants/Colors';
import { font_styles } from '../../lib/default-styles';
import { moderateScale, verticalScale } from '../../lib/size-normalizer';
import Icon from 'react-native-vector-icons/Ionicons';
import moment from 'moment';
import strings from '../../constants/Strings';
import { getUserCharges, clearStatus, searchNextPage } from '../../actions/chargeAction';
import { SafeAreaView } from 'react-native-safe-area-context';
import { NAV_BAR_HEIGHT_IOS, NAV_BAR_HEIGHT_ANDROID } from '../../components/header/HeaderWrapper';

const { width } = Dimensions.get('window');

class CoinsuranceChargeScreen extends Component {
    static propTypes = {
        charges: PropTypes.shape({
            charges: PropTypes.object,
            _embedded: PropTypes.shape({
                charges: PropTypes.array,
            }),
        }).isRequired,
        charges_loading: PropTypes.bool.isRequired,
        charges_loading_more: PropTypes.bool.isRequired,
        status_charges: PropTypes.oneOfType([PropTypes.oneOfType([null]), PropTypes.string]),
        getUserCharges: PropTypes.func.isRequired,
        clearStatus: PropTypes.func.isRequired,
        searchNextPage: PropTypes.func.isRequired,
    };

    constructor(props) {
        super(props);
        this.state = {
            charges: [],
            normalicedData: [],
            isFetching: false,
        };

        this._normaliceData = this._normaliceData.bind(this);
        this._renderSectionHeader = this._renderSectionHeader.bind(this);
        this._renderSectionItem = this._renderSectionItem.bind(this);
        this._renderListItemSeparator = this._renderListItemSeparator.bind(this);
        this._updateCharges = this._updateCharges.bind(this);
        this._renderEmptyList = this._renderEmptyList.bind(this);
        this._searchNextPage = this._searchNextPage.bind(this);
        this._renderFooter = this._renderFooter.bind(this);
    }

    componentDidUpdate(prevProps, prevState) {
        if (
            this.props.charges._embedded &&
            this.props.charges._embedded.charges !== (prevProps.charges._embedded?.charges ?? [])
        ) {
            this.setState({ charges: this.props.charges._embedded.charges }, () => {
                this.setState({ normalicedData: this._normaliceData() });
            });
        }

        if (
            this.props.status_charges !== prevProps.status_charges &&
            this.props.status_charges === 'charges_updated'
        ) {
            this.props.clearStatus();
            this.setState({ isFetching: false });
        }

        if (
            this.props.charges._embedded &&
            this.props.charges._embedded.charges !== (prevProps.charges._embedded?.charges ?? [])
        ) {
            this.props.clearStatus();
            this.setState({ isFetching: false });
        }
    }

    componentDidMount() {
        if (typeof this.props.charges._embedded !== 'undefined') {
            this.setState({ charges: this.props.charges._embedded.charges }, () => {
                this.setState({ normalicedData: this._normaliceData() });
            });
        }
    }

    _searchNextPage() {
        if (this.props.charges_loading_more) return;
        this.props.searchNextPage();
    }

    _normaliceData() {
        let newArray = [];
        this.state.charges.forEach((charge) => {
            const index = newArray.findIndex((item) => item.year === charge.year);
            if (index === -1)
                newArray.push({ title: `Año ${charge.year}`, year: charge.year, data: [charge] });
            else newArray[index] = { ...newArray[index], data: [...newArray[index].data, charge] };
        });
        return newArray;
    }

    _renderSectionHeader({ section }) {
        return (
            <View style={styles.sectionHeader}>
                <Text style={font_styles.title_2_header}>{section.title}</Text>
            </View>
        );
    }

    _renderSectionItem({ item, index, section }) {
        return (
            <View style={styles.sectionItem}>
                <View style={styles.sectionItemIconContainer}>
                    <Icon name='ios-cash-outline' size={moderateScale(24)} color={Colors.accent} />
                </View>
                <View style={styles.sectionItemBody}>
                    <Text style={[font_styles.title_3]}>
                        {moment(`${item.month}`, 'M')
                            .format('MMMM')
                            .replace(/^\w/, (c) => c.toUpperCase())}
                    </Text>
                </View>
                <View style={styles.sectionItemGo}>
                    <Text numberOfLines={1} style={[font_styles.title_3_bold]}>{`$${item.chargeTotal}`}</Text>
                </View>
            </View>
        );
    }

    _renderListItemSeparator() {
        return <View style={styles.listItemSeparator} />;
    }

    _updateCharges() {
        this.setState({ isFetching: true });
        this.props.getUserCharges(true);
    }

    _renderEmptyList() {
        return (
            <View style={styles.emptyListContainer}>
                <Text style={[font_styles.secondary_text, { color: Colors.secondaryText }]}>
                    {strings.coinsurance_charges.no_coinsurance_charges_found}
                </Text>
            </View>
        );
    }

    _renderFooter() {
        let loading = (
            <View style={styles.containerCentered}>
                <ActivityIndicator size='large' color={Colors.primaryText} />
            </View>
        );

        if (this.props.charges_loading_more) return loading;
        else return null;
    }

    render() {
        let mainView = null;

        if (this.props.charges_loading) {
            mainView = <ActivityIndicator size='large' color={Colors.primaryText} />;
        } else {
            mainView = (
                <SectionList
                    refreshing={this.state.isFetching}
                    onRefresh={() => this._updateCharges()}
                    stickySectionHeadersEnabled={true}
                    ItemSeparatorComponent={this._renderListItemSeparator}
                    contentContainerStyle={styles.flatListStyle}
                    renderSectionHeader={this._renderSectionHeader}
                    ListEmptyComponent={this._renderEmptyList}
                    renderItem={this._renderSectionItem}
                    onEndReached={this._searchNextPage()}
                    onEndReachedThreshold={0.1}
                    ListFooterComponent={() => this._renderFooter()}
                    keyExtractor={(item, index) => item + index}
                    sections={this.state.normalicedData}
                />
            );
        }

        return (
            <SafeAreaView style={styles.safeArea}>
                <View style={styles.container}>{mainView}</View>
            </SafeAreaView>
        );
    }
}

const styles = StyleSheet.create({
    safeArea: {
        flex: 1,
    },
    container: {
        flex: 1,
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: Colors.appBackground,
        paddingTop: Platform.OS === 'ios' ? NAV_BAR_HEIGHT_IOS : NAV_BAR_HEIGHT_ANDROID,
    },
    sectionHeader: {
        padding: 15,
        backgroundColor: Colors.appBackground,
    },
    sectionItem: {
        paddingVertical: verticalScale(10),
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'space-around',
        paddingHorizontal: 6,
    },
    containerCentered: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
    },
    flatListStyle: {
        flexGrow: 1,
        width: width,
    },
    sectionItemIconContainer: {
        alignItems: 'center',
        justifyContent: 'center',
    },
    sectionItemBody: {
        flex: 0.5,
        alignItems: 'flex-start',
        justifyContent: 'center',
    },
    sectionItemGo: {
        flex: 0.3,
        alignItems: 'flex-end',
        justifyContent: 'center',
    },
    listItemSeparator: {
        width: '100%',
        borderBottomColor: Colors.lightDividerLine,
        borderBottomWidth: 0.5,
    },
    emptyListContainer: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
    },
});

function mapStateToProps(state) {
    return {
        charges: state.charge.charges.items,
        charges_loading: state.charge.charges.loading,
        charges_loading_more: state.charge.charges.loadingMore,
        status_charges: state.charge.charges.status,
    };
}

export default connect(mapStateToProps, {
    getUserCharges,
    clearStatus,
    searchNextPage,
})(CoinsuranceChargeScreen);
