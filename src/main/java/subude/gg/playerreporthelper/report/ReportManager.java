package subude.gg.playerreporthelper.report;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import subude.gg.playerreporthelper.PlayerReportHelper;
import subude.gg.playerreporthelper.database.DBManager;
import subude.gg.playerreporthelper.utils.ColorUtil;
import subude.gg.playerreporthelper.utils.DiscordWebHookUtil;

public class ReportManager {
    private final DBManager database;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");

    public ReportManager(DBManager database) {
        this.database = database;
    }

    public void createReport(String reporter, String target, String reason) {
        Bukkit.getScheduler().runTaskAsynchronously(PlayerReportHelper.getInstance(), () -> {
            String date = dateFormat.format(new Date());
            String sql = "INSERT INTO reports (reporter, target, reason, date, status) VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement ps = this.database.getConnection().prepareStatement(sql)) {
                ps.setString(1, reporter);
                ps.setString(2, target);
                ps.setString(3, reason);
                ps.setString(4, date);
                ps.setString(5, "OPEN");
                ps.executeUpdate();

                List<String> webhookmsg = PlayerReportHelper.getInstance().getConfig().getStringList("webhook.messages.new-report");
                webhookmsg.replaceAll(line -> line
                        .replace("%reporter%", reporter)
                        .replace("%target%", target)
                        .replace("%reason%", reason)
                        .replace("%date%", date));
                DiscordWebHookUtil.send(webhookmsg);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void updateStatus(int id, String status, Player moderator, Runnable callback) {
        Bukkit.getScheduler().runTaskAsynchronously(PlayerReportHelper.getInstance(), () -> {
            try {
                String reporter = null;
                String target = null;
                String reason = null;
                String createdDate = null;

                String selectSql = "SELECT reporter, target, reason, date FROM reports WHERE id=?";
                try (PreparedStatement getReport = this.database.getConnection().prepareStatement(selectSql)) {
                    getReport.setInt(1, id);
                    try (ResultSet rs = getReport.executeQuery()) {
                        if (rs.next()) {
                            reporter = rs.getString("reporter");
                            target = rs.getString("target");
                            reason = rs.getString("reason");
                            createdDate = rs.getString("date");
                        }
                    }
                }

                if (reporter == null || createdDate == null) return;

                long createdTime = dateFormat.parse(createdDate).getTime();
                long responseTime = (System.currentTimeMillis() - createdTime) / 1000L;

                String updateSql = "UPDATE reports SET status=?, handled_by=?, handled_date=?, response_time=? WHERE id=?";
                try (PreparedStatement update = this.database.getConnection().prepareStatement(updateSql)) {
                    update.setString(1, status);
                    update.setString(2, moderator.getName());
                    update.setString(3, dateFormat.format(new Date()));
                    update.setLong(4, responseTime);
                    update.setInt(5, id);
                    update.executeUpdate();
                }

                updateModeratorStats(moderator.getName(), status, responseTime);
                if ("ACCEPTED".equals(status)) {
                    addReputation(reporter, 2);
                }

                List<String> msg = PlayerReportHelper.getInstance().getConfig().getStringList("webhook.messages.handled-report");
                msg.replaceAll(line -> line
                        .replace("%id%", String.valueOf(id))
                        .replace("%status%", status)
                        .replace("%moderator%", moderator.getName()));
                DiscordWebHookUtil.send(msg);

                String finalReporter = reporter;
                String finalTarget = target;
                String finalReason = reason;

                Bukkit.getScheduler().runTask(PlayerReportHelper.getInstance(), () -> {
                    FileConfiguration config = PlayerReportHelper.getInstance().getConfig();

                    if ("ACCEPTED".equals(status) && config.getBoolean("ban.enabled", true)) {
                        String banCmd = config.getString("ban.command", "ban %target% %reason%")
                                .replace("%target%", finalTarget)
                                .replace("%reason%", finalReason)
                                .replace("%moderator%", moderator.getName());

                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), banCmd);
                    }

                    Player targetReporter = Bukkit.getPlayerExact(finalReporter);
                    if (targetReporter != null && targetReporter.isOnline()) {
                        String messagePath = "ACCEPTED".equals(status)
                                ? "messages.report-accepted"
                                : "messages.report-denied";

                        String notification = config.getString(messagePath,
                                "ACCEPTED".equals(status)
                                        ? "&aВаш репорт на %target% был принят!"
                                        : "&cВаш репорт на %target% был отклонен.");

                        notification = notification
                                .replace("%target%", finalTarget)
                                .replace("%moderator%", moderator.getName())
                                .replace("%id%", String.valueOf(id));

                        targetReporter.sendMessage(ColorUtil.colorize(notification));
                    }

                    if (callback != null) {
                        callback.run();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void addReputation(String player, int amount) throws SQLException {
        String checkSql = "SELECT points FROM reputation WHERE player=?";
        try (PreparedStatement check = this.database.getConnection().prepareStatement(checkSql)) {
            check.setString(1, player);
            try (ResultSet rs = check.executeQuery()) {
                if (rs.next()) {
                    int current = rs.getInt("points");
                    String updateSql = "UPDATE reputation SET points=? WHERE player=?";
                    try (PreparedStatement update = this.database.getConnection().prepareStatement(updateSql)) {
                        update.setInt(1, current + amount);
                        update.setString(2, player);
                        update.executeUpdate();
                    }
                } else {
                    String insertSql = "INSERT INTO reputation (player, points) VALUES (?, ?)";
                    try (PreparedStatement insert = this.database.getConnection().prepareStatement(insertSql)) {
                        insert.setString(1, player);
                        insert.setInt(2, amount);
                        insert.executeUpdate();
                    }
                }
            }
        }
    }

    private void updateModeratorStats(String moderator, String status, long responseTime) throws SQLException {
        String checkSql = "SELECT * FROM moderator_stats WHERE moderator=?";
        try (PreparedStatement check = this.database.getConnection().prepareStatement(checkSql)) {
            check.setString(1, moderator);
            try (ResultSet rs = check.executeQuery()) {
                if (rs.next()) {
                    int accepted = rs.getInt("accepted");
                    int denied = rs.getInt("denied");
                    int total = rs.getInt("total_response_time");

                    if ("ACCEPTED".equals(status)) {
                        accepted++;
                    } else {
                        denied++;
                    }

                    String updateSql = "UPDATE moderator_stats SET accepted=?, denied=?, total_response_time=? WHERE moderator=?";
                    try (PreparedStatement update = this.database.getConnection().prepareStatement(updateSql)) {
                        update.setInt(1, accepted);
                        update.setInt(2, denied);
                        update.setInt(3, total + (int) responseTime);
                        update.setString(4, moderator);
                        update.executeUpdate();
                    }
                } else {
                    String insertSql = "INSERT INTO moderator_stats VALUES (?,?,?,?)";
                    try (PreparedStatement insert = this.database.getConnection().prepareStatement(insertSql)) {
                        insert.setString(1, moderator);
                        insert.setInt(2, "ACCEPTED".equals(status) ? 1 : 0);
                        insert.setInt(3, "DENIED".equals(status) ? 1 : 0);
                        insert.setInt(4, (int) responseTime);
                        insert.executeUpdate();
                    }
                }
            }
        }
    }

    public void cleanupOldReports() {
        Bukkit.getScheduler().runTaskAsynchronously(PlayerReportHelper.getInstance(), () -> {
            int days = PlayerReportHelper.getInstance().getConfig().getInt("reports-cleanup-days", 7);

            String sql = "DELETE FROM reports WHERE status != 'OPEN' AND handled_date IS NOT NULL AND " +
                    "(julianday('now') - julianday(substr(handled_date, 7, 4) || '-' || " +
                    "substr(handled_date, 4, 2) || '-' || substr(handled_date, 1, 2))) >= ?";

            try (PreparedStatement ps = this.database.getConnection().prepareStatement(sql)) {
                ps.setInt(1, days);
                int deleted = ps.executeUpdate();

                if (deleted > 0) {
                    PlayerReportHelper.getInstance().getLogger().info("[ReportHelper] Авто-очистка: удалено " + deleted + " старых репортов.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public int getReputation(String player) {
        String sql = "SELECT points FROM reputation WHERE player=?";
        try (PreparedStatement ps = this.database.getConnection().prepareStatement(sql)) {
            ps.setString(1, player);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("points");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public ModerStats getModeratorStats(String moderator) {
        String sql = "SELECT * FROM moderator_stats WHERE moderator=?";
        try (PreparedStatement ps = this.database.getConnection().prepareStatement(sql)) {
            ps.setString(1, moderator);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int accepted = rs.getInt("accepted");
                    int denied = rs.getInt("denied");
                    int totalResponseTime = rs.getInt("total_response_time");
                    int totalReports = accepted + denied;
                    long averageResponse = (totalReports > 0) ? (totalResponseTime / totalReports) : 0L;
                    return new ModerStats(moderator, accepted, denied, averageResponse);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Report> getReports(String status, int limit, int offset) {
        List<Report> list = new ArrayList<>();
        String sql = "SELECT r.* FROM reports r LEFT JOIN reputation rep ON r.reporter = rep.player " +
                ("OPEN".equals(status) ? "WHERE r.status='OPEN' " : "WHERE r.status!='OPEN' ") +
                "ORDER BY COALESCE(rep.points,0) DESC, r.id DESC LIMIT ? OFFSET ?";

        try (PreparedStatement ps = this.database.getConnection().prepareStatement(sql)) {
            ps.setInt(1, limit);
            ps.setInt(2, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Report(
                            rs.getInt("id"),
                            rs.getString("reporter"),
                            rs.getString("target"),
                            rs.getString("reason"),
                            rs.getString("date"),
                            rs.getString("status"),
                            rs.getString("handled_by"),
                            rs.getString("handled_date"),
                            rs.getLong("response_time")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public int countReports(String status) {
        String sql = "OPEN".equals(status) ?
                "SELECT COUNT(*) FROM reports WHERE status='OPEN'" :
                "SELECT COUNT(*) FROM reports WHERE status!='OPEN'";

        try (PreparedStatement ps = this.database.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}