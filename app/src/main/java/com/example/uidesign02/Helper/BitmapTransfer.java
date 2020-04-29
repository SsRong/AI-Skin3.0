package com.example.uidesign02.Helper;

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
