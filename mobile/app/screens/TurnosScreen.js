import React, { useState } from 'react';
import {
    View,
    Text,
    TouchableOpacity,
    StyleSheet,
    ScrollView,
    Modal,
    TextInput
} from 'react-native';
import Icon from 'react-native-vector-icons/Ionicons';
import * as Colors from '../constants/Colors';

const TurnosScreen = ({ onBack }) => {
    const [showSolicitar, setShowSolicitar] = useState(false);
    const [selectedEspecialidad, setSelectedEspecialidad] = useState('');
    const [selectedPrestador, setSelectedPrestador] = useState('');
    const [selectedLocalidad, setSelectedLocalidad] = useState('');

    const especialidades = [
        'Clínica Médica',
        'Cardiología',
        'Pediatría',
        'Traumatología',
        'Dermatología',
        'Ginecología',
        'Oftalmología',
        'Odontología'
    ];

    const prestadores = [
        'Hospital Público de Viedma',
        'Sanatorio San Carlos',
        'Centro Médico IPROSS',
        'Clínica del Valle'
    ];

    const localidades = [
        'Viedma',
        'Carmen de Patagones',
        'San Antonio Oeste',
        'General Roca',
        'Cipolletti'
    ];

    const turnosAgendados = [
        {
            id: 1,
            especialidad: 'Cardiología',
            prestador: 'Sanatorio San Carlos',
            profesional: 'Dr. Juan Pérez',
            fecha: '20/10/2025',
            hora: '10:30',
            localidad: 'Viedma',
            estado: 'confirmado'
        },
        {
            id: 2,
            especialidad: 'Clínica Médica',
            prestador: 'Centro Médico IPROSS',
            profesional: 'Dra. María González',
            fecha: '25/10/2025',
            hora: '14:00',
            localidad: 'Viedma',
            estado: 'pendiente'
        }
    ];

    const handleSolicitar = () => {
        if (!selectedEspecialidad || !selectedPrestador || !selectedLocalidad) {
            alert('Por favor complete todos los campos');
            return;
        }
        alert(`Turno solicitado:\nEspecialidad: ${selectedEspecialidad}\nPrestador: ${selectedPrestador}\nLocalidad: ${selectedLocalidad}\n\nRecibirá confirmación en las próximas 24hs.`);
        setShowSolicitar(false);
        setSelectedEspecialidad('');
        setSelectedPrestador('');
        setSelectedLocalidad('');
    };

    return (
        <View style={styles.container}>
            {/* Header */}
            <View style={styles.header}>
                <TouchableOpacity onPress={onBack} style={styles.backButton}>
                    <Icon name="arrow-back" size={24} color={Colors.textDark} />
                </TouchableOpacity>
                <Text style={styles.headerTitle}>Mis Turnos</Text>
                <View style={styles.placeholder} />
            </View>

            <ScrollView style={styles.content}>
                {/* Botón Solicitar Turno */}
                <TouchableOpacity 
                    style={styles.solicitarButton}
                    onPress={() => setShowSolicitar(true)}
                >
                    <Icon name="add-circle" size={24} color={Colors.white} />
                    <Text style={styles.solicitarButtonText}>Solicitar Nuevo Turno</Text>
                </TouchableOpacity>

                {/* Turnos Agendados */}
                <View style={styles.section}>
                    <Text style={styles.sectionTitle}>Turnos Agendados</Text>
                    
                    {turnosAgendados.map((turno) => (
                        <View key={turno.id} style={styles.turnoCard}>
                            <View style={styles.turnoHeader}>
                                <Text style={styles.turnoEspecialidad}>{turno.especialidad}</Text>
                                <View style={[
                                    styles.estadoBadge,
                                    turno.estado === 'confirmado' ? styles.confirmado : styles.pendiente
                                ]}>
                                    <Text style={styles.estadoText}>
                                        {turno.estado.toUpperCase()}
                                    </Text>
                                </View>
                            </View>
                            
                            <View style={styles.turnoInfo}>
                                <Icon name="medical" size={16} color={Colors.grisOscuro} />
                                <Text style={styles.turnoText}>{turno.prestador}</Text>
                            </View>
                            
                            <View style={styles.turnoInfo}>
                                <Icon name="person" size={16} color={Colors.grisOscuro} />
                                <Text style={styles.turnoText}>{turno.profesional}</Text>
                            </View>
                            
                            <View style={styles.turnoInfo}>
                                <Icon name="calendar" size={16} color={Colors.grisOscuro} />
                                <Text style={styles.turnoText}>{turno.fecha} - {turno.hora}</Text>
                            </View>
                            
                            <View style={styles.turnoInfo}>
                                <Icon name="location" size={16} color={Colors.grisOscuro} />
                                <Text style={styles.turnoText}>{turno.localidad}</Text>
                            </View>

                            <View style={styles.turnoActions}>
                                <TouchableOpacity 
                                    style={styles.actionButtonSmall}
                                    onPress={() => alert('Cancelar turno - funcionalidad en construcción')}
                                >
                                    <Text style={styles.cancelButtonText}>Cancelar</Text>
                                </TouchableOpacity>
                                <TouchableOpacity 
                                    style={styles.actionButtonPrimary}
                                    onPress={() => alert('Reprogramar turno - funcionalidad en construcción')}
                                >
                                    <Text style={styles.primaryButtonText}>Reprogramar</Text>
                                </TouchableOpacity>
                            </View>
                        </View>
                    ))}
                </View>

                {/* Historial */}
                <View style={styles.section}>
                    <Text style={styles.sectionTitle}>Historial de Turnos</Text>
                    <View style={styles.emptyState}>
                        <Icon name="time-outline" size={48} color={Colors.grisOscuro} />
                        <Text style={styles.emptyText}>No hay turnos anteriores</Text>
                    </View>
                </View>
            </ScrollView>

            {/* Modal Solicitar Turno */}
            <Modal
                visible={showSolicitar}
                transparent={true}
                animationType="slide"
                onRequestClose={() => setShowSolicitar(false)}
            >
                <View style={styles.modalContainer}>
                    <View style={styles.modalContent}>
                        <View style={styles.modalHeader}>
                            <Text style={styles.modalTitle}>Solicitar Turno</Text>
                            <TouchableOpacity onPress={() => setShowSolicitar(false)}>
                                <Icon name="close" size={24} color={Colors.textDark} />
                            </TouchableOpacity>
                        </View>

                        <ScrollView style={styles.modalBody}>
                            {/* Especialidad */}
                            <Text style={styles.inputLabel}>Especialidad *</Text>
                            <View style={styles.pickerContainer}>
                                {especialidades.map((esp) => (
                                    <TouchableOpacity
                                        key={esp}
                                        style={[
                                            styles.option,
                                            selectedEspecialidad === esp && styles.optionSelected
                                        ]}
                                        onPress={() => setSelectedEspecialidad(esp)}
                                    >
                                        <Text style={[
                                            styles.optionText,
                                            selectedEspecialidad === esp && styles.optionTextSelected
                                        ]}>
                                            {esp}
                                        </Text>
                                        {selectedEspecialidad === esp && (
                                            <Icon name="checkmark-circle" size={20} color={Colors.primary} />
                                        )}
                                    </TouchableOpacity>
                                ))}
                            </View>

                            {/* Prestador */}
                            <Text style={styles.inputLabel}>Prestador *</Text>
                            <View style={styles.pickerContainer}>
                                {prestadores.map((prest) => (
                                    <TouchableOpacity
                                        key={prest}
                                        style={[
                                            styles.option,
                                            selectedPrestador === prest && styles.optionSelected
                                        ]}
                                        onPress={() => setSelectedPrestador(prest)}
                                    >
                                        <Text style={[
                                            styles.optionText,
                                            selectedPrestador === prest && styles.optionTextSelected
                                        ]}>
                                            {prest}
                                        </Text>
                                        {selectedPrestador === prest && (
                                            <Icon name="checkmark-circle" size={20} color={Colors.primary} />
                                        )}
                                    </TouchableOpacity>
                                ))}
                            </View>

                            {/* Localidad */}
                            <Text style={styles.inputLabel}>Localidad *</Text>
                            <View style={styles.pickerContainer}>
                                {localidades.map((loc) => (
                                    <TouchableOpacity
                                        key={loc}
                                        style={[
                                            styles.option,
                                            selectedLocalidad === loc && styles.optionSelected
                                        ]}
                                        onPress={() => setSelectedLocalidad(loc)}
                                    >
                                        <Text style={[
                                            styles.optionText,
                                            selectedLocalidad === loc && styles.optionTextSelected
                                        ]}>
                                            {loc}
                                        </Text>
                                        {selectedLocalidad === loc && (
                                            <Icon name="checkmark-circle" size={20} color={Colors.primary} />
                                        )}
                                    </TouchableOpacity>
                                ))}
                            </View>

                            <View style={styles.infoBox}>
                                <Icon name="information-circle" size={20} color={Colors.primary} />
                                <Text style={styles.infoText}>
                                    Su solicitud será procesada y recibirá confirmación con fecha y hora disponible en las próximas 24 horas.
                                </Text>
                            </View>
                        </ScrollView>

                        <View style={styles.modalFooter}>
                            <TouchableOpacity 
                                style={styles.cancelButton}
                                onPress={() => setShowSolicitar(false)}
                            >
                                <Text style={styles.cancelButtonText}>Cancelar</Text>
                            </TouchableOpacity>
                            <TouchableOpacity 
                                style={styles.confirmButton}
                                onPress={handleSolicitar}
                            >
                                <Text style={styles.confirmButtonText}>Solicitar</Text>
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
    solicitarButton: {
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: Colors.primary,
        marginHorizontal: 16,
        marginTop: 16,
        padding: 16,
        borderRadius: 12,
        elevation: 3,
    },
    solicitarButtonText: {
        fontSize: 16,
        fontWeight: '600',
        color: Colors.white,
        marginLeft: 8,
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
    turnoCard: {
        backgroundColor: Colors.white,
        marginHorizontal: 16,
        marginBottom: 12,
        borderRadius: 12,
        padding: 16,
        elevation: 2,
    },
    turnoHeader: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: 12,
    },
    turnoEspecialidad: {
        fontSize: 18,
        fontWeight: '600',
        color: Colors.textDark,
        flex: 1,
    },
    estadoBadge: {
        paddingHorizontal: 12,
        paddingVertical: 4,
        borderRadius: 12,
    },
    confirmado: {
        backgroundColor: '#6ac64f',
    },
    pendiente: {
        backgroundColor: '#ff9800',
    },
    estadoText: {
        fontSize: 10,
        fontWeight: '600',
        color: Colors.white,
    },
    turnoInfo: {
        flexDirection: 'row',
        alignItems: 'center',
        marginBottom: 8,
    },
    turnoText: {
        fontSize: 14,
        color: Colors.grisOscuro,
        marginLeft: 8,
    },
    turnoActions: {
        flexDirection: 'row',
        justifyContent: 'flex-end',
        marginTop: 12,
        gap: 8,
    },
    actionButtonSmall: {
        paddingHorizontal: 16,
        paddingVertical: 8,
        borderRadius: 8,
        borderWidth: 1,
        borderColor: '#d32f2f',
    },
    actionButtonPrimary: {
        paddingHorizontal: 16,
        paddingVertical: 8,
        borderRadius: 8,
        backgroundColor: Colors.primary,
    },
    primaryButtonText: {
        fontSize: 14,
        fontWeight: '600',
        color: Colors.white,
    },
    emptyState: {
        alignItems: 'center',
        padding: 40,
    },
    emptyText: {
        fontSize: 14,
        color: Colors.grisOscuro,
        marginTop: 12,
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
        maxHeight: '90%',
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
    inputLabel: {
        fontSize: 14,
        fontWeight: '600',
        color: Colors.textDark,
        marginBottom: 8,
        marginTop: 12,
    },
    pickerContainer: {
        marginBottom: 16,
    },
    option: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        padding: 12,
        borderRadius: 8,
        borderWidth: 1,
        borderColor: Colors.light2,
        marginBottom: 8,
    },
    optionSelected: {
        borderColor: Colors.primary,
        backgroundColor: Colors.light1,
    },
    optionText: {
        fontSize: 14,
        color: Colors.textDark,
    },
    optionTextSelected: {
        color: Colors.primary,
        fontWeight: '600',
    },
    infoBox: {
        flexDirection: 'row',
        backgroundColor: Colors.light1,
        padding: 12,
        borderRadius: 8,
        marginTop: 16,
    },
    infoText: {
        fontSize: 12,
        color: Colors.grisOscuro,
        marginLeft: 8,
        flex: 1,
    },
    modalFooter: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        padding: 20,
        borderTopWidth: 1,
        borderTopColor: Colors.light1,
        gap: 12,
    },
    cancelButton: {
        flex: 1,
        paddingVertical: 12,
        borderRadius: 8,
        borderWidth: 1,
        borderColor: Colors.grisOscuro,
        alignItems: 'center',
    },
    cancelButtonText: {
        fontSize: 14,
        fontWeight: '600',
        color: '#d32f2f',
    },
    confirmButton: {
        flex: 1,
        paddingVertical: 12,
        borderRadius: 8,
        backgroundColor: Colors.primary,
        alignItems: 'center',
    },
    confirmButtonText: {
        fontSize: 14,
        fontWeight: '600',
        color: Colors.white,
    },
});

export default TurnosScreen;
