package com.phz.imagepicker.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.phz.imagepicker.R;
import com.phz.imagepicker.adapter.BitmapAdapter;
import com.phz.imagepicker.config.Constant;
import com.phz.photopicker.config.ImagePickerConstant;
import com.phz.photopicker.config.SelectMode;
import com.phz.photopicker.intent.PickImageIntent;
import com.phz.photopicker.intent.PreViewImageIntent;
import com.phz.photopicker.util.UsageUtil;
import com.phz.photopicker.view.MyGridView;

import java.util.ArrayList;

/**
 * @author haizhuo
 */
public class MainActivity extends AppCompatActivity {

    private MyGridView myGridView;

    private Context mContext;

    private static final int ACTION_REQUEST_PERMISSIONS = 0x001;
    /**
     * 需要的权限
     * 本来读写权限获取其中一只另外一个就也获取了（一个权限组的），随着安卓系统更新，只获取读不行了。
     */
    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE};

    /**
     * 没有权限时，点击GridView时点击的位置和路径，这2个默认值是0和{@link Constant.PLUS}
     */
    private int gridViewItemClickPosition;
    private String gridViewItemClickPath;

    /**
     * 允许上传照片最大数量
     */
    private static final int INT_MAXSIZE_IMG = 9;

    /**
     * 图片路径，和graidview的填充器有关 (可能包含plus加号)
     */
    private ArrayList<String> imagePathsList = new ArrayList<>();
    /**
     * 图片路径，不包含plus
     */
    private ArrayList<String> imagePathsListNew = new ArrayList<>();
    /**
     * 和gridView的填充器有关的填充器
     */
    private BitmapAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        myGridView = findViewById(R.id.myGridView);
        initData();
    }

    private void initData() {
        int cols = UsageUtil.getNumColumn(this);
        myGridView.setNumColumns(cols);
        imagePathsList.add(Constant.PLUS);
        adapter = new BitmapAdapter(imagePathsList, this);
        myGridView.setAdapter(adapter);
        myGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!MainActivity.this.checkPermissions(NEEDED_PERMISSIONS)) {
                    gridViewItemClickPosition = position;
                    gridViewItemClickPath = (String) parent.getItemAtPosition(position);
                    /**
                     * 请求一串权限
                     */
                    ActivityCompat.requestPermissions(MainActivity.this, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
                } else {
                    String string = (String) parent.getItemAtPosition(position);
                    MainActivity.this.toPickPhoto(position, string);
                }

            }
        });
    }

    /**
     * 跳转到图片选择器
     */
    private void toPickPhoto(int position, String string) {
        if (Constant.PLUS.equals(string)) {
            PickImageIntent intent = new PickImageIntent(mContext);
            //设置为多选模式
            intent.setSelectModel(SelectMode.MULTI);
            // 是否拍照
            intent.setIsShowCamera(true);
            //设置最多选择照片数量
            if (imagePathsList.size() > 0 && imagePathsList.size() < (INT_MAXSIZE_IMG + 1)) {
                // 最多选择照片数量
                intent.setSelectedCount(INT_MAXSIZE_IMG + 1 - imagePathsList.size());
            } else {
                intent.setSelectedCount(0);
            }
            // 已选中的照片地址， 用于回显选中状态
            /*intent.setSelectedPaths(imagePathsList);
            startActivityForResult(intent, REQUEST_CAMERA_CODE);*/
        } else {
            PreViewImageIntent intent = new PreViewImageIntent(mContext);
            intent.setCurrentItem(position);
            if (imagePathsList.contains(Constant.PLUS)) {
                imagePathsList.remove(Constant.PLUS);
            }
            intent.setPhotoPaths(imagePathsList);
            startActivityForResult(intent, REQUEST_PREVIEW_CODE);
        }
    }

    private static final int REQUEST_CAMERA_CODE = 10;
    private static final int REQUEST_PREVIEW_CODE = 20;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CAMERA_CODE:
                    if (data != null) {
                        ArrayList<String> list = data.getStringArrayListExtra(ImagePickerConstant.EXTRA_RESULT);
                        updateGridView(list);
                    }
                    break;
                case REQUEST_PREVIEW_CODE:
                    if (data != null) {
                        ArrayList<String> ListExtra = data.getStringArrayListExtra(ImagePickerConstant.EXTRA_RESULT);
                        if (imagePathsList != null) {
                            imagePathsList.clear();
                        }
                        imagePathsList.addAll(ListExtra);
                        if (imagePathsList.size() < INT_MAXSIZE_IMG) {
                            imagePathsList.add(Constant.PLUS);
                        }
                        adapter = new BitmapAdapter(imagePathsList, mContext);
                        myGridView.setAdapter(adapter);
                    }
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ACTION_REQUEST_PERMISSIONS) {
            boolean isAllGranted = true;
            for (int grantResult : grantResults) {
                isAllGranted &= (grantResult == PackageManager.PERMISSION_GRANTED);
            }
            if (isAllGranted) {
                //全部获取了
                toPickPhoto(gridViewItemClickPosition, gridViewItemClickPath);
            } else {
                //至少有一个被拒绝
                Toast.makeText(mContext, getString(R.string.notify_image_permission), Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * 遍历判断权限是否都有请求
     * @param neededPermissions
     * @return
     */
    private boolean checkPermissions(String[] neededPermissions) {
        if (neededPermissions == null || neededPermissions.length == 0) {
            return true;
        }
        boolean allGranted = true;
        for (String neededPermission : neededPermissions) {
            allGranted &= ContextCompat.checkSelfPermission(this, neededPermission) == PackageManager.PERMISSION_GRANTED;
        }
        return allGranted;
    }


    /**
     * 更新界面
     *
     * @param list 选择照片的路径列表
     */
    private void updateGridView(ArrayList<String> list) {
        if (imagePathsList.contains(Constant.PLUS)) {
            imagePathsList.remove(Constant.PLUS);
        }
        imagePathsList.addAll(list);
        /** 小于INT_MAXSIZE_IMG时显示添加图片item(也就是plus)*/
        if (imagePathsList.size() < INT_MAXSIZE_IMG) {
            imagePathsList.add(Constant.PLUS);
        }
        adapter = new BitmapAdapter(imagePathsList, mContext);
        myGridView.setAdapter(adapter);
    }
}
