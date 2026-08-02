package subude.gg.playerreporthelper.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import subude.gg.playerreporthelper.PlayerReportHelper;
import subude.gg.playerreporthelper.gui.ReportsGui;
import subude.gg.playerreporthelper.report.ReportManager;
import subude.gg.playerreporthelper.utils.ColorUtil;

public class InventoryListener implements Listener {
    private final ReportManager manager;

    public InventoryListener(ReportManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        HumanEntity humanEntity = e.getWhoClicked();
        if (!(humanEntity instanceof Player)) {
            return;
        }
        Player player = (Player) humanEntity;

        if (e.getClickedInventory() == null || e.getClickedInventory() != e.getView().getTopInventory()) {
            return;
        }

        FileConfiguration config = PlayerReportHelper.getInstance().getConfig();
        String title = e.getView().getTitle();
        String openTitle = ColorUtil.colorize(config.getString("menus.reports-open.title").split("%page%")[0]);
        String closedTitle = ColorUtil.colorize(config.getString("menus.reports-closed.title").split("%page%")[0]);

        if (!title.startsWith(openTitle) && !title.startsWith(closedTitle)) {
            return;
        }
        e.setCancelled(true);

        ItemStack item = e.getCurrentItem();
        if (item == null || !item.hasItemMeta()) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (!meta.hasDisplayName()) {
            return;
        }

        boolean closed = title.startsWith(closedTitle);
        int pageTemp = 0;
        try {
            String cleanTitle = ChatColor.stripColor(title);
            if (cleanTitle.contains("Страница ")) {
                pageTemp = Integer.parseInt(cleanTitle.split("Страница ")[1].trim()) - 1;
            }
        } catch (Exception ignored) {}
        int page = pageTemp;

        if (config.getBoolean("sounds.enabled") && item.getType().toString().contains("GLASS")) {
            try {
                Sound sound = Sound.valueOf(config.getString("sounds.click-glass"));
                player.playSound(player.getLocation(), sound, 1.0F, 1.0F);
            } catch (Exception ignored) {}
            return;
        }

        String name = meta.getDisplayName();
        String nextName = ColorUtil.colorize(config.getString("items.next-page.name"));
        String prevName = ColorUtil.colorize(config.getString("items.previous-page.name"));

        if (name.equals(nextName)) {
            ReportsGui.open(player, this.manager, page + 1, closed);
            return;
        }
        if (name.equals(prevName)) {
            ReportsGui.open(player, this.manager, page - 1, closed);
            return;
        }

        if (closed || !meta.hasLore()) {
            return;
        }

        int id = -1;
        for (String line : meta.getLore()) {
            String stripped = ChatColor.stripColor(line);
            if (stripped.contains("ID:")) {
                try {
                    String idPart = stripped.split("ID:")[1].trim();
                    id = Integer.parseInt(idPart.replaceAll("[^0-9]", ""));
                } catch (Exception ignored) {}
                break;
            }
        }

        if (id == -1) return;

        if (e.isShiftClick() && e.isLeftClick()) {
            String rawTarget = ChatColor.stripColor(meta.getDisplayName());
            if (rawTarget.contains("Репорт на ")) {
                rawTarget = rawTarget.replace("Репорт на ", "");
            }

            Player targetPlayer = Bukkit.getPlayerExact(rawTarget.trim());
            if (targetPlayer != null && targetPlayer.isOnline()) {
                player.teleport(targetPlayer.getLocation());
                player.sendMessage(ColorUtil.colorize(config.getString("messages.teleported")
                        .replace("%player%", targetPlayer.getName())));
            } else {
                player.sendMessage(ColorUtil.colorize(config.getString("messages.player-offline")));
            }
            return;
        }

        if (e.isLeftClick()) {
            this.manager.updateStatus(id, "ACCEPTED", player, () -> {
                ReportsGui.open(player, this.manager, page, false);
            });

            if (config.getBoolean("sounds.enabled")) {
                try {
                    Sound sound = Sound.valueOf(config.getString("sounds.accept"));
                    player.playSound(player.getLocation(), sound, 1.0F, 1.0F);
                } catch (Exception ignored) {}
            }
        }
        else if (e.isRightClick()) {
            this.manager.updateStatus(id, "DENIED", player, () -> {
                ReportsGui.open(player, this.manager, page, false);
            });

            if (config.getBoolean("sounds.enabled")) {
                try {
                    Sound sound = Sound.valueOf(config.getString("sounds.deny"));
                    player.playSound(player.getLocation(), sound, 1.0F, 1.0F);
                } catch (Exception ignored) {}
            }
        }
    }
}