package com.zhouk.zxing;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

import com.zhouk.zxing.callback.ZxingCallBack;

public class QRScanActivity extends Activity implements ZxingCallBack {
    public static int SCAN_CODE = 1000;
    private SurfaceView preview_view;
    private ImageView scopIm;
    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscan);

        preview_view = findViewById(R.id.preview_view);
        scopIm = findViewById(R.id.scanBox);
        new CaptureImpl(this).setSurfaceView(preview_view);
    }

    @Override
    public void scanResult(Result rawResult, Bitmap barcode) {
        Intent intent = new Intent();
        intent.putExtra("code",rawResult.getText());
        setResult(RESULT_OK,intent);
        finish();
    }

    @Override
    public View getScopImage() {
        return scopIm;
    }

    @Override
    public SurfaceView getSurfaceView() {
        return preview_view;
    }

    @Override
    public Activity getContext() {
        return this;
    }
}