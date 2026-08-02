package subude.gg.playerreporthelper;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import subude.gg.playerreporthelper.commands.ReportCommand;
import subude.gg.playerreporthelper.commands.ReportsCommand;
import subude.gg.playerreporthelper.commands.ReportsTab;
import subude.gg.playerreporthelper.database.DBManager;
import subude.gg.playerreporthelper.gui.ModerGui;
import subude.gg.playerreporthelper.gui.ReputationGui;
import subude.gg.playerreporthelper.listeners.InventoryListener;
import subude.gg.playerreporthelper.report.ReportManager;

public final class PlayerReportHelper extends JavaPlugin {
    private static PlayerReportHelper instance;

    private DBManager databaseManager;

    private ReportManager reportManager;


    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        this.databaseManager = new DBManager(getDataFolder());
        this.databaseManager.connect();
        this.reportManager = new ReportManager(this.databaseManager);
        this.reportManager.cleanupOldReports();
        getCommand("report").setExecutor(new ReportCommand(this.reportManager));
        getCommand("reports").setExecutor(new ReportsCommand(this.reportManager));
        getCommand("reports").setTabCompleter(new ReportsTab());
        getServer().getPluginManager().registerEvents(new InventoryListener(this.reportManager), this);
        getServer().getPluginManager().registerEvents(new ModerGui(), this);
        getServer().getPluginManager().registerEvents(new ReputationGui(), this);

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            if (this.reportManager != null) {
                this.reportManager.cleanupOldReports();
            }
        }, 1200L, 1728000L);
    }

    @Override
    public void onDisable() {
        getServer().getLogger().info("PlayerReportHelper has disable!");
    }

    public static PlayerReportHelper getInstance() {
        return instance;
    }
}
