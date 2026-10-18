package com.kapomui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Per-player flicker-free sidebar (each line is a team prefix on a unique invisible entry). */
final class SidebarManager {
    private static final int MAX_LINES = 15;

    private static final class Board {
        Scoreboard sb;
        Objective obj;
        Team[] teams;
        String[] last;
        String lastTitle;
    }

    private final KapomUIPlugin plugin;
    private final NamespacedKey offKey;
    private final Map<UUID, Board> boards = new HashMap<>();

    SidebarManager(KapomUIPlugin plugin) {
        this.plugin = plugin;
        this.offKey = new NamespacedKey(plugin, "sidebar_off");
    }

    void update(Snapshot s, Player p) {
        boolean on = plugin.getConfig().getBoolean("sidebar.enabled", true)
                && !p.getPersistentDataContainer().has(offKey, PersistentDataType.BYTE);
        if (!on) {
            if (boards.containsKey(p.getUniqueId())) reset(p);
            return;
        }
        List<String> tpl = plugin.getConfig().getStringList("sidebar.lines");
        if (tpl.size() > MAX_LINES) tpl = tpl.subList(0, MAX_LINES);

        Board b = boards.get(p.getUniqueId());
        if (b == null || b.teams.length != tpl.size()) b = create(p, tpl.size());

        String title = Placeholders.apply(plugin.getConfig().getString("sidebar.title", "{title}"), s, p);
        if (!title.equals(b.lastTitle)) {
            setTitle(b.obj, title);
            b.lastTitle = title;
        }
        for (int i = 0; i < tpl.size(); i++) {
            String text = Placeholders.apply(tpl.get(i), s, p);
            if (!text.equals(b.last[i])) {
                setPrefix(b.teams[i], text);
                b.last[i] = text;
            }
        }
    }

    private Board create(Player p, int lines) {
        Board b = new Board();
        b.sb = Bukkit.getScoreboardManager().getNewScoreboard();
        b.obj = b.sb.registerNewObjective("kapomui", Criteria.DUMMY, "KAPOM");
        b.obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        hideNumbers(b.obj);
        b.teams = new Team[lines];
        b.last = new String[lines];
        for (int i = 0; i < lines; i++) {
            String entry = "\u00a7" + Integer.toHexString(i) + "\u00a7r";
            Team t = b.sb.registerNewTeam("l" + i);
            t.addEntry(entry);
            b.teams[i] = t;
            b.obj.getScore(entry).setScore(lines - i);
        }
        boards.put(p.getUniqueId(), b);
        p.setScoreboard(b.sb);
        return b;
    }

    /** Paper only: hides the red score numbers. Silently skipped on Spigot. */
    private static void hideNumbers(Objective obj) {
        try {
            Class<?> nf = Class.forName("io.papermc.paper.scoreboard.numbers.NumberFormat");
            Object blank = nf.getMethod("blank").invoke(null);
            Objective.class.getMethod("numberFormat", nf).invoke(obj, blank);
        } catch (Throwable ignored) {
        }
    }

    private static void setTitle(Objective obj, String title) {
        try {
            obj.setDisplayName(title);
        } catch (IllegalArgumentException ex) {
            obj.setDisplayName(ChatColor.stripColor(title));
        }
    }

    private static void setPrefix(Team t, String text) {
        try {
            t.setPrefix(text);
        } catch (IllegalArgumentException ex) {
            String plain = ChatColor.stripColor(text);
            t.setPrefix(plain.length() > 64 ? plain.substring(0, 64) : plain);
        }
    }

    void toggle(Player p) {
        if (p.getPersistentDataContainer().has(offKey, PersistentDataType.BYTE)) {
            p.getPersistentDataContainer().remove(offKey);
        } else {
            p.getPersistentDataContainer().set(offKey, PersistentDataType.BYTE, (byte) 1);
        }
    }

    boolean isHidden(Player p) {
        return p.getPersistentDataContainer().has(offKey, PersistentDataType.BYTE);
    }

    void forget(Player p) {
        boards.remove(p.getUniqueId());
    }

    void reset(Player p) {
        boards.remove(p.getUniqueId());
        if (p.isOnline() && Bukkit.getScoreboardManager() != null) {
            p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }
}
