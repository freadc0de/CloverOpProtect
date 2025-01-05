package com.fread.cloverOpProtector;

import org.bukkit.plugin.java.JavaPlugin;

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
        return String.join("\n", getConfig().getStringList("messages." + key));
    }
}