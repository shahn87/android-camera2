package com.ahnstory.camera2tutorial;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import com.ahnstory.camera2tutorial.camera.CameraTextureView;
import com.ahnstory.camera2tutorial.camera.LollipopCameraManager;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 0;
    private final String TAG = "MainActivity";

    @Nullable private LollipopCameraManager mCameraManager;
    @Nullable private CameraTextureView mCameraTextureView;
//    @Nullable private CameraView mCameraView;

    @NonNull private final TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            Log.d(TAG, "onSurfaceTextureAvailable");
            if (mCameraManager != null) {
                //mCameraManager.setPreviewSurface(new Surface(surface));
                if (checkPermission(Manifest.permission.CAMERA)) {
                    mCameraManager.openCamera(width, height);
                }
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCameraTextureView = (CameraTextureView) findViewById(R.id.camera_texture_view);
        if (mCameraTextureView != null) {
            mCameraTextureView.setSurfaceTextureListener(mSurfaceTextureListener);

            mCameraManager = new LollipopCameraManager(this);
            mCameraManager.setTextureView(mCameraTextureView);
        }
//        mCameraView = (CameraView) findViewById(R.id.camera_view);
//        if (mCameraView != null) {
//            mCameraView.addSurfaceHolderCallback(new SurfaceHolder.Callback() {
//                @Override
//                public void surfaceCreated(SurfaceHolder holder) {
//                    if (mCameraManager != null) {
//                        mCameraManager.setPreviewSurface(mCameraView.getHolder().getSurface());
//                    }
//                }
//
//                @Override
//                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//                }
//
//                @Override
//                public void surfaceDestroyed(SurfaceHolder holder) {
//
//                }
//            });
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (checkPermission(Manifest.permission.CAMERA) && mCameraManager != null
                && mCameraTextureView != null && mCameraTextureView.isAvailable()) {
            mCameraManager.openCamera(mCameraTextureView.getWidth(), mCameraTextureView.getHeight());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCameraManager != null)
            mCameraManager.closeCamera();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    if (mCameraManager != null) {
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        if (mCameraTextureView != null && mCameraTextureView.isAvailable())
                            mCameraManager.openCamera(mCameraTextureView.getWidth(), mCameraTextureView.getHeight());
                    }
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.e(TAG, "Camera permission is denied.");
                    finish();
                }
                break;
        }
    }

    private boolean checkPermission(String permission) {
        if (ContextCompat.checkSelfPermission(this,
                permission)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    permission)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{permission},
                        MY_PERMISSIONS_REQUEST_CAMERA);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
            return false;
        }
        return true;
    }
}
