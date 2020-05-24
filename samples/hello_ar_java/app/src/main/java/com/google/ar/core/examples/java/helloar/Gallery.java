package com.google.ar.core.examples.java.helloar;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Gallery extends AppCompatActivity {
    ImageView[] importedPics = new ImageView[6];
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    ArrayList<ImageView> selected = new ArrayList<>();
    Button deleteBtn, clearBtn;
    ImageButton addBtn;
    Vibrator vibe;
    public static int amountOfImages = 0;

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

        vibe = (Vibrator) Gallery.this.getSystemService(Context.VIBRATOR_SERVICE);
        deleteBtn = findViewById(R.id.singleDelBtn);
        clearBtn = findViewById(R.id.delBtn);
        addBtn = findViewById(R.id.openCam);
        deleteBtn.setEnabled(false);
        deleteBtn.setOnClickListener(view -> {
            vibe.vibrate(80);//80 represents the milliseconds (the duration of the vibration)
            delBtnPressed(view);
        });
        clearBtn.setOnClickListener(view -> {
            vibe.vibrate(80);//80 represents the milliseconds (the duration of the vibration)
            clearBtnPressed(view);
        });
        addBtn.setOnClickListener(view -> {
            vibe.vibrate(80);//80 represents the milliseconds (the duration of the vibration)
            onTakePicture(view);
        });
        fetchLoadedPics();
    }

    private void setListener(ImageView imgView) {
        imgView.setOnClickListener(v -> {
            vibe.vibrate(80);//80 represents the milliseconds (the duration of the vibration)
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
        amountOfImages = 0;
        for (int i = 0; i < importedPics.length; i++) {
            Bitmap bitmap = new ImageSaver(getApplicationContext()).
                    setFileName(importedPics[i].getId() + ".png").
                    setDirectoryName("images").
                    load();
            if (bitmap == null) {
                bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(),
                        R.drawable.pictureplaceholder);

            } else {
                amountOfImages++;
                setListener(importedPics[i]);

            }
            importedPics[i].setImageBitmap(bitmap);

        }
    }

    public void delBtnPressed(View view){
        new AlertDialog.Builder(Gallery.this)
                .setMessage(R.string.delete)
                .setTitle(R.string.delete_title)
                .setCancelable(false)
                .setPositiveButton(R.string.fire, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        onDelete(view);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    public void clearBtnPressed(View view){
        new AlertDialog.Builder(Gallery.this)
                .setMessage(R.string.delete)
                .setTitle(R.string.delete_title)
                .setCancelable(false)
                .setPositiveButton(R.string.fire, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        onClear(view);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
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
        amountOfImages = 0;
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
        amountOfImages--;
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
            amountOfImages++;
            Log.d("AMOUNT", "Amount: " + amountOfImages);
            }
        }
    }

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


