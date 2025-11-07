/*
 * Copyright (c) 2021-2024 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package ml.coinlist.android.components;

import java.util.Objects;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import nl.plaatsoft.android.compat.ContextCompat;
import nl.plaatsoft.android.fetch.FetchImageTask;

import org.json.JSONException;
import org.jspecify.annotations.Nullable;

import ml.coinlist.android.Formatters;
import ml.coinlist.android.R;
import ml.coinlist.android.Settings;
import ml.coinlist.android.models.Coin;

public class CoinsAdapter extends ArrayAdapter<Coin> {
    private static class ViewHolder {
        public @SuppressWarnings("null") ImageView coinImage;
        public @SuppressWarnings("null") LinearLayout coinFirstLine;
        public @SuppressWarnings("null") TextView coinName;
        public @SuppressWarnings("null") TextView coinPrice;
        public @SuppressWarnings("null") LinearLayout coinSecondLine;
        public @SuppressWarnings("null") TextView coinRank;
        public @SuppressWarnings("null") TextView coinChange;
        public @SuppressWarnings("null") TextView coinExtra;
        public @SuppressWarnings("null") ImageButton coinStarButton;
    }

    private final Settings settings;

    public CoinsAdapter(Context context, Settings settings) {
        super(context, 0);
        this.settings = settings;
    }

    @Override
    public View getView(int position, @Nullable View convertView, @SuppressWarnings("null") ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_coin, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.coinImage = Objects.requireNonNull(convertView).findViewById(R.id.coin_image);
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
        if (coin.isPlaceholder()) {
            viewHolder.coinStarButton.setImageAlpha(0);
            return convertView;
        }

        FetchImageTask.with(getContext())
            .load(coin.imageUrl())
            .transparent()
            .fadeIn()
            .loadingColor(ContextCompat.getColor(getContext(), R.color.loading_background_color))
            .into(viewHolder.coinImage)
            .fetch();

        viewHolder.coinName.setText(coin.name());
        viewHolder.coinPrice.setText(Formatters.money(settings, coin.price()));
        if (((ColorDrawable)viewHolder.coinFirstLine.getBackground()).getColor() != Color.TRANSPARENT) {
            var set = (AnimatorSet)AnimatorInflater.loadAnimator(getContext(), R.animator.fade_in);
            set.setTarget(viewHolder.coinFirstLine);
            set.start();
        }

        viewHolder.coinRank.setText("#" + coin.rank());
        viewHolder.coinChange.setText(Formatters.changePercent(coin.change()));
        if (coin.change() > 0) {
            viewHolder.coinChange.setTextColor(ContextCompat.getColor(getContext(), R.color.positive_color));
        } else if (coin.change() < 0) {
            viewHolder.coinChange.setTextColor(ContextCompat.getColor(getContext(), R.color.negative_color));
        } else {
            viewHolder.coinChange.setTextColor(ContextCompat.getColor(getContext(), R.color.secondary_text_color));
        }

        if (coin.visibleStat() == Coin.VISIBLE_STAT_MARKET_CAP) {
            viewHolder.coinExtra.setText(getContext().getResources().getString(
                R.string.main_extra_market_cap, Formatters.money(settings, coin.marketCap())));
        }
        if (coin.visibleStat() == Coin.VISIBLE_STAT_VOLUME) {
            viewHolder.coinExtra.setText(getContext().getResources().getString(
                R.string.main_extra_volume, Formatters.money(settings, coin.volume())));
        }
        if (coin.visibleStat() == Coin.VISIBLE_STAT_SUPPLY) {
            viewHolder.coinExtra.setText(getContext().getResources().getString(
                R.string.main_extra_supply, Formatters.number(settings, coin.supply())));
        }

        if (((ColorDrawable)viewHolder.coinSecondLine.getBackground()).getColor() != Color.TRANSPARENT) {
            var set = (AnimatorSet)AnimatorInflater.loadAnimator(getContext(), R.animator.fade_in);
            set.setTarget(viewHolder.coinSecondLine);
            set.start();
        }

        viewHolder.coinStarButton.setImageResource(coin.starred() ? R.drawable.ic_star : R.drawable.ic_star_outline);
        viewHolder.coinStarButton.setOnClickListener(view -> {
            var updatedCoin = coin.toggleStarred();

            viewHolder.coinStarButton.setImageResource(
                updatedCoin.starred() ? R.drawable.ic_star : R.drawable.ic_star_outline);

            try {
                var jsonStarredCoins = settings.getStarredCoins();
                if (updatedCoin.starred()) {
                    jsonStarredCoins.put(updatedCoin.id());
                } else {
                    for (var i = 0; i < jsonStarredCoins.length(); i++) {
                        if (updatedCoin.id().equals(jsonStarredCoins.getString(i))) {
                            jsonStarredCoins.remove(i);
                        }
                    }
                }
                settings.setStarredCoins(jsonStarredCoins);
            } catch (JSONException exception) {
                Log.e(getContext().getPackageName(), "Can't update coin list item view", exception);
            }

            remove(coin);
            insert(updatedCoin, position);
        });

        var coinStarBackground = viewHolder.coinStarButton.getBackground();
        if (coinStarBackground instanceof ColorDrawable
            && ((ColorDrawable)coinStarBackground).getColor() != Color.TRANSPARENT) {
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
                    var outValue = new TypedValue();
                    getContext().getTheme().resolveAttribute(
                        android.R.attr.selectableItemBackgroundBorderless, outValue, true);
                    viewHolder.coinStarButton.setBackgroundResource(outValue.resourceId);
                }
            });
            alphaAnimation.start();
        }
        return convertView;
    }
}
