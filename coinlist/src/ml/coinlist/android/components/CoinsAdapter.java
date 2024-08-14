/*
 * Copyright (c) 2021-2024 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package ml.coinlist.android.components;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.graphics.Color;
import android.util.Log;
import android.util.TypedValue;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONArray;

import ml.coinlist.android.tasks.FetchImageTask;
import ml.coinlist.android.models.Coin;
import ml.coinlist.android.Consts;
import ml.coinlist.android.Formatters;
import ml.coinlist.android.Utils;
import ml.coinlist.android.R;

public class CoinsAdapter extends ArrayAdapter<Coin> {
    private static class ViewHolder {
        public ImageView coinImage;
        public LinearLayout coinFirstLine;
        public TextView coinName;
        public TextView coinPrice;
        public LinearLayout coinSecondLine;
        public TextView coinRank;
        public TextView coinChange;
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
            viewHolder.coinFirstLine = convertView.findViewById(R.id.coin_first_line);
            viewHolder.coinName = convertView.findViewById(R.id.coin_name);
            viewHolder.coinPrice = convertView.findViewById(R.id.coin_price);
            viewHolder.coinSecondLine = convertView.findViewById(R.id.coin_second_line);
            viewHolder.coinRank = convertView.findViewById(R.id.coin_rank);
            viewHolder.coinChange = convertView.findViewById(R.id.coin_change);
            viewHolder.coinExtra = convertView.findViewById(R.id.coin_extra);
            viewHolder.coinStarButton = convertView.findViewById(R.id.coin_star_button);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        var coin = getItem(position);
        if (coin.isEmpty()) {
            viewHolder.coinStarButton.setImageAlpha(0);
            return convertView;
        }

        FetchImageTask.with(getContext()).load(coin.getImageUrl()).transparent().fadeIn().into(viewHolder.coinImage).fetch();

        viewHolder.coinName.setText(coin.getName());
        viewHolder.coinPrice.setText(Formatters.money(getContext(), coin.getPrice()));
        if (((ColorDrawable)viewHolder.coinFirstLine.getBackground()).getColor() != Color.TRANSPARENT) {
            var set = (AnimatorSet)AnimatorInflater.loadAnimator(getContext(), R.animator.fade_in);
            set.setTarget(viewHolder.coinFirstLine);
            set.start();
        }

        viewHolder.coinRank.setText("#" + coin.getRank());
        viewHolder.coinChange.setText(Formatters.changePercent(coin.getChange()));
        if (coin.getChange() > 0) {
            viewHolder.coinChange.setTextColor(Utils.contextGetColor(getContext(), R.color.positive_color));
        } else if (coin.getChange() < 0) {
            viewHolder.coinChange.setTextColor(Utils.contextGetColor(getContext(), R.color.negative_color));
        } else {
            viewHolder.coinChange.setTextColor(Utils.contextGetColor(getContext(), R.color.secondary_text_color));
        }

        if (coin.getExtraIndex() == 0) {
            viewHolder.coinExtra.setText(getContext().getResources().getString(R.string.main_extra_market_cap) + " " +
                Formatters.money(getContext(), coin.getMarketCap()));
        }
        if (coin.getExtraIndex() == 1) {
            viewHolder.coinExtra.setText(getContext().getResources().getString(R.string.main_extra_volume) + " " +
                Formatters.money(getContext(), coin.getVolume()));
        }
        if (coin.getExtraIndex() == 2) {
            viewHolder.coinExtra.setText(getContext().getResources().getString(R.string.main_extra_supply) + " " +
                Formatters.number(getContext(), coin.getSupply()));
        }

        if (((ColorDrawable)viewHolder.coinSecondLine.getBackground()).getColor() != Color.TRANSPARENT) {
            var set = (AnimatorSet)AnimatorInflater.loadAnimator(getContext(), R.animator.fade_in);
            set.setTarget(viewHolder.coinSecondLine);
            set.start();
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
                    for (var i = 0; i < jsonStarredCoins.length(); i++) {
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

        var coinStarBackground = viewHolder.coinStarButton.getBackground();
        if (coinStarBackground instanceof ColorDrawable && ((ColorDrawable)coinStarBackground).getColor() != Color.TRANSPARENT) {
            var set = (AnimatorSet)AnimatorInflater.loadAnimator(getContext(), R.animator.fade_in);
            set.setTarget(viewHolder.coinStarButton);
            set.start();

            var alphaAnimation = ValueAnimator.ofInt(0, 255);
            alphaAnimation.setDuration(getContext().getResources().getInteger(R.integer.animation_duration));
            alphaAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
            alphaAnimation.addUpdateListener(animator -> {
                var value = (int)alphaAnimation.getAnimatedValue();
                viewHolder.coinStarButton.setImageAlpha(value);
                if (value == 255) {
                    TypedValue outValue = new TypedValue();
                    getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true);
                    viewHolder.coinStarButton.setBackgroundResource(outValue.resourceId);
                }
            });
            alphaAnimation.start();
        }
        return convertView;
    }
}
