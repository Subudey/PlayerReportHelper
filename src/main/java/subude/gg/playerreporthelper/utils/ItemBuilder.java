package subude.gg.playerreporthelper.utils;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import subude.gg.playerreporthelper.PlayerReportHelper;

import java.util.List;
import java.util.stream.Collectors;

public class ItemBuilder {
    public static ItemStack getItem(String path) {
        ConfigurationSection section = PlayerReportHelper.getInstance().getConfig().getConfigurationSection(path);
        if (section == null)
            return new ItemStack(Material.STONE);
        Material material = Material.valueOf(section.getString("material"));
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ColorUtil.colorize(section.getString("name")));
            List<String> lore = section.getStringList("lore").stream().map(ColorUtil::colorize).collect(Collectors.toList());
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}
