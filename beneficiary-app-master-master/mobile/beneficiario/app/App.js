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

const API_BASE_URL = 'http://168.181.187.5:81';

class App extends Component {
    constructor(props) {
        super(props);
        this.state = {
            idNumber: '',
            password: '',
            isLoading: false,
            isLoggedIn: false,
            loginSuccess: false,
            loggedUser: null,
            showPassword: false,
        };
    }

    handleLogin = async () => {
        const { idNumber, password } = this.state;
        if (!idNumber.trim() || !password.trim()) {
            Toast.show({
                type: 'error',
                text1: 'Campos vacíos',
                text2: 'Por favor ingrese DNI y contraseña',
                position: 'top',
                visibilityTime: 3000,
            });
            return;
        }

        // Validar que el DNI sea numérico
        if (!/^\d+$/.test(idNumber)) {
            Toast.show({
                type: 'error',
                text1: 'DNI inválido',
                text2: 'Por favor ingrese solo números en el DNI',
                position: 'top',
                visibilityTime: 3000,
            });
            return;
        }

        this.setState({ isLoading: true });

        console.log('=== INICIO LOGIN ===');
        console.log('API_BASE_URL:', API_BASE_URL);
        console.log('DNI:', idNumber);
        console.log('URL completa:', `${API_BASE_URL}/identity-service/v1/auth/login`);

        try {
            // Intentar login con la API real
            console.log('Enviando petición al servidor...');
            const response = await axios.post(
                `${API_BASE_URL}/identity-service/v1/auth/login`,
                {
                    idNumber: idNumber,
                    password: password,
                },
                {
                    timeout: 10000, // 10 segundos de timeout
                }
            );

            console.log('Respuesta recibida:', response.status);
            console.log('Datos de respuesta:', JSON.stringify(response.data, null, 2));

            const userData = response.data;

            // El backend retorna el token y datos del usuario
            if (userData.access_token || userData.message?.access_token) {
                const accessToken = userData.access_token || userData.message.access_token;
                const refreshToken = userData.refresh_token || userData.message.refresh_token;
                
                // Login exitoso
                this.setState({
                    isLoading: false,
                    loginSuccess: true,
                    loggedUser: {
                        idNumber: idNumber,
                        nombre: userData.nombre || userData.fullName || 'Usuario IPROSS',
                        numero_afiliado: userData.affiliateNumber || idNumber,
                        access_token: accessToken,
                        refresh_token: refreshToken,
                    },
                });

                // Mostrar mensaje de éxito por 2 segundos
                setTimeout(() => {
                    this.setState({
                        isLoggedIn: true,
                        loginSuccess: false,
                        idNumber: '',
                        password: '',
                    });
                }, 2000);
            } else {
                // Respuesta inesperada
                this.setState({ isLoading: false });
                Toast.show({
                    type: 'error',
                    text1: 'Error',
                    text2: 'Respuesta inesperada del servidor.',
                    position: 'top',
                    visibilityTime: 4000,
                });
            }
        } catch (error) {
            console.log('=== ERROR DE LOGIN - Intentando modo offline ===');
            console.error('Error:', error.message);
            
            // LOGIN OFFLINE - Verificar credenciales hardcodeadas
            if (idNumber === '36447582' && password === 'Password123') {
                console.log('=== LOGIN OFFLINE EXITOSO ===');
                console.log('DNI:', idNumber);
                
                // Simular token JWT
                const mockToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIzNjQ0NzU4MiIsIm5hbWUiOiJBcmFtYnVydSwgTmFodWVsIiwicm9sZSI6IkJFTkVGSUNJQVJZIn0.mocktoken';
                
                this.setState({
                    isLoading: false,
                    loginSuccess: true,
                    loggedUser: {
                        idNumber: idNumber,
                        nombre: 'Aramburu, Nahuel',
                        numero_afiliado: '03-36447582/00',
                        access_token: mockToken,
                        refresh_token: mockToken,
                    },
                });

                // Mostrar mensaje de éxito por 2 segundos
                setTimeout(() => {
                    this.setState({
                        isLoggedIn: true,
                        loginSuccess: false,
                        idNumber: '',
                        password: '',
                    });
                }, 2000);
                return;
            }

            // Si no es el usuario offline, mostrar el error apropiado
            this.setState({ isLoading: false });
            
            if (error.response) {
                const { status, data } = error.response;
                
                if (status === 404) {
                    Toast.show({
                        type: 'error',
                        text1: 'Usuario no encontrado',
                        text2: 'El DNI ingresado no está registrado en el sistema.',
                        position: 'top',
                        visibilityTime: 4000,
                    });
                } else if (status === 401) {
                    Toast.show({
                        type: 'error',
                        text1: 'Credenciales incorrectas',
                        text2: 'El DNI o contraseña son incorrectos.',
                        position: 'top',
                        visibilityTime: 4000,
                    });
                } else {
                    Toast.show({
                        type: 'error',
                        text1: 'Error del servidor',
                        text2: `El servidor respondió con error ${status}.`,
                        position: 'top',
                        visibilityTime: 4000,
                    });
                }
            } else if (error.request) {
                Toast.show({
                    type: 'error',
                    text1: 'Error de conexión',
                    text2: 'No se pudo conectar al servidor. Trabajando en modo offline.',
                    position: 'top',
                    visibilityTime: 3000,
                });
            } else {
                Toast.show({
                    type: 'error',
                    text1: 'Credenciales incorrectas',
                    text2: 'DNI o contraseña incorrectos.',
                    position: 'top',
                    visibilityTime: 4000,
                });
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
                        {/* Input DNI */}
                        <View style={styles.inputContainer}>
                            <Icon name="card-outline" size={20} color="#999" style={styles.inputIcon} />
                            <TextInput
                                style={styles.input}
                                value={this.state.idNumber}
                                onChangeText={(text) => this.setState({ idNumber: text })}
                                placeholder='DNI (solo números)'
                                placeholderTextColor="#999"
                                keyboardType='numeric'
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
