import React from 'react';
import { View, StyleSheet } from 'react-native';
import { Button, Card, Title, Paragraph, FAB, Chip } from 'react-native-paper';
import * as Colors from '../constants/Colors';

// Ejemplo de componente usando React Native Paper
const PaperExampleScreen = () => {
    return (
        <View style={styles.container}>
            <Card style={styles.card}>
                <Card.Content>
                    <Title>React Native Paper</Title>
                    <Paragraph>
                        Componentes Material Design integrados con tu paleta de colores personalizada.
                    </Paragraph>
                </Card.Content>
                <Card.Actions>
                    <Button mode="contained">Botón Principal</Button>
                    <Button mode="outlined">Botón Outline</Button>
                </Card.Actions>
            </Card>

            <View style={styles.chipsContainer}>
                <Chip icon="information" mode="outlined" style={styles.chip}>
                    Estado: Aprobado
                </Chip>
                <Chip icon="clock" mode="flat" style={styles.chip}>
                    Pendiente
                </Chip>
            </View>

            <FAB
                style={styles.fab}
                icon="plus"
                onPress={() => console.log('FAB pressed')}
            />
        </View>
    );
};

const styles = StyleSheet.create({
    container: {
        flex: 1,
        padding: 16,
        backgroundColor: Colors.appBackground,
    },
    card: {
        marginBottom: 16,
    },
    chipsContainer: {
        flexDirection: 'row',
        flexWrap: 'wrap',
        marginBottom: 16,
    },
    chip: {
        marginRight: 8,
        marginBottom: 8,
    },
    fab: {
        position: 'absolute',
        margin: 16,
        right: 0,
        bottom: 0,
        backgroundColor: Colors.primary,
    },
});

export default PaperExampleScreen;
