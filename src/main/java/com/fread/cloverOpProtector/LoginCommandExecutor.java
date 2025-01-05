package com.fread.cloverOpProtector;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class LoginCommandExecutor implements CommandExecutor {

    private final AdminProtectionPlugin plugin;
    private final LoginManager loginManager;

    public LoginCommandExecutor(AdminProtectionPlugin plugin, LoginManager loginManager) {
        this.plugin = plugin;
        this.loginManager = loginManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (!player.isOp()) {
            player.sendMessage("This command is only for server operators.");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessage("usage")));
            return true;
        }

        String password = args[0];
        if (password.equals(plugin.getAdminPassword())) {
            loginManager.logIn(player);
            player.sendTitle(
                    ChatColor.translateAlternateColorCodes('&', plugin.getMessage("login-successful-title")),
                    ChatColor.translateAlternateColorCodes('&', plugin.getMessage("login-successful-subtitle")),
                    10, 70, 20
            );
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessage("login-successful-message")));
        } else {
            player.kickPlayer(ChatColor.translateAlternateColorCodes('&', plugin.getMessage("incorrect-password")));
        }
        return true;
    }
}