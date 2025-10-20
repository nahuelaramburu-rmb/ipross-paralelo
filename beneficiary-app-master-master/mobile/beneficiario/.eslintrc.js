module.exports = {
    root: true,
    extends: '@react-native-community',
    rules: {
        'jsx-quotes': 0,
        curly: 0,
        'no-shadow': 0,
        'dot-notation': 0,
        'react/prop-types': 0, // Deshabilitar warnings de PropTypes
        'react-native/no-inline-styles': 0, // Deshabilitar warnings de estilos inline
        'no-unused-vars': 1, // Cambiar a warning en lugar de error
        'no-alert': 1, // Cambiar alerts a warning
        'no-bitwise': 0, // Deshabilitar warnings de operadores bitwise
        eqeqeq: 1, // Cambiar comparaciones == a warning
        'prettier/prettier': [
            'error',
            {
                endOfLine: 'auto',
            },
        ],
    },
};
