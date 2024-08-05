package nl.plaatsoft.redsquare.android;

import android.graphics.Canvas;
import android.graphics.Paint;

public class BlueSquare extends Square {
    private int vx;
    private int vy;
    private float speed;
    private Paint paint;

    public BlueSquare(float x, float y, float width, float height, int vx, int vy, float speed) {
        super(x, y, width, height);
        this.vx = vx;
        this.vy = vy;
        this.speed = speed;

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
            if (Random.rand(1, 3) == 1) {
                vy = -vy;
            }
        }

        if (y < 1 || y + height >= canvas.getHeight() - 1) {
            vy = -vy;
            if (Random.rand(1, 3) == 1) {
                vx = -vx;
            }
        }
    }

    public void draw(Canvas canvas) {
        canvas.drawRect(x, y, x + width, y + height, paint);
    }
}
