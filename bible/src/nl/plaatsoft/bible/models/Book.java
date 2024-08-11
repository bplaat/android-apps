package nl.plaatsoft.bible.models;

import java.util.ArrayList;

public record Book(int id, String key, String name, ArrayList<Chapter> chapters) {}
