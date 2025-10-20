import { useIsFocused, useNavigation } from '@react-navigation/native';
import { useCallback, useRef, useState, useEffect } from 'react';
import { Animated, Platform } from 'react-native';
import { NAV_BAR_HEIGHT_ANDROID, NAV_BAR_HEIGHT_IOS } from '../components/header/HeaderWrapper';

export const useFetchLoading = (promise) => {
    const [isLoading, setIsLoading] = useState(false);

    const execFn = useCallback(
        async (...args) => {
            try {
                setIsLoading(true);
                const result = await promise(...args);
                return result;
            } catch (err) {
                throw err;
            } finally {
                setIsLoading(false);
            }
        },
        [promise]
    );

    return [execFn, isLoading];
};

export const useIsMounted = () => {
    const mountedRef = useRef(true);
    const isMounted = useCallback(() => mountedRef.current, []);

    useEffect(() => {
        return () => {
            mountedRef.current = false;
        };
    }, []);

    return isMounted;
};

export const useAnimatableHeader = () => {
    const navigation = useNavigation();
    const isFocused = useIsFocused();
    const headerHeightAnim = useRef(new Animated.Value(0));

    const interpolatedHeightRef = useRef(
        headerHeightAnim.current.interpolate({
            inputRange: [0, Platform.OS === 'android' ? NAV_BAR_HEIGHT_ANDROID : NAV_BAR_HEIGHT_IOS],
            outputRange: [0, Platform.OS === 'android' ? -NAV_BAR_HEIGHT_ANDROID : -NAV_BAR_HEIGHT_IOS],
            extrapolate: 'clamp',
        })
    );

    const interpolatedOpacityRef = useRef(
        headerHeightAnim.current.interpolate({
            inputRange: [0, Platform.OS === 'android' ? NAV_BAR_HEIGHT_ANDROID : NAV_BAR_HEIGHT_IOS],
            outputRange: [1, 0],
            extrapolate: 'clamp',
        })
    );

    const setNavParams = useCallback(() => {
        navigation.setParams({
            animatedValueHeight: interpolatedHeightRef.current,
            animatedValueOpacity: interpolatedOpacityRef.current,
        });
    }, [navigation]);

    useEffect(() => {
        if (isFocused) setNavParams();
    }, [isFocused, setNavParams]);

    const onScroll = Animated.event(
        [
            {
                nativeEvent: {
                    contentOffset: { y: headerHeightAnim.current },
                },
            },
        ],
        { useNativeDriver: true }
    );

    return { interpolatedHeight: interpolatedHeightRef.current, onScroll };
};

export const useFetchPolling = (fn, delay = 10000) => {
    const fnRef = useRef(fn);

    useEffect(() => {
        fnRef.current = fn;
    }, [fn]);

    useEffect(() => {
        const tick = () => fnRef.current();
        const intervalId = setInterval(tick, delay);
        return () => {
            clearInterval(intervalId);
        };
    }, [delay]);
};
