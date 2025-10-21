import React, { useState } from 'react';
import {
    View,
    Text,
    TouchableOpacity,
    StyleSheet,
    ScrollView,
    Modal
} from 'react-native';
import Icon from 'react-native-vector-icons/Ionicons';
import * as Colors from '../constants/Colors';

const AtencionesScreen = ({ onBack }) => {
    const [selectedAtencion, setSelectedAtencion] = useState(null);

    const atenciones = [
        {
            id: 1,
            fecha: '10/10/2025',
            tipo: 'Consulta',
            especialidad: 'Cardiología',
            prestador: 'Sanatorio San Carlos',
            profesional: 'Dr. Juan Pérez',
            diagnostico: 'Control cardiológico de rutina',
            estado: 'finalizada',
            coseguro: 1500
        },
        {
            id: 2,
            fecha: '05/10/2025',
            tipo: 'Estudio',
            especialidad: 'Laboratorio',
            prestador: 'Lab. Bioquímico Central',
            profesional: 'Bioq. María García',
            diagnostico: 'Análisis de sangre completo',
            estado: 'finalizada',
            coseguro: 800
        },
        {
            id: 3,
            fecha: '28/09/2025',
            tipo: 'Consulta',
            especialidad: 'Clínica Médica',
            prestador: 'Centro Médico IPROSS',
            profesional: 'Dra. Ana López',
            diagnostico: 'Consulta clínica general',
            estado: 'finalizada',
            coseguro: 1200
        },
        {
            id: 4,
            fecha: '15/09/2025',
            tipo: 'Medicamentos',
            especialidad: 'Farmacia',
            prestador: 'Farmacia del Pueblo',
            profesional: '-',
            diagnostico: 'Dispensación de medicamentos recetados',
            estado: 'finalizada',
            coseguro: 2300
        }
    ];

    const getIconForTipo = (tipo) => {
        switch (tipo) {
            case 'Consulta':
                return 'medical';
            case 'Estudio':
                return 'flask';
            case 'Medicamentos':
                return 'fitness';
            default:
                return 'document-text';
        }
    };

    return (
        <View style={styles.container}>
            {/* Header */}
            <View style={styles.header}>
                <TouchableOpacity onPress={onBack} style={styles.backButton}>
                    <Icon name="arrow-back" size={24} color={Colors.textDark} />
                </TouchableOpacity>
                <Text style={styles.headerTitle}>Mis Atenciones</Text>
                <View style={styles.placeholder} />
            </View>

            <ScrollView style={styles.content}>
                {/* Resumen */}
                <View style={styles.summaryCard}>
                    <Text style={styles.summaryTitle}>Resumen del Mes</Text>
                    <View style={styles.summaryRow}>
                        <View style={styles.summaryItem}>
                            <Text style={styles.summaryNumber}>{atenciones.length}</Text>
                            <Text style={styles.summaryLabel}>Atenciones</Text>
                        </View>
                        <View style={styles.summaryDivider} />
                        <View style={styles.summaryItem}>
                            <Text style={styles.summaryNumber}>
                                ${atenciones.reduce((sum, a) => sum + a.coseguro, 0)}
                            </Text>
                            <Text style={styles.summaryLabel}>Total Coseguros</Text>
                        </View>
                    </View>
                </View>

                {/* Lista de Atenciones */}
                <View style={styles.section}>
                    <Text style={styles.sectionTitle}>Historial de Atenciones</Text>
                    
                    {atenciones.map((atencion) => (
                        <TouchableOpacity 
                            key={atencion.id} 
                            style={styles.atencionCard}
                            onPress={() => setSelectedAtencion(atencion)}
                        >
                            <View style={styles.atencionHeader}>
                                <View style={styles.atencionIconContainer}>
                                    <Icon 
                                        name={getIconForTipo(atencion.tipo)} 
                                        size={24} 
                                        color={Colors.primary} 
                                    />
                                </View>
                                <View style={styles.atencionInfo}>
                                    <Text style={styles.atencionTipo}>{atencion.tipo}</Text>
                                    <Text style={styles.atencionEspecialidad}>{atencion.especialidad}</Text>
                                </View>
                                <Text style={styles.atencionFecha}>{atencion.fecha}</Text>
                            </View>
                            
                            <View style={styles.atencionBody}>
                                <View style={styles.infoRow}>
                                    <Icon name="business" size={14} color={Colors.grisOscuro} />
                                    <Text style={styles.infoText}>{atencion.prestador}</Text>
                                </View>
                                {atencion.profesional !== '-' && (
                                    <View style={styles.infoRow}>
                                        <Icon name="person" size={14} color={Colors.grisOscuro} />
                                        <Text style={styles.infoText}>{atencion.profesional}</Text>
                                    </View>
                                )}
                            </View>

                            <View style={styles.atencionFooter}>
                                <View style={styles.estadoBadge}>
                                    <Text style={styles.estadoText}>FINALIZADA</Text>
                                </View>
                                <Text style={styles.coseguroText}>Coseguro: ${atencion.coseguro}</Text>
                            </View>
                        </TouchableOpacity>
                    ))}
                </View>
            </ScrollView>

            {/* Modal Detalle */}
            <Modal
                visible={selectedAtencion !== null}
                transparent={true}
                animationType="slide"
                onRequestClose={() => setSelectedAtencion(null)}
            >
                <View style={styles.modalContainer}>
                    <View style={styles.modalContent}>
                        <View style={styles.modalHeader}>
                            <Text style={styles.modalTitle}>Detalle de Atención</Text>
                            <TouchableOpacity onPress={() => setSelectedAtencion(null)}>
                                <Icon name="close" size={24} color={Colors.textDark} />
                            </TouchableOpacity>
                        </View>

                        {selectedAtencion && (
                            <ScrollView style={styles.modalBody}>
                                <View style={styles.detailSection}>
                                    <Text style={styles.detailLabel}>Fecha</Text>
                                    <Text style={styles.detailValue}>{selectedAtencion.fecha}</Text>
                                </View>

                                <View style={styles.detailSection}>
                                    <Text style={styles.detailLabel}>Tipo de Atención</Text>
                                    <Text style={styles.detailValue}>{selectedAtencion.tipo}</Text>
                                </View>

                                <View style={styles.detailSection}>
                                    <Text style={styles.detailLabel}>Especialidad</Text>
                                    <Text style={styles.detailValue}>{selectedAtencion.especialidad}</Text>
                                </View>

                                <View style={styles.detailSection}>
                                    <Text style={styles.detailLabel}>Prestador</Text>
                                    <Text style={styles.detailValue}>{selectedAtencion.prestador}</Text>
                                </View>

                                {selectedAtencion.profesional !== '-' && (
                                    <View style={styles.detailSection}>
                                        <Text style={styles.detailLabel}>Profesional</Text>
                                        <Text style={styles.detailValue}>{selectedAtencion.profesional}</Text>
                                    </View>
                                )}

                                <View style={styles.detailSection}>
                                    <Text style={styles.detailLabel}>Diagnóstico</Text>
                                    <Text style={styles.detailValue}>{selectedAtencion.diagnostico}</Text>
                                </View>

                                <View style={styles.detailSection}>
                                    <Text style={styles.detailLabel}>Coseguro</Text>
                                    <Text style={styles.detailValueHighlight}>${selectedAtencion.coseguro}</Text>
                                </View>

                                <View style={styles.detailSection}>
                                    <Text style={styles.detailLabel}>Estado</Text>
                                    <View style={styles.estadoBadgeLarge}>
                                        <Text style={styles.estadoTextLarge}>FINALIZADA</Text>
                                    </View>
                                </View>
                            </ScrollView>
                        )}

                        <View style={styles.modalFooter}>
                            <TouchableOpacity 
                                style={styles.closeButton}
                                onPress={() => setSelectedAtencion(null)}
                            >
                                <Text style={styles.closeButtonText}>Cerrar</Text>
                            </TouchableOpacity>
                        </View>
                    </View>
                </View>
            </Modal>
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
    summaryCard: {
        backgroundColor: Colors.white,
        marginHorizontal: 16,
        marginTop: 16,
        borderRadius: 12,
        padding: 20,
        elevation: 3,
    },
    summaryTitle: {
        fontSize: 16,
        fontWeight: '600',
        color: Colors.textDark,
        marginBottom: 16,
    },
    summaryRow: {
        flexDirection: 'row',
        justifyContent: 'space-around',
    },
    summaryItem: {
        alignItems: 'center',
        flex: 1,
    },
    summaryNumber: {
        fontSize: 28,
        fontWeight: 'bold',
        color: Colors.primary,
        marginBottom: 4,
    },
    summaryLabel: {
        fontSize: 12,
        color: Colors.grisOscuro,
    },
    summaryDivider: {
        width: 1,
        backgroundColor: Colors.light2,
        marginHorizontal: 20,
    },
    section: {
        marginTop: 24,
        marginBottom: 16,
    },
    sectionTitle: {
        fontSize: 18,
        fontWeight: '600',
        color: Colors.textDark,
        marginHorizontal: 16,
        marginBottom: 12,
    },
    atencionCard: {
        backgroundColor: Colors.white,
        marginHorizontal: 16,
        marginBottom: 12,
        borderRadius: 12,
        padding: 16,
        elevation: 2,
    },
    atencionHeader: {
        flexDirection: 'row',
        alignItems: 'center',
        marginBottom: 12,
    },
    atencionIconContainer: {
        width: 48,
        height: 48,
        borderRadius: 24,
        backgroundColor: Colors.light1,
        justifyContent: 'center',
        alignItems: 'center',
    },
    atencionInfo: {
        flex: 1,
        marginLeft: 12,
    },
    atencionTipo: {
        fontSize: 16,
        fontWeight: '600',
        color: Colors.textDark,
        marginBottom: 2,
    },
    atencionEspecialidad: {
        fontSize: 13,
        color: Colors.grisOscuro,
    },
    atencionFecha: {
        fontSize: 12,
        color: Colors.grisOscuro,
    },
    atencionBody: {
        marginBottom: 12,
    },
    infoRow: {
        flexDirection: 'row',
        alignItems: 'center',
        marginBottom: 6,
    },
    infoText: {
        fontSize: 13,
        color: Colors.grisOscuro,
        marginLeft: 8,
    },
    atencionFooter: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        paddingTop: 12,
        borderTopWidth: 1,
        borderTopColor: Colors.light1,
    },
    estadoBadge: {
        backgroundColor: '#6ac64f',
        paddingHorizontal: 12,
        paddingVertical: 4,
        borderRadius: 12,
    },
    estadoText: {
        fontSize: 10,
        fontWeight: '600',
        color: Colors.white,
    },
    coseguroText: {
        fontSize: 14,
        fontWeight: '600',
        color: Colors.primary,
    },

    // Modal Styles
    modalContainer: {
        flex: 1,
        backgroundColor: 'rgba(0,0,0,0.5)',
        justifyContent: 'flex-end',
    },
    modalContent: {
        backgroundColor: Colors.white,
        borderTopLeftRadius: 20,
        borderTopRightRadius: 20,
        maxHeight: '80%',
    },
    modalHeader: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        padding: 20,
        borderBottomWidth: 1,
        borderBottomColor: Colors.light1,
    },
    modalTitle: {
        fontSize: 20,
        fontWeight: '600',
        color: Colors.textDark,
    },
    modalBody: {
        padding: 20,
    },
    detailSection: {
        marginBottom: 20,
    },
    detailLabel: {
        fontSize: 12,
        fontWeight: '600',
        color: Colors.grisOscuro,
        marginBottom: 6,
        textTransform: 'uppercase',
    },
    detailValue: {
        fontSize: 16,
        color: Colors.textDark,
    },
    detailValueHighlight: {
        fontSize: 20,
        fontWeight: 'bold',
        color: Colors.primary,
    },
    estadoBadgeLarge: {
        backgroundColor: '#6ac64f',
        paddingHorizontal: 16,
        paddingVertical: 8,
        borderRadius: 8,
        alignSelf: 'flex-start',
    },
    estadoTextLarge: {
        fontSize: 12,
        fontWeight: '600',
        color: Colors.white,
    },
    modalFooter: {
        padding: 20,
        borderTopWidth: 1,
        borderTopColor: Colors.light1,
    },
    closeButton: {
        backgroundColor: Colors.primary,
        paddingVertical: 12,
        borderRadius: 8,
        alignItems: 'center',
    },
    closeButtonText: {
        fontSize: 16,
        fontWeight: '600',
        color: Colors.white,
    },
});

export default AtencionesScreen;
