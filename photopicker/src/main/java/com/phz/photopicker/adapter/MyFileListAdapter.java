package com.phz.photopicker.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.phz.photopicker.R;
import com.phz.photopicker.model.ImageFileModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * @author haizhuo
 * @introduction 可选择图片文件夹 列表适配器
 */
public class MyFileListAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater inflater;

    /**
     * 可供选择的图片文件夹列表
     */
    private List<ImageFileModel> list = new ArrayList<>();

    /**
     * 图片大小，长宽相等，单位为px
     * 默认为72dp转换成的像素大小
     */
    private int ImageSize;

    /**
     * 选中item的位置，默认选中第一个，显示为所有图片
     */
    private int selectIndex = 0;

    public MyFileListAdapter(Context context) {
        this.mContext = context;
        inflater = LayoutInflater.from(context);
        ImageSize = mContext.getResources().getDimensionPixelOffset(R.dimen.folder_cover_size);
    }


    /**
     * 设置数据集
     *
     * @param imageFileModelList
     */
    public void setData(List<ImageFileModel> imageFileModelList) {
        if (imageFileModelList != null && imageFileModelList.size() > 0) {
            list = imageFileModelList;
        } else {
            list.clear();
        }
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        //有个所有图片
        return list.size() + 1;
    }

    @Override
    public ImageFileModel getItem(int position) {
        if (position == 0) {
            return null;
        }
        return list.get(position - 1);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MyViewHolder holder;
        if(convertView == null){
            convertView = inflater.inflate(R.layout.item_folder, parent, false);
            holder = new MyViewHolder(convertView);
        }else{
            holder = (MyViewHolder) convertView.getTag();
        }
        if (holder != null) {
            if(position == 0){
                holder.tvName.setText(mContext.getResources().getString(R.string.all_image));
                int result = 0;
                //计算总计多少张图
                if(list != null && list.size()>0){
                    for (ImageFileModel f: list){
                        result += f.getList().size();
                    }
                }
                holder.tvSize.setText(result + "张");
                if(list.size()>0){
                    ImageFileModel f = list.get(0);

                    Glide.with(mContext)
                            .load(new File(f.getImageModel().getPath()))
                            .error(R.drawable.default_error)
                            .override(ImageSize, ImageSize)
                            .centerCrop()
                            .into(holder.ivCover);
                }
            }else {
                holder.bindData(getItem(position));
            }
            if(selectIndex == position){
                holder.ivIndicator.setVisibility(View.VISIBLE);
            }else{
                holder.ivIndicator.setVisibility(View.INVISIBLE);
            }
        }
        return convertView;
    }

    public int getSelectIndex() {
        return selectIndex;
    }

    public void setSelectIndex(int selectIndex) {
        if (this.selectIndex==selectIndex){
            return;
        }
        this.selectIndex = selectIndex;
        notifyDataSetChanged();
    }


    class MyViewHolder {

        ImageView ivCover;
        TextView tvName;
        TextView tvSize;
        ImageView ivIndicator;

        public MyViewHolder(View view) {
            ivCover=view.findViewById(R.id.cover);
            tvName=view.findViewById(R.id.name);
            tvSize=view.findViewById(R.id.size);
            ivIndicator=view.findViewById(R.id.indicator);
            view.setTag(this);
        }

        void bindData(ImageFileModel model){
            tvName.setText(model.getName());
            tvSize.setText(model.getList().size()+"张");
            // 显示图片
            Glide.with(mContext)
                    .load(new File(model.getImageModel().getPath()))
                    .placeholder(R.drawable.default_error)
                    .error(R.drawable.default_error)
                    .override(ImageSize, ImageSize)
                    .centerCrop()
                    .into(ivCover);
        }
    }
}
