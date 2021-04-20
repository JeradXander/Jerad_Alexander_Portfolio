package com.jerad_zac.solace.utilities;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionUtils {


    private static final int REQUEST_ALL = 0x101;
    static Activity mActivity;

    public static void requestPermissions(Activity activity) {

        mActivity = activity;

        if (!hasPermissions(mActivity)[0] || !hasPermissions(mActivity)[1] || !hasPermissions(mActivity)[2]) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CALL_PHONE}, REQUEST_ALL);
        }
    }

    public static boolean[] hasPermissions(Activity activity) {
        mActivity = activity;
        boolean result = ContextCompat.checkSelfPermission(mActivity, Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        boolean result2 = ContextCompat.checkSelfPermission(mActivity, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED;

        return new boolean[]{result, result1, result2};
    }


}
