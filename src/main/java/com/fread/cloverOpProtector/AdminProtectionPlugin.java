package com.fread.cloverOpProtector;

import org.bukkit.plugin.java.JavaPlugin;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AdminProtectionPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
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

    public String getMessage(String key) {
        String rawMessage = String.join("\n", getConfig().getStringList("messages." + key));
        return translateHexColorCodes(rawMessage);
    }

    public String getBossBarMessage() {
        return translateHexColorCodes(String.join("\n", getConfig().getStringList("messages.boss-bar-message")));
    }


    public static String translateHexColorCodes(String message) {
        Pattern hexPattern = Pattern.compile("(?i)&#([A-Fa-f0-9]{6})");
        Matcher matcher = hexPattern.matcher(message);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hex = matcher.group(1);
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