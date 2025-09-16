import React from 'react';
import { View, StyleSheet, Text, Dimensions, Platform } from 'react-native';
import * as Colors from '../constants/Colors';
import AuthenticationWrapper from '../components/AuthenticationWrapper';
import { font_styles } from '../lib/default-styles';
import { verticalScale, moderateScale } from '../lib/size-normalizer';
import ExtraDimensions from 'react-native-extra-dimensions-android';

const { height: iosHeight } = Dimensions.get('window');
const height =
    Platform.OS === 'ios'
        ? iosHeight
        : ExtraDimensions.getRealWindowHeight() - ExtraDimensions.getSoftMenuBarHeight();

const SignUpHelpModal = () => {
    return (
        <AuthenticationWrapper scrollEnabled={true}>
            <View style={styles.container}>
                <View style={styles.textContainer}>
                    <Text style={[font_styles.title_2, { paddingBottom: verticalScale(12) }]}>
                        Bienvenidos a la App de Ipross
                    </Text>
                    <Text
                        style={[
                            font_styles.primary_text,
                            { textAlign: 'center', paddingBottom: verticalScale(12) },
                        ]}>
                        A continuación usted podrá crear su cuenta para ingresar. Para ello, por favor,
                        prosiga con los siguientes pasos:
                    </Text>
                    <Text
                        style={[
                            font_styles.primary_text,
                            { textAlign: 'center', paddingBottom: verticalScale(12) },
                        ]}>
                        1. Rellene el formulario con sus datos. Tenga en cuenta que el número de afiliado debe
                        ingresarse con el formato: XX-XXXXXXX/XX (tal cuál lo muestra su credencial). Luego
                        presione "Siguiente".
                    </Text>
                    <Text
                        style={[
                            font_styles.primary_text,
                            { textAlign: 'center', paddingBottom: verticalScale(12) },
                        ]}>
                        2. Una vez constatados sus datos en el padrón, usted deberá ingresar una dirección de
                        correo válida y elegir su contraseña. Luego presione "Finalizar".
                    </Text>
                    <Text
                        style={[
                            font_styles.primary_text,
                            { textAlign: 'center', paddingBottom: verticalScale(12) },
                        ]}>
                        3. Su ha sido creada!, solo resta la confirmación. Por favor, diríjase a la casilla de
                        correo que proporcionó anteriormente y presione "Verificar mi cuenta". Sin éste paso,
                        no podrá ingresar a la aplicación. Una vez confirmada la cuenta, ingrese con su numero
                        de documento y contraseña elegida en el registro.
                    </Text>
                    <Text
                        style={[
                            font_styles.title_3,
                            { textAlign: 'center', paddingBottom: verticalScale(12) },
                        ]}>
                        Listo! Su cuenta está lista para operar!
                    </Text>
                    <Text
                        style={[
                            font_styles.secondary_text,
                            {
                                textAlign: 'center',
                                color: Colors.accent,
                                paddingBottom: verticalScale(12),
                            },
                        ]}>
                        Si usted está complentado sus datos correctamente, pero aún así no puede registrarse,
                        le pedimos por favor, que envíe un correo electrónico a
                        contact-center@ipross.rionegro.gov.ar o comuníquese telefónicamente al 08003334776,
                        para que actualicemos sus datos en el padrón.
                    </Text>
                </View>
            </View>
        </AuthenticationWrapper>
    );
};

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: Colors.splashBackground,
    },
    textContainer: {
        flex: 1,
        alignItems: 'center',
        paddingHorizontal: moderateScale(8),
    },
});

export default React.memo(SignUpHelpModal);
