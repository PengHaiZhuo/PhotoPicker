package com.phz.photopicker.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.phz.photopicker.R;


/**
 * @author haizhuo
 * @introduction 有用到的一些工具方法
 */
public class UsageUtil {
    private static final String APP_NAME="PhotoPicker";

    /**
     * 根据屏幕宽度与密度计算GridView显示的列数， 最少为三列
     * @return
     */
    public static int getNumColnums(Context context){
        int cols = context.getResources().getDisplayMetrics().widthPixels / context.getResources().getDisplayMetrics().densityDpi;
        return Math.max(cols, 3);
    }

    /**
     * 获取GridView Item宽度
     * @return
     */
    public static int getItemImageWidth(Context context){
        int cols = getNumColnums(context);
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        int columnSpace = context.getResources().getDimensionPixelOffset(R.dimen.space_size);
        return (screenWidth - columnSpace * (cols-1)) / cols;
    }


    /**
     * 根据路径得到文件名
     *
     * @param path
     * @return
     */
    public static String getFileName(String path) {
        String name = "";
        if (path != null) {
            name = path.substring(path.lastIndexOf("/") + 1);
        }
        return name;
    }

    /**
     * 获取app名称
     * @return
     */
    public static String getAppName(Context context){
        ApplicationInfo appInfo;
        try {
            appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            return appInfo.loadLabel(context.getPackageManager()) + "";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return APP_NAME;
    }
}
