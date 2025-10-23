import React, { useState } from 'react';
import {
    View,
    Text,
    TouchableOpacity,
    StyleSheet,
    ScrollView
} from 'react-native';
import Icon from 'react-native-vector-icons/Ionicons';
import * as Colors from '../constants/Colors';

const CosegurosScreen = ({ onBack }) => {
    const [selectedMonth, setSelectedMonth] = useState('octubre');

    const meses = [
        { id: 'octubre', label: 'Octubre 2025' },
        { id: 'septiembre', label: 'Septiembre 2025' },
        { id: 'agosto', label: 'Agosto 2025' },
    ];

    const coseguros = {
        octubre: [
            { id: 1, fecha: '10/10/2025', concepto: 'Consulta Cardiología', prestador: 'Sanatorio San Carlos', monto: 1500 },
            { id: 2, fecha: '05/10/2025', concepto: 'Análisis de sangre', prestador: 'Lab. Bioquímico', monto: 800 },
        ],
        septiembre: [
            { id: 3, fecha: '28/09/2025', concepto: 'Consulta Clínica Médica', prestador: 'Centro IPROSS', monto: 1200 },
            { id: 4, fecha: '15/09/2025', concepto: 'Medicamentos', prestador: 'Farmacia del Pueblo', monto: 2300 },
        ],
        agosto: [
            { id: 5, fecha: '20/08/2025', concepto: 'Consulta Oftalmología', prestador: 'Centro Visual', monto: 1400 },
        ],
    };

    const currentCoseguros = coseguros[selectedMonth] || [];
    const totalMes = currentCoseguros.reduce((sum, c) => sum + c.monto, 0);

    return (
        <View style={styles.container}>
            {/* Header */}
            <View style={styles.header}>
                <TouchableOpacity onPress={onBack} style={styles.backButton}>
                    <Icon name="arrow-back" size={24} color={Colors.textDark} />
                </TouchableOpacity>
                <Text style={styles.headerTitle}>Coseguros</Text>
                <View style={styles.placeholder} />
            </View>

            <ScrollView style={styles.content}>
                {/* Resumen Total */}
                <View style={styles.totalCard}>
                    <Icon name="cash" size={32} color={Colors.primary} />
                    <View style={styles.totalInfo}>
                        <Text style={styles.totalLabel}>Total del Mes</Text>
                        <Text style={styles.totalAmount}>${totalMes}</Text>
                    </View>
                </View>

                {/* Selector de Mes */}
                <View style={styles.section}>
                    <Text style={styles.sectionTitle}>Seleccionar Período</Text>
                    <ScrollView 
                        horizontal 
                        showsHorizontalScrollIndicator={false}
                        style={styles.monthSelector}
                    >
                        {meses.map((mes) => (
                            <TouchableOpacity
                                key={mes.id}
                                style={[
                                    styles.monthChip,
                                    selectedMonth === mes.id && styles.monthChipActive
                                ]}
                                onPress={() => setSelectedMonth(mes.id)}
                            >
                                <Text style={[
                                    styles.monthText,
                                    selectedMonth === mes.id && styles.monthTextActive
                                ]}>
                                    {mes.label}
                                </Text>
                            </TouchableOpacity>
                        ))}
                    </ScrollView>
                </View>

                {/* Lista de Coseguros */}
                <View style={styles.section}>
                    <Text style={styles.sectionTitle}>Detalle de Coseguros</Text>
                    
                    {currentCoseguros.length === 0 ? (
                        <View style={styles.emptyState}>
                            <Icon name="document-text-outline" size={64} color={Colors.grisOscuro} />
                            <Text style={styles.emptyText}>No hay coseguros registrados</Text>
                            <Text style={styles.emptySubtext}>para este período</Text>
                        </View>
                    ) : (
                        currentCoseguros.map((coseguro) => (
                            <View key={coseguro.id} style={styles.coseguroCard}>
                                <View style={styles.coseguroHeader}>
                                    <View style={styles.coseguroIcon}>
                                        <Icon name="medical" size={20} color={Colors.primary} />
                                    </View>
                                    <View style={styles.coseguroInfo}>
                                        <Text style={styles.coseguroConcepto}>{coseguro.concepto}</Text>
                                        <Text style={styles.coseguroFecha}>{coseguro.fecha}</Text>
                                    </View>
                                    <Text style={styles.coseguroMonto}>${coseguro.monto}</Text>
                                </View>
                                
                                <View style={styles.coseguroFooter}>
                                    <Icon name="business" size={14} color={Colors.grisOscuro} />
                                    <Text style={styles.prestadorText}>{coseguro.prestador}</Text>
                                </View>
                            </View>
                        ))
                    )}
                </View>

                {/* Información Adicional */}
                <View style={styles.infoCard}>
                    <Icon name="information-circle" size={24} color={Colors.primary} />
                    <View style={styles.infoContent}>
                        <Text style={styles.infoTitle}>Sobre los Coseguros</Text>
                        <Text style={styles.infoText}>
                            Los coseguros son los montos que el afiliado debe abonar por las prestaciones médicas recibidas.
                            El valor varía según el tipo de prestación y categoría del afiliado.
                        </Text>
                    </View>
                </View>

                {/* Botones de Acción */}
                <View style={styles.actionButtons}>
                    <TouchableOpacity 
                        style={styles.actionButton}
                        onPress={() => alert('Descargar comprobante - En construcción')}
                    >
                        <Icon name="download" size={20} color={Colors.white} />
                        <Text style={styles.actionButtonText}>Descargar Comprobante</Text>
                    </TouchableOpacity>

                    <TouchableOpacity 
                        style={[styles.actionButton, styles.actionButtonSecondary]}
                        onPress={() => alert('Ver historial completo - En construcción')}
                    >
                        <Icon name="time" size={20} color={Colors.primary} />
                        <Text style={[styles.actionButtonText, styles.actionButtonTextSecondary]}>
                            Ver Historial Completo
                        </Text>
                    </TouchableOpacity>
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
        paddingHorizontal: 16,
        paddingVertical: 14,
        elevation: 4,
    },
    backButton: {
        padding: 4,
    },
    headerTitle: {
        fontSize: 20,
        fontWeight: '600',
        color: Colors.textDark,
    },
    placeholder: {
        width: 32,
    },
    content: {
        flex: 1,
    },
    totalCard: {
        flexDirection: 'row',
        alignItems: 'center',
        backgroundColor: Colors.white,
        marginHorizontal: 16,
        marginTop: 16,
        padding: 20,
        borderRadius: 16,
        elevation: 4,
        borderLeftWidth: 4,
        borderLeftColor: Colors.primary,
    },
    totalInfo: {
        marginLeft: 16,
        flex: 1,
    },
    totalLabel: {
        fontSize: 14,
        color: Colors.grisOscuro,
        marginBottom: 4,
    },
    totalAmount: {
        fontSize: 32,
        fontWeight: 'bold',
        color: Colors.textDark,
    },
    section: {
        marginTop: 24,
        marginBottom: 8,
    },
    sectionTitle: {
        fontSize: 16,
        fontWeight: '600',
        color: Colors.textDark,
        marginHorizontal: 16,
        marginBottom: 12,
    },
    monthSelector: {
        paddingHorizontal: 16,
    },
    monthChip: {
        backgroundColor: Colors.white,
        paddingHorizontal: 20,
        paddingVertical: 10,
        borderRadius: 20,
        marginRight: 10,
        elevation: 2,
    },
    monthChipActive: {
        backgroundColor: Colors.primary,
    },
    monthText: {
        fontSize: 14,
        color: Colors.grisOscuro,
        fontWeight: '500',
    },
    monthTextActive: {
        color: Colors.white,
        fontWeight: '600',
    },
    coseguroCard: {
        backgroundColor: Colors.white,
        marginHorizontal: 16,
        marginBottom: 12,
        borderRadius: 12,
        padding: 16,
        elevation: 2,
    },
    coseguroHeader: {
        flexDirection: 'row',
        alignItems: 'center',
        marginBottom: 12,
    },
    coseguroIcon: {
        width: 40,
        height: 40,
        borderRadius: 20,
        backgroundColor: Colors.light1,
        justifyContent: 'center',
        alignItems: 'center',
    },
    coseguroInfo: {
        flex: 1,
        marginLeft: 12,
    },
    coseguroConcepto: {
        fontSize: 15,
        fontWeight: '600',
        color: Colors.textDark,
        marginBottom: 4,
    },
    coseguroFecha: {
        fontSize: 12,
        color: Colors.grisOscuro,
    },
    coseguroMonto: {
        fontSize: 18,
        fontWeight: 'bold',
        color: Colors.primary,
    },
    coseguroFooter: {
        flexDirection: 'row',
        alignItems: 'center',
        paddingTop: 12,
        borderTopWidth: 1,
        borderTopColor: Colors.light1,
    },
    prestadorText: {
        fontSize: 13,
        color: Colors.grisOscuro,
        marginLeft: 8,
    },
    emptyState: {
        alignItems: 'center',
        padding: 40,
    },
    emptyText: {
        fontSize: 16,
        fontWeight: '600',
        color: Colors.grisOscuro,
        marginTop: 16,
    },
    emptySubtext: {
        fontSize: 13,
        color: Colors.grisOscuro,
        marginTop: 4,
    },
    infoCard: {
        flexDirection: 'row',
        backgroundColor: Colors.light1,
        marginHorizontal: 16,
        marginTop: 16,
        padding: 16,
        borderRadius: 12,
    },
    infoContent: {
        flex: 1,
        marginLeft: 12,
    },
    infoTitle: {
        fontSize: 14,
        fontWeight: '600',
        color: Colors.textDark,
        marginBottom: 6,
    },
    infoText: {
        fontSize: 12,
        color: Colors.grisOscuro,
        lineHeight: 18,
    },
    actionButtons: {
        marginHorizontal: 16,
        marginTop: 24,
        marginBottom: 24,
    },
    actionButton: {
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: Colors.primary,
        paddingVertical: 14,
        borderRadius: 12,
        marginBottom: 12,
        elevation: 2,
    },
    actionButtonSecondary: {
        backgroundColor: Colors.white,
        borderWidth: 2,
        borderColor: Colors.primary,
    },
    actionButtonText: {
        fontSize: 15,
        fontWeight: '600',
        color: Colors.white,
        marginLeft: 8,
    },
    actionButtonTextSecondary: {
        color: Colors.primary,
    },
});

export default CosegurosScreen;
