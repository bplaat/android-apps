package nl.plaatsoft.redsquare.android;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

public class GameView extends View {
    private boolean running = false;
    private int frame = 0;
    private boolean dragging = false;

    private RedSquare redsquare;
    private BlueSquare[] blueSquares;

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float scale = metrics.density;
        int width = (int)(metrics.widthPixels / scale);
        int height = (int)(metrics.heightPixels / scale);

        redsquare = new RedSquare((int)(((width - 75) / 2) * scale), (int)(((height - 75) / 2) * scale), (int)(75 * scale), (int)(75 * scale));

        blueSquares = new BlueSquare[4];
        blueSquares[0] = new BlueSquare((int)(0 * scale), (int)(0 * scale), (int)(75 * scale), (int)(100 * scale), (int)(2 * scale), (int)(2 * scale));
        blueSquares[1] = new BlueSquare((int)((width - 150) * scale), (int)(0 * scale), (int)(150 * scale), (int)(75 * scale), (int)(-2 * scale), (int)(2 * scale));
        blueSquares[2] = new BlueSquare((int)(0 * scale), (int)((height - 125) * scale), (int)(75 * scale), (int)(125 * scale), (int)(2 * scale), (int)(-2 * scale));
        blueSquares[3] = new BlueSquare((int)((width - 100) * scale), (int)((height - 150) * scale), (int)(100 * scale), (int)(150 * scale), (int)(-2 * scale), (int)(-2 * scale));
    }

    public void start() {
        running = true;
        invalidate();
    }

    public void pause() {
        running = false;
    }

    protected void onDraw(Canvas canvas) {
        if (running) {
            for (int i = 0; i < blueSquares.length; i++) {
                blueSquares[i].update(canvas);

                if (blueSquares[i].collision(redsquare)) {

                }
            }

            canvas.drawRGB((frame * 2) % 256, (frame / 2) % 256, (frame << 2) % 256);

            for (int i = 0; i < blueSquares.length; i++) {
                blueSquares[i].draw(canvas);
            }

            redsquare.draw(canvas);

            invalidate();
            frame++;
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (
                touchX >= redsquare.getX() && touchY >= redsquare.getY() &&
                touchX < redsquare.getX() + redsquare.getWidth() &&
                touchY < redsquare.getY() + redsquare.getHeight()
            ) {
                dragging = true;
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
