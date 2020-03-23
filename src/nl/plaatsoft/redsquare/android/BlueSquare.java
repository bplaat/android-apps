package nl.plaatsoft.redsquare.android;

import android.graphics.Canvas;
import android.graphics.Paint;

public class BlueSquare extends Square {
    private int vx;
    private int vy;
    private int speed;
    private Paint paint;

    public BlueSquare(int x, int y, int width, int height, int vx, int vy, int speed) {
        super(x, y, width, height);
        this.vx = vx;
        this.vy = vy;
        this.speed = speed;

        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xaa0000ff);
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public void update(Canvas canvas) {
        x += vx * speed;
        y += vy * speed;
        if (x < 0 || x + width > canvas.getWidth()) vx = -vx;
        if (y < 0 || y + height > canvas.getHeight()) vy = -vy;
    }

    public void draw(Canvas canvas) {
        canvas.drawRect(x, y, x + width, y + height, paint);
    }
}
