/*
 * Copyright (c) 2020-2024 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.redsquare.android.models;

public abstract class Square {
    protected float x;
    protected float y;
    protected float width;
    protected float height;

    public Square(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public boolean collision(Square otherSquare) {
        return otherSquare.getX() < x + width && otherSquare.getX() + otherSquare.getWidth() > x
            && otherSquare.getY() < y + height && otherSquare.getY() + otherSquare.getHeight() > y;
    }
}
