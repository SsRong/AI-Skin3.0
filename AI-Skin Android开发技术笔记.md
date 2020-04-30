# AI-Skin Android开发技术笔记

********************

## 1、界面及控件布局设计

​	**1.1、主界面UI**

<div align="center"><img src="E:\毕业设计\实验和文章\图&amp;表\APP\主界面UI\drawable-xxxhdpi\主界面UI.png" alt="主界面UI" style="zoom:25%;" /></div>

​	**activity_main.xml文件部分代码**

```xml
<Button
        android:id="@+id/takephoto"
        android:onClick="takePhoto"
        android:layout_width="102dp"
        android:layout_height="103dp"
        android:background="#00000000" />

<Button
        android:id="@+id/loadImage"
        android:layout_width="102dp"
        android:layout_height="103dp"
        android:background="#00000000" />
```

​	**1.2、预测页面UI**

<div align = "center"><img src="E:\毕业设计\实验和文章\图&amp;表\APP\预测界面UI\drawable-xxxhdpi\预测页面UI.png" alt="预测界面UI" style="zoom:25%;" /></div>

​		**activity_predict_result.xml文件部分代码**

```xml
<Button
        android:id="@+id/takephoto"
        android:onClick="takePhoto"
        android:layout_width="102dp"
        android:layout_height="103dp"
        android:background="#00000000" />

<Button
        android:id="@+id/loadImage"
        android:layout_width="102dp"
        android:layout_height="103dp"
        android:background="#00000000" />
```

```xml
    <ImageView
        android:id="@+id/predictImageView"
        android:layout_width="170dp"
        android:layout_height="132dp"
        android:layout_gravity="center" />

    <TextView
        android:id="@+id/suggestionText"
        android:layout_width="225dp"
        android:layout_height="100dp"
        android:text="您的皮肤出现较严重的问题"
        android:textColor="@color/colorBlack"
        android:textSize="16dp" />

    <TextView
        android:id="@+id/dangerLevel"
        android:layout_width="100dp"
        android:layout_height="30dp"
        android:text="0.99"
        android:textSize="25dp" />
```

##  2、调用相机拍照或从相册中选取照片并显示照片

​	在调用系统相机拍照和调用系统相册的时候，首先需要获得系统权限，分别在AndroidMainfest.xml和触发“takePhoto”按钮的代码里进行权限申请；此外我们还需要一个 .../res/xml/provider_paths.xml 来辅助我们进行配置,并在AndroidMainfest.xml中声明。然后再使用Intent事件来启动相机和开启相册 。在获取图片后，需要将图片保存为Bitmap类对象进行预先处理。==provider_paths.xml文件的作用等我项目跟完后再来详细了解一下==
​	**！！！注意** ，可能因为android系统版本的原因，我的app在第一次打开相机功能的时候会出现闪退。这时需要去手动到系统设置里打开应用的相机拍照的权限。==具体的原因的话等我把整个项目走完再来继续解决==

​	**AndroidMainfest.xml中配置**

```xml
<!--获取写入和读取权限-->
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />

<-配置provider，辅助打开系统相机-->
<application
     ......
		<provider
            android:name="androidx.core.content.FileProvider"
            android:authorities="com.example.uidesign02.provider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/provider_paths" />
        </provider>
</application>
```

​	**provider_paths.xml代码**

```xml
<?xml version="1.0" encoding="utf-8"?>
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <external-path
        name="external_files"
        path="."/>
</paths>
```

​	**MainActivity.java中获取权限以及调用相机和系统相册的部分代码**

```java
public class MainActivity extends Activity {
    //定义静态成员变量
    private static final int EXTERNAL_STORAGE_REQUEST_RESULT = 1;
    private static final int START_CAMERA_ACTIVITY = 0;
    private static final int IMAGE_PICK_CODE = 1000;
    private static final int PERMISSION_CODE = 1001;
    private String imageFileLocation = "";
    
     @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//1、点击监听：加载loadImage按钮功能；当点击‘图库’时，访问图库选择图像
        Button loadImage = findViewById(R.id.loadImage);
        loadImage.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View arg0){
                //检查运行时的权限
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                        //权限未被赋予，向系统发出请求
                        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                        //跳出弹框请求赋予权限
                        requestPermissions(permissions,PERMISSION_CODE);
                    }
                    else {
                        //权限已被赋予
                        pickImageFromGallery();
                    }
                }else {
                    //系统版本低于marshmallow，不用权限申请
                    pickImageFromGallery();
                }
            }
        });
    }
    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_CODE);
    }
    private void transferToPredictResultActivity(String suggestion,String dangerLevel) {
        Intent intent =new Intent(MainActivity.this, PredictResultActivity.class);
        intent.putExtra("suggestion",suggestion);
        intent.putExtra("dangerLevel",dangerLevel);
        startActivity(intent);
    }
//2、点击拍照按钮，触发拍照事件。此处会出现一些权限问题，要手动去解决
    public void takePhoto(View view) {
        Intent cameraIntent = new Intent();
        cameraIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);

        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException e){
            e.printStackTrace();
        }
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(this,"com.example.uidesign02.provider",photoFile));
        cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Log.d(TAG, "添加权限。");
        startActivityForResult(cameraIntent,START_CAMERA_ACTIVITY);
    }
//3、搭配拍照功能，每次拍下的照片要写入到本地相册，实现createImageFile()方法
    File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "skinImage_"+ timeStamp + "_";
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName,".jpg",storageDirectory);
        imageFileLocation = image.getAbsolutePath();
        return image;
    }
//4、调用Bitmap对象类并对已经转为bitmap的照片进行预处理
    public Bitmap rotateImage(Bitmap bitmap) {
        //查询ExifInterface图形库的介绍
        ExifInterface exifInterface = null;
        //为路径下的图片构造一个ExifInterface图形库放进去
        try {
            exifInterface = new ExifInterface(imageFileLocation);
        }catch (IOException e){
            e.printStackTrace();
        }

        //用exifinterface获取图片的原始方向
        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        Matrix matrix = new Matrix();//矩阵
        //相应的旋转图像的矩阵
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(270);
                break;
        }
        //旋转 bitmap，并且展示它 ，查bitmap
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
        return rotatedBitmap;
    }
//5、拍完照片或选定图片后向应用程序返回响应值
    @SuppressLint("MissingSuperCall")
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        if(requestCode == START_CAMERA_ACTIVITY && resultCode == RESULT_OK) {
            //Toast.makeText(this,"拍照成功",Toast.LENGTH_SHORT).show();
            Bitmap photoCapturedBitmap = BitmapFactory.decodeFile(imageFileLocation);
            BitmapTransfer.getInstance().setBitmap(photoCapturedBitmap);
            String suggestion = predict(photoCapturedBitmap);
            //下面讲解跳转传值用到的函数
            transferToPredictResultActivity(suggestion,level);
        }
        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK) {
            Uri targetUri = data.getData();
            Bitmap bitmap;
            //Display the image and output the prediction
            try {
                bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(targetUri));
                BitmapTransfer.getInstance().setBitmap(bitmap);
                String suggestion = predict(bitmap);
                //跳转传值
                transferToPredictResultActivity(suggestion,level);
            } catch (FileNotFoundException e) {
                //Print out the error message
                e.printStackTrace();

            }
        }

    }
}
```

## 4、实现tensorflow模型嵌入

​	在实现tensorflow模型嵌入之前我们要准备好3样文件：1、已经训练好的神经网络模型，并且保存为 **.pb文件**格式；2、通过自己操作生成的或者别人做好的版本一致的**libandroid_tensorflow_inference_java.jar**的jar包；3、**libtensorflow_inference.so** 类库。
​	接下来要将这三样文件放入项目对应的文件夹位置中，如下图：

<img src="C:\Users\荣杉山\AppData\Roaming\Typora\typora-user-images\image-20200429195026665.png" alt="image-20200429195026665" style="zoom:67%;" />

​	**注意！！ .pb预测模型要放在 assets 资源文件夹下 。jar包引入后要右击选择 “Add as Libraries”**
​	此后，分别在*app:build.gradle*文件里进行相应的配置，并且在调用模型的Activity里对模型进行调用，其代码如下：

**配置 app: build.gradle 文件**

```
apply plugin: 'com.android.application'
android {
	......
	sourceSets {
        main {
            jniLibs.srcDirs = ['libs']
        }
    }
    productFlavors {
    }
    defaultConfig {

        ndk {
            abiFilters "armeabi-v7a", "arm64-v8a", "x86", "x86_64"
        }
    }
    aaptOptions { noCompress 'pb' }
}
```

**在MainActivity.java 文件中调用tensorflow模型进行预测**

```java
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;
public class MainActivity extends Activity {
    private TensorFlowInferenceInterface inferenceInterface;
    //Variables about the Skin Cancer Detector model
    private static final String MODEL_PATH = "file:///android_asset/skin_cancer_detector_model.pb";
    private static final String INPUT_NODE = "conv2d_1_input";
    private static final String OUTPUT_NODE = "dense_3/Sigmoid";
    //Dimensions of the input
    private static final int TARGET_WIDTH = 224;
    private static final int TARGET_HEIGHT = 224;
    //Target size required for the Skin Cancer Detector input
    private static final int[] INPUT_SIZE = {1, TARGET_WIDTH, TARGET_HEIGHT, 3};

    static {
        System.loadLibrary("tensorflow_inference");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ......
        /Prepare the Tensorflow Inference to run the model
        inferenceInterface = new TensorFlowInferenceInterface();
        inferenceInterface.initializeTensorFlow(getAssets(), MODEL_PATH);
        ......
    }
    @SuppressLint("MissingSuperCall")
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
    	if(requestCode == START_CAMERA_ACTIVITY && resultCode == RESULT_OK) {
           ......
            String suggestion = predict(photoCapturedBitmap);
           ......
        }
        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK) {
            //Display the image and output the prediction
            try {
                ......
                String suggestion = predict(bitmap);
                ......
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
    //主要预测函数！！！！
    String level;
    //Output the prediction given by running the image through the Skin Cancer Detector
    public String predict(Bitmap bitmap) {
        //Format the input
        float[] pixels = new float[TARGET_WIDTH * TARGET_HEIGHT * 3];
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, TARGET_WIDTH, TARGET_HEIGHT, false);
        //Input all the pixel values of the image into the array
        for(int i = 0; i < TARGET_WIDTH; i++) {
            for(int j = 0; j < TARGET_HEIGHT; j++) {
                int pixel = resizedBitmap.getPixel(i, j);
                pixels[(i + j * TARGET_WIDTH) * 3] = ((float) Color.red(pixel)) / 255;
                pixels[(i + j * TARGET_WIDTH) * 3 + 1] = ((float) Color.green(pixel)) / 255;
                pixels[(i + j * TARGET_WIDTH) * 3 + 2] = ((float) Color.blue(pixel)) / 255;
            }
        }
        //Run the Skin Cancer Detector with the inputted image's pixels
        inferenceInterface.fillNodeFloat(INPUT_NODE, INPUT_SIZE, pixels);
        inferenceInterface.runInference(new String[] {OUTPUT_NODE});
        //Read the prediction by the model
        float[] prediction = {0};
        inferenceInterface.readNodeFloat(OUTPUT_NODE, prediction);
        String suggestionText;
        //测试查看输出的原值
        //predictionText = Arrays.toString(prediction);
        //输出文本
        float score = prediction[0];
        float levelscore = score*100;
        NumberFormat formatter = new DecimalFormat("0.00");
        String danger = formatter.format(score*100);
        level = formatter.format(levelscore);
        //String danger = String.valueOf(pre);
        if(prediction[0]>0 && prediction[0]<=0.3){
            suggestionText = "您的皮肤状况良好！异常区域的危险系数为："+danger+"%，危险系数较低";
        }else if(prediction[0]>0.3 && prediction[0]<=0.8){
            suggestionText = "您的皮肤出现较明显的问题！异常区域的危险系数为："+danger+"%，危险系数较高，建议联系您的主治医生";
        }else if(prediction[0] == 0 || prediction[0] == 1){
            suggestionText = "请输入正确的图片格式";
        }else{
            suggestionText = "Dangerous！皮肤异常区域的危险系数为："+danger+"%，危险系数很高，请及时到医院就医";
        }
        //predictionText = prediction[0] < 0.5 ? "Benign" : "Malignant";
        return suggestionText;
    }
}
```

**注意！！！**INPUT_NODE 和 OUTPUT_NODE 要提前在tensorflow神经网络模型中记录下输入和输出的节点名称。
**注意！！！** 在不同版本的  jar  包中    inferenceInterface.  方法会有不同，具体要Ctrl+单击，进入inferenceInterface的class类中去对照查看。

## 5、预测函数predict()详解

​	predict() 函数的详细代码，如上面的 4 号标题中所示。图片在转换成 bitmap 之后作为参数输入predict()函数，bitmap首先进行预处理，被resize为224*224大小resizedBitmap。然后进入for循环进行归一化，并把二维的像素矩阵压缩到一维数组 pixels[ ] 中。然后通过  inferenceInterface.fillNodeFloat(INPUT_NODE, INPUT_SIZE, pixels)  方法将处理好的图片数据输入预测模型；==**fillNodeFloat**这个函数的具体方法源码找不到，可以之后去网上在搜索搜索细读一遍。==。之后通过          inferenceInterface.runInference(new String[] {OUTPUT_NODE})  方法进行预测和输出，参数为输出数据的形状大小。最后经过  inferenceInterface.readNodeFloat(OUTPUT_NODE, prediction)  方法将输出的结果存入prediction [ ] 数组中。通过对prediction[ ]的值对预测的结果进行文字上的判断。

## 6、拍照或选取照片后跳转到另一个Activity并显示预测结果

​	此操作又名为 跳转传值。本次开发中主要对 图片和字符串 两种数据进行跳转传值，对图片的跳转传值时我用到了一个BitmapTransfer.java类来进行辅助传值。字符串的传值则用到了Intent.putExtra("name",name)的函数。具体代码如下：
在MainActivity中调用相册和相机获取图片，在PredictResultActivity中显示图片和预测结果。

​	**MainActivity.java中的部分代码**

```java
	@SuppressLint("MissingSuperCall")
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        if(requestCode == START_CAMERA_ACTIVITY && resultCode == RESULT_OK) {
            Bitmap photoCapturedBitmap = BitmapFactory.decodeFile(imageFileLocation);
            //传递字符串
            BitmapTransfer.getInstance().setBitmap(photoCapturedBitmap);
            String suggestion = predict(photoCapturedBitmap);
            //传递字符串
            transferToPredictResultActivity(suggestion,level);
        }
        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK) {
            Uri targetUri = data.getData();
            Bitmap bitmap;
            try {
                bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(targetUri));
                //传递图片
                BitmapTransfer.getInstance().setBitmap(bitmap);
                String suggestion = predict(bitmap);
                //传递字符串
                transferToPredictResultActivity(suggestion,level);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
	private void transferToPredictResultActivity(String suggestion,String dangerLevel) {
        Intent intent =new Intent(MainActivity.this, PredictResultActivity.class);
        intent.putExtra("suggestion",suggestion);
        intent.putExtra("dangerLevel",dangerLevel);
        startActivity(intent);
    }
```

​	**BitmapTransfer.java中的代码**

```java
import android.graphics.Bitmap;
public class BitmapTransfer {
    private Bitmap bitmap = null;
    private static final BitmapTransfer instance = new BitmapTransfer();
    public BitmapTransfer() {
    }
    public static BitmapTransfer getInstance() {
        return instance;
    }
    public Bitmap getBitmap(){
        return bitmap;
    }
    public void setBitmap (Bitmap bitmap){
        this.bitmap = bitmap;
    }
}

```

​	**PredictResultActivity.java代码**

```java
import com.example.uidesign02.Helper.BitmapTransfer;
public class PredictResultActivity extends Activity {
    ImageView predictImageView;
    TextView suggestionText;
    TextView dangerLevel;
    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_predict_result);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        //获取传来的Intent事件里的数据
        Intent intent = getIntent();
        String prediction = intent.getStringExtra("suggestion");
        suggestionText= findViewById(R.id.suggestionText);
        suggestionText.setText(prediction);
        predictImageView = (ImageView)findViewById(R.id.predictImageView);
        Bitmap transferbitmap = BitmapTransfer.getInstance().getBitmap();
        predictImageView.setImageBitmap(transferbitmap);
        dangerLevel = findViewById(R.id.dangerLevel);
        String dangerlevel = intent.getStringExtra("dangerLevel");
        //String转换成Float后根据数值大小来改变文字颜色
        float dangerlevelFloat=Float.parseFloat(dangerlevel);
        if(dangerlevelFloat>0 && dangerlevelFloat<=30.0){
            dangerLevel.setTextColor(Color.parseColor("#00FA9A"));
            dangerLevel.setText(dangerlevel+"%");
        }else if(dangerlevelFloat>30.0 && dangerlevelFloat<=80.0){
            dangerLevel.setTextColor(Color.parseColor("#F0E68C"));
            dangerLevel.setText(dangerlevel+"%");
        }else{
            dangerLevel.setTextColor(Color.parseColor("#FA8072"));
            dangerLevel.setText(dangerlevel+"%");
        }
    }
}
```

## 7、UI美化之去除标题栏和透明化手机系统通知栏

	### 7.1 去除标题栏只要在AndroidManifest.xml中更改Theme主题以及在每个Activity中更改继承的Activity类型就好

​	**AndroidManifest.xml的部分代码**

```xml
<application
        android:allowBackup="true"
        android:icon="@mipmap/ic_logo01"
        android:label="AI-Skin"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/AppTheme">
        <activity android:name=".PredictResultActivity"></activity>
        <activity android:name=".MainActivity">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
</application>
```

​	android:icon 设置APP的图标；android:label 设置APP的名称； android:theme 设置主题，可以去掉标题栏

​	**Activity里的代码修改部分**

```java
public class MainActivity extends Activity {
}
```

​	把继承的Activity改成上面代码的模样就好

### 7.2 透明化手机系统通知栏

​	在Activity.java和activity.xml布局文件中进行两处修改

​	**Activity.java中的代码添加修改**

```java
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 使通知栏透明化
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
```

​	**activity.xml的代码添加**

```xml
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"                                                 
    android:background="@drawable/ic_backg"
    android:clipToPadding="false"
    android:fitsSystemWindows="true"
    tools:context=".MainActivity">
</androidx.constraintlayout.widget.ConstraintLayout>
```

​	android:background 设置页面背景图片； android:clipToPadding="false"和android:fitsSystemWindows="true"让系统通知栏透明化。

##  8、UI美化之使用Adobe XD，搭配透明化按钮

​	本文的UI界面是用Adobe XD来设计的，给我的感觉就是相对来说方便操作，不足的是国内版本支持的插件不多，用起来得科学上网，很麻烦。而且Mac对它的兼容很好，Win一般，所以又让我萌生拥有一台MBP的想法。呜呜呜，MBP真香！！！