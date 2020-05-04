package com.google.ar.core.examples.java.helloar;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class Launcher extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
    }

    public void openCamera(View view){
        Intent intent = new Intent(this, ArCamera.class);
        startActivity(intent);
    }
}
