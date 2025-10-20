# Script para agregar namespaces a las dependencias de React Native

$packages = @(
    @{
        path = "node_modules\react-native-push-notification\android\build.gradle"
        namespace = "com.dieam.reactnativepushnotification"
    },
    @{
        path = "node_modules\react-native-vector-icons\android\build.gradle"
        namespace = "com.oblador.vectoricons"
    },
    @{
        path = "node_modules\react-native-svg\android\build.gradle"
        namespace = "com.horcrux.svg"
    },
    @{
        path = "node_modules\react-native-screens\android\build.gradle"
        namespace = "com.swmansion.rnscreens"
    },
    @{
        path = "node_modules\react-native-safe-area-context\android\build.gradle"
        namespace = "com.th3rdwave.safeareacontext"
    },
    @{
        path = "node_modules\react-native-reanimated\android\build.gradle"
        namespace = "com.swmansion.reanimated"
    },
    @{
        path = "node_modules\@react-native-masked-view\masked-view\android\build.gradle"
        namespace = "org.reactnative.maskedview"
    },
    @{
        path = "node_modules\@react-native-community\datetimepicker\android\build.gradle"
        namespace = "com.reactcommunity.rndatetimepicker"
    },
    @{
        path = "node_modules\@react-native-community\netinfo\android\build.gradle"
        namespace = "com.reactnativecommunity.netinfo"
    },
    @{
        path = "node_modules\@react-native-firebase\app\android\build.gradle"
        namespace = "io.invertase.firebase.app"
    },
    @{
        path = "node_modules\@react-native-firebase\messaging\android\build.gradle"
        namespace = "io.invertase.firebase.messaging"
    },
    @{
        path = "node_modules\react-native-device-info\android\build.gradle"
        namespace = "com.learnium.RNDeviceInfo"
    },
    @{
        path = "node_modules\react-native-config\android\build.gradle"
        namespace = "com.lugg.ReactNativeConfig"
    }
)

foreach ($package in $packages) {
    $buildGradlePath = $package.path
    $namespace = $package.namespace
    
    if (Test-Path $buildGradlePath) {
        Write-Host "Procesando: $buildGradlePath"
        
        $content = Get-Content $buildGradlePath -Raw
        
        # Verificar si ya tiene namespace
        if ($content -notmatch "namespace\s+") {
            # Buscar el bloque android { y añadir namespace
            $updatedContent = $content -replace "(android\s*\{)", "`$1`r`n    namespace '$namespace'"
            
            Set-Content $buildGradlePath $updatedContent -Encoding UTF8
            Write-Host "Namespace añadido: $namespace"
        } else {
            Write-Host "Ya tiene namespace"
        }
    } else {
        Write-Host "No encontrado: $buildGradlePath"
    }
}

Write-Host "Proceso completado"