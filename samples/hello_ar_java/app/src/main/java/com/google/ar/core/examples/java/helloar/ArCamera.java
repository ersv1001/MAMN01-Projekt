package com.google.ar.core.examples.java.helloar;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.Frame;
import com.google.ar.core.TrackingState;
import com.google.ar.core.examples.java.common.helpers.AugmentedImageNode;
import com.google.ar.core.examples.java.common.helpers.SnackbarHelper;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.ux.ArFragment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ArCamera extends AppCompatActivity implements SensorEventListener {

    private ArFragment arFragment;
    private ImageView fitToScanView;

    // Augmented image and its associated center pose anchor, keyed by the augmented image in the database.
    private final Map<AugmentedImage, AugmentedImageNode> augmentedImageMap = new HashMap<>();
    private AugmentedImageNode node;


    private float mAccelNoGrav;
    private float mAccelWithGrav;
    private float mLastAccelWithGrav;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    ArrayList<Float> z = new ArrayList<>();

    public static boolean shakeIsHappening;
    public static int compNbr = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arcamera);

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        fitToScanView = findViewById(R.id.image_view_fit_to_scan);
        arFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdateFrame);

        node = new AugmentedImageNode(getApplicationContext(), arFragment);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);


        mAccelNoGrav = 0.00f;
        mAccelWithGrav = SensorManager.GRAVITY_EARTH;
        mLastAccelWithGrav = SensorManager.GRAVITY_EARTH;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (augmentedImageMap.isEmpty()) {
            fitToScanView.setVisibility(View.VISIBLE);
        }
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause(){
        super.onPause();
        sensorManager.unregisterListener(this, accelerometer);
    }

    /**
     * Registered with the Sceneform Scene object, this method is called at the start of each frame.
     *
     * @param frameTime - time since last frame.
     */
    private void onUpdateFrame(FrameTime frameTime) {
        Frame frame = arFragment.getArSceneView().getArFrame();

        // If there is no frame or ARCore is not tracking yet, just return.
        if (frame == null || frame.getCamera().getTrackingState() != TrackingState.TRACKING) {
            return;
        }

        Collection<AugmentedImage> updatedAugmentedImages = frame.getUpdatedTrackables(AugmentedImage.class);
        for (AugmentedImage augmentedImage : updatedAugmentedImages) {
            switch (augmentedImage.getTrackingState()) {
                case PAUSED:
                    // When an image is in PAUSED state, but the camera is not PAUSED, it has been detected,
                    // but not yet tracked.
                    String text = "Image has been detected, keep scanning to reveal posters";
                    SnackbarHelper.getInstance().showMessage(this, text);
                    break;

                case TRACKING:
                    // Have to switch to UI Thread to update View.
                    fitToScanView.setVisibility(View.GONE);
                    SnackbarHelper.getInstance().hide(this);
                    // Create a new anchor for newly found images.
                    if (!augmentedImageMap.containsKey(augmentedImage)) {
                        node.setImage(augmentedImage);
                        augmentedImageMap.put(augmentedImage, node);
                        arFragment.getArSceneView().getScene().addChild(node);

                    }
                    break;

                case STOPPED:
                    augmentedImageMap.remove(augmentedImage);
                    break;
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        float x = sensorEvent.values[0];
        float y = sensorEvent.values[1];
        z.add((sensorEvent.values[2])-SensorManager.GRAVITY_EARTH);
        mLastAccelWithGrav = mAccelWithGrav;
        mAccelWithGrav = (float) Math.sqrt(x * x + y * y + z.indexOf(z.size()-1) * z.indexOf(z.size()-1));
        float delta = mAccelWithGrav - mLastAccelWithGrav;
        mAccelNoGrav = mAccelNoGrav * 0.9f + delta; // Low-cut filter

        if (mAccelNoGrav > 8.5) {
            shakeIsHappening = true;
        }


        if (shakeIsHappening == true && mAccelNoGrav < 2) {
            if (sensorEvent.values[0] > 4) {
                if (compNbr == 2) {
                    compNbr = 0;
                } else {
                    compNbr++;
                }
                node.setComposition(compNbr);
                Log.d("ARRAYTEST", "Vänster" + sensorEvent.values[0]);
                shakeIsHappening = false;
            } else if (sensorEvent.values[0] < -4) {
                if (compNbr == 0) {
                    compNbr = 2;
                } else {
                    compNbr--;
                }
                //node.setComposition(compNbr);
                Log.d("ARRAYTEST", "Höger" + sensorEvent.values[1]);
                shakeIsHappening = false;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}