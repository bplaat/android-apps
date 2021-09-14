package ml.bastiaan.component;

import android.view.View;
import android.widget.Button;

public class ButtonComponent extends Text {
    protected View.OnClickListener clickListener;

    protected ButtonComponent(ComponentContext context) {
        super(context);
    }

    public static ButtonComponent create(ComponentContext context) {
        return new ButtonComponent(context);
    }

    public ButtonComponent text(String text) {
        super.text(text);
        return this;
    }

    public ButtonComponent onClick(View.OnClickListener clickListener) {
        this.clickListener = clickListener;
        return this;
    }

    public View build() {
        Button button = new Button(context.getContext());
        button.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        if (text != null) button.setText(text);
        if (clickListener != null) button.setOnClickListener(clickListener);
        view = (View)button;
        return view;
    }
}
