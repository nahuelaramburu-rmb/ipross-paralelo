import React, { useState } from 'react';
import {
    View,
    Text,
    StyleSheet,
    ScrollView,
    TouchableOpacity,
    TextInput,
    Modal,
    Alert,
} from 'react-native';
import Icon from 'react-native-vector-icons/Ionicons';

const PreAutorizacionesScreen = ({ onBack, loggedUser }) => {
    const [searchText, setSearchText] = useState('');
    const [selectedTab, setSelectedTab] = useState('pendientes'); // pendientes, aprobadas, rechazadas
    const [selectedAuth, setSelectedAuth] = useState(null);
    const [modalVisible, setModalVisible] = useState(false);

    // Datos mock de pre-autorizaciones
    const preAutorizaciones = {
        pendientes: [
            {
                id: 'PA-2025-001',
                tipo: 'Estudios',
                descripcion: 'Resonancia Magnética de Rodilla',
                prestador: 'Centro de Diagnóstico San Martín',
                fechaSolicitud: '15/10/2025',
                estado: 'En Evaluación',
                prioridad: 'Normal',
                profesionalSolicitante: 'Dr. Roberto Fernández',
                especialidad: 'Traumatología',
            },
            {
                id: 'PA-2025-002',
                tipo: 'Medicación',
                descripcion: 'Tratamiento Oncológico - Quimioterapia',
                prestador: 'Farmacia Central',
                fechaSolicitud: '18/10/2025',
                estado: 'Pendiente Documentación',
                prioridad: 'Urgente',
                profesionalSolicitante: 'Dra. María González',
                especialidad: 'Oncología',
            },
            {
                id: 'PA-2025-003',
                tipo: 'Cirugía',
                descripcion: 'Artroscopía de Hombro',
                prestador: 'Sanatorio del Valle',
                fechaSolicitud: '19/10/2025',
                estado: 'En Evaluación',
                prioridad: 'Normal',
                profesionalSolicitante: 'Dr. Carlos Martínez',
                especialidad: 'Ortopedia',
            },
        ],
        aprobadas: [
            {
                id: 'PA-2024-187',
                tipo: 'Estudios',
                descripcion: 'Tomografía Computada de Abdomen',
                prestador: 'Instituto de Diagnóstico',
                fechaSolicitud: '05/10/2025',
                fechaAprobacion: '08/10/2025',
                estado: 'Aprobada',
                validezHasta: '08/11/2025',
                numeroAutorizacion: 'AUT-789456',
                observaciones: 'Autorización válida por 30 días',
            },
            {
                id: 'PA-2024-156',
                tipo: 'Internación',
                descripcion: 'Internación Domiciliaria - 10 días',
                prestador: 'ServiSalud Domiciliaria',
                fechaSolicitud: '28/09/2025',
                fechaAprobacion: '30/09/2025',
                estado: 'Aprobada',
                validezHasta: '30/10/2025',
                numeroAutorizacion: 'AUT-654321',
                observaciones: 'Incluye enfermería y medicación',
            },
        ],
        rechazadas: [
            {
                id: 'PA-2024-132',
                tipo: 'Medicación',
                descripcion: 'Tratamiento Estético - Botox',
                prestador: 'Clínica Estética Norte',
                fechaSolicitud: '20/09/2025',
                fechaRechazo: '22/09/2025',
                estado: 'Rechazada',
                motivoRechazo: 'No está cubierto por el plan de salud. Tratamiento estético sin indicación médica.',
            },
        ],
    };

    const handleViewDetails = (auth) => {
        setSelectedAuth(auth);
        setModalVisible(true);
    };

    const handleSolicitar = () => {
        Alert.alert(
            'Solicitar Pre-autorización',
            'Esta funcionalidad estará disponible próximamente. Podrá solicitar pre-autorizaciones directamente desde la app.',
            [{ text: 'Entendido' }]
        );
    };

    const getEstadoColor = (estado) => {
        switch (estado) {
            case 'En Evaluación':
                return '#FF9800';
            case 'Pendiente Documentación':
                return '#F44336';
            case 'Aprobada':
                return '#4CAF50';
            case 'Rechazada':
                return '#D32F2F';
            default:
                return '#757575';
        }
    };

    const getPrioridadIcon = (prioridad) => {
        return prioridad === 'Urgente' ? 'alert-circle' : 'information-circle-outline';
    };

    const getPrioridadColor = (prioridad) => {
        return prioridad === 'Urgente' ? '#F44336' : '#2196F3';
    };

    const filteredData = preAutorizaciones[selectedTab].filter((item) =>
        item.descripcion.toLowerCase().includes(searchText.toLowerCase()) ||
        item.id.toLowerCase().includes(searchText.toLowerCase()) ||
        item.prestador.toLowerCase().includes(searchText.toLowerCase())
    );

    const renderAuthCard = (auth) => (
        <TouchableOpacity
            key={auth.id}
            style={styles.authCard}
            onPress={() => handleViewDetails(auth)}
        >
            <View style={styles.authHeader}>
                <View style={styles.authIdContainer}>
                    <Icon name="document-text" size={18} color="#6ac64f" />
                    <Text style={styles.authId}>{auth.id}</Text>
                </View>
                {auth.prioridad && (
                    <View style={[styles.priorityBadge, { backgroundColor: getPrioridadColor(auth.prioridad) }]}>
                        <Icon name={getPrioridadIcon(auth.prioridad)} size={14} color="#fff" />
                        <Text style={styles.priorityText}>{auth.prioridad}</Text>
                    </View>
                )}
            </View>

            <View style={styles.authContent}>
                <Text style={styles.authTipo}>{auth.tipo}</Text>
                <Text style={styles.authDescripcion}>{auth.descripcion}</Text>
                
                <View style={styles.authInfoRow}>
                    <Icon name="business-outline" size={16} color="#666" />
                    <Text style={styles.authInfoText}>{auth.prestador}</Text>
                </View>

                {auth.profesionalSolicitante && (
                    <View style={styles.authInfoRow}>
                        <Icon name="medkit-outline" size={16} color="#666" />
                        <Text style={styles.authInfoText}>
                            {auth.profesionalSolicitante} - {auth.especialidad}
                        </Text>
                    </View>
                )}

                <View style={styles.authFooter}>
                    <View style={styles.dateContainer}>
                        <Icon name="calendar-outline" size={14} color="#999" />
                        <Text style={styles.dateText}>
                            {auth.fechaSolicitud}
                            {auth.fechaAprobacion && ` → ${auth.fechaAprobacion}`}
                            {auth.fechaRechazo && ` → ${auth.fechaRechazo}`}
                        </Text>
                    </View>
                    <View style={[styles.statusBadge, { backgroundColor: getEstadoColor(auth.estado) }]}>
                        <Text style={styles.statusText}>{auth.estado}</Text>
                    </View>
                </View>
            </View>
        </TouchableOpacity>
    );

    return (
        <View style={styles.container}>
            {/* Header */}
            <View style={styles.header}>
                <TouchableOpacity onPress={onBack} style={styles.backButton}>
                    <Icon name="arrow-back" size={24} color="#fff" />
                </TouchableOpacity>
                <Text style={styles.headerTitle}>Pre-Autorizaciones</Text>
                <TouchableOpacity onPress={handleSolicitar} style={styles.addButton}>
                    <Icon name="add-circle" size={28} color="#fff" />
                </TouchableOpacity>
            </View>

            {/* Search Bar */}
            <View style={styles.searchContainer}>
                <Icon name="search-outline" size={20} color="#666" style={styles.searchIcon} />
                <TextInput
                    style={styles.searchInput}
                    placeholder="Buscar por código, descripción o prestador..."
                    value={searchText}
                    onChangeText={setSearchText}
                />
                {searchText.length > 0 && (
                    <TouchableOpacity onPress={() => setSearchText('')}>
                        <Icon name="close-circle" size={20} color="#666" />
                    </TouchableOpacity>
                )}
            </View>

            {/* Tabs */}
            <View style={styles.tabsContainer}>
                <TouchableOpacity
                    style={[styles.tab, selectedTab === 'pendientes' && styles.activeTab]}
                    onPress={() => setSelectedTab('pendientes')}
                >
                    <Text style={[styles.tabText, selectedTab === 'pendientes' && styles.activeTabText]}>
                        Pendientes ({preAutorizaciones.pendientes.length})
                    </Text>
                </TouchableOpacity>
                <TouchableOpacity
                    style={[styles.tab, selectedTab === 'aprobadas' && styles.activeTab]}
                    onPress={() => setSelectedTab('aprobadas')}
                >
                    <Text style={[styles.tabText, selectedTab === 'aprobadas' && styles.activeTabText]}>
                        Aprobadas ({preAutorizaciones.aprobadas.length})
                    </Text>
                </TouchableOpacity>
                <TouchableOpacity
                    style={[styles.tab, selectedTab === 'rechazadas' && styles.activeTab]}
                    onPress={() => setSelectedTab('rechazadas')}
                >
                    <Text style={[styles.tabText, selectedTab === 'rechazadas' && styles.activeTabText]}>
                        Rechazadas ({preAutorizaciones.rechazadas.length})
                    </Text>
                </TouchableOpacity>
            </View>

            {/* Content */}
            <ScrollView style={styles.content} showsVerticalScrollIndicator={false}>
                {filteredData.length > 0 ? (
                    filteredData.map(renderAuthCard)
                ) : (
                    <View style={styles.emptyContainer}>
                        <Icon name="document-text-outline" size={80} color="#ccc" />
                        <Text style={styles.emptyText}>
                            {searchText ? 'No se encontraron resultados' : `No hay pre-autorizaciones ${selectedTab}`}
                        </Text>
                    </View>
                )}
            </ScrollView>

            {/* Modal de Detalles */}
            <Modal
                visible={modalVisible}
                animationType="slide"
                transparent={true}
                onRequestClose={() => setModalVisible(false)}
            >
                <View style={styles.modalOverlay}>
                    <View style={styles.modalContent}>
                        <View style={styles.modalHeader}>
                            <Text style={styles.modalTitle}>Detalle de Pre-autorización</Text>
                            <TouchableOpacity onPress={() => setModalVisible(false)}>
                                <Icon name="close" size={28} color="#333" />
                            </TouchableOpacity>
                        </View>

                        {selectedAuth && (
                            <ScrollView style={styles.modalBody}>
                                <View style={styles.detailSection}>
                                    <Text style={styles.detailLabel}>Código:</Text>
                                    <Text style={styles.detailValue}>{selectedAuth.id}</Text>
                                </View>

                                <View style={styles.detailSection}>
                                    <Text style={styles.detailLabel}>Tipo:</Text>
                                    <Text style={styles.detailValue}>{selectedAuth.tipo}</Text>
                                </View>

                                <View style={styles.detailSection}>
                                    <Text style={styles.detailLabel}>Descripción:</Text>
                                    <Text style={styles.detailValue}>{selectedAuth.descripcion}</Text>
                                </View>

                                <View style={styles.detailSection}>
                                    <Text style={styles.detailLabel}>Prestador:</Text>
                                    <Text style={styles.detailValue}>{selectedAuth.prestador}</Text>
                                </View>

                                {selectedAuth.profesionalSolicitante && (
                                    <>
                                        <View style={styles.detailSection}>
                                            <Text style={styles.detailLabel}>Profesional Solicitante:</Text>
                                            <Text style={styles.detailValue}>{selectedAuth.profesionalSolicitante}</Text>
                                        </View>

                                        <View style={styles.detailSection}>
                                            <Text style={styles.detailLabel}>Especialidad:</Text>
                                            <Text style={styles.detailValue}>{selectedAuth.especialidad}</Text>
                                        </View>
                                    </>
                                )}

                                <View style={styles.detailSection}>
                                    <Text style={styles.detailLabel}>Fecha de Solicitud:</Text>
                                    <Text style={styles.detailValue}>{selectedAuth.fechaSolicitud}</Text>
                                </View>

                                {selectedAuth.fechaAprobacion && (
                                    <View style={styles.detailSection}>
                                        <Text style={styles.detailLabel}>Fecha de Aprobación:</Text>
                                        <Text style={styles.detailValue}>{selectedAuth.fechaAprobacion}</Text>
                                    </View>
                                )}

                                {selectedAuth.fechaRechazo && (
                                    <View style={styles.detailSection}>
                                        <Text style={styles.detailLabel}>Fecha de Rechazo:</Text>
                                        <Text style={styles.detailValue}>{selectedAuth.fechaRechazo}</Text>
                                    </View>
                                )}

                                {selectedAuth.validezHasta && (
                                    <View style={styles.detailSection}>
                                        <Text style={styles.detailLabel}>Válida Hasta:</Text>
                                        <Text style={[styles.detailValue, { color: '#4CAF50' }]}>
                                            {selectedAuth.validezHasta}
                                        </Text>
                                    </View>
                                )}

                                {selectedAuth.numeroAutorizacion && (
                                    <View style={styles.detailSection}>
                                        <Text style={styles.detailLabel}>Número de Autorización:</Text>
                                        <Text style={[styles.detailValue, { fontWeight: 'bold', color: '#6ac64f' }]}>
                                            {selectedAuth.numeroAutorizacion}
                                        </Text>
                                    </View>
                                )}

                                <View style={styles.detailSection}>
                                    <Text style={styles.detailLabel}>Estado:</Text>
                                    <View style={[styles.statusBadge, { backgroundColor: getEstadoColor(selectedAuth.estado) }]}>
                                        <Text style={styles.statusText}>{selectedAuth.estado}</Text>
                                    </View>
                                </View>

                                {selectedAuth.observaciones && (
                                    <View style={styles.detailSection}>
                                        <Text style={styles.detailLabel}>Observaciones:</Text>
                                        <Text style={styles.detailValue}>{selectedAuth.observaciones}</Text>
                                    </View>
                                )}

                                {selectedAuth.motivoRechazo && (
                                    <View style={[styles.detailSection, styles.rejectSection]}>
                                        <Text style={styles.detailLabel}>Motivo de Rechazo:</Text>
                                        <Text style={[styles.detailValue, { color: '#D32F2F' }]}>
                                            {selectedAuth.motivoRechazo}
                                        </Text>
                                    </View>
                                )}
                            </ScrollView>
                        )}

                        <TouchableOpacity
                            style={styles.closeButton}
                            onPress={() => setModalVisible(false)}
                        >
                            <Text style={styles.closeButtonText}>Cerrar</Text>
                        </TouchableOpacity>
                    </View>
                </View>
            </Modal>
        </View>
    );
};

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: '#f5f5f5',
    },
    header: {
        backgroundColor: '#6ac64f',
        paddingTop: 50,
        paddingBottom: 15,
        paddingHorizontal: 20,
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'space-between',
    },
    backButton: {
        padding: 5,
    },
    headerTitle: {
        fontSize: 20,
        fontWeight: 'bold',
        color: '#fff',
        flex: 1,
        textAlign: 'center',
    },
    addButton: {
        padding: 5,
    },
    searchContainer: {
        flexDirection: 'row',
        alignItems: 'center',
        backgroundColor: '#fff',
        margin: 15,
        marginBottom: 10,
        paddingHorizontal: 15,
        borderRadius: 25,
        elevation: 2,
    },
    searchIcon: {
        marginRight: 10,
    },
    searchInput: {
        flex: 1,
        paddingVertical: 12,
        fontSize: 14,
    },
    tabsContainer: {
        flexDirection: 'row',
        backgroundColor: '#fff',
        marginHorizontal: 15,
        marginBottom: 15,
        borderRadius: 10,
        padding: 5,
        elevation: 2,
    },
    tab: {
        flex: 1,
        paddingVertical: 10,
        alignItems: 'center',
        borderRadius: 8,
    },
    activeTab: {
        backgroundColor: '#6ac64f',
    },
    tabText: {
        fontSize: 13,
        color: '#666',
        fontWeight: '500',
    },
    activeTabText: {
        color: '#fff',
        fontWeight: 'bold',
    },
    content: {
        flex: 1,
        paddingHorizontal: 15,
    },
    authCard: {
        backgroundColor: '#fff',
        borderRadius: 12,
        marginBottom: 15,
        elevation: 3,
        overflow: 'hidden',
    },
    authHeader: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        paddingHorizontal: 15,
        paddingTop: 15,
    },
    authIdContainer: {
        flexDirection: 'row',
        alignItems: 'center',
    },
    authId: {
        fontSize: 14,
        fontWeight: 'bold',
        color: '#333',
        marginLeft: 5,
    },
    priorityBadge: {
        flexDirection: 'row',
        alignItems: 'center',
        paddingHorizontal: 10,
        paddingVertical: 5,
        borderRadius: 15,
    },
    priorityText: {
        color: '#fff',
        fontSize: 11,
        fontWeight: 'bold',
        marginLeft: 5,
    },
    authContent: {
        padding: 15,
    },
    authTipo: {
        fontSize: 12,
        color: '#6ac64f',
        fontWeight: '600',
        marginBottom: 5,
    },
    authDescripcion: {
        fontSize: 16,
        fontWeight: 'bold',
        color: '#333',
        marginBottom: 10,
    },
    authInfoRow: {
        flexDirection: 'row',
        alignItems: 'center',
        marginBottom: 8,
    },
    authInfoText: {
        fontSize: 13,
        color: '#666',
        marginLeft: 8,
        flex: 1,
    },
    authFooter: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginTop: 10,
        paddingTop: 10,
        borderTopWidth: 1,
        borderTopColor: '#f0f0f0',
    },
    dateContainer: {
        flexDirection: 'row',
        alignItems: 'center',
    },
    dateText: {
        fontSize: 12,
        color: '#999',
        marginLeft: 5,
    },
    statusBadge: {
        paddingHorizontal: 12,
        paddingVertical: 5,
        borderRadius: 15,
    },
    statusText: {
        color: '#fff',
        fontSize: 11,
        fontWeight: 'bold',
    },
    emptyContainer: {
        flex: 1,
        alignItems: 'center',
        justifyContent: 'center',
        paddingVertical: 60,
    },
    emptyText: {
        fontSize: 16,
        color: '#999',
        marginTop: 15,
    },
    modalOverlay: {
        flex: 1,
        backgroundColor: 'rgba(0, 0, 0, 0.5)',
        justifyContent: 'flex-end',
    },
    modalContent: {
        backgroundColor: '#fff',
        borderTopLeftRadius: 25,
        borderTopRightRadius: 25,
        maxHeight: '85%',
    },
    modalHeader: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        padding: 20,
        borderBottomWidth: 1,
        borderBottomColor: '#f0f0f0',
    },
    modalTitle: {
        fontSize: 20,
        fontWeight: 'bold',
        color: '#333',
    },
    modalBody: {
        padding: 20,
    },
    detailSection: {
        marginBottom: 20,
    },
    detailLabel: {
        fontSize: 13,
        color: '#999',
        marginBottom: 5,
        fontWeight: '600',
    },
    detailValue: {
        fontSize: 16,
        color: '#333',
    },
    rejectSection: {
        backgroundColor: '#ffebee',
        padding: 15,
        borderRadius: 10,
        borderLeftWidth: 4,
        borderLeftColor: '#D32F2F',
    },
    closeButton: {
        backgroundColor: '#6ac64f',
        margin: 20,
        marginTop: 10,
        padding: 15,
        borderRadius: 10,
        alignItems: 'center',
    },
    closeButtonText: {
        color: '#fff',
        fontSize: 16,
        fontWeight: 'bold',
    },
});

export default PreAutorizacionesScreen;
