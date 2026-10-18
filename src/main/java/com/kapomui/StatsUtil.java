package com.kapomui;

import org.bukkit.NamespacedKey;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

/** Reads player stats. Duel wins/losses are written by the KitDuel plugin (1v1 only, no party). */
final class StatsUtil {
    private static final Statistic PLAYTIME = resolvePlaytime();
    private static final NamespacedKey WINS = NamespacedKey.fromString("kitduel:duel_wins");
    private static final NamespacedKey LOSSES = NamespacedKey.fromString("kitduel:duel_losses");

    private StatsUtil() {}

    private static Statistic resolvePlaytime() {
        for (String n : new String[]{"PLAY_ONE_MINUTE", "PLAY_TIME"}) {
            try {
                return Statistic.valueOf(n);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return null;
    }

    static long playtimeSeconds(Player p) {
        if (PLAYTIME == null) return 0;
        try {
            return p.getStatistic(PLAYTIME) / 20L;
        } catch (Exception e) {
            return 0;
        }
    }

    static int kills(Player p) {
        try {
            return p.getStatistic(Statistic.PLAYER_KILLS);
        } catch (Exception e) {
            return 0;
        }
    }

    static int deaths(Player p) {
        try {
            return p.getStatistic(Statistic.DEATHS);
        } catch (Exception e) {
            return 0;
        }
    }

    static int wins(Player p) {
        return read(p, WINS);
    }

    static int losses(Player p) {
        return read(p, LOSSES);
    }

    private static int read(Player p, NamespacedKey key) {
        if (key == null) return 0;
        return p.getPersistentDataContainer().getOrDefault(key, PersistentDataType.INTEGER, 0);
    }
}
