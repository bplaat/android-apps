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
    private static final Executor executor = Executors.newFixedThreadPool(1);
    private static final Handler handler = new Handler(Looper.getMainLooper());

    public static interface OnSuccessListener {
        public abstract void onSuccess();
    }

    public static interface OnErrorListener {
        public abstract void onError(Exception exception);
    }

    private static class PendingWrite {
        public int blockIndex;
        public byte[] data;
    }

    public static class FailedWriteException extends Exception {
        private static final long serialVersionUID = 1;

        public FailedWriteException(String message) {
            super(message);
        }
    }

    private Context context;
    private MifareClassic mfc;
    private boolean isCanceled = false;
    private boolean isFinished = false;
    private OnSuccessListener onSuccessListener = null;
    private OnErrorListener onErrorListener = null;
    private List<PendingWrite> pendingWrites;

    private MifareWriteTask(Context context, MifareClassic mfc) {
        this.context = context;
        this.mfc = mfc;
        pendingWrites = new ArrayList<PendingWrite>();
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
        PendingWrite pendingWrite = new PendingWrite();
        pendingWrite.blockIndex = blockIndex;
        pendingWrite.data = data;
        pendingWrites.add(pendingWrite);
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
        for (PendingWrite pendingWrite : pendingWrites) {
            if (mfc.authenticateSectorWithKeyA(mfc.blockToSector(pendingWrite.blockIndex), MifareClassic.KEY_DEFAULT)) {
                mfc.writeBlock(pendingWrite.blockIndex, pendingWrite.data);

                byte[] writtenBytes = mfc.readBlock(pendingWrite.blockIndex);
                for (int i = 0; i < 16; i++) {
                    if (pendingWrite.data[i] != writtenBytes[i]) {
                        throw new FailedWriteException("Failed to write block " + pendingWrite.blockIndex + ": read back data is not the same as written data!");
                    }
                }
            }
        }
        mfc.close();
    }
}
