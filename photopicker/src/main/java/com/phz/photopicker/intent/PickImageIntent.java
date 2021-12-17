package com.phz.photopicker.intent;

import android.content.Context;
import android.content.Intent;

import com.phz.photopicker.activity.PickerImageActivity;
import com.phz.photopicker.config.ImageConfig;
import com.phz.photopicker.config.ImagePickerConstant;
import com.phz.photopicker.config.SelectMode;

import java.util.ArrayList;

/**
 * @author haizhuo
 * @introduction 选择图片意图
 */
public class PickImageIntent extends Intent {

    public PickImageIntent(Context mContext) {
        super(mContext, PickerImageActivity.class);
    }

    /**
     * 设置是否显示照相机图标
     * @param flag
     */
    public void setIsShowCamera(boolean flag){
        this.putExtra(ImagePickerConstant.EXTRA_SHOW_CAMERA,flag);
    }

    /**
     * 设置最多选择图片个数
     * @param total
     */
    public void setSelectedCount(int total){
        this.putExtra(ImagePickerConstant.EXTRA_SELECT_COUNT,total);
    }

    /**
     * 选择
     * @param model
     */
    public void setSelectModel(SelectMode model){
        this.putExtra(ImagePickerConstant.EXTRA_SELECT_MODE, model.getSelectMode());
    }

    /**
     * 已选择的照片地址
     * @param list
     */
    public void setSelectedPaths(ArrayList<String> list){
        this.putStringArrayListExtra(ImagePickerConstant.EXTRA_DEFAULT_SELECTED_LIST, list);
    }

    /**
     * 显示相册图片的属性
     * @param config
     */
    public void setImageConfig(ImageConfig config){
        this.putExtra(ImagePickerConstant.EXTRA_IMAGE_CONFIG, config);
    }

}
