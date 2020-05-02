package com.example.uidesign02;

import android.app.Activity;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.GridView;

import androidx.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class PredictionListActivity extends Activity {

    GridView gridView;
    ArrayList<PredictionBean> list;
    PredictListAdapter adapter = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_predict_list);
        //使系统状态栏透明化
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }

        gridView = (GridView) findViewById(R.id.gridView);
        list = new ArrayList<>();
        adapter = new PredictListAdapter(this, R.layout.predict_items, list);
        gridView.setAdapter(adapter);

        //从sqlite中获取所有的数据
        Cursor cursor = PredictResultActivity.sqLiteHelper.getData("SELECT * FROM PREDICTION");
        //Cursor cursorDate = PredictResultActivity.sqLiteHelper.getData("select datetime(timestamp,'localtime') from PREDICTION");

        list.clear();
        while (cursor.moveToNext()){
            int id = cursor.getInt(0);
            String dangerlevel = cursor.getString(1);
            String suggestiontext = cursor.getString(2);
            String predictedTime = cursor.getString(3);
            byte[] image = cursor.getBlob(4);

            //list.add(new PredictionBean(id,dangerlevel,suggestiontext,image,predictedTime));
            list.add(new PredictionBean(id,dangerlevel,suggestiontext,predictedTime, image));
        }
        adapter.notifyDataSetChanged();
    }
}
