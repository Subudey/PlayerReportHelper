package subude.gg.playerreporthelper.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import subude.gg.playerreporthelper.PlayerReportHelper;
import subude.gg.playerreporthelper.report.Report;
import subude.gg.playerreporthelper.report.ReportManager;
import subude.gg.playerreporthelper.utils.ColorUtil;
import subude.gg.playerreporthelper.utils.ItemBuilder;

import java.util.ArrayList;
import java.util.List;

public class ReportsGui {
    private static final int[] GLASS_SLOTS = new int[] {
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
            17, 18, 26, 27, 35, 36, 44, 45, 46, 47,
            51, 52, 53 };

    public static void open(Player player, ReportManager manager, int page, boolean closed) {
        FileConfiguration config = PlayerReportHelper.getInstance().getConfig();
        String path = closed ? "menus.reports-closed.title" : "menus.reports-open.title";
        String title = config.getString(path, "&8Репорты").replace("%page%", String.valueOf(page + 1));

        String status = closed ? "CLOSED" : "OPEN";

        Bukkit.getScheduler().runTaskAsynchronously(PlayerReportHelper.getInstance(), () -> {
            List<Report> reports = manager.getReports(status, 45, page * 45);
            int total = manager.countReports(status);

            Bukkit.getScheduler().runTask(PlayerReportHelper.getInstance(), () -> {
                if (!player.isOnline()) return;

                Inventory inv = Bukkit.createInventory(null, 54, ColorUtil.colorize(title));
                ItemStack filler = ItemBuilder.getItem("items.filler");
                for (int i : GLASS_SLOTS) {
                    inv.setItem(i, filler);
                }

                int slot = 0;
                for (Report report : reports) {
                    while (isGlassSlot(slot)) {
                        slot++;
                    }
                    if (slot >= 54) break;

                    ItemStack item = createReportItem(report, closed);
                    inv.setItem(slot, item);
                    slot++;
                }

                int maxPages = (int) Math.ceil(total / 45.0D);
                if (page + 1 < maxPages) {
                    ItemStack next = ItemBuilder.getItem("items.next-page");
                    int nextSlot = config.getInt("items.next-page.slot", 50);
                    inv.setItem(nextSlot, next);
                }
                if (page > 0) {
                    ItemStack prev = ItemBuilder.getItem("items.previous-page");
                    int prevSlot = config.getInt("items.previous-page.slot", 48);
                    inv.setItem(prevSlot, prev);
                }

                player.openInventory(inv);
            });
        });
    }

    public static ItemStack createReportItem(Report report, boolean closed) {
        ItemStack item = ItemBuilder.getItem(closed ? "items.report-item-closed" : "items.report-item-open");
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName(ColorUtil.colorize(meta.getDisplayName().replace("%target%", report.getTarget())));

        String formattedStatus;
        if (report.getStatus() != null) {
            switch (report.getStatus().toUpperCase()) {
                case "ACCEPTED":
                    formattedStatus = "&aПринят";
                    break;
                case "DENIED":
                    formattedStatus = "&cОтклонен";
                    break;
                case "OPEN":
                    formattedStatus = "&eОткрыт";
                    break;
                default:
                    formattedStatus = "&7" + report.getStatus();
                    break;
            }
        } else {
            formattedStatus = "&7Неизвестно";
        }

        List<String> lore = meta.getLore();
        if (lore != null) {
            List<String> newLore = new ArrayList<>();
            for (String line : lore) {
                newLore.add(ColorUtil.colorize(line
                        .replace("%reason%", report.getReason() != null ? report.getReason() : "")
                        .replace("%reporter%", report.getReporter() != null ? report.getReporter() : "")
                        .replace("%date%", report.getDate() != null ? report.getDate() : "")
                        .replace("%id%", String.valueOf(report.getId()))
                        .replace("%status%", formattedStatus)
                        .replace("%moderator%", report.getHandledBy() != null ? report.getHandledBy() : "—")
                        .replace("%handled_date%", report.getHandledDate() != null ? report.getHandledDate() : "—")
                        .replace("%response_time%", String.valueOf(report.getResponseTime()))
                ));
            }
            meta.setLore(newLore);
        }

        if (item.getType() == Material.PLAYER_HEAD) {
            SkullMeta skull = (SkullMeta) meta;
            OfflinePlayer target = Bukkit.getOfflinePlayer(report.getTarget());
            skull.setOwningPlayer(target);
            item.setItemMeta(skull);
        } else {
            item.setItemMeta(meta);
        }

        return item;
    }

    private static boolean isGlassSlot(int slot) {
        for (int glass : GLASS_SLOTS) {
            if (glass == slot) return true;
        }
        return false;
    }
}