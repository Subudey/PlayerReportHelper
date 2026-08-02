package subude.gg.playerreporthelper.commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import subude.gg.playerreporthelper.PlayerReportHelper;
import subude.gg.playerreporthelper.report.ReportManager;
import subude.gg.playerreporthelper.utils.ColorUtil;

public class ReportCommand implements CommandExecutor {
    private final ReportManager manager;

    private final HashMap<UUID, Long> cooldown = new HashMap<>();

    public ReportCommand(ReportManager manager) {
        this.manager = manager;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player;
        if (sender instanceof Player) {
            player = (Player)sender;
        } else {
            return true;
        }
        FileConfiguration config = PlayerReportHelper.getInstance().getConfig();
        if (args.length < 2) {
            player.sendMessage(ColorUtil.colorize(config.getString("messages.usage")));
            return true;
        }
        long now = System.currentTimeMillis();
        if (this.cooldown.containsKey(player.getUniqueId())) {
            long last = ((Long)this.cooldown.get(player.getUniqueId())).longValue();
            int cooldownSeconds = config.getInt("cooldown-seconds");
            if (now - last < (cooldownSeconds * 1000)) {
                long seconds = cooldownSeconds - (now - last) / 1000L;
                String msg = config.getString("messages.cooldown").replace("%seconds%", String.valueOf(seconds));
                player.sendMessage(ColorUtil.colorize(msg));
                return true;
            }
        }
        this.cooldown.put(player.getUniqueId(), Long.valueOf(now));
        String target = args[0];
        String reason = String.join(" ", Arrays.<CharSequence>copyOfRange((CharSequence[])args, 1, args.length));
        if (target.equalsIgnoreCase(player.getName())) {
            player.sendMessage(ColorUtil.colorize(config
                    .getString("messages.cannot-report-yourself")));
            return true;
        }
        if (config.getStringList("blacklist").contains(target)) {
            player.sendMessage("&cЭто игрока нельзя зарепортить.");
            return true;
        }
        this.manager.createReport(player.getName(), target, reason);
        player.sendMessage(ColorUtil.colorize(config.getString("messages.report-sent")));
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
        return true;
    }
}
