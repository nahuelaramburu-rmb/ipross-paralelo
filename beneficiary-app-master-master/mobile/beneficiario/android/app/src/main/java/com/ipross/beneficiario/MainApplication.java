package com.ipross.beneficiario.mobile;

import android.app.Application;
import android.content.Context;
import com.facebook.react.ReactApplication;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.config.ReactFeatureFlags;
import com.facebook.react.shell.MainReactPackage;
import com.facebook.soloader.SoLoader;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

// Gesture Handler
// import com.swmansion.gesturehandler.react.RNGestureHandlerPackage;

// AsyncStorage
// import com.reactnativecommunity.asyncstorage.AsyncStoragePackage;

// Firebase (commented for now)
// import io.invertase.firebase.app.ReactNativeFirebaseAppPackage;
// import io.invertase.firebase.messaging.ReactNativeFirebaseMessagingPackage;

public class MainApplication extends Application implements ReactApplication {

  private final ReactNativeHost mReactNativeHost =
      new ReactNativeHost(this) {
        @Override
        public boolean getUseDeveloperSupport() {
          return false;
        }

        @Override
        protected List<ReactPackage> getPackages() {
          @SuppressWarnings("UnnecessaryLocalVariable")
          List<ReactPackage> packages = Arrays.<ReactPackage>asList(
              new MainReactPackage()
              // new RNGestureHandlerPackage(),
              // new AsyncStoragePackage()
              // new ReactNativeFirebaseAppPackage(),
              // new ReactNativeFirebaseMessagingPackage()
              // Add other packages here as needed for manual configuration
          );
          return packages;
        }

        @Override
        protected String getJSMainModuleName() {
          return "index";
        }
      };

  @Override
  public ReactNativeHost getReactNativeHost() {
    return mReactNativeHost;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    SoLoader.init(this, /* native exopackage */ false);
    if (false) {
      // If you opted-in for the New Architecture, we load the native entry point for this app.
      // We use reflection here to pick up the class that initializes the new architecture,
      // since the new architecture is not available in old version of react native
      try {
        Class<?> newArchClass = Class.forName("com.facebook.react.NewArchBootstrapper");
        newArchClass.getMethod("load").invoke(null);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    // COMMENTED OUT: ReactNativeFlipper is not included in release builds
    // ReactNativeFlipper.initializeFlipper(this, getReactNativeHost().getReactInstanceManager());
  }

}
