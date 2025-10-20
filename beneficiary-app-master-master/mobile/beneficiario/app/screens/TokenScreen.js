import React, { useState, useEffect } from 'react';
import {
    View,
    Text,
    TouchableOpacity,
    StyleSheet,
    StatusBar,
    ScrollView,
    Animated,
} from 'react-native';
import Icon from 'react-native-vector-icons/Ionicons';
import * as Colors from '../constants/Colors';

const TokenScreen = ({ onBack, loggedUser }) => {
    const [token, setToken] = useState('');
    const [timeLeft, setTimeLeft] = useState(30);
    const [progressAnim] = useState(new Animated.Value(1));

    const userName = loggedUser?.nombre || 'Usuario IPROSS';
    const userNumber = loggedUser?.numero_afiliado || '03-36447582/00';
    const idNumber = loggedUser?.idNumber || '36447582';

    // Generar token de 6 dígitos basado en timestamp y DNI
    const generateToken = () => {
        const timestamp = Math.floor(Date.now() / 30000); // Cambia cada 30 segundos
        const seed = parseInt(idNumber) + timestamp;
        const tokenNum = (seed * 123456) % 1000000;
        return tokenNum.toString().padStart(6, '0');
    };

    useEffect(() => {
        // Generar primer token
        setToken(generateToken());

        // Timer para countdown
        const interval = setInterval(() => {
            setTimeLeft((prev) => {
                if (prev <= 1) {
                    // Regenerar token
                    setToken(generateToken());
                    // Reiniciar animación
                    progressAnim.setValue(1);
                    Animated.timing(progressAnim, {
                        toValue: 0,
                        duration: 30000,
                        useNativeDriver: false,
                    }).start();
                    return 30;
                }
                return prev - 1;
            });
        }, 1000);

        // Iniciar animación
        Animated.timing(progressAnim, {
            toValue: 0,
            duration: 30000,
            useNativeDriver: false,
        }).start();

        return () => clearInterval(interval);
    }, []);

    const progressWidth = progressAnim.interpolate({
        inputRange: [0, 1],
        outputRange: ['0%', '100%'],
    });

    const progressColor = timeLeft <= 10 ? '#f44336' : Colors.primary;

    return (
        <View style={styles.container}>
            <StatusBar barStyle="light-content" backgroundColor={Colors.primary} />
            
            {/* Header */}
            <View style={styles.header}>
                <TouchableOpacity onPress={onBack} style={styles.backButton}>
                    <Icon name="arrow-back" size={24} color="#fff" />
                </TouchableOpacity>
                <Text style={styles.headerTitle}>Código Token</Text>
                <View style={styles.placeholder} />
            </View>

            <ScrollView style={styles.content} contentContainerStyle={styles.contentContainer}>
                {/* Información */}
                <View style={styles.infoCard}>
                    <Icon name="information-circle" size={24} color={Colors.primary} />
                    <Text style={styles.infoText}>
                        Este código de seguridad cambia cada 30 segundos. Úselo para autenticación adicional.
                    </Text>
                </View>

                {/* Tarjeta del Token */}
                <View style={styles.tokenCard}>
                    <View style={styles.tokenHeader}>
                        <Text style={styles.tokenTitle}>Token de Autenticación</Text>
                        <Text style={styles.tokenSubtitle}>IPROSS - Código Temporal</Text>
                    </View>

                    {/* Token Display */}
                    <View style={styles.tokenDisplay}>
                        <View style={styles.tokenContainer}>
                            {token.split('').map((digit, index) => (
                                <View key={index} style={styles.digitContainer}>
                                    <Text style={styles.digitText}>{digit}</Text>
                                </View>
                            ))}
                        </View>
                    </View>

                    {/* Countdown Timer */}
                    <View style={styles.timerSection}>
                        <View style={styles.timerInfo}>
                            <Icon 
                                name="time-outline" 
                                size={24} 
                                color={timeLeft <= 10 ? '#f44336' : Colors.primary} 
                            />
                            <Text style={[
                                styles.timerText,
                                timeLeft <= 10 && styles.timerTextWarning
                            ]}>
                                Válido por: {timeLeft}s
                            </Text>
                        </View>

                        {/* Progress Bar */}
                        <View style={styles.progressBarContainer}>
                            <Animated.View 
                                style={[
                                    styles.progressBar,
                                    { 
                                        width: progressWidth,
                                        backgroundColor: progressColor,
                                    }
                                ]} 
                            />
                        </View>

                        {timeLeft <= 10 && (
                            <Text style={styles.warningText}>
                                ⚠️ El código está por expirar
                            </Text>
                        )}
                    </View>

                    {/* Información del usuario */}
                    <View style={styles.userInfo}>
                        <Text style={styles.userName}>{userName}</Text>
                        <Text style={styles.userNumber}>N° Afiliado: {userNumber}</Text>
                        <Text style={styles.userDni}>DNI: {idNumber}</Text>
                    </View>
                </View>

                {/* Instrucciones */}
                <View style={styles.instructionsCard}>
                    <Text style={styles.instructionsTitle}>¿Cómo usar el token?</Text>
                    
                    <View style={styles.instructionItem}>
                        <View style={styles.instructionNumber}>
                            <Text style={styles.instructionNumberText}>1</Text>
                        </View>
                        <Text style={styles.instructionText}>
                            Solicite al prestador o farmacia que requiere autenticación con token
                        </Text>
                    </View>

                    <View style={styles.instructionItem}>
                        <View style={styles.instructionNumber}>
                            <Text style={styles.instructionNumberText}>2</Text>
                        </View>
                        <Text style={styles.instructionText}>
                            Proporcione el código de 6 dígitos que se muestra en pantalla
                        </Text>
                    </View>

                    <View style={styles.instructionItem}>
                        <View style={styles.instructionNumber}>
                            <Text style={styles.instructionNumberText}>3</Text>
                        </View>
                        <Text style={styles.instructionText}>
                            El código es válido por 30 segundos. Si expira, se generará uno nuevo automáticamente
                        </Text>
                    </View>
                </View>

                {/* Información de Seguridad */}
                <View style={styles.securityCard}>
                    <View style={styles.securityHeader}>
                        <Icon name="shield-checkmark" size={24} color={Colors.primary} />
                        <Text style={styles.securityTitle}>Información de Seguridad</Text>
                    </View>
                    <Text style={styles.securityText}>
                        • Este código es único y temporal{'\n'}
                        • Se genera usando criptografía segura{'\n'}
                        • No lo comparta con terceros no autorizados{'\n'}
                        • Solo personal de IPROSS debe solicitarlo
                    </Text>
                </View>
            </ScrollView>
        </View>
    );
};

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: Colors.light1,
    },
    header: {
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'space-between',
        backgroundColor: Colors.primary,
        paddingTop: StatusBar.currentHeight || 40,
        paddingBottom: 16,
        paddingHorizontal: 16,
        elevation: 4,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 0.2,
        shadowRadius: 2,
    },
    backButton: {
        padding: 8,
    },
    headerTitle: {
        fontSize: 20,
        fontWeight: 'bold',
        color: '#fff',
    },
    placeholder: {
        width: 40,
    },
    content: {
        flex: 1,
    },
    contentContainer: {
        padding: 16,
    },
    infoCard: {
        flexDirection: 'row',
        alignItems: 'center',
        backgroundColor: '#e3f2fd',
        padding: 16,
        borderRadius: 12,
        marginBottom: 20,
    },
    infoText: {
        flex: 1,
        fontSize: 14,
        color: '#1565c0',
        marginLeft: 12,
        lineHeight: 20,
    },
    tokenCard: {
        backgroundColor: '#fff',
        borderRadius: 16,
        padding: 24,
        marginBottom: 20,
        elevation: 4,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 0.1,
        shadowRadius: 4,
        alignItems: 'center',
    },
    tokenHeader: {
        alignItems: 'center',
        marginBottom: 30,
    },
    tokenTitle: {
        fontSize: 20,
        fontWeight: 'bold',
        color: Colors.grisOscuro,
    },
    tokenSubtitle: {
        fontSize: 14,
        color: Colors.primary,
        marginTop: 4,
    },
    tokenDisplay: {
        marginBottom: 30,
    },
    tokenContainer: {
        flexDirection: 'row',
        gap: 8,
    },
    digitContainer: {
        width: 45,
        height: 60,
        backgroundColor: Colors.light1,
        borderRadius: 12,
        borderWidth: 2,
        borderColor: Colors.primary,
        justifyContent: 'center',
        alignItems: 'center',
    },
    digitText: {
        fontSize: 32,
        fontWeight: 'bold',
        color: Colors.primary,
        fontFamily: 'monospace',
    },
    timerSection: {
        width: '100%',
        alignItems: 'center',
        paddingVertical: 20,
        borderTopWidth: 1,
        borderTopColor: Colors.light2,
    },
    timerInfo: {
        flexDirection: 'row',
        alignItems: 'center',
        marginBottom: 12,
    },
    timerText: {
        fontSize: 18,
        fontWeight: 'bold',
        color: Colors.grisOscuro,
        marginLeft: 8,
    },
    timerTextWarning: {
        color: '#f44336',
    },
    progressBarContainer: {
        width: '100%',
        height: 8,
        backgroundColor: Colors.light2,
        borderRadius: 4,
        overflow: 'hidden',
        marginBottom: 12,
    },
    progressBar: {
        height: '100%',
        borderRadius: 4,
    },
    warningText: {
        fontSize: 14,
        color: '#f44336',
        fontWeight: '600',
    },
    userInfo: {
        alignItems: 'center',
        width: '100%',
        paddingVertical: 16,
        borderTopWidth: 1,
        borderTopColor: Colors.light2,
    },
    userName: {
        fontSize: 18,
        fontWeight: 'bold',
        color: Colors.grisOscuro,
        marginBottom: 8,
    },
    userNumber: {
        fontSize: 16,
        color: Colors.grisOscuro,
        marginBottom: 4,
    },
    userDni: {
        fontSize: 14,
        color: Colors.grisOscuro,
    },
    instructionsCard: {
        backgroundColor: '#fff',
        borderRadius: 12,
        padding: 20,
        marginBottom: 20,
    },
    instructionsTitle: {
        fontSize: 16,
        fontWeight: 'bold',
        color: Colors.grisOscuro,
        marginBottom: 16,
    },
    instructionItem: {
        flexDirection: 'row',
        alignItems: 'flex-start',
        marginBottom: 16,
    },
    instructionNumber: {
        width: 28,
        height: 28,
        borderRadius: 14,
        backgroundColor: Colors.primary,
        justifyContent: 'center',
        alignItems: 'center',
        marginRight: 12,
    },
    instructionNumberText: {
        fontSize: 14,
        fontWeight: 'bold',
        color: '#fff',
    },
    instructionText: {
        flex: 1,
        fontSize: 14,
        color: Colors.grisOscuro,
        lineHeight: 20,
        paddingTop: 4,
    },
    securityCard: {
        backgroundColor: '#f1f8e9',
        borderRadius: 12,
        padding: 20,
        borderLeftWidth: 4,
        borderLeftColor: Colors.primary,
    },
    securityHeader: {
        flexDirection: 'row',
        alignItems: 'center',
        marginBottom: 12,
    },
    securityTitle: {
        fontSize: 16,
        fontWeight: 'bold',
        color: Colors.grisOscuro,
        marginLeft: 12,
    },
    securityText: {
        fontSize: 14,
        color: '#558b2f',
        lineHeight: 22,
    },
});

export default TokenScreen;
