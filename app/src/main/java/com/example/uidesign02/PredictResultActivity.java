package com.example.uidesign02;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.uidesign02.Helper.BitmapTransfer;
import com.example.uidesign02.Helper.SQLiteHelper;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PredictResultActivity extends Activity {

    ImageView predictImageView;
    TextView suggestionText;
    TextView dangerLevel;
    Button saveResult;
    TextView predictResultTime;
    public static SQLiteHelper sqLiteHelper;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_predict_result);
        //使系统状态栏透明化
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }

        final String time = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        predictResultTime = findViewById(R.id.predictResultTime);
        predictResultTime.setText(time);
        //显示从主界面传来的预测结果，建议和图片
        Intent intent = getIntent();
        final String suggestiontext = intent.getStringExtra("suggestion");
        suggestionText= findViewById(R.id.suggestionText);
        suggestionText.setText(suggestiontext);

        predictImageView = (ImageView)findViewById(R.id.predictImageView);
        final Bitmap transferbitmap = BitmapTransfer.getInstance().getBitmap();
        predictImageView.setImageBitmap(transferbitmap);

        dangerLevel = findViewById(R.id.dangerLevel);
        String dangerlevel = intent.getStringExtra("dangerLevel");
        float dangerlevelFloat=Float.parseFloat(dangerlevel);
        //将结果按等级划分，并标上不同的颜色
        if(dangerlevelFloat>0 && dangerlevelFloat<=30.0){
            dangerLevel.setTextColor(Color.parseColor("#00FA9A"));
            dangerlevel = dangerlevel+"%  安全";
            dangerLevel.setText(dangerlevel);
        }else if(dangerlevelFloat>30.0 && dangerlevelFloat<=80.0){
            dangerLevel.setTextColor(Color.parseColor("#F0E68C"));
            dangerlevel = dangerlevel+"%  注意！";
            dangerLevel.setText(dangerlevel);
        }else{
            dangerLevel.setTextColor(Color.parseColor("#FA8072"));
            dangerlevel = dangerlevel+"%  危险！！";
            dangerLevel.setText(dangerlevel);
        }

        //为保存按钮添加功能，点击保存，将预测结果保存到本地的sqLite数据库
        saveResult = (Button) findViewById(R.id.saveResult);
        sqLiteHelper = new SQLiteHelper(this,"PredictionDB.sqlite",null,1);
        sqLiteHelper.queryData("CREATE TABLE IF NOT EXISTS PREDICTION(Id INTEGER PRIMARY KEY AUTOINCREMENT, dangerlevel VARCHAR, suggestiontext VARCHAR, image BLOB, time VARCHAR)");

        final String finalDangerlevel = dangerlevel;
        saveResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    sqLiteHelper.insertData(
                            finalDangerlevel.trim(),
                            suggestiontext.trim(),
                            time.trim(),
                            imageViewToByte(transferbitmap)
                    );
                    Toast.makeText(getApplicationContext(),"保存成功!",Toast.LENGTH_SHORT).show();
                    //suggestionText.setText("");
                    //dangerLevel.setText("");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    public byte[] imageViewToByte(Bitmap transferbitmap){
        //Intent intent = getIntent();
        //Bitmap transferbitmap = BitmapTransfer.getInstance().getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        transferbitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }
}
