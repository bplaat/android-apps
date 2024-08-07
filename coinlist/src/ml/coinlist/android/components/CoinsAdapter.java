package ml.coinlist.android.components;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import org.json.JSONArray;

import ml.coinlist.android.tasks.FetchImageTask;
import ml.coinlist.android.models.Coin;
import ml.coinlist.android.Consts;
import ml.coinlist.android.Utils;
import ml.coinlist.android.R;

public class CoinsAdapter extends ArrayAdapter<Coin> {
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_coin, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.coinImage = convertView.findViewById(R.id.coin_image);
            viewHolder.coinName = convertView.findViewById(R.id.coin_name);
            viewHolder.coinRank = convertView.findViewById(R.id.coin_rank);
            viewHolder.coinChange = convertView.findViewById(R.id.coin_change);
            viewHolder.coinPrice = convertView.findViewById(R.id.coin_price);
            viewHolder.coinExtra = convertView.findViewById(R.id.coin_extra);
            viewHolder.coinStarButton = convertView.findViewById(R.id.coin_star_button);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        var coin = getItem(position);

        FetchImageTask.with(getContext()).load(coin.getImageUrl()).transparent().fadeIn().into(viewHolder.coinImage).fetch();

        viewHolder.coinName.setText(coin.getName());
        viewHolder.coinRank.setText("#" + coin.getRank());
        if (coin.getChange() > 0) {
            viewHolder.coinChange.setTextColor(Utils.contextGetColor(getContext(), R.color.positive_color));
        } else {
            if (coin.getChange() < 0) {
                viewHolder.coinChange.setTextColor(Utils.contextGetColor(getContext(), R.color.negative_color));
            } else {
                viewHolder.coinChange.setTextColor(Utils.contextGetColor(getContext(), R.color.secondary_text_color));
            }
        }
        viewHolder.coinChange.setText(Coin.formatChangePercent(coin.getChange()));

        viewHolder.coinPrice.setText(Coin.formatMoney(getContext(), coin.getPrice()));
        if (coin.getExtraIndex() == 0) {
            viewHolder.coinExtra.setText(getContext().getResources().getString(R.string.main_extra_marketcap) + " " +
                Coin.formatMoney(getContext(), coin.getMarketcap()));
        }
        if (coin.getExtraIndex() == 1) {
            viewHolder.coinExtra.setText(getContext().getResources().getString(R.string.main_extra_volume) + " " +
                Coin.formatMoney(getContext(), coin.getVolume()));
        }
        if (coin.getExtraIndex() == 2) {
            viewHolder.coinExtra.setText(getContext().getResources().getString(R.string.main_extra_supply) + " " +
                Coin.formatNumber(getContext(), coin.getSupply()));
        }

        viewHolder.coinStarButton.setImageResource(coin.getStarred() ? R.drawable.ic_star : R.drawable.ic_star_outline);
        viewHolder.coinStarButton.setOnClickListener(view -> {
            coin.setStarred(!coin.getStarred());
            viewHolder.coinStarButton.setImageResource(coin.getStarred() ? R.drawable.ic_star : R.drawable.ic_star_outline);

            try {
                var settings = getContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
                var jsonStarredCoins = new JSONArray(settings.getString("starred_coins", "[]"));
                var settingsEditor = settings.edit();
                if (coin.getStarred()) {
                    jsonStarredCoins.put(coin.getId());
                } else {
                    for (int i = 0; i < jsonStarredCoins.length(); i++) {
                        if (coin.getId().equals(jsonStarredCoins.getString(i))) {
                            jsonStarredCoins.remove(i);
                            break;
                        }
                    }
                }
                settingsEditor.putString("starred_coins", jsonStarredCoins.toString());
                settingsEditor.apply();
            } catch (Exception exception) {
                Log.e(getContext().getPackageName(), "Can't update coin list item view", exception);
            }
        });
        return convertView;
    }
}
