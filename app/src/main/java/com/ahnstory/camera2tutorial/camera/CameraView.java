package com.ahnstory.camera2tutorial.camera;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by Sohyun Ahn on 2016. 9. 27..
 * If you have a question, please send an e-mail (sohyun.ahn.sh@gmail.com)
 */

public class CameraView extends SurfaceView {

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void addSurfaceHolderCallback(SurfaceHolder.Callback callback) {
        getHolder().addCallback(callback);
    }
}
