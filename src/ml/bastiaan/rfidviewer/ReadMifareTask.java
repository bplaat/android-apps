package ml.bastiaan.rfidviewer;

import android.nfc.tech.MifareClassic;
import android.os.Looper;
import android.os.Handler;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ReadMifareTask {
    private static final Executor executor = Executors.newFixedThreadPool(4);
    private static final Handler handler = new Handler(Looper.getMainLooper());

    public static interface OnLoadListener {
        public abstract void onLoad(byte[] data);
    }

    public static interface OnErrorListener {
        public abstract void onError(Exception exception);
    }

    private MifareClassic mfc;
    private boolean isCanceled = false;
    private boolean isFinished = false;
    private OnLoadListener onLoadListener = null;
    private OnErrorListener onErrorListener = null;

    private ReadMifareTask(MifareClassic mfc) {
        this.mfc = mfc;
    }

    public static ReadMifareTask with(MifareClassic mfc) {
        return new ReadMifareTask(mfc);
    }

    public ReadMifareTask then(OnLoadListener onLoadListener) {
        this.onLoadListener = onLoadListener;
        return this;
    }

    public ReadMifareTask then(OnLoadListener onLoadListener, OnErrorListener onErrorListener) {
        this.onLoadListener = onLoadListener;
        this.onErrorListener = onErrorListener;
        return this;
    }

    public ReadMifareTask read() {
        executor.execute(() -> {
            try {
                byte[] data = readCard();
                handler.post(() -> {
                    isFinished = true;
                    if (!isCanceled && onLoadListener != null) {
                        onLoadListener.onLoad(data);
                    }
                });
            } catch (Exception exception) {
                handler.post(() -> {
                    isFinished = true;
                    if (!isCanceled) {
                        if (onErrorListener != null) {
                            onErrorListener.onError(exception);
                        } else {
                            exception.printStackTrace();
                        }
                    }
                });
            }
        });
        return this;
    }

    public boolean isCanceled() {
        return isCanceled;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void cancel() {
        isCanceled = true;
        isFinished = true;
    }

    private byte[] readCard() throws Exception {
        mfc.connect();
        byte[] data = new byte[mfc.getSize()];
        int pos = 0;
        for (int i = 0; i < mfc.getSectorCount(); i++) {
            boolean auth = mfc.authenticateSectorWithKeyA(i, MifareClassic.KEY_DEFAULT);
            if (auth) {
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
