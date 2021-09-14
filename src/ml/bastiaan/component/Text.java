package ml.bastiaan.component;

import android.content.ContextWrapper;
import android.graphics.Typeface;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

public class Text extends Component {
    protected String text;
    protected int textColor = -1;
    protected int fontSize = -1;
    protected int fontWeight = 400;

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

    public Text fontSizeSp(int fontSize) {
        this.fontSize = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, fontSize, context.getContext().getResources().getDisplayMetrics());
        return this;
    }

    public Text fontWeight(int fontWeight) {
        this.fontWeight = fontWeight;
        return this;
    }

    @SuppressWarnings("deprecation")
    public Text textColorRes(int colorId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            textColor =  new ContextWrapper(context.getContext()).getColor(colorId);
        } else {
            textColor = context.getContext().getResources().getColor(colorId);
        }
        return this;
    }

    public TextView view() {
        TextView textView = new TextView(context.getContext());
        textView.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        if (text != null) textView.setText(text);
        if (textColor != -1) textView.setTextColor(textColor);
        if (fontSize != -1) textView.setTextSize(fontSize);
        if (fontWeight == 500) textView.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        if (fontWeight == 700) textView.setTypeface(null, Typeface.BOLD);
        return textView;
    }
}
