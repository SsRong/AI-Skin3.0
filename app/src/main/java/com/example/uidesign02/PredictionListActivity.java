package com.example.uidesign02;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

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
        //Cursor cursor = PredictResultActivity.sqLiteHelper.getData("SELECT * FROM PREDICTION");
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
        //点击图片后弹出删除选项的选项框
        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                CharSequence[] items = {"上传","删除"};
                AlertDialog.Builder dialog = new AlertDialog.Builder(PredictionListActivity.this);

                dialog.setTitle("请选择");
                dialog.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0){
                            //上传
                            Toast.makeText(getApplicationContext(), "正在上传...", Toast.LENGTH_SHORT).show();
                        }else{
                            //删除
                            //Toast.makeText(getApplicationContext(), "正在删除...", Toast.LENGTH_SHORT).show();
                            Cursor c = PredictResultActivity.sqLiteHelper.getData("SELECT id FROM PREDICTION");
                            ArrayList<Integer> arrID = new ArrayList<>();
                            while (c.moveToNext()){
                                arrID.add(c.getInt(0));
                            }
                            showDialogDelete(arrID.get(position));
                        }
                    }
                });
                dialog.show();
                return true;
            }
        });
    }
    private void showDialogDelete(final int idPrediction){
        final AlertDialog.Builder dialogDelete = new AlertDialog.Builder(PredictionListActivity.this);
        dialogDelete.setTitle("注意！！");
        dialogDelete.setMessage("你确定要删除这个预测结果吗？");
        dialogDelete.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    PredictResultActivity.sqLiteHelper.deleteData(idPrediction);
                    Toast.makeText(getApplicationContext(),"删除成功", Toast.LENGTH_SHORT).show();
                } catch (Exception e){
                    Log.e("error", e.getMessage());
                }
                updatePredictionList();
            }
        });
        dialogDelete.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialogDelete.show();
    }

    private void updatePredictionList(){
        //从sqlite中获取所有的数据
        Cursor cursor = PredictResultActivity.sqLiteHelper.getData("SELECT * FROM PREDICTION");
        list.clear();
        while (cursor.moveToNext()){
            int id = cursor.getInt(0);
            String dangerlevel = cursor.getString(1);
            String suggestiontext = cursor.getString(2);
            String predictedTime = cursor.getString(3);
            byte[] image = cursor.getBlob(4);
            list.add(new PredictionBean(id,dangerlevel,suggestiontext,predictedTime, image));
        }
        adapter.notifyDataSetChanged();
    }
}
