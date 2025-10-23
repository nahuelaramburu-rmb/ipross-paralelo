import React from 'react';
import { View, Text, StyleSheet } from 'react-native';

const ProfessionalMapsScreenTemp = () => {
    return (
        <View style={styles.container}>
            <Text style={styles.title}>Mapas de Prestadores</Text>
            <Text style={styles.subtitle}>Funcionalidad temporalmente deshabilitada</Text>
            <Text style={styles.message}>
                Esta función será restaurada próximamente una vez que se actualicen las dependencias.
            </Text>
        </View>
    );
};

const styles = StyleSheet.create({
    container: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
        padding: 20,
        backgroundColor: '#f5f5f5',
    },
    title: {
        fontSize: 24,
        fontWeight: 'bold',
        color: '#333',
        marginBottom: 10,
    },
    subtitle: {
        fontSize: 18,
        color: '#666',
        marginBottom: 20,
        textAlign: 'center',
    },
    message: {
        fontSize: 16,
        color: '#888',
        textAlign: 'center',
        lineHeight: 24,
    },
});

export default ProfessionalMapsScreenTemp;