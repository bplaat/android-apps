/*
 * Copyright (c) 2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package ml.coinlist.android.components;

import static nl.plaatsoft.android.react.Unit.*;

import android.view.Gravity;

import nl.plaatsoft.android.compat.ContextCompat;
import nl.plaatsoft.android.react.*;

import org.jspecify.annotations.Nullable;

import ml.coinlist.android.Formatters;
import ml.coinlist.android.R;
import ml.coinlist.android.Settings;
import ml.coinlist.android.models.GlobalData;

public class GlobalInfoBar {
    private static Modifier bar() {
        return Modifier.of()
            .width(matchParent())
            .padding(dp(8), dp(16))
            .backgroundAttr(android.R.attr.selectableItemBackground);
    }

    private static Modifier loadingRow() {
        return Modifier.of()
            .width(matchParent())
            .height(dp(16))
            .marginBottom(dp(4))
            .background(R.color.loading_background_color);
    }

    private static Modifier textRow() {
        return Modifier.of()
            .width(matchParent())
            .backgroundColor(android.graphics.Color.TRANSPARENT)
            .marginBottom(dp(4));
    }

    public GlobalInfoBar(@Nullable GlobalData data, @Nullable Settings settings, Runnable onRefresh) {
        var context = BuildContext.current().getContext();
        new Column(() -> {
            if (data == null || settings == null) {
                new Text("").modifier(loadingRow());
                new Text("").modifier(loadingRow());
                new Text("").modifier(loadingRow().margin(dp(0)));
            } else {
                var changeColor = data.marketCapChange() > 0
                    ? ContextCompat.getColor(context, R.color.positive_color)
                    : (data.marketCapChange() < 0 ? ContextCompat.getColor(context, R.color.negative_color)
                                                  : ContextCompat.getColor(context, R.color.secondary_text_color));

                new Row(() -> {
                    new Text(context.getString(
                        R.string.main_global_market_cap, Formatters.money(settings, data.marketCap())));
                    new Text(" " + Formatters.changePercent(data.marketCapChange()))
                        .modifier(Modifier.of().textColorInt(changeColor));
                }).modifier(textRow());

                new Text(context.getString(R.string.main_global_volume, Formatters.money(settings, data.volume())))
                    .modifier(textRow());

                new Text(context.getString(R.string.main_global_dominance, Formatters.percent(data.bitcoinDominance()),
                             Formatters.percent(data.ethereumDominance())))
                    .modifier(textRow().margin(dp(0)));
            }
        })
            .modifier(bar())
            .onClick(onRefresh);
    }
}
