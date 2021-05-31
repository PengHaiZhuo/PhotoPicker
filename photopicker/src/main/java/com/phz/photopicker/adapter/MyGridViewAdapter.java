package com.phz.photopicker.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.phz.photopicker.R;
import com.phz.photopicker.model.ImageModel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author haizhuo
 * @introduction 网格列表填充器
 */
public class MyGridViewAdapter extends BaseAdapter {
    private static final int TYPE_CAMERA = 0;
    private static final int TYPE_NORMAL = 1;

    private Context mContext;
    private LayoutInflater mInflater;
    private GridView.LayoutParams mItemLayoutParams;

    /**
     * 子view的类型数
     */
    private int viewTypeCount=2;
    /**
     * 几成几的网格
     */
    private int mItemSize;
    /**
     * 默认显示相机和右上角指示器（选择/可选数）
     */
    private boolean showCamera = true;
    private boolean showSelectIndicator = true;

    private List<ImageModel> imageModelList = new ArrayList<>();
    /**
     * 选中图片列表
     */
    private List<ImageModel> mSelectedList = new ArrayList<>();


    public MyGridViewAdapter(Context mContext, int mItemSize, boolean showCamera,boolean showSelectIndicator) {
        this.mContext = mContext;
        mInflater =  LayoutInflater.from(mContext);
        this.mItemSize = mItemSize;
        this.showCamera = showCamera;
        this.showSelectIndicator=showSelectIndicator;
        mItemLayoutParams = new GridView.LayoutParams(mItemSize, mItemSize);
    }

    /**
     * 设置数据集
     * @param list
     */
    public void setData(List<ImageModel> list) {
        mSelectedList.clear();

        if(list != null && list.size() > 0){
            imageModelList.clear();
            imageModelList.addAll(list);
        }else{
            imageModelList.clear();
        }
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return showCamera ? imageModelList.size() + 1 : imageModelList.size();
    }

    @Override
    public ImageModel getItem(int position) {
        if(showCamera){
            if(position == 0){
                return null;
            }
            return imageModelList.get(position - 1);
        }else{
            return imageModelList.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int type = getItemViewType(position);
        if(type == TYPE_CAMERA){
            convertView = mInflater.inflate(R.layout.item_camera, parent, false);
            convertView.setTag(null);
        }else if(type == TYPE_NORMAL){
            MyViewHolder holde;
            if(convertView == null){
                convertView = mInflater.inflate(R.layout.item_select_image, parent, false);
                holde = new MyViewHolder(convertView);
            }else{
                holde = (MyViewHolder) convertView.getTag();
                if(holde == null){
                    convertView = mInflater.inflate(R.layout.item_select_image, parent, false);
                    holde = new MyViewHolder(convertView);
                }
            }
            if(holde != null) {
                holde.bindData(getItem(position));
            }
        }

        /** Fixed View Size */
        GridView.LayoutParams lp = (GridView.LayoutParams) convertView.getLayoutParams();
        if(lp.height != mItemSize){
            convertView.setLayoutParams(mItemLayoutParams);
        }
        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        if(showCamera){
            return position == 0 ? TYPE_CAMERA : TYPE_NORMAL;
        }
        return TYPE_NORMAL;
    }

    @Override
    public int getViewTypeCount() {
        return viewTypeCount;
    }

    public boolean isShowCamera() {
        return showCamera;
    }

    public void setShowCamera(boolean showCamera) {
        this.showCamera = showCamera;
    }

    public boolean isShowSelectIndicator() {
        return showSelectIndicator;
    }

    public void setShowSelectIndicator(boolean showSelectIndicator) {
        this.showSelectIndicator = showSelectIndicator;
    }

    class MyViewHolder {
        ImageView ivImage;
        ImageView ivCheck;
        View mask;

        MyViewHolder(View view){
            ivImage =  view.findViewById(R.id.iv_image);
            ivCheck =  view.findViewById(R.id.iv_check);
            mask = view.findViewById(R.id.mask);
            view.setTag(this);
        }

        void bindData(final ImageModel data){
            if(data == null) {return;}
            // 处理单选和多选状态
            if(showSelectIndicator){
                ivCheck.setVisibility(View.VISIBLE);
                if(mSelectedList.contains(data)){
                    // 设置选中状态
                    ivCheck.setImageResource(R.drawable.btn_selected);
                    mask.setVisibility(View.VISIBLE);
                }else{
                    // 未选择
                    ivCheck.setImageResource(R.drawable.btn_unselected);
                    mask.setVisibility(View.GONE);
                }
            }else{
                ivCheck.setVisibility(View.GONE);
            }

            if(mItemSize > 0) {
                // 显示图片
                Glide.with(mContext)
                        .load(data.getPath())
                        .placeholder(R.drawable.default_error)
                        .error(R.drawable.default_error)
                        .override(mItemSize, mItemSize)
                        .centerCrop()
                        .into(ivImage);
            }
        }
    }


    /**
     * 选择某个图片，改变选择状态
     * @param data)
     */
    public void select(ImageModel data) {
        if(mSelectedList.contains(data)){
            mSelectedList.remove(data);
        }else{
            mSelectedList.add(data);
        }
        notifyDataSetChanged();
    }

    /**
     * 通过图片路径设置默认选择
     * @param resultList
     */
    public void setDefaultSelected(ArrayList<String> resultList) {
        mSelectedList.clear();
        for(String path : resultList){
            ImageModel bean = getImageByPath(path);
            if(bean != null){
                mSelectedList.add(bean);
            }
        }
        notifyDataSetChanged();
    }

    private ImageModel getImageByPath(String path){
        if(imageModelList != null && imageModelList.size() > 0){
            for(ImageModel bean : imageModelList){
                if(bean.getPath().equalsIgnoreCase(path)){
                    return bean;
                }
            }
        }
        return null;
    }


    /**
     * 重置每个Column的Size
     * @param columnWidth
     */
    public void setItemSize(int columnWidth) {

        if(mItemSize == columnWidth){
            return;
        }

        mItemSize = columnWidth;

        mItemLayoutParams = new GridView.LayoutParams(mItemSize, mItemSize);

        notifyDataSetChanged();
    }

}


