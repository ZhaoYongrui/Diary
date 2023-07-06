package com.example.notebook;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 编辑日记本活动类
 */
public class EditActivity extends AppCompatActivity implements View.OnClickListener {
    public static final int TAKE_PHOTO = 1;
    private ImageView iv_photo;
    private Uri imageUri;
    public static final int CHOOSE_PHOTO = 2;
    public static String imagePath = null;

    DBService myDb;
    private Button btnCancel;
    private Button btnSave;
    private EditText titleEditText;
    private EditText contentEditText;
    private TextView timeTextView;
    private ImageView picture;
    private Button chooseFromAlbum;
    private Button takePhoto;
    private int num = 100000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_editor);
        init();
        if (timeTextView.getText().length() == 0)
            timeTextView.setText(getTime());
    }

    @SuppressLint("CutPasteId")
    private void init() {

        myDb = new DBService(this);
        titleEditText = findViewById(R.id.et_title);
        contentEditText = findViewById(R.id.et_content);
        timeTextView = findViewById(R.id.edit_time);
        btnCancel = findViewById(R.id.btn_cancel);
        btnSave = findViewById(R.id.btn_save);
        picture = (ImageView) findViewById(R.id.edit_image);
        chooseFromAlbum = findViewById(R.id.choose_from_album);
        takePhoto = findViewById(R.id.take_photo);
        takePhoto.setOnClickListener(this);
        this.iv_photo = findViewById(R.id.edit_image);
        //添加照片
        chooseFromAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(EditActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(EditActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                } else {
                    openAlbum();
                }

            }
        });
//        Intent intent = this.getIntent();
//        if (intent != null) {
//            Values value = new Values();
//
//            value.setTime(intent.getStringExtra(DBService.TIME));
//            value.setTitle(intent.getStringExtra(DBService.TITLE));
//            value.setContent(intent.getStringExtra(DBService.CONTENT));
//            value.setId(Integer.valueOf(intent.getStringExtra(DBService.ID)));
//            value.setPath(intent.getStringExtra(DBService.PATH));
//
//            timeTextView.setText(value.getTime());
//            titleEditText.setText(value.getTitle());
//            contentEditText.setText(value.getContent());
//            //读取数据库中的path并将其转化为Uri
//            Uri u = Uri.parse(value.getPath());
//            picture.setImageURI(u);
//        }


        //取消按钮点击事件
        btnCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        //保存按钮点击事件
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SQLiteDatabase db = myDb.getWritableDatabase();
                ContentValues values = new ContentValues();

                String title = titleEditText.getText().toString();
                String content = contentEditText.getText().toString();
                String time = timeTextView.getText().toString();
                String path = imagePath;


                if ("".equals(titleEditText.getText().toString())) {
                    Toast.makeText(EditActivity.this, "标题不能为空", Toast.LENGTH_LONG).show();
                    return;
                }
                if ("".equals(contentEditText.getText().toString())) {
                    Toast.makeText(EditActivity.this, "内容不能为空", Toast.LENGTH_LONG).show();
                    return;
                }
                if (path == null) {
                    Toast.makeText(EditActivity.this, "照片不能为空", Toast.LENGTH_LONG).show();
                    return;
                }
                values.put(DBService.UID, LoginActivity.pref.getString("account", ""));
                values.put(DBService.TITLE, title);
                values.put(DBService.CONTENT, content);
                values.put(DBService.TIME, time);
                values.put(DBService.PATH, path);
                db.insert(DBService.TABLE, null, values);
                Toast.makeText(EditActivity.this, "保存成功", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(EditActivity.this, MainActivity.class);
                startActivity(intent);
                db.close();
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
                if (resultCode == RESULT_OK) {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(
                                getContentResolver().openInputStream(imageUri)
                        );
                        Log.d("sss", String.valueOf(imageUri));
                        iv_photo.setImageBitmap(bitmap);
                        imagePath = String.valueOf(imageUri);
                    } catch (FileNotFoundException e) {
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
                Log.d("sss", "1");
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);

            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            imagePath = getImagePath(uri, null);

        }
        Log.d("sss", imagePath);
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
            picture.setImageBitmap(bitmap);
        } else {
            Toast.makeText(this, "failed to get image", Toast.LENGTH_SHORT).show();
        }
    }

    //获取当前时间
    private String getTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        String str = sdf.format(date);
        return str;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.take_photo:
                File outputImage = new File(getExternalCacheDir(), "output_image" + num + ".jpg");

                try {
                    while (outputImage.exists()) {
                        num++;
                        outputImage = new File(getExternalCacheDir(), "output_image" + num + ".jpg");
                    }
                    outputImage.createNewFile();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (Build.VERSION.SDK_INT >= 24) {
                    imageUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", outputImage);
                } else {
                    imageUri = Uri.fromFile(outputImage);
                }

                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, TAKE_PHOTO);
                break;
        }
    }
}
