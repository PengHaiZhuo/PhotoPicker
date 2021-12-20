package com.phz.photopicker.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.phz.photopicker.R;
import com.phz.photopicker.adapter.PhotoPagerAdapter;
import com.phz.photopicker.config.ImagePickerConstant;
import com.phz.photopicker.view.MyViewPager;

import java.util.ArrayList;

/**
 * @author haizhuo
 * @introduction 预览图片
 */
public class PreViewImageActivity extends AppCompatActivity implements OnPhotoTapListener {

    private MyViewPager myViewPager;

    private Context mContext;
    /**
     * 选中图片path列表
     */
    private ArrayList<String> resultList = new ArrayList<>();

    /**
     * 适配器
     */
    private PhotoPagerAdapter mPageAdapter;

    /**
     * 当前item的页面
     */
    private int currentItem;

    /**
     * 是否显示删除菜单按钮
     * 默认显示
     */
    private boolean isShowDeleteMenu = true;

    /**
     * 闲置状态下保持在屏幕外的页面个数
     */
    private int offscreenPageLimit = 5;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);

        ArrayList<String> exportList = getIntent().getStringArrayListExtra(ImagePickerConstant.EXTRA_PHOTOS);
        currentItem = getIntent().getIntExtra(ImagePickerConstant.EXTRA_CURRENT_ITEM, 0);
        isShowDeleteMenu = getIntent().getBooleanExtra(ImagePickerConstant.EXTRA_IS_SHOW_DELETE, true);
        if (exportList != null) {
            resultList.addAll(exportList);
        }

        //加载控件
        myViewPager = findViewById(R.id.vp_photos);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mPageAdapter = new PhotoPagerAdapter(resultList, this, this);
        myViewPager.setAdapter(mPageAdapter);
        myViewPager.setCurrentItem(currentItem);
        myViewPager.setOffscreenPageLimit(offscreenPageLimit);

        myViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                getSupportActionBar().setTitle(getString(R.string.image_index, myViewPager.getCurrentItem() + 1, resultList.size()));
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        getSupportActionBar().setTitle(getString(R.string.image_index, myViewPager.getCurrentItem() + 1, resultList.size()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isShowDeleteMenu) {
            getMenuInflater().inflate(R.menu.menu_preview, menu);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        //返回上个界面，把数据传递回去
        Intent intent = new Intent();
        intent.putExtra(ImagePickerConstant.EXTRA_RESULT, resultList);
        setResult(RESULT_OK, intent);
        finish();
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //home键直接返回
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        // 删除当前照片
        if (item.getItemId() == R.id.action_discard) {
            final int index = myViewPager.getCurrentItem();
            final String deletedPath = resultList.get(index);
            if (resultList.size() <= 1) {
                // 最后一张照片弹出删除提示
                // show confirm dialog
                new AlertDialog.Builder(this)
                        .setTitle(R.string.confirm_to_delete)
                        .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                resultList.remove(index);
                                onBackPressed();
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .show();
            } else {
                resultList.remove(index);
                mPageAdapter.notifyDataSetChanged();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPhotoTap(ImageView view, float x, float y) {

    }
}
