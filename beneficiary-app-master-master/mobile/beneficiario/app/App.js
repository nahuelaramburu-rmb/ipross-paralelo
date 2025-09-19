import React, { Component } from 'react';
import { View, Text, TextInput, TouchableOpacity, Alert, StyleSheet, Image } from 'react-native';

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
                    <Image 
                        source={require('./images/ipross_logo_green.jpg')} 
                        style={styles.logoImage}
                        resizeMode="contain"
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
        backgroundColor: '#6ac54e',
        paddingHorizontal: 24,
        paddingVertical: 32,
        justifyContent: 'center'
    },
    logoContainer: {
        alignItems: 'center',
        marginBottom: 48
    },
    logoImage: {
        width: 200,
        height: 100
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
        elevation: 5
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
        elevation: 3
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
        color: '#4a9f3a',
        fontSize: 16,
        fontWeight: '500'
    }
});

export default App;