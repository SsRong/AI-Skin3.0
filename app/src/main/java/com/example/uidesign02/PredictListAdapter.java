package com.example.uidesign02;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class PredictListAdapter extends BaseAdapter {

    private Context context;
    private int layout;
    private ArrayList<PredictionBean> predictionBeanList;

    public PredictListAdapter(Context context, int layout, ArrayList<PredictionBean> predictionBeanList) {
        this.context = context;
        this.layout = layout;
        this.predictionBeanList = predictionBeanList;
    }

    @Override
    public int getCount() {
        return predictionBeanList.size();
    }

    @Override
    public Object getItem(int position) {
        return predictionBeanList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class ViewHolder{
        ImageView predictedImage;
        TextView predictedLevel;
        TextView predictedTime;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View row = convertView;
        ViewHolder holder = new ViewHolder();
        if(row == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(layout,null);

            holder.predictedLevel = (TextView) row.findViewById(R.id.predictedLevel);
            holder.predictedImage = (ImageView) row.findViewById(R.id.predictedImage);
            holder.predictedTime = (TextView) row.findViewById(R.id.predictedTime);
            row.setTag(holder);
        }
        else{
            holder = (ViewHolder) row.getTag();
        }

        PredictionBean predictionBean = predictionBeanList.get(position);
        holder.predictedLevel.setText(predictionBean.getDangerlevel());
        holder.predictedTime.setText((CharSequence) predictionBean.getPredictedTime());
        byte[] Image = predictionBean.getImage();
        Bitmap bitmap = BitmapFactory.decodeByteArray(Image,0, Image.length);
        holder.predictedImage.setImageBitmap(bitmap);
        return row;
    }
}
