import React, { Component } from 'react';
import { View, Text, TextInput, TouchableOpacity, Alert, StyleSheet } from 'react-native';

class App extends Component {
    constructor(props) {
        super(props);
        this.state = {
            username: '',
            password: ''
        };
    }

    handleLogin = () => {
        const { username, password } = this.state;
        if (!username.trim() || !password.trim()) {
            Alert.alert('Error', 'Por favor ingrese usuario y contraseña');
            return;
        }
        Alert.alert('Éxito', 'Bienvenido ' + username + ' a IPROSS!');
        this.setState({ username: '', password: '' });
    };

    render() {
        return (
            <View style={styles.container}>
                <View style={styles.logoContainer}>
                    <View style={styles.logoCircle}>
                        <Text style={styles.logoText}>I</Text>
                    </View>
                    <Text style={styles.appTitle}>IPROSS</Text>
                    <Text style={styles.subtitle}>Instituto Provincial de Obra Social</Text>
                </View>

                <View style={styles.formContainer}>
                    <Text style={styles.welcomeTitle}>Bienvenido</Text>
                    
                    <View style={styles.inputWrapper}>
                        <Text style={styles.inputLabel}>Número de Beneficiario</Text>
                        <TextInput
                            style={styles.input}
                            value={this.state.username}
                            onChangeText={(text) => this.setState({ username: text })}
                            placeholder="Ingrese su número de beneficiario"
                            keyboardType="numeric"
                        />
                    </View>

                    <View style={styles.inputWrapper}>
                        <Text style={styles.inputLabel}>Contraseña</Text>
                        <TextInput
                            style={styles.input}
                            value={this.state.password}
                            onChangeText={(text) => this.setState({ password: text })}
                            placeholder="Ingrese su contraseña"
                            secureTextEntry={true}
                        />
                    </View>

                    <TouchableOpacity style={styles.loginButton} onPress={this.handleLogin}>
                        <Text style={styles.loginButtonText}>Iniciar Sesión</Text>
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
        backgroundColor: '#f8fafc',
        paddingHorizontal: 24,
        paddingVertical: 32,
        justifyContent: 'center'
    },
    logoContainer: {
        alignItems: 'center',
        marginBottom: 48
    },
    logoCircle: {
        width: 80,
        height: 80,
        borderRadius: 40,
        backgroundColor: '#2563eb',
        justifyContent: 'center',
        alignItems: 'center',
        marginBottom: 16
    },
    logoText: {
        fontSize: 36,
        fontWeight: 'bold',
        color: '#ffffff'
    },
    appTitle: {
        fontSize: 32,
        fontWeight: 'bold',
        color: '#1e293b',
        marginBottom: 4
    },
    subtitle: {
        fontSize: 16,
        color: '#64748b',
        textAlign: 'center'
    },
    formContainer: {
        backgroundColor: '#ffffff',
        borderRadius: 16,
        padding: 24
    },
    welcomeTitle: {
        fontSize: 28,
        fontWeight: 'bold',
        color: '#1e293b',
        textAlign: 'center',
        marginBottom: 32
    },
    inputWrapper: {
        marginBottom: 20
    },
    inputLabel: {
        fontSize: 14,
        fontWeight: '600',
        color: '#374151',
        marginBottom: 8
    },
    input: {
        borderWidth: 1,
        borderColor: '#d1d5db',
        borderRadius: 12,
        paddingHorizontal: 16,
        paddingVertical: 14,
        fontSize: 16,
        backgroundColor: '#f9fafb'
    },
    loginButton: {
        backgroundColor: '#2563eb',
        borderRadius: 12,
        paddingVertical: 16,
        alignItems: 'center',
        marginVertical: 24
    },
    loginButtonText: {
        color: '#ffffff',
        fontSize: 18,
        fontWeight: 'bold'
    },
    forgotButton: {
        alignItems: 'center'
    },
    forgotText: {
        color: '#2563eb',
        fontSize: 16
    }
});

export default App;