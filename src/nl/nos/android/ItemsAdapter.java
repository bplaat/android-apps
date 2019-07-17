package nl.nos.android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ItemsAdapter extends ArrayAdapter<Item> {
    private static class ViewHolder {
        public ImageView itemImage;
        public TextView itemTitleLabel;
    }

    public ItemsAdapter(Context context) {
       super(context, 0);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.itemImage = (ImageView)convertView.findViewById(R.id.item_image);
            viewHolder.itemTitleLabel = (TextView)convertView.findViewById(R.id.item_title_label);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }
        Item item = getItem(position);
        new FetchImageTask(viewHolder.itemImage, item.getImage()).execute();
        viewHolder.itemTitleLabel.setText(item.getTitle());
        return convertView;
    }
}
