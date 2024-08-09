package nl.plaatsoft.redsquare.android.models;

import android.graphics.Canvas;
import android.graphics.Paint;

import nl.plaatsoft.redsquare.android.Random;

public class BlueSquare extends Square {
    private int vx;
    private int vy;
    private float speed;
    private final Random random;
    private final Paint paint;

    public BlueSquare(Random random, float x, float y, float width, float height, int vx, int vy, float speed) {
        super(x, y, width, height);
        this.vx = vx;
        this.vy = vy;
        this.speed = speed;
        this.random = random;

        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xcc1155ff);
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void update(Canvas canvas) {
        x += vx * speed;
        y += vy * speed;

        if (x < 1 || x + width >= canvas.getWidth() - 1) {
            vx = -vx;
            if (random.nextInt(1, 3) == 1) {
                vy = -vy;
            }
        }

        if (y < 1 || y + height >= canvas.getHeight() - 1) {
            vy = -vy;
            if (random.nextInt(1, 3) == 1) {
                vx = -vx;
            }
        }
    }

    public void draw(Canvas canvas) {
        canvas.drawRect(x, y, x + width, y + height, paint);
    }
}
