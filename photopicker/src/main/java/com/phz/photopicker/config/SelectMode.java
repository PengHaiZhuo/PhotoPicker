package com.phz.photopicker.config;

/**
 * @author haizhuo
 * @introduction
 */
public enum SelectMode implements ImagePickerConstant.GetSelectMode {
    SINGLE(ImagePickerConstant.MODE_SINGLE),
    MULTI(ImagePickerConstant.MODE_MULTI);

    private int mode;

    SelectMode(int mode) {
        this.mode=mode;
    }

    @Override
    public int getSelectMode() {
        return mode;
    }
}
