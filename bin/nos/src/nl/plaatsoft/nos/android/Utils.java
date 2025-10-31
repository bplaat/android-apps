/*
 * Copyright (c) 2019-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.nos.android;

import java.security.MessageDigest;

public class Utils {
    private Utils() {}

    public static String md5(String data) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(data.getBytes());
            byte[] bytes = messageDigest.digest();
            String hash = "";
            for (int i = 0; i < bytes.length; i++) {
                hash += String.format("%02x", bytes[i]);
            }
            return hash;
        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }
}
