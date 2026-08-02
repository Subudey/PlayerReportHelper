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
import subude.gg.playerreporthelper.report.ModerStats;
import subude.gg.playerreporthelper.utils.ColorUtil;
import subude.gg.playerreporthelper.utils.ItemBuilder;

import java.util.List;

public class ModerGui implements Listener {
    public static void open(Player player, ModerStats stats) {
        FileConfiguration config = PlayerReportHelper.getInstance().getConfig();
        String title = ColorUtil.colorize(config.getString("menus.stats.title"));
        int size = config.getInt("menus.stats.size");
        Inventory inv = Bukkit.createInventory(null, size, title);
        ItemStack filler = ItemBuilder.getItem("items.filler");
        for (int i = 0; i < size; i++)
            inv.setItem(i, filler);
        ItemStack head = ItemBuilder.getItem("items.stats-item");
        ItemMeta meta = head.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ColorUtil.colorize(meta
                    .getDisplayName()
                    .replace("%player%", stats.getName())));
            List<String> lore = meta.getLore();
            for (int j = 0; j < lore.size(); j++)
                lore.set(j, ColorUtil.colorize(((String)lore
                        .get(j))
                        .replace("%accepted%", String.valueOf(stats.getAccepted()))
                        .replace("%denied%", String.valueOf(stats.getDenied()))
                        .replace("%time%", String.valueOf(stats.getAverageResponse()))));
            meta.setLore(lore);
            if (head.getType() == Material.PLAYER_HEAD) {
                SkullMeta skull = (SkullMeta)meta;
                skull.setOwningPlayer(Bukkit.getOfflinePlayer(stats.getName()));
                head.setItemMeta((ItemMeta)skull);
            } else {
                head.setItemMeta(meta);
            }
        }
        int slot = config.getInt("items.stats-item.slot");
        inv.setItem(slot, head);
        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        String title = ColorUtil.colorize(
                PlayerReportHelper.getInstance().getConfig().getString("menus.stats.title"));
        if (e.getView().getTitle().equals(title))
            e.setCancelled(true);
    }
}
