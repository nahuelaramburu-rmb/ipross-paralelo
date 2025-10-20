import React, { useCallback, memo, useRef } from 'react';
import {
    View,
    Text,
    StyleSheet,
    ActivityIndicator,
    Image,
    Dimensions,
    FlatList,
    ScrollView,
} from 'react-native';
import PropTypes from 'prop-types';
import { shallowEqual, useDispatch, useSelector } from 'react-redux';
import { getPreAuthorizationById } from '../../actions/validationAction';
import { useFocusEffect } from '@react-navigation/native';
import { SafeAreaView } from 'react-native-safe-area-context';
import * as Colors from '../../constants/Colors';
import { moderateScale, verticalScale } from '../../lib/size-normalizer';
import { font_styles } from '../../lib/default-styles';
import TitleCard from '../../components/TitleCard';
import strings from '../../constants/Strings';
import { getStatusColor } from '../../lib/utils';

const { width, height } = Dimensions.get('window');

const QRImage = memo(({ image }) => {
    return <Image source={{ uri: `data:image/gif;base64,${image}` }} style={styles.qrImage} />;
});

const PreAuthorizationItems = ({ items }) => {
    const renderItem = ({ item, index }) => {
        return (
            <View style={styles.preAuthItemContainer} key={index}>
                <View style={styles.preAuthItem}>
                    <Text
                        numberOfLines={1}
                        ellipsizeMode='tail'
                        style={[font_styles.title_3_bold, { marginBottom: verticalScale(12) }]}>
                        {item.nomenclator.medicalPractice.name}
                    </Text>
                    <View style={styles.divider} />
                    <Text numberOfLines={2} ellipsizeMode='tail' style={{ marginVertical: moderateScale(8) }}>
                        <Text style={font_styles.primary_text}>
                            {strings.preAuthorizationDetail.quantity}:{' '}
                        </Text>
                        <Text style={[font_styles.primary_text_bold]}>{item.quantity}</Text>
                    </Text>
                    <Text numberOfLines={2} ellipsizeMode='tail' style={{ marginVertical: moderateScale(8) }}>
                        <Text style={font_styles.primary_text}>
                            {strings.preAuthorizationDetail.available}:{' '}
                        </Text>
                        <Text style={[font_styles.primary_text_bold]}>{item.remaining}</Text>
                    </Text>
                    <Text numberOfLines={2} ellipsizeMode='tail' style={{ marginVertical: moderateScale(8) }}>
                        <Text style={font_styles.primary_text}>
                            {strings.preAuthorizationDetail.coInsurance}:{' '}
                        </Text>
                        <Text style={[font_styles.primary_text_bold]}>
                            {item.chargeUnitPrice
                                ? `$${item.chargeUnitPrice}`
                                : strings.preAuthorizationDetail.coverage}
                        </Text>
                    </Text>
                </View>
            </View>
        );
    };

    return (
        <FlatList
            style={styles.flatList}
            contentContainerStyle={styles.contentFlatList}
            showsVerticalScrollIndicator={false}
            horizontal={true}
            snapToInterval={width}
            scrollEventThrottle={16}
            decelerationRate='fast'
            showsHorizontalScrollIndicator={false}
            data={items}
            renderItem={renderItem}
            keyExtractor={(item) => item.id.toString()}
        />
    );
};

const PreAuthorizationDetailScreen = ({ route }) => {
    const preAuthorizationId = route.params.preAuthorizationId ?? null;
    const dispatch = useDispatch();

    const { preAuthorization, loadingPreAuthorization } = useSelector(
        (state) => ({
            preAuthorization:
                state.validation.preAuthorizationDetails.preAuthorizationsById[preAuthorizationId],
            loadingPreAuthorization: state.validation.preAuthorizationDetails.loading,
        }),
        shallowEqual
    );

    const preAuthorizationAlreadyExistsRef = useRef(typeof preAuthorization !== 'undefined');

    const fetchData = useCallback(() => {
        if (preAuthorizationId !== null) {
            const refreshEntity = preAuthorizationAlreadyExistsRef.current;
            dispatch(getPreAuthorizationById(preAuthorizationId, refreshEntity));
        }
    }, [dispatch, preAuthorizationId]);

    useFocusEffect(fetchData);

    if (!preAuthorizationId) return null;

    const qrImage = preAuthorization?.qr;
    return (
        <SafeAreaView style={styles.container}>
            {loadingPreAuthorization ? (
                <View style={styles.loadingContainer}>
                    <ActivityIndicator size='large' color={Colors.primaryText} />
                </View>
            ) : (
                <>
                    {typeof preAuthorization !== 'undefined' ? (
                        <ScrollView contentContainerStyle={styles.mainContainer} nestedScrollEnabled={true}>
                            <TitleCard
                                subtitle={preAuthorization.status.name}
                                style={styles.statusCard}
                                title={strings.preAuthorizationDetail.generalStatus}
                                subtitleStyle={{ color: getStatusColor(preAuthorization.status.name) }}
                            />
                            <TitleCard
                                subtitle={preAuthorization.code}
                                style={styles.statusCard}
                                title={strings.preAuthorizationDetail.manualCode}
                            />
                            <QRImage image={qrImage} />
                            <PreAuthorizationItems items={preAuthorization.medicalAuthorizationItems} />
                        </ScrollView>
                    ) : null}
                </>
            )}
        </SafeAreaView>
    );
};

PreAuthorizationDetailScreen.propTypes = {
    route: PropTypes.object,
};

QRImage.propTypes = {
    image: PropTypes.string,
};

PreAuthorizationItems.propTypes = {
    items: PropTypes.array,
};

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: Colors.appBackground,
    },
    loadingContainer: {
        flex: 1,
        alignItems: 'center',
        justifyContent: 'center',
    },
    mainContainer: {
        flexGrow: 1,
        padding: moderateScale(16),
        alignItems: 'center',
        justifyContent: 'flex-start',
    },
    preAuthItemContainer: {
        width: width,
        alignItems: 'center',
        justifyContent: 'center',
        paddingHorizontal: moderateScale(16),
    },
    preAuthItem: {
        width: '100%',
        padding: moderateScale(12),
        borderRadius: moderateScale(6),
        backgroundColor: Colors.white,
        flexDirection: 'column',
        justifyContent: 'flex-start',
        alignItems: 'flex-start',
        elevation: 1,
        shadowColor: '#000',
        shadowOffset: {
            width: 0,
            height: 1,
        },
        shadowOpacity: 0.18,
        shadowRadius: 1.0,
    },
    divider: {
        borderBottomColor: Colors.lightDividerLine,
        borderBottomWidth: 0.5,
        width: '100%',
        marginBottom: verticalScale(12),
    },
    qrImage: {
        height: height * 0.4,
        width: width * 0.8,
        resizeMode: 'cover',
    },
    flatList: {
        flex: 1,
        marginTop: verticalScale(14),
        width: width,
    },
    contentFlatList: {
        flexGrow: 1,
    },
    statusCard: {
        height: height * 0.1,
        width: '100%',
        marginBottom: verticalScale(14),
    },
});

export default memo(PreAuthorizationDetailScreen);
