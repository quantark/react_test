package com.testmodule;

import android.os.Environment;
import android.text.TextUtils;
import android.widget.Toast;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableNativeArray;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableNativeArray;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by user on 12.07.2017.
 */

public class ToastModule extends ReactContextBaseJavaModule {
    private static final String DURATION_SHORT_KEY = "SHORT";
    private static final String DURATION_LONG_KEY = "LONG";
    Toast toast;
    File root;

    public ToastModule(ReactApplicationContext reactContext) {
        super(reactContext);
//        root = reactContext.getFilesDir();
        root = Environment.getExternalStorageDirectory();
    }

    @Override
    public String getName() {
        return "MyToastAndroid";
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        constants.put(DURATION_SHORT_KEY, Toast.LENGTH_SHORT);
        constants.put(DURATION_LONG_KEY, Toast.LENGTH_LONG);
        return constants;
    }

    @ReactMethod
    public void getFilesList(String currentUri, Callback resultCallback) {
        File[] files;
        show(currentUri, Toast.LENGTH_SHORT);
        if (TextUtils.isEmpty(currentUri)) {
            files = root.listFiles();
        } else {
            files = new File(currentUri).listFiles();
        }
        WritableArray uris = getUriList(files);
        resultCallback.invoke(uris);
    }

    private WritableArray getUriList(File[] files) {
        WritableArray uris = new WritableNativeArray();
        if(files == null){
            return uris;
        }
        for (File file : files) {
            uris.pushString(file.getPath());
        }
        return uris;
    }

    @ReactMethod
    public void show(String message, int duration) {
        if (toast != null && toast.getView().isShown()) {
            toast.cancel();
        }
        toast = Toast.makeText(getReactApplicationContext(), message, duration);
        toast.show();
    }
}
