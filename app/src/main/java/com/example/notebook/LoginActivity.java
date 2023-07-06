package com.example.notebook;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * 登录活动类
 */
public class LoginActivity extends AppCompatActivity {
    UserDB myDb;
    public static SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private EditText accountEdit;
    private EditText passwordEdit;
    private Button login;
    private Button register;
    private CheckBox rememberPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        myDb = new UserDB(this);
        pref = PreferenceManager.getDefaultSharedPreferences(this);
//        SharedPreferences sharedPre = this.getSharedPreferences("config",this.MODE_PRIVATE);
//        final SharedPreferences.Editor  shareEditor = sharedPre.edit();
        accountEdit = (EditText) findViewById(R.id.account);
        passwordEdit = (EditText) findViewById(R.id.password);
        rememberPass = (CheckBox) findViewById(R.id.remember_pass);
        login = (Button) findViewById(R.id.login);
        register = (Button) findViewById(R.id.register);
        boolean isRemember = pref.getBoolean("remember_password", false);
        if (isRemember) {
            //把账号密码都设置到文本框
            String account = pref.getString("account", "");
            String password = pref.getString("password", "");
            accountEdit.setText(account);
            passwordEdit.setText(password);
            rememberPass.setChecked(true);
        }

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View V) {
                String account = accountEdit.getText().toString();
                String password = passwordEdit.getText().toString();
                if (!haveUser(account)) {
                    SQLiteDatabase db = myDb.getWritableDatabase();
                    ContentValues values = new ContentValues();
                    values.put(UserDB.ACCOUNT, account);
                    values.put(UserDB.PASSWORD, password);
                    db.insert(UserDB.TABLE, null, values);
                    db.close();
                    editor = pref.edit();
//                shareEditor.putString("account",account);
//                shareEditor.putString("password",password);
//                shareEditor.commit();
                    editor.putString("account", account);
                    editor.putString("password", password);
                    editor.apply();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "用户名已存在！", Toast.LENGTH_SHORT).show();
                }
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String account = accountEdit.getText().toString();
                String password = passwordEdit.getText().toString();
                editor = pref.edit();

                if (account.equals("zhaoyr") && password.equals("123456")) {

                    if (rememberPass.isChecked()) {//检查复选框是否被选中
                        editor.putBoolean("remember_password", true);
                        editor.putString("account", account);
                        editor.putString("password", password);
                        editor.apply();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        editor.clear();
                        editor.putBoolean("remember_password", false);
                        editor.putString("account", account);
                        editor.putString("password", password);
                        editor.apply();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
//                    editor.apply();
//                    Intent intent = new Intent(LoginActivity.this,MainActivity.class);
//                    startActivity(intent);
//                    finish();
                } else if (account.equals("") && password.equals("")) {
                    account = "default";
                    password = "0000";
                    //editor.putBoolean("remember_password",true);
                    editor.putString("account", account);
                    editor.putString("password", password);
                    editor.apply();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else if (account.equals(pref.getString("account", "")) && password.equals(pref.getString("password", ""))) {
                    if (rememberPass.isChecked()) {//检查复选框是否被选中
                        editor.putBoolean("remember_password", true);
                        editor.putString("account", account);
                        editor.putString("password", password);
                        editor.apply();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        editor.clear();
                        editor.putBoolean("remember_password", false);
                        editor.putString("account", account);
                        editor.putString("password", password);
                        editor.apply();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                } else if (log(account, password)) {
                    if (rememberPass.isChecked()) {//检查复选框是否被选中
                        editor.putBoolean("remember_password", true);
                        editor.putString("account", account);
                        editor.putString("password", password);
                        editor.apply();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        editor.clear();
                        editor.putBoolean("remember_password", false);
                        editor.putString("account", account);
                        editor.putString("password", password);
                        editor.apply();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "用户名或密码错误！", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean haveUser(String account) {
        SQLiteDatabase db = myDb.getReadableDatabase();
        Cursor cursor = db.query(UserDB.TABLE, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                if (cursor.getString(cursor.getColumnIndex(UserDB.ACCOUNT)).equals(account)) {
                    cursor.close();
                    db.close();
                    return true;
                }
            }
        }
        cursor.close();
        db.close();
        return false;
    }

    private boolean log(String account, String password) {
        SQLiteDatabase db = myDb.getReadableDatabase();
        Cursor cursor = db.query(UserDB.TABLE, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                if (cursor.getString(cursor.getColumnIndex(UserDB.ACCOUNT)).equals(account) &&
                        cursor.getString(cursor.getColumnIndex(UserDB.PASSWORD)).equals(password)) {
                    cursor.close();
                    db.close();
                    return true;
                }
            }
        }
        cursor.close();
        db.close();
        return false;
    }
}
