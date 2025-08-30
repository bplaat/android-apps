package ml.bastiaan.widgets;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

public class Button extends Text {
    protected View.OnClickListener clickListener;

    protected Button(WidgetContext context) {
        super(context);
    }

    public static Button create(WidgetContext context) {
        return new Button(context);
    }

    public Button text(String text) {
        this.text = text;
        return this;
    }

    public Button onClick(View.OnClickListener clickListener) {
        this.clickListener = clickListener;
        return this;
    }

    public View render(ViewGroup parent, View view) {
        android.widget.Button button;
        if (view != null && view.getClass().equals(android.widget.Button.class)) {
            button = (android.widget.Button)view;
        } else {
            if (view != null) {
                int index = parent.indexOfChild(view);
                parent.removeView(view);
                button = new android.widget.Button(context.getContext());
                parent.addView(button, index);
            } else {
                button = new android.widget.Button(context.getContext());
                parent.addView(button);
            }
            button.setTag(key);
        }

        if (!button.getText().equals(text)) {
            button.setText(text);
        }

        button.setOnClickListener(clickListener);

        button.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);

        return button;
    }
}
