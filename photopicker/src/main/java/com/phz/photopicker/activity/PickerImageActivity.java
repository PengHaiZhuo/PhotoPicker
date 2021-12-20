package com.phz.photopicker.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.phz.photopicker.R;
import com.phz.photopicker.adapter.MyFileListAdapter;
import com.phz.photopicker.adapter.MyGridViewAdapter;
import com.phz.photopicker.config.ImageConfig;
import com.phz.photopicker.config.ImagePickerConstant;
import com.phz.photopicker.intent.PreViewImageIntent;
import com.phz.photopicker.model.ImageFileModel;
import com.phz.photopicker.model.ImageModel;
import com.phz.photopicker.util.ImageCaptureManager;
import com.phz.photopicker.util.UsageUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author haizhuo
 * @introduction 图片选择界面
 */
public class PickerImageActivity extends AppCompatActivity {
    private Context mContext;

    private MenuItem menuDoneItem;
    private GridView mGridView;
    private View mPopupAnchorView;
    private Button btnAlbum;
    private Button btnPreview;
    /**
     * 照片文件夹弹出框
     */
    private ListPopupWindow mFolderPopupWindow;
    /**
     * 默认加载所有图片
     */
    private static final int LOADER_ALL = 0;
    private static final int LOADER_CATEGORY = 1;

    /**
     * 最大选择照片数量
     */
    private int maxImageSize;

    /**
     * 文件列表是否初始化成功
     */
    private boolean isMyFileListGenerate = false;

    /**
     * 是否显示相机
     */
    private boolean isShowCamera = false;

    /**
     * 照片配置
     */
    private ImageConfig imageConfig;

    /**
     * 图片管理工具
     */
    private ImageCaptureManager imageCaptureManager;

    /**
     * 文件列表适配器
     */
    private MyFileListAdapter myFileListAdapter;

    /**
     * 表格适配器
     */
    private MyGridViewAdapter myGridViewAdapter;

    /**
     * 结果数据
     */
    private ArrayList<String> resultList = new ArrayList<>();

    /**
     * 可选图片文件夹数据
     */
    private ArrayList<ImageFileModel> resultFolderList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picker_image);
        mContext = this;
        initView();
        initData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_picker, menu);
        menuDoneItem = menu.findItem(R.id.action_picker_done);
        menuDoneItem.setVisible(false);
        refreshActionStatus();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        if (item.getItemId() == R.id.action_picker_done) {
            // 关闭界面并把图片路径列表放入Intent回传
            Intent intent = new Intent();
            intent.putStringArrayListExtra(ImagePickerConstant.EXTRA_RESULT, resultList);
            setResult(RESULT_OK, intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initData() {
        imageCaptureManager = new ImageCaptureManager(mContext);
        imageConfig = getIntent().getParcelableExtra(ImagePickerConstant.EXTRA_IMAGE_CONFIG);
        // 首次加载所有图片
        LoaderManager.getInstance(this).initLoader(LOADER_ALL, null, mLoaderCallback);
        //最大选择照片数量
        maxImageSize = getIntent().getIntExtra(ImagePickerConstant.EXTRA_SELECT_COUNT, ImagePickerConstant.DEFAULT_MAX_TOTAL);
        // 图片选择模式 单选or多选
        final int mode = getIntent().getExtras().getInt(ImagePickerConstant.EXTRA_SELECT_MODE, ImagePickerConstant.MODE_SINGLE);
        isShowCamera = getIntent().getBooleanExtra(ImagePickerConstant.EXTRA_SHOW_CAMERA, false);

        boolean isShowSelectIndicator = mode == ImagePickerConstant.MODE_MULTI;
        myGridViewAdapter = new MyGridViewAdapter(mContext, UsageUtil.getItemImageWidth(mContext), isShowCamera, isShowSelectIndicator);
        mGridView.setAdapter(myGridViewAdapter);

        myFileListAdapter = new MyFileListAdapter(mContext);

        mGridView.setOnItemClickListener((parent, view, position, id) -> {
            if (myGridViewAdapter.isShowCamera()) {
                // 如果显示照相机，则第一个Grid显示为照相机，处理特殊逻辑
                if (position == 0) {
                    if (mode == ImagePickerConstant.MODE_MULTI) {
                        // 判断选择数量问题
                        if (maxImageSize == resultList.size()) {
                            Toast.makeText(mContext, R.string.msg_amount_limit, Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    /**选择相机*/
                    try {
                        Intent intent = imageCaptureManager.dispatchTakePictureIntent();
                        startActivityForResult(intent, ImagePickerConstant.REQUEST_TAKE_PHOTO);
                    } catch (IOException e) {
                        Toast.makeText(mContext, R.string.msg_no_camera, Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                } else {
                    // 正常操作
                    ImageModel model = (ImageModel) parent.getAdapter().getItem(position);
                    selectImageFromGrid(model, mode);
                }
            } else {
                // 正常操作
                ImageModel model = (ImageModel) parent.getAdapter().getItem(position);
                selectImageFromGrid(model, mode);
            }
        });

        //点击相册列表按钮
        btnAlbum.setOnClickListener(v -> {
            if (mFolderPopupWindow == null) {
                PickerImageActivity.this.createPopupFolderList();
            }

            if (mFolderPopupWindow.isShowing()) {
                mFolderPopupWindow.dismiss();
            } else {
                mFolderPopupWindow.show();
                int index = myFileListAdapter.getSelectIndex();
                index = index == 0 ? index : index - 1;
                mFolderPopupWindow.getListView().setSelection(index);
            }
        });

        // 点击预览按钮
        btnPreview.setOnClickListener(v -> {
            PreViewImageIntent intent = new PreViewImageIntent(mContext);
            intent.setCurrentItem(0);
            intent.setPhotoPaths(resultList);
            PickerImageActivity.this.startActivityForResult(intent, ImagePickerConstant.REQUEST_PREVIEW);
        });
    }

    private void initView() {
        getSupportActionBar().setTitle(getResources().getString(R.string.image));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mGridView = findViewById(R.id.grid);
        mGridView.setNumColumns(UsageUtil.getNumColumn(mContext));

        mPopupAnchorView = findViewById(R.id.photo_picker_footer);
        btnAlbum = findViewById(R.id.btnAlbum);
        btnPreview = findViewById(R.id.btnPreview);
    }

    /**
     * 加载管理器回调
     */
    private LoaderManager.LoaderCallbacks<Cursor> mLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {

        private final String[] IMAGE_PROJECTION = {
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media._ID};

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {

            // 根据图片设置参数新增扫描条件
            StringBuilder selectionArgs = new StringBuilder();

            if (imageConfig != null) {
                if (imageConfig.minWidth != 0) {
                    selectionArgs.append(MediaStore.Images.Media.WIDTH + " >= " + imageConfig.minWidth);
                }

                final String str = "".equals(selectionArgs.toString()) ? "" : " and ";
                if (imageConfig.minHeight != 0) {
                    selectionArgs.append(str);
                    selectionArgs.append(MediaStore.Images.Media.HEIGHT + " >= " + imageConfig.minHeight);
                }

                if (imageConfig.minSize != 0f) {
                    selectionArgs.append(str);
                    selectionArgs.append(MediaStore.Images.Media.SIZE + " >= " + imageConfig.minSize);
                }

                if (imageConfig.mimeType != null) {
                    selectionArgs.append(" and (");
                    for (int i = 0, len = imageConfig.mimeType.length; i < len; i++) {
                        if (i != 0) {
                            selectionArgs.append(" or ");
                        }
                        selectionArgs.append(MediaStore.Images.Media.MIME_TYPE + " = '" + imageConfig.mimeType[i] + "'");
                    }
                    selectionArgs.append(")");
                }
            }

            if (id == LOADER_ALL) {
                CursorLoader cursorLoader = new CursorLoader(mContext,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION,
                        selectionArgs.toString(), null, IMAGE_PROJECTION[2] + " DESC");
                return cursorLoader;
            } else if (id == LOADER_CATEGORY) {
                String selectionStr = selectionArgs.toString();
                if (!"".equals(selectionStr)) {
                    selectionStr += " and" + selectionStr;
                }
                CursorLoader cursorLoader = new CursorLoader(mContext,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION,
                        IMAGE_PROJECTION[0] + " like '%" + args.getString("path") + "%'" + selectionStr, null,
                        IMAGE_PROJECTION[2] + " DESC");
                return cursorLoader;
            }

            return null;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            if (cursor != null) {
                List<ImageModel> imageModelList = new ArrayList<>();
                int count = cursor.getCount();
                if (count > 0) {
                    cursor.moveToFirst();
                    do {
                        int id = cursor.getInt(cursor.getColumnIndexOrThrow(IMAGE_PROJECTION[3]));
                        String path;
                        //如果是Video，记得换成MediaStore.Videos
                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                            //Android 11开始 公有目录只能通过Content Uri + id的方式访问，以前的File路径全部无效
                            //通过内容提供者扫描也扫不到很多图片，因为没有权限，只有DCIM、Download等目录下图片是能扫描出来的
                            path = MediaStore.Images.Media//示例：content://media/external/images/media/33
                                    .EXTERNAL_CONTENT_URI
                                    .buildUpon()
                                    .appendPath(String.valueOf(id)).build().toString();
                        } else {
                            path = cursor.getString(cursor.getColumnIndexOrThrow(IMAGE_PROJECTION[0]));
                        }
                        String name = cursor.getString(cursor.getColumnIndexOrThrow(IMAGE_PROJECTION[1]));
                        long dateTime = cursor.getLong(cursor.getColumnIndexOrThrow(IMAGE_PROJECTION[2]));

                        //获取数据后添加到resultFolderList表
                        ImageModel imageModel = new ImageModel(path, name, dateTime);
                        imageModelList.add(imageModel);
                        if (!isMyFileListGenerate) {
                            // 获取文件夹名称
                            File imageFile = new File(path);
                            File folderFile = imageFile.getParentFile();
                            ImageFileModel imageFileModel = new ImageFileModel();
                            imageFileModel.setName(folderFile.getName());
                            imageFileModel.setPath(folderFile.getAbsolutePath());
                            imageFileModel.setImageModel(imageModel);
                            if (!resultFolderList.contains(imageFileModel)) {
                                List<ImageModel> list = new ArrayList<>();
                                list.add(imageModel);
                                imageFileModel.setList(list);
                                resultFolderList.add(imageFileModel);
                            } else {
                                // 更新
                                ImageFileModel model = resultFolderList.get(resultFolderList.indexOf(imageFileModel));
                                model.getList().add(imageModel);
                            }
                        }

                    } while (cursor.moveToNext());

                    myGridViewAdapter.setData(imageModelList);
                    // 设定默认选择
                    if (resultList != null && resultList.size() > 0) {
                        myGridViewAdapter.setDefaultSelected(resultList);
                    }
                    myFileListAdapter.setData(resultFolderList);
                    isMyFileListGenerate = true;
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    };

    /**
     * 初始化文件选择弹出框
     */
    private void createPopupFolderList() {

        mFolderPopupWindow = new ListPopupWindow(mContext);
        mFolderPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mFolderPopupWindow.setAdapter(myFileListAdapter);
        mFolderPopupWindow.setContentWidth(ListPopupWindow.MATCH_PARENT);
        mFolderPopupWindow.setWidth(ListPopupWindow.MATCH_PARENT);

        // 计算ListPopupWindow内容的高度(忽略mPopupAnchorView.height)，R.layout.item_foloer
        int folderItemViewHeight =
                // 图片高度
                getResources().getDimensionPixelOffset(R.dimen.folder_cover_size) +
                        // Padding Top
                        getResources().getDimensionPixelOffset(R.dimen.folder_padding) +
                        // Padding Bottom
                        getResources().getDimensionPixelOffset(R.dimen.folder_padding);
        int folderViewHeight = myFileListAdapter.getCount() * folderItemViewHeight;

        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        if (folderViewHeight >= screenHeight) {
            mFolderPopupWindow.setHeight(Math.round(screenHeight * 0.6f));
        } else {
            mFolderPopupWindow.setHeight(ListPopupWindow.WRAP_CONTENT);
        }

        mFolderPopupWindow.setAnchorView(mPopupAnchorView);
        mFolderPopupWindow.setModal(true);
        mFolderPopupWindow.setAnimationStyle(R.style.Animation_AppCompat_DropDownUp);
        mFolderPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                myFileListAdapter.setSelectIndex(position);

                final int index = position;
                final AdapterView v = parent;

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mFolderPopupWindow.dismiss();

                        if (index == 0) {
                            LoaderManager.getInstance(PickerImageActivity.this).restartLoader(LOADER_ALL, null, mLoaderCallback);
                            btnAlbum.setText(R.string.all_image);
                            myGridViewAdapter.setShowCamera(isShowCamera);
                        } else {
                            ImageFileModel folder = (ImageFileModel) v.getAdapter().getItem(index);
                            if (null != folder) {
                                myGridViewAdapter.setData(folder.getList());
                                btnAlbum.setText(folder.getName());
                                // 设定默认选择
                                if (resultList != null && resultList.size() > 0) {
                                    myGridViewAdapter.setDefaultSelected(resultList);
                                }
                            }
                            myGridViewAdapter.setShowCamera(false);
                        }

                        // 滑动到最初始位置
                        mGridView.smoothScrollToPosition(0);
                    }
                }, 100);
            }
        });
    }


    /**
     * 选择图片操作
     */
    private void selectImageFromGrid(ImageModel imageModel, int mode) {
        if (imageModel != null) {
            // 多选模式
            if (mode == ImagePickerConstant.MODE_MULTI) {
                if (resultList.contains(imageModel.getPath())) {
                    resultList.remove(imageModel.getPath());
                    onImageUnselected(imageModel.getPath());
                } else {
                    // 判断选择数量问题
                    if (maxImageSize == resultList.size()) {
                        Toast.makeText(mContext, R.string.msg_amount_limit, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    resultList.add(imageModel.getPath());
                    onImageSelected(imageModel.getPath());
                }
                myGridViewAdapter.select(imageModel);
            } else if (mode == ImagePickerConstant.MODE_SINGLE) {
                // 单选模式
                onSingleImageSelected(imageModel.getPath());
            }
        }
    }

    public void onSingleImageSelected(String path) {
        Intent data = new Intent();
        resultList.add(path);
        data.putStringArrayListExtra(ImagePickerConstant.EXTRA_RESULT, resultList);
        setResult(RESULT_OK, data);
        finish();
    }

    public void onImageSelected(String path) {
        if (!resultList.contains(path)) {
            resultList.add(path);
        }
        refreshActionStatus();
    }

    public void onImageUnselected(String path) {
        if (resultList.contains(path)) {
            resultList.remove(path);
        }
        refreshActionStatus();
    }

    /**
     * 刷新操作按钮状态
     */
    private void refreshActionStatus() {
        String text = getString(R.string.done_with_count, resultList.size(), maxImageSize);
        menuDoneItem.setTitle(text);
        boolean hasSelected = resultList.size() > 0;
        menuDoneItem.setVisible(hasSelected);
        btnPreview.setEnabled(hasSelected);
        if (hasSelected) {
            btnPreview.setText(getResources().getString(R.string.preview) + "(" + (resultList.size()) + ")");
        } else {
            btnPreview.setText(getResources().getString(R.string.preview));
        }
    }

    /**
     * 状态发生改变时
     *
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        Log.d("PickerImage", "onConfigurationChanged");

        // 重置列数
        mGridView.setNumColumns(UsageUtil.getNumColumn(mContext));
        // 重置Item宽度
        myGridViewAdapter.setItemSize(UsageUtil.getItemImageWidth(mContext));

        if (mFolderPopupWindow != null) {
            if (mFolderPopupWindow.isShowing()) {
                mFolderPopupWindow.dismiss();
            }

            // 重置PopupWindow高度
            int screenHeigh = getResources().getDisplayMetrics().heightPixels;
            mFolderPopupWindow.setHeight(Math.round(screenHeigh * 0.6f));
        }
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                /**相机拍照完成后，返回图片路径*/
                case ImagePickerConstant.REQUEST_TAKE_PHOTO:
                    if (imageCaptureManager.getCurrentPhotoPath() != null) {
                        //通知刷新图库
                        refreshGallery();
                        resultList.add(imageCaptureManager.getCurrentPhotoPath());
                    }
                    // 关闭界面并把图片路径列表放入Intent回传
                    Intent intent = new Intent();
                    intent.putStringArrayListExtra(ImagePickerConstant.EXTRA_RESULT, resultList);
                    setResult(RESULT_OK, intent);
                    finish();
                    break;
                /**预览照片*/
                case ImagePickerConstant.REQUEST_PREVIEW:
                    ArrayList<String> pathArr = data.getStringArrayListExtra(ImagePickerConstant.EXTRA_RESULT);
                    // 刷新页面
                    if (pathArr != null && pathArr.size() != resultList.size()) {
                        resultList = pathArr;
                        refreshActionStatus();
                        myGridViewAdapter.setDefaultSelected(resultList);
                    }
                    break;
            }
        }
    }

    /**
     * 通知刷新图库，方法有很多，api29之前普遍是使用广播的
     */
    private void refreshGallery() {
        //发通知刷新相册

        File file = new File(imageCaptureManager.getCurrentPhotoPath());
        MediaScannerConnection.scanFile(mContext,
                new String[]{file.toString()},
                null, null);
        /* //ACTION_MEDIA_SCANNER_SCAN_FILE在api29被标注为过时
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(imageCaptureManager.getCurrentPhotoPath());
        Uri contentUri = CheckUtil.getUriForFile(mContext, f);
        mediaScanIntent.setData(contentUri);
        mContext.sendBroadcast(mediaScanIntent);*/
    }
}
