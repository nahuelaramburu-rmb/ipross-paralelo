const fs = require('fs');
const path = require('path');

// Lista de librerías que necesitan namespace
const librariesNeedingNamespace = [
    'react-native-extra-dimensions-android',
    'react-native-device-info',
    'react-native-image-picker',
    'react-native-push-notification',
    'react-native-sensitive-info',
    'react-native-vector-icons',
    'react-native-maps',
    'react-native-svg',
    'react-native-reanimated',
    'react-native-gesture-handler',
    'react-native-safe-area-context',
    'react-native-screens',
];

function patchLibraryNamespace(libraryName) {
    const buildGradlePath = path.join(__dirname, 'node_modules', libraryName, 'android', 'build.gradle');

    if (fs.existsSync(buildGradlePath)) {
        let content = fs.readFileSync(buildGradlePath, 'utf8');

        // Verificar si ya tiene namespace
        if (!content.includes('namespace ')) {
            // Buscar la sección android{}
            const androidMatch = content.match(/android\s*\{/);
            if (androidMatch) {
                const insertIndex = androidMatch.index + androidMatch[0].length;
                const namespace = `\n    namespace '${getNamespaceForLibrary(libraryName)}'`;
                content = content.slice(0, insertIndex) + namespace + content.slice(insertIndex);

                fs.writeFileSync(buildGradlePath, content);
                console.log(`Patched namespace for ${libraryName}`);
            }
        } else {
            console.log(`${libraryName} already has namespace`);
        }
    } else {
        console.log(`Build file not found for ${libraryName}`);
    }
}

function getNamespaceForLibrary(libraryName) {
    const namespaceMap = {
        'react-native-extra-dimensions-android': 'ca.jaysoo.extradimensions',
        'react-native-device-info': 'com.learnium.RNDeviceInfo',
        'react-native-image-picker': 'com.imagepicker',
        'react-native-push-notification': 'com.dieam.reactnativepushnotification',
        'react-native-sensitive-info': 'br.com.classapp.RNSensitiveInfo',
        'react-native-vector-icons': 'com.oblador.vectoricons',
        'react-native-maps': 'com.rnmaps.maps',
        'react-native-svg': 'com.horcrux.svg',
        'react-native-reanimated': 'com.swmansion.reanimated',
        'react-native-gesture-handler': 'com.swmansion.gesturehandler.react',
        'react-native-safe-area-context': 'com.th3rdwave.safeareacontext',
        'react-native-screens': 'com.swmansion.rnscreens',
    };
    return namespaceMap[libraryName] || `com.${libraryName.replace(/-/g, '.')}`;
}

console.log('Patching React Native libraries...');
librariesNeedingNamespace.forEach(patchLibraryNamespace);
console.log('Patching completed!');
