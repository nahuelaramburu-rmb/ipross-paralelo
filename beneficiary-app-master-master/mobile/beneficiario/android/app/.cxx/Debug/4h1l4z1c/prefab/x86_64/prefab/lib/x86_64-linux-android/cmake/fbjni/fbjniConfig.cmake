if(NOT TARGET fbjni::fbjni)
add_library(fbjni::fbjni SHARED IMPORTED)
set_target_properties(fbjni::fbjni PROPERTIES
    IMPORTED_LOCATION "C:/Users/Lucas/.gradle/caches/8.13/transforms/4589cc6e0b930df8190c155418a64bea/transformed/jetified-fbjni-0.6.0/prefab/modules/fbjni/libs/android.x86_64/libfbjni.so"
    INTERFACE_INCLUDE_DIRECTORIES "C:/Users/Lucas/.gradle/caches/8.13/transforms/4589cc6e0b930df8190c155418a64bea/transformed/jetified-fbjni-0.6.0/prefab/modules/fbjni/include"
    INTERFACE_LINK_LIBRARIES ""
)
endif()

