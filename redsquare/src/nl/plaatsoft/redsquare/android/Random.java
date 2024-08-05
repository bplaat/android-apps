package nl.plaatsoft.redsquare.android;

public class Random extends java.util.Random {
    public static final long serialVersionUID = 1;

    public Random(long seed) {
        super(seed);
    }

    public int nextInt(int min, int max) {
        return nextInt(max - min + 1) + min;
    }
}
