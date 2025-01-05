package com.fread.cloverOpProtector;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatHandler implements Listener {

    private final AdminProtectionPlugin plugin;
    private final LoginManager loginManager;

    public ChatHandler(AdminProtectionPlugin plugin, LoginManager loginManager) {
        this.plugin = plugin;
        this.loginManager = loginManager;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (player.isOp() && !loginManager.isLoggedIn(player)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessage("chat-blocked")));
        }
    }
}