/*
 * Copyright (C) 2010 ZXing authors
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

package com.zhouk.zxing.callback;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.zhouk.zxing.BinaryBitmap;
import com.zhouk.zxing.DecodeHintType;
import com.zhouk.zxing.MultiFormatReader;
import com.zhouk.zxing.PlanarYUVLuminanceSource;
import com.zhouk.zxing.R;
import com.zhouk.zxing.ReaderException;
import com.zhouk.zxing.Result;
import com.zhouk.zxing.common.GlobalHistogramBinarizer;

import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class DecodeImplHandler extends Handler {
    //用于并发解析frame
    private ExecutorService pool = Executors.newFixedThreadPool(10);
    private static final String TAG = DecodeImplHandler.class.getSimpleName();
    private final ResultCallBack callBack;
    private final MultiFormatReader multiFormatReader;
    private boolean running = true;

    DecodeImplHandler(ResultCallBack callBack, Map<DecodeHintType, Object> hints) {
        multiFormatReader = new MultiFormatReader();
        multiFormatReader.setHints(hints);
        this.callBack = callBack;
    }

    @Override
    public void handleMessage(final Message message) {
        if (message == null || !running) {
            return;
        }
        if (message.what == R.id.decode) {
            decode((byte[]) message.obj, message.arg1, message.arg2);
//            if(!pool.isShutdown()){
//                pool.execute(new MyRunning((byte[]) message.obj, message.arg1, message.arg2));
//                activity.getCameraManager().requestPreviewFrame(this, R.id.decode);
//            }
        } else if (message.what == R.id.quit) {
            running = false;
            Looper.myLooper().quit();

        }
    }

    /**
     * 解析本帧
     * @param data   The YUV preview frame.
     * @param width  The width of the preview frame.
     * @param height The height of the preview frame.
     */
    private void decode(byte[] data, int width, int height) {
        long start = System.currentTimeMillis();
        Result rawResult = null;
        PlanarYUVLuminanceSource source = callBack.getCameraManager().buildLuminanceSource(data,
                width, height, callBack.getScanWith(), callBack.getScanHeight());
        if (source != null) {
            BinaryBitmap bitmap = new BinaryBitmap(new GlobalHistogramBinarizer(source));
            try {
                rawResult = multiFormatReader.decodeWithState(bitmap);
            } catch (ReaderException re) {
                // continue
            } finally {
                multiFormatReader.reset();
            }
        }

        Handler handler = callBack.getHandler();
        if (rawResult != null) {
            if (handler != null) {
                Log.e(TAG, "decode cost time(ms):" + (System.currentTimeMillis() - start));
                Message message = Message.obtain(handler, R.id.decode_succeeded, rawResult);
                Bundle bundle = new Bundle();
                bundleThumbnail(source, bundle);
                message.setData(bundle);
                message.sendToTarget();
            }
        } else {
            if (handler != null) {
                Message message = Message.obtain(handler, R.id.decode_failed);
                Bundle bundle = new Bundle();
                bundleThumbnail(source, bundle);
                message.setData(bundle);
                message.sendToTarget();
            }
        }
    }

    private static void bundleThumbnail(PlanarYUVLuminanceSource source, Bundle bundle) {
        int[] pixels = source.renderThumbnail();
        int width = source.getThumbnailWidth();
        int height = source.getThumbnailHeight();
        Bitmap bitmap = Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.ARGB_8888);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
        bundle.putByteArray(DecodeImplThread.BARCODE_BITMAP, out.toByteArray());
        bundle.putFloat(DecodeImplThread.BARCODE_SCALED_FACTOR, (float) width / source.getWidth());
    }

    class MyRunning implements Runnable {
        private byte[] data;
        private int width,height;
        public MyRunning(byte[] data, int width, int height) {
            this.data = data;
            this.width = width;
            this.height = height;
        }

        @Override
        public void run() {
            Log.e(TAG, "执行running。。。。 ");
            long start = System.currentTimeMillis();
            Result rawResult = null;
            PlanarYUVLuminanceSource source = callBack.getCameraManager().buildLuminanceSource(data,
                    width, height, callBack.getScanWith(), callBack.getScanHeight());
            if (source != null) {
                BinaryBitmap bitmap = new BinaryBitmap(new GlobalHistogramBinarizer(source));
                try {
                    rawResult = multiFormatReader.decodeWithState(bitmap);
                } catch (ReaderException re) {
                    // continue
                } finally {
                    multiFormatReader.reset();
                }
            }

            Handler handler = callBack.getHandler();
            if (rawResult != null) {
                if (handler != null) {
                    pool.shutdown();
                    Log.e(TAG, " mulThread decode cost time(ms):  " + (System.currentTimeMillis() - start));
                    Message message = Message.obtain(handler, R.id.decode_succeeded, rawResult);
                    Bundle bundle = new Bundle();
                    bundleThumbnail(source, bundle);
                    message.setData(bundle);
                    message.sendToTarget();
                }
            } else {
                if (handler != null) {
                    Message message = Message.obtain(handler, R.id.decode_failed);
//                    Bundle bundle = new Bundle();
//                    bundleThumbnail(source, bundle);
//                    message.setData(bundle);
                    message.sendToTarget();
                }
            }
        }

    }

}
