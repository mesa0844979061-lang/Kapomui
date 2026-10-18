package com.kapomui;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Tab list: header (server name + online count), entries "[name] [ping]", footer (uptime + RAM). */
final class TabManager {
    private final KapomUIPlugin plugin;
    private final Map<UUID, String> lastHeaderFooter = new HashMap<>();
    private final Map<UUID, String> lastName = new HashMap<>();

    TabManager(KapomUIPlugin plugin) {
        this.plugin = plugin;
    }

    void update(Snapshot s, Player p) {
        FileConfiguration c = plugin.getConfig();
        String header = Placeholders.apply(String.join("\n", c.getStringList("tab.header")), s, p);
        String footer = Placeholders.apply(String.join("\n", c.getStringList("tab.footer")), s, p);
        UUID id = p.getUniqueId();

        String hf = header + "\u0001" + footer;
        if (!hf.equals(lastHeaderFooter.get(id))) {
            p.setPlayerListHeaderFooter(header, footer);
            lastHeaderFooter.put(id, hf);
        }
        String name = Placeholders.apply(c.getString("tab.player-format", "{player}"), s, p);
        if (!name.equals(lastName.get(id))) {
            p.setPlayerListName(name);
            lastName.put(id, name);
        }
    }

    void forget(Player p) {
        lastHeaderFooter.remove(p.getUniqueId());
        lastName.remove(p.getUniqueId());
    }

    void reset(Player p) {
        forget(p);
        p.setPlayerListHeader(null);
        p.setPlayerListFooter(null);
        p.setPlayerListName(null);
    }
}
