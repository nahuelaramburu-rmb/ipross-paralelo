if(NOT TARGET hermes-engine::libhermes)
add_library(hermes-engine::libhermes SHARED IMPORTED)
set_target_properties(hermes-engine::libhermes PROPERTIES
    IMPORTED_LOCATION "C:/Users/Lucas/.gradle/caches/8.13/transforms/125adb48676b14ef7344667ca8a81b85/transformed/jetified-hermes-android-0.76.9-debug/prefab/modules/libhermes/libs/android.x86/libhermes.so"
    INTERFACE_INCLUDE_DIRECTORIES "C:/Users/Lucas/.gradle/caches/8.13/transforms/125adb48676b14ef7344667ca8a81b85/transformed/jetified-hermes-android-0.76.9-debug/prefab/modules/libhermes/include"
    INTERFACE_LINK_LIBRARIES ""
)
endif()

