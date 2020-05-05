package com.google.ar.core.examples.java.helloar;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Gallery extends AppCompatActivity {
    ImageView[] importedPics = new ImageView[3];
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private int pics = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        importedPics[0] = (ImageView) findViewById(R.id.importedPic0);
        importedPics[1] = (ImageView) findViewById(R.id.importedPic1);
        importedPics[2] = (ImageView) findViewById(R.id.importedPic2);

    }

    @Override
    protected void onResume() {
        super.onResume();
        checkLoadedPics();
    }

    private void checkLoadedPics() {
        for(int i = 0; i<3; i++){

            Bitmap bitmap = new ImageSaver(getApplicationContext()).
                    setFileName("img" + i + ".png").
                    setDirectoryName("images").
                    load();
            importedPics[i].setImageBitmap(bitmap);

        }
    }

    public void onTakePicture(View view){
            dispatchTakePictureIntent();
    }

    private void dispatchTakePictureIntent(){
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }




    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data){
        if(reqCode == REQUEST_IMAGE_CAPTURE && resCode == RESULT_OK){
            Bundle extras = data.getExtras();
            Bitmap imgBitmap = (Bitmap) extras.get("data");
            String name = "img" + pics + ".png";
            new ImageSaver(getApplicationContext()).
                    setFileName(name).
                    setDirectoryName("images").
                    save(imgBitmap);
            Bitmap bitmap = new ImageSaver(getApplicationContext()).
                    setFileName(name).
                    setDirectoryName("images").
                    load();
            importedPics[pics].setImageBitmap(bitmap);
            pics++;
        }
    }
}

class ImageSaver {

    private String directoryName = "images";
    private String fileName = "image.png";
    private Context context;
    private boolean external;

    public ImageSaver(Context context) {
        this.context = context;
    }

    public ImageSaver setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public ImageSaver setExternal(boolean external) {
        this.external = external;
        return this;
    }

    public ImageSaver setDirectoryName(String directoryName) {
        this.directoryName = directoryName;
        return this;
    }

    public void save(Bitmap bitmapImage) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(createFile());
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @NonNull
    private File createFile() {
        File directory;
        if(external){
            directory = getAlbumStorageDir(directoryName);
        }
        else {
            directory = context.getDir(directoryName, Context.MODE_PRIVATE);
        }
        if(!directory.exists() && !directory.mkdirs()){
            Log.e("ImageSaver","Error creating directory " + directory);
        }

        return new File(directory, fileName);
    }

    private File getAlbumStorageDir(String albumName) {
        return new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), albumName);
    }

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    public Bitmap load() {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(createFile());
            return BitmapFactory.decodeStream(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }



}