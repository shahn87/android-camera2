package com.ahnstory.camera2tutorial.camera;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sohyun Ahn on 2016. 9. 27..
 * If you have a question, please send an e-mail (sohyun.ahn.sh@gmail.com)
 */

public class LollipopCameraManager {

    private static final String TAG = "LollipopCameraManager";

    @NonNull private final Context mContext;
    @NonNull private final Handler mHandler = new Handler();

    @NonNull private final CameraManager mCameraManager;
    @Nullable private CameraDevice mCameraDevice;
    @Nullable private CameraTextureView mTextureView;
    @Nullable private Size mPreviewSize;
    @Nullable private CaptureRequest.Builder mPreviewRequestBuilder;

    @NonNull private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            Log.d(TAG, "onOpened");
            mCameraDevice = camera;
            if (mTextureView != null && mPreviewSize != null) {
                SurfaceTexture texture = mTextureView.getSurfaceTexture();
                texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

                Surface surface = new Surface(texture);
                List<Surface> surfaces = new ArrayList<>();
                surfaces.add(surface);
                try {
                    mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                    mPreviewRequestBuilder.addTarget(surface);
                    camera.createCaptureSession(surfaces, mSessionStateCallback, mHandler);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e(TAG, "Surface is not linked.");
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            Log.d(TAG, "onDisconnected");
            camera.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            Log.d(TAG, "onError");
            camera.close();
            mCameraDevice = null;
        }
    };

    @NonNull private final CameraCaptureSession.StateCallback mSessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            if (mCameraDevice != null) {
                try {
                    if (mPreviewRequestBuilder != null)
                        session.setRepeatingRequest(mPreviewRequestBuilder.build(), null, mHandler);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            session.close();
        }
    };

    public LollipopCameraManager(@NonNull Context context) {
        mContext = context;
        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
    }

    public void setTextureView(@NonNull CameraTextureView textureView) {
        mTextureView = textureView;
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    public void openCamera(int textureWidth, int textureHeight) {
        Log.d(TAG, "openCamera()");
        int permissionCheck = ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Camera permission is denied.");
            return;
        }

        try {
            for (String cameraId : mCameraManager.getCameraIdList()) {
                CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraId);
                Integer lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_FRONT) {
                    setCameraParameters(characteristics, textureWidth, textureHeight);
                    configureTransform(textureWidth, textureHeight);
                    mCameraManager.openCamera(cameraId, mStateCallback, mHandler);
                    break;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void closeCamera() {
        Log.d(TAG, "closeCamera()");
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
        mPreviewSize = null;
    }

    private void setCameraParameters(CameraCharacteristics characteristics, int textureWidth, int textureHeight) {
        StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        if (map != null) {
            mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), 0.75f, 2048);
            if (mPreviewSize != null && mTextureView != null) {
                mTextureView.setAspectRatio(mContext.getResources().getConfiguration().orientation, mPreviewSize.getWidth(), mPreviewSize.getHeight());
            }
        }

    }

    @Nullable private Size chooseOptimalSize(@NonNull Size[] sizes, float optimalRatio, int maxSize) {
        for (Size size : sizes) {
            if (size.getWidth() < maxSize && size.getHeight() < maxSize) {
                float ratio = (float) size.getHeight() / size.getWidth();
                if (Math.abs(ratio - optimalRatio) < 0.01 ) {
                    Log.i(TAG, "Optimal size is [" + size.getWidth() + ", " + size.getHeight() + "]");
                    return size;
                }
            }
        }
        Log.e(TAG, "Couldn't find optimal size. ");
        return null;
    }

    private void configureTransform(int viewWidth, int viewHeight) {
        if (mTextureView != null && mPreviewSize != null) {

            RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
            RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
            float centerX = viewRect.centerX();
            float centerY = viewRect.centerY();

            Matrix matrix = new Matrix();
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            mTextureView.setTransform(matrix);
        }
    }
}
