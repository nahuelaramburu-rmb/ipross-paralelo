import React, { useState } from 'react';
import {
    View,
    Text,
    TouchableOpacity,
    StyleSheet,
    ScrollView,
    TextInput,
    Linking
} from 'react-native';
import Icon from 'react-native-vector-icons/Ionicons';
import * as Colors from '../constants/Colors';

const PrestadoresScreen = ({ onBack }) => {
    const [searchQuery, setSearchQuery] = useState('');
    const [selectedCategory, setSelectedCategory] = useState('todos');

    const categories = [
        { id: 'todos', label: 'Todos', icon: 'medical' },
        { id: 'hospitales', label: 'Hospitales', icon: 'business' },
        { id: 'clinicas', label: 'Clínicas', icon: 'medkit' },
        { id: 'farmacias', label: 'Farmacias', icon: 'fitness' },
        { id: 'laboratorios', label: 'Laboratorios', icon: 'flask' },
    ];

    const prestadores = [
        {
            id: 1,
            nombre: 'Hospital Público de Viedma',
            categoria: 'hospitales',
            direccion: 'Rivadavia 250',
            localidad: 'Viedma',
            telefono: '2920-422222',
            horario: '24 horas',
            especialidades: ['Emergencias', 'Clínica Médica', 'Pediatría', 'Traumatología']
        },
        {
            id: 2,
            nombre: 'Sanatorio San Carlos',
            categoria: 'clinicas',
            direccion: 'Colón 498',
            localidad: 'Viedma',
            telefono: '2920-423456',
            horario: 'Lun a Vie: 8:00 - 20:00',
            especialidades: ['Cardiología', 'Dermatología', 'Ginecología']
        },
        {
            id: 3,
            nombre: 'Centro Médico IPROSS',
            categoria: 'clinicas',
            direccion: 'Buenos Aires 181',
            localidad: 'Viedma',
            telefono: '2920-425000',
            horario: 'Lun a Vie: 7:00 - 19:00',
            especialidades: ['Clínica Médica', 'Oftalmología', 'Odontología']
        },
        {
            id: 4,
            nombre: 'Farmacia del Pueblo',
            categoria: 'farmacias',
            direccion: 'San Martín 324',
            localidad: 'Viedma',
            telefono: '2920-428888',
            horario: 'Lun a Sáb: 8:00 - 22:00',
            especialidades: ['Medicamentos', 'Perfumería', 'Ortopedia']
        },
        {
            id: 5,
            nombre: 'Laboratorio Bioquímico Central',
            categoria: 'laboratorios',
            direccion: 'Rivadavia 156',
            localidad: 'Viedma',
            telefono: '2920-427777',
            horario: 'Lun a Vie: 7:00 - 12:00',
            especialidades: ['Análisis Clínicos', 'Microbiología', 'Inmunología']
        },
        {
            id: 6,
            nombre: 'Clínica del Valle',
            categoria: 'clinicas',
            direccion: 'Av. San Martín 1250',
            localidad: 'General Roca',
            telefono: '2983-434343',
            horario: 'Lun a Vie: 8:00 - 20:00',
            especialidades: ['Traumatología', 'Kinesiología', 'Nutrición']
        }
    ];

    const filteredPrestadores = prestadores.filter(p => {
        const matchesCategory = selectedCategory === 'todos' || p.categoria === selectedCategory;
        const matchesSearch = p.nombre.toLowerCase().includes(searchQuery.toLowerCase()) ||
                            p.localidad.toLowerCase().includes(searchQuery.toLowerCase());
        return matchesCategory && matchesSearch;
    });

    const handleCall = (telefono) => {
        Linking.openURL(`tel:${telefono}`);
    };

    const handleLocation = (direccion, localidad) => {
        const query = encodeURIComponent(`${direccion}, ${localidad}, Río Negro`);
        Linking.openURL(`https://www.google.com/maps/search/?api=1&query=${query}`);
    };

    return (
        <View style={styles.container}>
            {/* Header */}
            <View style={styles.header}>
                <TouchableOpacity onPress={onBack} style={styles.backButton}>
                    <Icon name="arrow-back" size={24} color={Colors.textDark} />
                </TouchableOpacity>
                <Text style={styles.headerTitle}>Prestadores</Text>
                <View style={styles.placeholder} />
            </View>

            {/* Search Bar */}
            <View style={styles.searchContainer}>
                <Icon name="search" size={20} color={Colors.grisOscuro} />
                <TextInput
                    style={styles.searchInput}
                    placeholder="Buscar por nombre o localidad..."
                    value={searchQuery}
                    onChangeText={setSearchQuery}
                    placeholderTextColor={Colors.grisOscuro}
                />
                {searchQuery.length > 0 && (
                    <TouchableOpacity onPress={() => setSearchQuery('')}>
                        <Icon name="close-circle" size={20} color={Colors.grisOscuro} />
                    </TouchableOpacity>
                )}
            </View>

            {/* Categories */}
            <ScrollView 
                horizontal 
                showsHorizontalScrollIndicator={false}
                style={styles.categoriesContainer}
            >
                {categories.map((cat) => (
                    <TouchableOpacity
                        key={cat.id}
                        style={[
                            styles.categoryChip,
                            selectedCategory === cat.id && styles.categoryChipActive
                        ]}
                        onPress={() => setSelectedCategory(cat.id)}
                    >
                        <Icon 
                            name={cat.icon} 
                            size={16} 
                            color={selectedCategory === cat.id ? Colors.white : Colors.grisOscuro} 
                        />
                        <Text style={[
                            styles.categoryText,
                            selectedCategory === cat.id && styles.categoryTextActive
                        ]}>
                            {cat.label}
                        </Text>
                    </TouchableOpacity>
                ))}
            </ScrollView>

            {/* Prestadores List */}
            <ScrollView style={styles.content}>
                <Text style={styles.resultsText}>
                    {filteredPrestadores.length} prestador{filteredPrestadores.length !== 1 ? 'es' : ''} encontrado{filteredPrestadores.length !== 1 ? 's' : ''}
                </Text>

                {filteredPrestadores.map((prestador) => (
                    <View key={prestador.id} style={styles.prestadorCard}>
                        <View style={styles.prestadorHeader}>
                            <View style={styles.prestadorIcon}>
                                <Icon 
                                    name={categories.find(c => c.id === prestador.categoria)?.icon || 'medical'} 
                                    size={24} 
                                    color={Colors.primary} 
                                />
                            </View>
                            <View style={styles.prestadorInfo}>
                                <Text style={styles.prestadorNombre}>{prestador.nombre}</Text>
                                <Text style={styles.prestadorLocalidad}>{prestador.localidad}</Text>
                            </View>
                        </View>

                        <View style={styles.detailRow}>
                            <Icon name="location" size={16} color={Colors.grisOscuro} />
                            <Text style={styles.detailText}>{prestador.direccion}</Text>
                        </View>

                        <View style={styles.detailRow}>
                            <Icon name="call" size={16} color={Colors.grisOscuro} />
                            <Text style={styles.detailText}>{prestador.telefono}</Text>
                        </View>

                        <View style={styles.detailRow}>
                            <Icon name="time" size={16} color={Colors.grisOscuro} />
                            <Text style={styles.detailText}>{prestador.horario}</Text>
                        </View>

                        {/* Especialidades */}
                        <View style={styles.especialidadesContainer}>
                            {prestador.especialidades.map((esp, index) => (
                                <View key={index} style={styles.especialidadTag}>
                                    <Text style={styles.especialidadText}>{esp}</Text>
                                </View>
                            ))}
                        </View>

                        {/* Actions */}
                        <View style={styles.prestadorActions}>
                            <TouchableOpacity 
                                style={styles.actionButton}
                                onPress={() => handleCall(prestador.telefono)}
                            >
                                <Icon name="call" size={18} color={Colors.white} />
                                <Text style={styles.actionButtonText}>Llamar</Text>
                            </TouchableOpacity>
                            <TouchableOpacity 
                                style={styles.actionButton}
                                onPress={() => handleLocation(prestador.direccion, prestador.localidad)}
                            >
                                <Icon name="navigate" size={18} color={Colors.white} />
                                <Text style={styles.actionButtonText}>Cómo llegar</Text>
                            </TouchableOpacity>
                        </View>
                    </View>
                ))}

                {filteredPrestadores.length === 0 && (
                    <View style={styles.emptyState}>
                        <Icon name="search-outline" size={64} color={Colors.grisOscuro} />
                        <Text style={styles.emptyText}>No se encontraron prestadores</Text>
                        <Text style={styles.emptySubtext}>Intenta con otros términos de búsqueda</Text>
                    </View>
                )}
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
    searchContainer: {
        flexDirection: 'row',
        alignItems: 'center',
        backgroundColor: Colors.white,
        marginHorizontal: 16,
        marginTop: 16,
        paddingHorizontal: 16,
        paddingVertical: 12,
        borderRadius: 12,
        elevation: 2,
    },
    searchInput: {
        flex: 1,
        fontSize: 14,
        color: Colors.textDark,
        marginLeft: 12,
    },
    categoriesContainer: {
        marginTop: 16,
        marginBottom: 8,
        paddingHorizontal: 16,
    },
    categoryChip: {
        flexDirection: 'row',
        alignItems: 'center',
        backgroundColor: Colors.white,
        paddingHorizontal: 16,
        paddingVertical: 8,
        borderRadius: 20,
        marginRight: 8,
        elevation: 2,
    },
    categoryChipActive: {
        backgroundColor: Colors.primary,
    },
    categoryText: {
        fontSize: 12,
        color: Colors.grisOscuro,
        marginLeft: 6,
        fontWeight: '500',
    },
    categoryTextActive: {
        color: Colors.white,
    },
    content: {
        flex: 1,
        paddingTop: 16,
    },
    resultsText: {
        fontSize: 12,
        color: Colors.grisOscuro,
        marginHorizontal: 16,
        marginBottom: 12,
    },
    prestadorCard: {
        backgroundColor: Colors.white,
        marginHorizontal: 16,
        marginBottom: 16,
        borderRadius: 12,
        padding: 16,
        elevation: 2,
    },
    prestadorHeader: {
        flexDirection: 'row',
        marginBottom: 16,
    },
    prestadorIcon: {
        width: 48,
        height: 48,
        borderRadius: 24,
        backgroundColor: Colors.light1,
        justifyContent: 'center',
        alignItems: 'center',
    },
    prestadorInfo: {
        flex: 1,
        marginLeft: 12,
        justifyContent: 'center',
    },
    prestadorNombre: {
        fontSize: 16,
        fontWeight: '600',
        color: Colors.textDark,
        marginBottom: 4,
    },
    prestadorLocalidad: {
        fontSize: 12,
        color: Colors.grisOscuro,
    },
    detailRow: {
        flexDirection: 'row',
        alignItems: 'center',
        marginBottom: 8,
    },
    detailText: {
        fontSize: 13,
        color: Colors.grisOscuro,
        marginLeft: 8,
    },
    especialidadesContainer: {
        flexDirection: 'row',
        flexWrap: 'wrap',
        marginTop: 8,
        marginBottom: 12,
    },
    especialidadTag: {
        backgroundColor: Colors.light1,
        paddingHorizontal: 10,
        paddingVertical: 4,
        borderRadius: 12,
        marginRight: 6,
        marginBottom: 6,
    },
    especialidadText: {
        fontSize: 11,
        color: Colors.primary,
        fontWeight: '500',
    },
    prestadorActions: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        marginTop: 8,
        gap: 8,
    },
    actionButton: {
        flex: 1,
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: Colors.primary,
        paddingVertical: 10,
        borderRadius: 8,
    },
    actionButtonText: {
        fontSize: 13,
        fontWeight: '600',
        color: Colors.white,
        marginLeft: 6,
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
        marginTop: 8,
    },
});

export default PrestadoresScreen;
