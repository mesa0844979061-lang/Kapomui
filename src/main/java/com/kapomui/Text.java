package com.kapomui;

import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Color / gradient / time formatting helpers. */
public final class Text {
    private static final Pattern HEX = Pattern.compile("&#([A-Fa-f0-9]{6})");

    private Text() {}

    /** Converts &a style codes and &#RRGGBB hex codes to section-sign codes. */
    public static String color(String s) {
        if (s == null) return "";
        Matcher m = HEX.matcher(s);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            m.appendReplacement(sb, Matcher.quoteReplacement(hex(Integer.parseInt(m.group(1), 16))));
        }
        m.appendTail(sb);
        return ChatColor.translateAlternateColorCodes('&', sb.toString());
    }

    public static String hex(int rgb) {
        String h = String.format("%06x", rgb & 0xFFFFFF);
        StringBuilder sb = new StringBuilder("\u00a7x");
        for (char c : h.toCharArray()) sb.append('\u00a7').append(c);
        return sb.toString();
    }

    public static int parseHex(String s, int fallback) {
        try {
            return Integer.parseInt(s.replace("#", "").trim(), 16);
        } catch (Exception e) {
            return fallback;
        }
    }

    /** Per-letter gradient. phase (0..1) slides the gradient so it can be animated. */
    public static String gradient(String text, int[] colors, double phase, boolean bold) {
        int visible = 0;
        for (char c : text.toCharArray()) if (c != ' ') visible++;
        StringBuilder sb = new StringBuilder();
        int idx = 0;
        for (char c : text.toCharArray()) {
            if (c == ' ') {
                sb.append(c);
                continue;
            }
            double t = visible <= 1 ? 0 : (double) idx / visible;
            idx++;
            t = (t + phase) % 1.0;
            sb.append(hex(sample(colors, t)));
            if (bold) sb.append("\u00a7l");
            sb.append(c);
        }
        return sb.toString();
    }

    private static int sample(int[] colors, double t) {
        double p = t * colors.length;
        int i = (int) Math.floor(p) % colors.length;
        double f = p - Math.floor(p);
        int a = colors[i];
        int b = colors[(i + 1) % colors.length];
        int r = lerp((a >> 16) & 255, (b >> 16) & 255, f);
        int g = lerp((a >> 8) & 255, (b >> 8) & 255, f);
        int bl = lerp(a & 255, b & 255, f);
        return (r << 16) | (g << 8) | bl;
    }

    private static int lerp(int a, int b, double f) {
        return (int) Math.round(a + (b - a) * f);
    }

    public static String duration(long seconds) {
        if (seconds < 0) seconds = 0;
        long d = seconds / 86400;
        long h = (seconds % 86400) / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        if (d > 0) return d + "d " + h + "h " + m + "m";
        if (h > 0) return h + "h " + m + "m";
        return m + "m " + s + "s";
    }
}
