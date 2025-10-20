import React from 'react';
import { StyleSheet, View, Dimensions, Animated, Platform } from 'react-native';
import { verticalScale, moderateScale } from '../../lib/size-normalizer';
import * as Colors from '../../constants/Colors';

const { width } = Dimensions.get('screen');

const NAV_BAR_HEIGHT_ANDROID = verticalScale(56);
const NAV_BAR_HEIGHT_IOS = verticalScale(64);
const STATUS_BAR_IOS = verticalScale(25);

const HeaderWrapper = ({ children, style, animationStyle, titleCentered }) => {
    const leftButtons = [];
    const rightButtons = [];
    let title = null;

    React.Children.forEach(children, (child, index) => {
        if (child) {
            if (child.props.type === 'RIGHT') {
                rightButtons.push(
                    <View
                        key={`${child.key}_${index}}`}
                        style={rightButtons.length >= 1 ? styles.rightButton : null}>
                        {child}
                    </View>
                );
            } else if (child.props.type === 'LEFT') leftButtons.push(child);
            else title = child;
        }
    });

    return (
        <React.Fragment>
            <Animated.View style={[styles.animatedView, animationStyle]}>
                <View style={[styles.navigationBar, style]}>
                    <View style={[leftButtons.length === 0 ? null : styles.leftButtonGroup]}>
                        {leftButtons}
                    </View>
                    <View
                        style={[
                            styles.title,
                            titleCentered ? { alignItems: 'center' } : { alignItems: 'flex-start' },
                        ]}>
                        {title}
                    </View>
                    <View style={styles.rightButtonGroup}>{rightButtons}</View>
                </View>
            </Animated.View>
        </React.Fragment>
    );
};

const styles = StyleSheet.create({
    navigationBar: {
        height: Platform.OS === 'ios' ? NAV_BAR_HEIGHT_IOS : NAV_BAR_HEIGHT_ANDROID,
        //marginTop: Platform.OS === 'ios' ? STATUS_BAR_IOS : StatusBar.currentHeight,
        paddingTop: Platform.OS === 'ios' ? STATUS_BAR_IOS : 0,
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        backgroundColor: Colors.white,
        paddingHorizontal: moderateScale(16),
    },
    leftButtonGroup: {
        marginRight: moderateScale(32),
    },
    rightButtonGroup: {
        //marginLeft: moderateScale(32),
        flexDirection: 'row',
        justifyContent: 'flex-end',
    },
    rightButton: {
        marginLeft: moderateScale(24),
    },
    title: {
        flex: 1,
    },
    animatedView: {
        width: width,
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
    },
});

export { NAV_BAR_HEIGHT_ANDROID, NAV_BAR_HEIGHT_IOS, STATUS_BAR_IOS };
export default React.memo(HeaderWrapper);
