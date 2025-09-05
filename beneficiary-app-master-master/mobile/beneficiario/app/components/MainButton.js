
import PropTypes from 'prop-types';
import React, { Component } from 'react';
import {
    StyleSheet,
    Text,
    View,
    ActivityIndicator,
    TouchableOpacity
} from 'react-native';

import * as Colors from '../constants/Colors';
import { moderateScale, verticalScale } from '../lib/size-normalizer';
import { font_styles } from '../lib/default-styles';
import { RectButton, } from 'react-native-gesture-handler';

export default class MainButton extends Component {

    static defaultProps = {
        color: Colors.primary,
        text: '',
        useNative: true
    };

    static propTypes = {
        size: PropTypes.string.isRequired,
        style: PropTypes.object,
        color: PropTypes.string,
        onPress: PropTypes.func.isRequired,
        loading: PropTypes.bool,
        children: PropTypes.element,
        textStyle: PropTypes.object,
        text: PropTypes.string.isRequired
    };

    _getButtonSize() {
        switch (this.props.size) {
            case 'small':
                return {
                    width: moderateScale(85),
                    height: verticalScale(30),
                    fontSize: moderateScale(15, 1)
                };
            case 'large':
                return {
                    width: moderateScale(205),
                    height: verticalScale(35),
                    fontSize: moderateScale(14, 1)
                };
            default:
                return {
                    width: moderateScale(180),
                    height: verticalScale(35),
                    fontSize: moderateScale(16, 1)
                };
        }
    }

    render() {
        const { useNative } = this.props;
        const size = this._getButtonSize();
        if (useNative) {
            return (<RectButton style={[styles.container, size, { backgroundColor: this.props.color ? this.props.color : Colors.primaryText }]}
                onPress={this.props.onPress}>
                {!this.props.loading ? <View style={{ flex: 1, flexDirection: 'row', alignItems: 'center', justifyContent: 'center' }}>{this.props.children}<Text style={[styles.text, this.props.textStyle, { fontSize: size.fontSize }]}>{this.props.text}</Text></View> :
                    <ActivityIndicator color={Colors.white} size="small" />}
            </RectButton>);
        } else {
            return (<TouchableOpacity style={[styles.container, size, { backgroundColor: this.props.color ? this.props.color : Colors.primaryText }]}
                onPress={this.props.onPress}>
                {!this.props.loading ? <View style={{ flex: 1, flexDirection: 'row', alignItems: 'center', justifyContent: 'center' }}>{this.props.children}<Text style={[styles.text, this.props.textStyle, { fontSize: size.fontSize }]}>{this.props.text}</Text></View> :
                    <ActivityIndicator color={Colors.white} size="small" />}
            </TouchableOpacity>);
        }
    }
}

const styles = StyleSheet.create({
    container: {
        borderRadius: 5,
        alignItems: 'center',
        justifyContent: 'center',
        shadowColor: "#000",
        shadowOffset: {
            width: 0,
            height: 1,
        },
        shadowOpacity: 0.22,
        shadowRadius: 2.22,
        alignSelf: 'center',
        elevation: 3
    },
    text: {
        ...font_styles.title_3,
        color: Colors.white,
        textAlign: 'center',
        letterSpacing: 2,
    }
});
