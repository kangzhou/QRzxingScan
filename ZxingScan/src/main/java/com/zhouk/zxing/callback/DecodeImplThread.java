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

package com.zhouk.zxing.callback;


import android.os.Handler;
import android.os.Looper;

import com.zhouk.zxing.BarcodeFormat;
import com.zhouk.zxing.DecodeFormatManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;


public final class DecodeImplThread extends Thread {
    public static final String BARCODE_BITMAP = "barcode_bitmap";
    public static final String BARCODE_SCALED_FACTOR = "barcode_scaled_factor";
    private final ResultCallBack callBack;
    private DecodeImplHandler handler;
    private final CountDownLatch handlerInitLatch;
    private Collection<BarcodeFormat> decodeFormats;

    public DecodeImplThread(ResultCallBack callBack) {
        this.callBack = callBack;
        handlerInitLatch = new CountDownLatch(1);
        decodeFormats = new ArrayList<>();
        decodeFormats.addAll(DecodeFormatManager.getQrCodeFormats());
    }

    public Handler getHandler() {
        try {
            handlerInitLatch.await();
        } catch (InterruptedException ie) {
            // continue
        }
        return handler;
    }

    @Override
    public void run() {
        Looper.prepare();
        handler = new DecodeImplHandler(callBack, null);
        handlerInitLatch.countDown();
        Looper.loop();
    }

}
