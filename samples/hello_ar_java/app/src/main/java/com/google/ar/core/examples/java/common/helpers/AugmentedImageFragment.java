
/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.ar.core.examples.java.common.helpers;

        import android.Manifest;
        import android.app.ActivityManager;
        import android.content.Context;
        import android.content.Intent;
        import android.content.pm.PackageManager;
        import android.content.res.AssetManager;
        import android.graphics.Bitmap;
        import android.graphics.BitmapFactory;
        import android.os.Build;
        import android.os.Bundle;
        import android.provider.MediaStore;
        import android.support.annotation.Nullable;
        import android.support.v4.app.ActivityCompat;
        import android.support.v4.content.ContextCompat;
        import android.util.Log;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import com.google.ar.core.AugmentedImageDatabase;
        import com.google.ar.core.Config;
        import com.google.ar.core.Session;
        import com.google.ar.core.examples.java.common.helpers.SnackbarHelper;
        import com.google.ar.sceneform.ux.ArFragment;
        import java.io.IOException;
        import java.io.InputStream;

/**
 * Extend the ArFragment to customize the ARCore session configuration to include Augmented Images.
 */
public class AugmentedImageFragment extends ArFragment {
    private static final String TAG = "AugmentedImageFragment";

    // This is the name of the image in the sample database.  A copy of the image is in the assets
    // directory.  Opening this image on your computer is a good quick way to test the augmented image
    // matching.
    private static final String DEFAULT_IMAGE_NAME = "default.jpg";

    // Add a Uri that stores the path of the target image chosen from
    // device storage.
    private android.net.Uri chosenImageUri = null;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    // Do a runtime check for the OpenGL level available at runtime to avoid Sceneform crashing the
    // application.
    private static final double MIN_OPENGL_VERSION = 3.0;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // Check for Sceneform being supported on this device.  This check will be integrated into
        // Sceneform eventually.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Log.e(TAG, "Sceneform requires Android N or later");
            SnackbarHelper.getInstance()
                    .showError(getActivity(), "Sceneform requires Android N or later");
        }

        String openGlVersionString =
                ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE))
                        .getDeviceConfigurationInfo()
                        .getGlEsVersion();
        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 or later");
            SnackbarHelper.getInstance()
                    .showError(getActivity(), "Sceneform requires OpenGL ES 3.0 or later");
        }


        onTakePicture();
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        // Turn off the plane discovery since we're only looking for images
        getPlaneDiscoveryController().hide();
        getPlaneDiscoveryController().setInstructionView(null);
        getArSceneView().getPlaneRenderer().setEnabled(false);
        return view;
    }


    // TODO Implementera Olivias imageSaver lösning istället.

    // Add a new function that prompts the user to choose an image from
    // device storage.

    // Borde göras om till att man bara tar en bild direkt och då kan man
    // även sätta in gränser för hur bra bild man valt med hjälp av Arcoreimg tool
//    void chooseNewImage() {
//        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_GET_CONTENT);
//        intent.addCategory(android.content.Intent.CATEGORY_OPENABLE);
//        intent.setType("image/*");
//        startActivityForResult(
//                android.content.Intent.createChooser(intent, "Select target augmented image"),
//                REQUEST_CODE_CHOOSE_IMAGE);
//    }

    public void onTakePicture() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, 110);
            dispatchTakePictureIntent();
        } else {
            dispatchTakePictureIntent();
        }

    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    // Add a new onActivityResult function to handle the user-selected
    // image, and to reconfigure the ARCore session in the internal
    // ArSceneView.
    @Override
    public void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (resultCode == android.app.Activity.RESULT_OK) {
                if (requestCode == REQUEST_IMAGE_CAPTURE) {
                    // Get the Uri of target image
                    chosenImageUri = data.getData();

                    // Reconfig ARCore session to use the new image
                    Session arcoreSession = getArSceneView().getSession();
                    Config config = getSessionConfiguration(arcoreSession);
                    config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);
                    arcoreSession.configure(config);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "onActivityResult - target image selection error ", e);
        }
    }

    @Override
    protected Config getSessionConfiguration(Session session) {
        Config config = super.getSessionConfiguration(session);
        if (!setupAugmentedImageDatabase(config, session)) {
            SnackbarHelper.getInstance()
                    .showError(getActivity(), "Could not setup augmented image database");
        }
        return config;
    }

    private boolean setupAugmentedImageDatabase(Config config, Session session) {
        AugmentedImageDatabase augmentedImageDatabase;

        AssetManager assetManager = getContext() != null ? getContext().getAssets() : null;
        if (assetManager == null) {
            Log.e(TAG, "Context is null, cannot intitialize image database.");
            return false;
        }

        Bitmap augmentedImageBitmap = loadAugmentedImageBitmap(assetManager);
        if (augmentedImageBitmap == null) {
            return false;
        }

        augmentedImageDatabase = new AugmentedImageDatabase(session);
        augmentedImageDatabase.addImage(DEFAULT_IMAGE_NAME, augmentedImageBitmap);

        config.setAugmentedImageDatabase(augmentedImageDatabase);
        return true;
    }

    private Bitmap loadAugmentedImageBitmap(AssetManager assetManager) {
        if (chosenImageUri == null) {
            try (InputStream is = assetManager.open(DEFAULT_IMAGE_NAME)) {
                return BitmapFactory.decodeStream(is);
            } catch (IOException e) {
                Log.e(TAG, "IO exception loading augmented image bitmap.", e);
            }
        } else {
            try (InputStream is = getContext().getContentResolver().openInputStream(chosenImageUri)) {
                return BitmapFactory.decodeStream(is);
            } catch (IOException e) {
                Log.e(TAG, "IO exception loading augmented image bitmap from storage.", e);
            }
        }
        return null;
    }
}