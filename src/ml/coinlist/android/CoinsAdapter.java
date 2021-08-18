package ml.coinlist.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import org.json.JSONArray;

public class CoinsAdapter extends ArrayAdapter<Coin>{
    private static class ViewHolder {
        public ImageView coinImage;
        public TextView coinName;
        public TextView coinRank;
        public TextView coinChange;
        public TextView coinPrice;
        public TextView coinExtra;
        public ImageButton coinStarButton;
    }

    public CoinsAdapter(Context context) {
        super(context, 0);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_coin, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.coinImage = (ImageView)convertView.findViewById(R.id.coin_image);
            viewHolder.coinName = (TextView)convertView.findViewById(R.id.coin_name);
            viewHolder.coinRank = (TextView)convertView.findViewById(R.id.coin_rank);
            viewHolder.coinChange = (TextView)convertView.findViewById(R.id.coin_change);
            viewHolder.coinPrice = (TextView)convertView.findViewById(R.id.coin_price);
            viewHolder.coinExtra = (TextView)convertView.findViewById(R.id.coin_extra);
            viewHolder.coinStarButton = (ImageButton)convertView.findViewById(R.id.coin_star_button);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        Coin coin = getItem(position);

        FetchImageTask.with(getContext()).load(coin.getImageUrl()).transparent().fadeIn().into(viewHolder.coinImage).fetch();

        viewHolder.coinName.setText(coin.getName());
        viewHolder.coinRank.setText("#" + coin.getRank());
        if (coin.getChange() > 0) {
            viewHolder.coinChange.setTextColor(Utils.getColor(getContext(), R.color.positive_color));
        } else {
            if (coin.getChange() < 0) {
                viewHolder.coinChange.setTextColor(Utils.getColor(getContext(), R.color.negative_color));
            } else {
                viewHolder.coinChange.setTextColor(Utils.getColor(getContext(), R.color.secondary_text_color));
            }
        }
        viewHolder.coinChange.setText(Coin.formatPercent(coin.getChange()));

        viewHolder.coinPrice.setText(Coin.formatMoney(coin.getPrice()));
        if (coin.getExtraIndex() == 0) {
            viewHolder.coinExtra.setText("MCap " + Coin.formatMoney(coin.getMarketcap()));
        }
        if (coin.getExtraIndex() == 1) {
            viewHolder.coinExtra.setText("Volume " + Coin.formatMoney(coin.getVolume()));
        }
        if (coin.getExtraIndex() == 2) {
            viewHolder.coinExtra.setText("Supply " + Coin.formatNumber(coin.getSupply()));
        }

        if (coin.getStarred()) {
            viewHolder.coinStarButton.setImageResource(R.drawable.ic_star);
        } else {
            viewHolder.coinStarButton.setImageResource(R.drawable.ic_star_outline);
        }
        viewHolder.coinStarButton.setOnClickListener((View view) -> {
            coin.setStarred(!coin.getStarred());

            try {
                SharedPreferences settings = getContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
                JSONArray jsonStarredCoins = new JSONArray(settings.getString("starred_coins", "[]"));
                SharedPreferences.Editor settingsEditor = settings.edit();
                if (coin.getStarred()) {
                    viewHolder.coinStarButton.setImageResource(R.drawable.ic_star);

                    jsonStarredCoins.put(coin.getId());
                } else {
                    viewHolder.coinStarButton.setImageResource(R.drawable.ic_star_outline);

                    for (int i = 0; i  < jsonStarredCoins.length(); i++) {
                        if (coin.getId().equals(jsonStarredCoins.getString(i))) {
                            jsonStarredCoins.remove(i);
                            break;
                        }
                    }
                }
                settingsEditor.putString("starred_coins", jsonStarredCoins.toString());
                settingsEditor.apply();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });

        return convertView;
    }
}
