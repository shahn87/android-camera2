package com.ahnstory.camera2tutorial.camera;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.TextureView;

/**
 * Created by Sohyun Ahn on 2016. 10. 3..
 * If you have a question, please send an e-mail (sohyun.ahn.sh@gmail.com)
 */

public class CameraTextureView extends TextureView {

    private int mRatioWidth = 0;
    private int mRatioHeight = 0;

    public CameraTextureView(Context context) {
        super(context);
    }

    public CameraTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CameraTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setAspectRatio(int screenOrientation, int width, int height) {
        switch (screenOrientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                mRatioWidth = height;
                mRatioHeight = width;
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                mRatioWidth = width;
                mRatioHeight = height;
                break;
        }
        requestLayout();
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (mRatioWidth == 0 || mRatioHeight == 0) {
            setMeasuredDimension(width, height);
        } else {
            if (width < height * mRatioWidth / mRatioHeight) {
                setMeasuredDimension(width, width * mRatioHeight / mRatioWidth);
            } else {
                setMeasuredDimension(height * mRatioWidth / mRatioHeight, height);
            }
        }
    }
}
