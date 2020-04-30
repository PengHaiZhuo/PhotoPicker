package com.phz.photopicker.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author haizhuo
 * @introduction 图片捕获管理工具
 */
public class ImageCaptureManager {
    private final static String CAPTURED_PHOTO_PATH_KEY = "mCurrentPhotoPath";
    private final static String yyyy_MM_dd_HH_mm_ss ="yyyyMMdd_HH时mm分ss";

    private Context mContext;
    /**
     * 当前图片路径，ImageCaptureManager传入Context对象初始化后调用createImageFile后才会有。
     */
    private String mCurrentPhotoPath;

    /**
     * 用于保存图片Uri
     * Android10因为文件系统的关系，使用uri来访问图片
     */
    private Uri mCurrentPhotoPathUri;


    public ImageCaptureManager(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * 创建图片文件
     * @return
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat(yyyy_MM_dd_HH_mm_ss).format(new Date());
        String imageFileName = "IMG_" + timeStamp;
        File storageDir = StorageUtil.getImageDir(mContext);
        File image = new File(storageDir, imageFileName + ".jpg");
        mCurrentPhotoPath = image.getAbsolutePath();
        mCurrentPhotoPathUri=CheckUtil.getUriForFile(mContext, image);
        return image;
    }

    /**
     * 吊起系统的拍照
     * @return
     * @throws IOException
     */
    public Intent dispatchTakePictureIntent() throws IOException {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(mContext.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = createImageFile();
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCurrentPhotoPathUri);
            }
        }
        return takePictureIntent;
    }


    public String getCurrentPhotoPath() {
        return mCurrentPhotoPath;
    }
    public Uri getCurrentPhotoPathUri() {
        return mCurrentPhotoPathUri;
    }


    /**
     * 保存状态的方法，需要在调用此类的dispatchTakePictureIntent拉起图片模块的宿主调用
     * @param savedInstanceState
     */
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null && mCurrentPhotoPath != null) {
            savedInstanceState.putString(CAPTURED_PHOTO_PATH_KEY, mCurrentPhotoPath);
        }
    }

    /**
     * 回复状态的方法，需要在调用此类的dispatchTakePictureIntent拉起图片模块的宿主调用
     * @param savedInstanceState
     */
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey(CAPTURED_PHOTO_PATH_KEY)) {
            mCurrentPhotoPath = savedInstanceState.getString(CAPTURED_PHOTO_PATH_KEY);
        }
    }

    /**
     * 简单压缩图片
     * @param filePath
     * @param targetPath 压缩后保存地址
     * @return
     */
    public static String compressImage(String filePath, String targetPath) {
        Bitmap image = getSmallBitmap(filePath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        int options = 90;
        // 循环判断如果压缩后图片是否大于1M,大于继续压缩
        while (baos.toByteArray().length / 1024 > 1024) {
            // 重置baos即清空baos
            baos.reset();
            // 这里压缩options%，把压缩后的数据存放到baos中
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);
            // 每次都减少10
            options -= 10;
        }
        // 把压缩后的数据baos存放到ByteArrayInputStream中
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        // 把ByteArrayInputStream数据生成图片
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);

        File outputFile = new File(targetPath);
        try {
            if (!outputFile.exists()) {
                outputFile.getParentFile().mkdirs();
            } else {
                outputFile.delete();
            }
            FileOutputStream out = new FileOutputStream(outputFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, out);
        } catch (Exception e) {
        }
        return outputFile.getPath();
    }

    public static Bitmap getSmallBitmap(String filePath) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        //只解析图片边沿，获取宽高
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        // 计算缩放比
        options.inSampleSize = calculateInSampleSize(options, 480, 800);
        // 完整解析图片返回bitmap
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }
}
