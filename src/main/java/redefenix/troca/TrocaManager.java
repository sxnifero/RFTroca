package redefenix.troca;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class TrocaManager {

    private final RFTrocaPlugin plugin;
    private final Map<UUID, UUID> requests = new HashMap<>();
    private final Map<UUID, BukkitTask> requestTasks = new HashMap<>();
    private final Map<UUID, TrocaSession> activeSessions = new HashMap<>();

    public TrocaManager(RFTrocaPlugin plugin) {
        this.plugin = plugin;
    }

    public void sendRequest(Player requester, Player target) {
        UUID targetId = target.getUniqueId();
        removeRequest(target);

        requests.put(targetId, requester.getUniqueId());
        BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!hasRequestFrom(target, requester)) return;

            removeRequest(target);
            requester.sendMessage(plugin.message("request-expired-sender", "{player}", target.getName()));
            target.sendMessage(plugin.message("request-expired-target", "{player}", requester.getName()));
        }, plugin.getRequestExpirationSeconds() * 20L);
        requestTasks.put(targetId, task);
    }

    public boolean hasRequestFrom(Player target, Player requester) {
        return requests.getOrDefault(target.getUniqueId(), null) != null &&
                requests.get(target.getUniqueId()).equals(requester.getUniqueId());
    }

    public void removeRequest(Player target) {
        UUID targetId = target.getUniqueId();
        requests.remove(targetId);

        BukkitTask task = requestTasks.remove(targetId);
        if (task != null) {
            task.cancel();
        }
    }

    public void startTrade(Player p1, Player p2) {
        TrocaSession session = new TrocaSession(plugin, p1, p2);
        activeSessions.put(p1.getUniqueId(), session);
        activeSessions.put(p2.getUniqueId(), session);
        session.open();
    }

    public TrocaSession getSession(Player p) {
        return activeSessions.get(p.getUniqueId());
    }

    public void endTrade(Player p1, Player p2) {
        activeSessions.remove(p1.getUniqueId());
        activeSessions.remove(p2.getUniqueId());
    }

    public void cancelAllTrades() {
        Set<TrocaSession> sessions = new HashSet<>(activeSessions.values());
        for (TrocaSession session : sessions) {
            session.cancel(plugin.message("cancel-server-stop"));
        }
        requestTasks.values().forEach(BukkitTask::cancel);
        requestTasks.clear();
        requests.clear();
        activeSessions.clear();
    }
}
