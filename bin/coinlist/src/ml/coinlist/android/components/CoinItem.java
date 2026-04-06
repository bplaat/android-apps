/*
 * Copyright (c) 2021-2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package ml.coinlist.android.components;

import static nl.plaatsoft.android.react.Unit.*;

import java.util.function.Consumer;

import android.util.Log;
import android.view.Gravity;
import android.widget.ImageView;

import nl.plaatsoft.android.compat.ContextCompat;
import nl.plaatsoft.android.react.*;

import org.json.JSONException;

import ml.coinlist.android.Formatters;
import ml.coinlist.android.R;
import ml.coinlist.android.Settings;
import ml.coinlist.android.models.Coin;

public class CoinItem {
    private static Modifier coinRow() {
        return Modifier.of().width(matchParent()).padding(dp(8), dp(16)).contentGravity(Gravity.CENTER_VERTICAL);
    }

    private static Modifier coinIcon() {
        return Modifier.of().size(dp(24)).margin(dp(0), dp(16), dp(0), dp(0));
    }

    private static Modifier coinFlex() {
        return Modifier.of().weight(1);
    }

    private static Modifier coinStarButton() {
        return Modifier.of().size(dp(24)).margin(dp(0), dp(0), dp(0), dp(16));
    }

    public CoinItem(Coin coin, Settings settings, Consumer<Coin> onToggleStar, Runnable onCycleStat) {
        var context = BuildContext.current().getContext();

        if (coin.isPlaceholder()) {
            new Row(() -> {
                new Spacer().modifier(coinIcon().background(R.color.loading_background_color));
                new Column(() -> {
                    new Spacer().modifier(Modifier.of()
                            .width(matchParent())
                            .height(dp(14))
                            .background(R.color.loading_background_color)
                            .margin(dp(0), dp(0), dp(4), dp(0)));
                    new Spacer().modifier(
                        Modifier.of().width(matchParent()).height(dp(12)).background(R.color.loading_background_color));
                }).modifier(coinFlex());
                new Spacer().modifier(coinStarButton().background(R.color.loading_background_color));
            }).modifier(coinRow());
            return;
        }

        new Row(() -> {
            new Image(coin.imageUrl())
                .scaleType(ImageView.ScaleType.CENTER_CROP)
                .transparent()
                .loadingColor(ContextCompat.getColor(context, R.color.loading_background_color))
                .modifier(coinIcon());

            new Column(() -> {
                new Row(() -> {
                    new Text(coin.name())
                        .modifier(Modifier.of().weight(1).fontSize(sp(14)).textSingleLine().margin(
                            dp(0), dp(8), dp(0), dp(0)));
                    new Text(Formatters.money(settings, coin.price())).modifier(Modifier.of().fontSize(sp(14)));
                }).modifier(Modifier.of().width(matchParent()).margin(dp(0), dp(0), dp(4), dp(0)));

                new Row(() -> {
                    new Text("#" + coin.rank())
                        .modifier(Modifier.of()
                                .fontSize(sp(12))
                                .textColor(R.color.secondary_text_color)
                                .margin(dp(0), dp(4), dp(0), dp(0)));
                    var changeColor = coin.priceChange() > 0
                        ? ContextCompat.getColor(context, R.color.positive_color)
                        : (coin.priceChange() < 0 ? ContextCompat.getColor(context, R.color.negative_color)
                                                  : ContextCompat.getColor(context, R.color.secondary_text_color));
                    new Text(Formatters.changePercent(coin.priceChange()))
                        .modifier(Modifier.of()
                                .fontSize(sp(12))
                                .textColorInt(changeColor)
                                .margin(dp(0), dp(8), dp(0), dp(0)));
                    var extraText = coin.visibleStat() == Coin.VISIBLE_STAT_VOLUME
                        ? context.getString(R.string.main_extra_volume, Formatters.money(settings, coin.volume()))
                        : (coin.visibleStat() == Coin.VISIBLE_STAT_SUPPLY
                                  ? context.getString(
                                        R.string.main_extra_supply, Formatters.number(settings, coin.supply()))
                                  : context.getString(
                                        R.string.main_extra_market_cap, Formatters.money(settings, coin.marketCap())));
                    new Text(extraText).modifier(Modifier.of()
                            .weight(1)
                            .fontSize(sp(12))
                            .textColor(R.color.secondary_text_color)
                            .textSingleLine()
                            .textGravity(Gravity.END));
                }).modifier(Modifier.of().width(matchParent()));
            }).modifier(coinFlex());

            new ImageButton(coin.starred() ? R.drawable.ic_star : R.drawable.ic_star_outline)
                .onClick(() -> {
                    var updated = coin.withToggledStarred();
                    try {
                        var jsonStarredCoins = settings.getStarredCoins();
                        if (updated.starred()) {
                            jsonStarredCoins.put(updated.id());
                        } else {
                            for (var i = 0; i < jsonStarredCoins.length(); i++) {
                                if (updated.id().equals(jsonStarredCoins.getString(i))) {
                                    jsonStarredCoins.remove(i);
                                    break;
                                }
                            }
                        }
                        settings.setStarredCoins(jsonStarredCoins);
                    } catch (JSONException exception) {
                        Log.e(context.getPackageName(), "Can't update starred coins", exception);
                    }
                    onToggleStar.accept(updated);
                })
                .modifier(coinStarButton().backgroundAttr(android.R.attr.selectableItemBackgroundBorderless));
        })
            .modifier(coinRow().backgroundAttr(android.R.attr.selectableItemBackground))
            .onClick(onCycleStat);
    }
}
