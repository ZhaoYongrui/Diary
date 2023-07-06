package com.example.notebook;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UserDB extends SQLiteOpenHelper{
    public static final String ID = "_id";
    public static final String TABLE = "users";
    public static final String ACCOUNT = "account";
    public static final String PASSWORD ="password";

    public UserDB(Context context) {
        super(context,"users.db",null,1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE "+TABLE+"( "+ID+
                " INTEGER PRIMARY KEY AUTOINCREMENT, "+
                ACCOUNT + " TEXT , "+
                PASSWORD + "  TEXT )";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
