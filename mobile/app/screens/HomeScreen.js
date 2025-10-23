import React, { useState } from 'react';
import {
    View,
    Text,
    TouchableOpacity,
    StyleSheet,
    Image,
    ScrollView,
    StatusBar,
    Linking,
    Modal,
    Animated
} from 'react-native';
import Icon from 'react-native-vector-icons/Ionicons';
import * as Colors from '../constants/Colors';
import TurnosScreen from './TurnosScreen';
import PrestadoresScreen from './PrestadoresScreen';
import AtencionesScreen from './AtencionesScreen';
import CosegurosScreen from './CosegurosScreen';
import QRScreen from './QRScreen';
import TokenScreen from './TokenScreen';
import FamiliaresScreen from './FamiliaresScreen';
import TramitesScreen from './TramitesScreen';
import PreAutorizacionesScreen from './PreAutorizacionesScreen';
import ModulosScreen from './ModulosScreen';

const HomeScreen = ({ loggedUser, onLogout }) => {
    const [sidebarVisible, setSidebarVisible] = useState(false);
    const [slideAnim] = useState(new Animated.Value(-300));
    const [menuExpanded, setMenuExpanded] = useState(false);
    const [fabMenuVisible, setFabMenuVisible] = useState(false);
    const [currentScreen, setCurrentScreen] = useState('home');

    const userName = loggedUser?.nombre || 'Usuario';
    const userNumber = loggedUser?.numero_afiliado || '03-13642194/00';
    const dni = userNumber.split('/')[0].replace('-', '');

    const toggleSidebar = () => {
        if (sidebarVisible) {
            Animated.timing(slideAnim, {
                toValue: -300,
                duration: 300,
                useNativeDriver: true,
            }).start(() => setSidebarVisible(false));
        } else {
            setSidebarVisible(true);
            Animated.timing(slideAnim, {
                toValue: 0,
                duration: 300,
                useNativeDriver: true,
            }).start();
        }
    };

    const openWhatsApp = () => {
        alert('WhatsApp - En construcción');
    };

    const handleMenuOption = (option) => {
        toggleSidebar();
        
        // TODAS las opciones muestran "En construcción" excepto Cerrar Sesión
        setTimeout(() => {
            alert(`${option} - En construcción`);
        }, 300);
    };

    const handleBackToHome = () => {
        setCurrentScreen('home');
    };

    const handleTabChange = (screen) => {
        if (screen === 'home') {
            setCurrentScreen(screen);
        } else {
            // Todas las pestañas excepto 'home' muestran "En construcción"
            alert('Funcionalidad en construcción');
        }
    };

    // Si estamos en otra pantalla, mostrar esa pantalla
    if (currentScreen === 'turnos') {
        return <TurnosScreen onBack={handleBackToHome} />;
    }

    if (currentScreen === 'prestadores') {
        return <PrestadoresScreen onBack={handleBackToHome} />;
    }

    if (currentScreen === 'atenciones') {
        return <AtencionesScreen onBack={handleBackToHome} />;
    }

    if (currentScreen === 'coseguros') {
        return <CosegurosScreen onBack={handleBackToHome} />;
    }

    if (currentScreen === 'qr') {
        return <QRScreen onBack={handleBackToHome} loggedUser={loggedUser} />;
    }

    if (currentScreen === 'token') {
        return <TokenScreen onBack={handleBackToHome} loggedUser={loggedUser} />;
    }

    if (currentScreen === 'familiares') {
        return <FamiliaresScreen onBack={handleBackToHome} loggedUser={loggedUser} />;
    }

    if (currentScreen === 'tramites') {
        return <TramitesScreen onBack={handleBackToHome} loggedUser={loggedUser} />;
    }

    if (currentScreen === 'preautorizaciones') {
        return <PreAutorizacionesScreen onBack={handleBackToHome} loggedUser={loggedUser} />;
    }

    if (currentScreen === 'modulos') {
        return <ModulosScreen onBack={handleBackToHome} loggedUser={loggedUser} />;
    }

    return (
        <View style={styles.container}>
            <StatusBar backgroundColor={Colors.primary} barStyle="dark-content" translucent={false} />
            
            {/* Header */}
            <View style={styles.header}>
                <TouchableOpacity onPress={toggleSidebar} style={styles.menuButton}>
                    <Icon name="menu" size={28} color={Colors.textDark} />
                </TouchableOpacity>
                
                <Text style={styles.headerTitle}>Credencial Virtual</Text>
                
                <TouchableOpacity onPress={openWhatsApp} style={styles.whatsappButton}>
                    <Icon name="logo-whatsapp" size={28} color={Colors.textDark} />
                </TouchableOpacity>
            </View>

            {/* Content */}
            <ScrollView style={styles.content}>
                {/* Credencial Card */}
                <View style={styles.credentialCard}>
                    {/* Header con logos */}
                    <View style={styles.cardHeader}>
                        <Image
                            source={require('../images/ipross_logo_green.jpg')}
                            style={styles.logoIpross}
                            resizeMode="contain"
                        />
                        <View style={styles.rioNegroLogo}>
                            <Text style={styles.rioNegroText}>RN</Text>
                            <Text style={styles.rioNegroSubtext}>RIO NEGRO</Text>
                        </View>
                    </View>

                    {/* Foto y datos del afiliado */}
                    <View style={styles.userSection}>
                        <View style={styles.avatarContainer}>
                            <View style={styles.avatar}>
                                <Icon name="person" size={50} color={Colors.primary} />
                            </View>
                        </View>
                        
                        <View style={styles.userInfo}>
                            <Text style={styles.userName}>{userName}</Text>
                            <View style={styles.userDataRow}>
                                <Text style={styles.userLabel}>Afiliado: </Text>
                                <Text style={styles.userNumber}>{userNumber}</Text>
                            </View>
                            <View style={styles.userDataRow}>
                                <Text style={styles.userLabel}>DNI: </Text>
                                <Text style={styles.userDni}>{dni}</Text>
                            </View>
                        </View>
                    </View>
                </View>

                {/* Información General */}
                <View style={styles.section}>
                    <Text style={styles.sectionTitle}>Información General</Text>
                    
                    <View style={styles.statusCard}>
                        <View style={styles.statusRow}>
                            <Text style={styles.statusLabel}>Con cobertura</Text>
                            <Icon name="checkmark-circle" size={24} color={Colors.primary} />
                        </View>
                    </View>

                    <View style={styles.infoCard}>
                        <Text style={styles.copagoLabel}>COSEGUROS</Text>
                        <Text style={styles.copagoDate}>Octubre 2025</Text>
                        <Text style={styles.copagoAmount}>$-</Text>
                    </View>
                </View>

                {/* Quick Actions */}
                <View style={styles.quickActions}>
                    <TouchableOpacity style={styles.actionButton} onPress={() => alert('Plan Único - En construcción')}>
                        <Icon name="document-text-outline" size={32} color={Colors.grisOscuro} />
                        <Text style={styles.actionText}>Plan Único</Text>
                    </TouchableOpacity>

                    <TouchableOpacity style={styles.actionButton} onPress={() => alert('Recibo de Sueldo - En construcción')}>
                        <Icon name="card-outline" size={32} color={Colors.grisOscuro} />
                        <Text style={styles.actionText}>Recibo de Sueldo</Text>
                    </TouchableOpacity>
                </View>

                {/* Bottom Navigation Icons */}
                <View style={styles.bottomIcons}>
                    <TouchableOpacity style={styles.iconButton} onPress={() => alert('Familiares - En construcción')}>
                        <Icon name="people-outline" size={24} color={Colors.grisOscuro} />
                    </TouchableOpacity>
                    <TouchableOpacity style={styles.iconButton} onPress={() => alert('Editar - En construcción')}>
                        <Icon name="create-outline" size={24} color={Colors.grisOscuro} />
                    </TouchableOpacity>
                </View>
            </ScrollView>

            {/* Floating Action Button */}
            <TouchableOpacity style={styles.fab} onPress={() => alert('Acción rápida - En construcción')}>
                <Icon name="add" size={32} color={Colors.white} />
            </TouchableOpacity>

            {/* Bottom Tab Bar */}
            <View style={styles.tabBar}>
                <TouchableOpacity 
                    style={currentScreen === 'home' ? styles.tabActive : styles.tab}
                    onPress={() => handleTabChange('home')}
                >
                    <Icon name="card" size={24} color={currentScreen === 'home' ? Colors.primary : Colors.grisOscuro} />
                    <Text style={currentScreen === 'home' ? styles.tabTextActive : styles.tabText}>Credencial</Text>
                </TouchableOpacity>
                <TouchableOpacity 
                    style={currentScreen === 'atenciones' ? styles.tabActive : styles.tab}
                    onPress={() => handleTabChange('atenciones')}
                >
                    <Icon name="heart-outline" size={24} color={currentScreen === 'atenciones' ? Colors.primary : Colors.grisOscuro} />
                    <Text style={currentScreen === 'atenciones' ? styles.tabTextActive : styles.tabText}>Mis Atenciones</Text>
                </TouchableOpacity>
                <TouchableOpacity 
                    style={currentScreen === 'coseguros' ? styles.tabActive : styles.tab}
                    onPress={() => handleTabChange('coseguros')}
                >
                    <Icon name="cash-outline" size={24} color={currentScreen === 'coseguros' ? Colors.primary : Colors.grisOscuro} />
                    <Text style={currentScreen === 'coseguros' ? styles.tabTextActive : styles.tabText}>Coseguros</Text>
                </TouchableOpacity>
                <TouchableOpacity 
                    style={currentScreen === 'tramites' ? styles.tabActive : styles.tab}
                    onPress={() => handleTabChange('tramites')}
                >
                    <Icon name="document-outline" size={24} color={currentScreen === 'tramites' ? Colors.primary : Colors.grisOscuro} />
                    <Text style={currentScreen === 'tramites' ? styles.tabTextActive : styles.tabText}>Trámites</Text>
                </TouchableOpacity>
            </View>

            {/* Sidebar */}
            <Modal
                animationType="none"
                transparent={true}
                visible={sidebarVisible}
                onRequestClose={toggleSidebar}
            >
                <View style={styles.sidebarContainer}>
                    <TouchableOpacity 
                        style={styles.sidebarOverlay} 
                        activeOpacity={1} 
                        onPress={toggleSidebar}
                    />
                    
                    <Animated.View style={[styles.sidebar, { transform: [{ translateX: slideAnim }] }]}>
                        <View style={styles.sidebarHeader}>
                            <Text style={styles.sidebarTitle}>{userName}</Text>
                            <Text style={styles.sidebarSubtitle}>TITULAR</Text>
                        </View>

                        <ScrollView style={styles.sidebarMenu}>
                            <TouchableOpacity 
                                style={styles.menuItem} 
                                onPress={() => handleMenuOption('Familiares')}
                            >
                                <Icon name="people" size={24} color={Colors.primary} />
                                <Text style={styles.menuItemText}>Familiares</Text>
                            </TouchableOpacity>

                            <TouchableOpacity 
                                style={styles.menuItem} 
                                onPress={() => handleMenuOption('Pre-Autorizaciones')}
                            >
                                <Icon name="document-text" size={24} color={Colors.primary} />
                                <Text style={styles.menuItemText}>Pre-Autorizaciones</Text>
                            </TouchableOpacity>

                            <TouchableOpacity 
                                style={styles.menuItem} 
                                onPress={() => handleMenuOption('Módulos')}
                            >
                                <Icon name="albums" size={24} color={Colors.primary} />
                                <Text style={styles.menuItemText}>Módulos</Text>
                            </TouchableOpacity>

                            <TouchableOpacity 
                                style={styles.menuItem} 
                                onPress={openWhatsApp}
                            >
                                <Icon name="call" size={24} color={Colors.primary} />
                                <Text style={styles.menuItemText}>Llamar al Contact-Center</Text>
                            </TouchableOpacity>

                            <TouchableOpacity 
                                style={styles.menuItem} 
                                onPress={() => handleMenuOption('Turnos')}
                            >
                                <Icon name="time" size={24} color={Colors.primary} />
                                <Text style={styles.menuItemText}>Turnos</Text>
                            </TouchableOpacity>

                            <TouchableOpacity 
                                style={styles.menuItem} 
                                onPress={() => handleMenuOption('Prestadores')}
                            >
                                <Icon name="medical" size={24} color={Colors.primary} />
                                <Text style={styles.menuItemText}>Prestadores</Text>
                            </TouchableOpacity>

                            <View style={styles.divider} />

                            <TouchableOpacity 
                                style={styles.menuItemLogout} 
                                onPress={() => {
                                    toggleSidebar();
                                    setTimeout(() => onLogout(), 300);
                                }}
                            >
                                <Icon name="log-out" size={24} color="#d32f2f" />
                                <Text style={styles.menuItemLogoutText}>Cerrar Sesión</Text>
                            </TouchableOpacity>

                            <Text style={styles.version}>v1.8.0</Text>
                        </ScrollView>
                    </Animated.View>
                </View>
            </Modal>

            {/* Botón flotante (FAB) para QR/Token */}
            <TouchableOpacity 
                style={styles.fab}
                onPress={() => setFabMenuVisible(true)}
            >
                <Icon name="qr-code" size={28} color="#fff" />
            </TouchableOpacity>

            {/* Modal del menú FAB */}
            <Modal
                animationType="fade"
                transparent={true}
                visible={fabMenuVisible}
                onRequestClose={() => setFabMenuVisible(false)}
            >
                <TouchableOpacity 
                    style={styles.fabModalOverlay}
                    activeOpacity={1}
                    onPress={() => setFabMenuVisible(false)}
                >
                    <View style={styles.fabMenu}>
                        <TouchableOpacity 
                            style={styles.fabMenuItem}
                            onPress={() => {
                                setFabMenuVisible(false);
                                setTimeout(() => alert('Código QR - En construcción'), 300);
                            }}
                        >
                            <Icon name="qr-code" size={24} color={Colors.primary} />
                            <Text style={styles.fabMenuItemText}>Código QR</Text>
                        </TouchableOpacity>

                        <View style={styles.fabMenuDivider} />

                        <TouchableOpacity 
                            style={styles.fabMenuItem}
                            onPress={() => {
                                setFabMenuVisible(false);
                                setTimeout(() => alert('Código Token - En construcción'), 300);
                            }}
                        >
                            <Icon name="key" size={24} color={Colors.primary} />
                            <Text style={styles.fabMenuItemText}>Código Token</Text>
                        </TouchableOpacity>

                        <View style={styles.fabMenuDivider} />

                        <TouchableOpacity 
                            style={styles.fabMenuItem}
                            onPress={() => {
                                setFabMenuVisible(false);
                                alert('Categoría: Obligatorio');
                            }}
                        >
                            <Icon name="pricetag" size={24} color={Colors.primary} />
                            <Text style={styles.fabMenuItemText}>Categoría: Obligatorio</Text>
                        </TouchableOpacity>
                    </View>
                </TouchableOpacity>
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
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 0.2,
        shadowRadius: 4,
    },
    menuButton: {
        padding: 4,
    },
    headerTitle: {
        fontSize: 20,
        fontWeight: '600',
        color: Colors.textDark,
    },
    whatsappButton: {
        padding: 4,
    },
    content: {
        flex: 1,
    },
    credentialCard: {
        backgroundColor: Colors.white,
        marginHorizontal: 16,
        marginTop: 16,
        borderRadius: 16,
        padding: 20,
        elevation: 6,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 3 },
        shadowOpacity: 0.15,
        shadowRadius: 12,
        borderWidth: 1,
        borderColor: Colors.light1,
    },
    cardHeader: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: 24,
        paddingBottom: 16,
        borderBottomWidth: 2,
        borderBottomColor: Colors.primary,
    },
    logoIpross: {
        width: 120,
        height: 45,
    },
    rioNegroLogo: {
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: Colors.primary,
        paddingHorizontal: 12,
        paddingVertical: 6,
        borderRadius: 8,
    },
    rioNegroText: {
        fontSize: 16,
        fontWeight: 'bold',
        color: Colors.white,
        letterSpacing: 1,
    },
    rioNegroSubtext: {
        fontSize: 8,
        fontWeight: '600',
        color: Colors.white,
        letterSpacing: 0.5,
    },
    userSection: {
        flexDirection: 'row',
        alignItems: 'flex-start',
        marginBottom: 20,
    },
    avatarContainer: {
        marginRight: 16,
    },
    avatar: {
        width: 90,
        height: 90,
        borderRadius: 45,
        backgroundColor: '#f5f5f5',
        justifyContent: 'center',
        alignItems: 'center',
        borderWidth: 4,
        borderColor: Colors.primary,
        elevation: 3,
    },
    userInfo: {
        flex: 1,
        justifyContent: 'center',
        paddingTop: 8,
    },
    userName: {
        fontSize: 20,
        fontWeight: 'bold',
        color: Colors.textDark,
        marginBottom: 12,
        textTransform: 'uppercase',
    },
    userDataRow: {
        flexDirection: 'row',
        marginBottom: 6,
    },
    userLabel: {
        fontSize: 13,
        color: Colors.grisOscuro,
        fontWeight: '500',
    },
    userNumber: {
        fontSize: 13,
        color: Colors.textDark,
        fontWeight: '600',
    },
    userDni: {
        fontSize: 13,
        color: Colors.textDark,
        fontWeight: '600',
    },
    expandButton: {
        flexDirection: 'row',
        justifyContent: 'center',
        alignItems: 'center',
        paddingVertical: 12,
        borderTopWidth: 1,
        borderTopColor: Colors.light1,
        marginTop: 4,
    },
    expandButtonText: {
        fontSize: 14,
        fontWeight: '600',
        color: Colors.primary,
        marginRight: 8,
    },
    expandedMenu: {
        marginTop: 12,
        borderTopWidth: 1,
        borderTopColor: Colors.light1,
        paddingTop: 12,
    },
    menuOption: {
        flexDirection: 'row',
        alignItems: 'center',
        paddingVertical: 14,
        paddingHorizontal: 8,
        borderRadius: 8,
        marginBottom: 8,
        backgroundColor: Colors.light1,
    },
    menuOptionText: {
        flex: 1,
        fontSize: 14,
        color: Colors.textDark,
        marginLeft: 12,
        fontWeight: '500',
    },
    section: {
        marginTop: 16,
    },
    sectionTitle: {
        fontSize: 16,
        fontWeight: '600',
        color: Colors.textDark,
        marginHorizontal: 16,
        marginBottom: 12,
    },
    statusCard: {
        backgroundColor: Colors.white,
        marginHorizontal: 16,
        borderRadius: 8,
        padding: 16,
        marginBottom: 12,
        elevation: 2,
    },
    statusRow: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
    },
    statusLabel: {
        fontSize: 16,
        fontWeight: '500',
        color: Colors.textDark,
    },
    infoCard: {
        backgroundColor: Colors.white,
        marginHorizontal: 16,
        borderRadius: 8,
        padding: 16,
        elevation: 2,
    },
    copagoLabel: {
        fontSize: 12,
        fontWeight: '600',
        color: Colors.primary,
        marginBottom: 4,
    },
    copagoDate: {
        fontSize: 14,
        color: Colors.textDark,
        marginBottom: 8,
    },
    copagoAmount: {
        fontSize: 24,
        fontWeight: 'bold',
        color: Colors.textDark,
        textAlign: 'right',
    },
    quickActions: {
        flexDirection: 'row',
        justifyContent: 'space-around',
        marginTop: 24,
        marginHorizontal: 16,
        marginBottom: 16,
    },
    actionButton: {
        alignItems: 'center',
        flex: 1,
    },
    actionText: {
        fontSize: 12,
        color: Colors.grisOscuro,
        marginTop: 8,
        textAlign: 'center',
    },
    bottomIcons: {
        flexDirection: 'row',
        justifyContent: 'space-around',
        marginVertical: 20,
        marginHorizontal: 60,
    },
    iconButton: {
        padding: 12,
    },
    fab: {
        position: 'absolute',
        right: 20,
        bottom: 90,
        width: 56,
        height: 56,
        borderRadius: 28,
        backgroundColor: Colors.primary,
        justifyContent: 'center',
        alignItems: 'center',
        elevation: 6,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 3 },
        shadowOpacity: 0.3,
        shadowRadius: 6,
    },
    tabBar: {
        flexDirection: 'row',
        backgroundColor: Colors.white,
        borderTopWidth: 1,
        borderTopColor: Colors.light2,
        paddingVertical: 8,
        elevation: 8,
    },
    tab: {
        flex: 1,
        alignItems: 'center',
        paddingVertical: 8,
    },
    tabActive: {
        flex: 1,
        alignItems: 'center',
        paddingVertical: 8,
    },
    tabText: {
        fontSize: 11,
        color: Colors.grisOscuro,
        marginTop: 4,
    },
    tabTextActive: {
        fontSize: 11,
        color: Colors.primary,
        marginTop: 4,
        fontWeight: '600',
    },
    
    // Sidebar Styles
    sidebarContainer: {
        flex: 1,
        flexDirection: 'row',
    },
    sidebarOverlay: {
        flex: 1,
        backgroundColor: 'rgba(0,0,0,0.5)',
    },
    sidebar: {
        width: 280,
        backgroundColor: Colors.white,
        elevation: 16,
        shadowColor: '#000',
        shadowOffset: { width: 2, height: 0 },
        shadowOpacity: 0.3,
        shadowRadius: 8,
    },
    sidebarHeader: {
        backgroundColor: Colors.primary,
        padding: 24,
        paddingTop: 40,
    },
    sidebarTitle: {
        fontSize: 20,
        fontWeight: 'bold',
        color: Colors.textDark,
        marginBottom: 4,
    },
    sidebarSubtitle: {
        fontSize: 14,
        color: Colors.textDark,
        opacity: 0.8,
    },
    sidebarMenu: {
        flex: 1,
    },
    menuItem: {
        flexDirection: 'row',
        alignItems: 'center',
        paddingVertical: 16,
        paddingHorizontal: 20,
        borderBottomWidth: 1,
        borderBottomColor: Colors.light1,
    },
    menuItemText: {
        fontSize: 16,
        color: Colors.textDark,
        marginLeft: 16,
    },
    divider: {
        height: 1,
        backgroundColor: Colors.light2,
        marginVertical: 8,
    },
    menuItemLogout: {
        flexDirection: 'row',
        alignItems: 'center',
        paddingVertical: 16,
        paddingHorizontal: 20,
    },
    menuItemLogoutText: {
        fontSize: 16,
        color: '#d32f2f',
        marginLeft: 16,
        fontWeight: '600',
    },
    version: {
        fontSize: 12,
        color: Colors.grisOscuro,
        textAlign: 'center',
        paddingVertical: 20,
    },
    // Botón flotante (FAB)
    fab: {
        position: 'absolute',
        bottom: 80,
        right: 20,
        width: 60,
        height: 60,
        borderRadius: 30,
        backgroundColor: Colors.primary,
        justifyContent: 'center',
        alignItems: 'center',
        elevation: 8,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 4 },
        shadowOpacity: 0.3,
        shadowRadius: 4,
    },
    fabModalOverlay: {
        flex: 1,
        backgroundColor: 'rgba(0,0,0,0.5)',
        justifyContent: 'flex-end',
        alignItems: 'flex-end',
        paddingBottom: 150,
        paddingRight: 20,
    },
    fabMenu: {
        backgroundColor: '#fff',
        borderRadius: 12,
        padding: 8,
        minWidth: 220,
        elevation: 8,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 4 },
        shadowOpacity: 0.3,
        shadowRadius: 4,
    },
    fabMenuItem: {
        flexDirection: 'row',
        alignItems: 'center',
        padding: 16,
    },
    fabMenuItemText: {
        fontSize: 16,
        color: Colors.grisOscuro,
        marginLeft: 16,
        fontWeight: '500',
    },
    fabMenuDivider: {
        height: 1,
        backgroundColor: Colors.light2,
    },
});

export default HomeScreen;
