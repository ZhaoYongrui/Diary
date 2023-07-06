package com.example.notebook;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 数据库后台服务类
 */
public class DBService extends SQLiteOpenHelper {
    public static final String TABLE = "notes";
    public static final String ID = "_id";
    public static final String TITLE ="title";
    public static final String CONTENT = "content";
    public static final String TIME = "time";
    public static final String PATH = "path";
    public static final String UID ="uid";

    public DBService(Context context) {
        super(context,"notepad.db",null,1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE "+TABLE+"( "+ID+
                " INTEGER PRIMARY KEY AUTOINCREMENT, "+
                TITLE +" VARCHAR(30) ,"+
                CONTENT + " TEXT , "+
                PATH + "  TEXT ,"+
                UID + " TEXt ,"+
                TIME + " DATETIME NOT NULL )";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}