package nl.plaatsoft.redsquare.android.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import nl.plaatsoft.redsquare.android.models.BlueSquare;
import nl.plaatsoft.redsquare.android.models.RedSquare;
import nl.plaatsoft.redsquare.android.Utils;
import nl.plaatsoft.redsquare.android.R;
import nl.plaatsoft.redsquare.android.Random;

public class GamePage extends View {
    public interface OnEventListener {
        public void onGameover(int score, int seconds, int level);
    }

    private boolean started = false;
    private boolean running = false;
    private float scale;
    private int width;
    private int height;
    private boolean dragging = false;
    private int score;
    private long startTime;
    private long levelTime;
    private int level;
    private int borderWidth;
    private Random random;
    private RedSquare redsquare;
    private BlueSquare[] blueSquares;
    private Paint paint;
    private OnEventListener onEventListener;

    private String scoreLabelString;
    private String timeLabelString;
    private String levelLabelString;

    public GamePage(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);

        scoreLabelString = getResources().getString(R.string.game_score_label);
        timeLabelString = getResources().getString(R.string.game_time_label);
        levelLabelString = getResources().getString(R.string.game_level_label);
    }

    public boolean isRunning() {
        return running;
    }

    public void setOnEventListener(OnEventListener onEventListener) {
        this.onEventListener = onEventListener;
    }

    public void start() {
        started = true;
        running = true;

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        scale = metrics.density;
        width = (int)(getWidth() / scale);
        height = (int)(getHeight() / scale);
        paint.setTextSize(16 * scale);

        score = 0;
        startTime = System.currentTimeMillis();
        levelTime = startTime;
        level = 1;
        borderWidth = 0;
        random = new Random(startTime);

        int redsquareSize = 60;
        redsquare = new RedSquare(((width - redsquareSize) / 2) * scale, ((height - redsquareSize) / 2) * scale, redsquareSize * scale, redsquareSize * scale);

        blueSquares = new BlueSquare[4];

        blueSquares[0] = new BlueSquare(random, scale, scale, random.nextInt(50, 100) * scale, random.nextInt(75, 125) * scale, 1, 1, 0.5f * scale);

        var _width = random.nextInt(125, 150);
        blueSquares[1] = new BlueSquare(random, (width - _width - 1) * scale, scale, _width * scale, random.nextInt(50, 100) * scale, -1, 1, 0.5f * scale);

        var _height = random.nextInt(75, 125);
        blueSquares[2] = new BlueSquare(random, 2 * scale, (height - _height - 1) * scale, random.nextInt(50, 100) * scale, _height * scale, 1, -1, 0.5f * scale);

        _width = random.nextInt(75, 125);
        _height = random.nextInt(125, 150);
        blueSquares[3] = new BlueSquare(random, (width - _width - 1) * scale, (height - _height - 1) * scale, _width * scale, _height * scale, -1, -1, 0.5f * scale);

        invalidate();
    }

    public void stop() {
        started = false;
        running = false;
    }

    private void gameover() {
        stop();
        int seconds = (int)((System.currentTimeMillis() - startTime) / 1000);
        onEventListener.onGameover(score, seconds, level);
    }

    protected void onDraw(Canvas canvas) {
        if (!started) return;

        if (running) {
            if (System.currentTimeMillis() - levelTime > 5000) {
                level++;
                borderWidth += random.nextInt(4, 12);
                levelTime = System.currentTimeMillis();

                for (BlueSquare blueSquare : blueSquares) {
                    blueSquare.setSpeed((0.25f + level * 0.25f) * scale);
                }
            }

            for (BlueSquare blueSquare : blueSquares) {
                blueSquare.update(canvas);
                if (blueSquare.collision(redsquare)) {
                    gameover();
                }
            }

            if (
                redsquare.getX() < borderWidth * scale ||
                redsquare.getY() < borderWidth * scale ||
                redsquare.getX() + redsquare.getWidth() > (width - borderWidth) * scale ||
                redsquare.getY() + redsquare.getHeight() > (height - borderWidth) * scale
            ) {
                gameover();
            }
        }

        canvas.drawColor(Color.TRANSPARENT);

        paint.setColor(Utils.contextGetColor(getContext(), R.color.border_background_color));
        canvas.drawRect(borderWidth * scale, borderWidth * scale, (width - borderWidth) * scale, (height - borderWidth) * scale, paint);

        for (int i = 0; i < blueSquares.length; i++) {
            blueSquares[i].draw(canvas);
        }
        redsquare.draw(canvas);

        float textPadding = 24;
        paint.setColor(Utils.contextGetColor(getContext(), R.color.primary_text_color));
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(String.format(scoreLabelString, score), textPadding * scale, (textPadding + 8) * scale, paint);

        paint.setTextAlign(Paint.Align.CENTER);
        var seconds = (int)((System.currentTimeMillis() - startTime) / 1000);
        canvas.drawText(String.format(timeLabelString, seconds / 60, seconds % 60), (width / 2) * scale, (textPadding + 8) * scale, paint);

        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(String.format(levelLabelString, level), (width - textPadding) * scale, (textPadding + 8) * scale, paint);

        if (running) {
            invalidate();
            score += level;
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (!started) return false;

        var touchX = event.getX();
        var touchY = event.getY();

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (
                touchX >= redsquare.getX() &&
                touchY >= redsquare.getY() &&
                touchX < redsquare.getX() + redsquare.getWidth() &&
                touchY < redsquare.getY() + redsquare.getHeight()
            ) {
                dragging = true;
            }
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (dragging) {
                redsquare.setX(touchX - redsquare.getWidth() / 2);
                redsquare.setY(touchY - redsquare.getHeight() / 2);
            }
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            dragging = false;
            return true;
        }

        return false;
    }
}
