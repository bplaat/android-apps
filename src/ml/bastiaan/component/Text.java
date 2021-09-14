package ml.bastiaan.component;

import android.view.View;
import android.widget.TextView;

public class Text extends ViewComponent {
    protected String text;

    protected Text(ComponentContext context) {
        super(context);
    }

    public static Text create(ComponentContext context) {
        return new Text(context);
    }

    public Text text(String text) {
        this.text = text;
        return this;
    }

    public View build() {
        TextView textView = new TextView(context.getContext());
        textView.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        if (text != null) textView.setText(text);
        view = (View)textView;
        return view;
    }
}
