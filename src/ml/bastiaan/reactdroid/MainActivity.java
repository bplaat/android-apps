package ml.bastiaan.reactdroid;

import android.app.Activity;
import android.os.Bundle;
import ml.bastiaan.component.ComponentContext;
import ml.bastiaan.component.Column;
import ml.bastiaan.component.Text;
import ml.bastiaan.component.VerticalScroll;

public class MainActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ComponentContext context = new ComponentContext(this);
        setContentView(
            VerticalScroll.create(context)
                .child(Column.create(context)
                    .child(
                        Text.create(context)
                            .text("ReactDroid")
                            .fontSizeSp(16)
                            .fontWeight(500)
                            .paddingDp(16)
                    )
                    .child(HelloComponent.create(context).name("Willem").years(52))
                    .child(HelloComponent.create(context).name("Wietske").years(47))
                    .child(HelloComponent.create(context).name("Bastiaan").years(19))
                    .child(HelloComponent.create(context).name("Sander").years(17))
                    .child(HelloComponent.create(context).name("Leonard").years(14))
                    .child(HelloComponent.create(context).name("Jiska").years(13))
                    .child(HelloComponent.create(context).name("Piepert").years(3))
                    .child(HelloComponent.create(context).name("Snoetje").years(2))
                    .child(HelloComponent.create(context).name("Brownie").years(3))
                    .child(
                        Text.create(context)
                            .text("Made by Bastiaan van der Plaat")
                            .textColorRes(R.color.secondary_text_color)
                            .paddingDp(16)
                    )
                )
                .build()
        );
    }
}
