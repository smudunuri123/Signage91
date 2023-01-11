package com.app.signage91.utils.silent_app_install;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;


import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


public final class FileUtils {

    public static final String MODE_READ_ONLY = "r";

    private static final String EXT_STORAGE_PATH = getExtStoragePath();

    private static final String EXT_STORAGE_DIR = EXT_STORAGE_PATH + File.separator;

    private static final String APP_EXT_STORAGE_PATH = EXT_STORAGE_DIR + "Android";

    private static final String EXT_DOWNLOADS_PATH = getExtDownloadsPath();

    private static final String EXT_PICTURES_PATH = getExtPicturesPath();

    private static final String EXT_DCIM_PATH = getExtDCIMPath();

    private FileUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }


    @Nullable
    public static File getFileByPath(final String filePath) {
        return isSpace(filePath) ? null : new File(filePath);
    }

    public static boolean isFileExists(Context context, final File file) {
        if (file == null) {
            return false;
        }
        if (file.exists()) {
            return true;
        }
        return isFileExists(context, file.getAbsolutePath());
    }


    public static boolean isFileExists(Context context, final String filePath) {
        File file = getFileByPath(filePath);
        if (file == null) {
            return false;
        }
        if (file.exists()) {
            return true;
        }
        return isFileExistsApi29(context, filePath);
    }

    private static boolean isFileExistsApi29(Context context, String filePath) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            AssetFileDescriptor afd = null;
            try {
                Uri uri = Uri.parse(filePath);
                afd = openAssetFileDescriptor(context, uri);
                if (afd == null) {
                    return false;
                } else {
                    closeIOQuietly(afd);
                }
            } catch (FileNotFoundException e) {
                return false;
            } finally {
                closeIOQuietly(afd);
            }
            return true;
        }
        return false;
    }

    public static InputStream getFileInputStream(Context context, File file) throws FileNotFoundException {
        if (isScopedStorageMode()) {
            return getContentResolver(context).openInputStream(getUriByFile(context, file));
        } else {
            return new FileInputStream(file);
        }
    }


    public static Uri getUriByFile(Context context, final File file) {
        if (file == null) {
            return null;
        }
        if (isScopedStorageMode() && isPublicPath(file)) {
            String filePath = file.getAbsolutePath();
            if (filePath.startsWith(EXT_DOWNLOADS_PATH)) {
                return getDownloadContentUri(context, file);
            } else if (filePath.startsWith(EXT_PICTURES_PATH) || filePath.startsWith(EXT_DCIM_PATH)) {
                return getMediaContentUri(context, file);
            } else {
                return getUriForFile(context, file);
            }
        } else {
            return getUriForFile(context, file);
        }
    }


    @Nullable
    public static Uri getUriForFile(Context context, final File file) {
        if (file == null) {
            return null;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            String authority = context.getPackageName() + ".updateFileProvider";
            return FileProvider.getUriForFile(context, authority, file);
        } else {
            return Uri.fromFile(file);
        }
    }


    public static boolean isScopedStorageMode() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !Environment.isExternalStorageLegacy();
    }


    public static Uri getMediaContentUri(Context context, File mediaFile) {
        String filePath = mediaFile.getAbsolutePath();
        Uri baseUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = context.getContentResolver().query(baseUri,
                new String[]{MediaStore.Images.Media._ID}, MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);
        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            cursor.close();
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (mediaFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(baseUri, values);
            }
            return null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static Uri getDownloadContentUri(Context context, File file) {
        String filePath = file.getAbsolutePath();
        Uri baseUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
        Cursor cursor = context.getContentResolver().query(baseUri,
                new String[]{MediaStore.Downloads._ID}, MediaStore.Downloads.DATA + "=? ",
                new String[]{filePath}, null);
        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(MediaStore.DownloadColumns._ID));
            cursor.close();
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (file.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DATA, filePath);
                return context.getContentResolver().insert(baseUri, values);
            }
            return null;
        }
    }

    public static boolean isPrivatePath(@NonNull Context context, @NonNull String path) {
        if (isSpace(path)) {
            return false;
        }
        String appIntPath = getAppIntPath(context);
        String appExtPath = getAppExtPath(context);
        return (!TextUtils.isEmpty(appIntPath) && path.startsWith(appIntPath))
                || (!TextUtils.isEmpty(appExtPath) && path.startsWith(appExtPath));
    }


    public static boolean isPublicPath(File file) {
        if (file == null) {
            return false;
        }
        try {
            return isPublicPath(file.getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    public static boolean isPublicPath(String filePath) {
        if (isSpace(filePath)) {
            return false;
        }
        return filePath.startsWith(EXT_STORAGE_PATH) && !filePath.startsWith(APP_EXT_STORAGE_PATH);
    }

    private static boolean isSpace(final String s) {
        if (s == null) {
            return true;
        }
        for (int i = 0, len = s.length(); i < len; ++i) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }


    public static void closeIOQuietly(final Closeable... closeables) {
        if (closeables == null) {
            return;
        }
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException ignored) {
                }
            }
        }
    }


    public static AssetFileDescriptor openAssetFileDescriptor(Context context, Uri uri) throws FileNotFoundException {
        return getContentResolver(context).openAssetFileDescriptor(uri, MODE_READ_ONLY);
    }

    private static ContentResolver getContentResolver(Context context) {
        return context.getContentResolver();
    }


    public static String getExtStoragePath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }


    public static String getExtDownloadsPath() {
        return Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                .getAbsolutePath();
    }


    public static String getExtPicturesPath() {
        return Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                .getAbsolutePath();
    }


    public static String getExtDCIMPath() {
        return Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                .getAbsolutePath();
    }


    public static String getAppIntPath(@NonNull Context context) {
        File appIntCacheFile = context.getCacheDir();
        if (appIntCacheFile != null) {
            String appIntCachePath = appIntCacheFile.getAbsolutePath();
            return getDirName(appIntCachePath);
        }
        return null;
    }


    public static String getAppExtPath(@NonNull Context context) {
        File appExtCacheFile = context.getExternalCacheDir();
        if (appExtCacheFile != null) {
            String appExtCachePath = appExtCacheFile.getAbsolutePath();
            return getDirName(appExtCachePath);
        }
        return null;
    }

    public static String getDirName(final String filePath) {
        if (isSpace(filePath)) {
            return filePath;
        }
        int lastSep = filePath.lastIndexOf(File.separator);
        return lastSep == -1 ? "" : filePath.substring(0, lastSep + 1);
    }

}
