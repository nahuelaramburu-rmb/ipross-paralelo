import React from 'react';
import {
    View,
    Text,
    TouchableOpacity,
    StyleSheet,
    StatusBar,
    ScrollView,
} from 'react-native';
import Icon from 'react-native-vector-icons/Ionicons';
import QRCode from 'react-native-qrcode-svg';
import * as Colors from '../constants/Colors';

const QRScreen = ({ onBack, loggedUser }) => {
    const userName = loggedUser?.nombre || 'Usuario IPROSS';
    const userNumber = loggedUser?.numero_afiliado || '03-36447582/00';
    const idNumber = loggedUser?.idNumber || '36447582';
    
    // Datos para el QR (simulando información del beneficiario)
    const qrData = JSON.stringify({
        dni: idNumber,
        numeroAfiliado: userNumber,
        nombre: userName,
        tipo: 'SUELDO',
        validoHasta: '2025-12-31',
        timestamp: new Date().getTime(),
    });

    return (
        <View style={styles.container}>
            <StatusBar barStyle="light-content" backgroundColor={Colors.primary} />
            
            {/* Header */}
            <View style={styles.header}>
                <TouchableOpacity onPress={onBack} style={styles.backButton}>
                    <Icon name="arrow-back" size={24} color="#fff" />
                </TouchableOpacity>
                <Text style={styles.headerTitle}>Código QR</Text>
                <View style={styles.placeholder} />
            </View>

            <ScrollView style={styles.content} contentContainerStyle={styles.contentContainer}>
                {/* Información */}
                <View style={styles.infoCard}>
                    <Icon name="information-circle" size={24} color={Colors.primary} />
                    <Text style={styles.infoText}>
                        Presente este código QR en la farmacia o prestador para acreditar su identidad
                    </Text>
                </View>

                {/* Tarjeta del QR */}
                <View style={styles.qrCard}>
                    <View style={styles.qrHeader}>
                        <Text style={styles.qrTitle}>Credencial Virtual</Text>
                        <Text style={styles.qrSubtitle}>IPROSS - Río Negro</Text>
                    </View>

                    {/* QR Code */}
                    <View style={styles.qrContainer}>
                        <QRCode
                            value={qrData}
                            size={250}
                            color="#000"
                            backgroundColor="#fff"
                            logo={require('../images/ipross_logo.png')}
                            logoSize={40}
                            logoBackgroundColor='#fff'
                        />
                    </View>

                    {/* Información del usuario */}
                    <View style={styles.userInfo}>
                        <Text style={styles.userName}>{userName}</Text>
                        <Text style={styles.userNumber}>N° Afiliado: {userNumber}</Text>
                        <Text style={styles.userDni}>DNI: {idNumber}</Text>
                    </View>

                    {/* Timestamp */}
                    <View style={styles.timestampContainer}>
                        <Icon name="time-outline" size={16} color={Colors.grisOscuro} />
                        <Text style={styles.timestamp}>
                            Generado: {new Date().toLocaleString('es-AR')}
                        </Text>
                    </View>
                </View>

                {/* Instrucciones */}
                <View style={styles.instructionsCard}>
                    <Text style={styles.instructionsTitle}>Instrucciones de uso:</Text>
                    
                    <View style={styles.instructionItem}>
                        <View style={styles.instructionNumber}>
                            <Text style={styles.instructionNumberText}>1</Text>
                        </View>
                        <Text style={styles.instructionText}>
                            Aumente el brillo de su pantalla para facilitar la lectura
                        </Text>
                    </View>

                    <View style={styles.instructionItem}>
                        <View style={styles.instructionNumber}>
                            <Text style={styles.instructionNumberText}>2</Text>
                        </View>
                        <Text style={styles.instructionText}>
                            Muestre el código QR al prestador o farmacia
                        </Text>
                    </View>

                    <View style={styles.instructionItem}>
                        <View style={styles.instructionNumber}>
                            <Text style={styles.instructionNumberText}>3</Text>
                        </View>
                        <Text style={styles.instructionText}>
                            Espere a que escaneen el código para validar su identidad
                        </Text>
                    </View>
                </View>

                {/* Advertencia */}
                <View style={styles.warningCard}>
                    <Icon name="alert-circle-outline" size={20} color="#f57c00" />
                    <Text style={styles.warningText}>
                        No comparta capturas de pantalla de este código QR. Puede contener información sensible.
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
    qrCard: {
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
    qrHeader: {
        alignItems: 'center',
        marginBottom: 20,
    },
    qrTitle: {
        fontSize: 20,
        fontWeight: 'bold',
        color: Colors.grisOscuro,
    },
    qrSubtitle: {
        fontSize: 14,
        color: Colors.primary,
        marginTop: 4,
    },
    qrContainer: {
        padding: 20,
        backgroundColor: '#fff',
        borderRadius: 12,
        marginBottom: 20,
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
    timestampContainer: {
        flexDirection: 'row',
        alignItems: 'center',
        marginTop: 12,
    },
    timestamp: {
        fontSize: 12,
        color: Colors.grisOscuro,
        marginLeft: 6,
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
    warningCard: {
        flexDirection: 'row',
        alignItems: 'center',
        backgroundColor: '#fff3e0',
        padding: 16,
        borderRadius: 12,
        borderLeftWidth: 4,
        borderLeftColor: '#f57c00',
    },
    warningText: {
        flex: 1,
        fontSize: 13,
        color: '#e65100',
        marginLeft: 12,
        lineHeight: 18,
    },
});

export default QRScreen;
