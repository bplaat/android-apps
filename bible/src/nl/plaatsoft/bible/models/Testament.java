package nl.plaatsoft.bible.models;

import java.util.ArrayList;

public record Testament(int id, String key, String name, ArrayList<Book> books) {}
