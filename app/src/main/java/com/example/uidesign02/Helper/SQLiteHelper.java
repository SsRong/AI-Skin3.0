package com.example.uidesign02.Helper;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import androidx.annotation.Nullable;

public class SQLiteHelper extends SQLiteOpenHelper {
    public SQLiteHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    //生成并访问数据库
    public void queryData(String sql) {
        SQLiteDatabase database = getWritableDatabase();//以读写的方式打开数据库，如果数据库满了，会报错
        database.execSQL(sql);//execSQL() 可以执行insert、delete、update和CREATE TABLE之类有更改行为的SQL语句;rawQuery()用于delete操作
    }

    public void insertData(String trim, String dangerlevel, String suggestiontext, String time, byte[] image){
        SQLiteDatabase database = getWritableDatabase();
        String sql = "INSERT INTO PREDICTION VALUES (NULL, ?, ?, ?, ?)";

        SQLiteStatement statement = database.compileStatement(sql);
        statement.clearBindings();
        statement.bindString(1,dangerlevel);
        statement.bindString(2,suggestiontext);
        statement.bindString(3,time);
        statement.bindBlob(4,image);
        statement.executeInsert();
    }

    public void deleteData(int id){
        SQLiteDatabase database = getWritableDatabase();
        String sql = "DELETE FROM PREDICTION WHERE id = ?";
        SQLiteStatement statement = database.compileStatement(sql);
        statement.clearBindings();
        statement.bindDouble(1,(double)id);
        statement.execute();
        database.close();
    }

    public Cursor getData(String sql){
        SQLiteDatabase database = getReadableDatabase();
        return database.rawQuery(sql,null);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void insertData(String dangerlevel, String suggestiontext, String time, byte[] image) {
        SQLiteDatabase database = getWritableDatabase();
        String sql = "INSERT INTO PREDICTION VALUES (NULL, ?, ?, ?, ?)";

        SQLiteStatement statement = database.compileStatement(sql);
        statement.clearBindings();
        statement.bindString(1,dangerlevel);
        statement.bindString(2,suggestiontext);
        statement.bindString(3,time);
        statement.bindBlob(4,image);
        statement.executeInsert();
    }
}
