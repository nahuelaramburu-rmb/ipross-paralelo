import React, { useState } from 'react';
import { View, Text, TouchableOpacity, StyleSheet, Image, Alert, ScrollView, StatusBar } from 'react-native';

const Router = ({ loggedUser }) => {
    const [currentScreen, setCurrentScreen] = useState('BeneficiaryInformation');

    // Obtener nombre del usuario o usar valor por defecto
    const userName = loggedUser && loggedUser.nombre ? loggedUser.nombre : 'Usuario';

    const renderBeneficiaryInformation = () => (
        <ScrollView style={styles.screenContainer}>
            <View style={styles.header}>
                <Image
                    source={require('../images/ipross_logo_white.png')}
                    style={styles.headerLogo}
                    resizeMode='contain'
                />
            </View>

            <View style={styles.contentContainer}>
                <Text style={styles.sectionTitle}>Mi Información</Text>

                <View style={styles.infoCard}>
                    <Text style={styles.cardTitle}>Datos del Beneficiario</Text>
                    <Text style={styles.cardSubtext}>Nombre: {userName}</Text>
                    <Text style={styles.cardSubtext}>
                        N° Afiliado:{' '}
                        {loggedUser && loggedUser.numero_afiliado
                            ? loggedUser.numero_afiliado
                            : 'No disponible'}
                    </Text>
                </View>

                <View style={styles.infoCard}>
                    <Text style={styles.cardTitle}>Estado del Plan</Text>
                    <Text style={styles.cardSubtext}>Plan activo - Al día</Text>
                </View>

                <Text style={styles.sectionTitle}>Acciones Rápidas</Text>

                <TouchableOpacity
                    style={styles.actionButton}
                    onPress={() => Alert.alert('Información', 'Función en desarrollo')}>
                    <Text style={styles.actionButtonText}>Generar Token QR</Text>
                </TouchableOpacity>

                <TouchableOpacity
                    style={styles.actionButton}
                    onPress={() => Alert.alert('Información', 'Función en desarrollo')}>
                    <Text style={styles.actionButtonText}>Gestión Familiar</Text>
                </TouchableOpacity>
            </View>
        </ScrollView>
    );

    const renderAuthorizations = () => (
        <ScrollView style={styles.screenContainer}>
            <View style={styles.header}>
                <Text style={styles.headerTitle}>Autorizaciones</Text>
                <Text style={styles.headerSubtitle}>Gestión de autorizaciones</Text>
            </View>

            <View style={styles.contentContainer}>
                <View style={styles.infoCard}>
                    <Text style={styles.cardTitle}>Autorizaciones Pendientes</Text>
                    <Text style={styles.cardSubtext}>No hay autorizaciones pendientes</Text>
                </View>

                <TouchableOpacity
                    style={styles.actionButton}
                    onPress={() => Alert.alert('Información', 'Función en desarrollo')}>
                    <Text style={styles.actionButtonText}>Nueva Autorización</Text>
                </TouchableOpacity>
            </View>
        </ScrollView>
    );

    const renderCoinsuranceCharges = () => (
        <ScrollView style={styles.screenContainer}>
            <View style={styles.header}>
                <Text style={styles.headerTitle}>Copagos</Text>
                <Text style={styles.headerSubtitle}>Gestión de copagos</Text>
            </View>

            <View style={styles.contentContainer}>
                <View style={styles.infoCard}>
                    <Text style={styles.cardTitle}>Copagos Pendientes</Text>
                    <Text style={styles.cardSubtext}>No hay copagos pendientes</Text>
                </View>
            </View>
        </ScrollView>
    );

    const renderProcedures = () => (
        <ScrollView style={styles.screenContainer}>
            <View style={styles.header}>
                <Text style={styles.headerTitle}>Trámites</Text>
                <Text style={styles.headerSubtitle}>Gestión de trámites</Text>
            </View>

            <View style={styles.contentContainer}>
                <View style={styles.infoCard}>
                    <Text style={styles.cardTitle}>Trámites Activos</Text>
                    <Text style={styles.cardSubtext}>No hay trámites en curso</Text>
                </View>

                <TouchableOpacity
                    style={styles.actionButton}
                    onPress={() => Alert.alert('Información', 'Función en desarrollo')}>
                    <Text style={styles.actionButtonText}>Nuevo Trámite</Text>
                </TouchableOpacity>
            </View>
        </ScrollView>
    );

    const renderCurrentScreen = () => {
        switch (currentScreen) {
            case 'BeneficiaryInformation':
                return renderBeneficiaryInformation();
            case 'Authorizations':
                return renderAuthorizations();
            case 'CoinsuranceCharges':
                return renderCoinsuranceCharges();
            case 'Procedures':
                return renderProcedures();
            default:
                return renderBeneficiaryInformation();
        }
    };

    return (
        <View style={styles.container}>
            <StatusBar 
                backgroundColor="#6ac64f" 
                barStyle="light-content" 
                translucent={false}
            />
            {renderCurrentScreen()}

            {/* Bottom Tab Bar Simple */}
            <View style={styles.bottomTabBar}>
                <TouchableOpacity
                    style={[styles.tabButton, currentScreen === 'BeneficiaryInformation' && styles.activeTab]}
                    onPress={() => setCurrentScreen('BeneficiaryInformation')}>
                    <Text
                        style={[
                            styles.tabText,
                            currentScreen === 'BeneficiaryInformation' && styles.activeTabText,
                        ]}>
                        Información
                    </Text>
                </TouchableOpacity>

                <TouchableOpacity
                    style={[styles.tabButton, currentScreen === 'Authorizations' && styles.activeTab]}
                    onPress={() => setCurrentScreen('Authorizations')}>
                    <Text
                        style={[styles.tabText, currentScreen === 'Authorizations' && styles.activeTabText]}>
                        Autorizaciones
                    </Text>
                </TouchableOpacity>

                <TouchableOpacity
                    style={[styles.tabButton, currentScreen === 'CoinsuranceCharges' && styles.activeTab]}
                    onPress={() => setCurrentScreen('CoinsuranceCharges')}>
                    <Text
                        style={[
                            styles.tabText,
                            currentScreen === 'CoinsuranceCharges' && styles.activeTabText,
                        ]}>
                        Copagos
                    </Text>
                </TouchableOpacity>

                <TouchableOpacity
                    style={[styles.tabButton, currentScreen === 'Procedures' && styles.activeTab]}
                    onPress={() => setCurrentScreen('Procedures')}>
                    <Text style={[styles.tabText, currentScreen === 'Procedures' && styles.activeTabText]}>
                        Trámites
                    </Text>
                </TouchableOpacity>
            </View>
        </View>
    );
};

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: '#f5f5f5',
    },
    screenContainer: {
        flex: 1,
        backgroundColor: '#f5f5f5',
    },
    header: {
        backgroundColor: '#6ac64f',
        paddingTop: 50,
        paddingBottom: 24,
        paddingHorizontal: 24,
        alignItems: 'center',
        justifyContent: 'center',
        shadowColor: '#000000',
        shadowOffset: {
            width: 0,
            height: 4,
        },
        shadowOpacity: 0.15,
        shadowRadius: 8,
        elevation: 4,
    },
    headerLogo: {
        width: 180,
        height: 80,
    },
    headerTitle: {
        fontSize: 24,
        fontWeight: 'bold',
        color: '#FFFFFF',
        marginBottom: 4,
    },
    headerSubtitle: {
        fontSize: 16,
        fontWeight: '400',
        color: '#FFFFFF',
        opacity: 0.9,
    },
    contentContainer: {
        flex: 1,
        padding: 24,
    },
    sectionTitle: {
        fontSize: 20,
        fontWeight: '600',
        color: '#000000',
        marginBottom: 16,
        marginTop: 8,
    },
    infoCard: {
        backgroundColor: '#FFFFFF',
        borderRadius: 16,
        padding: 20,
        marginVertical: 8,
        shadowColor: '#000000',
        shadowOffset: {
            width: 0,
            height: 2,
        },
        shadowOpacity: 0.1,
        shadowRadius: 8,
        elevation: 4,
    },
    cardTitle: {
        fontSize: 18,
        fontWeight: '600',
        color: '#000000',
        marginBottom: 8,
    },
    cardSubtext: {
        fontSize: 14,
        fontWeight: '400',
        color: '#666666',
        marginTop: 4,
    },
    actionButton: {
        backgroundColor: '#6ac64f',
        borderRadius: 12,
        paddingVertical: 15,
        paddingHorizontal: 24,
        marginBottom: 16,
        alignItems: 'center',
        shadowColor: '#000000',
        shadowOffset: {
            width: 0,
            height: 2,
        },
        shadowOpacity: 0.1,
        shadowRadius: 4,
        elevation: 2,
    },
    actionButtonText: {
        fontSize: 16,
        fontWeight: '600',
        color: '#FFFFFF',
    },
    bottomTabBar: {
        flexDirection: 'row',
        backgroundColor: '#FFFFFF',
        borderTopWidth: 1,
        borderTopColor: '#e8e8e8',
        paddingVertical: 8,
        paddingHorizontal: 5,
        shadowColor: '#000000',
        shadowOffset: {
            width: 0,
            height: -2,
        },
        shadowOpacity: 0.1,
        shadowRadius: 4,
        elevation: 8,
    },
    tabButton: {
        flex: 1,
        paddingVertical: 12,
        alignItems: 'center',
        borderRadius: 8,
        marginHorizontal: 2,
    },
    activeTab: {
        backgroundColor: '#e8f5e8',
    },
    tabText: {
        fontSize: 12,
        fontWeight: '500',
        color: '#666666',
        textAlign: 'center',
    },
    activeTabText: {
        fontWeight: '700',
        color: '#4a9f3a',
    },
});

export default Router;
