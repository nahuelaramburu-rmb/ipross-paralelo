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
import Toast from 'react-native-toast-message';
import HomeScreen from './screens/HomeScreen';
import ApiService from './services/api.service';

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
        try {
            // Intentar login usando ApiService (usa API_HOST desde configs o fallback)
            const result = await ApiService.login(idNumber, password);

            if (result.success) {
                // Caso fallback/local
                if (result.fallback || result.userData) {
                    const userData = result.userData || result.user || {};
                    const mockToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIzNjQ0NzU4MiIsIm5hbWUiOiJFeGFtcGxlIFVzZXIiLCJyb2xlIjoiQkVORUZJQ0lBUll9.mock';

                    this.setState({
                        isLoading: false,
                        loginSuccess: true,
                        loggedUser: {
                            idNumber: String(userData.idNumber || idNumber),
                            nombre: userData.name || userData.nombre || 'Usuario IPROSS',
                            numero_afiliado: userData.beneficiaryCode || idNumber,
                            access_token: mockToken,
                            refresh_token: mockToken,
                        },
                    });

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

                // Login real con tokens
                const tokens = result.tokens || result.message || {};
                const accessToken = tokens.access_token || tokens.accessToken || null;
                const refreshToken = tokens.refresh_token || tokens.refreshToken || null;

                // Guardar tokens en el servicio
                ApiService.accessToken = accessToken;
                ApiService.refreshToken = refreshToken;

                // Intentar obtener datos del beneficiario
                const beneficiaryRes = await ApiService.getBeneficiaryData();
                const beneficiaryData = (beneficiaryRes && beneficiaryRes.data) ? beneficiaryRes.data : {};

                this.setState({
                    isLoading: false,
                    loginSuccess: true,
                    loggedUser: {
                        idNumber: idNumber,
                        nombre: beneficiaryData.name || beneficiaryData.nombre || 'Usuario IPROSS',
                        numero_afiliado: beneficiaryData.beneficiaryCode || idNumber,
                        access_token: accessToken,
                        refresh_token: refreshToken,
                    },
                });

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

            // Si llegamos aquí, resultado.success === false
            this.setState({ isLoading: false });
            Toast.show({
                type: 'error',
                text1: 'Error de autenticación',
                text2: result.error || 'Credenciales incorrectas',
                position: 'top',
                visibilityTime: 4000,
            });
        } catch (error) {
            console.error('Login unexpected error:', error);
            this.setState({ isLoading: false });
            Toast.show({
                type: 'error',
                text1: 'Error',
                text2: 'Ocurrió un error al intentar iniciar sesión. Intentá nuevamente.',
                position: 'top',
                visibilityTime: 4000,
            });
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
