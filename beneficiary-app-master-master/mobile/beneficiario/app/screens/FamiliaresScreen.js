import React, { useState } from 'react';
import {
    View,
    Text,
    TouchableOpacity,
    StyleSheet,
    StatusBar,
    ScrollView,
    Image,
} from 'react-native';
import Icon from 'react-native-vector-icons/Ionicons';
import * as Colors from '../constants/Colors';

const FamiliaresScreen = ({ onBack, loggedUser }) => {
    const [selectedMember, setSelectedMember] = useState(null);

    // Mock data de familiares
    const familiares = [
        {
            id: 1,
            nombre: 'Aramburu, Nahuel',
            relacion: 'Titular',
            dni: '36447582',
            numeroAfiliado: '03-36447582/00',
            categoria: 'Obligatorio',
            estado: 'Activo',
            edad: 32,
            sexo: 'M',
        },
        {
            id: 2,
            nombre: 'García, María Laura',
            relacion: 'Cónyuge',
            dni: '35987654',
            numeroAfiliado: '03-36447582/01',
            categoria: 'Adherente',
            estado: 'Activo',
            edad: 30,
            sexo: 'F',
        },
        {
            id: 3,
            nombre: 'Aramburu, Santiago',
            relacion: 'Hijo/a',
            dni: '48123456',
            numeroAfiliado: '03-36447582/02',
            categoria: 'Adherente',
            estado: 'Activo',
            edad: 5,
            sexo: 'M',
        },
        {
            id: 4,
            nombre: 'Aramburu, Valentina',
            relacion: 'Hijo/a',
            dni: '50234567',
            numeroAfiliado: '03-36447582/03',
            categoria: 'Adherente',
            estado: 'Activo',
            edad: 3,
            sexo: 'F',
        },
    ];

    const getAvatarColor = (sexo) => {
        return sexo === 'M' ? '#1976d2' : '#e91e63';
    };

    const getInitials = (nombre) => {
        const parts = nombre.split(',');
        if (parts.length === 2) {
            const apellido = parts[0].trim()[0];
            const nombreProp = parts[1].trim()[0];
            return apellido + nombreProp;
        }
        return nombre.substring(0, 2).toUpperCase();
    };

    return (
        <View style={styles.container}>
            <StatusBar barStyle="light-content" backgroundColor={Colors.primary} />
            
            {/* Header */}
            <View style={styles.header}>
                <TouchableOpacity onPress={onBack} style={styles.backButton}>
                    <Icon name="arrow-back" size={24} color="#fff" />
                </TouchableOpacity>
                <Text style={styles.headerTitle}>Grupo Familiar</Text>
                <TouchableOpacity 
                    style={styles.addButton}
                    onPress={() => alert('Agregar familiar - En construcción')}
                >
                    <Icon name="add-circle-outline" size={24} color="#fff" />
                </TouchableOpacity>
            </View>

            <ScrollView style={styles.content}>
                {/* Resumen */}
                <View style={styles.summaryCard}>
                    <View style={styles.summaryItem}>
                        <Text style={styles.summaryNumber}>{familiares.length}</Text>
                        <Text style={styles.summaryLabel}>Integrantes</Text>
                    </View>
                    <View style={styles.summaryDivider} />
                    <View style={styles.summaryItem}>
                        <Text style={styles.summaryNumber}>
                            {familiares.filter(f => f.estado === 'Activo').length}
                        </Text>
                        <Text style={styles.summaryLabel}>Activos</Text>
                    </View>
                    <View style={styles.summaryDivider} />
                    <View style={styles.summaryItem}>
                        <Text style={styles.summaryNumber}>1</Text>
                        <Text style={styles.summaryLabel}>Titular</Text>
                    </View>
                </View>

                {/* Lista de familiares */}
                <View style={styles.section}>
                    <Text style={styles.sectionTitle}>Integrantes del Grupo</Text>
                    
                    {familiares.map((familiar) => (
                        <TouchableOpacity
                            key={familiar.id}
                            style={[
                                styles.familiarCard,
                                familiar.relacion === 'Titular' && styles.familiarCardTitular
                            ]}
                            onPress={() => setSelectedMember(
                                selectedMember?.id === familiar.id ? null : familiar
                            )}
                        >
                            {/* Header de la tarjeta */}
                            <View style={styles.familiarHeader}>
                                {/* Avatar */}
                                <View style={[
                                    styles.avatar,
                                    { backgroundColor: getAvatarColor(familiar.sexo) }
                                ]}>
                                    <Text style={styles.avatarText}>
                                        {getInitials(familiar.nombre)}
                                    </Text>
                                </View>

                                {/* Información básica */}
                                <View style={styles.familiarInfo}>
                                    <View style={styles.familiarNameRow}>
                                        <Text style={styles.familiarName}>{familiar.nombre}</Text>
                                        {familiar.relacion === 'Titular' && (
                                            <View style={styles.badgeTitular}>
                                                <Icon name="star" size={12} color="#fff" />
                                                <Text style={styles.badgeText}>Titular</Text>
                                            </View>
                                        )}
                                    </View>
                                    <Text style={styles.familiarRelacion}>{familiar.relacion}</Text>
                                    <Text style={styles.familiarNumber}>N° {familiar.numeroAfiliado}</Text>
                                </View>

                                {/* Estado */}
                                <View style={styles.estadoContainer}>
                                    <View style={[
                                        styles.estadoBadge,
                                        familiar.estado === 'Activo' && styles.estadoActivo
                                    ]}>
                                        <Icon 
                                            name={familiar.estado === 'Activo' ? 'checkmark-circle' : 'close-circle'} 
                                            size={16} 
                                            color={familiar.estado === 'Activo' ? '#4caf50' : '#f44336'} 
                                        />
                                        <Text style={[
                                            styles.estadoText,
                                            familiar.estado === 'Activo' && styles.estadoActivoText
                                        ]}>
                                            {familiar.estado}
                                        </Text>
                                    </View>
                                    <Icon 
                                        name={selectedMember?.id === familiar.id ? 'chevron-up' : 'chevron-down'} 
                                        size={20} 
                                        color={Colors.grisOscuro} 
                                    />
                                </View>
                            </View>

                            {/* Detalles expandibles */}
                            {selectedMember?.id === familiar.id && (
                                <View style={styles.familiarDetails}>
                                    <View style={styles.detailRow}>
                                        <Icon name="card-outline" size={18} color={Colors.grisOscuro} />
                                        <Text style={styles.detailLabel}>DNI:</Text>
                                        <Text style={styles.detailValue}>{familiar.dni}</Text>
                                    </View>
                                    <View style={styles.detailRow}>
                                        <Icon name="calendar-outline" size={18} color={Colors.grisOscuro} />
                                        <Text style={styles.detailLabel}>Edad:</Text>
                                        <Text style={styles.detailValue}>{familiar.edad} años</Text>
                                    </View>
                                    <View style={styles.detailRow}>
                                        <Icon name="pricetag-outline" size={18} color={Colors.grisOscuro} />
                                        <Text style={styles.detailLabel}>Categoría:</Text>
                                        <Text style={styles.detailValue}>{familiar.categoria}</Text>
                                    </View>

                                    {/* Acciones */}
                                    <View style={styles.actionsRow}>
                                        <TouchableOpacity 
                                            style={styles.actionButton}
                                            onPress={() => alert('Ver credencial - En construcción')}
                                        >
                                            <Icon name="card" size={20} color={Colors.primary} />
                                            <Text style={styles.actionButtonText}>Ver Credencial</Text>
                                        </TouchableOpacity>
                                        <TouchableOpacity 
                                            style={styles.actionButton}
                                            onPress={() => alert('Historial - En construcción')}
                                        >
                                            <Icon name="time" size={20} color={Colors.primary} />
                                            <Text style={styles.actionButtonText}>Historial</Text>
                                        </TouchableOpacity>
                                    </View>
                                </View>
                            )}
                        </TouchableOpacity>
                    ))}
                </View>

                {/* Botón agregar familiar */}
                <TouchableOpacity 
                    style={styles.addFamilyButton}
                    onPress={() => alert('Solicitar adhesión de familiar - En construcción')}
                >
                    <Icon name="person-add" size={24} color={Colors.primary} />
                    <Text style={styles.addFamilyButtonText}>Solicitar Adhesión de Familiar</Text>
                </TouchableOpacity>

                {/* Información */}
                <View style={styles.infoCard}>
                    <Icon name="information-circle" size={20} color={Colors.primary} />
                    <Text style={styles.infoText}>
                        Para dar de baja o modificar datos de un familiar, comuníquese con IPROSS o acérquese a una delegación.
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
    addButton: {
        padding: 8,
    },
    headerTitle: {
        fontSize: 20,
        fontWeight: 'bold',
        color: '#fff',
    },
    content: {
        flex: 1,
    },
    summaryCard: {
        flexDirection: 'row',
        backgroundColor: '#fff',
        marginHorizontal: 16,
        marginTop: 16,
        marginBottom: 20,
        borderRadius: 12,
        padding: 20,
        elevation: 2,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 1 },
        shadowOpacity: 0.1,
        shadowRadius: 2,
    },
    summaryItem: {
        flex: 1,
        alignItems: 'center',
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
    },
    section: {
        marginBottom: 20,
    },
    sectionTitle: {
        fontSize: 18,
        fontWeight: 'bold',
        color: Colors.grisOscuro,
        marginHorizontal: 16,
        marginBottom: 12,
    },
    familiarCard: {
        backgroundColor: '#fff',
        marginHorizontal: 16,
        marginBottom: 12,
        borderRadius: 12,
        padding: 16,
        elevation: 2,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 1 },
        shadowOpacity: 0.1,
        shadowRadius: 2,
    },
    familiarCardTitular: {
        borderLeftWidth: 4,
        borderLeftColor: Colors.primary,
    },
    familiarHeader: {
        flexDirection: 'row',
        alignItems: 'center',
    },
    avatar: {
        width: 50,
        height: 50,
        borderRadius: 25,
        justifyContent: 'center',
        alignItems: 'center',
        marginRight: 12,
    },
    avatarText: {
        fontSize: 18,
        fontWeight: 'bold',
        color: '#fff',
    },
    familiarInfo: {
        flex: 1,
    },
    familiarNameRow: {
        flexDirection: 'row',
        alignItems: 'center',
        marginBottom: 4,
    },
    familiarName: {
        fontSize: 16,
        fontWeight: 'bold',
        color: Colors.grisOscuro,
        marginRight: 8,
    },
    badgeTitular: {
        flexDirection: 'row',
        alignItems: 'center',
        backgroundColor: Colors.primary,
        paddingHorizontal: 8,
        paddingVertical: 2,
        borderRadius: 12,
    },
    badgeText: {
        fontSize: 10,
        color: '#fff',
        fontWeight: 'bold',
        marginLeft: 4,
    },
    familiarRelacion: {
        fontSize: 14,
        color: Colors.grisOscuro,
        marginBottom: 2,
    },
    familiarNumber: {
        fontSize: 12,
        color: Colors.grisOscuro,
    },
    estadoContainer: {
        alignItems: 'flex-end',
    },
    estadoBadge: {
        flexDirection: 'row',
        alignItems: 'center',
        paddingHorizontal: 10,
        paddingVertical: 4,
        borderRadius: 12,
        backgroundColor: '#ffebee',
        marginBottom: 8,
    },
    estadoActivo: {
        backgroundColor: '#e8f5e9',
    },
    estadoText: {
        fontSize: 12,
        color: '#f44336',
        marginLeft: 4,
        fontWeight: '600',
    },
    estadoActivoText: {
        color: '#4caf50',
    },
    familiarDetails: {
        marginTop: 16,
        paddingTop: 16,
        borderTopWidth: 1,
        borderTopColor: Colors.light2,
    },
    detailRow: {
        flexDirection: 'row',
        alignItems: 'center',
        marginBottom: 12,
    },
    detailLabel: {
        fontSize: 14,
        color: Colors.grisOscuro,
        marginLeft: 8,
        marginRight: 8,
        fontWeight: '600',
    },
    detailValue: {
        fontSize: 14,
        color: Colors.grisOscuro,
    },
    actionsRow: {
        flexDirection: 'row',
        marginTop: 16,
        gap: 12,
    },
    actionButton: {
        flex: 1,
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: Colors.light1,
        paddingVertical: 12,
        borderRadius: 8,
        borderWidth: 1,
        borderColor: Colors.primary,
    },
    actionButtonText: {
        fontSize: 13,
        color: Colors.primary,
        marginLeft: 6,
        fontWeight: '600',
    },
    addFamilyButton: {
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: '#fff',
        marginHorizontal: 16,
        marginBottom: 20,
        paddingVertical: 16,
        borderRadius: 12,
        borderWidth: 2,
        borderColor: Colors.primary,
        borderStyle: 'dashed',
    },
    addFamilyButtonText: {
        fontSize: 16,
        color: Colors.primary,
        marginLeft: 12,
        fontWeight: '600',
    },
    infoCard: {
        flexDirection: 'row',
        alignItems: 'center',
        backgroundColor: '#e3f2fd',
        marginHorizontal: 16,
        marginBottom: 20,
        padding: 16,
        borderRadius: 12,
    },
    infoText: {
        flex: 1,
        fontSize: 13,
        color: '#1565c0',
        marginLeft: 12,
        lineHeight: 18,
    },
});

export default FamiliaresScreen;
