import PropTypes from 'prop-types';
import React, { Component } from 'react';
import { StyleSheet, Text, View, Dimensions, Animated } from 'react-native';

import * as Colors from '../constants/Colors';

const { width, height } = Dimensions.get('window');
import { font_styles } from '../lib/default-styles';

import { moderateScale } from '../lib/size-normalizer';

export default class DecoratedCard extends Component {
    static propTypes = {
        style: PropTypes.object,
        color: PropTypes.string,
        value: PropTypes.string,
        image: PropTypes.element,
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
        return (
            <Animated.View
                style={[this.props.style, { transform: [{ translateX: this.state.animation.x }] }]}>
                <View style={[styles.container]}>
                    <View
                        style={[
                            styles.decoration,
                            { width: moderateScale(8), backgroundColor: this.props.color },
                        ]}
                    />
                    <View style={styles.titleContainer}>
                        <Text style={font_styles.title_3_bold}>{this.props.value}</Text>
                        {this.props.image}
                    </View>
                </View>
            </Animated.View>
        );
    }
}

const styles = StyleSheet.create({
    container: {
        borderRadius: moderateScale(4),
        width: '100%',
        justifyContent: 'center',
        alignItems: 'center',
        backgroundColor: Colors.white,
        alignSelf: 'center',
        flexDirection: 'row',
        flex: 1,
        shadowColor: Colors.primaryText,
        shadowOffset: {
            width: 0,
            height: 1,
        },
        shadowOpacity: 0.18,
        shadowRadius: 1.0,
        elevation: 1,
    },
    decoration: {
        height: '100%',
        borderTopLeftRadius: moderateScale(4),
        borderTopRightRadius: moderateScale(1),
        borderBottomLeftRadius: moderateScale(4),
        borderBottomRightRadius: moderateScale(1),
    },
    titleContainer: {
        flex: 1,
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'space-between',
        height: '100%',
        paddingHorizontal: moderateScale(12),
    },
});
