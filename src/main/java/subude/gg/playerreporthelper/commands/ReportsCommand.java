package subude.gg.playerreporthelper.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import subude.gg.playerreporthelper.PlayerReportHelper;
import subude.gg.playerreporthelper.gui.ModerGui;
import subude.gg.playerreporthelper.gui.ReportsGui;
import subude.gg.playerreporthelper.gui.ReputationGui;
import subude.gg.playerreporthelper.report.ModerStats;
import subude.gg.playerreporthelper.report.ReportManager;
import subude.gg.playerreporthelper.utils.ColorUtil;

public class ReportsCommand implements CommandExecutor {
    private final ReportManager manager;

    public ReportsCommand(ReportManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        Player player = (Player) sender;

        if (!player.hasPermission("playerreporthelper.permission")) {
            player.sendMessage(ColorUtil.colorize(
                    PlayerReportHelper.getInstance().getConfig().getString("messages.no-permission")));
            return true;
        }

        if (args.length == 0) {
            ReportsGui.open(player, this.manager, 0, false);
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
            player.sendMessage(ColorUtil.colorize("&e/reports list &7- Список закрытых репортов"));
            player.sendMessage(ColorUtil.colorize("&e/reports reputation <player> &7- Репутация игрока"));
            player.sendMessage(ColorUtil.colorize("&e/reports stats <moderator> &7- Статистика модератора"));
            player.sendMessage(ColorUtil.colorize("&e/reports reload &7- Перезагрузка конфига"));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!player.hasPermission("playerreporthelper.reload")) {
                player.sendMessage(ColorUtil.colorize(
                        PlayerReportHelper.getInstance().getConfig().getString("messages.no-permission")));
                return true;
            }
            PlayerReportHelper.getInstance().reloadConfig();
            player.sendMessage(ColorUtil.colorize("&aКонфигурация успешно перезагружена!"));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
            ReportsGui.open(player, this.manager, 0, true);
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("reputation")) {
            int rep = this.manager.getReputation(args[1]);
            ReputationGui.open(player, args[1], rep);
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("stats")) {
            if (!player.hasPermission("playerreporthelper.statsmoderator")) {
                player.sendMessage(ColorUtil.colorize(
                        PlayerReportHelper.getInstance().getConfig().getString("messages.no-permission")));
                return true;
            }
            ModerStats stats = this.manager.getModeratorStats(args[1]);
            if (stats == null) {
                stats = new ModerStats(args[1], 0, 0, 0L);
            }
            ModerGui.open(player, stats);
            return true;
        }

        return true;
    }
}