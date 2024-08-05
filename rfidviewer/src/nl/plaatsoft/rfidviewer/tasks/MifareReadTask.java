package nl.plaatsoft.rfidviewer.tasks;

import android.nfc.tech.MifareClassic;
import android.os.Looper;
import android.os.Handler;
import android.util.Log;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import nl.plaatsoft.rfidviewer.Consts;

public class MifareReadTask {
    private static final Executor executor = Executors.newFixedThreadPool(1);
    private static final Handler handler = new Handler(Looper.getMainLooper());

    public static interface OnLoadListener {
        public abstract void onLoad(byte[] data);
    }

    public static interface OnErrorListener {
        public abstract void onError(Exception exception);
    }

    private MifareClassic mfc;
    private boolean isCanceled;
    private boolean isFinished;
    private OnLoadListener onLoadListener;
    private OnErrorListener onErrorListener;

    private MifareReadTask(MifareClassic mfc) {
        this.mfc = mfc;
    }

    public boolean isCanceled() {
        return isCanceled;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public static MifareReadTask with(MifareClassic mfc) {
        return new MifareReadTask(mfc);
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
        // Create async task to read mifire classic tag
        executor.execute(() -> {
            try {
                byte[] data = readTag();
                handler.post(() -> {
                    if (!isCanceled) {
                        finish();

                        if (onLoadListener != null) {
                            onLoadListener.onLoad(data);
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
                            Log.e(Consts.LOG_TAG, "Can't read Mifare Classic tag", exception);
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
        byte[] data = new byte[mfc.getSize()];
        int pos = 0;
        for (int i = 0; i < mfc.getSectorCount(); i++) {
            if (mfc.authenticateSectorWithKeyA(i, MifareClassic.KEY_DEFAULT)) {
                int blockIndex = mfc.sectorToBlock(i);
                for (int j = 0; j < mfc.getBlockCountInSector(i); j++) {
                    byte[] bytes = mfc.readBlock(blockIndex++);
                    for (int k = 0; k < 16; k++) {
                        data[pos++] = bytes[k];
                    }
                }
            }
        }
        mfc.close();
        return data;
    }
}
