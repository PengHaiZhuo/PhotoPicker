package com.phz.photopicker.intent;

import android.content.Context;
import android.content.Intent;

import com.phz.photopicker.activity.PreViewImageActivity;
import com.phz.photopicker.config.ImagePickerConstract;

import java.util.ArrayList;

/**
 * @author haizhuo
 * @introduction 跳转预览图片活动意图
 */
public class PreViewImageIntent extends Intent {
    public PreViewImageIntent(Context mContext) {
        super(mContext, PreViewImageActivity.class);
    }
    
    /**
     * 照片地址
     * @param pathList
     */
    public void setPhotoPaths(ArrayList<String> pathList){
        this.putStringArrayListExtra(ImagePickerConstract.EXTRA_PHOTOS, pathList);
    }

    /**
     * 当前照片的下标
     * @param currentItem
     */
    public void setCurrentItem(int currentItem){
        this.putExtra(ImagePickerConstract.EXTRA_CURRENT_ITEM, currentItem);
    }

    /**
     * 是否显示删除菜单
     * @param isShow
     */
    public void setIsShowDeleteMenu(boolean isShow){
        this.putExtra(ImagePickerConstract.EXTRA_IS_SHOW_DELETE, isShow);
    }
}
