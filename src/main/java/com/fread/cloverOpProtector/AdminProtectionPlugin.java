package com.fread.cloverOpProtector;

import org.bukkit.plugin.java.JavaPlugin;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.Base64;

public class AdminProtectionPlugin extends JavaPlugin {

    // Зашифрованный ключ
    private static final String ENCRYPTED_KEY = "cWlMNmFNZ2wldHhoT0tuVmhnZCM="; // "qiL6aMgl%txhOKnVhgd#"

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
        return String.join("\n", getConfig().getStringList("messages." + key));
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

    public static String translateHexColorCodes(String message) {
        Pattern hexPattern = Pattern.compile("(?i)&#([A-Fa-f0-9]{6})");
        Matcher matcher = hexPattern.matcher(message);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hex = matcher.group(1); // Извлекаем HEX-значение
            StringBuilder builder = new StringBuilder("§x");
            for (char c : hex.toCharArray()) {
                builder.append("§").append(c);
            }
            matcher.appendReplacement(buffer, builder.toString());
        }
        matcher.appendTail(buffer);

        return buffer.toString();
    }
}