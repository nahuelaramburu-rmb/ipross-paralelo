import React, { Component } from 'react';
import {
    View,
    Text,
    TextInput,
    TouchableOpacity,
    Alert,
    StyleSheet,
    Image,
    ActivityIndicator,
} from 'react-native';
import axios from 'axios';
import Router from './components/RouterSimple';

class App extends Component {
    constructor(props) {
        super(props);
        this.state = {
            username: '',
            password: '',
            isLoading: false,
            isLoggedIn: false,
            loginSuccess: false,
            loggedUser: null,
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
            // Llamada a la API real de IPROSS
            const response = await axios.post('https://backend-ipross-production.up.railway.app/api/auth/login', {
                numero_afiliado: username,
                contraseña: password,
            });

            const userData = response.data;

            if (userData.status === 'success') {
                // Login exitoso
                this.setState({
                    isLoading: false,
                    loginSuccess: true,
                    loggedUser: userData,
                });

                // Mostrar mensaje de éxito por 2 segundos
                setTimeout(() => {
                    this.setState({
                        isLoggedIn: true,
                        loginSuccess: false,
                        username: '',
                        password: '',
                    });
                }, 2000);
            } else {
                // Credenciales incorrectas
                this.setState({ isLoading: false });
                Alert.alert('Error', 'Credenciales incorrectas. Verifique su usuario y contraseña.');
            }
        } catch (error) {
            this.setState({ isLoading: false });
            console.error('Error de login:', error);
            
            if (error.response && error.response.status === 404) {
                Alert.alert('Error', 'Usuario no encontrado. Verifique sus credenciales.');
            } else {
                Alert.alert('Error', 'Error en la conexión. Intente nuevamente.');
            }
        }
    };

    render() {
        const { isLoggedIn, loginSuccess } = this.state;

        if (isLoggedIn) {
            // Menú oficial: panel principal de la app
            return <Router loggedUser={this.state.loggedUser} />;
        }

        // Mostrar mensaje de éxito
        if (loginSuccess) {
            return (
                <View style={[styles.container, styles.successContainer]}>
                    <View style={styles.successMessage}>
                        <Text style={styles.successText}>✅ Logueado con éxito</Text>
                        <Text style={styles.successSubText}>Redirigiendo...</Text>
                    </View>
                </View>
            );
        }
        return (
            <View style={styles.container}>
                <View style={styles.logoContainer}>
                    <Image
                        source={require('./images/ipross_logo_green.jpg')}
                        style={styles.logoImage}
                        resizeMode='cover'
                    />
                </View>

                <View style={styles.formContainer}>
                    <Text style={styles.welcomeTitle}>Bienvenido</Text>

                    <View style={styles.inputWrapper}>
                        <Text style={styles.inputLabel}>Número de Beneficiario</Text>
                        <TextInput
                            style={styles.input}
                            value={this.state.username}
                            onChangeText={(text) => this.setState({ username: text })}
                            placeholder='Ingrese su número de beneficiario'
                            keyboardType='numeric'
                        />
                    </View>

                    <View style={styles.inputWrapper}>
                        <Text style={styles.inputLabel}>Contraseña</Text>
                        <TextInput
                            style={styles.input}
                            value={this.state.password}
                            onChangeText={(text) => this.setState({ password: text })}
                            placeholder='Ingrese su contraseña'
                            secureTextEntry={true}
                        />
                    </View>

                    <TouchableOpacity
                        style={[styles.loginButton, this.state.isLoading && styles.loginButtonDisabled]}
                        onPress={this.handleLogin}
                        disabled={this.state.isLoading}>
                        {this.state.isLoading ? (
                            <View style={styles.loadingContainer}>
                                <ActivityIndicator color='#ffffff' />
                                <Text style={styles.loginButtonText}>Iniciando sesión...</Text>
                            </View>
                        ) : (
                            <Text style={styles.loginButtonText}>Iniciar Sesión</Text>
                        )}
                    </TouchableOpacity>

                    <TouchableOpacity style={styles.forgotButton}>
                        <Text style={styles.forgotText}>¿Olvidaste tu contraseña?</Text>
                    </TouchableOpacity>
                </View>
            </View>
        );
    }
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: '#6ac54e',
        paddingHorizontal: 24,
        paddingVertical: 32,
        justifyContent: 'center',
    },
    logoContainer: {
        alignItems: 'center',
        marginBottom: 48,
    },
    logoImage: {
        width: 400,
        height: 200,
        marginHorizontal: -40,
    },
    formContainer: {
        backgroundColor: '#e8f5e8',
        borderRadius: 16,
        padding: 24,
        shadowColor: '#000',
        shadowOffset: {
            width: 0,
            height: 2,
        },
        shadowOpacity: 0.1,
        shadowRadius: 8,
        elevation: 5,
    },
    welcomeTitle: {
        fontSize: 28,
        fontWeight: 'bold',
        color: '#1e293b',
        textAlign: 'center',
        marginBottom: 32,
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
    input: {
        borderWidth: 1,
        borderColor: '#d1d5db',
        borderRadius: 12,
        paddingHorizontal: 16,
        paddingVertical: 14,
        fontSize: 16,
        backgroundColor: '#f9fafb',
    },
    loginButton: {
        backgroundColor: '#4a9f3a',
        borderRadius: 12,
        paddingVertical: 16,
        alignItems: 'center',
        marginVertical: 24,
        shadowColor: '#000',
        shadowOffset: {
            width: 0,
            height: 2,
        },
        shadowOpacity: 0.2,
        shadowRadius: 4,
        elevation: 3,
    },
    loginButtonText: {
        color: '#ffffff',
        fontSize: 18,
        fontWeight: 'bold',
    },
    forgotButton: {
        alignItems: 'center',
    },
    forgotText: {
        color: '#4a9f3a',
        fontSize: 16,
        fontWeight: '500',
    },
    // Estilos para loading
    loginButtonDisabled: {
        backgroundColor: '#9ca3af',
    },
    loadingContainer: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: 8,
    },
    // Estilos para mensaje de éxito
    successContainer: {
        justifyContent: 'center',
        alignItems: 'center',
    },
    successMessage: {
        backgroundColor: '#ffffff',
        borderRadius: 16,
        padding: 32,
        alignItems: 'center',
        shadowColor: '#000',
        shadowOffset: {
            width: 0,
            height: 4,
        },
        shadowOpacity: 0.2,
        shadowRadius: 8,
        elevation: 6,
    },
    successText: {
        fontSize: 24,
        fontWeight: 'bold',
        color: '#4a9f3a',
        marginBottom: 8,
    },
    successSubText: {
        fontSize: 16,
        color: '#6b7280',
    },
    // Estilos para menú simple
    menuContainer: {
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
        elevation: 5,
    },
    menuTitle: {
        fontSize: 28,
        fontWeight: 'bold',
        color: '#1e293b',
        textAlign: 'center',
        marginBottom: 8,
    },
    welcomeText: {
        fontSize: 16,
        color: '#6b7280',
        textAlign: 'center',
        marginBottom: 24,
    },
    menuButton: {
        backgroundColor: '#f3f4f6',
        borderRadius: 12,
        paddingVertical: 16,
        paddingHorizontal: 20,
        marginBottom: 12,
        borderWidth: 1,
        borderColor: '#e5e7eb',
    },
    menuButtonText: {
        fontSize: 16,
        fontWeight: '600',
        color: '#374151',
        textAlign: 'center',
    },
    logoutButton: {
        backgroundColor: '#fee2e2',
        borderColor: '#fca5a5',
        marginTop: 16,
    },
    logoutText: {
        color: '#dc2626',
    },
});

export default App;
