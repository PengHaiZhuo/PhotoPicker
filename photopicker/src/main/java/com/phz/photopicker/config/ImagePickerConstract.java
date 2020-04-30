package com.phz.photopicker.config;

/**
 * @author haizhuo
 * @introduction 配置信息
 */
public interface ImagePickerConstract {
    /**
     * 普通回调监听
     */
    public interface SampleResultListener<T>{
        void onSuccess(T Data);
        void onFailure(T Data);
    }

    /**
     * 普通回调监听
     */
    interface NormalResultListener{
        void onSuccess();
        void onFailure();
    }

    interface GetSlectMode {
        int getSlectMode();
    }

    int REQUEST_TAKE_PHOTO = 1;
    /**
     * 预览请求状态码
     */
     int REQUEST_PREVIEW = 99;

    String mimeTypeImage="image/*";

    /** 图片选择模式，int类型 */
    String EXTRA_SELECT_MODE = "select_count_mode";
    /** 单选 */
    int MODE_SINGLE = 0;
    /** 多选 */
    int MODE_MULTI = 1;

    /** 最大图片选择次数，int类型 */
    String EXTRA_SELECT_COUNT = "max_select_count";
    /** 默认最大照片数量 */
    int DEFAULT_MAX_TOTAL= 9;

    /** 是否显示相机，boolean类型 */
    String EXTRA_SHOW_CAMERA = "show_camera";

    /** 默认选择的数据集 */
    String EXTRA_DEFAULT_SELECTED_LIST = "default_result";

    /** 筛选照片配置信息 */
    String EXTRA_IMAGE_CONFIG = "image_filter_configuration";

    /** 选择结果，返回为ArrayList<String>图片路径集合 */
    String EXTRA_RESULT = "pathList_result";

    /**
     * 预览的图片列表
     */
    String EXTRA_PHOTOS = "extra_photos";

    /**
     * 当前预览照片位置
     */
    String EXTRA_CURRENT_ITEM = "extra_current_item";

    /**
     * 预览界面是否显示删除Menu
     */
    String EXTRA_IS_SHOW_DELETE = "extra_is_show_delete";
}
