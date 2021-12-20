package com.phz.photopicker.adapter;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.PhotoView;
import com.phz.photopicker.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author haizhuo
 * @introduction 图片预览页 页面适配器
 */
public class PhotoPagerAdapter extends PagerAdapter {
    /**
     * 路径判断依据字段一：http
     */
    private static final String STRING_FLAG_IMAGE = "http";
    /**
     * 图片路径列表
     */
    private List<String> pathList = new ArrayList<>();
    /**
     * 上下文
     */
    private Context mContext;
    /**
     * 视图填充器
     */
    private LayoutInflater mLayoutInflater;

    private OnPhotoTapListener mOnPhotoTapListener;

    public PhotoPagerAdapter(List<String> pathList, Context mContext, OnPhotoTapListener onPhotoTapListener) {
        this.pathList = pathList;
        this.mContext = mContext;
        mLayoutInflater = LayoutInflater.from(mContext);
        mOnPhotoTapListener = onPhotoTapListener;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View itemView = mLayoutInflater.inflate(R.layout.item_preview_image, container, false);
        PhotoView photoView = itemView.findViewById(R.id.iv_photo);

        //判断路径是否是http字段开头
        final String path = pathList.get(position);
        if (TextUtils.isEmpty(path)) {
        } else if (path.startsWith(STRING_FLAG_IMAGE)) {
            //路径以http开头
            Glide.with(mContext)
                    .load(Uri.parse(path))
                    .placeholder(R.drawable.default_error)
                    .error(R.drawable.default_error)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(photoView);
        } else {
            //不是网络的图片，把path当成本地路径
            Glide.with(mContext)
                    .load(path)
                    .placeholder(R.drawable.default_error)
                    .error(R.drawable.default_error)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(photoView);
        }
        photoView.setOnPhotoTapListener(mOnPhotoTapListener);
        container.addView(itemView);
        return itemView;
    }

    @Override
    public int getCount() {
        return pathList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    /**
     * 对象的新位置索引
     *
     * @param object
     * @return 默认实现假定项目永远不会改变位置，并且始终返回POSITION_UNCHANGED。重写成始终返回不存在POSITION_NONE
     */
    @Override
    public int getItemPosition(@NonNull Object object) {
        /*return super.getItemPosition(object);*/
        return POSITION_NONE;
    }
}
