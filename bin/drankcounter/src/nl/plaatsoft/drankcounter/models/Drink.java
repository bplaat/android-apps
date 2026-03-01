/*
 * Copyright (c) 2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.drankcounter.models;

public record Drink(long id, int type, long createdAt) {
    public static final int TYPE_BEER = 0;
    public static final int TYPE_WINE = 1;
    public static final int TYPE_LIQUEUR = 2;
}
