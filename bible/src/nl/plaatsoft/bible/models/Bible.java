package nl.plaatsoft.bible.models;

import java.time.LocalDateTime;
import java.util.ArrayList;

public record Bible(
    String path,
    String name,
    String abbreviation,
    String language,
    String copyright,
    LocalDateTime releasedAt,
    ArrayList<Testament> testaments
) {}
