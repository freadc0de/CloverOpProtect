package com.fread.cloverOpProtector;

import org.bukkit.plugin.java.JavaPlugin;

public class AdminProtectionPlugin extends JavaPlugin {

    private LoginManager loginManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loginManager = new LoginManager(this);

        getServer().getPluginManager().registerEvents(new PlayerEventHandler(this, loginManager), this);
        getCommand("pass").setExecutor(new LoginCommandExecutor(this, loginManager));

        getLogger().info("AdminProtection enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("AdminProtection disabled.");
    }

    public String getAdminPassword() {
        return getConfig().getString("admin-password", "defaultPassword");
    }
}