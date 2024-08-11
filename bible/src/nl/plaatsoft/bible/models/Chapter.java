package nl.plaatsoft.bible.models;

import java.util.ArrayList;

public record Chapter(int id, int number, Book book, ArrayList<Verse> verses) {} // FIXME
