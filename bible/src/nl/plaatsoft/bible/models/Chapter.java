package nl.plaatsoft.bible.models;

import java.util.ArrayList;

public record Chapter(int id, int number, ArrayList<Verse> verses) {}
