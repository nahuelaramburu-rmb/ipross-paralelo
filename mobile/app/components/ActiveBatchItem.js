import React, { PureComponent } from 'react';
import { View, StyleSheet, Text, Platform, Animated, Dimensions } from 'react-native';
import { moderateScale, verticalScale } from '../lib/size-normalizer';
import * as Colors from '../constants/Colors';
import { font_styles } from '../lib/default-styles';
import Icon from 'react-native-vector-icons/Ionicons';
import moment from 'moment';
import { PanGestureHandler, RectButton } from 'react-native-gesture-handler';
import { useNavigation } from '@react-navigation/native';
import strings from '../constants/Strings';

const { width } = Dimensions.get('screen');

export default (props) => {
    const navigation = useNavigation();

    return <ActiveBatchItem {...props} navigation={navigation} />;
};

class ActiveBatchItem extends PureComponent {
    constructor(props) {
        super(props);
        this.state = {};

        this._onPanGestureEvent = this._onPanGestureEvent.bind(this);

        this.yTranslation = new Animated.Value(moderateScale(150));
    }

    _onPanGestureEvent(e) {
        if (e.nativeEvent.translationY < 0) {
            Animated.spring(this.yTranslation, {
                toValue: moderateScale(50),
                friction: 5,
                tension: 50,
                //useNativeDriver: true
            }).start();
        } else {
            Animated.spring(this.yTranslation, {
                toValue: moderateScale(150),
                friction: 5,
                tension: 50,
                //useNativeDriver: true
            }).start();
        }
    }

    render() {
        const { item, navigation } = this.props;

        const biggerCardOpacity = this.yTranslation.interpolate({
            inputRange: [moderateScale(50), moderateScale(150)],
            outputRange: [0, 1],
            extrapolate: 'clamp',
        });

        const smallerCardOpacity = this.yTranslation.interpolate({
            inputRange: [moderateScale(50), moderateScale(150)],
            outputRange: [1, 0],
            extrapolate: 'clamp',
        });

        return (
            <PanGestureHandler onGestureEvent={this._onPanGestureEvent} minDist={10} maxPointers={1}>
                <Animated.View style={[styles.container, { height: this.yTranslation }]}>
                    <Animated.View style={[styles.card, { opacity: biggerCardOpacity }]}>
                        <RectButton
                            onPress={() =>
                                navigation.navigate('BatchDetail', {
                                    batchLink: item._links.self.href,
                                    batchId: item.id,
                                })
                            }
                            style={{ backgroundColor: 'transparent' }}>
                            <View style={styles.cardContent}>
                                <View style={styles.cardContentHeader}>
                                    <Text style={font_styles.title_3_bold}>{`Módulo N° ${item.id}`}</Text>
                                    <Icon
                                        name={Platform.OS === 'ios' ? 'ios-cart' : 'md-cart'}
                                        size={moderateScale(24)}
                                        color={Colors.statusApproved}
                                    />
                                </View>
                                <View style={styles.cardContentBody}>
                                    <View style={[styles.dataRow, { paddingBottom: moderateScale(8) }]}>
                                        <Text style={font_styles.primary_text}>
                                            {strings.activeBatchItem.status}:
                                        </Text>
                                        <Text
                                            style={[
                                                font_styles.primary_text_bold,
                                                {
                                                    color: Colors.statusApproved,
                                                    marginLeft: moderateScale(5),
                                                },
                                            ]}>
                                            {item.status.name}
                                        </Text>
                                    </View>
                                    <View style={styles.dataRow}>
                                        <View style={styles.dataRow}>
                                            <Text style={font_styles.primary_text}>
                                                {strings.activeBatchItem.from}:
                                            </Text>
                                            <Text
                                                style={[
                                                    font_styles.primary_text_bold,
                                                    { marginLeft: moderateScale(5) },
                                                ]}>
                                                {moment(item.dateFrom).format('D/M/YYYY')}
                                            </Text>
                                        </View>
                                        <View style={styles.dataRow}>
                                            <Text style={font_styles.primary_text}>
                                                {strings.activeBatchItem.to}:
                                            </Text>
                                            <Text
                                                style={[
                                                    font_styles.primary_text_bold,
                                                    { marginLeft: moderateScale(5) },
                                                ]}>
                                                {moment(item.dateTo).format('D/M/YYYY')}
                                            </Text>
                                        </View>
                                    </View>
                                </View>
                            </View>
                        </RectButton>
                    </Animated.View>
                    <Animated.View
                        style={{
                            opacity: smallerCardOpacity,
                            position: 'absolute',
                            height: moderateScale(50),
                            width: width,
                        }}>
                        <View style={styles.smallCard}>
                            <View style={styles.smallCardBar} />
                            <Text
                                style={
                                    font_styles.title_3_bold
                                }>{`${strings.activeBatchItem.active_batch} ${item.id}`}</Text>
                            <Icon
                                name={Platform.OS === 'ios' ? 'ios-cart' : 'md-cart'}
                                size={moderateScale(24)}
                                color={Colors.statusApproved}
                                style={{ paddingRight: moderateScale(12) }}
                            />
                        </View>
                    </Animated.View>
                </Animated.View>
            </PanGestureHandler>
        );
    }
}

const styles = StyleSheet.create({
    container: {
        padding: moderateScale(14),
        backgroundColor: Colors.appBackground,
        borderBottomWidth: moderateScale(0.5),
        borderBottomColor: Colors.logoTextInactive,
        //height: verticalScale(120)
    },
    card: {
        borderRadius: moderateScale(6),
        backgroundColor: Colors.white,
        elevation: 1,
        shadowColor: Colors.primaryText,
        shadowOffset: {
            width: 0,
            height: 1,
        },
        shadowOpacity: 0.18,
        shadowRadius: 1.0,
        borderColor: Colors.statusApproved,
        borderWidth: moderateScale(1),
    },
    smallCardBar: {
        height: moderateScale(50),
        width: moderateScale(10),
        backgroundColor: Colors.statusApproved,
    },
    smallCard: {
        flex: 1,
        display: 'flex',
        flexDirection: 'row',
        paddingVertical: verticalScale(8),
        //paddingHorizontal: moderateScale(12),
        alignItems: 'center',
        justifyContent: 'space-between',
    },
    cardContent: {
        paddingVertical: verticalScale(12),
        paddingHorizontal: moderateScale(20),
    },
    cardContentHeader: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'space-between',
        marginBottom: moderateScale(16),
    },
    cardContentBody: {},
    dataRow: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'space-between',
    },
});
