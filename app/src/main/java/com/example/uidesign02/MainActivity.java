package com.example.uidesign02;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.uidesign02.Helper.BitmapTransfer;
import com.example.uidesign02.Helper.SQLiteHelper;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends Activity {
    private static final int EXTERNAL_STORAGE_REQUEST_RESULT = 1;
    private static final int START_CAMERA_ACTIVITY = 0;
    private static final int IMAGE_PICK_CODE = 1000;
    private static final int PERMISSION_CODE = 1001;
    private String imageFileLocation = "";
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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 使通知栏透明化
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }

        //Prepare the Tensorflow Inference to run the model
        inferenceInterface = new TensorFlowInferenceInterface();
        inferenceInterface.initializeTensorFlow(getAssets(), MODEL_PATH);

        //点击监听：加载loadImage按钮功能；当点击‘图库’时，访问图库选择图像
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
                        //transferToPredictResultActivity();
                    }
                }else {
                    //系统版本低于marshmallow，不用权限申请
                    pickImageFromGallery();
                    //transferToPredictResultActivity();
                }
                //连接图库
                //Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                //启动intent功能
                //startActivityForResult(intent,OPEN_GALLERY_ACTIVITY);
            }
        });

         Button predictionItems = findViewById(R.id.predictionItems);
         predictionItems.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent intent = new Intent(MainActivity.this, PredictionListActivity.class);
                 startActivity(intent);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == EXTERNAL_STORAGE_REQUEST_RESULT) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                pickImageFromGallery();
            }else {
                Toast.makeText(this, "外部存储权限遭到拒绝", Toast.LENGTH_SHORT).show();
            }
        }else {
            super.onRequestPermissionsResult(requestCode, permissions,grantResults);
        }
    }


    @SuppressLint("MissingSuperCall")
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        if(requestCode == START_CAMERA_ACTIVITY && resultCode == RESULT_OK) {
            //Toast.makeText(this,"拍照成功",Toast.LENGTH_SHORT).show();
            //Bundle extras = data.getExtras();
            //Bitmap photoCapturedBitmap = (Bitmap) extras.get("data");
            Bitmap photoCapturedBitmap = BitmapFactory.decodeFile(imageFileLocation);
            BitmapTransfer.getInstance().setBitmap(photoCapturedBitmap);
            String suggestion = predict(photoCapturedBitmap);
            transferToPredictResultActivity(suggestion,level);
        }
        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK) {
            //imageViewCapturedPhoto.setImageURI(data.getData());
            Uri targetUri = data.getData();
            Bitmap bitmap;

            //Display the image and output the prediction
            try {

                bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(targetUri));
                BitmapTransfer.getInstance().setBitmap(bitmap);
                String suggestion = predict(bitmap);
                transferToPredictResultActivity(suggestion,level);

            } catch (FileNotFoundException e) {

                //Print out the error message
                e.printStackTrace();

            }
        }

    }

    public void takePhoto(View view) {
        Intent cameraIntent = new Intent();
        cameraIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);

        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException e){
            e.printStackTrace();
        }
        //Uri uri = FileProvider.getUriForFile(this,"com.example.skindemo02.provider",photoFile);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(this,"com.example.uidesign02.provider",photoFile));
        cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        //Log.d(TAG, "添加权限。");
        startActivityForResult(cameraIntent,START_CAMERA_ACTIVITY);
    }

    //将输入的照片旋转到合适的角度和大小放到imageView框里去
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

    File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "skinImage_"+ timeStamp + "_";
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(imageFileName,".jpg",storageDirectory);
        imageFileLocation = image.getAbsolutePath();
        return image;
    }


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
        double max =0;
        int maxIndex =0;
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
