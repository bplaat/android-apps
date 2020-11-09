package nl.plaatsoft.redsquare.android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

public class GamePage extends View {
    public interface OnEventListener {
        public void onGameover(int score, int seconds, int level);
    }

    private boolean running = false;
    private boolean dragging = false;
    private int score;
    private long startTime;
    private long levelTime;
    private int level;
    private float scale;
    private int width;
    private int height;
    private int borderWidth;
    private RedSquare redsquare;
    private BlueSquare[] blueSquares;
    private Paint paint;
    private OnEventListener onEventListener;

    private String scoreLabelString;
    private String timeLabelString;
    private String levelLabelString;

    public GamePage(Context context, AttributeSet attrs) {
        super(context, attrs);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        scale = metrics.density;
        width = (int)(metrics.widthPixels / scale);
        height = (int)(metrics.heightPixels / scale);

        paint = new Paint();

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
        running = true;

        score = 0;
        startTime = System.currentTimeMillis();
        levelTime = startTime;
        level = 1;
        borderWidth = 0;

        Random.seed = startTime;

        int redsquareSize = 60;
        redsquare = new RedSquare(((width - redsquareSize) / 2) * scale, ((height - redsquareSize) / 2) * scale, redsquareSize * scale, redsquareSize * scale);

        blueSquares = new BlueSquare[4];
        blueSquares[0] = new BlueSquare(scale, scale, Random.rand(50, 100) * scale, Random.rand(75, 125) * scale, 1, 1, 0.5f * scale);
        int _width = Random.rand(125, 150);
        blueSquares[1] = new BlueSquare((width - _width - 1) * scale, scale, _width * scale, Random.rand(50, 100) * scale, -1, 1, 0.5f * scale);
        int _height = Random.rand(75, 125);
        blueSquares[2] = new BlueSquare(2 * scale, (height - _height - 1) * scale, Random.rand(50, 100) * scale, _height * scale, 1, -1, 0.5f * scale);

        _width = Random.rand(75, 125);
        _height = Random.rand(125, 150);
        blueSquares[3] = new BlueSquare((width - _width - 1) * scale, (height - _height - 1) * scale, _width * scale, _height * scale, -1, -1, 0.5f * scale);

        invalidate();
    }

    public void stop() {
        running = false;
    }

    private void gameover() {
        stop();
        int seconds = (int)((System.currentTimeMillis() - startTime) / 1000);
        onEventListener.onGameover(score, seconds, level);
    }

    protected void onDraw(Canvas canvas) {
        if (running) {
            if (System.currentTimeMillis() - levelTime > 10000) {
                level++;
                borderWidth += Random.rand(4, 12);
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

        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0x22ffffff);
        canvas.drawRect(borderWidth * scale, borderWidth * scale, (width - borderWidth) * scale, (height - borderWidth) * scale, paint);

        for (int i = 0; i < blueSquares.length; i++) {
            blueSquares[i].draw(canvas);
        }

        redsquare.draw(canvas);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Utils.getColor(getContext(), R.color.primary_text_color));
        paint.setTextSize(16 * scale);
        paint.setTextAlign(Paint.Align.LEFT);

        canvas.drawText(String.format(scoreLabelString, score), 16 * scale, (16 + 8) * scale, paint);

        paint.setTextAlign(Paint.Align.CENTER);
        int seconds = (int)((System.currentTimeMillis() - startTime) / 1000);
        canvas.drawText(String.format(timeLabelString, seconds / 60, seconds % 60), (width / 2) * scale, (16 + 8) * scale, paint);

        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(String.format(levelLabelString, level), (width - 16) * scale, (16 + 8) * scale, paint);

        if (running) {
            invalidate();
            score += level;
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

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

        else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (dragging) {
                redsquare.setX(touchX - redsquare.getWidth() / 2);
                redsquare.setY(touchY - redsquare.getHeight() / 2);
            }

            return true;
        }

        else if (event.getAction() == MotionEvent.ACTION_UP) {
            dragging = false;

            return true;
        }

        else {
            return false;
        }
    }
}
