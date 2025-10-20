import PropTypes from 'prop-types';
import React, { PureComponent } from 'react';
import { StyleSheet, Text, View, Dimensions, Animated, Platform } from 'react-native';
import * as Colors from '../constants/Colors';
import { font_styles } from '../lib/default-styles';
import { moderateScale, verticalScale } from '../lib/size-normalizer';
import ExtraDimensions from 'react-native-extra-dimensions-android';
import { TouchableWithoutFeedback } from 'react-native-gesture-handler';

const { width, height: iosHeight } = Dimensions.get('screen');
const height = Platform.OS === 'ios' ? iosHeight : ExtraDimensions.getRealWindowHeight();

export default class ImageCard extends PureComponent {
    static propTypes = {
        style: PropTypes.object,
        image: PropTypes.element.isRequired,
        header: PropTypes.string.isRequired,
        title: PropTypes.string.isRequired,
    };

    constructor(props) {
        super(props);
        this.state = {
            animation: {
                x: new Animated.Value(-width),
            },
        };
    }

    componentDidMount() {
        const randomDuration = Math.floor(Math.random() * 501) + 750;
        Animated.timing(this.state.animation.x, {
            toValue: 0,
            duration: randomDuration,
            useNativeDriver: true,
        }).start();
    }

    render() {
        const { onPress, style, image, header, title } = this.props;
        return (
            <Animated.View style={[style, { transform: [{ translateX: this.state.animation.x }] }]}>
                <TouchableWithoutFeedback onPress={onPress ? onPress : null}>
                    <View style={[styles.container]}>
                        {onPress ? <View style={styles.mark} /> : null}
                        <View style={{ alignItems: 'center' }}>
                            {image}
                            <Text
                                style={[
                                    font_styles.subtitle,
                                    { color: Colors.accent, paddingVertical: verticalScale(5) },
                                ]}>
                                {header}
                            </Text>
                        </View>
                        <Text
                            numberOfLines={2}
                            ellipsizeMode='tail'
                            style={[font_styles.title_3_bold, { textAlign: 'center' }]}>
                            {title}
                        </Text>
                    </View>
                </TouchableWithoutFeedback>
            </Animated.View>
        );
    }
}

const styles = StyleSheet.create({
    container: {
        width: '100%',
        justifyContent: 'space-between',
        alignItems: 'center',
        backgroundColor: Colors.white,
        alignSelf: 'center',
        flexDirection: 'column',
        padding: moderateScale(16),
        flex: 1,
        shadowColor: Colors.primaryText,
        shadowOffset: {
            width: 0,
            height: 1,
        },
        shadowOpacity: 0.18,
        shadowRadius: 1.0,
        elevation: 1,
        borderRadius: moderateScale(4),
    },
    mark: {
        width: moderateScale(10),
        height: moderateScale(10),
        borderRadius: moderateScale(5),
        backgroundColor: Colors.accent,
        position: 'absolute',
        top: 10,
        right: 10,
    },
});
