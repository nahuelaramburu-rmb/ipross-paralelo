# Script para ejecutar la aplicación Android con configuración legacy de OpenSSL
$env:NODE_OPTIONS = "--openssl-legacy-provider"
npx react-native run-android
