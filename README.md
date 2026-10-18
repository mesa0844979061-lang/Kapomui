# KapomUI

Tab list + left sidebar for the KAPOM server (Spigot/Paper 1.21+, JDK 21).

**Tab list** — header: KAPOM + players online · entries: `[name] [ping]` · footer: server uptime + RAM
**Sidebar** — server name · player · playtime · kills/deaths · duel wins/losses (1v1 only, from KitDuel 1.2.0+) · online

Build: `mvn clean package` → `target/KapomUI.jar` → put in `plugins/`.
Everything (text, colors, lines) is editable in `config.yml`.

Commands: `/kapomui sidebar` (toggle), `/kapomui reload` (`kapomui.admin`)
