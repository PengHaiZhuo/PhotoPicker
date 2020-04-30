package com.phz.photopicker.config;

/**
 * @author haizhuo
 * @introduction
 */
public enum SelectMode implements ImagePickerConstract.GetSlectMode{
    SINGLE(ImagePickerConstract.MODE_SINGLE),
    MULTI(ImagePickerConstract.MODE_MULTI);

    private int mode;

    SelectMode(int mode) {
        this.mode=mode;
    }

    @Override
    public int getSlectMode() {
        return mode;
    }
}
