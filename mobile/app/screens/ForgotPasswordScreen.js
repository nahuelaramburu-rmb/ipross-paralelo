import React, { Component } from 'react';
import {
    View,
    Text,
    TextInput,
    TouchableOpacity,
    StyleSheet,
    ScrollView,
    StatusBar,
    ActivityIndicator,
} from 'react-native';
import Icon from 'react-native-vector-icons/Ionicons';
import Toast from 'react-native-toast-message';

class ForgotPasswordScreen extends Component {
    constructor(props) {
        super(props);
        this.state = {
            dni: '',
            email: '',
            isLoading: false,
            step: 1, // 1: solicitar datos, 2: código enviado
            code: '',
            newPassword: '',
            confirmPassword: '',
            showPassword: false,
            showConfirmPassword: false,
        };
    }

    handleSendCode = () => {
        const { dni, email } = this.state;

        if (!dni || !email) {
            Toast.show({
                type: 'error',
                text1: 'Campos incompletos',
                text2: 'Por favor complete DNI y email',
                position: 'top',
                visibilityTime: 3000,
            });
            return;
        }

        this.setState({ isLoading: true });

        // Simular envío de código
        setTimeout(() => {
            this.setState({ 
                isLoading: false,
                step: 2 
            });
            Toast.show({
                type: 'success',
                text1: 'Código enviado',
                text2: 'Revise su email para obtener el código',
                position: 'top',
                visibilityTime: 4000,
            });
        }, 1500);
    };

    handleResetPassword = () => {
        const { code, newPassword, confirmPassword } = this.state;

        if (!code || !newPassword || !confirmPassword) {
            Toast.show({
                type: 'error',
                text1: 'Campos incompletos',
                text2: 'Por favor complete todos los campos',
                position: 'top',
                visibilityTime: 3000,
            });
            return;
        }

        if (newPassword !== confirmPassword) {
            Toast.show({
                type: 'error',
                text1: 'Error',
                text2: 'Las contraseñas no coinciden',
                position: 'top',
                visibilityTime: 3000,
            });
            return;
        }

        this.setState({ isLoading: true });

        // Simular cambio de contraseña
        setTimeout(() => {
            this.setState({ isLoading: false });
            Toast.show({
                type: 'success',
                text1: 'Contraseña actualizada',
                text2: 'Su contraseña ha sido cambiada exitosamente',
                position: 'top',
                visibilityTime: 3000,
            });

            setTimeout(() => {
                this.props.onBack();
            }, 2000);
        }, 1500);
    };

    renderStep1() {
        return (
            <>
                <Text style={styles.subtitle}>
                    Ingrese su DNI y email registrado para recibir un código de recuperación
                </Text>

                {/* DNI */}
                <View style={styles.inputContainer}>
                    <Icon name="card-outline" size={20} color="#999" style={styles.inputIcon} />
                    <TextInput
                        style={styles.input}
                        value={this.state.dni}
                        onChangeText={(text) => this.setState({ dni: text })}
                        placeholder='DNI'
                        placeholderTextColor="#999"
                        keyboardType='numeric'
                    />
                </View>

                {/* Email */}
                <View style={styles.inputContainer}>
                    <Icon name="mail-outline" size={20} color="#999" style={styles.inputIcon} />
                    <TextInput
                        style={styles.input}
                        value={this.state.email}
                        onChangeText={(text) => this.setState({ email: text })}
                        placeholder='Email'
                        placeholderTextColor="#999"
                        keyboardType='email-address'
                        autoCapitalize='none'
                    />
                </View>

                {/* Botón Enviar Código */}
                <TouchableOpacity
                    style={[styles.button, this.state.isLoading && styles.buttonDisabled]}
                    onPress={this.handleSendCode}
                    disabled={this.state.isLoading}>
                    {this.state.isLoading ? (
                        <ActivityIndicator color='#fff' />
                    ) : (
                        <Text style={styles.buttonText}>Enviar Código</Text>
                    )}
                </TouchableOpacity>
            </>
        );
    }

    renderStep2() {
        return (
            <>
                <Text style={styles.subtitle}>
                    Ingrese el código recibido por email y su nueva contraseña
                </Text>

                {/* Código */}
                <View style={styles.inputContainer}>
                    <Icon name="key-outline" size={20} color="#999" style={styles.inputIcon} />
                    <TextInput
                        style={styles.input}
                        value={this.state.code}
                        onChangeText={(text) => this.setState({ code: text })}
                        placeholder='Código de verificación'
                        placeholderTextColor="#999"
                        keyboardType='numeric'
                    />
                </View>

                {/* Nueva Contraseña */}
                <View style={styles.inputContainer}>
                    <Icon name="lock-closed-outline" size={20} color="#999" style={styles.inputIcon} />
                    <TextInput
                        style={styles.input}
                        value={this.state.newPassword}
                        onChangeText={(text) => this.setState({ newPassword: text })}
                        placeholder='Nueva Contraseña'
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
                        placeholder='Confirmar Contraseña'
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

                {/* Botón Cambiar Contraseña */}
                <TouchableOpacity
                    style={[styles.button, this.state.isLoading && styles.buttonDisabled]}
                    onPress={this.handleResetPassword}
                    disabled={this.state.isLoading}>
                    {this.state.isLoading ? (
                        <ActivityIndicator color='#fff' />
                    ) : (
                        <Text style={styles.buttonText}>Cambiar Contraseña</Text>
                    )}
                </TouchableOpacity>

                {/* Botón Reenviar código */}
                <TouchableOpacity
                    style={styles.resendButton}
                    onPress={this.handleSendCode}>
                    <Text style={styles.resendText}>Reenviar código</Text>
                </TouchableOpacity>
            </>
        );
    }

    render() {
        return (
            <View style={styles.container}>
                <StatusBar backgroundColor="#6ac64f" barStyle="light-content" />
                
                {/* Header */}
                <View style={styles.header}>
                    <TouchableOpacity onPress={this.props.onBack} style={styles.backButton}>
                        <Icon name="arrow-back" size={24} color="#fff" />
                    </TouchableOpacity>
                    <Text style={styles.headerTitle}>Recuperar Contraseña</Text>
                    <View style={styles.placeholder} />
                </View>

                <ScrollView 
                    contentContainerStyle={styles.scrollContainer}
                    showsVerticalScrollIndicator={false}
                >
                    {this.state.step === 1 ? this.renderStep1() : this.renderStep2()}
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
        paddingVertical: 32,
    },
    subtitle: {
        fontSize: 16,
        color: '#666',
        marginBottom: 32,
        textAlign: 'center',
        lineHeight: 24,
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
    button: {
        backgroundColor: '#6ac64f',
        borderRadius: 32,
        paddingVertical: 18,
        alignItems: 'center',
        marginTop: 16,
        shadowColor: '#000',
        shadowOffset: {
            width: 0,
            height: 4,
        },
        shadowOpacity: 0.15,
        shadowRadius: 12,
        elevation: 3,
    },
    buttonDisabled: {
        backgroundColor: '#a8d89a',
    },
    buttonText: {
        color: '#fff',
        fontSize: 18,
        fontWeight: 'bold',
    },
    resendButton: {
        marginTop: 16,
        alignItems: 'center',
        padding: 12,
    },
    resendText: {
        color: '#6ac64f',
        fontSize: 16,
        fontWeight: '600',
    },
});

export default ForgotPasswordScreen;
