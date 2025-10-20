import React from 'react';
import { Text, View } from 'react-native';
import LottieView from 'lottie-react-native';

import TopNavigation from './TopNavigation';
import strings from '../constants/Strings';
import * as Colors from '../constants/Colors';
import { bold } from '../constants/Fonts';

export default class ErrorBoundary extends React.Component {
    constructor(props) {
        super(props);
        this.state = { hasError: false };
        this._processError = this._processError.bind(this);
    }

    componentDidCatch(error, info) {
        let err = this._processError(error);
        err = err.replace(/(\r\n|\n|\r)/gm, ' ');
        this.props.sendError(err);
        this.setState({ hasError: true });
    }

    _processError(err) {
        const errChunk = err.toString().split(/\sin\s/);
        const data = errChunk.splice(0, 3);
        return data.join('');
    }

    render() {
        if (this.state.hasError) {
            return (
                <View style={{ flex: 1 }}>
                    <TopNavigation currentScene={'error-boundary'} />
                    <View style={{ flex: 1, marginTop: 0 }}>
                        <View style={{ flex: 0.6, alignItems: 'center', justifyContent: 'center' }}>
                            <LottieView source={require('../animations/warning.json')} autoPlay loop={true} />
                        </View>
                        <View style={{ flex: 0.4, alignItems: 'center', justifyContent: 'center' }}>
                            <Text
                                style={{ fontFamily: bold, fontSize: 15, textAlign: 'center', margin: '2%' }}>
                                {strings.errorBoundary.error_ocurred}
                            </Text>
                        </View>
                    </View>
                </View>
            );
        }
        return this.props.children;
    }
}
