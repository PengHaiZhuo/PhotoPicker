package com.phz.photopicker.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author haizhuo
 * @introduction 工具类，存储文件
 */
@SuppressWarnings("all")
public class StorageUtil {
    private static final String TAG = StorageUtil.class.getSimpleName();

    private StorageUtil() {
    }

    private enum TYPE {
        FILEDIR, FILEIMG, FILECACHE, FILEAUDIO
    }

    /**
     * 判断外存储是否挂载
     *
     * @return
     */
    public static boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    private static File getAppDir(Context context, TYPE typeSub) {
        File rootDir;
        /**
         * ---关于Android10的分区适配----
         * 1.从 Android 4.4 到 Android 10，可以通过 Environment.getExternalStorageDirectory() 以 File Api 的方式读写。
         * 2.通过Context访问自己的私有目录，不需要读写权限，不管系统是哪个版本或者是外部存储还是内部存储。
         * 3.注意uri和真实路径的区别，查看res/xml/file_paths.xml中的示例。
         * 4.通过Storage Access Framework的Api不需要权限，可以访问其他应用创建的文件。
         * 不重要的知识：
         *      ① 6.0开始需要申请存储权限；
         *      ② Android 10开始可以做分区适配，不想做的话在配置清单application节点添加声明（requestLegacyExternalStorage = true）。
         *      不过②这种方式在Android11失效了，谷歌给了开发者一个的版本的适应时间，然后逼着你适配
         */
        File mFile;
        if (isExternalStorageWritable()) {//有外部存储
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {//Android 11以前
                //mFile👉外部存储根目录/项目名称
                mFile = new File(Environment.getExternalStorageDirectory(), UsageUtil.getAppName(context));
            } else {//Android11及以后，返回私有目录files
                //mFile👉外部存储当前项目目录/files/项目名称
                mFile = new File(context.getExternalFilesDir(null), UsageUtil.getAppName(context));
            }
        } else {//没有外部存储
            //mFile👉内部存储当前项目目录/files
            mFile = new File(context.getFilesDir(), UsageUtil.getAppName(context));
        }
        switch (typeSub) {
            case FILEDIR:
                rootDir = new File(mFile, "fs");
                break;
            case FILEIMG:
                rootDir = new File(mFile, "images");
                break;
            case FILECACHE:
                rootDir = new File(mFile, "caches");
                break;
            case FILEAUDIO:
                rootDir = new File(mFile, "audios");
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + typeSub);
        }
        if (!rootDir.exists()) {
            rootDir.mkdirs();
        }
        return rootDir;
    }

    /**
     * 获取当前app文件存储目录
     *
     * @return
     */
    public static File getFileDir(Context context) {
        return getAppDir(context, TYPE.FILEDIR);
    }


    /**
     * 获取当前app图片文件存储目录
     *
     * @return
     */
    public static File getImageDir(Context context) {
        return getAppDir(context, TYPE.FILEIMG);
    }

    /**
     * 获取当前app缓存文件存储目录
     *
     * @return
     */
    public static File getCacheDir(Context context) {
        return getAppDir(context, TYPE.FILECACHE);
    }

    /**
     * 获取当前app音频文件存储目录
     *
     * @return
     */
    public static File getAudioDir(Context context) {
        return getAppDir(context, TYPE.FILEAUDIO);
    }

    /**
     * @param context
     * @return 真实路径"/storage/emulated/0/Android/data/包名/cache"
     */
    public static String getExternalCacheDir(Context context) {
        return context.getExternalCacheDir().getAbsolutePath();
    }

    /**
     * 创建一个文件夹, 存在则返回, 不存在则新建
     *
     * @param parentDirectory 父目录路径
     * @param directory       目录名
     * @return 文件，null代表失败
     */
    public static File getDirectory(String parentDirectory, String directory) {
        if (TextUtils.isEmpty(parentDirectory) || TextUtils.isEmpty(directory)) {
            return null;
        }
        File file = new File(parentDirectory, directory);
        boolean flag;
        if (!file.exists()) {
            flag = file.mkdir();
        } else {
            flag = true;
        }
        return flag ? file : null;
    }

    /**
     * 根据输入流，保存文件
     * 类型：直接覆盖文件
     *
     * @param file
     * @param is
     * @return
     */
    public static boolean writeFile(File file, InputStream is) {
        OutputStream os = null;
        try {
            //在每次调用的时候都会覆盖掉原来的数据
            os = new FileOutputStream(file);
            byte data[] = new byte[1024];
            int length = -1;
            while ((length = is.read(data)) != -1) {
                os.write(data, 0, length);
            }
            os.flush();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
            return false;
        } finally {
            closeStream(os);
            closeStream(is);
        }
    }

    /**
     * 删除文件或文件夹
     *
     * @param file
     */
    public static void deleteFile(File file) {
        try {
            if (file == null || !file.exists()) {
                return;
            }

            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null && files.length > 0) {
                    for (File f : files) {
                        if (f.exists()) {
                            if (f.isDirectory()) {
                                deleteFile(f);
                            } else {
                                f.deleteOnExit();
                                Log.d(TAG, "删除文件 " + f.getAbsolutePath());
                            }
                        }
                    }
                }
            } else {
                file.deleteOnExit();
                Log.d(TAG, "删除文件 " + file.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存文件
     *
     * @param inputStream  输入流，比如获取网络下载的字节流 ResponseBody.byteStream()
     * @param outputStream 输出流，比如FileOutputStream则是保存文件
     * @return
     */
    public static boolean saveFile(InputStream inputStream, OutputStream outputStream) {
        if (inputStream == null || outputStream == null) {
            return false;
        }
        try {
            try {
                byte[] buffer = new byte[1024 * 4];
                while (true) {
                    int read = inputStream.read(buffer);
                    if (read == -1) {
                        break;
                    }
                    outputStream.write(buffer, 0, read);
                }
                outputStream.flush();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } finally {
                inputStream.close();
                outputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 关闭流
     *
     * @param closeable
     */
    public static void closeStream(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                throw new RuntimeException("关闭流失败!", e);
            }
        }
    }


    /**
     * 通过uri拿到图片文件真实路径
     *
     * @param context
     * @param uri
     * @return
     * @deprecated Android10开始，MediaStore.Images.ImageColumns.DATA被标记为过期
     */
    @Deprecated
    public static String getImgRealFilePath(final Context context, final Uri uri) {
        if (null == uri) {
            return null;
        }
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            final Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    final int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }
}
