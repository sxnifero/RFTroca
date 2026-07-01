package redefenix.troca;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class TrocaListener implements Listener {

    private final RFTrocaPlugin plugin;

    public TrocaListener(RFTrocaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        TrocaSession session = plugin.getTrocaManager().getSession(p);
        if (session == null || !e.getView().getTopInventory().equals(session.inv)) return;

        boolean isP1 = p.equals(session.p1);

        if (e.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
            e.setCancelled(true);
            return;
        }

        if (e.getClickedInventory() == e.getView().getBottomInventory()) {
            if (e.isShiftClick()) {
                e.setCancelled(true);
                ItemStack clicked = e.getCurrentItem();
                if (clicked == null || clicked.getType().isAir()) return;

                int[] mySlots = isP1 ? TrocaSession.P1_SLOTS : TrocaSession.P2_SLOTS;

                for (int slot : mySlots) {
                    if (clicked.getAmount() <= 0) break;
                    ItemStack inSlot = session.inv.getItem(slot);

                    if (inSlot == null || inSlot.getType().isAir()) {
                        session.inv.setItem(slot, clicked.clone());
                        clicked.setAmount(0);
                    } else if (inSlot.isSimilar(clicked)) {
                        int space = inSlot.getMaxStackSize() - inSlot.getAmount();
                        if (space > 0) {
                            int transfer = Math.min(space, clicked.getAmount());
                            inSlot.setAmount(inSlot.getAmount() + transfer);
                            clicked.setAmount(clicked.getAmount() - transfer);
                        }
                    }
                }

                e.setCurrentItem(clicked.getAmount() > 0 ? clicked : null);
                session.resetAccepts();
            }
            return;
        }

        if (e.getClickedInventory() == e.getView().getTopInventory()) {
            int slot = e.getSlot();

            if (slot == TrocaSession.P1_WOOL && isP1) { e.setCancelled(true); session.toggleAccept(p); return; }
            if (slot == TrocaSession.P2_WOOL && !isP1) { e.setCancelled(true); session.toggleAccept(p); return; }

            if (isP1) {
                if (TrocaSession.contains(TrocaSession.P1_SLOTS, slot)) {
                    session.resetAccepts();
                } else {
                    e.setCancelled(true);
                }
            } else {
                if (TrocaSession.contains(TrocaSession.P2_SLOTS, slot)) {
                    session.resetAccepts();
                } else {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        TrocaSession session = plugin.getTrocaManager().getSession(p);
        if (session == null || !e.getView().getTopInventory().equals(session.inv)) return;

        boolean isP1 = p.equals(session.p1);
        int[] mySlots = isP1 ? TrocaSession.P1_SLOTS : TrocaSession.P2_SLOTS;

        for (int slot : e.getRawSlots()) {
            if (slot < e.getView().getTopInventory().getSize()) {
                if (!TrocaSession.contains(mySlots, slot)) {
                    e.setCancelled(true);
                    return;
                }
            }
        }
        session.resetAccepts();
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player p)) return;
        TrocaSession session = plugin.getTrocaManager().getSession(p);
        if (session != null && e.getView().getTopInventory().equals(session.inv)) {
            session.cancel(plugin.message("cancel-menu-closed"));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        TrocaSession session = plugin.getTrocaManager().getSession(e.getPlayer());
        if (session != null) {
            session.cancel(plugin.message("cancel-player-quit"));
        }
    }
}
