import React, { Component } from 'react';
import {
    View,
    Text,
    TextInput,
    TouchableOpacity,
    StyleSheet,
    ScrollView,
    StatusBar,
} from 'react-native';
import Icon from 'react-native-vector-icons/Ionicons';
import Toast from 'react-native-toast-message';

class RegisterScreen extends Component {
    constructor(props) {
        super(props);
        this.state = {
            dni: '',
            nombre: '',
            apellido: '',
            email: '',
            telefono: '',
            password: '',
            confirmPassword: '',
            showPassword: false,
            showConfirmPassword: false,
        };
    }

    handleRegister = () => {
        const { dni, nombre, apellido, email, telefono, password, confirmPassword } = this.state;

        // Validaciones básicas
        if (!dni || !nombre || !apellido || !email || !password || !confirmPassword) {
            Toast.show({
                type: 'error',
                text1: 'Campos incompletos',
                text2: 'Por favor complete todos los campos obligatorios',
                position: 'top',
                visibilityTime: 3000,
            });
            return;
        }

        if (password !== confirmPassword) {
            Toast.show({
                type: 'error',
                text1: 'Error',
                text2: 'Las contraseñas no coinciden',
                position: 'top',
                visibilityTime: 3000,
            });
            return;
        }

        // Simulación de registro exitoso
        Toast.show({
            type: 'success',
            text1: 'Registro exitoso',
            text2: 'Tu cuenta ha sido creada correctamente',
            position: 'top',
            visibilityTime: 3000,
        });

        // Volver a la pantalla anterior después de 2 segundos
        setTimeout(() => {
            this.props.onBack();
        }, 2000);
    };

    render() {
        return (
            <View style={styles.container}>
                <StatusBar backgroundColor="#6ac64f" barStyle="light-content" />
                
                {/* Header */}
                <View style={styles.header}>
                    <TouchableOpacity onPress={this.props.onBack} style={styles.backButton}>
                        <Icon name="arrow-back" size={24} color="#fff" />
                    </TouchableOpacity>
                    <Text style={styles.headerTitle}>Registrarse</Text>
                    <View style={styles.placeholder} />
                </View>

                <ScrollView 
                    contentContainerStyle={styles.scrollContainer}
                    showsVerticalScrollIndicator={false}
                >
                    <Text style={styles.subtitle}>Complete sus datos para crear una cuenta</Text>

                    {/* DNI */}
                    <View style={styles.inputContainer}>
                        <Icon name="card-outline" size={20} color="#999" style={styles.inputIcon} />
                        <TextInput
                            style={styles.input}
                            value={this.state.dni}
                            onChangeText={(text) => this.setState({ dni: text })}
                            placeholder='DNI *'
                            placeholderTextColor="#999"
                            keyboardType='numeric'
                        />
                    </View>

                    {/* Nombre */}
                    <View style={styles.inputContainer}>
                        <Icon name="person-outline" size={20} color="#999" style={styles.inputIcon} />
                        <TextInput
                            style={styles.input}
                            value={this.state.nombre}
                            onChangeText={(text) => this.setState({ nombre: text })}
                            placeholder='Nombre *'
                            placeholderTextColor="#999"
                        />
                    </View>

                    {/* Apellido */}
                    <View style={styles.inputContainer}>
                        <Icon name="person-outline" size={20} color="#999" style={styles.inputIcon} />
                        <TextInput
                            style={styles.input}
                            value={this.state.apellido}
                            onChangeText={(text) => this.setState({ apellido: text })}
                            placeholder='Apellido *'
                            placeholderTextColor="#999"
                        />
                    </View>

                    {/* Email */}
                    <View style={styles.inputContainer}>
                        <Icon name="mail-outline" size={20} color="#999" style={styles.inputIcon} />
                        <TextInput
                            style={styles.input}
                            value={this.state.email}
                            onChangeText={(text) => this.setState({ email: text })}
                            placeholder='Email *'
                            placeholderTextColor="#999"
                            keyboardType='email-address'
                            autoCapitalize='none'
                        />
                    </View>

                    {/* Teléfono */}
                    <View style={styles.inputContainer}>
                        <Icon name="call-outline" size={20} color="#999" style={styles.inputIcon} />
                        <TextInput
                            style={styles.input}
                            value={this.state.telefono}
                            onChangeText={(text) => this.setState({ telefono: text })}
                            placeholder='Teléfono (opcional)'
                            placeholderTextColor="#999"
                            keyboardType='phone-pad'
                        />
                    </View>

                    {/* Contraseña */}
                    <View style={styles.inputContainer}>
                        <Icon name="lock-closed-outline" size={20} color="#999" style={styles.inputIcon} />
                        <TextInput
                            style={styles.input}
                            value={this.state.password}
                            onChangeText={(text) => this.setState({ password: text })}
                            placeholder='Contraseña *'
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

                    {/* Confirmar Contraseña */}
                    <View style={styles.inputContainer}>
                        <Icon name="lock-closed-outline" size={20} color="#999" style={styles.inputIcon} />
                        <TextInput
                            style={styles.input}
                            value={this.state.confirmPassword}
                            onChangeText={(text) => this.setState({ confirmPassword: text })}
                            placeholder='Confirmar Contraseña *'
                            placeholderTextColor="#999"
                            secureTextEntry={!this.state.showConfirmPassword}
                        />
                        <TouchableOpacity 
                            onPress={() => this.setState({ showConfirmPassword: !this.state.showConfirmPassword })}
                            style={styles.eyeIcon}
                        >
                            <Icon 
                                name={this.state.showConfirmPassword ? "eye-outline" : "eye-off-outline"} 
                                size={22} 
                                color="#999" 
                            />
                        </TouchableOpacity>
                    </View>

                    {/* Botón Registrar */}
                    <TouchableOpacity
                        style={styles.registerButton}
                        onPress={this.handleRegister}>
                        <Text style={styles.registerButtonText}>Crear Cuenta</Text>
                    </TouchableOpacity>

                    <Text style={styles.infoText}>* Campos obligatorios</Text>
                </ScrollView>

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
    header: {
        backgroundColor: '#6ac64f',
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'space-between',
        paddingHorizontal: 16,
        paddingVertical: 16,
        elevation: 4,
    },
    backButton: {
        padding: 8,
    },
    headerTitle: {
        fontSize: 20,
        fontWeight: 'bold',
        color: '#fff',
    },
    placeholder: {
        width: 40,
    },
    scrollContainer: {
        paddingHorizontal: 24,
        paddingVertical: 24,
    },
    subtitle: {
        fontSize: 16,
        color: '#666',
        marginBottom: 24,
        textAlign: 'center',
    },
    inputContainer: {
        flexDirection: 'row',
        alignItems: 'center',
        backgroundColor: '#fff',
        borderRadius: 12,
        paddingHorizontal: 16,
        paddingVertical: 4,
        marginBottom: 16,
        borderWidth: 1,
        borderColor: '#e0e0e0',
    },
    inputIcon: {
        marginRight: 12,
    },
    input: {
        flex: 1,
        fontSize: 16,
        color: '#333',
        paddingVertical: 14,
    },
    eyeIcon: {
        padding: 8,
    },
    registerButton: {
        backgroundColor: '#6ac64f',
        borderRadius: 32,
        paddingVertical: 18,
        alignItems: 'center',
        marginTop: 8,
        marginBottom: 16,
        shadowColor: '#000',
        shadowOffset: {
            width: 0,
            height: 4,
        },
        shadowOpacity: 0.15,
        shadowRadius: 12,
        elevation: 3,
    },
    registerButtonText: {
        color: '#fff',
        fontSize: 18,
        fontWeight: 'bold',
    },
    infoText: {
        fontSize: 14,
        color: '#999',
        textAlign: 'center',
        marginBottom: 24,
    },
});

export default RegisterScreen;
