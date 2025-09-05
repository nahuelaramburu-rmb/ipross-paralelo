import React, {
    useReducer,
    useRef,
    cloneElement,
    useState,
    useCallback,
    forwardRef,
    useImperativeHandle,
    memo,
} from 'react';
import { View, FlatList, StyleSheet, Platform, Animated, Modal, Dimensions } from 'react-native';
import Icon from 'react-native-vector-icons/Ionicons';
import * as Colors from '../../constants/Colors';
import { moderateScale } from '../../lib/size-normalizer';
import TextField from '../TextField';
import { OPTION_HEIGHT } from './Option';
import PropTypes from 'prop-types';
import ExtraDimensions from 'react-native-extra-dimensions-android';
import { BaseButton } from 'react-native-gesture-handler';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

const { height: iosHeight } = Dimensions.get('window');
const wheight =
    Platform.OS === 'ios'
        ? iosHeight
        : ExtraDimensions.getRealWindowHeight() - ExtraDimensions.getSoftMenuBarHeight();

const ANIM_DURATION = 225;
const ITEMS_TO_SHOW = 3;
const ITEM_PADDING = 8;

const dropdownInitialState = {
    height: 0,
    width: 0,
    left: 0,
    top: 0,
    modalVisible: false,
};

const actionTypes = {
    SET_MODAL_DIMENSIONS: 'SET_MODAL_DIMENSIONS',
    OPEN_DROPDOWN: 'OPEN_DROPDOWN',
    CLOSE_DROPDOWN: 'CLOSE_DROPDOWN',
};

const dropdownReducer = (state = dropdownInitialState, action) => {
    switch (action.type) {
        case actionTypes.SET_MODAL_DIMENSIONS:
            return {
                ...state,
                height: action.height,
                width: action.width,
            };
        case actionTypes.OPEN_DROPDOWN:
            return {
                ...state,
                modalVisible: true,
                top: action.top,
                left: action.left,
                height: action.height,
                width: action.width,
            };
        case actionTypes.CLOSE_DROPDOWN:
            return {
                ...state,
                modalVisible: false,
            };
        default:
            return state;
    }
};

const DropdownModal = forwardRef(
    ({ visible, handleCloseDropdown, pickerStyle, children, onItemSelect, overlayStyle, value }, ref) => {
        let modalHeight;
        if (children.length >= ITEMS_TO_SHOW) {
            modalHeight = OPTION_HEIGHT * ITEMS_TO_SHOW + 2 * ITEM_PADDING;
        } else {
            modalHeight = OPTION_HEIGHT * children.length + 2 * ITEM_PADDING;
        }

        const handleItemSelect = (selectedItem) => {
            onItemSelect(selectedItem);
        };

        const renderItem = ({ item, index }) => {
            let selected = false;
            
            if (item.props.id === value) selected = true;
            return cloneElement(item, { onItemSelect: handleItemSelect, index, selected });
        };

        const filteredChildren = React.Children.map(children, (child) => {
            if (child && typeof child.type === 'function') return child;
        });


        return (
            <Modal
                visible={visible}
                transparent={true}
                onRequestClose={handleCloseDropdown}
                supportedOrientations={['portrait', 'portrait-upside-down']}>
                <Animated.View
                    style={[styles.modalOverlay, overlayStyle]}
                    onResponderRelease={handleCloseDropdown}
                    onStartShouldSetResponder={() => true}>
                    <View
                        style={[styles.modalContainer, pickerStyle, { height: modalHeight }]}
                        onStartShouldSetResponder={() => true}>
                        <FlatList
                            ref={ref}
                            data={filteredChildren}
                            style={styles.list}
                            renderItem={renderItem}
                            scrollEnabled={true}
                            keyExtractor={(item, index) => item.props.id.toString()}
                            contentContainerStyle={styles.listContainer}
                        />
                    </View>
                </Animated.View>
            </Modal>
        );
    }
);

const MemoizedDropdownModal = memo(DropdownModal);

const Dropdown = forwardRef(
    (
        {
            leftIcon,
            children,
            value: initialValue = null,
            onChangeText,
            onBlur,
            rightIcon,
            disabled,
            ...props
        },
        ref
    ) => {
        const [dropdownState, dispatch] = useReducer(dropdownReducer, dropdownInitialState);
        const [value, setValue] = useState(initialValue);
        const dropdownRef = useRef(null);
        const flatListRef = useRef(null);
        const opacityAnim = useRef(new Animated.Value(0)).current;
        const insets = useSafeAreaInsets();

        const childObjs = React.Children.map(children, (child) => {
            if (child) {
                return {
                    id: child.props.id,
                    label: child.props.children,
                };
            }
        });

        const textInputLabel = childObjs.find((it) => it.id === value)?.label ?? '';

        const { modalVisible, left, top, width, height } = dropdownState;

        const resetScrollOffset = () => {
            if (!flatListRef.current) return;
            let offset = 0;
            const position = childObjs.findIndex((it) => it.id === value);
            if (position > -1) offset = OPTION_HEIGHT * position;
            flatListRef.current.scrollToOffset({ offset: offset, animated: false });
        };

        const handleOpenDropdown = () => {
            if (!childObjs.length) return;

            dropdownRef.current.measureInWindow((x, y, width, height) => {
                dispatch({ type: actionTypes.OPEN_DROPDOWN, top: y, left: x, width, height });

                setTimeout(() => {
                    resetScrollOffset();
                    Animated.timing(opacityAnim, {
                        duration: ANIM_DURATION,
                        toValue: 1,
                        useNativeDriver: true,
                    }).start();
                }, 0);
            });
        };

        const handleItemSelect = useCallback(
            (selectedItem) => {
                setValue(selectedItem.id);

                if (onChangeText) onChangeText(selectedItem.id);

                handleCloseDropdown();
            },
            [handleCloseDropdown, onChangeText]
        );

        const handleCloseDropdown = useCallback(() => {
            Animated.timing(opacityAnim, {
                duration: ANIM_DURATION,
                toValue: 0,
                useNativeDriver: true,
            }).start(() => {
                dispatch({ type: actionTypes.CLOSE_DROPDOWN });

                if (onBlur) onBlur();
            });
        }, [opacityAnim, dispatch, onBlur]);

        useImperativeHandle(ref, () => ({
            focus: handleOpenDropdown,
        }));

        let translateY = 0;
        if (top + height > wheight) {
            translateY = wheight - (top + height) - insets.bottom;
        }

        return (
            <View ref={dropdownRef} collapsable={false}>
                <BaseButton onPress={!disabled ? handleOpenDropdown : null}>
                    <View pointerEvents='box-only'>
                        <TextField
                            value={textInputLabel}
                            editable={false}
                            disabled={disabled}
                            leftIcon={leftIcon}
                            rightIcon={
                                rightIcon || (
                                    <Icon
                                        name='ios-chevron-down'
                                        color={!disabled ? Colors.accent : Colors.darkDividerLine}
                                        size={moderateScale(20)}
                                    />
                                )
                            }
                            {...props}
                        />
                    </View>
                </BaseButton>
                <MemoizedDropdownModal
                    ref={flatListRef}
                    onItemSelect={handleItemSelect}
                    visible={modalVisible}
                    handleCloseDropdown={handleCloseDropdown}
                    pickerStyle={{ left, top, width, transform: [{ translateY: translateY }] }}
                    value={value}
                    overlayStyle={{ opacity: opacityAnim }}>
                    {children}
                </MemoizedDropdownModal>
            </View>
        );
    }
);

DropdownModal.propTypes = {
    visible: PropTypes.bool,
    handleCloseDropdown: PropTypes.func,
    pickerStyle: PropTypes.object,
    children: PropTypes.node,
    onItemSelect: PropTypes.func,
    overlayStyle: PropTypes.object,
    value: PropTypes.oneOfType([PropTypes.number, PropTypes.string]),
};

Dropdown.propTypes = {
    leftIcon: PropTypes.node,
    children: PropTypes.node,
    value: PropTypes.oneOfType([PropTypes.number, PropTypes.string]),
    onChangeText: PropTypes.func,
    onBlur: PropTypes.func,
    rightIcon: PropTypes.node,
    disabled: PropTypes.bool,
};

const styles = StyleSheet.create({
    modalOverlay: {
        ...StyleSheet.absoluteFill,
    },
    modalContainer: {
        backgroundColor: Colors.white,
        borderRadius: 2,
        //height: verticalScale(100),

        position: 'absolute',

        ...Platform.select({
            ios: {
                shadowRadius: 2,
                shadowColor: 'rgba(0, 0, 0, 1.0)',
                shadowOpacity: 0.54,
                shadowOffset: { width: 0, height: 2 },
            },

            android: {
                elevation: 2,
            },
        }),
    },
    list: {
        flex: 1,
    },
    listContainer: {
        paddingVertical: ITEM_PADDING,
    },
});

export default memo(Dropdown);
