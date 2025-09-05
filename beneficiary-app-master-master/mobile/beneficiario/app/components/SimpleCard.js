import PropTypes from 'prop-types';
import React, { Component } from 'react';
import { StyleSheet, Text, View, Dimensions, Animated } from 'react-native';

import * as Colors from '../constants/Colors';

const { width, height } = Dimensions.get('window');
import { font_styles } from '../lib/default-styles';

import { moderateScale } from '../lib/size-normalizer';

export default class SimpleCard extends Component {
    static propTypes = {
        style: PropTypes.object,
        header: PropTypes.string.isRequired,
        title: PropTypes.string.isRequired,
        value: PropTypes.string.isRequired,
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
                        style={{
                            flex: 1,
                            flexDirection: 'row',
                            justifyContent: 'space-between',
                            alignItems: 'center',
                            paddingHorizontal: moderateScale(16),
                        }}>
                        <View
                            style={{
                                flex: 1,
                                flexDirection: 'column',
                                justifyContent: 'center',
                                alignItems: 'flex-start',
                            }}>
                            <Text
                                style={[
                                    font_styles.subtitle,
                                    { color: Colors.accent, marginBottom: moderateScale(5) },
                                ]}>
                                {this.props.header}
                            </Text>
                            <Text style={[font_styles.title_3, { color: Colors.primaryText }]}>
                                {this.props.title}
                            </Text>
                        </View>
                        <View>
                            <Text style={font_styles.title_3_bold}>{this.props.value}</Text>
                        </View>
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
        flexDirection: 'column',
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
});
