
package com.app.signage91.utils.silent_app_install;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresPermission;

import com.app.signage91.activities.MainActivity;
import com.app.signage91.utils.silent_app_install.ShellUtils.CommandResult;


import java.io.File;
import java.io.IOException;
import java.util.Objects;

import static android.Manifest.permission.INSTALL_PACKAGES;

public final class ApkInstallUtils {
    private static final int APP_INSTALL_AUTO = 0;
    private static final int APP_INSTALL_INTERNAL = 1;
    private static final int APP_INSTALL_EXTERNAL = 2;

    public static final int REQUEST_CODE_INSTALL_APP = 999;


    private static boolean sSupportSilentInstall = true;

    public static boolean isSupportSilentInstall() {
        return sSupportSilentInstall;
    }


    public static void setSupportSilentInstall(boolean supportSilentInstall) {
        ApkInstallUtils.sSupportSilentInstall = supportSilentInstall;
    }

    private ApkInstallUtils() {
        throw new UnsupportedOperationException("Do not need instantiate!");
    }


    public static boolean install(Context context, File apkFile) throws IOException {
        return isSupportSilentInstall() ? install(context, apkFile.getCanonicalPath()) : installNormal(context, apkFile.getCanonicalPath());
    }



    public static boolean install(Context context, String filePath) {
        if (ApkInstallUtils.isSystemApplication(context)
                || ShellUtils.checkRootPermission()) {
            return installAppSilent(context, filePath);
        }
        return installNormal(context, filePath);
    }


    @RequiresPermission(INSTALL_PACKAGES)
    public static boolean installAppSilent(Context context, String filePath) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return installAppSilentBelow24(context, filePath);
        } else {
            return installAppSilentAbove24(context , filePath);
        }
    }


    @RequiresPermission(INSTALL_PACKAGES)
    private static boolean installAppSilentBelow24(Context context, String filePath) {
        File file = FileUtils.getFileByPath(filePath);
        if (!FileUtils.isFileExists(context, file)) {
            return false;
        }
        String pmParams = " -r " + getInstallLocationParams();

        String subCommandString = "LD_LIBRARY_PATH=/vendor/lib:/system/lib pm install ";
        StringBuilder command = new StringBuilder()
                .append(subCommandString)
                .append(pmParams).append(" ")
                .append(filePath.replace(" ", "\\ "));
        CommandResult commandResult = ShellUtils.execCommand(
                command.toString(), !isSystemApplication(context), true);
        return commandResult.successMsg != null
                && (commandResult.successMsg.contains("Success") || commandResult.successMsg
                .contains("success"));
    }

    private static String getInstallLocationParams() {
        int location = getInstallLocation();
        switch (location) {
            case APP_INSTALL_INTERNAL:
                return "-f";
            case APP_INSTALL_EXTERNAL:
                return "-s";
            default:
                break;
        }
        return "";
    }

    public static int getInstallLocation() {
        String subCommandString;
        if (Objects.equals(System.getProperty("os.arch"), "aarch64")){
            subCommandString = "LD_LIBRARY_PATH=/vendor/lib:/system/lib pm get-install-location";
        }else {
            subCommandString = "LD_LIBRARY_PATH=/vendor/lib:/system/lib32 pm get-install-location";
        }

        CommandResult commandResult = ShellUtils
                .execCommand(
                        subCommandString,
                        false, true);
        if (commandResult.result == 0 && commandResult.successMsg != null
                && commandResult.successMsg.length() > 0) {
            try {
                int location = Integer.parseInt(commandResult.successMsg
                        .substring(0, 1));
                switch (location) {
                    case APP_INSTALL_INTERNAL:
                        return APP_INSTALL_INTERNAL;
                    case APP_INSTALL_EXTERNAL:
                        return APP_INSTALL_EXTERNAL;
                    default:
                        break;
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return APP_INSTALL_AUTO;
    }

    @RequiresPermission(INSTALL_PACKAGES)
    private static boolean installAppSilentAbove24(Context context, String filePath) {
        File file = FileUtils.getFileByPath(filePath);
        if (!FileUtils.isFileExists(context, file)) {
            return false;
        }
        boolean isRoot = isDeviceRooted();
        String command = "pm install -i " + context.getPackageName() + " --user 0 " + filePath;
        CommandResult commandResult = ShellUtils.execCommand(command, isRoot);
        return (commandResult.successMsg != null
                && commandResult.successMsg.toLowerCase().contains("success"));
    }

    private static boolean installNormal(Context context, String filePath) {
        File file = FileUtils.getFileByPath(filePath);
        return FileUtils.isFileExists(context, file) && installNormal(context, file);
    }

    private static boolean installNormal(Context context, File appFile) {
        try {
            Intent intent = getInstallAppIntent(context, appFile);
            if (context.getPackageManager().queryIntentActivities(intent, 0).size() > 0) {
                if (context instanceof MainActivity) {
                    ((MainActivity) context).startActivityForResult(intent, REQUEST_CODE_INSTALL_APP);
                } else {
                    context.startActivity(intent);
                }
                return true;
            }
        } catch (Exception e) {
            Log.i("_ERROR", e.getMessage());
            //_XUpdate.onUpdateError(INSTALL_FAILED, "Apk installation failed using the intent of the system!");
        }
        return false;
    }

    public static Intent getInstallAppIntent(Context context, File appFile) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                //区别于 FLAG_GRANT_READ_URI_PERMISSION 跟 FLAG_GRANT_WRITE_URI_PERMISSION， URI权限会持久存在即使重启，直到明确的用 revokeUriPermission(Uri, int) 撤销。 这个flag只提供可能持久授权。但是接收的应用必须调用ContentResolver的takePersistableUriPermission(Uri, int)方法实现
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            }
            Uri fileUri = FileUtils.getUriByFile(context, appFile);
            intent.setDataAndType(fileUri, "application/vnd.android.package-archive");
            return intent;
        } catch (Exception e) {
            //_XUpdate.onUpdateError(INSTALL_FAILED, "Failed to get intent for installation！");
        }
        return null;
    }

    private static boolean isDeviceRooted() {
        String su = "su";
        String[] locations = {"/system/bin/", "/system/xbin/", "/sbin/", "/system/sd/xbin/",
                "/system/bin/failsafe/", "/data/local/xbin/", "/data/local/bin/", "/data/local/"};
        for (String location : locations) {
            if (new File(location + su).exists()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSystemApplication(Context context) {
        return context != null && isSystemApplication(context, context.getPackageName());
    }


    private static boolean isSystemApplication(Context context,
                                               String packageName) {
        return context != null && isSystemApplication(context.getPackageManager(), packageName);
    }

    private static boolean isSystemApplication(PackageManager packageManager,
                                               String packageName) {
        if (packageManager == null || packageName == null
                || packageName.length() == 0) {
            return false;
        }
        try {
            ApplicationInfo app = packageManager.getApplicationInfo(
                    packageName, 0);
            return (app != null && (app.flags & ApplicationInfo.FLAG_SYSTEM) > 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

}
