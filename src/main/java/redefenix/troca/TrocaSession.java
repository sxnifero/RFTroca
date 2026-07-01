package redefenix.troca;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class TrocaSession {

    private final RFTrocaPlugin plugin;
    public final Player p1;
    public final Player p2;
    public final Inventory inv;

    public boolean p1Accepted = false;
    public boolean p2Accepted = false;
    private boolean finished = false;
    private BukkitTask countdownTask = null;

    public static final int[] P1_SLOTS = {10, 11, 12, 19, 20, 21, 28, 29, 30};
    public static final int[] P2_SLOTS = {14, 15, 16, 23, 24, 25, 32, 33, 34};

    public static final int P1_WOOL = 38;
    public static final int P2_WOOL = 42;

    public TrocaSession(RFTrocaPlugin plugin, Player p1, Player p2) {
        this.plugin = plugin;
        this.p1 = p1;
        this.p2 = p2;
        this.inv = Bukkit.createInventory(null, 54, plugin.message("inventory-title", "{player1}", p1.getName(), "{player2}", p2.getName()));
        buildGUI();
    }

    private void buildGUI() {
        ItemStack glass = createItem(Material.BLACK_STAINED_GLASS_PANE, plugin.color("&r"));
        ItemStack line = createItem(Material.BLUE_STAINED_GLASS_PANE, plugin.color("&r"));

        for (int i = 0; i < 54; i++) {
            if (i % 9 == 4) inv.setItem(i, line);
            else if (i == P1_WOOL || i == P2_WOOL) continue;
            else if (!contains(P1_SLOTS, i) && !contains(P2_SLOTS, i)) inv.setItem(i, glass);
        }
        updateWools();
    }

    public void open() {
        p1.openInventory(inv);
        p2.openInventory(inv);
    }

    public void updateWools() {
        inv.setItem(P1_WOOL, createItem(p1Accepted ? Material.LIME_WOOL : Material.RED_WOOL, woolName(p1, p1Accepted)));
        inv.setItem(P2_WOOL, createItem(p2Accepted ? Material.LIME_WOOL : Material.RED_WOOL, woolName(p2, p2Accepted)));
    }

    public void toggleAccept(Player p) {
        if (p.equals(p1)) {
            if (!p1Accepted && countItems(P1_SLOTS) < plugin.getMinimumItems()) {
                p.sendMessage(plugin.message("minimum-items", "{amount}", String.valueOf(plugin.getMinimumItems())));
                return;
            }
            p1Accepted = !p1Accepted;
        } else if (p.equals(p2)) {
            if (!p2Accepted && countItems(P2_SLOTS) < plugin.getMinimumItems()) {
                p.sendMessage(plugin.message("minimum-items", "{amount}", String.valueOf(plugin.getMinimumItems())));
                return;
            }
            p2Accepted = !p2Accepted;
        }

        updateWools();
        checkReady();
    }

    private int countItems(int[] slots) {
        int amount = 0;
        for (int slot : slots) {
            ItemStack i = inv.getItem(slot);
            if (i != null && !i.getType().isAir()) amount += i.getAmount();
        }
        return amount;
    }

    public void resetAccepts() {
        p1Accepted = false;
        p2Accepted = false;
        updateWools();
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
            p1.sendMessage(plugin.message("offer-changed"));
            p2.sendMessage(plugin.message("offer-changed"));
        }
    }

    private void checkReady() {
        if (p1Accepted && p2Accepted) {
            countdownTask = new BukkitRunnable() {
                int time = plugin.getCountdownSeconds();
                @Override
                public void run() {
                    if (time <= 0) {
                        finishTrade();
                        cancel();
                        return;
                    }
                    String message = plugin.message("countdown", "{seconds}", String.valueOf(time));
                    p1.sendMessage(message);
                    p2.sendMessage(message);
                    p1.playSound(p1.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
                    p2.playSound(p2.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
                    time--;
                }
            }.runTaskTimer(plugin, 0, 20L);
        } else if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
            p1.sendMessage(plugin.message("countdown-cancelled"));
            p2.sendMessage(plugin.message("countdown-cancelled"));
        }
    }

    private void finishTrade() {
        finished = true;
        List<ItemStack> itemsP1 = getItemsAndClear(P1_SLOTS);
        List<ItemStack> itemsP2 = getItemsAndClear(P2_SLOTS);

        for (ItemStack item : itemsP2) giveItem(p1, item);
        for (ItemStack item : itemsP1) giveItem(p2, item);

        p1.closeInventory();
        p2.closeInventory();

        p1.playSound(p1.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
        p2.playSound(p2.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
        p1.sendMessage(plugin.message("trade-completed"));
        p2.sendMessage(plugin.message("trade-completed"));

        plugin.getTrocaManager().endTrade(p1, p2);
    }

    public void cancel(String reason) {
        if (finished) return;
        finished = true;

        if (countdownTask != null) countdownTask.cancel();

        List<ItemStack> itemsP1 = getItemsAndClear(P1_SLOTS);
        List<ItemStack> itemsP2 = getItemsAndClear(P2_SLOTS);

        for (ItemStack item : itemsP1) giveItem(p1, item);
        for (ItemStack item : itemsP2) giveItem(p2, item);

        if (p1.getOpenInventory().getTopInventory().equals(inv)) p1.closeInventory();
        if (p2.getOpenInventory().getTopInventory().equals(inv)) p2.closeInventory();

        p1.sendMessage(plugin.message("trade-cancelled", "{reason}", reason));
        p2.sendMessage(plugin.message("trade-cancelled", "{reason}", reason));

        plugin.getTrocaManager().endTrade(p1, p2);
    }

    private List<ItemStack> getItemsAndClear(int[] slots) {
        List<ItemStack> list = new ArrayList<>();
        for (int slot : slots) {
            ItemStack item = inv.getItem(slot);
            if (item != null && !item.getType().isAir()) {
                list.add(item.clone());
                inv.setItem(slot, null);
            }
        }
        return list;
    }

    private void giveItem(Player p, ItemStack item) {
        if (!p.getInventory().addItem(item).isEmpty()) {
            p.getWorld().dropItemNaturally(p.getLocation(), item);
            p.sendMessage(plugin.message("inventory-full"));
        }
    }

    private ItemStack createItem(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    public static boolean contains(int[] array, int val) {
        for (int i : array) if (i == val) return true;
        return false;
    }

    private String woolName(Player player, boolean accepted) {
        return plugin.message(accepted ? "status-accepted" : "status-waiting", "{player}", player.getName());
    }
}
