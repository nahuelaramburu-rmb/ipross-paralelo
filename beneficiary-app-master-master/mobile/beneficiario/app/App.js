import React, { Component } from 'react';
import {
    View,
    Text,
    StyleSheet,
    TextInput,
    TouchableOpacity,
    Alert,
    KeyboardAvoidingView,
    Platform,
    ScrollView,
    StatusBar,
    ActivityIndicator,
} from 'react-native';
import { SafeAreaProvider, SafeAreaView } from 'react-native-safe-area-context';
import axios from 'axios';

class App extends Component {
    constructor(props) {
        super(props);
        this.state = {
            username: '',
            password: '',
            isLoading: false,
            showPassword: false,
        };
    }

    handleLogin = async () => {
        const { username, password } = this.state;

        if (!username.trim() || !password.trim()) {
            Alert.alert('Error', 'Por favor ingrese usuario y contraseña');
            return;
        }

        this.setState({ isLoading: true });

        try {
            // Configuración real basada en el código original encontrado
            const identityHost = 'http://localhost:8080'; // Cambiar por la URL real del servidor
            const endpoint = `${identityHost}/identity-service/v1/oauth/token`;
            
            const loginData = new URLSearchParams({
                username: username.trim(),
                password: password.trim(),
                grant_type: 'password',
                client_id: 'client', // Credenciales encontradas en application-test.properties
                client_secret: 'secret', // Credenciales encontradas en application-test.properties
            });

            const response = await axios.post(endpoint, loginData, {
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                timeout: 10000, // 10 segundos de timeout
            });

            if (response.data && response.data.access_token) {
                this.setState({ isLoading: false });
                // Guardar el token para futuras peticiones
                Alert.alert('Éxito', `Bienvenido ${username} a IPROSS`);
                console.log('Token obtenido:', response.data.access_token);
            } else {
                throw new Error('No se recibió token de acceso');
            }

        } catch (error) {
            this.setState({ isLoading: false });
            console.error('Login error:', error);
            
            if (error.code === 'ECONNABORTED') {
                Alert.alert('Error', 'Tiempo de espera agotado. Verifique su conexión.');
            } else if (error.response?.status === 401) {
                Alert.alert('Error', 'Usuario y/o contraseña incorrectos');
            } else if (error.response?.status >= 500) {
                Alert.alert('Error', 'Error del servidor. Intente más tarde.');
            } else {
                Alert.alert('Error', 'No se pudo conectar con el servidor. Verifique su conexión a internet.');
            }
        }
    };

    togglePasswordVisibility = () => {
        this.setState(prevState => ({
            showPassword: !prevState.showPassword
        }));
    };

    render() {
        const { username, password, isLoading, showPassword } = this.state;

        return (
            <SafeAreaProvider>
                <StatusBar barStyle="light-content" backgroundColor="#1a365d" />
                <SafeAreaView style={styles.container}>
                    <KeyboardAvoidingView
                        style={styles.keyboardContainer}
                        behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
                    >
                        <ScrollView 
                            contentContainerStyle={styles.scrollContainer}
                            showsVerticalScrollIndicator={false}
                        >
                            {/* Header con logo */}
                            <View style={styles.headerContainer}>
                                <View style={styles.logoContainer}>
                                    <View style={styles.logoCircle}>
                                        <Text style={styles.logoText}>I</Text>
                                    </View>
                                    <Text style={styles.appTitle}>IPROSS</Text>
                                    <Text style={styles.appSubtitle}>Instituto Provincial de Obra Social</Text>
                                </View>
                            </View>

                            {/* Formulario de login */}
                            <View style={styles.formContainer}>
                                <View style={styles.welcomeContainer}>
                                    <Text style={styles.welcomeTitle}>Bienvenido</Text>
                                    <Text style={styles.welcomeSubtitle}>
                                        Ingrese sus credenciales para acceder a su cuenta
                                    </Text>
                                </View>

                                <View style={styles.inputsContainer}>
                                    <View style={styles.inputWrapper}>
                                        <Text style={styles.inputLabel}>Número de Beneficiario</Text>
                                        <View style={styles.inputContainer}>
                                            <TextInput
                                                style={styles.input}
                                                value={username}
                                                onChangeText={(text) => this.setState({ username: text })}
                                                placeholder='Ingrese su número de beneficiario'
                                                placeholderTextColor='#9ca3af'
                                                autoCapitalize='none'
                                                autoCorrect={false}
                                                keyboardType='numeric'
                                                returnKeyType='next'
                                                onSubmitEditing={() => this.passwordInput?.focus()}
                                            />
                                        </View>
                                    </View>

                                    <View style={styles.inputWrapper}>
                                        <Text style={styles.inputLabel}>Contraseña</Text>
                                        <View style={styles.inputContainer}>
                                            <TextInput
                                                ref={ref => this.passwordInput = ref}
                                                style={[styles.input, { paddingRight: 50 }]}
                                                value={password}
                                                onChangeText={(text) => this.setState({ password: text })}
                                                placeholder='Ingrese su contraseña'
                                                placeholderTextColor='#9ca3af'
                                                secureTextEntry={!showPassword}
                                                returnKeyType='done'
                                                onSubmitEditing={this.handleLogin}
                                            />
                                            <TouchableOpacity 
                                                style={styles.eyeButton}
                                                onPress={this.togglePasswordVisibility}
                                            >
                                                <Text style={styles.eyeIcon}>
                                                    {showPassword ? '👁️' : '👁️‍🗨️'}
                                                </Text>
                                            </TouchableOpacity>
                                        </View>
                                    </View>
                                </View>

                                <TouchableOpacity
                                    style={[styles.loginButton, isLoading && styles.loginButtonDisabled]}
                                    onPress={this.handleLogin}
                                    disabled={isLoading}
                                    activeOpacity={0.8}
                                >
                                    {isLoading ? (
                                        <View style={styles.loadingContainer}>
                                            <ActivityIndicator size="small" color="#ffffff" />
                                            <Text style={styles.loginButtonText}>Ingresando...</Text>
                                        </View>
                                    ) : (
                                        <Text style={styles.loginButtonText}>Ingresar</Text>
                                    )}
                                </TouchableOpacity>

                                <View style={styles.footerContainer}>
                                    <TouchableOpacity style={styles.forgotPasswordButton}>
                                        <Text style={styles.forgotPasswordText}>
                                            ¿Olvidaste tu contraseña?
                                        </Text>
                                    </TouchableOpacity>
                                    
                                    <View style={styles.registerContainer}>
                                        <Text style={styles.registerText}>¿No tienes una cuenta? </Text>
                                        <TouchableOpacity>
                                            <Text style={styles.registerLink}>Regístrate aquí</Text>
                                        </TouchableOpacity>
                                    </View>
                                </View>
                            </View>
                        </ScrollView>
                    </KeyboardAvoidingView>
                </SafeAreaView>
            </SafeAreaProvider>
        );
    }
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: '#f8fafc',
    },
    keyboardContainer: {
        flex: 1,
    },
    scrollContainer: {
        flexGrow: 1,
        justifyContent: 'center',
        paddingHorizontal: 24,
        paddingVertical: 32,
    },
    headerContainer: {
        alignItems: 'center',
        marginBottom: 48,
    },
    logoContainer: {
        alignItems: 'center',
    },
    logoCircle: {
        width: 80,
        height: 80,
        borderRadius: 40,
        backgroundColor: '#2563eb',
        justifyContent: 'center',
        alignItems: 'center',
        marginBottom: 16,
        shadowColor: '#2563eb',
        shadowOffset: {
            width: 0,
            height: 4,
        },
        shadowOpacity: 0.3,
        shadowRadius: 8,
        elevation: 8,
    },
    logoText: {
        fontSize: 36,
        fontWeight: 'bold',
        color: '#ffffff',
    },
    appTitle: {
        fontSize: 32,
        fontWeight: 'bold',
        color: '#1e293b',
        marginBottom: 4,
    },
    appSubtitle: {
        fontSize: 16,
        color: '#64748b',
        textAlign: 'center',
    },
    formContainer: {
        backgroundColor: '#ffffff',
        borderRadius: 16,
        padding: 24,
        shadowColor: '#000',
        shadowOffset: {
            width: 0,
            height: 2,
        },
        shadowOpacity: 0.1,
        shadowRadius: 8,
        elevation: 4,
    },
    welcomeContainer: {
        alignItems: 'center',
        marginBottom: 32,
    },
    welcomeTitle: {
        fontSize: 28,
        fontWeight: 'bold',
        color: '#1e293b',
        marginBottom: 8,
    },
    welcomeSubtitle: {
        fontSize: 16,
        color: '#64748b',
        textAlign: 'center',
        lineHeight: 24,
    },
    inputsContainer: {
        marginBottom: 24,
    },
    inputWrapper: {
        marginBottom: 20,
    },
    inputLabel: {
        fontSize: 14,
        fontWeight: '600',
        color: '#374151',
        marginBottom: 8,
    },
    inputContainer: {
        position: 'relative',
    },
    input: {
        borderWidth: 1,
        borderColor: '#d1d5db',
        borderRadius: 12,
        paddingHorizontal: 16,
        paddingVertical: 14,
        fontSize: 16,
        backgroundColor: '#f9fafb',
        color: '#1f2937',
    },
    eyeButton: {
        position: 'absolute',
        right: 12,
        top: '50%',
        transform: [{ translateY: -12 }],
        padding: 4,
    },
    eyeIcon: {
        fontSize: 20,
    },
    loginButton: {
        backgroundColor: '#2563eb',
        borderRadius: 12,
        paddingVertical: 16,
        alignItems: 'center',
        marginBottom: 24,
        shadowColor: '#2563eb',
        shadowOffset: {
            width: 0,
            height: 4,
        },
        shadowOpacity: 0.3,
        shadowRadius: 8,
        elevation: 4,
    },
    loginButtonDisabled: {
        backgroundColor: '#9ca3af',
        shadowOpacity: 0,
        elevation: 0,
    },
    loadingContainer: {
        flexDirection: 'row',
        alignItems: 'center',
    },
    loginButtonText: {
        color: '#ffffff',
        fontSize: 18,
        fontWeight: 'bold',
        marginLeft: 8,
    },
    footerContainer: {
        alignItems: 'center',
    },
    forgotPasswordButton: {
        marginBottom: 16,
    },
    forgotPasswordText: {
        color: '#2563eb',
        fontSize: 16,
        fontWeight: '500',
    },
    registerContainer: {
        flexDirection: 'row',
        alignItems: 'center',
    },
    registerText: {
        color: '#64748b',
        fontSize: 14,
    },
    registerLink: {
        color: '#2563eb',
        fontSize: 14,
        fontWeight: '600',
    },
});

export default App;
