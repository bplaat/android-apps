package ml.bastiaan.reactdroid;

public class Person {
    public long id;
    public String name;
    public int age;
    public boolean dead = false;

    public Person(long id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }
}
