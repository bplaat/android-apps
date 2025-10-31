package nl.plaatsoft.nos.android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ArticlesAdapter extends ArrayAdapter<Article> {
    private static class ViewHolder {
        public ImageView articleItemImage;
        public TextView articleItemTitleLabel;
    }

    public ArticlesAdapter(Context context) {
       super(context, 0);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_article, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.articleItemImage = (ImageView)convertView.findViewById(R.id.article_item_image);
            viewHolder.articleItemTitleLabel = (TextView)convertView.findViewById(R.id.article_item_title_label);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        Article article = getItem(position);
        new FetchImageTask(getContext(), viewHolder.articleItemImage, article.getImageUrl(), true, true);
        viewHolder.articleItemTitleLabel.setText(article.getTitle());
        return convertView;
    }
}
