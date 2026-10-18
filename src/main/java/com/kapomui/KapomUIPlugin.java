package com.kapomui;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class KapomUIPlugin extends JavaPlugin implements Listener, CommandExecutor, TabCompleter {
    private static final String PREFIX = "&#7C4DFF&lKAPOM &8» &7";

    private TabManager tab;
    private SidebarManager sidebar;
    private BukkitTask task;

    private String serverName = "KAPOM";
    private int[] colors = {0x00E5FF, 0x7C4DFF, 0xFF4DD2};
    private boolean animate = true;
    private long animMs = 4000;
    private boolean warned = false;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        tab = new TabManager(this);
        sidebar = new SidebarManager(this);
        loadSettings();
        Bukkit.getPluginManager().registerEvents(this, this);
        PluginCommand cmd = getCommand("kapomui");
        if (cmd != null) {
            cmd.setExecutor(this);
            cmd.setTabCompleter(this);
        }
        startTask();
        getLogger().info("KapomUI enabled");
    }

    @Override
    public void onDisable() {
        if (task != null) task.cancel();
        for (Player p : Bukkit.getOnlinePlayers()) {
            tab.reset(p);
            sidebar.reset(p);
        }
    }

    private void loadSettings() {
        serverName = getConfig().getString("server-name", "KAPOM");
        List<String> list = getConfig().getStringList("theme.colors");
        if (list.size() >= 2) {
            colors = new int[list.size()];
            for (int i = 0; i < colors.length; i++) colors[i] = Text.parseHex(list.get(i), 0xFFFFFF);
        }
        animate = getConfig().getBoolean("theme.animate", true);
        animMs = Math.max(500L, (long) (getConfig().getDouble("theme.animation-seconds", 4.0) * 1000));
    }

    private void startTask() {
        if (task != null) task.cancel();
        long period = Math.max(1L, getConfig().getLong("update-ticks", 5L));
        task = Bukkit.getScheduler().runTaskTimer(this, this::tick, 20L, period);
    }

    String serverName() {
        return serverName;
    }

    int[] colors() {
        return colors;
    }

    double phase() {
        return animate ? (System.currentTimeMillis() % animMs) / (double) animMs : 0.0;
    }

    void tick() {
        if (Bukkit.getOnlinePlayers().isEmpty()) return;
        Snapshot s = Snapshot.capture(this);
        boolean tabOn = getConfig().getBoolean("tab.enabled", true);
        for (Player p : Bukkit.getOnlinePlayers()) {
            try {
                if (tabOn) tab.update(s, p);
                sidebar.update(s, p);
            } catch (Exception ex) {
                if (!warned) {
                    warned = true;
                    getLogger().warning("UI update failed: " + ex);
                }
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Bukkit.getScheduler().runTask(this, this::tick);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        tab.forget(e.getPlayer());
        sidebar.forget(e.getPlayer());
        Bukkit.getScheduler().runTask(this, this::tick);
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] a) {
        String sub = a.length == 0 ? "help" : a[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "reload" -> {
                if (!s.hasPermission("kapomui.admin")) {
                    s.sendMessage(Text.color(PREFIX + "&cYou don't have permission."));
                    return true;
                }
                reloadConfig();
                loadSettings();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    tab.reset(p);
                    sidebar.reset(p);
                }
                startTask();
                s.sendMessage(Text.color(PREFIX + "&aConfig reloaded."));
            }
            case "sidebar" -> {
                if (!(s instanceof Player p)) {
                    s.sendMessage("Players only.");
                    return true;
                }
                sidebar.toggle(p);
                boolean hidden = sidebar.isHidden(p);
                if (hidden) sidebar.reset(p);
                p.sendMessage(Text.color(PREFIX + "Sidebar " + (hidden ? "&chidden" : "&ashown") + "&7."));
                tick();
            }
            default -> {
                s.sendMessage(Text.color(PREFIX + "&f/kapomui sidebar &8- &7toggle the sidebar"));
                if (s.hasPermission("kapomui.admin")) {
                    s.sendMessage(Text.color(PREFIX + "&f/kapomui reload &8- &7reload config"));
                }
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command cmd, String label, String[] a) {
        List<String> out = new ArrayList<>();
        if (a.length == 1) {
            out.add("sidebar");
            if (s.hasPermission("kapomui.admin")) out.add("reload");
            out.removeIf(x -> !x.startsWith(a[0].toLowerCase(Locale.ROOT)));
        }
        return out;
    }
}
