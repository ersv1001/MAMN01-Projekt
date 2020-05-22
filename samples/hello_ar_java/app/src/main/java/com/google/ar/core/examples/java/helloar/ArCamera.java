package com.google.ar.core.examples.java.helloar;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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


//        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
//
//
//        ViewRenderable.builder()
//                .setView(this, R.layout.testfile)
//                .build()
//                .thenAccept(renderable -> pictureRenderable = renderable);
//
//        arFragment.setOnTapArPlaneListener(
//                (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
//                    if (pictureRenderable == null) {
//                        return;
//                    }
//
//                    // Create the Anchor.
//                    Anchor anchor = hitResult.createAnchor();
//                    AnchorNode anchorNode = new AnchorNode(anchor);
//                    anchorNode.setParent(arFragment.getArSceneView().getScene());
//
//                    // Create the transformable andy and add it to the anchor.
//                    TransformableNode andy = new TransformableNode(arFragment.getTransformationSystem());
//                    andy.setParent(anchorNode);
//                    andy.setLocalRotation(Quaternion.axisAngle(new Vector3(-1f, 0, 0), 90f));
//                    andy.setRenderable(pictureRenderable);
//                    andy.select();
//                });
//    }

public class ArCamera extends AppCompatActivity implements SensorEventListener {

    private ArFragment arFragment;
    private ImageView fitToScanView;

    // Augmented image and its associated center pose anchor, keyed by the augmented image in the database.
    private final Map<AugmentedImage, AugmentedImageNode> augmentedImageMap = new HashMap<>();

    //TODO//////////////////////////////////////// Flick

    private float mAccelNoGrav;
    private float mAccelWithGrav;
    private float mLastAccelWithGrav;

    ArrayList<Float> z = new ArrayList<>();

    public static float finalZ;

    public static boolean shakeIsHappening;
    public static float highZ;
    public static float lowZ;
    public static boolean flick;
    public static boolean pull;
    public static int beatnumber = 0;

    //TODO////////////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arcamera);

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        fitToScanView = findViewById(R.id.image_view_fit_to_scan);
        arFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdateFrame);

        //TODO//////////////////////////////////////// Flick

        SensorManager manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (!manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST)) {
            // Nånting som talar om att det inte funkar? Annars ta bort
        }

        mAccelNoGrav = 0.00f;
        mAccelWithGrav = SensorManager.GRAVITY_EARTH;
        mLastAccelWithGrav = SensorManager.GRAVITY_EARTH;

        //TODO////////////////////////////////////////////////////
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (augmentedImageMap.isEmpty()) {
            fitToScanView.setVisibility(View.VISIBLE);
        }
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
                    String text = "Detected Image " + augmentedImage.getIndex();
                    SnackbarHelper.getInstance().showMessage(this, text);
                    break;

                case TRACKING:
                    // Have to switch to UI Thread to update View.
                    fitToScanView.setVisibility(View.GONE);

                    // Create a new anchor for newly found images.
                    if (!augmentedImageMap.containsKey(augmentedImage)) {
                        AugmentedImageNode node = new AugmentedImageNode(getApplicationContext(), arFragment);
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

    //TODO//////////////////////////////////////// Flick

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
//
//        float x = sensorEvent.values[0];
//        float y = sensorEvent.values[1];
//        z.add((sensorEvent.values[2])-SensorManager.GRAVITY_EARTH);
//        mLastAccelWithGrav = mAccelWithGrav;
//        mAccelWithGrav = (float) Math.sqrt(x * x + y * y + z.indexOf(z.size()-1) * z.indexOf(z.size()-1));
//        float delta = mAccelWithGrav - mLastAccelWithGrav;
//        mAccelNoGrav = mAccelNoGrav * 0.9f + delta; // Low-cut filter
//
//        if (mAccelNoGrav > 8.5) {
//            shakeIsHappening = true;
//
//            z.clear();
//
//            if  (z.indexOf(z.size()-2) > z.indexOf(z.size()-1)) {
//                clickresults.append(" Z shrinking" + z);
//            } else if (z.indexOf(z.size()-2) < z.indexOf(z.size()-1)) {
//                clickresults.append(" Z growing" + z);
//            }
//
//        }
//
//
//        if (shakeIsHappening == true && mAccelNoGrav < 2) {
//
//            finalZ = z.get(z.size()-1);
//            highZ= z.get(z.size()-1);
//            lowZ= z.get(z.size()-1);
//            for (int i = 0; i < z.size(); i++) {
//                if (z.get(i) > highZ) {
//                    highZ = z.get(i);
//                } else if ((z.get(i) < lowZ)) {
//                    lowZ = z.get(i);
//                }
//                if (highZ==finalZ) {
//                    flick = true;
//                    pull = false;
//                } else if (lowZ==finalZ) {
//                    flick = false;
//                    pull = true;
//
//                }
//
//                // En till höger och en till vänster
//                if (flick) {
//
//                    //kalla metod här
//                    beatnumber++;
//
//                    shakeIsHappening = false;
//                }
//
//                if(pull) {
//
//                    beatnumber--;
//
//                    shakeIsHappening = false;
//                }
//
//                z.clear();
//
//            } }
//
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}