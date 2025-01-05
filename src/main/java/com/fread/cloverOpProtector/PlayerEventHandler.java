package com.fread.cloverOpProtector;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class PlayerEventHandler implements Listener {

    private final AdminProtectionPlugin plugin;
    private final LoginManager loginManager;
    private final Map<Player, BossBar> bossBars = new HashMap<>();
    private final Map<Player, Integer> loginTimers = new HashMap<>(); // Хранение таймеров для каждого игрока

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
                loginManager.saveAndClearInventory(player); // Сохраняем и очищаем инвентарь
                freezePlayer(player);

                player.sendTitle(
                        ChatColor.translateAlternateColorCodes('&', plugin.getMessage("login-required-title")),
                        ChatColor.translateAlternateColorCodes('&', plugin.getMessage("login-required-subtitle")),
                        10, 70, 20
                );

                // Создаём босс-бар
                BossBar bossBar = Bukkit.createBossBar(
                        replaceTimePlaceholder(plugin.getBossBarMessage(), 30),
                        BarColor.YELLOW,
                        BarStyle.SOLID
                );
                bossBar.setProgress(1.0);
                bossBar.addPlayer(player);
                bossBars.put(player, bossBar);

                // Запускаем таймер обратного отсчёта
                startLoginCountdown(player, bossBar);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        loginManager.logOut(player);

        // Удаляем босс-бар и таймер
        removeBossBar(player);
        loginTimers.remove(player);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.isOp() && !loginManager.isLoggedIn(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (player.isOp() && !loginManager.isLoggedIn(player)) {
                event.setCancelled(true);
            }
        }
    }

    private void freezePlayer(Player player) {
        player.setGameMode(GameMode.ADVENTURE);
        player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 1));
        player.getInventory().clear();
    }

    private void startLoginCountdown(Player player, BossBar bossBar) {
        final int loginTime = 30; // Время на авторизацию в секундах
        loginTimers.put(player, loginTime);

        new BukkitRunnable() {
            int timeLeft = loginTime;

            @Override
            public void run() {
                if (!loginManager.isLoggedIn(player)) {
                    if (timeLeft <= 0) {
                        // Время истекло, кикаем игрока
                        player.kickPlayer(ChatColor.translateAlternateColorCodes('&', plugin.getMessage("timeout-kick-message")));
                        removeBossBar(player);
                        cancel();
                    } else {
                        // Обновляем прогресс и текст босс-бара
                        bossBar.setProgress(timeLeft / (double) loginTime);
                        bossBar.setTitle(replaceTimePlaceholder(plugin.getBossBarMessage(), timeLeft));
                        timeLeft--;
                        loginTimers.put(player, timeLeft);
                    }
                } else {
                    // Игрок залогинился, убираем босс-бар
                    removeBossBar(player);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 20); // Запускаем каждую секунду
    }

    private void removeBossBar(Player player) {
        if (bossBars.containsKey(player)) {
            BossBar bossBar = bossBars.get(player);
            bossBar.removeAll();
            bossBars.remove(player);
        }
    }

    private String replaceTimePlaceholder(String message, int timeLeft) {
        return ChatColor.translateAlternateColorCodes('&', message.replace("{time}", String.valueOf(timeLeft)));
    }
}