package com.phz.photopicker.model;

import androidx.annotation.Nullable;

/**
 * @author haizhuo
 * @introduction 图片实体
 */
public class ImageModel {
    private String path;
    private String name;
    private long time;

    public ImageModel(String path, String name, long time) {
        this.path = path;
        this.name = name;
        this.time = time;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        try {
            ImageModel other = (ImageModel) obj;
            return this.path.equalsIgnoreCase(other.path);
        }catch (ClassCastException e){
            e.printStackTrace();
        }
        return super.equals(obj);
    }
}
