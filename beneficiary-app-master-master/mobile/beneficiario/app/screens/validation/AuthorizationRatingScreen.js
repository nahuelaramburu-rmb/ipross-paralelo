import React, { useCallback, useEffect, useLayoutEffect, useRef, useState } from 'react';
import { View, Text, StyleSheet, Image, Animated, ScrollView } from 'react-native';
import * as Colors from '../../constants/Colors';
import { moderateScale, verticalScale } from '../../lib/size-normalizer';
import { font_styles } from '../../lib/default-styles';
import { useNavigation } from '@react-navigation/native';
import strings from '../../constants/Strings';
import { shallowEqual, useDispatch, useSelector } from 'react-redux';
import { useFetchLoading } from '../../hooks/utils';
import { rankAuthorization } from '../../actions/validationAction';
import { DropDownHolder } from '../../components/DropDownHolder';
import { SafeAreaView } from 'react-native-safe-area-context';
import Dropdown, { Option } from '../../components/dropdown';
import * as Yup from 'yup';
import { useFormik } from 'formik';
import PropTypes from 'prop-types';

let STAR_IMAGE;

const STAR_HEIGHT = Math.trunc(moderateScale(48));
const STAR_WIDTH = Math.trunc(moderateScale(48));
const STAR_COUNT = 5;
const STARS_WIDTH = STAR_COUNT * STAR_WIDTH;

const MAX_RATING = 15;

const ratings = {
    veryGood: {
        value: 'veryGood',
        label: 'Muy Buena',
        rating: 5,
    },
    good: {
        value: 'good',
        label: 'Buena',
        rating: 4,
    },
    regular: {
        value: 'regular',
        label: 'Regular',
        rating: 3,
    },
    bad: {
        value: 'bad',
        label: 'Mala',
        rating: 2,
    },
    veryBad: {
        value: 'veryBad',
        label: 'Muy Mala',
        rating: 1,
    },
};

const RatingSchema = Yup.object().shape({
    attentionQuality: Yup.string().required(strings.errors.required),
    attentionTime: Yup.string().required(strings.errors.required),
    awaitingTime: Yup.string().required(strings.errors.required),
    extraBudget: Yup.string().required(strings.errors.required),
});

const RatingStars = ({ style, value }) => {
    const [isMounted, setIsMounted] = useState(false);
    const selectedStarsWidth = useRef(new Animated.Value(-STARS_WIDTH)).current;

    const valueSum = value.reduce((prev, current) => {
        if (current) return (prev += ratings[current].rating);
        else return (prev += 0);
    }, 0);

    const avg = (valueSum * 100) / MAX_RATING;

    const width = avg ? (avg * STARS_WIDTH) / 100 : 0;

    Animated.timing(selectedStarsWidth, {
        toValue: -STARS_WIDTH + width,
        duration: 500,
        useNativeDriver: true,
    }).start();

    const rating = Object.values(ratings).find((it) => it.rating === Math.ceil(valueSum / value.length));

    const title = rating ? rating.label : null;

    useLayoutEffect(() => {
        const loadImage = async () => {
            try {
                STAR_IMAGE = await require('../../images/star.png');
                setIsMounted(true);
            } catch (err) {
                console.log(err);
            }
        };

        loadImage();
    }, []);

    if (!isMounted) return null;

    return (
        <View style={[styles.starsContainer, style]}>
            <View style={styles.starsWrapper}>
                <View style={styles.stars}>
                    <Animated.View
                        style={[styles.overlay, { transform: [{ translateX: selectedStarsWidth }] }]}
                    />
                    {Array(5)
                        .fill(null)
                        .map((_, index) => (
                            <Image key={index} source={STAR_IMAGE} style={styles.star} />
                        ))}
                </View>
                <Text style={[font_styles.title_3_bold, { color: Colors.statusPending }]}>{title}</Text>
            </View>
        </View>
    );
};

const RatingForm = ({ values, errors, touched, setFieldValue }) => {
    const attentionQualityRef = useRef(null);
    const attentionTimeRef = useRef(null);
    const awaitingTimeRef = useRef(null);
    const extraBudgetRef = useRef(null);
    const scrollViewRef = useRef(null);

    const [cardHeight, setCardHeight] = useState(null);

    const { authorization } = useSelector(
        (state) => ({
            authorization: state.validation.selectedAuthorization.item,
        }),
        shallowEqual
    );

    const cannotRate = !!authorization.rating;

    const handleAttentionQualityChange = (text) => {
        setFieldValue('attentionQuality', text);
        scrollViewRef.current?.scrollTo({ y: 1 * cardHeight });
    };

    const handleAttentionTimeChange = (text) => {
        setFieldValue('attentionTime', text);
        scrollViewRef.current?.scrollTo({ y: 2 * cardHeight });
    };

    const handleAwaitingTimeChange = (text) => {
        setFieldValue('awaitingTime', text);
        scrollViewRef.current?.scrollTo({ y: 3 * cardHeight });
    };

    const handleExtraBudgetChange = (text) => {
        setFieldValue('extraBudget', text);
    };

    const handleCardLayout = (e) => {
        const { height } = e.nativeEvent.layout;
        setCardHeight(height);
    };

    return (
        <View style={styles.formContainer}>
            <ScrollView scrollEventThrottle={16} showsVerticalScrollIndicator={false} ref={scrollViewRef}>
                <View style={styles.item} onLayout={handleCardLayout}>
                    <Text style={[font_styles.title_3_bold, styles.cardTitle]}>
                        {strings.authorizationRanking.attention_quality}
                    </Text>
                    <View style={styles.divider} />
                    <View style={styles.inputContainer}>
                        <Dropdown
                            value={values.attentionQuality}
                            leftIcon='ios-star'
                            onChangeText={handleAttentionQualityChange}
                            ref={attentionQualityRef}
                            disabled={cannotRate}
                            placeholder={strings.authorizationRanking.rating}
                            error={errors.attentionQuality}
                            touched={touched.attentionQuality}>
                            {Object.values(ratings).map((it) => (
                                <Option key={it.value} id={it.value}>
                                    {it.label}
                                </Option>
                            ))}
                        </Dropdown>
                    </View>
                </View>
                <View style={styles.item}>
                    <Text style={[font_styles.title_3_bold, styles.cardTitle]}>
                        {strings.authorizationRanking.attention_duration}
                    </Text>
                    <View style={styles.divider} />
                    <View style={styles.inputContainer}>
                        <Dropdown
                            value={values.attentionTime}
                            leftIcon='ios-star'
                            onChangeText={handleAttentionTimeChange}
                            ref={attentionTimeRef}
                            disabled={cannotRate}
                            placeholder={strings.authorizationRanking.rating}
                            error={errors.attentionTime}
                            touched={touched.attentionTime}>
                            {Object.values(ratings).map((it) => (
                                <Option key={it.value} id={it.value}>
                                    {it.label}
                                </Option>
                            ))}
                        </Dropdown>
                    </View>
                </View>
                <View style={styles.item}>
                    <Text style={[font_styles.title_3_bold, styles.cardTitle]}>
                        {strings.authorizationRanking.wait_time}
                    </Text>
                    <View style={styles.divider} />
                    <View style={styles.inputContainer}>
                        <Dropdown
                            value={values.awaitingTime}
                            leftIcon='ios-star'
                            onChangeText={handleAwaitingTimeChange}
                            ref={awaitingTimeRef}
                            disabled={cannotRate}
                            placeholder={strings.authorizationRanking.rating}
                            error={errors.awaitingTime}
                            touched={touched.awaitingTime}>
                            {Object.values(ratings).map((it) => (
                                <Option key={it.value} id={it.value}>
                                    {it.label}
                                </Option>
                            ))}
                        </Dropdown>
                    </View>
                </View>
                <View style={styles.item}>
                    <Text style={[font_styles.title_3_bold, styles.cardTitle]}>
                        {strings.authorizationRanking.additional_payed}
                    </Text>
                    <View style={styles.divider} />
                    <View style={styles.inputContainer}>
                        <Dropdown
                            value={values.extraBudget}
                            leftIcon='ios-star'
                            onChangeText={handleExtraBudgetChange}
                            ref={extraBudgetRef}
                            disabled={cannotRate}
                            placeholder={strings.authorizationRanking.rating}
                            error={errors.extraBudget}
                            touched={touched.extraBudget}>
                            {Object.values(ratings)
                                .filter((it) => it.rating === 5 || it.rating === 1)
                                .map((it) => (
                                    <Option key={it.value} id={it.value}>
                                        {it.rating === 5 ? 'No' : 'Sí'}
                                    </Option>
                                ))}
                        </Dropdown>
                    </View>
                </View>
            </ScrollView>
        </View>
    );
};

const AuthorizationRatingScreen = () => {
    const navigation = useNavigation();
    const dispatch = useDispatch();

    const { authorization } = useSelector(
        (state) => ({
            authorization: state.validation.selectedAuthorization.item,
        }),
        shallowEqual
    );

    let authorizationRating = {};

    if (authorization.rating) {
        for (const prop in authorization.rating) {
            authorizationRating[prop] =
                Object.values(ratings).find((it) => it.rating === authorization.rating[prop])?.value ?? null;
        }
    }

    const handleOnConfirm = (values) => {
        /**
         * Se trabaja en un workaround para que si el rating en el extraBudget es 1, pongamos un 0,
         * asi luego en la web en base a eso podemos sacar un promedio basado en el rating y la cantidad de calificaciones totales.
         */
        const ranks = {
            quality: ratings[values.attentionQuality].rating,
            waitTime: ratings[values.awaitingTime].rating,
            charges: ratings[values.extraBudget].rating === 1 ? 0 : ratings[values.extraBudget].rating,
            duration: ratings[values.attentionTime].rating,
        };

        execFn(authorization.id, ranks)
            .then(() => {
                DropDownHolder.alert(
                    'success',
                    'Éxito',
                    strings.authorizationRanking.rating_successfully_saved
                );
            })
            .catch((err) => console.log(err));
    };

    const { handleSubmit, errors, touched, values, setFieldValue } = useFormik({
        validationSchema: RatingSchema,
        initialValues: {
            attentionQuality: authorizationRating.quality || '',
            attentionTime: authorizationRating.duration || '',
            awaitingTime: authorizationRating.waitTime || '',
            extraBudget: authorizationRating.charges || '',
        },
        onSubmit: handleOnConfirm,
    });

    const handleAsyncOperation = useCallback((id, ranks) => dispatch(rankAuthorization(id, ranks)), [
        dispatch,
    ]);

    const [execFn, isLoading] = useFetchLoading(handleAsyncOperation);

    useEffect(() => {
        navigation.setParams({
            cannotRate: !!authorization.rating,
            onConfirm: handleSubmit,
            isLoading: isLoading,
        });
    }, [navigation, handleSubmit, isLoading, authorization]);

    return (
        <SafeAreaView style={styles.safeArea}>
            <View style={styles.container}>
                <RatingStars
                    style={styles.ratingStars}
                    value={[values.attentionQuality, values.attentionTime, values.awaitingTime]}
                />
                <RatingForm values={values} errors={errors} touched={touched} setFieldValue={setFieldValue} />
            </View>
        </SafeAreaView>
    );
};

RatingStars.propTypes = {
    style: PropTypes.object,
    value: PropTypes.array,
};

RatingForm.propTypes = {
    values: PropTypes.object,
    errors: PropTypes.object,
    touched: PropTypes.object,
    setFieldValue: PropTypes.func,
};

const styles = StyleSheet.create({
    safeArea: {
        flex: 1,
    },
    container: {
        flex: 1,
        backgroundColor: Colors.appBackground,
    },
    ratingStars: {
        flex: 0.3,
    },
    starsContainer: {
        alignItems: 'center',
        justifyContent: 'center',
        borderBottomColor: Colors.lightDividerLine,
        borderBottomWidth: 0.5,
        backgroundColor: Colors.white,
    },
    formContainer: {
        flex: 1,
        paddingHorizontal: moderateScale(24),
        flexDirection: 'column',
        paddingTop: verticalScale(12),
    },
    startsWrapper: {
        position: 'absolute',
        left: 0,
        right: 0,
        top: 0,
        bottom: 0,
        flexDirection: 'row',
        justifyContent: 'center',
        alignItems: 'center',
    },
    overlay: {
        height: STAR_HEIGHT,
        position: 'absolute',
        left: 0,
        right: 0,
        top: 0,
        bottom: 0,
        backgroundColor: Colors.statusPending,
    },
    star: {
        height: STAR_HEIGHT,
        width: STAR_WIDTH,
    },
    item: {
        width: '100%',
        padding: moderateScale(12),
        borderRadius: moderateScale(6),
        backgroundColor: Colors.white,
        paddingVertical: verticalScale(12),
        elevation: 1,
        marginBottom: moderateScale(10),
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
    },
    starsWrapper: {
        alignItems: 'center',
        flexDirection: 'column',
        overflow: 'hidden',
    },
    stars: {
        flexDirection: 'row',
        marginVertical: moderateScale(12),
        width: STARS_WIDTH,
    },
    dropdownLabel: {
        paddingTop: 1,
    },
    cardTitle: {
        marginBottom: verticalScale(12),
    },
    inputContainer: {
        marginTop: verticalScale(12, 0.25),
    },
});

export default AuthorizationRatingScreen;
