# Script para iniciar Metro Bundler con configuración legacy de OpenSSL
$env:NODE_OPTIONS = "--openssl-legacy-provider"
npx react-native start --reset-cache
