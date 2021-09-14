package ml.bastiaan.component;

import android.view.View;

public class Button extends Text {
    protected View.OnClickListener clickListener;

    protected Button(ComponentContext context) {
        super(context);
    }

    public static Button create(ComponentContext context) {
        return new Button(context);
    }

    public Button text(String text) {
        super.text(text);
        return this;
    }

    public Button onClick(View.OnClickListener clickListener) {
        this.clickListener = clickListener;
        return this;
    }

    public View build() {
        android.widget.Button button = new android.widget.Button(context.getContext());
        button.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        if (text != null) button.setText(text);
        if (clickListener != null) button.setOnClickListener(clickListener);
        view = (View)button;
        return view;
    }
}
