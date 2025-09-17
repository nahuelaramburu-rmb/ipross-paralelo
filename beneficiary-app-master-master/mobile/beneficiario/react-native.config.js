module.exports = {
    project: {
        ios: {},
        android: {
            packageName: 'com.ipross.beneficiario',
        },
    },
    assets: ['./app/fonts'],
    dependencies: {
        '@react-native-async-storage/async-storage': {
            platforms: {
                android: null,
            },
        },
        'react-native-gesture-handler': {
            platforms: {
                android: null,
            },
        },
        '@react-native-firebase/app': {
            platforms: {
                android: null,
            },
        },
        '@react-native-firebase/messaging': {
            platforms: {
                android: null,
            },
        },
    },
};
