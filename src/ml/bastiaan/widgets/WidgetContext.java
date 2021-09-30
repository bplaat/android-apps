package ml.bastiaan.widgets;

import android.content.Context;

public class WidgetContext {
    protected Context context;

    public WidgetContext(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }
}
