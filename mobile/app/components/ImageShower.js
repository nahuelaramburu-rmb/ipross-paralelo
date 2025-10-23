import PropTypes from 'prop-types';
import React, { Component } from 'react';
import { View, Image } from 'react-native';

export default class ImageShower extends Component {
    static propTypes = {
        photoContainer: PropTypes.object,
        imageStyle: PropTypes.object,
        image: PropTypes.number.isRequired,
    };

    constructor(props) {
        super(props);
    }

    render() {
        return (
            <View style={this.props.photoContainer}>
                <Image source={this.props.image} style={[this.props.imageStyle]} resizeMode='contain' />
            </View>
        );
    }
}
