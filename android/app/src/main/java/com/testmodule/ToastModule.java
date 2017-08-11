package com.testmodule;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.widget.Toast;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;

import java.io.File;
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

    @ReactMethod
    public void getFilesMap(String currentUri, Callback resultCallback) {
        File[] files;
        if (TextUtils.isEmpty(currentUri)) {
            files = root.listFiles();
        } else {
            files = new File(currentUri).listFiles();
        }
        WritableMap uris = getUriMap(files);
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

    private WritableMap getUriMap(File[] files) {
        WritableMap uris = new WritableNativeMap();
        if(files == null){
            return uris;
        }
        for (File file : files) {
            if(file.isDirectory()) {
                uris.putString("icon_uri", getFolderIconUri());
            } else {
                uris.putString("icon_uri", getFileIconUri(file.getPath()));
            }
            uris.putBoolean("directory", file.isDirectory());
            uris.putString("uri", file.getPath());
        }
        return uris;
    }

    private String getFolderIconUri(){
        String packageName = getReactApplicationContext().getPackageName();
        String fileName = "ic_folder_icon";
        return "android.resource://" + packageName + "/drawable/" + fileName;
    }

    @ReactMethod
    public void show(String message, int duration) {
        if (toast != null && toast.getView().isShown()) {
            toast.cancel();
        }
        toast = Toast.makeText(getReactApplicationContext(), message, duration);
        toast.show();
    }

    private String getFileIconUri(String fileUri){
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(fileUri));
        intent.setType("application/pdf");

        final List<ResolveInfo> matches = getReactApplicationContext().getPackageManager().queryIntentActivities(intent, 0);
        if(!matches.isEmpty()){
            return getIconUri(matches.get(0).activityInfo).toString();
        }
        return "";
    }

    @ReactMethod
    public void getFileIcon(String fileUri, Callback resultCallBack){
         resultCallBack.invoke(getFileIconUri(fileUri));
    }

    private Uri getIconUri(ActivityInfo appInfo){
        return Uri.parse("android.resource://" + appInfo.packageName + "/" + appInfo.applicationInfo.icon);
    }
}
