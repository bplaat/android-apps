/*
 * Copyright (c) 2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.android.react;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import nl.plaatsoft.android.compat.WindowInsetsCompat;

import org.jspecify.annotations.Nullable;

public abstract class Component extends FrameLayout {
    private boolean initialized = false;
    private boolean mounted = false;
    private @Nullable View slottedView = null;

    public Component(Context context) {
        super(context);
    }

    // Nested-component constructor: self-slots so existing state is reused across rebuilds
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Component() {
        super(BuildContext.current().getContext());
        BuildContext c = BuildContext.current();
        slottedView = c.slot((Class)getClass(), () -> this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        // Default: decor handles all insets. Modifier.windowInsets() overrides this for edge-to-edge scroll views.
        if (getContext() instanceof Activity activity) {
            var decor = activity.getWindow().getDecorView();
            if (decor.getTag(Modifier.TAG_WINDOW_INSETS) == null) {
                decor.setOnApplyWindowInsetsListener((dv, insets) -> {
                    var i = WindowInsetsCompat.getInsets(insets);
                    dv.setPadding(i.left(), i.top(), i.right(), i.bottom());
                    return insets;
                });
            }
        }
        if (!initialized) {
            initialized = true;
            rebuild();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mounted) {
            mounted = false;
            initialized = false;
            onUnmount();
        }
    }

    protected void rebuild() {
        var bc = new BuildContext(getContext(), this);
        BuildContext.push(bc);
        try {
            render();
        } finally {
            bc.cleanup();
            BuildContext.pop();
        }
        if (!mounted) {
            mounted = true;
            onMount();
        } else {
            onUpdate();
        }
    }

    public Component modifier(Modifier modifier) {
        var target = slottedView != null ? slottedView : this;
        modifier.applyTo(target);
        modifier.applyLayoutTo(target);
        return this;
    }

    public abstract void render();

    protected void onMount() {}

    protected void onUpdate() {}

    protected void onUnmount() {}
}
