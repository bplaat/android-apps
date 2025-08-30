/*
 * Copyright (c) 2021-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.reacttest.models;

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
