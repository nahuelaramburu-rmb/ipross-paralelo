import React from 'react';
import { Text } from 'react-native';
import { font_styles } from '../lib/default-styles';
import * as Colors from '../constants/Colors';

const TopNavigationTitle = ({ title, textColor }) => {
    return (
        <React.Fragment>
            {typeof title === 'string' ? (
                <Text
                    style={[font_styles.headline, { color: textColor ? textColor : Colors.inverseText }]}
                    numberOfLines={1}
                    ellipsizeMode='tail'>
                    {title}
                </Text>
            ) : (
                title
            )}
        </React.Fragment>
    );
};

export default React.memo(TopNavigationTitle);
