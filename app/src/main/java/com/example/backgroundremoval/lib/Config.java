package com.example.backgroundremoval.lib;

import android.content.Context;
import android.content.res.AssetManager;

public class Config {

    private static Context appContext;

    public static Context getAppContext() {
        return appContext;
    }

    public static AssetManager getAssetManager() {
        return appContext.getAssets();
    }

    public static void configure(Context context) {
        appContext = context.getApplicationContext();
      //  ApiClient apiClient= new ApiClient();
        //apiClient.execute("https://github.com/tupm97/MyApplication7/releases/download/model_person/people_hq_768x768_1_1543441179.tflite");
    }
}
