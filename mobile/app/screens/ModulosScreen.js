import React from 'react';
import {
    View,
    Text,
    StyleSheet,
    ScrollView,
    TouchableOpacity,
    Alert,
} from 'react-native';
import Icon from 'react-native-vector-icons/Ionicons';

const ModulosScreen = ({ onBack, loggedUser }) => {
    const modulos = [
        {
            id: 1,
            nombre: 'Turnos',
            descripcion: 'Gestión de turnos médicos',
            icon: 'calendar',
            color: '#2196F3',
            disponible: true,
        },
        {
            id: 2,
            nombre: 'Prestadores',
            descripcion: 'Red de prestadores de salud',
            icon: 'business',
            color: '#6ac64f',
            disponible: true,
        },
        {
            id: 3,
            nombre: 'Atenciones',
            descripcion: 'Historial de atenciones médicas',
            icon: 'medical',
            color: '#FF9800',
            disponible: true,
        },
        {
            id: 4,
            nombre: 'Coseguros',
            descripcion: 'Consulta de coseguros',
            icon: 'cash',
            color: '#9C27B0',
            disponible: true,
        },
        {
            id: 5,
            nombre: 'Trámites',
            descripcion: 'Gestión de trámites administrativos',
            icon: 'document-text',
            color: '#00BCD4',
            disponible: true,
        },
        {
            id: 6,
            nombre: 'Familiares',
            descripcion: 'Grupo familiar',
            icon: 'people',
            color: '#E91E63',
            disponible: true,
        },
        {
            id: 7,
            nombre: 'Pre-Autorizaciones',
            descripcion: 'Solicitud y seguimiento de autorizaciones',
            icon: 'checkmark-circle',
            color: '#6ac64f',
            disponible: true,
        },
        {
            id: 8,
            nombre: 'Credencial QR',
            descripcion: 'Credencial digital con código QR',
            icon: 'qr-code',
            color: '#6ac64f',
            disponible: true,
        },
        {
            id: 9,
            nombre: 'Token de Seguridad',
            descripcion: 'Token temporal para validación',
            icon: 'shield-checkmark',
            color: '#3F51B5',
            disponible: true,
        },
        {
            id: 10,
            nombre: 'Recetas Digitales',
            descripcion: 'Gestión de recetas electrónicas',
            icon: 'receipt',
            color: '#607D8B',
            disponible: false,
        },
        {
            id: 11,
            nombre: 'Telemedicina',
            descripcion: 'Consultas médicas virtuales',
            icon: 'videocam',
            color: '#009688',
            disponible: false,
        },
        {
            id: 12,
            nombre: 'Cartilla de Vacunación',
            descripcion: 'Registro de vacunas aplicadas',
            icon: 'fitness',
            color: '#FF5722',
            disponible: false,
        },
        {
            id: 13,
            nombre: 'Análisis Clínicos',
            descripcion: 'Resultados de laboratorio',
            icon: 'flask',
            color: '#795548',
            disponible: false,
        },
        {
            id: 14,
            nombre: 'Estudios por Imágenes',
            descripcion: 'Visualización de estudios',
            icon: 'images',
            color: '#9E9E9E',
            disponible: false,
        },
        {
            id: 15,
            nombre: 'Reintegros',
            descripcion: 'Solicitud de reintegros de gastos',
            icon: 'return-down-back',
            color: '#FFC107',
            disponible: false,
        },
        {
            id: 16,
            nombre: 'Emergencias',
            descripcion: 'Números de emergencia y guía',
            icon: 'alert-circle',
            color: '#F44336',
            disponible: false,
        },
        {
            id: 17,
            nombre: 'Plan de Salud',
            descripcion: 'Detalles de tu cobertura',
            icon: 'heart',
            color: '#E91E63',
            disponible: false,
        },
        {
            id: 18,
            nombre: 'Notificaciones',
            descripcion: 'Centro de notificaciones',
            icon: 'notifications',
            color: '#FF9800',
            disponible: false,
        },
    ];

    const handleModulePress = (modulo) => {
        if (modulo.disponible) {
            Alert.alert(
                modulo.nombre,
                `El módulo "${modulo.nombre}" ya está disponible. Acceda desde el menú principal o la barra de navegación.`,
                [{ text: 'Entendido' }]
            );
        } else {
            Alert.alert(
                'Módulo en Desarrollo',
                `El módulo "${modulo.nombre}" estará disponible próximamente.`,
                [{ text: 'Entendido' }]
            );
        }
    };

    const disponibles = modulos.filter(m => m.disponible);
    const enDesarrollo = modulos.filter(m => !m.disponible);

    const renderModuleCard = (modulo) => (
        <TouchableOpacity
            key={modulo.id}
            style={[
                styles.moduleCard,
                !modulo.disponible && styles.moduleCardDisabled,
            ]}
            onPress={() => handleModulePress(modulo)}
            activeOpacity={0.7}
        >
            <View style={[styles.iconContainer, { backgroundColor: modulo.color }]}>
                <Icon name={modulo.icon} size={32} color="#fff" />
            </View>
            <View style={styles.moduleContent}>
                <Text style={[styles.moduleName, !modulo.disponible && styles.moduleNameDisabled]}>
                    {modulo.nombre}
                </Text>
                <Text style={styles.moduleDescription}>{modulo.descripcion}</Text>
            </View>
            {!modulo.disponible && (
                <View style={styles.comingSoonBadge}>
                    <Text style={styles.comingSoonText}>Próximamente</Text>
                </View>
            )}
            {modulo.disponible && (
                <Icon name="chevron-forward" size={24} color="#ccc" />
            )}
        </TouchableOpacity>
    );

    return (
        <View style={styles.container}>
            {/* Header */}
            <View style={styles.header}>
                <TouchableOpacity onPress={onBack} style={styles.backButton}>
                    <Icon name="arrow-back" size={24} color="#fff" />
                </TouchableOpacity>
                <Text style={styles.headerTitle}>Módulos</Text>
                <View style={styles.placeholder} />
            </View>

            <ScrollView style={styles.content} showsVerticalScrollIndicator={false}>
                {/* Bienvenida */}
                <View style={styles.welcomeCard}>
                    <Icon name="apps" size={50} color="#6ac64f" />
                    <Text style={styles.welcomeTitle}>Centro de Módulos</Text>
                    <Text style={styles.welcomeText}>
                        Explora todas las funcionalidades disponibles y las que estarán disponibles próximamente.
                    </Text>
                </View>

                {/* Estadísticas */}
                <View style={styles.statsContainer}>
                    <View style={styles.statBox}>
                        <Text style={styles.statNumber}>{disponibles.length}</Text>
                        <Text style={styles.statLabel}>Disponibles</Text>
                    </View>
                    <View style={styles.statBox}>
                        <Text style={[styles.statNumber, { color: '#FF9800' }]}>{enDesarrollo.length}</Text>
                        <Text style={styles.statLabel}>En Desarrollo</Text>
                    </View>
                    <View style={styles.statBox}>
                        <Text style={[styles.statNumber, { color: '#6ac64f' }]}>{modulos.length}</Text>
                        <Text style={styles.statLabel}>Total</Text>
                    </View>
                </View>

                {/* Módulos Disponibles */}
                <View style={styles.section}>
                    <View style={styles.sectionHeader}>
                        <Icon name="checkmark-circle" size={24} color="#6ac64f" />
                        <Text style={styles.sectionTitle}>Módulos Disponibles</Text>
                    </View>
                    {disponibles.map(renderModuleCard)}
                </View>

                {/* Módulos en Desarrollo */}
                <View style={styles.section}>
                    <View style={styles.sectionHeader}>
                        <Icon name="construct" size={24} color="#FF9800" />
                        <Text style={styles.sectionTitle}>Próximamente</Text>
                    </View>
                    <Text style={styles.sectionDescription}>
                        Estos módulos estarán disponibles en futuras actualizaciones de la aplicación.
                    </Text>
                    {enDesarrollo.map(renderModuleCard)}
                </View>

                {/* Footer */}
                <View style={styles.footer}>
                    <Icon name="information-circle-outline" size={20} color="#999" />
                    <Text style={styles.footerText}>
                        La disponibilidad de los módulos puede variar según tu plan de salud.
                    </Text>
                </View>
            </ScrollView>
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
    placeholder: {
        width: 34,
    },
    content: {
        flex: 1,
    },
    welcomeCard: {
        backgroundColor: '#fff',
        margin: 15,
        padding: 25,
        borderRadius: 15,
        alignItems: 'center',
        elevation: 3,
    },
    welcomeTitle: {
        fontSize: 22,
        fontWeight: 'bold',
        color: '#333',
        marginTop: 15,
        marginBottom: 10,
    },
    welcomeText: {
        fontSize: 14,
        color: '#666',
        textAlign: 'center',
        lineHeight: 20,
    },
    statsContainer: {
        flexDirection: 'row',
        marginHorizontal: 15,
        marginBottom: 20,
    },
    statBox: {
        flex: 1,
        backgroundColor: '#fff',
        padding: 15,
        marginHorizontal: 5,
        borderRadius: 10,
        alignItems: 'center',
        elevation: 2,
    },
    statNumber: {
        fontSize: 28,
        fontWeight: 'bold',
        color: '#2196F3',
    },
    statLabel: {
        fontSize: 12,
        color: '#999',
        marginTop: 5,
    },
    section: {
        marginBottom: 25,
    },
    sectionHeader: {
        flexDirection: 'row',
        alignItems: 'center',
        paddingHorizontal: 20,
        marginBottom: 15,
    },
    sectionTitle: {
        fontSize: 18,
        fontWeight: 'bold',
        color: '#333',
        marginLeft: 10,
    },
    sectionDescription: {
        fontSize: 13,
        color: '#666',
        paddingHorizontal: 20,
        marginBottom: 15,
        fontStyle: 'italic',
    },
    moduleCard: {
        backgroundColor: '#fff',
        marginHorizontal: 15,
        marginBottom: 12,
        padding: 15,
        borderRadius: 12,
        flexDirection: 'row',
        alignItems: 'center',
        elevation: 2,
    },
    moduleCardDisabled: {
        opacity: 0.6,
    },
    iconContainer: {
        width: 60,
        height: 60,
        borderRadius: 30,
        justifyContent: 'center',
        alignItems: 'center',
        marginRight: 15,
    },
    moduleContent: {
        flex: 1,
    },
    moduleName: {
        fontSize: 16,
        fontWeight: 'bold',
        color: '#333',
        marginBottom: 4,
    },
    moduleNameDisabled: {
        color: '#999',
    },
    moduleDescription: {
        fontSize: 13,
        color: '#666',
    },
    comingSoonBadge: {
        backgroundColor: '#FF9800',
        paddingHorizontal: 10,
        paddingVertical: 5,
        borderRadius: 12,
        marginRight: 10,
    },
    comingSoonText: {
        color: '#fff',
        fontSize: 10,
        fontWeight: 'bold',
    },
    footer: {
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: '#fff',
        marginHorizontal: 15,
        marginBottom: 20,
        padding: 15,
        borderRadius: 10,
    },
    footerText: {
        fontSize: 12,
        color: '#999',
        marginLeft: 8,
        flex: 1,
    },
});

export default ModulosScreen;
