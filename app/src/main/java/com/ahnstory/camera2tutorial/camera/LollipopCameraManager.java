package com.ahnstory.camera2tutorial.camera;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Surface;

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
    @Nullable private Surface mPreviewSurface;

    public LollipopCameraManager(@NonNull Context context) {
        mContext = context;
        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        mCameraManager.registerAvailabilityCallback(new CameraManager.AvailabilityCallback() {
            @Override
            public void onCameraAvailable(@NonNull String cameraId) {
                super.onCameraAvailable(cameraId);
                Log.d(TAG, Thread.currentThread().getName() + "onCameraAvailable");
            }

            @Override
            public void onCameraUnavailable(@NonNull String cameraId) {
                super.onCameraUnavailable(cameraId);
                Log.d(TAG, Thread.currentThread().getName() + "onCameraUnavailable");
            }
        }, mHandler);
    }

    public void getPreviewSurface(final Surface surface) {
        if (mCameraDevice != null) {
            try {
                List<Surface> surfaces = new ArrayList<>();
                surfaces.add(surface);
                mCameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession session) {
                        Log.d(TAG, "onConfigured");
                        if (mPreviewSurface != null) {
                            try {
                                CaptureRequest.Builder builder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                                builder.addTarget(mPreviewSurface);
                                session.setRepeatingRequest(builder.build(), new CameraCaptureSession.CaptureCallback() {
                                }, mHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                        Log.d(TAG, "onConfigurFailed");
                        session.close();
                    }
                }, mHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }

        }
        mPreviewSurface = surface;
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    public void openCamera() {
        int permissionCheck = ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            try {
                for (String cameraId : mCameraManager.getCameraIdList()) {
                    CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraId);
                    Integer lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING);
                    if (lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_FRONT) {
                        mCameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                            @Override
                            public void onOpened(@NonNull CameraDevice camera) {
                                Log.d(TAG, "onOpened");
                                if (mPreviewSurface != null) {
                                    try {
                                        List<Surface> surfaces = new ArrayList<>();
                                        surfaces.add(mPreviewSurface);
                                        camera.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
                                            @Override
                                            public void onConfigured(@NonNull CameraCaptureSession session) {
                                            }

                                            @Override
                                            public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                                                session.close();
                                            }
                                        }, mHandler);
                                    } catch (CameraAccessException e) {
                                        e.printStackTrace();
                                    }
                                }
                                mCameraDevice = camera;
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
                        }, mHandler);
                        break;
                    }
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "Camera permission is denied.");
        }
    }

    public void closeCamera() {

    }
}
