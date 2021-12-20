package com.phz.photopicker.model;

import androidx.annotation.Nullable;

import java.util.List;

/**
 * @author haizhuo
 * @introduction 选择文件夹
 */
public class ImageFileModel {
    private String name;
    private String path;
    private ImageModel imageModel;
    private List<ImageModel> list;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public ImageModel getImageModel() {
        return imageModel;
    }

    public void setImageModel(ImageModel imageModel) {
        this.imageModel = imageModel;
    }

    public List<ImageModel> getList() {
        return list;
    }

    public void setList(List<ImageModel> list) {
        this.list = list;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        try {
            ImageFileModel other = (ImageFileModel) obj;
            return this.path.equalsIgnoreCase(other.path);
        }catch (ClassCastException e){
            e.printStackTrace();
        }
        return super.equals(obj);
    }
}
