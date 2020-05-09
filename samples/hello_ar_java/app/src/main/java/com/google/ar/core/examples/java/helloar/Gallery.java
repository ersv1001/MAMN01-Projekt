package com.google.ar.core.examples.java.helloar;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Gallery extends AppCompatActivity {
    ImageView[] importedPics = new ImageView[6];
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    ArrayList<ImageView> selected = new ArrayList<>();
    Button deleteBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        importedPics[0] = findViewById(R.id.importedPic0);
        importedPics[1] = findViewById(R.id.importedPic1);
        importedPics[2] = findViewById(R.id.importedPic2);
        importedPics[3] = findViewById(R.id.importedPic3);
        importedPics[4] = findViewById(R.id.importedPic4);
        importedPics[5] = findViewById(R.id.importedPic5);
        deleteBtn = findViewById(R.id.singleDelBtn);
        deleteBtn.setEnabled(false);
        fetchLoadedPics();
    }

    private void setListener(ImageView imgView) {
        imgView.setOnClickListener(v -> {
            Drawable highlight = ResourcesCompat.getDrawable(getResources(), R.drawable.highlight, null);
            if (!selected.contains(imgView)) {
                imgView.setBackground(highlight);
                selected.add(imgView);
                deleteBtn.setEnabled(true);
            } else {
                imgView.setBackgroundResource(0);
                selected.remove(imgView);
                if (selected.isEmpty()) {
                    deleteBtn.setEnabled(false);
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchLoadedPics();
    }

    private void fetchLoadedPics() {
        for (int i = 0; i < importedPics.length; i++) {
            Bitmap bitmap = new ImageSaver(getApplicationContext()).
                    setFileName(importedPics[i].getId() + ".png").
                    setDirectoryName("images").
                    load();
            if (bitmap == null) {
                bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(),
                        R.drawable.pictureplaceholder);

            } else {
                setListener(importedPics[i]);
            }
            importedPics[i].setImageBitmap(bitmap);
        }
    }

    public void onClear(View view) {
        for (int i = 0; i < importedPics.length; i++) {
            String picture = importedPics[i].getId() + ".png";
            new ImageSaver(getApplicationContext()).
                    setFileName(picture).
                    setDirectoryName("images").
                    deleteFile();
            importedPics[i].setImageBitmap(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                    R.drawable.pictureplaceholder));
            importedPics[i].setBackgroundResource(0);
        }
        selected.clear();
        deleteBtn.setEnabled(false);
        fetchLoadedPics();
    }

    public void onDelete(View view) {
        for (int i = 0; i < selected.size(); i++) {
            String picture = selected.get(i).getId() + ".png";
            new ImageSaver(getApplicationContext()).
                    setFileName(picture).
                    setDirectoryName("images").
                    deleteFile();
            selected.get(i).setImageBitmap(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                    R.drawable.pictureplaceholder));
            selected.get(i).setBackgroundResource(0);
        }
        selected.clear();
        deleteBtn.setEnabled(false);
    }

    public void onTakePicture(View view) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 110);
            dispatchTakePictureIntent();
        } else {
            dispatchTakePictureIntent();
        }

    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    //TODO: Change pics to findEmptySpot().
    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data) {

        if (reqCode == REQUEST_IMAGE_CAPTURE && resCode == RESULT_OK) {

            int spot = findEmptySpot();
            if(spot == -1){
                Toast.makeText(getApplicationContext(),
                        "The picture couldn't be added, library is full",
                        Toast.LENGTH_SHORT).show();
            } else {
            Bundle extras = data.getExtras();
            Bitmap imgBitmap = (Bitmap) extras.get("data");
            String name = importedPics[spot].getId() + ".png";
            new ImageSaver(getApplicationContext()).
                    setFileName(name).
                    setDirectoryName("images").
                    save(imgBitmap);
            Bitmap bitmap = new ImageSaver(getApplicationContext()).
                    setFileName(name).
                    setDirectoryName("images").
                    load();
            importedPics[spot].setImageBitmap(bitmap);
            setListener(importedPics[spot]);
            }
        }
    }

    //TODO: DOESN'T WORK YET
    private int findEmptySpot() {
        for (int i = 0; i < importedPics.length; i++) {
            if (new ImageSaver(getApplicationContext()).
                    setFileName(importedPics[i].getId() + ".png").
                    setDirectoryName("images").
                    load() == null) {
                return i;
            }
        }
        return -1;
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

    public boolean deleteFile(){
        File file = createFile();
        return file.delete();
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

    File getAlbumStorageDir(String albumName) {
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