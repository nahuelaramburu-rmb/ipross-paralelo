package com.ipross.beneficiario.mobile;

import com.facebook.react.ReactActivity;
import com.facebook.react.ReactActivityDelegate;
import com.facebook.react.ReactRootView;

// Gesture Handler
import android.os.Bundle;
// import com.swmansion.gesturehandler.react.RNGestureHandlerEnabledRootView;

public class MainActivity extends ReactActivity {

    /**
     * Returns the name of the main component registered from JavaScript.
     * This is used to schedule rendering of the component.
     */
    @Override
    protected String getMainComponentName() {
        return "IprossVEM";
    }

    /**
     * Returns the instance of the {@link ReactActivityDelegate}. We use a util class {@link
     * ReactActivityDelegate} which allows you to easily enable Fabric and Concurrent React
     * (aka React 18) with two boolean flags.
     */
    @Override
    protected ReactActivityDelegate createReactActivityDelegate() {
        return new ReactActivityDelegate(this, getMainComponentName()) {
            @Override
            protected ReactRootView createRootView() {
                ReactRootView reactRootView = new ReactRootView(MainActivity.this);
                // ReactRootView reactRootView = new RNGestureHandlerEnabledRootView(MainActivity.this);
                // If you opted-in for the New Architecture, we enable the Fabric Renderer.
                reactRootView.setIsFabric(false);
                return reactRootView;
            }
        };
    }
}
