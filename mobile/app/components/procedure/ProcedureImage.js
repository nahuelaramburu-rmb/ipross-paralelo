import React from 'react';
import { Text, View, StyleSheet } from 'react-native';
import { moderateScale, verticalScale } from '../../lib/size-normalizer';
import * as Colors from '../../constants/Colors';
import { font_styles } from '../../lib/default-styles';
import Icon from 'react-native-vector-icons/Ionicons';
import { RectButton } from 'react-native-gesture-handler';

const ProcedureImageContainer = React.memo(({ children }) => {
    return (
        <View style={{ flex: 1 }}>
            <Text style={[font_styles.primary_text_bold, { marginBottom: verticalScale(8) }]}>
                {'Adjuntos'}
            </Text>
            <View style={{ flex: 1, alignItems: 'center', justifyContent: 'flex-start' }}>
                {!children.length && (
                    <Text
                        style={[
                            font_styles.secondary_text,
                            { alignSelf: 'center', marginTop: moderateScale(8) },
                        ]}>
                        Debe adjuntar al menos una imagen
                    </Text>
                )}
                {children}
            </View>
        </View>
    );
});

const ProcedureImage = React.memo(({ image, style, onRemove, editing }) => {
    return (
        <View style={[style, styles.container]}>
            <Text style={[font_styles.secondary_text_bold, { flex: 0.9 }]}>{image.name}</Text>
            {editing && (
                <RectButton style={styles.iconContainer} onPress={() => onRemove(image.name)}>
                    <Icon
                        name='md-close'
                        size={moderateScale(20)}
                        color={Colors.statusRejected}
                        testID='remove-procedure-image'
                    />
                </RectButton>
            )}
        </View>
    );
});

const styles = StyleSheet.create({
    container: {
        borderWidth: 1,
        padding: moderateScale(12),
        borderRadius: moderateScale(5),
        borderColor: Colors.accent,
        alignItems: 'center',
        flexDirection: 'row',
        justifyContent: 'space-between',
        height: verticalScale(48),
        width: '100%',
    },
    iconContainer: {
        flex: 0.1,
        alignItems: 'center',
    },
});

export { ProcedureImage, ProcedureImageContainer };
