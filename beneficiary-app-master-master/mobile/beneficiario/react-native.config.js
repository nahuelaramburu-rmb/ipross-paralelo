module.exports = {
    project: {
        ios: {},
        android: {
            packageName: 'com.capacidad.beneficiaryapp',
        },
    },
    assets: ['./app/fonts'],
    dependencies: {
        'react-native-date-picker': {
            platforms: {
                android: {
                    sourceDir: '../node_modules/react-native-date-picker/android',
                    packageImportPath: 'import io.github.wix.RNDatePicker.DatePickerPackage;',
                    packagePath: 'new DatePickerPackage()',
                },
            },
        },
    },
};
