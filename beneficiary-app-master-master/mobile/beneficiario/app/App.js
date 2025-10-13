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
    StatusBar,
} from 'react-native';
import axios from 'axios';
import HomeScreen from './screens/HomeScreen';

const API_BASE_URL = 'http://168.181.187.5:81';

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
            showConstructionBanner: true,
        };
    }

    handleCloseConstructionBanner = () => {
        this.setState({ showConstructionBanner: false });
    };

    handleLogin = async () => {
        const { username, password } = this.state;
        if (!username.trim() || !password.trim()) {
            Alert.alert('Error', 'Por favor ingrese email y contraseña');
            return;
        }

        this.setState({ isLoading: true });

        try {
            // Llamada al nuevo endpoint de login con email
            const response = await axios.post(
                `${API_BASE_URL}/identity-service/v1/auth/login`,
                {
                    email: username,
                    password: password,
                }
            );

            const userData = response.data;

            // El backend retorna { message: { access_token, refresh_token } }
            if (userData.message && userData.message.access_token) {
                // Login exitoso
                this.setState({
                    isLoading: false,
                    loginSuccess: true,
                    loggedUser: {
                        email: username,
                        access_token: userData.message.access_token,
                        refresh_token: userData.message.refresh_token,
                    },
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
                Alert.alert('Error', 'Credenciales incorrectas. Verifique su email y contraseña.');
            }
        } catch (error) {
            this.setState({ isLoading: false });
            console.error('Error de login:', error);

            if (error.response && error.response.status === 404) {
                Alert.alert('Error', 'Usuario no encontrado. Verifique sus credenciales.');
            } else if (error.response && error.response.status === 401) {
                Alert.alert('Error', 'Email o contraseña incorrectos.');
            } else {
                Alert.alert('Error', 'Error en la conexión. Intente nuevamente.');
            }
        }
    };

    render() {
        const { isLoggedIn, loginSuccess } = this.state;

        if (isLoggedIn) {
            // Pantalla principal con credencial virtual y sidebar
            return <HomeScreen loggedUser={this.state.loggedUser} onLogout={() => this.setState({ isLoggedIn: false })} />;
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
                <StatusBar 
                    backgroundColor="#a6ce39" 
                    barStyle="dark-content" 
                    translucent={false}
                />
                
                {/* Banner de Construcción - Arriba de todo */}
                {this.state.showConstructionBanner && (
                    <View style={styles.constructionBanner}>
                        <Text style={styles.constructionIcon}>🚧</Text>
                        <View style={styles.constructionTextContainer}>
                            <Text style={styles.constructionTitle}>App en Construcción</Text>
                            <Text style={styles.constructionText}>
                                La app no está operativa. Estamos trabajando para mejorar tu atención
                            </Text>
                            <Text style={styles.constructionSubtext}>Estate atento a las novedades</Text>
                            <TouchableOpacity 
                                style={styles.acceptButton}
                                onPress={this.handleCloseConstructionBanner}
                            >
                                <Text style={styles.acceptButtonText}>Aceptar</Text>
                            </TouchableOpacity>
                        </View>
                        <Text style={styles.constructionIcon}>🚧</Text>
                    </View>
                )}

                <View style={styles.contentContainer}>
                    <View style={styles.logoContainer}>
                        <Image
                            source={require('./images/ipross_logo_white.png')}
                            style={styles.logoImage}
                            resizeMode='contain'
                        />
                    </View>

                    <View style={styles.formContainer}>
                    <Text style={styles.welcomeTitle}>Bienvenido</Text>

                    <View style={styles.inputWrapper}>
                        <Text style={styles.inputLabel}>Email</Text>
                        <TextInput
                            style={styles.input}
                            value={this.state.username}
                            onChangeText={(text) => this.setState({ username: text })}
                            placeholder='Ingrese su email'
                            keyboardType='email-address'
                            autoCapitalize='none'
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
            </View>
        );
    }
}

const styles = StyleSheet.create({
    // Container principal con fondo verde IPROSS (nuevo diseño)
    container: {
        flex: 1,
        backgroundColor: '#a6ce39',
    },
    
    // Contenedor que centra verticalmente
    contentContainer: {
        flex: 1,
        paddingHorizontal: 24,
        paddingVertical: 20,
        justifyContent: 'center',
    },
    
    logoContainer: {
        alignItems: 'center',
        marginBottom: 32,
    },
    logoImage: {
        width: 280,
        height: 100,
    },
    
    // Formulario con diseño IPROSS
    formContainer: {
        backgroundColor: '#FFFFFF',
        borderRadius: 20,
        padding: 28,
        shadowColor: '#000000',
        shadowOffset: {
            width: 0,
            height: 4,
        },
        shadowOpacity: 0.1,
        shadowRadius: 12,
        elevation: 8,
        borderWidth: 1,
        borderColor: '#e8e8e8',
    },
    welcomeTitle: {
        fontSize: 32,
        fontWeight: '900',
        color: '#000000',
        textAlign: 'center',
        marginBottom: 8,
    },
    
    // Inputs con diseño IPROSS
    inputWrapper: {
        marginBottom: 20,
    },
    inputLabel: {
        fontSize: 14,
        fontWeight: '600',
        color: '#000000',
        marginBottom: 8,
    },
    input: {
        borderWidth: 2,
        borderColor: '#e8e8e8',
        borderRadius: 12,
        paddingHorizontal: 16,
        paddingVertical: 14,
        fontSize: 16,
        backgroundColor: '#FFFFFF',
        color: '#000000',
    },
    
    // Botón principal verde IPROSS (nuevo diseño)
    loginButton: {
        backgroundColor: '#a6ce39',
        borderRadius: 12,
        paddingVertical: 16,
        alignItems: 'center',
        marginVertical: 24,
        shadowColor: '#a6ce39',
        shadowOffset: {
            width: 0,
            height: 4,
        },
        shadowOpacity: 0.3,
        shadowRadius: 8,
        elevation: 6,
    },
    loginButtonText: {
        color: '#FFFFFF',
        fontSize: 18,
        fontWeight: 'bold',
    },
    forgotButton: {
        alignItems: 'center',
    },
    forgotText: {
        color: '#007be0',
        fontSize: 16,
        fontWeight: '600',
    },
    
    // Estados del botón
    loginButtonDisabled: {
        backgroundColor: '#999999',
        shadowOpacity: 0.1,
    },
    loadingContainer: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: 12,
    },
    
    // Mensaje de éxito con colores IPROSS (nuevo diseño)
    successContainer: {
        justifyContent: 'center',
        alignItems: 'center',
        backgroundColor: '#a6ce39',
    },
    successMessage: {
        backgroundColor: '#FFFFFF',
        borderRadius: 20,
        padding: 40,
        alignItems: 'center',
        shadowColor: '#000000',
        shadowOffset: {
            width: 0,
            height: 6,
        },
        shadowOpacity: 0.25,
        shadowRadius: 12,
        elevation: 10,
        borderWidth: 3,
        borderColor: '#a6ce39',
    },
    successText: {
        fontSize: 24,
        fontWeight: 'bold',
        color: '#a6ce39',
        marginBottom: 12,
    },
    successSubText: {
        fontSize: 16,
        color: '#666666',
    },
    
    // Banner de construcción rediseñado con identidad IPROSS
    constructionBanner: {
        backgroundColor: '#f39c12',
        borderWidth: 3,
        borderColor: '#e67e22',
        borderRadius: 16,
        marginHorizontal: 20,
        marginTop: 20,
        marginBottom: 12,
        paddingVertical: 20,
        paddingHorizontal: 16,
        shadowColor: '#000000',
        shadowOffset: {
            width: 0,
            height: 4,
        },
        shadowOpacity: 0.2,
        shadowRadius: 8,
        elevation: 999,
        zIndex: 999,
        position: 'relative',
    },
    constructionTextContainer: {
        alignItems: 'center',
        marginVertical: 8,
    },
    constructionIcon: {
        fontSize: 32,
        textAlign: 'center',
        marginBottom: 8,
    },
    constructionTitle: {
        fontSize: 22,
        fontWeight: 'bold',
        color: '#000000',
        textAlign: 'center',
        marginBottom: 8,
    },
    constructionText: {
        fontSize: 15,
        color: '#000000',
        textAlign: 'center',
        fontWeight: '500',
        marginBottom: 6,
        lineHeight: 22,
    },
    constructionSubtext: {
        fontSize: 13,
        color: '#666666',
        textAlign: 'center',
        fontStyle: 'italic',
        marginBottom: 16,
    },
    acceptButton: {
        backgroundColor: '#a6ce39',
        borderRadius: 10,
        paddingVertical: 12,
        paddingHorizontal: 32,
        alignSelf: 'center',
        shadowColor: '#a6ce39',
        shadowOffset: {
            width: 0,
            height: 3,
        },
        shadowOpacity: 0.3,
        shadowRadius: 6,
        elevation: 5,
    },
    acceptButtonText: {
        color: '#FFFFFF',
        fontSize: 16,
        fontWeight: 'bold',
        textAlign: 'center',
    },
});

export default App;
