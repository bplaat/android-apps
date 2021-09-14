package ml.bastiaan.reactdroid;

import android.app.Activity;
import android.os.Bundle;
import ml.bastiaan.component.ComponentContext;

public class MainActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ComponentContext context = new ComponentContext(this);
        setContentView(MainComponent.create(context).build());
    }
}
