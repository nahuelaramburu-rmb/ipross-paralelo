import React, { useState } from 'react';
import { View, Text, TouchableOpacity, StyleSheet, Image, Alert, ScrollView } from 'react-native';

const Router = () => {
    const [currentScreen, setCurrentScreen] = useState('BeneficiaryInformation');

    const renderBeneficiaryInformation = () => (
        <ScrollView style={styles.screenContainer}>
            <View style={styles.header}>
                <Image
                    source={require('../images/ipross_logo_green.jpg')}
                    style={styles.headerLogo}
                    resizeMode='contain'
                />
                <Text style={styles.headerTitle}>IPROSS Beneficiario</Text>
                <Text style={styles.headerSubtitle}>Panel Principal</Text>
            </View>

            <View style={styles.contentContainer}>
                <Text style={styles.sectionTitle}>Mi Información</Text>

                <View style={styles.infoCard}>
                    <Text style={styles.cardTitle}>Datos del Beneficiario</Text>
                    <Text style={styles.cardSubtext}>Consulte su información personal</Text>
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
        backgroundColor: '#6ac54e',
        paddingTop: 50,
        paddingBottom: 20,
        paddingHorizontal: 20,
        alignItems: 'center',
    },
    headerLogo: {
        width: 120,
        height: 60,
        marginBottom: 10,
    },
    headerTitle: {
        fontSize: 24,
        fontWeight: 'bold',
        color: '#ffffff',
        marginBottom: 5,
    },
    headerSubtitle: {
        fontSize: 16,
        color: '#ffffff',
        opacity: 0.8,
    },
    contentContainer: {
        flex: 1,
        padding: 20,
    },
    sectionTitle: {
        fontSize: 20,
        fontWeight: 'bold',
        color: '#1e293b',
        marginBottom: 15,
        marginTop: 10,
    },
    infoCard: {
        backgroundColor: '#ffffff',
        borderRadius: 12,
        padding: 20,
        marginBottom: 15,
        shadowColor: '#000',
        shadowOffset: {
            width: 0,
            height: 2,
        },
        shadowOpacity: 0.1,
        shadowRadius: 4,
        elevation: 3,
    },
    cardTitle: {
        fontSize: 18,
        fontWeight: '600',
        color: '#1e293b',
        marginBottom: 8,
    },
    cardSubtext: {
        fontSize: 14,
        color: '#6b7280',
    },
    actionButton: {
        backgroundColor: '#4a9f3a',
        borderRadius: 12,
        paddingVertical: 15,
        paddingHorizontal: 20,
        marginBottom: 12,
        alignItems: 'center',
    },
    actionButtonText: {
        color: '#ffffff',
        fontSize: 16,
        fontWeight: '600',
    },
    bottomTabBar: {
        flexDirection: 'row',
        backgroundColor: '#ffffff',
        borderTopWidth: 1,
        borderTopColor: '#e5e7eb',
        paddingVertical: 8,
        paddingHorizontal: 5,
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
        color: '#6b7280',
        textAlign: 'center',
    },
    activeTabText: {
        color: '#4a9f3a',
        fontWeight: '600',
    },
});

export default Router;
