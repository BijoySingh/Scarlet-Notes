package com.bijoysingh.quicknote;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import com.github.bijoysingh.starter.util.PermissionManager;

/**
 * Created by bijoy on 5/4/16.
 */
public class Utils {
    public static boolean requiresPermission(Activity activity) {
        return Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(activity);
    }

    @TargetApi(23)
    public static void startPermissionRequest(Activity activity) {
        if (requiresPermission(activity)) {
            Intent intent = new Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + activity.getPackageName()));
            activity.startActivityForResult(intent, PermissionManager.REQUEST_CODE_ASK_PERMISSIONS);
        }
    }

    public static void startService(Activity activity) {

    }

    public static void startService(Activity activity, NoteItem item) {

    }

}
