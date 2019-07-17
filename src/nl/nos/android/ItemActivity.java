package nl.nos.android;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.Jsoup;

public class ItemActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        ((ImageView)findViewById(R.id.item_back_button)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        Item item = (Item)getIntent().getSerializableExtra("item");
        new FetchImageTask((ImageView)findViewById(R.id.item_large_image), item.getImage()).execute();
        ((TextView)findViewById(R.id.item_title_label)).setText(item.getTitle());
        ((TextView)findViewById(R.id.item_date_label)).setText(item.getDate());

        LinearLayout itemContent = (LinearLayout)findViewById(R.id.item_content);
        Document doc = Jsoup.parse(item.getContent());
        for (Element child : doc.body().children()) {
            if (child.nodeName() == "h2" || child.nodeName() == "p") {
                TextView textView = new TextView(this);
                textView.setText(child.text());
                if (child.nodeName() == "h2") {
                    textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
                    textView.setTextSize(18);
                } else {
                    textView.setLineSpacing(0, 1.3f);
                }
                if (child.nextElementSibling() != null) {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0, 0, 0, (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics()));
                    textView.setLayoutParams(params);
                }
                itemContent.addView(textView);
            }
        }
    }
}
