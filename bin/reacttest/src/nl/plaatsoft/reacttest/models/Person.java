/*
 * Copyright (c) 2021-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.reacttest.models;

public record Person(long id, String name, int age, boolean isDead) {
    public Person(long id, String name, int age) {
        this(id, name, age, age >= 100);
    }

    public Person ageInYears(int years) {
        var new_age = Math.max(0, Math.min(100, age + years));
        return new Person(id, name, new_age, new_age >= 100);
    }
}
