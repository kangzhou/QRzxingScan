/*
 * Copyright (C) 2008 ZXing authors
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

package com.zhouk.zxing;

import android.app.Activity;
import android.app.FragmentManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.zhouk.zxing.callback.ResultCallBack;
import com.zhouk.zxing.callback.ZxingCallBack;
import com.zhouk.zxing.callback.lifecallback.LifeListener;
import com.zhouk.zxing.camera.CameraManager;

import java.io.IOException;

public class CaptureImpl implements ResultCallBack {
    private static final String TAG = CaptureImpl.class.getSimpleName();
    private InactivityTimer inactivityTimer;
    private CameraManager cameraManager;
    private CaptureImplHandler handler;
    private BeepManager beepManager;
    private AmbientLightManager ambientLightManager;
    private Rect scanRect;
    private ZxingCallBack callBack;
    private SurfaceHolder.Callback shCallback;
    private boolean hasSurface;
    private SurfaceHolder surfaceHolder;
    private SurfaceView surfaceView;
    private ResultCallBack resultCallBack;
    private double cropWight = -1;
    private double cropHeight = -1;

    public CaptureImpl(ZxingCallBack callBack) {
        this.callBack = callBack;
        resultCallBack = this;
        hasSurface = false;
        resultCallBack.onCreate();
        addLifeListener(callBack.getContext());
    }

    @Override
    public void onCreate(){
        inactivityTimer = new InactivityTimer(callBack.getContext());
        beepManager = new BeepManager(callBack.getContext());
        ambientLightManager = new AmbientLightManager(callBack.getContext());

        cameraManager = new CameraManager(callBack.getContext());
        beepManager.updatePrefs();
        ambientLightManager.start(cameraManager);

        surfaceView = callBack.getSurfaceView();
        if (surfaceView == null) {
            throw new IllegalStateException("surfaceView is null!");
        }
        surfaceHolder = surfaceView.getHolder();
        shCallback = new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (holder == null) {
                    Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
                }
                if (!hasSurface) {
                    hasSurface = true;
                    initCamera(holder);
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                hasSurface = false;
            }
        };
    }

    @Override
    public void onResume() {
        cameraManager = new CameraManager(callBack.getContext().getApplication());
        handler = null;
        beepManager.updatePrefs();
        ambientLightManager.start(cameraManager);
        surfaceHolder.addCallback(shCallback);
    }

    @Override
    public void onPause() {
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        inactivityTimer.onPause();
        ambientLightManager.stop();
        beepManager.close();
        cameraManager.closeDriver();
        if (!hasSurface) {
            surfaceHolder.removeCallback(shCallback);
        }
    }

    @Override
    public void onDestroy() {
        inactivityTimer.shutdown();
    }

    @Override
    public void handleResult(Result rawResult, Bitmap barcode, float scaleFactor) {
        inactivityTimer.onActivity();
        beepManager.playBeepSoundAndVibrate();
        callBack.scanResult(rawResult, barcode);
    }

    @Override
    public double getScanWith() {
        if(cropWight==-1){
            getSurfaceWith();
        }
        return cropWight;
    }

    @Override
    public double getScanHeight() {
        if(cropHeight==-1){
            getSurfaceWith();
        }
        return cropHeight;
    }

    @Override
    public CameraManager getCameraManager() {
        return cameraManager;
    }

    @Override
    public Handler getHandler() {
        return handler;
    }


    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager.isOpen()) {
//            Log.w(TAG, "重复打开");
            return;
        }
        try {
            cameraManager.openDriver(surfaceHolder);
            // Creating the handler starts the preview, which can also throw a RuntimeException.
            if (handler == null) {
                handler = new CaptureImplHandler(resultCallBack,  cameraManager);
            }
        } catch (IOException ioe) {
            Log.e(TAG, "Do you apply for camera permission?");
        } catch (RuntimeException e) {
//            throw new RuntimeException("Do you apply for camera permission?");
            Log.e(TAG, "Do you apply for camera permission?");
        }
    }

    /**
     * 重新扫码
     */
    public void continuousScan() {
        if (handler != null) {
            handler.sendEmptyMessage(R.id.restart_preview);
        }
    }

    @Override
    public Rect getScanRect() {
        return scanRect;
    }

    @Override
    public void setScanRect(Rect scanRect) {
        this.scanRect = scanRect;
    }

    /**
     * 闪光灯开启/关闭
     */
    private void setLight(boolean isLight) {
        cameraManager.setTorch(isLight);
    }

    private void getSurfaceWith(){
        View cropImage = callBack.getScopImage();
        if (cropImage == null) {
            throw new IllegalStateException("ScopImage is null!");
        }
        DisplayMetrics outMetrics = new DisplayMetrics();
        callBack.getContext().getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        float density = outMetrics.density;
        int widthPixels = (int) (outMetrics.widthPixels / density);  // 屏幕宽度(dp)
        int heightPixels = (int) (outMetrics.heightPixels / density);// 屏幕高度(dp)

        cropWight = (double)widthPixels/(double)px2dip(cropImage.getWidth());
        cropHeight = (double)heightPixels/(double)px2dip(cropImage.getHeight());
    }

    LifeListener mLifeListener = new LifeListener() {

        @Override
        public void onResume() {
            resultCallBack.onResume();
        }

        @Override
        public void onPause() {
            resultCallBack.onPause();
        }

        @Override
        public void onDestroy() {
            resultCallBack.onDestroy();
        }
    };

    /**
     * 监听activity
     * @param activity
     */
    private void addLifeListener(Activity activity) {
        LifeMonitorActivity lifeMonitorActivity = getLifeListenerFragment(activity);
        lifeMonitorActivity.addLifeListener(mLifeListener);
    }

    private LifeMonitorActivity getLifeListenerFragment(Activity activity) {
        FragmentManager manager = activity.getFragmentManager();
        return getLifeListenerFragment(manager);
    }

    private LifeMonitorActivity getLifeListenerFragment(FragmentManager manager) {
        LifeMonitorActivity fragment = (LifeMonitorActivity) manager.findFragmentByTag(TAG);
        if (fragment == null) {
            fragment = new LifeMonitorActivity();
            manager.beginTransaction().add(fragment, TAG).commitAllowingStateLoss();
        }
        return fragment;
    }

    private int px2dip(float pxValue) {
        final float scale = callBack.getContext().getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public CaptureImpl setSurfaceView(SurfaceView preview_view) {
        return this;
    }
}
