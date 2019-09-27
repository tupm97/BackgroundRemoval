package com.example.backgroundremoval.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.backgroundremoval.R;
import com.example.backgroundremoval.lib.BitmapUtils;
import com.example.backgroundremoval.lib.Config;
import com.example.backgroundremoval.lib.MaskType;
import com.example.backgroundremoval.lib.PeopleSegmentMediumOnDeviceModel;
import com.example.backgroundremoval.lib.Vision;
import com.example.backgroundremoval.lib.VisionImage;
import com.example.backgroundremoval.lib.VisionOrientation;
import com.example.backgroundremoval.lib.VisionSegmentPredictor;
import com.example.backgroundremoval.lib.VisionSegmentPredictorOptions;
import com.example.backgroundremoval.lib.VisionSegmentResult;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;


public class MainActivity extends BaseCameraActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int SELECT_IMAGE = 1;
    private AtomicBoolean shouldSample = new AtomicBoolean(true);
    private VisionSegmentPredictor predictor;
    private int imgRotation;

    private VisionSegmentResult segmentResult;
    private VisionImage visionImage;

    Button snapshotButton;
    Button switchCamera;
    Button btnPredict;
    RelativeLayout previewLayout;
    RelativeLayout snapshotLayout;
    OverlayView snapshotOverlay;
    ProgressBar snapshotProcessingSpinner;
    Button closeButton;

    private Bitmap backgroundBitmap;

    private Bitmap picture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Config.configure(getApplicationContext());
        PeopleSegmentMediumOnDeviceModel onDeviceModel = new PeopleSegmentMediumOnDeviceModel();
        VisionSegmentPredictorOptions options = new VisionSegmentPredictorOptions.Builder()
                .targetConfidenceThreshold(.4f)
                .build();
        predictor = Vision.ImageSegmentation.getPredictor(onDeviceModel, options);
    }

    @Override
    protected void onPreviewSizeChosen(Size previewSize, final Size cameraViewSize, final int rotation) {
        Log.d(TAG, "onPreviewSizeChosen: rotation "+rotation);
        imgRotation = VisionOrientation.getImageRotationFromCamera(this, cameraId);
        Log.d(TAG, "onPreviewSizeChosen: rotation2 "+imgRotation);
        snapshotButton = findViewById(R.id.take_picture_btn);
        previewLayout = findViewById(R.id.preview_frame);
        snapshotLayout = findViewById(R.id.snapshot_frame);
        snapshotOverlay = findViewById(R.id.snapshot_view);
        closeButton = findViewById(R.id.close_btn);
        switchCamera=findViewById(R.id.btn_switch_camera);
        snapshotProcessingSpinner = findViewById(R.id.snapshotProcessingSpinner);
        btnPredict=findViewById(R.id.predict);

        snapshotOverlay.setCallBack(new OverlayView.DrawCallBack() {
            @Override
            public void drawCallBack(Canvas canvas) {

                if(segmentResult==null){
                    Matrix matrix= new Matrix();
                    Bitmap output=visionImage.getBitmap();
                    Bitmap result= BitmapUtils.resize(output,cameraViewSize.getWidth(),cameraViewSize.getHeight());
                    result=BitmapUtils.rotate(result,rotation);
                    canvas.drawBitmap(result,null,new RectF(0,0,cameraViewSize.getWidth(),cameraViewSize.getHeight()),null);
                    return;
                }

                // Show the background replacement
                Bitmap.Config conf= Bitmap.Config.ARGB_8888;
                Bitmap bg=Bitmap.createBitmap(cameraViewSize.getWidth(),cameraViewSize.getHeight(),conf);
                Bitmap scaledBackgroundBitmap = BitmapUtils.resize(bg, cameraViewSize.getWidth(), cameraViewSize.getHeight());
                canvas.drawBitmap(scaledBackgroundBitmap, new Matrix(), new Paint());
                canvas.drawBitmap(bg, new Matrix(), new Paint());

                // Draw the masked bitmap
                long startTime = System.currentTimeMillis();
                // Use a max alpha of 255 so that there isn't any transparency in the mask.
                Bitmap maskedBitmap = segmentResult.buildSingleClassMask(MaskType.PERSON, 255, .5f, .5f);
                Bitmap croppedMask = visionImage.mask(maskedBitmap, false);
                Log.d(TAG, "Masked bitmap took " + (System.currentTimeMillis() - startTime) + "ms to create.");

                if (croppedMask != null) {
                    canvas.drawBitmap(BitmapUtils.resize(croppedMask,cameraViewSize.getWidth(),cameraViewSize.getHeight()),new Matrix(), new Paint());
                }
            }
        });


        snapshotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!shouldSample.compareAndSet(true, false)) {
                    return;
                }
                getFragment1().getSurfaceView().setVisibility(View.GONE);
                segmentResult=null;
                snapshotOverlay.postInvalidate();
                showSnapshotLayout();
            }
        });
        btnPredict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: click");
                snapshotOverlay.postInvalidate();
//                showSpinner();
                runInBackground(new Runnable() {
                    @Override
                    public void run() {

                        showSnapshotLayout();
                        showSpinner();
                        segmentResult=predictor.predict(visionImage);
                        hideSpinner();
                        snapshotOverlay.postInvalidate();
                    }
                });
            }
        });

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPreviewLayout();
                shouldSample.set(true);
                backgroundBitmap = null;
                showButton();
                getFragment1().getSurfaceView().setVisibility(View.VISIBLE);
            }
        });
        switchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragment1().switchCamera();
                setCameraId(getFragment1().getCameraId());
                imgRotation= VisionOrientation.getImageRotationFromCamera(MainActivity.this, getFragment1().getCameraId());

                Log.d(TAG, "onClick: imgRotation: "+imgRotation);
            }
        });
    }
    private void showSpinner() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: progress bar running");
                snapshotProcessingSpinner.setVisibility(View.VISIBLE);
                disableButton();
            }
        });
    }

    private void enableButton(){
       // selectBackgroundBtn.setEnabled(true);
        btnPredict.setEnabled(true);
    }

    private void disableButton(){
       // selectBackgroundBtn.setEnabled(false);
        btnPredict.setEnabled(false);
    }

    private void hideSpinner() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                snapshotProcessingSpinner.setVisibility(View.GONE);
                enableButton();
            }
        });
    }

    private void showSnapshotLayout() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                previewLayout.setVisibility(View.GONE);
                snapshotLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showButton(){
       // selectBackgroundBtn.setVisibility(View.VISIBLE);
        btnPredict.setVisibility(View.VISIBLE);
    }

    private void hidenButton(){
      //  selectBackgroundBtn.setVisibility(View.GONE);
        btnPredict.setVisibility(View.GONE);
    }

    private void showPreviewLayout() {
        previewLayout.setVisibility(View.VISIBLE);
        snapshotLayout.setVisibility(View.GONE);
    }



    @Override
    protected int getLayoutId() {
        return R.layout.camera_connection;
    }

    @Override
    public void onImageAvailable(ImageReader reader) {

        Log.d(TAG, "onImageAvailable: read Image");
        Image image=reader.acquireLatestImage();
        if (image==null) return;

        if (!shouldSample.get()) {
            image.close();
            return;
        }
        visionImage= VisionImage.fromMediaImage(image,imgRotation);
        image.close();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != SELECT_IMAGE) {
            return;
        }
        if (resultCode == Activity.RESULT_CANCELED) {
            showButton();
            Toast.makeText(this, "Canceled", Toast.LENGTH_SHORT).show();
            return;
        }

        if (resultCode == Activity.RESULT_OK) {
            if (data == null) {
                showButton();
                return;
            }
            try {
                hidenButton();
                Uri selectedPicture = data.getData();
                Log.d(TAG, "IMAGE CHOSEN: " + selectedPicture);

                InputStream inputStream = getContentResolver().openInputStream(selectedPicture);
                ExifInterface exif = new ExifInterface(inputStream);
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

                backgroundBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedPicture);

                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        backgroundBitmap = BitmapUtils.rotate(backgroundBitmap, 0);
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        backgroundBitmap = BitmapUtils.rotate(backgroundBitmap, 270);
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        backgroundBitmap = BitmapUtils.rotate(backgroundBitmap, 180);
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
