/*
 * Copyright (c) 2021-2024 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.rfidviewer.tasks;

import android.content.Context;
import android.nfc.tech.MifareClassic;
import android.os.Looper;
import android.os.Handler;
import android.util.Log;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.annotation.Nullable;

public class MifareReadTask {
    public static interface OnLoadListener {
        void onLoad(byte[] data);
    }

    public static interface OnErrorListener {
        void onError(Exception exception);
    }

    private static final Handler handler = new Handler(Looper.getMainLooper());
    private static final Executor executor = Executors.newFixedThreadPool(1);

    private final Context context;
    private final MifareClassic mfc;
    private boolean isCanceled = false;
    private boolean isFinished = false;
    private @Nullable OnLoadListener onLoadListener;
    private @Nullable OnErrorListener onErrorListener;

    private MifareReadTask(Context context, MifareClassic mfc) {
        this.context = context;
        this.mfc = mfc;
    }

    public static MifareReadTask with(Context context, MifareClassic mfc) {
        return new MifareReadTask(context, mfc);
    }

    public boolean isCanceled() {
        return isCanceled;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public MifareReadTask then(OnLoadListener onLoadListener) {
        this.onLoadListener = onLoadListener;
        return this;
    }

    public MifareReadTask then(OnLoadListener onLoadListener, OnErrorListener onErrorListener) {
        this.onLoadListener = onLoadListener;
        this.onErrorListener = onErrorListener;
        return this;
    }

    public MifareReadTask read() {
        // Create async task to read Mifare Classic tag
        executor.execute(() -> {
            try {
                var data = readTag();
                handler.post(() -> {
                    if (!isCanceled) {
                        finish();
                        if (onLoadListener != null)
                            onLoadListener.onLoad(data);
                    }
                });
            } catch (Exception exception) {
                handler.post(() -> {
                    if (!isCanceled) {
                        finish();
                        if (onErrorListener != null) {
                            onErrorListener.onError(exception);
                        } else {
                            Log.e(context.getPackageName(), "Can't read Mifare Classic tag", exception);
                        }
                    }
                });
            }
        });
        return this;
    }

    public void cancel() {
        isCanceled = true;
        finish();
    }

    public void finish() {
        isFinished = true;
    }

    private byte[] readTag() throws Exception {
        // Connect to the tag and read block for block into byte buffer
        mfc.connect();
        var data = new byte[mfc.getSize()];
        var pos = 0;
        for (var i = 0; i < mfc.getSectorCount(); i++) {
            if (mfc.authenticateSectorWithKeyA(i, MifareClassic.KEY_DEFAULT)) {
                var blockIndex = mfc.sectorToBlock(i);
                for (var j = 0; j < mfc.getBlockCountInSector(i); j++) {
                    var bytes = mfc.readBlock(blockIndex++);
                    for (var k = 0; k < 16; k++)
                        data[pos++] = bytes[k];
                }
            }
        }
        mfc.close();
        return data;
    }
}
