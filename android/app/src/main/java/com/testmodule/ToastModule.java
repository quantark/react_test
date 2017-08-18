package com.testmodule;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;

import org.json.JSONException;

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
        WritableArray uris = getUriMap(files);
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

    private WritableArray getUriMap(File[] files) {
        WritableArray uris = new WritableNativeArray();
        if(files == null){
            return uris;
        }
        for (File file : files) {
            WritableMap map = new WritableNativeMap();
            if(file.isDirectory()) {
                map.putString("icon_uri", getFolderIconUri());
            } else {
                map.putString("icon_uri", getFileIconUri(file.getPath()));
            }
            map.putBoolean("directory", file.isDirectory());
            map.putString("uri", file.getPath());
            uris.pushMap(map);
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

    @ReactMethod
    public void open(String fileUriStr, Promise promise) throws JSONException {

        String realUri = getRealUri(fileUriStr);
        File file = new File(realUri);
        if (file.exists()) {
            try {
//                Uri path = FileProvider.getUriForFile(getReactApplicationContext(), getReactApplicationContext().getPackageName() + ".fileprovider", file);
                Intent intent = new Intent(Intent.ACTION_VIEW);
//                intent.setDataAndType(Uri.parse(realUri), getMimeType(realUri));
                intent.setDataAndType(Uri.fromFile(file), getMimeType(realUri));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                getReactApplicationContext().startActivity(intent);

                promise.resolve("Open success!!");
            } catch (android.content.ActivityNotFoundException e) {
                promise.reject("Open error!!");
            }
        } else {
            promise.reject("File not found");
        }
    }

    private String getRealUri(String contentUri){
        Uri fileUri = Uri.parse(contentUri);
        String filePath;
        if (fileUri != null && "content".equals(fileUri.getScheme())) {
            Cursor cursor = getReactApplicationContext().getContentResolver().query(fileUri, new String[] { android.provider.MediaStore.Images.ImageColumns.DATA }, null, null, null);
            cursor.moveToFirst();
            filePath = cursor.getString(0);
            cursor.close();
        } else {
            filePath = fileUri.getPath();
        }

        return filePath;
    }

    private String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }
}
