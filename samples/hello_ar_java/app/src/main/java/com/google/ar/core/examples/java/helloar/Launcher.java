package com.google.ar.core.examples.java.helloar;

import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Launcher extends AppCompatActivity {
    Button galleryBtn;
    Button ARbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        final Vibrator vibe = (Vibrator) Launcher.this.getSystemService(Context.VIBRATOR_SERVICE);
        ARbtn = findViewById(R.id.button);
        galleryBtn = findViewById(R.id.button2);
        galleryBtn.setOnClickListener(view -> {
            vibe.vibrate(80);//80 represents the milliseconds (the duration of the vibration)
            openGallery(view);
        });
        ARbtn.setOnClickListener(view -> {
            vibe.vibrate(80);//80 represents the milliseconds (the duration of the vibration)
            openCamera(view);
        });
    }

    public void openCamera(View view){
        Intent intent = new Intent(this, ArCamera.class);
        startActivity(intent);
    }

    public void openGallery(View view){
        Intent intent = new Intent(this, Gallery.class);
        startActivity(intent);
    }
}
