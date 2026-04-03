/*
 * Copyright (c) 2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.reacttest.models;

import java.util.UUID;

public record Person(UUID id, String name, int age, boolean isDead) {
    public Person(String name, int age) {
        this(UUID.randomUUID(), name, age, age >= 100);
    }

    public Person withAgeIncrement(int years) {
        var newAge = Math.max(0, Math.min(100, age + years));
        return new Person(id, name, newAge, newAge >= 100);
    }
}
