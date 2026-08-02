package subude.gg.playerreporthelper.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import subude.gg.playerreporthelper.PlayerReportHelper;
import subude.gg.playerreporthelper.utils.ColorUtil;
import subude.gg.playerreporthelper.utils.ItemBuilder;

import java.util.List;

public class ReputationGui implements Listener {
    public static void open(Player viewer, String target, int reputation) {
        FileConfiguration config = PlayerReportHelper.getInstance().getConfig();
        String title = ColorUtil.colorize(config
                .getString("menus.reputation.title"));
        int size = config.getInt("menus.reputation.size");
        Inventory inv = Bukkit.createInventory(null, size, title);
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta)head.getItemMeta();
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(target));
        String name = config.getString("items.reputation-item.name").replace("%player%", target);
        meta.setDisplayName(ColorUtil.colorize(name));
        List<String> lore = config.getStringList("items.reputation-item.lore");
        lore.replaceAll(line -> ColorUtil.colorize(line.replace("%points%", String.valueOf(reputation))));
        meta.setLore(lore);
        head.setItemMeta(meta);
        int slot = config.getInt("items.reputation-item.slot");
        inv.setItem(slot, head);
        ItemStack filler = ItemBuilder.getItem("items.filler");
        for (int i = 0; i < size; i++) {
            if (inv.getItem(i) == null)
                inv.setItem(i, filler);
        }
        viewer.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        String title = e.getView().getTitle();
        if (!title.equals(ColorUtil.colorize(
                PlayerReportHelper.getInstance().getConfig().getString("menus.reputation.title"))))
            return;
        e.setCancelled(true);
    }
}
