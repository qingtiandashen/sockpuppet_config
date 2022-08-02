package com.andrea.sockpuppet.plugin.utils;

public class StringUtils {

    public static String formatSize(long size) {
        if (size < 1024) {
            return (size / 1024) + "k";
        } else {
            return (size / (1024 * 1024)) + "M";
        }
    }

    /**
     * 是不是bool值
     *
     * @param s
     * @return
     */
    public static boolean isBool(String s) {
        return ((s != null) && (s.equalsIgnoreCase("true") || s.equalsIgnoreCase("false")));
    }

    /**
     * 是不是int值
     *
     * @param s
     * @return
     */
    public static boolean isInt(String s) {
        if (s == null) {
            return false;
        }
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Returns true if the parameter is null or of zero length
     */
    public static boolean isEmpty(final CharSequence s) {
        if (s == null) {
            return true;
        }
        return s.length() == 0;
    }

    /**
     * Returns true if the parameter is null or contains only whitespace
     */
    public static boolean isBlank(final CharSequence s) {
        if (s == null) {
            return true;
        }
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * @since 4.4
     */
    public static boolean containsBlanks(final CharSequence s) {
        if (s == null) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            if (Character.isWhitespace(s.charAt(i))) {
                return true;
            }
        }
        return false;
    }
}
