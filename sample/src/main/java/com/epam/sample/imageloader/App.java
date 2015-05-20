package com.epam.sample.imageloader;

import android.app.Application;

import com.epam.imageloader.ImageLoader;

/**
 * Created by jafar_qaddoumi on 5/20/15.
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onLowMemory() {
        ImageLoader.getInstance(this).clearMemoryCache();

        super.onLowMemory();
    }

}
