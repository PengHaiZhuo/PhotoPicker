# PhotoPicker [![1.0.0](https://jitpack.io/v/PengHaiZhuo/PhotoPicker.svg)](https://jitpack.io/#PengHaiZhuo/PhotoPicker)
图片选择器


#### 工程目录下gradle文件添加jetpack依赖：

```java
allprojects {
    repositories {
        google()
        jcenter()
        maven { url "https://jitpack.io" }
    }
}
```
#### 添加权限AndroidManifest.xml

```java
 	<uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.CAMERA" />
```


#### sample使用了第三方库九宫格，添加依赖如下（本库也支持单选，请根据自己情况而定）

```java
dependencies {
	//此处省略...
	
    //9宫格的图片选择
    implementation 'com.jaeger.ninegridimageview:library:1.1.1'
	
	//本地库依赖
    //implementation project(':photopicker')
    //远程依赖
    implementation 'com.github.PengHaiZhuo:PhotoPicker:1.0.0'
}
```

#### 使用

```java
    /**
     * 当前选择的图片路径
     */
    private ArrayList<String> imagePathsList = new ArrayList<>();
	/**
     * 允许上传照片最大数量
     */
    private static final int INT_MAXSIZE_IMG = 9;
    
    /**
     * 跳转到图片选择器
     * @param position：预览图片需要传当前图片位置，配合imagePathsList使用
     * @param isPick 是否是添加图片
     */
    private void toPickPhoto(int position,boolean isPick){
        if (isPick) {
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
            /*// 已选中的照片地址，用于回显选中状态
            intent.setSelectedPaths(imagePathsList);*/
            startActivityForResult(intent, REQUEST_CAMERA_CODE);
        } else {
        	//预览图片
            PreViewImageIntent intent = new PreViewImageIntent(mContext);
            intent.setCurrentItem(position);
            intent.setPhotoPaths(imagePathsList);
            startActivityForResult(intent, REQUEST_PREVIEW_CODE);
        }
    }
```


#### 重写onActivityResult方法，获取选择的照片路径列表

```java
	private static final int REQUEST_CAMERA_CODE = 10;
    private static final int REQUEST_PREVIEW_CODE = 20;
     @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==RESULT_OK){
            switch (requestCode) {
                case REQUEST_CAMERA_CODE:
                    if (data != null) {
                        ArrayList<String> list = data.getStringArrayListExtra(ImagePickerConstract.EXTRA_RESULT);
                        //todo 显示图片
                    }
                    break;
                case REQUEST_PREVIEW_CODE:
                    if (data != null) {
                        ArrayList<String> ListExtra = data.getStringArrayListExtra(ImagePickerConstract.EXTRA_RESULT);
                        if (imagePathsList != null) {
                            imagePathsList.clear();
                        }
                        imagePathsList.addAll(ListExtra);
                        //todo 预览里可能删除了图片，所以需要更新显示的图片
                    }
                    break;
            }
        }
    }
```

## 部分截图
#### 图片选择

![可以选择文件夹中文件也可以拍照](https://img-blog.csdnimg.cn/20200430120612533.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzE2NjkyNTE3,size_16,color_FFFFFF,t_70)




#### 预览
![使用了PhotoView控件，可放大缩小查看图片](https://img-blog.csdnimg.cn/20200430120156179.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzE2NjkyNTE3,size_16,color_FFFFFF,t_70)



#### 9宫格
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200430120032606.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzE2NjkyNTE3,size_16,color_FFFFFF,t_70)
