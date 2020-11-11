package nl.plaatsoft.bassietest;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.View;
import android.widget.TextView;
import java.security.MessageDigest;

public class Utils {
    private Utils() {}

    // Function that hashes its input data to a md5 hash
    public static String md5(String data) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(data.getBytes());
            byte[] bytes = messageDigest.digest();
            String hash = "";
            for (int i = 0; i < bytes.length; i++) {
                hash += String.format("%02x", bytes[i]);
            }
            return hash;
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
            .setDuration(Config.ANIMATION_FADE_IN_DURATION)
            .withEndAction(() -> {
                fadeOutView.setVisibility(View.GONE);
            });

        fadeInView.setVisibility(View.VISIBLE);
        fadeInView.setAlpha(0);
        fadeInView.animate()
            .alpha(1)
            .setDuration(Config.ANIMATION_FADE_IN_DURATION);
    }

    // A function that fades in a textview
    public static void fadeInTextView(Context context, TextView textView) {
        ValueAnimator backgroundColorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), ((ColorDrawable)textView.getBackground()).getColor(), context.getColor(android.R.color.transparent));
        backgroundColorAnimation.addUpdateListener((ValueAnimator animator) -> {
            textView.setBackgroundColor((int)animator.getAnimatedValue());
        });
        backgroundColorAnimation.setDuration(Config.ANIMATION_FADE_IN_DURATION);
        backgroundColorAnimation.start();

        ValueAnimator textColorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), context.getColor(android.R.color.transparent), textView.getCurrentTextColor());
        textColorAnimation.addUpdateListener((ValueAnimator animator) -> {
            textView.setTextColor((int)animator.getAnimatedValue());
        });
        textColorAnimation.setDuration(Config.ANIMATION_FADE_IN_DURATION);
        textColorAnimation.start();
    }

    // Function that opens the right store page for this app
    public static void openStorePage(Context context) {
        if (Config.SETTINGS_OVERRIDE_STORE_PAGE != null) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Config.SETTINGS_OVERRIDE_STORE_PAGE)));
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
