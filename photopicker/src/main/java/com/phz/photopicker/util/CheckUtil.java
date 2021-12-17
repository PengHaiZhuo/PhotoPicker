package com.phz.photopicker.util;

import android.content.Context;
import android.net.Uri;
import android.os.Build;

import java.io.File;

import androidx.core.content.FileProvider;

/**
 * @author haizhuo
 * @introduction
 */
public class CheckUtil {
    //与AndroidManifest.xml中FileProvider的权限字段一致
    public static final String AUTHS="com.phz.android7.fp";
    public static Uri getUriForFile(Context context, File file) {
        if (context == null || file == null) {
            throw new NullPointerException();
        }
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(context.getApplicationContext(), AUTHS, file);
        } else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }

}
