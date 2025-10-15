import React, { Component } from 'react';
import {
    View,
    Text,
    TextInput,
    TouchableOpacity,
    StyleSheet,
    Image,
    ActivityIndicator,
    StatusBar,
    ScrollView,
} from 'react-native';
import Icon from 'react-native-vector-icons/Ionicons';
import axios from 'axios';
import Toast from 'react-native-toast-message';
import HomeScreen from './screens/HomeScreen';

const API_BASE_URL = 'https://168.181.187.5:882';

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
            showPassword: false,
        };
    }

    handleLogin = async () => {
        const { username, password } = this.state;
        if (!username.trim() || !password.trim()) {
            Toast.show({
                type: 'error',
                text1: 'Campos vacíos',
                text2: 'Por favor ingrese email y contraseña',
                position: 'top',
                visibilityTime: 3000,
            });
            return;
        }

        // Validar formato de email básico
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(username)) {
            Toast.show({
                type: 'error',
                text1: 'Email inválido',
                text2: 'Por favor ingrese un email válido (ejemplo: usuario@dominio.com)',
                position: 'top',
                visibilityTime: 3000,
            });
            return;
        }

        this.setState({ isLoading: true });

        // LOGIN OFFLINE - Verificar credenciales hardcodeadas
        if (username === 'miguel@gmail.com' && password === 'Password123') {
            console.log('=== LOGIN OFFLINE EXITOSO ===');
            console.log('Email:', username);
            
            // Simular token JWT
            const mockToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJtaWd1ZWxAZ21haWwuY29tIiwibmFtZSI6Ik1pZ3VlbCIsInJvbGUiOiJCRU5FRklDSUFSWSJ9.mocktoken';
            
            this.setState({
                isLoading: false,
                loginSuccess: true,
                loggedUser: {
                    email: username,
                    nombre: 'Aramburu, Nahuel',
                    numero_afiliado: '03-13642194/00',
                    access_token: mockToken,
                    refresh_token: mockToken,
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
            return;
        }

        // Si las credenciales no coinciden, mostrar error
        this.setState({ isLoading: false });
        Toast.show({
            type: 'error',
            text1: 'Credenciales incorrectas',
            text2: 'El email o contraseña son incorrectos.',
            position: 'top',
            visibilityTime: 4000,
        });

        /* CÓDIGO ONLINE COMENTADO TEMPORALMENTE
        console.log('=== INICIO LOGIN ===');
        console.log('API_BASE_URL:', API_BASE_URL);
        console.log('Email:', username);
        console.log('URL completa:', `${API_BASE_URL}/identity-service/v1/auth/login`);

        try {
            // Llamada al nuevo endpoint de login con email
            console.log('Enviando petición al servidor...');
            const response = await axios.post(
                `${API_BASE_URL}/identity-service/v1/auth/login`,
                {
                    email: username,
                    password: password,
                },
                {
                    timeout: 10000, // 10 segundos de timeout
                }
            );

            console.log('Respuesta recibida:', response.status);
            console.log('Datos de respuesta:', JSON.stringify(response.data, null, 2));

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
                Toast.show({
                    type: 'error',
                    text1: 'Error',
                    text2: 'Credenciales incorrectas. Verifique su email y contraseña.',
                    position: 'top',
                    visibilityTime: 4000,
                });
            }
        } catch (error) {
            this.setState({ isLoading: false });
            console.log('=== ERROR DE LOGIN ===');
            console.error('Error completo:', error);
            console.log('Error.message:', error.message);
            console.log('Error.code:', error.code);
            console.log('Error.response:', error.response ? 'SÍ' : 'NO');
            console.log('Error.request:', error.request ? 'SÍ' : 'NO');
            
            if (error.response) {
                console.log('Response status:', error.response.status);
                console.log('Response data:', JSON.stringify(error.response.data, null, 2));
            }

            // Manejar diferentes tipos de errores
            if (error.response) {
                // El servidor respondió con un código de error
                const { status, data } = error.response;
                
                if (status === 404) {
                    // Usuario no encontrado
                    const errorMsg = data?.message || data?.code || 'Usuario no encontrado';
                    Toast.show({
                        type: 'error',
                        text1: 'Usuario no encontrado',
                        text2: `${errorMsg}\n\nVerifique que el email esté registrado en el sistema.`,
                        position: 'top',
                        visibilityTime: 4000,
                    });
                } else if (status === 401) {
                    Toast.show({
                        type: 'error',
                        text1: 'Credenciales incorrectas',
                        text2: 'El email o contraseña son incorrectos. Por favor, verifique sus datos.',
                        position: 'top',
                        visibilityTime: 4000,
                    });
                } else if (status === 400) {
                    Toast.show({
                        type: 'error',
                        text1: 'Error en la solicitud',
                        text2: 'Los datos ingresados no son válidos. Verifique el formato del email.',
                        position: 'top',
                        visibilityTime: 4000,
                    });
                } else {
                    Toast.show({
                        type: 'error',
                        text1: 'Error del servidor',
                        text2: `El servidor respondió con error ${status}. Intente nuevamente más tarde.`,
                        position: 'top',
                        visibilityTime: 4000,
                    });
                }
            } else if (error.request) {
                // La petición se hizo pero no hubo respuesta (Network Error)
                Toast.show({
                    type: 'error',
                    text1: 'Error de conexión',
                    text2: `No se pudo conectar al servidor.\n\nVerifique:\n• Su conexión a internet\n• Que esté en la misma red que el servidor\n• Que el servidor esté disponible en ${API_BASE_URL}`,
                    position: 'top',
                    visibilityTime: 5000,
                });
            } else {
                // Error al configurar la petición
                Toast.show({
                    type: 'error',
                    text1: 'Error inesperado',
                    text2: `Ocurrió un error: ${error.message}`,
                    position: 'top',
                    visibilityTime: 4000,
                });
            }
        }
        */
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
                    backgroundColor="#f5f5f5" 
                    barStyle="dark-content" 
                    translucent={false}
                />
                
                <ScrollView 
                    contentContainerStyle={styles.scrollContainer}
                    showsVerticalScrollIndicator={false}
                >
                    {/* Logo IPROSS con degradado */}
                    <View style={styles.logoContainer}>
                        <Image
                            source={require('./images/ipross_logo.png')}
                            style={styles.logoImage}
                            resizeMode='contain'
                        />
                    </View>

                    {/* Formulario */}
                    <View style={styles.formContainer}>
                        {/* Input Usuario */}
                        <View style={styles.inputContainer}>
                            <Icon name="person-outline" size={20} color="#999" style={styles.inputIcon} />
                            <TextInput
                                style={styles.input}
                                value={this.state.username}
                                onChangeText={(text) => this.setState({ username: text })}
                                placeholder='Usuario'
                                placeholderTextColor="#999"
                                keyboardType='email-address'
                                autoCapitalize='none'
                            />
                        </View>

                        {/* Input Contraseña */}
                        <View style={styles.inputContainer}>
                            <Icon name="lock-closed-outline" size={20} color="#999" style={styles.inputIcon} />
                            <TextInput
                                style={styles.input}
                                value={this.state.password}
                                onChangeText={(text) => this.setState({ password: text })}
                                placeholder='Contraseña'
                                placeholderTextColor="#999"
                                secureTextEntry={!this.state.showPassword}
                            />
                            <TouchableOpacity 
                                onPress={() => this.setState({ showPassword: !this.state.showPassword })}
                                style={styles.eyeIcon}
                            >
                                <Icon 
                                    name={this.state.showPassword ? "eye-outline" : "eye-off-outline"} 
                                    size={22} 
                                    color="#999" 
                                />
                            </TouchableOpacity>
                        </View>

                        {/* Link Olvidó contraseña */}
                        <TouchableOpacity style={styles.forgotButton}>
                            <Text style={styles.forgotText}>¿Olvidó su contraseña?</Text>
                        </TouchableOpacity>

                        {/* Botón Ingresar */}
                        <TouchableOpacity
                            style={[styles.loginButton, this.state.isLoading && styles.loginButtonDisabled]}
                            onPress={this.handleLogin}
                            disabled={this.state.isLoading}>
                            {this.state.isLoading ? (
                                <ActivityIndicator color='#000' />
                            ) : (
                                <Text style={styles.loginButtonText}>Ingresar</Text>
                            )}
                        </TouchableOpacity>

                        {/* Botón Registrarse */}
                        <TouchableOpacity
                            style={styles.registerButton}
                            onPress={() => Toast.show({
                                type: 'info',
                                text1: 'Registro',
                                text2: 'Funcionalidad en desarrollo',
                                position: 'top',
                                visibilityTime: 3000,
                            })}>
                            <Text style={styles.registerButtonText}>Registrarse</Text>
                        </TouchableOpacity>
                    </View>
                </ScrollView>
                
                {/* Toast Component */}
                <Toast />
            </View>
        );
    }
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: '#f5f5f5',
    },
    scrollContainer: {
        flexGrow: 1,
        justifyContent: 'center',
        paddingHorizontal: 24,
        paddingVertical: 40,
    },
    
    // Logo IPROSS
    logoContainer: {
        alignItems: 'center',
        marginBottom: 60,
        marginTop: 20,
    },
    logoImage: {
        width: 280,
        height: 120,
    },
    
    // Formulario
    formContainer: {
        width: '100%',
        maxWidth: 400,
        alignSelf: 'center',
    },
    
    // Inputs con iconos
    inputContainer: {
        flexDirection: 'row',
        alignItems: 'center',
        backgroundColor: '#FFFFFF',
        borderRadius: 16,
        borderWidth: 1.5,
        borderColor: '#e0e0e0',
        paddingHorizontal: 16,
        paddingVertical: 4,
        marginBottom: 16,
    },
    inputIcon: {
        marginRight: 12,
    },
    input: {
        flex: 1,
        fontSize: 16,
        color: '#000',
        paddingVertical: 14,
    },
    eyeIcon: {
        padding: 8,
    },
    
    // Link olvidó contraseña
    forgotButton: {
        alignSelf: 'flex-end',
        marginBottom: 32,
        marginTop: -4,
    },
    forgotText: {
        color: '#b8d154',
        fontSize: 15,
        fontWeight: '500',
    },
    
    // Botón Ingresar (verde lima)
    loginButton: {
        backgroundColor: '#b8d154',
        borderRadius: 32,
        paddingVertical: 18,
        alignItems: 'center',
        marginBottom: 16,
        shadowColor: '#000',
        shadowOffset: {
            width: 0,
            height: 2,
        },
        shadowOpacity: 0.1,
        shadowRadius: 4,
        elevation: 3,
    },
    loginButtonText: {
        color: '#000',
        fontSize: 18,
        fontWeight: 'bold',
    },
    loginButtonDisabled: {
        backgroundColor: '#d4e49f',
    },
    
    // Botón Registrarse (blanco con borde verde)
    registerButton: {
        backgroundColor: '#FFFFFF',
        borderRadius: 32,
        paddingVertical: 18,
        alignItems: 'center',
        borderWidth: 2,
        borderColor: '#b8d154',
    },
    registerButtonText: {
        color: '#000',
        fontSize: 18,
        fontWeight: 'bold',
    },
    
    // Mensaje de éxito
    successContainer: {
        justifyContent: 'center',
        alignItems: 'center',
        backgroundColor: '#f5f5f5',
    },
    successMessage: {
        backgroundColor: '#FFFFFF',
        borderRadius: 20,
        padding: 40,
        alignItems: 'center',
        shadowColor: '#000',
        shadowOffset: {
            width: 0,
            height: 4,
        },
        shadowOpacity: 0.15,
        shadowRadius: 12,
        elevation: 8,
        borderWidth: 2,
        borderColor: '#b8d154',
    },
    successText: {
        fontSize: 24,
        fontWeight: 'bold',
        color: '#b8d154',
        marginBottom: 12,
    },
    successSubText: {
        fontSize: 16,
        color: '#666',
    },
});

export default App;
