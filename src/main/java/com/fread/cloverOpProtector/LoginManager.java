package com.fread.cloverOpProtector;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LoginManager {

    private final AdminProtectionPlugin plugin;
    private final Map<UUID, Boolean> loggedInPlayers = new HashMap<>();
    private final Map<String, Long> ipLoginTimestamps = new HashMap<>();
    private static final long LOGIN_EXPIRATION_TIME = 3600000; // 1 hour in milliseconds

    public LoginManager(AdminProtectionPlugin plugin) {
        this.plugin = plugin; // Сохраняем ссылку на плагин для будущего использования
    }

    public boolean isLoggedIn(Player player) {
        return loggedInPlayers.getOrDefault(player.getUniqueId(), false);
    }

    public void logIn(Player player) {
        loggedInPlayers.put(player.getUniqueId(), true);
        ipLoginTimestamps.put(getPlayerIp(player), System.currentTimeMillis());
        player.removePotionEffect(org.bukkit.potion.PotionEffectType.BLINDNESS);

        // Пример использования plugin
        plugin.getLogger().info("Player " + player.getName() + " has logged in.");
    }

    public void logOut(Player player) {
        loggedInPlayers.remove(player.getUniqueId());
    }

    public boolean isIpAuthorized(Player player) {
        String playerIp = getPlayerIp(player);
        long currentTime = System.currentTimeMillis();
        return ipLoginTimestamps.containsKey(playerIp) && (currentTime - ipLoginTimestamps.get(playerIp) < LOGIN_EXPIRATION_TIME);
    }

    public String getPlayerIp(Player player) {
        return player.getAddress() != null && player.getAddress().getAddress() != null
                ? player.getAddress().getAddress().getHostAddress()
                : "0.0.0.0";
    }
}
