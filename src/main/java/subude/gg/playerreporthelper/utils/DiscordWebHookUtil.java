package subude.gg.playerreporthelper.utils;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import subude.gg.playerreporthelper.PlayerReportHelper;

public class DiscordWebHookUtil {

    public static void send(List<String> messages) {
        FileConfiguration config = PlayerReportHelper.getInstance().getConfig();

        if (!config.getBoolean("webhook.enabled", false)) {
            return;
        }

        String urlString = config.getString("webhook.url", "");

        if (urlString.isEmpty()
                || urlString.equalsIgnoreCase("PASTE_YOUR_WEBHOOK")
                || (!urlString.startsWith("http://") && !urlString.startsWith("https://"))) {
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(PlayerReportHelper.getInstance(), () -> {
            try {
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                String username = config.getString("webhook.username", "PlayerReportHelper");
                String avatarUrl = config.getString("webhook.avatar-url", "");

                StringBuilder contentBuilder = new StringBuilder();
                for (int i = 0; i < messages.size(); i++) {
                    contentBuilder.append(messages.get(i));
                    if (i < messages.size() - 1) {
                        contentBuilder.append("\\n");
                    }
                }

                String jsonPayload = String.format(
                        "{\"username\": \"%s\", \"avatar_url\": \"%s\", \"content\": \"%s\"}",
                        escapeJson(username),
                        escapeJson(avatarUrl),
                        escapeJson(contentBuilder.toString())
                );

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                conn.getResponseCode();
                conn.disconnect();

            } catch (Exception e) {
                PlayerReportHelper.getInstance().getLogger().warning("Не удалось отправить Discord Webhook: " + e.getMessage());
            }
        });
    }

    private static String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}