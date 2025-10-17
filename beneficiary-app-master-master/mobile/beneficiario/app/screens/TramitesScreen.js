import React, { useState } from 'react';
import {
    View,
    Text,
    TouchableOpacity,
    StyleSheet,
    StatusBar,
    ScrollView,
    Modal,
} from 'react-native';
import Icon from 'react-native-vector-icons/Ionicons';
import * as Colors from '../constants/Colors';

const TramitesScreen = ({ onBack }) => {
    const [selectedTab, setSelectedTab] = useState('disponibles'); // disponibles, proceso, completados
    const [selectedTramite, setSelectedTramite] = useState(null);

    // Mock data de trámites disponibles
    const tramitesDisponibles = [
        {
            id: 1,
            titulo: 'Solicitud de Reintegro',
            descripcion: 'Solicite el reintegro de gastos médicos no cubiertos',
            icon: 'cash-outline',
            categoria: 'Reintegros',
            requiereDocumentacion: true,
            plazo: '15 días hábiles',
        },
        {
            id: 2,
            titulo: 'Cambio de Prestador',
            descripcion: 'Solicite el cambio de prestador de cabecera',
            icon: 'swap-horizontal-outline',
            categoria: 'Prestadores',
            requiereDocumentacion: false,
            plazo: '10 días hábiles',
        },
        {
            id: 3,
            titulo: 'Alta de Familiar',
            descripcion: 'Incorpore un nuevo integrante a su grupo familiar',
            icon: 'person-add-outline',
            categoria: 'Grupo Familiar',
            requiereDocumentacion: true,
            plazo: '20 días hábiles',
        },
        {
            id: 4,
            titulo: 'Autorización Médica',
            descripcion: 'Solicite autorización para estudios o tratamientos',
            icon: 'document-text-outline',
            categoria: 'Autorizaciones',
            requiereDocumentacion: true,
            plazo: '5 días hábiles',
        },
        {
            id: 5,
            titulo: 'Cambio de Domicilio',
            descripcion: 'Actualice sus datos de domicilio',
            icon: 'home-outline',
            categoria: 'Datos Personales',
            requiereDocumentacion: false,
            plazo: '7 días hábiles',
        },
    ];

    // Mock data de trámites en proceso
    const tramitesEnProceso = [
        {
            id: 101,
            titulo: 'Solicitud de Reintegro #2024-001234',
            descripcion: 'Reintegro de gastos por consulta médica particular',
            estado: 'En Revisión',
            progreso: 60,
            fechaInicio: '10/10/2025',
            ultimaActualizacion: '15/10/2025',
            icon: 'cash-outline',
        },
        {
            id: 102,
            titulo: 'Autorización Médica #2024-005678',
            descripcion: 'Autorización para resonancia magnética',
            estado: 'Pendiente de Documentación',
            progreso: 30,
            fechaInicio: '12/10/2025',
            ultimaActualizacion: '14/10/2025',
            icon: 'document-text-outline',
        },
    ];

    // Mock data de trámites completados
    const tramitesCompletados = [
        {
            id: 201,
            titulo: 'Cambio de Prestador #2024-000987',
            descripcion: 'Cambio a Centro Médico Norte',
            estado: 'Aprobado',
            fechaCompletado: '05/10/2025',
            icon: 'swap-horizontal-outline',
        },
        {
            id: 202,
            titulo: 'Alta de Familiar #2024-000654',
            descripcion: 'Incorporación de cónyuge',
            estado: 'Aprobado',
            fechaCompletado: '28/09/2025',
            icon: 'person-add-outline',
        },
    ];

    const getEstadoColor = (estado) => {
        switch (estado) {
            case 'En Revisión':
                return '#2196f3';
            case 'Pendiente de Documentación':
                return '#ff9800';
            case 'Aprobado':
                return '#4caf50';
            case 'Rechazado':
                return '#f44336';
            default:
                return Colors.grisOscuro;
        }
    };

    const renderDisponibles = () => (
        <View style={styles.tabContent}>
            {tramitesDisponibles.map((tramite) => (
                <TouchableOpacity
                    key={tramite.id}
                    style={styles.tramiteCard}
                    onPress={() => setSelectedTramite(tramite)}
                >
                    <View style={styles.tramiteIcon}>
                        <Icon name={tramite.icon} size={32} color={Colors.primary} />
                    </View>
                    <View style={styles.tramiteInfo}>
                        <Text style={styles.tramiteTitulo}>{tramite.titulo}</Text>
                        <Text style={styles.tramiteDescripcion}>{tramite.descripcion}</Text>
                        <View style={styles.tramiteMetadata}>
                            <View style={styles.tramiteChip}>
                                <Icon name="folder-outline" size={14} color={Colors.primary} />
                                <Text style={styles.tramiteChipText}>{tramite.categoria}</Text>
                            </View>
                            <View style={styles.tramiteChip}>
                                <Icon name="time-outline" size={14} color={Colors.grisOscuro} />
                                <Text style={styles.tramiteChipText}>{tramite.plazo}</Text>
                            </View>
                        </View>
                    </View>
                    <Icon name="chevron-forward" size={24} color={Colors.grisOscuro} />
                </TouchableOpacity>
            ))}
        </View>
    );

    const renderEnProceso = () => (
        <View style={styles.tabContent}>
            {tramitesEnProceso.length === 0 ? (
                <View style={styles.emptyState}>
                    <Icon name="documents-outline" size={64} color={Colors.light2} />
                    <Text style={styles.emptyStateText}>No hay trámites en proceso</Text>
                </View>
            ) : (
                tramitesEnProceso.map((tramite) => (
                    <View key={tramite.id} style={styles.tramiteCard}>
                        <View style={styles.tramiteIcon}>
                            <Icon name={tramite.icon} size={32} color={Colors.primary} />
                        </View>
                        <View style={styles.tramiteInfo}>
                            <Text style={styles.tramiteTitulo}>{tramite.titulo}</Text>
                            <Text style={styles.tramiteDescripcion}>{tramite.descripcion}</Text>
                            
                            {/* Estado */}
                            <View style={[
                                styles.estadoBadge,
                                { backgroundColor: getEstadoColor(tramite.estado) + '20' }
                            ]}>
                                <Icon 
                                    name="ellipse" 
                                    size={8} 
                                    color={getEstadoColor(tramite.estado)} 
                                />
                                <Text style={[
                                    styles.estadoText,
                                    { color: getEstadoColor(tramite.estado) }
                                ]}>
                                    {tramite.estado}
                                </Text>
                            </View>

                            {/* Barra de progreso */}
                            <View style={styles.progressContainer}>
                                <View style={styles.progressBar}>
                                    <View 
                                        style={[
                                            styles.progressFill,
                                            { width: `${tramite.progreso}%` }
                                        ]} 
                                    />
                                </View>
                                <Text style={styles.progressText}>{tramite.progreso}%</Text>
                            </View>

                            {/* Fechas */}
                            <View style={styles.fechasContainer}>
                                <Text style={styles.fechaText}>
                                    Iniciado: {tramite.fechaInicio}
                                </Text>
                                <Text style={styles.fechaText}>
                                    Actualizado: {tramite.ultimaActualizacion}
                                </Text>
                            </View>
                        </View>
                    </View>
                ))
            )}
        </View>
    );

    const renderCompletados = () => (
        <View style={styles.tabContent}>
            {tramitesCompletados.map((tramite) => (
                <View key={tramite.id} style={styles.tramiteCard}>
                    <View style={styles.tramiteIcon}>
                        <Icon name={tramite.icon} size={32} color={Colors.grisOscuro} />
                    </View>
                    <View style={styles.tramiteInfo}>
                        <Text style={styles.tramiteTitulo}>{tramite.titulo}</Text>
                        <Text style={styles.tramiteDescripcion}>{tramite.descripcion}</Text>
                        
                        {/* Estado */}
                        <View style={[
                            styles.estadoBadge,
                            { backgroundColor: getEstadoColor(tramite.estado) + '20' }
                        ]}>
                            <Icon 
                                name="checkmark-circle" 
                                size={16} 
                                color={getEstadoColor(tramite.estado)} 
                            />
                            <Text style={[
                                styles.estadoText,
                                { color: getEstadoColor(tramite.estado) }
                            ]}>
                                {tramite.estado}
                            </Text>
                        </View>

                        <Text style={styles.fechaText}>
                            Completado: {tramite.fechaCompletado}
                        </Text>
                    </View>
                </View>
            ))}
        </View>
    );

    return (
        <View style={styles.container}>
            <StatusBar barStyle="light-content" backgroundColor={Colors.primary} />
            
            {/* Header */}
            <View style={styles.header}>
                <TouchableOpacity onPress={onBack} style={styles.backButton}>
                    <Icon name="arrow-back" size={24} color="#fff" />
                </TouchableOpacity>
                <Text style={styles.headerTitle}>Mis Trámites</Text>
                <TouchableOpacity 
                    style={styles.helpButton}
                    onPress={() => alert('Ayuda - En construcción')}
                >
                    <Icon name="help-circle-outline" size={24} color="#fff" />
                </TouchableOpacity>
            </View>

            {/* Tabs */}
            <View style={styles.tabsContainer}>
                <TouchableOpacity
                    style={[styles.tab, selectedTab === 'disponibles' && styles.tabActive]}
                    onPress={() => setSelectedTab('disponibles')}
                >
                    <Text style={[
                        styles.tabText,
                        selectedTab === 'disponibles' && styles.tabTextActive
                    ]}>
                        Disponibles
                    </Text>
                </TouchableOpacity>
                <TouchableOpacity
                    style={[styles.tab, selectedTab === 'proceso' && styles.tabActive]}
                    onPress={() => setSelectedTab('proceso')}
                >
                    <Text style={[
                        styles.tabText,
                        selectedTab === 'proceso' && styles.tabTextActive
                    ]}>
                        En Proceso ({tramitesEnProceso.length})
                    </Text>
                </TouchableOpacity>
                <TouchableOpacity
                    style={[styles.tab, selectedTab === 'completados' && styles.tabActive]}
                    onPress={() => setSelectedTab('completados')}
                >
                    <Text style={[
                        styles.tabText,
                        selectedTab === 'completados' && styles.tabTextActive
                    ]}>
                        Completados
                    </Text>
                </TouchableOpacity>
            </View>

            {/* Content */}
            <ScrollView style={styles.content}>
                {selectedTab === 'disponibles' && renderDisponibles()}
                {selectedTab === 'proceso' && renderEnProceso()}
                {selectedTab === 'completados' && renderCompletados()}
            </ScrollView>

            {/* Modal de detalle */}
            {selectedTramite && (
                <Modal
                    animationType="slide"
                    transparent={true}
                    visible={selectedTramite !== null}
                    onRequestClose={() => setSelectedTramite(null)}
                >
                    <View style={styles.modalOverlay}>
                        <View style={styles.modalContent}>
                            <View style={styles.modalHeader}>
                                <Text style={styles.modalTitle}>{selectedTramite.titulo}</Text>
                                <TouchableOpacity onPress={() => setSelectedTramite(null)}>
                                    <Icon name="close" size={28} color={Colors.grisOscuro} />
                                </TouchableOpacity>
                            </View>

                            <ScrollView style={styles.modalBody}>
                                <View style={styles.modalSection}>
                                    <Text style={styles.modalSectionTitle}>Descripción</Text>
                                    <Text style={styles.modalText}>{selectedTramite.descripcion}</Text>
                                </View>

                                <View style={styles.modalSection}>
                                    <Text style={styles.modalSectionTitle}>Categoría</Text>
                                    <Text style={styles.modalText}>{selectedTramite.categoria}</Text>
                                </View>

                                <View style={styles.modalSection}>
                                    <Text style={styles.modalSectionTitle}>Plazo de Resolución</Text>
                                    <Text style={styles.modalText}>{selectedTramite.plazo}</Text>
                                </View>

                                <View style={styles.modalSection}>
                                    <Text style={styles.modalSectionTitle}>Documentación Requerida</Text>
                                    {selectedTramite.requiereDocumentacion ? (
                                        <View>
                                            <Text style={styles.modalListItem}>• DNI (frente y dorso)</Text>
                                            <Text style={styles.modalListItem}>• Carnet de afiliado</Text>
                                            <Text style={styles.modalListItem}>• Documentación específica del trámite</Text>
                                        </View>
                                    ) : (
                                        <Text style={styles.modalText}>No requiere documentación adicional</Text>
                                    )}
                                </View>

                                <TouchableOpacity 
                                    style={styles.iniciarButton}
                                    onPress={() => {
                                        setSelectedTramite(null);
                                        alert('Iniciar trámite - En construcción');
                                    }}
                                >
                                    <Icon name="arrow-forward-circle" size={24} color="#fff" />
                                    <Text style={styles.iniciarButtonText}>Iniciar Trámite</Text>
                                </TouchableOpacity>
                            </ScrollView>
                        </View>
                    </View>
                </Modal>
            )}
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
    helpButton: {
        padding: 8,
    },
    headerTitle: {
        fontSize: 20,
        fontWeight: 'bold',
        color: '#fff',
    },
    tabsContainer: {
        flexDirection: 'row',
        backgroundColor: '#fff',
        elevation: 2,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 1 },
        shadowOpacity: 0.1,
        shadowRadius: 2,
    },
    tab: {
        flex: 1,
        paddingVertical: 16,
        alignItems: 'center',
        borderBottomWidth: 2,
        borderBottomColor: 'transparent',
    },
    tabActive: {
        borderBottomColor: Colors.primary,
    },
    tabText: {
        fontSize: 14,
        color: Colors.grisOscuro,
        fontWeight: '500',
    },
    tabTextActive: {
        color: Colors.primary,
        fontWeight: 'bold',
    },
    content: {
        flex: 1,
    },
    tabContent: {
        padding: 16,
    },
    tramiteCard: {
        flexDirection: 'row',
        backgroundColor: '#fff',
        borderRadius: 12,
        padding: 16,
        marginBottom: 12,
        elevation: 2,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 1 },
        shadowOpacity: 0.1,
        shadowRadius: 2,
    },
    tramiteIcon: {
        width: 50,
        height: 50,
        borderRadius: 25,
        backgroundColor: Colors.light1,
        justifyContent: 'center',
        alignItems: 'center',
        marginRight: 12,
    },
    tramiteInfo: {
        flex: 1,
    },
    tramiteTitulo: {
        fontSize: 16,
        fontWeight: 'bold',
        color: Colors.grisOscuro,
        marginBottom: 4,
    },
    tramiteDescripcion: {
        fontSize: 14,
        color: Colors.grisOscuro,
        marginBottom: 8,
    },
    tramiteMetadata: {
        flexDirection: 'row',
        gap: 8,
    },
    tramiteChip: {
        flexDirection: 'row',
        alignItems: 'center',
        backgroundColor: Colors.light1,
        paddingHorizontal: 8,
        paddingVertical: 4,
        borderRadius: 12,
    },
    tramiteChipText: {
        fontSize: 12,
        color: Colors.grisOscuro,
        marginLeft: 4,
    },
    estadoBadge: {
        flexDirection: 'row',
        alignItems: 'center',
        alignSelf: 'flex-start',
        paddingHorizontal: 10,
        paddingVertical: 4,
        borderRadius: 12,
        marginBottom: 8,
    },
    estadoText: {
        fontSize: 12,
        marginLeft: 6,
        fontWeight: '600',
    },
    progressContainer: {
        flexDirection: 'row',
        alignItems: 'center',
        marginVertical: 8,
    },
    progressBar: {
        flex: 1,
        height: 6,
        backgroundColor: Colors.light2,
        borderRadius: 3,
        marginRight: 12,
        overflow: 'hidden',
    },
    progressFill: {
        height: '100%',
        backgroundColor: Colors.primary,
        borderRadius: 3,
    },
    progressText: {
        fontSize: 12,
        fontWeight: 'bold',
        color: Colors.grisOscuro,
    },
    fechasContainer: {
        marginTop: 8,
    },
    fechaText: {
        fontSize: 12,
        color: Colors.grisOscuro,
        marginBottom: 2,
    },
    emptyState: {
        alignItems: 'center',
        justifyContent: 'center',
        paddingVertical: 60,
    },
    emptyStateText: {
        fontSize: 16,
        color: Colors.grisOscuro,
        marginTop: 16,
    },
    // Modal styles
    modalOverlay: {
        flex: 1,
        backgroundColor: 'rgba(0,0,0,0.5)',
        justifyContent: 'flex-end',
    },
    modalContent: {
        backgroundColor: '#fff',
        borderTopLeftRadius: 24,
        borderTopRightRadius: 24,
        maxHeight: '85%',
    },
    modalHeader: {
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'space-between',
        padding: 20,
        borderBottomWidth: 1,
        borderBottomColor: Colors.light2,
    },
    modalTitle: {
        fontSize: 20,
        fontWeight: 'bold',
        color: Colors.grisOscuro,
        flex: 1,
        marginRight: 16,
    },
    modalBody: {
        padding: 20,
    },
    modalSection: {
        marginBottom: 24,
    },
    modalSectionTitle: {
        fontSize: 16,
        fontWeight: 'bold',
        color: Colors.grisOscuro,
        marginBottom: 8,
    },
    modalText: {
        fontSize: 14,
        color: Colors.grisOscuro,
        lineHeight: 20,
    },
    modalListItem: {
        fontSize: 14,
        color: Colors.grisOscuro,
        lineHeight: 24,
    },
    iniciarButton: {
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: Colors.primary,
        paddingVertical: 16,
        borderRadius: 12,
        marginTop: 20,
    },
    iniciarButtonText: {
        fontSize: 16,
        fontWeight: 'bold',
        color: '#fff',
        marginLeft: 12,
    },
});

export default TramitesScreen;
