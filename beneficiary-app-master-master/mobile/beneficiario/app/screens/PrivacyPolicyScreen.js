import React from 'react';
import {
    View,
    Text,
    StyleSheet,
    ScrollView,
    TouchableOpacity,
    Linking,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';

const PrivacyPolicyScreen = ({ onBack }) => {
    const openEmail = () => {
        Linking.openURL('mailto:consultas@ipross.gob.ar');
    };

    const openWebsite = () => {
        Linking.openURL('https://www.ipross.gob.ar');
    };

    return (
        <View style={styles.container}>
            {/* Header */}
            <View style={styles.header}>
                <TouchableOpacity onPress={onBack} style={styles.backButton}>
                    <Ionicons name="arrow-back" size={24} color="#fff" />
                </TouchableOpacity>
                <Text style={styles.headerTitle}>Política de Privacidad</Text>
                <View style={styles.placeholder} />
            </View>

            <ScrollView style={styles.scrollView} showsVerticalScrollIndicator={false}>
                <View style={styles.content}>
                    {/* Logo y título */}
                    <View style={styles.logoSection}>
                        <Ionicons name="shield-checkmark" size={60} color="#6ac64f" />
                        <Text style={styles.appName}>IPROSS Beneficiarios</Text>
                        <Text style={styles.institutionName}>
                            Instituto Provincial del Seguro de Salud de Río Negro
                        </Text>
                        <Text style={styles.lastUpdate}>Última actualización: 17 de octubre de 2025</Text>
                    </View>

                    {/* 1. Introducción */}
                    <View style={styles.section}>
                        <Text style={styles.sectionTitle}>1. Introducción</Text>
                        <Text style={styles.sectionText}>
                            Esta Política de Privacidad describe cómo la aplicación <Text style={styles.bold}>IPROSS Beneficiarios</Text> recopila, 
                            utiliza y protege la información personal de los usuarios. Al utilizar esta aplicación, usted acepta 
                            las prácticas descritas en esta política.
                        </Text>
                    </View>

                    {/* 2. Información que Recopilamos */}
                    <View style={styles.section}>
                        <Text style={styles.sectionTitle}>2. Información que Recopilamos</Text>
                        <Text style={styles.sectionText}>
                            Nuestra aplicación puede recopilar los siguientes tipos de información:
                        </Text>
                        <View style={styles.bulletList}>
                            <View style={styles.bulletItem}>
                                <Ionicons name="checkmark-circle" size={18} color="#6ac64f" />
                                <Text style={styles.bulletText}>
                                    <Text style={styles.bold}>Información de Identificación Personal:</Text> DNI, nombre completo, 
                                    número de afiliado, fecha de nacimiento.
                                </Text>
                            </View>
                            <View style={styles.bulletItem}>
                                <Ionicons name="checkmark-circle" size={18} color="#6ac64f" />
                                <Text style={styles.bulletText}>
                                    <Text style={styles.bold}>Información de Contacto:</Text> Dirección, teléfono, correo electrónico.
                                </Text>
                            </View>
                            <View style={styles.bulletItem}>
                                <Ionicons name="checkmark-circle" size={18} color="#6ac64f" />
                                <Text style={styles.bulletText}>
                                    <Text style={styles.bold}>Información de Salud:</Text> Datos relacionados con prestaciones médicas, 
                                    autorizaciones, recetas y trámites.
                                </Text>
                            </View>
                            <View style={styles.bulletItem}>
                                <Ionicons name="checkmark-circle" size={18} color="#6ac64f" />
                                <Text style={styles.bulletText}>
                                    <Text style={styles.bold}>Datos de Uso:</Text> Información sobre cómo utiliza la aplicación.
                                </Text>
                            </View>
                        </View>
                    </View>

                    {/* 3. Permisos de la Aplicación */}
                    <View style={styles.section}>
                        <Text style={styles.sectionTitle}>3. Permisos de la Aplicación</Text>
                        <Text style={styles.sectionText}>La aplicación solicita los siguientes permisos:</Text>
                        <View style={styles.bulletList}>
                            <View style={styles.bulletItem}>
                                <Ionicons name="camera" size={18} color="#6ac64f" />
                                <Text style={styles.bulletText}>
                                    <Text style={styles.bold}>CÁMARA:</Text> Para escanear códigos QR de prestadores y credenciales.
                                </Text>
                            </View>
                            <View style={styles.bulletItem}>
                                <Ionicons name="folder" size={18} color="#6ac64f" />
                                <Text style={styles.bulletText}>
                                    <Text style={styles.bold}>ALMACENAMIENTO:</Text> Para guardar documentos relacionados con trámites.
                                </Text>
                            </View>
                            <View style={styles.bulletItem}>
                                <Ionicons name="wifi" size={18} color="#6ac64f" />
                                <Text style={styles.bulletText}>
                                    <Text style={styles.bold}>INTERNET:</Text> Para conectarse con los servidores de IPROSS.
                                </Text>
                            </View>
                            <View style={styles.bulletItem}>
                                <Ionicons name="notifications" size={18} color="#6ac64f" />
                                <Text style={styles.bulletText}>
                                    <Text style={styles.bold}>NOTIFICACIONES:</Text> Para alertas sobre trámites y turnos.
                                </Text>
                            </View>
                        </View>
                    </View>

                    {/* 4. Uso de la Información */}
                    <View style={styles.section}>
                        <Text style={styles.sectionTitle}>4. Uso de la Información</Text>
                        <Text style={styles.sectionText}>Utilizamos la información recopilada para:</Text>
                        <View style={styles.bulletList}>
                            <View style={styles.bulletItem}>
                                <Ionicons name="person" size={18} color="#6ac64f" />
                                <Text style={styles.bulletText}>
                                    Proporcionar acceso a su información como beneficiario de IPROSS
                                </Text>
                            </View>
                            <View style={styles.bulletItem}>
                                <Ionicons name="document-text" size={18} color="#6ac64f" />
                                <Text style={styles.bulletText}>
                                    Gestionar trámites, autorizaciones y solicitudes
                                </Text>
                            </View>
                            <View style={styles.bulletItem}>
                                <Ionicons name="qr-code" size={18} color="#6ac64f" />
                                <Text style={styles.bulletText}>
                                    Generar credenciales digitales y códigos QR de identificación
                                </Text>
                            </View>
                            <View style={styles.bulletItem}>
                                <Ionicons name="notifications-outline" size={18} color="#6ac64f" />
                                <Text style={styles.bulletText}>
                                    Enviar notificaciones sobre el estado de sus gestiones
                                </Text>
                            </View>
                        </View>
                    </View>

                    {/* 5. Seguridad de los Datos */}
                    <View style={styles.section}>
                        <Text style={styles.sectionTitle}>5. Seguridad de los Datos</Text>
                        <Text style={styles.sectionText}>
                            Implementamos medidas de seguridad técnicas y organizativas para proteger su información:
                        </Text>
                        <View style={styles.bulletList}>
                            <View style={styles.bulletItem}>
                                <Ionicons name="lock-closed" size={18} color="#6ac64f" />
                                <Text style={styles.bulletText}>Encriptación de datos en tránsito y en reposo</Text>
                            </View>
                            <View style={styles.bulletItem}>
                                <Ionicons name="key" size={18} color="#6ac64f" />
                                <Text style={styles.bulletText}>Autenticación segura mediante DNI y contraseña</Text>
                            </View>
                            <View style={styles.bulletItem}>
                                <Ionicons name="time" size={18} color="#6ac64f" />
                                <Text style={styles.bulletText}>Tokens de acceso temporales para mayor seguridad</Text>
                            </View>
                            <View style={styles.bulletItem}>
                                <Ionicons name="shield" size={18} color="#6ac64f" />
                                <Text style={styles.bulletText}>Monitoreo constante de accesos no autorizados</Text>
                            </View>
                        </View>
                    </View>

                    {/* 6. Sus Derechos */}
                    <View style={styles.section}>
                        <Text style={styles.sectionTitle}>6. Sus Derechos</Text>
                        <Text style={styles.sectionText}>Como usuario de la aplicación, usted tiene derecho a:</Text>
                        <View style={styles.bulletList}>
                            <View style={styles.bulletItem}>
                                <Ionicons name="eye" size={18} color="#6ac64f" />
                                <Text style={styles.bulletText}>
                                    <Text style={styles.bold}>Acceder</Text> a su información personal
                                </Text>
                            </View>
                            <View style={styles.bulletItem}>
                                <Ionicons name="create" size={18} color="#6ac64f" />
                                <Text style={styles.bulletText}>
                                    <Text style={styles.bold}>Corregir</Text> datos inexactos o incompletos
                                </Text>
                            </View>
                            <View style={styles.bulletItem}>
                                <Ionicons name="trash" size={18} color="#6ac64f" />
                                <Text style={styles.bulletText}>
                                    <Text style={styles.bold}>Solicitar la eliminación</Text> de sus datos (sujeto a obligaciones legales)
                                </Text>
                            </View>
                            <View style={styles.bulletItem}>
                                <Ionicons name="close-circle" size={18} color="#6ac64f" />
                                <Text style={styles.bulletText}>
                                    <Text style={styles.bold}>Revocar el consentimiento</Text> para el uso de ciertos datos
                                </Text>
                            </View>
                        </View>
                    </View>

                    {/* 7. Compartir Información */}
                    <View style={styles.section}>
                        <Text style={styles.sectionTitle}>7. Compartir Información</Text>
                        <Text style={styles.sectionText}>
                            IPROSS no vende, alquila ni comparte su información personal con terceros, excepto:
                        </Text>
                        <View style={styles.bulletList}>
                            <View style={styles.bulletItem}>
                                <Ionicons name="medkit" size={18} color="#6ac64f" />
                                <Text style={styles.bulletText}>
                                    Con prestadores de salud autorizados para brindarle atención médica
                                </Text>
                            </View>
                            <View style={styles.bulletItem}>
                                <Ionicons name="document" size={18} color="#6ac64f" />
                                <Text style={styles.bulletText}>Con autoridades sanitarias cuando sea requerido por ley</Text>
                            </View>
                            <View style={styles.bulletItem}>
                                <Ionicons name="people" size={18} color="#6ac64f" />
                                <Text style={styles.bulletText}>
                                    Con proveedores de servicios técnicos (sujetos a acuerdos de confidencialidad)
                                </Text>
                            </View>
                        </View>
                    </View>

                    {/* 8. Cumplimiento Legal */}
                    <View style={styles.section}>
                        <Text style={styles.sectionTitle}>8. Cumplimiento Legal</Text>
                        <Text style={styles.sectionText}>Esta política cumple con:</Text>
                        <View style={styles.bulletList}>
                            <View style={styles.bulletItem}>
                                <Ionicons name="document-text" size={18} color="#6ac64f" />
                                <Text style={styles.bulletText}>
                                    Ley Nacional de Protección de Datos Personales N° 25.326
                                </Text>
                            </View>
                            <View style={styles.bulletItem}>
                                <Ionicons name="document-text" size={18} color="#6ac64f" />
                                <Text style={styles.bulletText}>
                                    Disposición 60/2016 de la Dirección Nacional de Protección de Datos Personales
                                </Text>
                            </View>
                            <View style={styles.bulletItem}>
                                <Ionicons name="location" size={18} color="#6ac64f" />
                                <Text style={styles.bulletText}>Normativas provinciales de Río Negro aplicables</Text>
                            </View>
                        </View>
                    </View>

                    {/* Contacto */}
                    <View style={styles.contactSection}>
                        <Ionicons name="mail" size={40} color="#6ac64f" style={styles.contactIcon} />
                        <Text style={styles.sectionTitle}>Contacto</Text>
                        <Text style={styles.sectionText}>
                            Si tiene preguntas sobre esta Política de Privacidad o desea ejercer sus derechos:
                        </Text>
                        
                        <View style={styles.contactCard}>
                            <View style={styles.contactItem}>
                                <Ionicons name="business" size={20} color="#2c5f2d" />
                                <Text style={styles.contactText}>
                                    IPROSS - Instituto Provincial del Seguro de Salud
                                </Text>
                            </View>
                            <View style={styles.contactItem}>
                                <Ionicons name="location-outline" size={20} color="#2c5f2d" />
                                <Text style={styles.contactText}>Río Negro, Argentina</Text>
                            </View>
                            <TouchableOpacity style={styles.contactItem} onPress={openEmail}>
                                <Ionicons name="mail-outline" size={20} color="#2c5f2d" />
                                <Text style={[styles.contactText, styles.link]}>consultas@ipross.gob.ar</Text>
                            </TouchableOpacity>
                            <TouchableOpacity style={styles.contactItem} onPress={openWebsite}>
                                <Ionicons name="globe-outline" size={20} color="#2c5f2d" />
                                <Text style={[styles.contactText, styles.link]}>www.ipross.gob.ar</Text>
                            </TouchableOpacity>
                            <View style={styles.contactItem}>
                                <Ionicons name="time-outline" size={20} color="#2c5f2d" />
                                <Text style={styles.contactText}>Lunes a Viernes de 8:00 a 16:00 hs</Text>
                            </View>
                        </View>
                    </View>

                    {/* Footer */}
                    <View style={styles.footer}>
                        <Text style={styles.footerText}>
                            © 2025 IPROSS - Instituto Provincial del Seguro de Salud de Río Negro
                        </Text>
                        <Text style={styles.footerText}>Todos los derechos reservados</Text>
                    </View>
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
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'space-between',
        backgroundColor: '#2c5f2d',
        paddingTop: 50,
        paddingBottom: 15,
        paddingHorizontal: 15,
        elevation: 4,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 0.2,
        shadowRadius: 4,
    },
    backButton: {
        padding: 5,
    },
    headerTitle: {
        fontSize: 18,
        fontWeight: 'bold',
        color: '#fff',
    },
    placeholder: {
        width: 34,
    },
    scrollView: {
        flex: 1,
    },
    content: {
        padding: 20,
    },
    logoSection: {
        alignItems: 'center',
        marginBottom: 30,
        backgroundColor: '#fff',
        padding: 25,
        borderRadius: 15,
        elevation: 2,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 1 },
        shadowOpacity: 0.1,
        shadowRadius: 3,
    },
    appName: {
        fontSize: 24,
        fontWeight: 'bold',
        color: '#2c5f2d',
        marginTop: 15,
        marginBottom: 5,
    },
    institutionName: {
        fontSize: 14,
        color: '#666',
        textAlign: 'center',
        marginBottom: 10,
    },
    lastUpdate: {
        fontSize: 12,
        color: '#999',
        fontStyle: 'italic',
        marginTop: 10,
    },
    section: {
        backgroundColor: '#fff',
        padding: 20,
        borderRadius: 12,
        marginBottom: 15,
        elevation: 1,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 1 },
        shadowOpacity: 0.05,
        shadowRadius: 2,
    },
    sectionTitle: {
        fontSize: 18,
        fontWeight: 'bold',
        color: '#2c5f2d',
        marginBottom: 12,
    },
    sectionText: {
        fontSize: 15,
        color: '#444',
        lineHeight: 22,
        marginBottom: 10,
    },
    bold: {
        fontWeight: 'bold',
        color: '#2c5f2d',
    },
    bulletList: {
        marginTop: 10,
    },
    bulletItem: {
        flexDirection: 'row',
        alignItems: 'flex-start',
        marginBottom: 12,
    },
    bulletText: {
        fontSize: 14,
        color: '#555',
        marginLeft: 10,
        flex: 1,
        lineHeight: 20,
    },
    contactSection: {
        backgroundColor: '#fff',
        padding: 20,
        borderRadius: 12,
        marginBottom: 15,
        alignItems: 'center',
        elevation: 2,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 1 },
        shadowOpacity: 0.1,
        shadowRadius: 3,
    },
    contactIcon: {
        marginBottom: 15,
    },
    contactCard: {
        width: '100%',
        backgroundColor: '#f9f9f9',
        padding: 15,
        borderRadius: 10,
        marginTop: 15,
    },
    contactItem: {
        flexDirection: 'row',
        alignItems: 'center',
        marginBottom: 12,
    },
    contactText: {
        fontSize: 14,
        color: '#444',
        marginLeft: 12,
        flex: 1,
    },
    link: {
        color: '#6ac64f',
        textDecorationLine: 'underline',
    },
    footer: {
        alignItems: 'center',
        paddingVertical: 30,
        borderTopWidth: 1,
        borderTopColor: '#e0e0e0',
        marginTop: 20,
    },
    footerText: {
        fontSize: 12,
        color: '#999',
        textAlign: 'center',
        marginBottom: 5,
    },
});

export default PrivacyPolicyScreen;
