package com.example.notebook;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 查看详细日记活动类
 */
public class ShowActivity extends AppCompatActivity implements View.OnClickListener{

    public static final int TAKE_PHOTO = 1;
    private ImageView iv_photo;
    private Uri imageUri;
    public static final int CHOOSE_PHOTO = 2;
    public static String imagePath = null;

    private Button btnSave;
    private Button btnCancel;
    private TextView showTime;
    private EditText showContent;
    private EditText showTitle;
    private ImageView showPicture;
    private Button chooseFromAlbum;
    private Button takePhoto;

    private Values value;
    DBService myDb;
    private int num = 200000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);
        init();

    }

    @SuppressLint("CutPasteId")
    public void init() {
        myDb = new DBService(this);
        btnCancel = findViewById(R.id.show_cancel);
        btnSave = findViewById(R.id.show_save);
        showTime = findViewById(R.id.show_time);
        showTitle = findViewById(R.id.show_title);
        showContent = findViewById(R.id.show_content);
        showPicture = findViewById(R.id.show_image);
        chooseFromAlbum = findViewById(R.id.choose_from_album);
        takePhoto = findViewById(R.id.take_photo);
        takePhoto.setOnClickListener(this);
        this.iv_photo = findViewById(R.id.show_image);
        chooseFromAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(ShowActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ShowActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                } else {
                    openAlbum();
                }

            }
        });
        Intent intent = this.getIntent();
        if (intent != null) {
            value = new Values();

            value.setTime(intent.getStringExtra(DBService.TIME));
            value.setTitle(intent.getStringExtra(DBService.TITLE));
            value.setContent(intent.getStringExtra(DBService.CONTENT));
            value.setId(Integer.valueOf(intent.getStringExtra(DBService.ID)));
            value.setPath(intent.getStringExtra(DBService.PATH));

            showTime.setText(value.getTime());
            showTitle.setText(value.getTitle());
            showContent.setText(value.getContent());
            //读取数据库中的path并将其转化为Uri
            Uri u = Uri.parse(value.getPath());
            showPicture.setImageURI(u);
        }

        //按钮点击事件
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SQLiteDatabase db = myDb.getWritableDatabase();
                ContentValues values = new ContentValues();
                String content = showContent.getText().toString();
                String title = showTitle.getText().toString();
                String path = imagePath;

                values.put(DBService.TIME, getTime());
                values.put(DBService.TITLE,title);
                values.put(DBService.CONTENT,content);
                values.put(DBService.PATH, path);
                db.update(DBService.TABLE,values,DBService.ID+"=?",new String[]{value.getId().toString()});
                Toast.makeText(ShowActivity.this,"修改成功",Toast.LENGTH_LONG).show();
                db.close();
                Intent intent = new Intent(ShowActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String content = showContent.getText().toString();
                final String title = showTitle.getText().toString();
                new AlertDialog.Builder(ShowActivity.this)
                        .setTitle("提示框")
                        .setMessage("是否保存当前内容?")
                        .setPositiveButton("yes",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        SQLiteDatabase db = myDb.getWritableDatabase();
                                        ContentValues values = new ContentValues();
                                        values.put(DBService.TIME, getTime());
                                        values.put(DBService.TITLE,title);
                                        values.put(DBService.CONTENT,content);
                                        db.update(DBService.TABLE,values,DBService.ID+"=?",new String[]{value.getId().toString()});
                                        Toast.makeText(ShowActivity.this,"修改成功",Toast.LENGTH_LONG).show();
                                        db.close();
                                        Intent intent = new Intent(ShowActivity.this,MainActivity.class);
                                        startActivity(intent);
                                    }
                                })
                        .setNegativeButton("no",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(ShowActivity.this,MainActivity.class);
                                        startActivity(intent);
                                    }
                                }).show();
            }
        });
    }
    private void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO);//打开相册
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                } else {
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }
    String getTime() {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //获取当前时间
        Date date = new Date(System.currentTimeMillis());
        return simpleDateFormat.format(date);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.take_photo:
                File outputImage = new File(getExternalCacheDir(), "output_image.jpg");
                try{
                    while (outputImage.exists()) {
                        num++;
                        outputImage = new File(getExternalCacheDir(), "output_image" + num + ".jpg");
                    }
                    outputImage.createNewFile();
                }catch (Exception e){
                    e.printStackTrace();
                }

                if (Build.VERSION.SDK_INT >= 24){
                    imageUri = FileProvider.getUriForFile(this,BuildConfig.APPLICATION_ID+".fileprovider",outputImage);
                }else{
                    imageUri = Uri.fromFile(outputImage);
                }

                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent,TAKE_PHOTO);
                break;
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    //判断手机版本号
                    if (Build.VERSION.SDK_INT >= 19) {
                        //4.4及以上系统
                        handleImageOnKitKat(data);
                    } else {
                        handleImageBeforeKitKat(data);
                    }
                }
                break;
            case TAKE_PHOTO:
                if(resultCode == RESULT_OK){
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(
                                getContentResolver().openInputStream(imageUri)
                        );
                        iv_photo.setImageBitmap(bitmap);
                        imagePath = String.valueOf(imageUri);
                    }catch (FileNotFoundException e){
                        e.printStackTrace();
                    }
                }
                break;
            default:
                break;
        }
    }

    @TargetApi(19)
    private void handleImageOnKitKat(Intent data) {
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this, uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);

            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            imagePath = getImagePath(uri, null);

        }
        displayImage(imagePath);
    }

    private void handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        displayImage(imagePath);
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    private void displayImage(String imagePath) {
        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            showPicture.setImageBitmap(bitmap);
        } else {
            Toast.makeText(this, "failed to get image", Toast.LENGTH_SHORT).show();
        }
    }

}
