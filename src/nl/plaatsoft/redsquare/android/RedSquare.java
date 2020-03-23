package nl.plaatsoft.redsquare.android;

import android.graphics.Canvas;
import android.graphics.Paint;

public class RedSquare extends Square {
    private Paint paint;

    public RedSquare(int x, int y, int width, int height) {
        super(x, y, width, height);

        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xaaff0000);
    }

    public void draw(Canvas canvas) {
        canvas.drawRect(x, y, x + width, y + height, paint);
    }
}
