package nl.plaatsoft.bassietest;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.View;
import android.widget.TextView;
import java.security.MessageDigest;

public class Utils {
    private Utils() {}

    // Function that hashes its input data to a md5 hash string
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
            exception.printStackTrace();
            return null;
        }
    }

    // A function that fades a view out and a view in
    public static void fadeInOut(View fadeOutView, View fadeInView) {
        fadeOutView.animate()
            .alpha(0)
            .setDuration(Config.APP_ANIMATION_DURATION)
            .setInterpolator(new AccelerateDecelerateInterpolator())
            .withEndAction(() -> {
                fadeOutView.setVisibility(View.GONE);
            });

        fadeInView.setVisibility(View.VISIBLE);
        fadeInView.setAlpha(0);
        fadeInView.animate()
            .alpha(1)
            .setDuration(Config.APP_ANIMATION_DURATION)
            .setInterpolator(new AccelerateDecelerateInterpolator());
    }

    // A function that fades in a textview
    public static void fadeInTextView(Context context, TextView textView) {
        ValueAnimator backgroundColorAnimation = ValueAnimator.ofArgb(((ColorDrawable)textView.getBackground()).getColor(), 0);
        backgroundColorAnimation.setDuration(Config.APP_ANIMATION_DURATION);
        backgroundColorAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        backgroundColorAnimation.addUpdateListener(animator -> {
            textView.setBackgroundColor((int)animator.getAnimatedValue());
        });
        backgroundColorAnimation.start();

        ValueAnimator textColorAnimation = ValueAnimator.ofArgb(0, textView.getCurrentTextColor());
        textColorAnimation.setDuration(Config.APP_ANIMATION_DURATION);
        textColorAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        textColorAnimation.addUpdateListener(animator -> {
            textView.setTextColor((int)animator.getAnimatedValue());
        });
        textColorAnimation.start();
    }

    // Function that resturns the url of the right store page
    public static String getStorePageUrl(Context context) {
        if (Config.APP_OVERRIDE_STORE_PAGE_URL != null) {
            return Config.APP_OVERRIDE_STORE_PAGE_URL;
        } else {
            return "https://play.google.com/store/apps/details?id=" + context.getPackageName();
        }
    }

    // Function that opens the right store page for this app
    public static void openStorePage(Context context) {
        if (Config.APP_OVERRIDE_STORE_PAGE_URL != null) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Config.APP_OVERRIDE_STORE_PAGE_URL)));
        } else {
            String appPackageName = context.getPackageName();
            try {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            } catch (Exception exception) {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            }
        }
    }
}
