package com.kapomui;

import java.lang.management.ManagementFactory;
import java.util.Locale;

/** Server-wide values computed once per refresh and shared by every player's UI. */
record Snapshot(String server, String title, String line, int online, int max,
                String uptime, String ramUsed, String ramMax, int ramPct, String ramBar) {

    static Snapshot capture(KapomUIPlugin pl) {
        double phase = pl.phase();
        String title = Text.gradient(pl.serverName(), pl.colors(), phase, true);
        String line = Text.gradient("\u2501".repeat(18), pl.colors(), phase, false);

        long upSec = (System.currentTimeMillis() - ManagementFactory.getRuntimeMXBean().getStartTime()) / 1000L;

        Runtime rt = Runtime.getRuntime();
        long maxMem = rt.maxMemory();
        long used = rt.totalMemory() - rt.freeMemory();
        int pct = maxMem <= 0 ? 0 : (int) Math.round(used * 100.0 / maxMem);
        pct = Math.max(0, Math.min(100, pct));

        int seg = 12;
        int filled = Math.round(pct / 100f * seg);
        String col = pct < 60 ? "&#55FFAA" : pct < 85 ? "&#FFE55C" : "&#FF5555";
        String bar = col + "\u25a0".repeat(filled) + "&8" + "\u25a0".repeat(seg - filled);

        return new Snapshot(
                pl.serverName(), title, line,
                org.bukkit.Bukkit.getOnlinePlayers().size(), org.bukkit.Bukkit.getMaxPlayers(),
                Text.duration(upSec),
                String.format(Locale.US, "%,d", used / 1048576L),
                String.format(Locale.US, "%,d", maxMem / 1048576L),
                pct, bar);
    }
}
