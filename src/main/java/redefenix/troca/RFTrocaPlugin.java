package redefenix.troca;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class RFTrocaPlugin extends JavaPlugin {

    private static RFTrocaPlugin instance;
    private TrocaManager trocaManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.trocaManager = new TrocaManager(this);

        getCommand("trocar").setExecutor(new TrocaCommand(this));
        getServer().getPluginManager().registerEvents(new TrocaListener(this), this);

        getLogger().info("RFTroca ativado.");
    }

    @Override
    public void onDisable() {
        if (trocaManager != null) {
            trocaManager.cancelAllTrades();
        }
    }

    public static RFTrocaPlugin getInstance() {
        return instance;
    }

    public TrocaManager getTrocaManager() {
        return trocaManager;
    }

    public int getCountdownSeconds() {
        return Math.max(1, getConfig().getInt("trade.countdown-seconds", 5));
    }

    public int getMinimumItems() {
        return Math.max(1, getConfig().getInt("trade.minimum-items", 1));
    }

    public int getRequestExpirationSeconds() {
        return Math.max(1, getConfig().getInt("trade.request-expiration-seconds", 60));
    }

    public String message(String path) {
        return color(getConfig().getString("messages." + path, ""));
    }

    public String message(String path, String... replacements) {
        String message = message(path);
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            message = message.replace(replacements[i], replacements[i + 1]);
        }
        return message;
    }

    public String color(String value) {
        return ChatColor.translateAlternateColorCodes('&', value);
    }
}
