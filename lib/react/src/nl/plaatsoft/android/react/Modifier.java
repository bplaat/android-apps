/*
 * Copyright (c) 2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.android.react;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import nl.plaatsoft.android.compat.ContextCompat;
import nl.plaatsoft.android.compat.WindowInsetsCompat;

public class Modifier {
    public static class FontWeight {
        public static final int NORMAL = 400;
        public static final int MEDIUM = 500;
        public static final int BOLD = 700;
    }

    private static final int TAG_BACKGROUND_RES = 0x62677265; // 'bgre'
    private static final int TAG_BACKGROUND_COLOR = 0x6267636c; // 'bgcl'
    private static final int TAG_BACKGROUND_ATTR = 0x62676174; // 'bgat'
    private static final int TAG_TEXT_COLOR_RES = 0x74787263; // 'txrc'
    private static final int TAG_TEXT_COLOR_VAL = 0x74787663; // 'txvc'
    static final int TAG_WINDOW_INSETS = 0x77696e73; // 'wins'

    private static final Typeface TYPEFACE_MEDIUM = Typeface.create("sans-serif-medium", Typeface.NORMAL);

    private Unit paddingTop = null;
    private Unit paddingRight = null;
    private Unit paddingBottom = null;
    private Unit paddingLeft = null;

    private Unit marginTop = null;
    private Unit marginRight = null;
    private Unit marginBottom = null;
    private Unit marginLeft = null;

    private float weight = -1;

    private Unit width = null;
    private Unit height = null;

    private int gravity = 0;

    private Unit positionX = null;
    private Unit positionY = null;

    private int backgroundResId = 0;
    private int backgroundColorValue = 0;
    private boolean hasBackgroundColor = false;

    private Unit elevation = null;

    private Unit fontSize = null;
    private int fontWeight = FontWeight.NORMAL;
    private int textColorResId = 0;
    private int textColorValue = 0;
    private boolean hasTextColorValue = false;

    private Unit minWidth = null;
    private Unit minHeight = null;

    private boolean singleLine = false;
    private int textGravityVal = 0;

    private int backgroundAttrId = 0;

    private boolean scrollVertical = false;
    private boolean scrollHorizontal = false;

    private int contentGravityVal = 0;
    private boolean useWindowInsets = false;

    private Modifier() {}

    public static Modifier of() {
        return new Modifier();
    }

    public Modifier padding(Unit all) {
        paddingTop = paddingRight = paddingBottom = paddingLeft = all;
        return this;
    }

    public Modifier padding(Unit vertical, Unit horizontal) {
        paddingTop = paddingBottom = vertical;
        paddingRight = paddingLeft = horizontal;
        return this;
    }

    public Modifier paddingX(Unit horizontal) {
        paddingRight = paddingLeft = horizontal;
        return this;
    }

    public Modifier paddingY(Unit vertical) {
        paddingTop = paddingBottom = vertical;
        return this;
    }

    public Modifier padding(Unit top, Unit right, Unit bottom, Unit left) {
        paddingTop = top;
        paddingRight = right;
        paddingBottom = bottom;
        paddingLeft = left;
        return this;
    }

    public Modifier margin(Unit all) {
        marginTop = marginRight = marginBottom = marginLeft = all;
        return this;
    }

    public Modifier margin(Unit vertical, Unit horizontal) {
        marginTop = marginBottom = vertical;
        marginRight = marginLeft = horizontal;
        return this;
    }

    public Modifier margin(Unit top, Unit right, Unit bottom, Unit left) {
        marginTop = top;
        marginRight = right;
        marginBottom = bottom;
        marginLeft = left;
        return this;
    }

    public Modifier marginX(Unit horizontal) {
        marginRight = marginLeft = horizontal;
        return this;
    }

    public Modifier marginY(Unit vertical) {
        marginTop = marginBottom = vertical;
        return this;
    }

    public Modifier weight(float w) {
        weight = w;
        return this;
    }

    public Modifier width(Unit u) {
        width = u;
        return this;
    }

    public Modifier height(Unit u) {
        height = u;
        return this;
    }

    public Modifier size(Unit u) {
        width = height = u;
        return this;
    }

    public Modifier size(Unit w, Unit h) {
        width = w;
        height = h;
        return this;
    }

    public Modifier align(int g) {
        gravity = g;
        return this;
    }

    public Modifier position(Unit x, Unit y) {
        positionX = x;
        positionY = y;
        return this;
    }

    public Modifier background(int resId) {
        backgroundResId = resId;
        return this;
    }

    public Modifier backgroundColor(int colorValue) {
        backgroundColorValue = colorValue;
        hasBackgroundColor = true;
        return this;
    }

    public Modifier elevation(Unit u) {
        elevation = u;
        return this;
    }

    public Modifier fontSize(Unit u) {
        fontSize = u;
        return this;
    }

    public Modifier fontWeight(int w) {
        fontWeight = w;
        return this;
    }

    public Modifier textColor(int colorRes) {
        textColorResId = colorRes;
        return this;
    }

    public Modifier textColorInt(int colorValue) {
        this.textColorValue = colorValue;
        hasTextColorValue = true;
        return this;
    }

    public Modifier scrollVertical() {
        scrollVertical = true;
        return this;
    }

    public Modifier scrollHorizontal() {
        scrollHorizontal = true;
        return this;
    }

    public Modifier contentGravity(int g) {
        contentGravityVal = g;
        return this;
    }

    public Modifier useWindowInsets() {
        useWindowInsets = true;
        return this;
    }

    public Modifier minWidth(Unit u) {
        minWidth = u;
        return this;
    }

    public Modifier minHeight(Unit u) {
        minHeight = u;
        return this;
    }

    public Modifier textSingleLine() {
        singleLine = true;
        return this;
    }

    public Modifier textGravity(int g) {
        textGravityVal = g;
        return this;
    }

    public Modifier backgroundAttr(int attrId) {
        backgroundAttrId = attrId;
        return this;
    }

    boolean isScrollVertical() {
        return scrollVertical;
    }

    boolean isScrollHorizontal() {
        return scrollHorizontal;
    }

    public void applyTo(View v) {
        if (paddingTop != null || paddingRight != null || paddingBottom != null || paddingLeft != null) {
            var ctx = v.getContext();
            int pl = (int)(paddingLeft != null ? paddingLeft : Unit.dp(0)).resolve(ctx);
            int pt = (int)(paddingTop != null ? paddingTop : Unit.dp(0)).resolve(ctx);
            int pr = (int)(paddingRight != null ? paddingRight : Unit.dp(0)).resolve(ctx);
            int pb = (int)(paddingBottom != null ? paddingBottom : Unit.dp(0)).resolve(ctx);
            if (v.getPaddingLeft() != pl || v.getPaddingTop() != pt || v.getPaddingRight() != pr
                || v.getPaddingBottom() != pb) {
                v.setPadding(pl, pt, pr, pb);
            }
        }
        if (backgroundResId != 0) {
            Integer last = (Integer)v.getTag(TAG_BACKGROUND_RES);
            if (last == null || last != backgroundResId) {
                v.setBackgroundResource(backgroundResId);
                v.setTag(TAG_BACKGROUND_RES, backgroundResId);
            }
        } else if (hasBackgroundColor) {
            Integer last = (Integer)v.getTag(TAG_BACKGROUND_COLOR);
            if (last == null || last != backgroundColorValue) {
                v.setBackgroundColor(backgroundColorValue);
                v.setTag(TAG_BACKGROUND_COLOR, backgroundColorValue);
            }
        } else if (backgroundAttrId != 0) {
            Integer last = (Integer)v.getTag(TAG_BACKGROUND_ATTR);
            if (last == null || last != backgroundAttrId) {
                var outValue = new TypedValue();
                v.getContext().getTheme().resolveAttribute(backgroundAttrId, outValue, true);
                v.setBackgroundResource(outValue.resourceId);
                v.setTag(TAG_BACKGROUND_ATTR, backgroundAttrId);
            }
        }
        if (elevation != null) {
            float elev = elevation.resolve(v.getContext());
            if (v.getElevation() != elev)
                v.setElevation(elev);
        }
        if (contentGravityVal != 0 && v instanceof LinearLayout ll) {
            if (ll.getGravity() != contentGravityVal)
                ll.setGravity(contentGravityVal);
        }
        if (useWindowInsets && v instanceof ViewGroup vg) {
            vg.setClipToPadding(false);
            // Always override the decor listener so edge-to-edge scroll views get top on decor,
            // bottom on this view (overrides the default full-insets listener from Component)
            if (v.getContext() instanceof Activity activity) {
                var decor = activity.getWindow().getDecorView();
                decor.setTag(TAG_WINDOW_INSETS, true);
                decor.setOnApplyWindowInsetsListener((dv, insets) -> {
                    var i = WindowInsetsCompat.getInsets(insets);
                    dv.setPadding(i.left(), i.top(), i.right(), 0);
                    return insets;
                });
            }
            // One-time per view: apply bottom padding so items at rest are above the nav bar
            if (v.getTag(TAG_WINDOW_INSETS) == null) {
                v.setTag(TAG_WINDOW_INSETS, true);
                v.setOnApplyWindowInsetsListener((view, insets) -> {
                    var i = WindowInsetsCompat.getInsets(insets);
                    view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), i.bottom());
                    return insets;
                });
                if (v.isAttachedToWindow())
                    v.requestApplyInsets();
            }
        }
        if (minWidth != null) {
            int mw = (int)minWidth.resolve(v.getContext());
            if (v.getMinimumWidth() != mw)
                v.setMinimumWidth(mw);
        }
        if (minHeight != null) {
            int mh = (int)minHeight.resolve(v.getContext());
            if (v.getMinimumHeight() != mh)
                v.setMinimumHeight(mh);
        }
    }

    public void applyToTextView(TextView tv) {
        applyTo(tv);
        if (fontSize != null) {
            float px = fontSize.resolve(tv.getContext());
            if (tv.getTextSize() != px)
                tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, px);
        }
        if (fontWeight != FontWeight.NORMAL) {
            Typeface target = fontWeight == FontWeight.MEDIUM ? TYPEFACE_MEDIUM : Typeface.DEFAULT_BOLD;
            if (tv.getTypeface() != target)
                tv.setTypeface(target);
        }
        if (textColorResId != 0) {
            Integer last = (Integer)tv.getTag(TAG_TEXT_COLOR_RES);
            if (last == null || last != textColorResId) {
                tv.setTextColor(ContextCompat.getColor(tv.getContext(), textColorResId));
                tv.setTag(TAG_TEXT_COLOR_RES, textColorResId);
            }
        }
        if (hasTextColorValue) {
            Integer last = (Integer)tv.getTag(TAG_TEXT_COLOR_VAL);
            if (last == null || last != textColorValue) {
                tv.setTextColor(textColorValue);
                tv.setTag(TAG_TEXT_COLOR_VAL, textColorValue);
            }
        }
        if (singleLine) {
            tv.setSingleLine(true);
            tv.setEllipsize(TextUtils.TruncateAt.END);
        }
        if (textGravityVal != 0 && tv.getGravity() != textGravityVal)
            tv.setGravity(textGravityVal);
        applyLayoutTo(tv);
    }

    public void applyLayoutTo(View v) {
        var hasWidth = width != null;
        var hasHeight = height != null;
        var hasWeight = weight >= 0;
        var hasGravity = gravity != Gravity.NO_GRAVITY;
        var hasPosX = positionX != null;
        var hasPosY = positionY != null;
        var hasMargin = marginTop != null || marginRight != null || marginBottom != null || marginLeft != null;
        if (!hasWidth && !hasHeight && !hasWeight && !hasGravity && !hasPosX && !hasPosY && !hasMargin)
            return;
        var ctx = v.getContext();
        var existing = v.getLayoutParams();
        if (hasWeight) {
            var parent = v.getParent();
            boolean horizontal =
                parent instanceof LinearLayout && ((LinearLayout)parent).getOrientation() == LinearLayout.HORIZONTAL;
            int w = hasWidth ? (int)width.resolve(ctx) : (horizontal ? 0 : LinearLayout.LayoutParams.MATCH_PARENT);
            int h = hasHeight ? (int)height.resolve(ctx) : (horizontal ? LinearLayout.LayoutParams.WRAP_CONTENT : 0);
            if (existing instanceof LinearLayout.LayoutParams lp) {
                if (updateLinearParams(lp, w, h, weight, hasGravity ? gravity : lp.gravity, hasMargin, ctx))
                    v.requestLayout();
            } else {
                var lp = new LinearLayout.LayoutParams(w, h, weight);
                if (hasGravity)
                    lp.gravity = gravity;
                if (hasMargin)
                    applyMargins(lp, v);
                v.setLayoutParams(lp);
            }
        } else if (v.getParent() instanceof LinearLayout) {
            int w = hasWidth ? (int)width.resolve(ctx) : LinearLayout.LayoutParams.WRAP_CONTENT;
            int h = hasHeight ? (int)height.resolve(ctx) : LinearLayout.LayoutParams.WRAP_CONTENT;
            if (existing instanceof LinearLayout.LayoutParams lp) {
                if (updateLinearParams(lp, w, h, 0, hasGravity ? gravity : lp.gravity, hasMargin, ctx))
                    v.requestLayout();
            } else {
                var lp = new LinearLayout.LayoutParams(w, h);
                if (hasGravity)
                    lp.gravity = gravity;
                if (hasMargin)
                    applyMargins(lp, v);
                v.setLayoutParams(lp);
            }
        } else {
            int w = hasWidth ? (int)width.resolve(ctx) : FrameLayout.LayoutParams.WRAP_CONTENT;
            int h = hasHeight ? (int)height.resolve(ctx) : FrameLayout.LayoutParams.WRAP_CONTENT;
            if (existing instanceof FrameLayout.LayoutParams lp) {
                if (updateFrameParams(lp, w, h, hasGravity ? gravity : lp.gravity, hasPosX, hasPosY, hasMargin, ctx))
                    v.requestLayout();
            } else {
                var lp = new FrameLayout.LayoutParams(w, h);
                if (hasGravity)
                    lp.gravity = gravity;
                if (hasPosX)
                    lp.leftMargin = (int)positionX.resolve(ctx);
                if (hasPosY)
                    lp.topMargin = (int)positionY.resolve(ctx);
                if (hasMargin)
                    applyMargins(lp, v);
                v.setLayoutParams(lp);
            }
        }
    }

    private boolean updateLinearParams(
        LinearLayout.LayoutParams lp, int w, int h, float wt, int grav, boolean hasMargin, Context ctx) {
        boolean changed = false;
        if (lp.width != w) {
            lp.width = w;
            changed = true;
        }
        if (lp.height != h) {
            lp.height = h;
            changed = true;
        }
        if (lp.weight != wt) {
            lp.weight = wt;
            changed = true;
        }
        if (lp.gravity != grav) {
            lp.gravity = grav;
            changed = true;
        }
        if (hasMargin) {
            if (marginTop != null) {
                int m = (int)marginTop.resolve(ctx);
                if (lp.topMargin != m) {
                    lp.topMargin = m;
                    changed = true;
                }
            }
            if (marginRight != null) {
                int m = (int)marginRight.resolve(ctx);
                if (lp.rightMargin != m) {
                    lp.rightMargin = m;
                    changed = true;
                }
            }
            if (marginBottom != null) {
                int m = (int)marginBottom.resolve(ctx);
                if (lp.bottomMargin != m) {
                    lp.bottomMargin = m;
                    changed = true;
                }
            }
            if (marginLeft != null) {
                int m = (int)marginLeft.resolve(ctx);
                if (lp.leftMargin != m) {
                    lp.leftMargin = m;
                    changed = true;
                }
            }
        }
        return changed;
    }

    private boolean updateFrameParams(FrameLayout.LayoutParams lp, int w, int h, int grav, boolean hasPosX,
        boolean hasPosY, boolean hasMargin, Context ctx) {
        boolean changed = false;
        if (lp.width != w) {
            lp.width = w;
            changed = true;
        }
        if (lp.height != h) {
            lp.height = h;
            changed = true;
        }
        if (lp.gravity != grav) {
            lp.gravity = grav;
            changed = true;
        }
        if (hasPosX) {
            int px = (int)positionX.resolve(ctx);
            if (lp.leftMargin != px) {
                lp.leftMargin = px;
                changed = true;
            }
        }
        if (hasPosY) {
            int py = (int)positionY.resolve(ctx);
            if (lp.topMargin != py) {
                lp.topMargin = py;
                changed = true;
            }
        }
        if (hasMargin) {
            if (marginTop != null) {
                int m = (int)marginTop.resolve(ctx);
                if (lp.topMargin != m) {
                    lp.topMargin = m;
                    changed = true;
                }
            }
            if (marginRight != null) {
                int m = (int)marginRight.resolve(ctx);
                if (lp.rightMargin != m) {
                    lp.rightMargin = m;
                    changed = true;
                }
            }
            if (marginBottom != null) {
                int m = (int)marginBottom.resolve(ctx);
                if (lp.bottomMargin != m) {
                    lp.bottomMargin = m;
                    changed = true;
                }
            }
            if (marginLeft != null) {
                int m = (int)marginLeft.resolve(ctx);
                if (lp.leftMargin != m) {
                    lp.leftMargin = m;
                    changed = true;
                }
            }
        }
        return changed;
    }

    private void applyMargins(android.view.ViewGroup.MarginLayoutParams lp, android.view.View v) {
        if (marginTop != null)
            lp.topMargin = (int)marginTop.resolve(v.getContext());
        if (marginRight != null)
            lp.rightMargin = (int)marginRight.resolve(v.getContext());
        if (marginBottom != null)
            lp.bottomMargin = (int)marginBottom.resolve(v.getContext());
        if (marginLeft != null)
            lp.leftMargin = (int)marginLeft.resolve(v.getContext());
    }
}
