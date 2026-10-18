package com.kapomui;

import org.bukkit.entity.Player;

final class Placeholders {
    private Placeholders() {}

    static String pingColor(int ping) {
        if (ping < 80) return "&#55FFAA";
        if (ping < 150) return "&#FFE55C";
        if (ping < 250) return "&#FFA24D";
        return "&#FF5555";
    }

    static String apply(String t, Snapshot s, Player p) {
        String r = t.replace("{server}", s.server())
                .replace("{title}", s.title())
                .replace("{line}", s.line())
                .replace("{online}", String.valueOf(s.online()))
                .replace("{max}", String.valueOf(s.max()))
                .replace("{uptime}", s.uptime())
                .replace("{ram_used}", s.ramUsed())
                .replace("{ram_max}", s.ramMax())
                .replace("{ram_percent}", String.valueOf(s.ramPct()))
                .replace("{ram_bar}", s.ramBar());
        if (p != null) {
            int ping = Math.max(0, p.getPing());
            r = r.replace("{player}", p.getName())
                    .replace("{ping}", String.valueOf(ping))
                    .replace("{ping_color}", pingColor(ping))
                    .replace("{playtime}", Text.duration(StatsUtil.playtimeSeconds(p)))
                    .replace("{kills}", String.valueOf(StatsUtil.kills(p)))
                    .replace("{deaths}", String.valueOf(StatsUtil.deaths(p)))
                    .replace("{wins}", String.valueOf(StatsUtil.wins(p)))
                    .replace("{losses}", String.valueOf(StatsUtil.losses(p)));
        }
        return Text.color(r);
    }
}
