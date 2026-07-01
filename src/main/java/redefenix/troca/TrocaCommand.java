package redefenix.troca;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TrocaCommand implements CommandExecutor {

    private final RFTrocaPlugin plugin;

    public TrocaCommand(RFTrocaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;

        if (!p.hasPermission("rftroca.usar")) {
            p.sendMessage(plugin.message("no-permission"));
            return true;
        }

        if (args.length == 0) {
            p.sendMessage(plugin.message("usage"));
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("aceitar")) {
            Player target = Bukkit.getPlayer(args[1]);
            if (target != null && plugin.getTrocaManager().hasRequestFrom(p, target)) {
                plugin.getTrocaManager().removeRequest(p);
                target.sendMessage(plugin.message("request-accepted-sender", "{player}", p.getName()));
                plugin.getTrocaManager().startTrade(target, p);
            } else {
                p.sendMessage(plugin.message("request-invalid"));
            }
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("negar")) {
            Player target = Bukkit.getPlayer(args[1]);
            if (target != null && plugin.getTrocaManager().hasRequestFrom(p, target)) {
                plugin.getTrocaManager().removeRequest(p);
                target.sendMessage(plugin.message("request-denied-sender", "{player}", p.getName()));
                p.sendMessage(plugin.message("request-denied-target", "{player}", target.getName()));
            }
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || target == p) {
            p.sendMessage(plugin.message("player-invalid"));
            return true;
        }

        if (plugin.getTrocaManager().getSession(p) != null || plugin.getTrocaManager().getSession(target) != null) {
            p.sendMessage(plugin.message("already-trading"));
            return true;
        }

        plugin.getTrocaManager().sendRequest(p, target);
        p.sendMessage(plugin.message("request-sent", "{player}", target.getName()));

        Component msg = Component.text(plugin.message("request-received", "{player}", p.getName()) + "\n", NamedTextColor.YELLOW)
                .append(Component.text(plugin.message("button-accept"))
                        .color(NamedTextColor.GREEN)
                        .decorate(TextDecoration.BOLD)
                        .clickEvent(ClickEvent.runCommand("/trocar aceitar " + p.getName())))
                .append(Component.text(plugin.message("button-deny"))
                        .color(NamedTextColor.RED)
                        .decorate(TextDecoration.BOLD)
                        .clickEvent(ClickEvent.runCommand("/trocar negar " + p.getName())));

        target.sendMessage(msg);
        return true;
    }
}
