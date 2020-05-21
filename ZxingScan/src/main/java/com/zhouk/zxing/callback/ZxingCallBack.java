package com.zhouk.zxing.callback;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.SurfaceView;
import android.view.View;

import com.zhouk.zxing.Result;

public interface ZxingCallBack {
    /**
     * 扫码结果
     * @param rawResult 结果
     * @param barcode 识别图片
     */
    void scanResult(Result rawResult, Bitmap barcode);

    /**
     * 扫码框
     * @return
     */
    View getScopImage();

    /**
     * @return SurfaceView
     */
    SurfaceView getSurfaceView();

    /**
     * 当前activity
     * @return
     */
    Activity getContext();
}
