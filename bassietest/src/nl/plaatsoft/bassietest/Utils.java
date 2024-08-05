package nl.plaatsoft.bassietest;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.View;
import android.widget.TextView;
import java.security.MessageDigest;

public class Utils {
    private Utils() {}

    public static String md5(String data) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(data.getBytes());
            byte[] bytes = messageDigest.digest();
            StringBuilder hashBuilder = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                hashBuilder.append(String.format("%02x", bytes[i]));
            }
            return hashBuilder.toString();
        }
        catch (Exception exception) {
            Log.e(Consts.LOG_TAG, "Can't digest md5 hash", exception);
            return null;
        }
    }

    public static int convertDpToPixel(Context context, float dp) {
        return (int)(dp * ((float)context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static void fadeInOut(View fadeOutView, View fadeInView) {
        fadeOutView.animate()
            .alpha(0)
            .setDuration(Consts.ANIMATION_DURATION)
            .setInterpolator(new AccelerateDecelerateInterpolator())
            .withEndAction(() -> {
                fadeOutView.setVisibility(View.GONE);
            });

        fadeInView.setVisibility(View.VISIBLE);
        fadeInView.setAlpha(0);
        fadeInView.animate()
            .alpha(1)
            .setDuration(Consts.ANIMATION_DURATION)
            .setInterpolator(new AccelerateDecelerateInterpolator());
    }

    public static void fadeInTextView(Context context, TextView textView) {
        ValueAnimator backgroundColorAnimation = ValueAnimator.ofArgb(((ColorDrawable)textView.getBackground()).getColor(), 0);
        backgroundColorAnimation.setDuration(Consts.ANIMATION_DURATION);
        backgroundColorAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        backgroundColorAnimation.addUpdateListener(animator -> {
            textView.setBackgroundColor((int)animator.getAnimatedValue());
        });
        backgroundColorAnimation.start();

        ValueAnimator textColorAnimation = ValueAnimator.ofArgb(0, textView.getCurrentTextColor());
        textColorAnimation.setDuration(Consts.ANIMATION_DURATION);
        textColorAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        textColorAnimation.addUpdateListener(animator -> {
            textView.setTextColor((int)animator.getAnimatedValue());
        });
        textColorAnimation.start();
    }
}
