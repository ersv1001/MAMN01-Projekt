package com.google.ar.core.examples.java.helloar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class Gallery extends AppCompatActivity {
    ImageView importedPic;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        importedPic = (ImageView) findViewById(R.id.importedPic);

    }

    public void onTakePicture(View view){
        dispatchTakePictureIntent();
    }

    private void dispatchTakePictureIntent(){
        Intent takePicIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePicIntent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(takePicIntent, REQUEST_IMAGE_CAPTURE);
        }
    }



    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data){
        if(reqCode == REQUEST_IMAGE_CAPTURE && resCode == RESULT_OK){
            Bundle extras = data.getExtras();
            Bitmap imgBitmap = (Bitmap) extras.get("data");
            importedPic.setImageBitmap(imgBitmap);
        }
    }

}
