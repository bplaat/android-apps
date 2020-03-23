package nl.plaatsoft.redsquare.android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

public class GameView extends View {
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

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);

        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRealMetrics(metrics);

        scale = metrics.density;
        width = (int)(metrics.widthPixels / scale);
        height = (int)(metrics.heightPixels / scale);

        paint = new Paint();
    }

    public void start() {
        running = true;

        score = 0;
        startTime = System.currentTimeMillis();
        levelTime = System.currentTimeMillis();
        level = 1;
        borderWidth = 20;

        int redsquareSize = 60;
        redsquare = new RedSquare((int)(((width - redsquareSize) / 2) * scale), (int)(((height - redsquareSize) / 2) * scale), (int)(redsquareSize * scale), (int)(redsquareSize * scale));

        blueSquares = new BlueSquare[4];
        blueSquares[0] = new BlueSquare((int)(0 * scale), (int)(0 * scale), (int)(75 * scale), (int)(100 * scale), 1, 1, (int)(1 * scale));
        blueSquares[1] = new BlueSquare((int)((width - 150) * scale), (int)(0 * scale), (int)(150 * scale), (int)(75 * scale), -1, 1, (int)(1 * scale));
        blueSquares[2] = new BlueSquare((int)(0 * scale), (int)((height - 125) * scale), (int)(75 * scale), (int)(125 * scale), 1, -1, (int)(1 * scale));
        blueSquares[3] = new BlueSquare((int)((width - 100) * scale), (int)((height - 150) * scale), (int)(100 * scale), (int)(150 * scale), -1, -1, (int)(1 * scale));

        invalidate();
    }

    public void pause() {
        running = false;
    }

    protected void onDraw(Canvas canvas) {
        if (running) {
            if (System.currentTimeMillis() - levelTime > 10000) {
                borderWidth += level;
                level++;
                levelTime = System.currentTimeMillis();

                for (BlueSquare blueSquare : blueSquares) {
                    blueSquare.setSpeed((int)(level * scale));
                }
            }

            for (BlueSquare blueSquare : blueSquares) {
                blueSquare.update(canvas);
                if (blueSquare.collision(redsquare)) {
                    pause();
                }
            }

            if (
                redsquare.getX() < (int)(borderWidth * scale) ||
                redsquare.getY() < (int)(borderWidth * scale) ||
                redsquare.getX() + redsquare.getWidth() > (int)((width - borderWidth) * scale) ||
                redsquare.getY() + redsquare.getHeight() > (int)((height - borderWidth) * scale)
            ) {
                pause();
            }
        }

        canvas.drawRGB((score * 2) % 256, (score / 2) % 256, (score << 2) % 256);

        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0x44ffffff);
        canvas.drawRect(borderWidth * scale, borderWidth * scale, (width - borderWidth) * scale, (height - borderWidth) * scale, paint);

        for (int i = 0; i < blueSquares.length; i++) {
            blueSquares[i].draw(canvas);
        }

        redsquare.draw(canvas);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xffffffff);
        paint.setTextSize(16 * scale);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("Score: " + score, borderWidth * scale, 32 * scale, paint);

        paint.setTextAlign(Paint.Align.CENTER);
        int seconds = (int)((System.currentTimeMillis() - startTime) / 1000);
        canvas.drawText(String.format("Time: %d:%02d", seconds / 60, seconds % 60), (width / 2) * scale, 32 * scale, paint);

        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("Level: " + level, (width - borderWidth) * scale, 32 * scale, paint);

        if (running) {
            invalidate();
            score += level;
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (!running) {
                start();
            }

            else {
                if (
                    touchX >= redsquare.getX() && touchY >= redsquare.getY() &&
                    touchX < redsquare.getX() + redsquare.getWidth() &&
                    touchY < redsquare.getY() + redsquare.getHeight()
                ) {
                    dragging = true;
                }
            }

            return true;
        }

        else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (dragging) {
                redsquare.setX((int)(touchX - redsquare.getWidth() / 2));
                redsquare.setY((int)(touchY - redsquare.getHeight() / 2));
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
