package com.example.uidesign02;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

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


        Intent intent = getIntent();
        String prediction = intent.getStringExtra("suggestion");
        suggestionText= findViewById(R.id.suggestionText);
        suggestionText.setText(prediction);

        predictImageView = (ImageView)findViewById(R.id.predictImageView);
        Bitmap transferbitmap = BitmapTransfer.getInstance().getBitmap();
        predictImageView.setImageBitmap(transferbitmap);

        dangerLevel = findViewById(R.id.dangerLevel);
        String dangerlevel = intent.getStringExtra("dangerLevel");
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
