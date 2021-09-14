package ml.bastiaan.component;

import android.content.Context;

public class ComponentContext {
    protected Context context;

    public ComponentContext(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }
}
