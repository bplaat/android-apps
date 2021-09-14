package ml.bastiaan.reactdroid;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import ml.bastiaan.component.ComponentContext;
import ml.bastiaan.component.Component;
import ml.bastiaan.component.Column;
import ml.bastiaan.component.VerticalScroll;

public class MainActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ComponentContext context = new ComponentContext(this);
        setContentView(
            VerticalScroll.create(context)
                .child(Column.create(context)
                    .child(HelloComponent.create(context).name("Willem"))
                    .child(HelloComponent.create(context).name("Wietske"))
                    .child(HelloComponent.create(context).name("Bastiaan"))
                    .child(HelloComponent.create(context).name("Sander"))
                    .child(HelloComponent.create(context).name("Leonard"))
                    .child(HelloComponent.create(context).name("Jiska"))
                    .child(HelloComponent.create(context).name("Piepert"))
                    .child(HelloComponent.create(context).name("Snoetje"))
                ).build()
        );
    }
}
