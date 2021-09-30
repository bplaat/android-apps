package ml.bastiaan.reactdroid;

import android.app.Activity;
import android.os.Bundle;
import android.widget.FrameLayout;
import ml.bastiaan.widgets.WidgetContext;

public class MainActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WidgetContext context = new WidgetContext(this);

        FrameLayout root = new FrameLayout(this);
        HomeScreen.create(context).render(root, null);
        setContentView(root);
    }
}
