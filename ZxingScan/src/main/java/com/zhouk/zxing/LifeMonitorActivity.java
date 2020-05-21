package com.zhouk.zxing;

import android.app.Fragment;

import com.zhouk.zxing.callback.lifecallback.LifeListener;

/**
 * 监听activity的生命周期
 */
public class LifeMonitorActivity extends Fragment {
    private LifeListener mLifeListener;

    public void addLifeListener(LifeListener listener) {
        mLifeListener = listener;
    }

    public void removeLifeListener() {
        mLifeListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mLifeListener != null) {
            mLifeListener.onResume();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mLifeListener != null) {
            mLifeListener.onDestroy();
        }
    }
}
