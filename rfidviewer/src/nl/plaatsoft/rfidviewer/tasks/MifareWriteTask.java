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
import java.util.ArrayList;
import java.util.List;

import nl.plaatsoft.rfidviewer.Consts;

public class MifareWriteTask {
    public static interface OnSuccessListener {
        void onSuccess();
    }
    public static interface OnErrorListener {
        void onError(Exception exception);
    }

    private static final Handler handler = new Handler(Looper.getMainLooper());
    private static final Executor executor = Executors.newFixedThreadPool(1);

    private static record PendingWrite(int blockIndex, byte[] data) {};

    public static class FailedWriteException extends Exception {
        private static final long serialVersionUID = 1;

        public FailedWriteException(String message) {
            super(message);
        }
    }

    private final Context context;
    private final MifareClassic mfc;
    private boolean isCanceled = false;
    private boolean isFinished = false;
    private OnSuccessListener onSuccessListener = null;
    private OnErrorListener onErrorListener = null;
    private List<PendingWrite> pendingWrites;

    private MifareWriteTask(Context context, MifareClassic mfc) {
        this.context = context;
        this.mfc = mfc;
        pendingWrites = new ArrayList<>();
    }

    public static MifareWriteTask with(Context context, MifareClassic mfc) {
        return new MifareWriteTask(context, mfc);
    }

    public boolean isCanceled() {
        return isCanceled;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public MifareWriteTask writeBlock(int blockIndex, byte[] data) {
        pendingWrites.add(new PendingWrite(blockIndex, data));
        return this;
    }

    public MifareWriteTask then(OnSuccessListener onSuccessListener) {
        this.onSuccessListener = onSuccessListener;
        return this;
    }

    public MifareWriteTask then(OnSuccessListener onSuccessListener, OnErrorListener onErrorListener) {
        this.onSuccessListener = onSuccessListener;
        this.onErrorListener = onErrorListener;
        return this;
    }

    public MifareWriteTask write() {
        // Create async task to write blocks to mifire classic tag
        executor.execute(() -> {
            try {
                writeBlocks();
                handler.post(() -> {
                    if (!isCanceled) {
                        finish();

                        if (onSuccessListener != null) {
                            onSuccessListener.onSuccess();
                        }
                    }
                });
            } catch (Exception exception) {
                handler.post(() -> {
                    if (!isCanceled) {
                        finish();

                        if (onErrorListener != null) {
                            onErrorListener.onError(exception);
                        } else {
                            Log.e(context.getPackageName(), "Can't write Mifare Classic tag", exception);
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

    private void writeBlocks() throws Exception {
        // Connect to the tag and write pending block writes
        mfc.connect();
        for (var pendingWrite : pendingWrites) {
            if (mfc.authenticateSectorWithKeyA(mfc.blockToSector(pendingWrite.blockIndex()), MifareClassic.KEY_DEFAULT)) {
                mfc.writeBlock(pendingWrite.blockIndex(), pendingWrite.data());

                var writtenBytes = mfc.readBlock(pendingWrite.blockIndex());
                for (var i = 0; i < 16; i++) {
                    if (pendingWrite.data()[i] != writtenBytes[i]) {
                        throw new FailedWriteException("Failed to write block " + pendingWrite.blockIndex() + ": read back data is not the same as written data!");
                    }
                }
            }
        }
        mfc.close();
    }
}
