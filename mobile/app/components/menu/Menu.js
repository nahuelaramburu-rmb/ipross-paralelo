import React, { forwardRef, useImperativeHandle, useRef, useReducer } from 'react';
import {
    View,
    Modal,
    TouchableWithoutFeedback,
    StyleSheet,
    Easing,
    Animated,
    Platform,
    Dimensions,
    StatusBar,
} from 'react-native';
import PropTypes from 'prop-types';
import { moderateScale } from '../../lib/size-normalizer';

const { width: windowWidth, height } = Dimensions.get('window');
const windowHeight = height - (StatusBar.currentHeight || 0);

const EASING = Easing.bezier(0.4, 0, 0.2, 1);
const SCREEN_INDENT = 8;

const MODAL_STATES = {
    HIDDEN: 'HIDDEN',
    ANIMATING: 'ANIMATING',
    OPEN: 'OPEN',
};

const MODAL_REDUCER_ACTIONS = {
    OPEN_MODAL: 'OPEN_MODAL',
    CLOSE_MODAL: 'CLOSE_MODAL',
    ANIMATE_MODAL: 'ANIMATE_MODAL',
};

const menuInitialState = {
    left: 0,
    top: 0,
    buttonWidth: 0,
    buttonHeight: 0,
    menuWidth: 0,
    menuHeight: 0,
    modalStatus: MODAL_STATES.HIDDEN,
};

const menuReducer = (state, action) => {
    switch (action.type) {
        case MODAL_REDUCER_ACTIONS.OPEN_MODAL:
        case MODAL_REDUCER_ACTIONS.CLOSE_MODAL:
        case MODAL_REDUCER_ACTIONS.ANIMATE_MODAL:
            return {
                ...state,
                ...action,
            };
        default:
            throw new Error(`${action.type} is not supported by this reducer`);
    }
};

const Menu = forwardRef(({ button, children, style }, ref) => {
    const containerRef = useRef();
    const [state, dispatch] = useReducer(menuReducer, menuInitialState);

    const menuSizeAnim = useRef(new Animated.ValueXY({ x: 0, y: 0 }));
    const menuOpacityAnim = useRef(new Animated.Value(0));

    const { modalStatus, buttonWidth, menuWidth, menuHeight, buttonHeight } = state;
    let { top, left } = state;

    useImperativeHandle(ref, () => ({
        openModal: handleOpenModal,
        hideModal: handleHideModal,
    }));

    const handleOpenModal = () => {
        containerRef.current.measureInWindow((left, top, buttonWidth, buttonHeight) => {
            dispatch({
                type: MODAL_REDUCER_ACTIONS.OPEN_MODAL,
                modalStatus: MODAL_STATES.OPEN,
                left,
                top,
                buttonWidth,
                buttonHeight,
            });
        });
    };

    const handleHideModal = () => {
        Animated.timing(menuOpacityAnim.current, {
            toValue: 0,
            duration: 300,
            easing: EASING,
            useNativeDriver: false,
        }).start(() => {
            dispatch({ type: MODAL_REDUCER_ACTIONS.CLOSE_MODAL, modalStatus: MODAL_STATES.HIDDEN });
            menuSizeAnim.current.setValue({ x: 0, y: 0 });
            menuOpacityAnim.current.setValue(0);
        });
    };

    const handleMenuLayout = (e) => {
        if (modalStatus === MODAL_STATES.ANIMATING) return;

        const { width: menuWidth, height: menuHeight } = e.nativeEvent.layout;
        dispatch({
            type: MODAL_REDUCER_ACTIONS.ANIMATE_MODAL,
            menuWidth,
            menuHeight,
            modalStatus: MODAL_STATES.ANIMATING,
        });

        Animated.parallel([
            Animated.timing(menuSizeAnim.current, {
                toValue: { x: menuWidth, y: menuHeight },
                duration: 300,
                easing: EASING,
                useNativeDriver: false,
            }),
            Animated.timing(menuOpacityAnim.current, {
                toValue: 1,
                duration: 300,
                easing: EASING,
                useNativeDriver: false,
            }),
        ]).start();
    };

    const menuSize = {
        width: menuSizeAnim.current.x,
        height: menuSizeAnim.current.y,
    };

    const transforms = [];
    if (left + buttonWidth - menuWidth > SCREEN_INDENT || left + menuWidth > windowWidth - SCREEN_INDENT) {
        transforms.push({
            translateX: Animated.multiply(menuSizeAnim.current.x, -1),
        });

        left = Math.min(windowWidth - SCREEN_INDENT, left + buttonWidth);
    } else if (left < SCREEN_INDENT) {
        left = SCREEN_INDENT;
    }

    if (top > windowHeight - menuHeight - SCREEN_INDENT) {
        transforms.push({
            translateY: Animated.multiply(menuSizeAnim.current.y, -1),
        });

        top = Math.min(windowHeight - SCREEN_INDENT, top + buttonHeight);
    } else if (top < SCREEN_INDENT) {
        top = SCREEN_INDENT;
    }

    const shadowMenuContainerStyle = {
        opacity: menuOpacityAnim.current,
        transform: transforms,
        top,
        left,
    };

    const animationStarted = modalStatus === MODAL_STATES.ANIMATING;
    const modalVisible = modalStatus === MODAL_STATES.OPEN || animationStarted;

    return (
        <View ref={containerRef} collapsable={false}>
            {button}
            <Modal
                transparent={true}
                supportedOrientations={['portrait', 'portrait-upside-down']}
                onRequestClose={handleHideModal}
                visible={modalVisible}>
                <TouchableWithoutFeedback onPress={handleHideModal}>
                    <View style={styles.container}>
                        <Animated.View
                            onLayout={handleMenuLayout}
                            style={[styles.containerShadow, shadowMenuContainerStyle, style]}>
                            <Animated.View style={[styles.containerHidden, animationStarted && menuSize]}>
                                {children}
                            </Animated.View>
                        </Animated.View>
                    </View>
                </TouchableWithoutFeedback>
            </Modal>
        </View>
    );
});

Menu.propTypes = {
    button: PropTypes.element,
    children: PropTypes.node,
    style: PropTypes.object,
};

const styles = StyleSheet.create({
    container: {
        ...StyleSheet.absoluteFill,
    },
    containerShadow: {
        position: 'absolute',
        backgroundColor: 'white',
        borderRadius: moderateScale(4),
        opacity: 0,
        ...Platform.select({
            ios: {
                shadowColor: 'black',
                shadowOffset: { width: 0, height: 2 },
                shadowOpacity: 0.14,
                shadowRadius: 2,
            },
            android: {
                elevation: 8,
            },
        }),
    },
    containerHidden: {
        overflow: 'hidden',
    },
});

Menu.propTypes = {
    button: PropTypes.element,
};

export default Menu;
