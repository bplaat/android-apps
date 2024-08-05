package nl.plaatsoft.redsquare.android;

public class Random {
    public static long seed = 1;

    private Random() {}

    public static double random() {
        double x = Math.sin(seed++) * 10000;
        return x - Math.floor(x);
    }

    public static int rand(int min, int max) {
        return (int)(random() * (max - min + 1)) + min;
    }
}
