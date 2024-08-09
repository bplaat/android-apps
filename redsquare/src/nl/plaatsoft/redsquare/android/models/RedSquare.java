package nl.plaatsoft.redsquare.android.models;

import android.graphics.Canvas;
import android.graphics.Paint;

public class RedSquare extends Square {
    private final Paint paint;

    public RedSquare(float x, float y, float width, float height) {
        super(x, y, width, height);

        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xccee3322);
    }

    public void draw(Canvas canvas) {
        canvas.drawRect(x, y, x + width, y + height, paint);
    }
}
