import React, { Component } from 'react';
import {
    StyleSheet,
    FlatList,
    Dimensions,
    Text,
    ActivityIndicator,
    View,
    ImageBackground,
    Platform,
    StatusBar,
} from 'react-native';
import { connect } from 'react-redux';
import Icon from 'react-native-vector-icons/Ionicons';
import { TouchableWithoutFeedback } from 'react-native-gesture-handler';
import ImageShow from '../../components/ImageShower';
import * as Colors from '../../constants/Colors';
import images from '../../configs/images';
import strings from '../../constants/Strings';
import findIndex from 'lodash/findIndex';
import { moderateScale, verticalScale } from '../../lib/size-normalizer';
import { font_styles } from '../../lib/default-styles';
import {
    STATUS_BAR_IOS,
    NAV_BAR_HEIGHT_ANDROID,
    NAV_BAR_HEIGHT_IOS,
} from '../../components/header/HeaderWrapper';
import { findRelatives, changeUser } from '../../actions/profileAction';
import { getValidations } from '../../actions/validationAction';
import { getUserCharges } from '../../actions/chargeAction';
import { getBatches } from '../../actions/batchAction';
import { getPrescriptions } from '../../actions/prescriptionAction';
import PropTypes from 'prop-types';
import { SafeAreaView, useSafeAreaInsets } from 'react-native-safe-area-context';
import CredentialUser from '../../images/credential_user.svg';

const { width } = Dimensions.get('screen');

export default (props) => {
    const insets = useSafeAreaInsets();
    return <ConnectedComp {...props} insets={insets} />;
};

class FamilyManagementScreen extends Component {
    static propTypes = {
        relatives: PropTypes.array.isRequired,
        ownerUser: PropTypes.object.isRequired,
        selectedUser: PropTypes.object.isRequired,
        findRelatives: PropTypes.func.isRequired,
        changeUser: PropTypes.func.isRequired,
        getValidations: PropTypes.func.isRequired,
        getUserCharges: PropTypes.func.isRequired,
        loading_relatives: PropTypes.bool.isRequired,
    };

    constructor(props) {
        super(props);
        this.state = {
            pictureSource: null,
            loading: false,
            loadingText: strings.familyManagement.loadingFamily,
            relatives: props.relatives,
            ownerUser: props.ownerUser,
            selectedUser: props.selectedUser,
        };

        this._changeUser = this._changeUser.bind(this);
        this._confirmSelectedUser = this._confirmSelectedUser.bind(this);
    }

    componentDidMount() {
        this.setState({ relatives: [...this.state.relatives, this.state.ownerUser] });
        if (this.props.relatives.length === 0) this.props.findRelatives();
    }

    getSnapshotBeforeUpdate(prevProps, prevState) {
        if (prevProps.relatives.length !== this.props.relatives.length) {
            return this.props.relatives;
        }

        return null;
    }

    componentDidUpdate(prevProps, prevState, snapshot) {
        if (snapshot !== null) {
            this.setState({ relatives: [...this.state.relatives.concat(snapshot)] });
        }
    }

    _changeUser(userId) {
        if (this.state.selectedUser.id !== userId) {
            const indx = findIndex(this.state.relatives, (rel) => {
                return rel.id === userId;
            });
            this.setState({ selectedUser: this.state.relatives[indx] }, () => {
                this._confirmSelectedUser();
            });
        }
    }

    _renderItem(item) {
        const { selectedUser } = this.state;
        return (
            <TouchableWithoutFeedback onPress={() => this._changeUser(item.item.id)}>
                <View style={styles.itemContainer}>
                    <View style={styles.itemTitleContainer}>
                        <Text
                            style={[font_styles.primary_text_bold, styles.nameLastNameItemLabel]}
                            numberOfLines={1}>
                            {`${item.item.lastName}, ${item.item.name}`}
                        </Text>
                        <Text style={[font_styles.subtitle]} numberOfLines={1}>
                            {item.item.relationshipType.name.toUpperCase()}
                        </Text>
                    </View>
                    {item.item.id === selectedUser.id ? (
                        <Icon
                            name='ios-checkmark-circle'
                            size={moderateScale(20, 0.75)}
                            color={Colors.statusApproved}
                        />
                    ) : null}
                </View>
            </TouchableWithoutFeedback>
        );
    }

    _confirmSelectedUser() {
        if (this.state.selectedUser === null) return;
        this.setState({ loading: true, loadingText: strings.common.loading });
        this.props.changeUser(this.state.selectedUser.id);
        setTimeout(() => {
            this.props.getValidations(false);
            this.props.getUserCharges(false);
            this.props.getPrescriptions({});
            this.props.getBatches();
            this.props.navigation.navigate('BeneficiaryInformation');
        }, 300);
    }

    render() {
        const { selectedUser, loadingText, loading } = this.state;
        const { loading_relatives, insets } = this.props;
        let mainView = null;
        mainView = (
            <React.Fragment>
                <View style={styles.imageContainer}>
                    <ImageBackground
                        source={images.family}
                        resizeMode={'cover'}
                        imageStyle={styles.image}
                        style={styles.backgroundImage}>
                        <View style={styles.profileTextContainer}>
                            <View style={styles.profileImageContainer}>
                                <CredentialUser fill={Colors.white} />
                            </View>
                            <Text style={[font_styles.title_2, styles.nameLastNameLabel]}>
                                {`${selectedUser.lastName}, ${selectedUser.name}`}
                            </Text>
                            <Text style={[font_styles.title_3, styles.idLabel]}>
                                {selectedUser.idType.alias} {selectedUser.idNumber}
                            </Text>
                        </View>
                    </ImageBackground>
                </View>
                <View style={styles.listContainer}>
                    <Text style={[font_styles.title_2_header, styles.listTitle]}>
                        {strings.familyManagement.available_users}
                    </Text>
                    <View style={styles.userList}>
                        {loading_relatives || loading ? (
                            <View style={styles.loadingContainer}>
                                <ActivityIndicator size='large' color={Colors.primaryText} />
                                <Text style={[font_styles.secondary_text, styles.loadingText]}>
                                    {loadingText}
                                </Text>
                            </View>
                        ) : (
                            <FlatList
                                scrollEventThrottle={16}
                                data={this.state.relatives}
                                renderItem={(item) => this._renderItem(item)}
                                keyExtractor={(item, index) => index.toString()}
                                extraData={this.state}
                            />
                        )}
                    </View>
                </View>
            </React.Fragment>
        );

        return (
            <SafeAreaView style={{ flex: 1, paddingTop: -insets.top }}>
                <View style={styles.container}>{mainView}</View>
            </SafeAreaView>
        );
    }
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
        backgroundColor: Colors.appBackground,
    },
    profileImageContainer: {
        width: moderateScale(100, 0.75),
        height: moderateScale(100, 0.75),
        marginVertical: 24,
        justifyContent: 'center',
        alignItems: 'center',
        backgroundColor: 'transparent',
        borderRadius: moderateScale(70, 0.75),
        borderWidth: 4,
        borderColor: Colors.white,
        overflow: 'hidden',
    },
    profileImage: {
        width: moderateScale(100, 1),
        height: moderateScale(100, 1),
        borderRadius: moderateScale(70, 1),
        overflow: 'hidden',
        tintColor: Colors.white,
    },
    userList: {
        flex: 1,
    },
    confirmationButton: {
        flex: 0.3,
        alignItems: 'center',
        justifyContent: 'center',
        width: width,
    },
    itemContainer: {
        flexGrow: 1,
        flexShrink: 0,
        flexDirection: 'row',
        justifyContent: 'flex-start',
        alignItems: 'center',
        paddingHorizontal: moderateScale(24),
        paddingVertical: verticalScale(15),
        borderBottomWidth: 0.5,
        borderBottomColor: Colors.lightDividerLine,
    },
    imageContainer: {
        flex: 0.5,
        shadowColor: Colors.primaryText,
        shadowOffset: { width: 0, height: 5 },
        shadowOpacity: 0.34,
        shadowRadius: 6.27,
        elevation: 10,
        width: width,
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: 'white',
    },
    backgroundImage: {
        flex: 1,
        width: width,
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: 'transparent',
    },
    image: {
        opacity: 0.8,
    },
    profileTextContainer: {
        flex: 1,
        width: width,
        alignItems: 'center',
        justifyContent: 'center',
        marginTop: Platform.OS === 'ios' ? STATUS_BAR_IOS + NAV_BAR_HEIGHT_IOS : NAV_BAR_HEIGHT_ANDROID,
    },
    nameLastNameLabel: {
        color: Colors.white,
    },
    idLabel: {
        color: Colors.white,
        marginTop: '1%',
    },
    listContainer: {
        flex: 0.5,
        width: width,
    },
    listTitle: {
        marginBottom: verticalScale(12),
        paddingTop: moderateScale(24),
        paddingLeft: moderateScale(24),
    },
    loadingContainer: {
        flex: 1,
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
    },
    nameLastNameItemLabel: {
        color: Colors.primaryText,
        paddingVertical: '1%',
    },
    itemTitleContainer: {
        flexDirection: 'column',
        flex: 1,
    },
    loadingText: {
        color: Colors.secondaryText,
        marginTop: verticalScale(6),
    },
});

function mapStateToProps(state) {
    return {
        relatives: state.profile.relatives.items,
        loading_relatives: state.profile.relatives.loading,
        ownerUser: state.profile.userData,
        selectedUser: state.profile.relatives.selectedUser,
    };
}

const ConnectedComp = connect(mapStateToProps, {
    findRelatives,
    changeUser,
    getValidations,
    getUserCharges,
    getBatches,
    getPrescriptions,
})(FamilyManagementScreen);
