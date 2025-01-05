package com.fread.cloverOpProtector;

import org.bukkit.plugin.java.JavaPlugin;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Base64;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;

public class AdminProtectionPlugin extends JavaPlugin {

    // Зашифрованный ключ
    private static final String ENCRYPTED_KEY = "cWlMNmFNZ2wldHhoT0tuVmhnZCM="; // "qiL6aMgl%txhOKnVhgd#"
    private static final String GITHUB_API_URL = "https://api.github.com/repos/freadc0de/CloverOpProtect/releases/latest"; // Замените {username} и {repository}

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // Проверяем введённый пользователем ключ
        String providedKey = getConfig().getString("activation-key", "");

        if (!isKeyValid(providedKey)) {
            getLogger().severe("The activation key is invalid or missing! Plugin will be disabled.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("Activation key is valid. Plugin is starting...");

        // Проверяем обновления
        checkForUpdates();

        // Инициализация менеджера логина и обработчиков событий
        LoginManager loginManager = new LoginManager(this);

        getServer().getPluginManager().registerEvents(new PlayerEventHandler(this, loginManager), this);
        getServer().getPluginManager().registerEvents(new ChatHandler(this, loginManager), this);

        if (getCommand("pass") != null) {
            getCommand("pass").setExecutor(new LoginCommandExecutor(this, loginManager));
        } else {
            getLogger().severe("Command 'pass' is not defined in plugin.yml!");
        }

        getLogger().info("AdminProtection enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("AdminProtection disabled.");
    }

    public String getAdminPassword() {
        return getConfig().getString("admin-password", "defaultPassword");
    }

    public String getBossBarMessage() {
        String rawMessage = String.join("\n", getConfig().getStringList("messages.boss-bar-message"));
        return translateHexColorCodes(rawMessage);
    }

    public String getMessage(String key) {
        return translateHexColorCodes(String.join("\n", getConfig().getStringList("messages." + key)));
    }

    private boolean isKeyValid(String key) {
        String decryptedKey = decryptKey(ENCRYPTED_KEY);
        return decryptedKey.equals(key); // Сравнение расшифрованного ключа с введённым
    }

    private String decryptKey(String encryptedKey) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedKey);
            return new String(decodedBytes);
        } catch (Exception e) {
            getLogger().severe("Failed to decrypt the activation key: " + e.getMessage());
            return "";
        }
    }

    private void checkForUpdates() {
        try {
            // Запрос к GitHub API
            URL url = new URL(GITHUB_API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            if (connection.getResponseCode() == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Обрабатываем JSON
                String jsonResponse = response.toString();
                String latestVersion = parseVersionFromJson(jsonResponse);
                String currentVersion = getDescription().getVersion();

                if (!currentVersion.equalsIgnoreCase(latestVersion)) {
                    getLogger().warning("A new version of the plugin is available! Please update to the latest version: " + latestVersion);
                    getLogger().warning("Download here: https://github.com/freadc0de/CloverOpProtect/releases");
                } else {
                    getLogger().info("You are using the latest version of the plugin.");
                }
            } else {
                getLogger().severe("Failed to check for updates. HTTP response code: " + connection.getResponseCode());
            }
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error while checking for updates: ", e);
        }
    }

    private String parseVersionFromJson(String jsonResponse) {
        // Простой парсер для извлечения "tag_name" из JSON ответа
        int startIndex = jsonResponse.indexOf("\"tag_name\":\"") + 12;
        int endIndex = jsonResponse.indexOf("\"", startIndex);
        return jsonResponse.substring(startIndex, endIndex);
    }

    public static String translateHexColorCodes(String message) {
        if (message == null) {
            return null;
        }

        Pattern hexPattern = Pattern.compile("(?i)&#([A-Fa-f0-9]{6})");
        Matcher matcher = hexPattern.matcher(message);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder("§x");
            for (char c : hex.toCharArray()) {
                replacement.append("§").append(c);
            }
            matcher.appendReplacement(buffer, replacement.toString());
        }
        matcher.appendTail(buffer);

        return buffer.toString();
    }
}