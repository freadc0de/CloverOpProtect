package com.fread.cloverOpProtector;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerEventHandler implements Listener {

    private final AdminProtectionPlugin plugin;
    private final LoginManager loginManager;

    public PlayerEventHandler(AdminProtectionPlugin plugin, LoginManager loginManager) {
        this.plugin = plugin;
        this.loginManager = loginManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.isOp()) {
            if (loginManager.isIpAuthorized(player)) {
                loginManager.logIn(player);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessage("already-logged-in")));
            } else {
                freezePlayer(player);
                player.sendTitle(
                        ChatColor.translateAlternateColorCodes('&', plugin.getMessage("login-required-title")),
                        ChatColor.translateAlternateColorCodes('&', plugin.getMessage("login-required-subtitle")),
                        10, 70, 20
                );
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        loginManager.logOut(event.getPlayer());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.isOp() && !loginManager.isLoggedIn(player)) {
            event.setCancelled(true);
        }
    }

    private void freezePlayer(Player player) {
        player.setGameMode(GameMode.ADVENTURE);
        player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 1));
        player.getInventory().clear();
    }
}