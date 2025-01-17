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

        import android.content.Context;
        import android.graphics.Bitmap;
        import android.graphics.BitmapFactory;
        import android.media.Image;
        import android.net.Uri;
        import android.support.v4.content.res.ResourcesCompat;
        import android.util.Log;
        import android.widget.ImageView;

        import com.google.ar.core.AugmentedImage;
        import com.google.ar.core.examples.java.helloar.Gallery;
        import com.google.ar.core.examples.java.helloar.ImageSaver;
        import com.google.ar.core.examples.java.helloar.R;
        import com.google.ar.sceneform.AnchorNode;
        import com.google.ar.sceneform.Node;
        import com.google.ar.sceneform.math.Quaternion;
        import com.google.ar.sceneform.math.Vector3;
        import com.google.ar.sceneform.rendering.ModelRenderable;
        import com.google.ar.sceneform.rendering.ViewRenderable;
        import com.google.ar.sceneform.ux.ArFragment;
        import com.google.ar.sceneform.ux.TransformableNode;

        import java.io.File;
        import java.util.concurrent.CompletableFuture;

/**
 * Node for rendering an augmented image. The image is framed by placing the virtual picture frame
 * at the corners of the augmented image trackable.
 */
@SuppressWarnings({"AndroidApiChecker"})
public class AugmentedImageNode extends AnchorNode {

    private static final String TAG = "AugmentedImageNode";
    private ArFragment arFragment;

    // The augmented image represented by this node.
    private AugmentedImage image;

    TransformableNode nodes[] = new TransformableNode[6];

    Vector3 localPosition1[] = new Vector3[3];
    Vector3 localPosition2[] = new Vector3[3];
    Vector3 localPosition3[] = new Vector3[3];
    Vector3 localPosition4[] = new Vector3[3];
    Vector3 localPosition5[] = new Vector3[3];
    Vector3 localPosition6[] = new Vector3[3];

    private CompletableFuture<ViewRenderable> RenderablePicture1;
    private CompletableFuture<ViewRenderable> RenderablePicture2;
    private CompletableFuture<ViewRenderable> RenderablePicture3;
    private CompletableFuture<ViewRenderable> RenderablePicture4;
    private CompletableFuture<ViewRenderable> RenderablePicture5;
    private CompletableFuture<ViewRenderable> RenderablePicture6;


    public AugmentedImageNode(Context context, ArFragment arFragment) {
        this.arFragment = arFragment;

        ImageView[] imageViews = new ImageView[6];
        for(int i = 0; i < 6; i++){
            imageViews[i] = new ImageView(context);
            imageViews[i].setForeground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.picture_frame, null));

        }

        int amountOfImages = Gallery.amountOfImages;
        Log.d("AMOUNT", "amount: " + amountOfImages);

        for (int i = 0; i < amountOfImages; i++) {
            int currentPicture = 2131230815 + i;
            Bitmap bitmap = new ImageSaver(context).
                    setFileName(currentPicture + ".png").
                    setDirectoryName("images").
                    load();
            if (bitmap == null) {
                bitmap = BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.pictureplaceholder);
            }
            imageViews[i].setImageBitmap(bitmap);
        }

        if (RenderablePicture1 == null) {
            RenderablePicture1 = ViewRenderable.builder()
                    .setView(context, imageViews[0])
                    .build();

            RenderablePicture2 = ViewRenderable.builder()
                    .setView(context, imageViews[1])
                    .build();

            RenderablePicture3 = ViewRenderable.builder()
                    .setView(context, imageViews[2])
                    .build();

            RenderablePicture4 = ViewRenderable.builder()
                    .setView(context, imageViews[3])
                    .build();

            RenderablePicture5 = ViewRenderable.builder()
                    .setView(context, imageViews[4])
                    .build();

            RenderablePicture6 = ViewRenderable.builder()
                    .setView(context, imageViews[5])
                    .build();
        }
    }

    /**
     * Called when the AugmentedImage is detected and should be rendered. A Sceneform node tree is
     * created based on an Anchor created from the image. The corners are then positioned based on the
     * extents of the image. There is no need to worry about world coordinates since everything is
     * relative to the center of the image, which is the parent node of the corners.
     */
    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    public void setImage(AugmentedImage image) {
        this.image = image;

        if (!RenderablePicture1.isDone() || !RenderablePicture2.isDone() || !RenderablePicture3.isDone() || !RenderablePicture4.isDone() || !RenderablePicture5.isDone() || !RenderablePicture6.isDone()) {
            CompletableFuture.allOf(RenderablePicture1, RenderablePicture2, RenderablePicture3, RenderablePicture4, RenderablePicture5, RenderablePicture6)
                    .thenAccept((Void aVoid) -> setImage(image))
                    .exceptionally(
                            throwable -> {
                                Log.e(TAG, "Exception loading", throwable);
                                return null;
                            });
            return;
        }

        // Set the anchor based on the center of the image.
        setAnchor(image.createAnchor(image.getCenterPose()));

        // Picture 1

        TransformableNode PictureNode1 = new TransformableNode(arFragment.getTransformationSystem());
        nodes[0] = PictureNode1;

        localPosition1[0] = new Vector3(image.getExtentX() * 0.6f, 0f, 0.6f * image.getExtentZ());
        localPosition1[1] = new Vector3(image.getExtentX() * -0.5f, 0f, 0.4f * image.getExtentZ());
        localPosition1[2] = new Vector3(image.getExtentX() * 0.05f, 0f, 0.3f * image.getExtentZ());

        PictureNode1.setParent(this);
        PictureNode1.getRotationController().setEnabled(false);
        PictureNode1.getTranslationController().setEnabled(false);
        PictureNode1.setWorldRotation(Quaternion.axisAngle(new Vector3(1f, 0f, 0f), -15f));
        PictureNode1.setLocalScale(new Vector3(3.0f, 3.0f, 3.0f));
        PictureNode1.setRenderable(RenderablePicture1.getNow(null));
        arFragment.getTransformationSystem().getSelectionVisualizer().removeSelectionVisual(PictureNode1);

        // Picture 2

        TransformableNode PictureNode2 = new TransformableNode(arFragment.getTransformationSystem());
        nodes[1] = PictureNode2;

        localPosition2[0] = new Vector3(image.getExtentX() * -0.65f, 0f, -0.45f * image.getExtentZ());
        localPosition2[1] = new Vector3(image.getExtentX() * 0.7f, 0f, 0.7f * image.getExtentZ());
        localPosition2[2] = new Vector3(image.getExtentX() * 0.5f, 0f, -0.7f * image.getExtentZ());

        PictureNode2.setParent(this);
        PictureNode2.getRotationController().setEnabled(false);
        PictureNode2.getTranslationController().setEnabled(false);
        PictureNode2.setWorldRotation(Quaternion.axisAngle(new Vector3(1f, 0f, 0f), -15f));
        PictureNode2.setLocalScale(new Vector3(2.0f, 2.0f, 2.0f));
        PictureNode2.setRenderable(RenderablePicture2.getNow(null));
        arFragment.getTransformationSystem().getSelectionVisualizer().removeSelectionVisual(PictureNode2);

        // Picture 3

        TransformableNode PictureNode3 = new TransformableNode(arFragment.getTransformationSystem());
        nodes[2] = PictureNode3;

        localPosition3[0] = new Vector3(image.getExtentX() * 0.5f, 0f, -0.5f * image.getExtentZ());
        localPosition3[1] = new Vector3(image.getExtentX() * 0.6f, 0f, -0.3f * image.getExtentZ());
        localPosition3[2] = new Vector3(image.getExtentX() * -0.6f, 0f, -0.6f * image.getExtentZ());

        PictureNode3.setParent(this);
        PictureNode3.getRotationController().setEnabled(false);
        PictureNode3.getTranslationController().setEnabled(false);
        PictureNode3.setWorldRotation(Quaternion.axisAngle(new Vector3(1f, 0f, 0f), -15f));
        PictureNode3.setLocalScale(new Vector3(3.0f, 3.0f, 3.0f));
        PictureNode3.setRenderable(RenderablePicture3.getNow(null));
        arFragment.getTransformationSystem().getSelectionVisualizer().removeSelectionVisual(PictureNode3);

        // Picture 4

        TransformableNode PictureNode4 = new TransformableNode(arFragment.getTransformationSystem());
        nodes[3] = PictureNode4;

        localPosition4[0] = new Vector3(image.getExtentX() * -0.45f, 0f, 0.45f * image.getExtentZ());
        localPosition4[1] = new Vector3(image.getExtentX() * -0.6f, 0f, -0.55f * image.getExtentZ());
        localPosition4[2] = new Vector3(image.getExtentX() * 0.9f, 0f, 0.4f * image.getExtentZ());

        PictureNode4.setParent(this);
        PictureNode4.getRotationController().setEnabled(false);
        PictureNode4.getTranslationController().setEnabled(false);
        PictureNode4.setWorldRotation(Quaternion.axisAngle(new Vector3(1f, 0f, 0f), -15f));
        PictureNode4.setLocalScale(new Vector3(1.0f, 1.0f, 1.0f));
        PictureNode4.setRenderable(RenderablePicture4.getNow(null));
        arFragment.getTransformationSystem().getSelectionVisualizer().removeSelectionVisual(PictureNode4);

        // Picture 5

        TransformableNode PictureNode5 = new TransformableNode(arFragment.getTransformationSystem());
        nodes[4] = PictureNode5;

        localPosition5[0] = new Vector3(image.getExtentX() * -0.45f, 0f, 0.45f * image.getExtentZ());
        localPosition5[1] = new Vector3(image.getExtentX() * -0.6f, 0f, -0.55f * image.getExtentZ());
        localPosition5[2] = new Vector3(image.getExtentX() * 0.9f, 0f, 0.4f * image.getExtentZ());

        PictureNode5.setParent(this);
        PictureNode5.getRotationController().setEnabled(false);
        PictureNode5.getTranslationController().setEnabled(false);
        PictureNode5.setWorldRotation(Quaternion.axisAngle(new Vector3(1f, 0f, 0f), -15f));
        PictureNode5.setLocalScale(new Vector3(1.0f, 1.0f, 1.0f));
        PictureNode5.setRenderable(RenderablePicture5.getNow(null));
        arFragment.getTransformationSystem().getSelectionVisualizer().removeSelectionVisual(PictureNode5);

        // Picture 6

        TransformableNode PictureNode6 = new TransformableNode(arFragment.getTransformationSystem());
        nodes[5] = PictureNode6;

        localPosition6[0] = new Vector3(image.getExtentX() * -0.45f, 0f, 0.45f * image.getExtentZ());
        localPosition6[1] = new Vector3(image.getExtentX() * -0.6f, 0f, -0.55f * image.getExtentZ());
        localPosition6[2] = new Vector3(image.getExtentX() * 0.9f, 0f, 0.4f * image.getExtentZ());

        PictureNode6.setParent(this);
        PictureNode6.getRotationController().setEnabled(false);
        PictureNode6.getTranslationController().setEnabled(false);
        PictureNode6.setWorldRotation(Quaternion.axisAngle(new Vector3(1f, 0f, 0f), -15f));
        PictureNode6.setLocalScale(new Vector3(1.0f, 1.0f, 1.0f));
        PictureNode6.setRenderable(RenderablePicture6.getNow(null));
        arFragment.getTransformationSystem().getSelectionVisualizer().removeSelectionVisual(PictureNode6);

    }

    public void setComposition(int compNbr){

        nodes[0].setLocalPosition(localPosition1[compNbr]);
        nodes[1].setLocalPosition(localPosition2[compNbr]);
        nodes[2].setLocalPosition(localPosition3[compNbr]);
        nodes[3].setLocalPosition(localPosition4[compNbr]);
        nodes[4].setLocalPosition(localPosition5[compNbr]);
        nodes[5].setLocalPosition(localPosition6[compNbr]);
    }

    public AugmentedImage getImage() {
        return image;
    }
}