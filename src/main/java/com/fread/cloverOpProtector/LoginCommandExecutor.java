package com.fread.cloverOpProtector;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LoginCommandExecutor implements CommandExecutor {

    private final AdminProtectionPlugin plugin;
    private final LoginManager loginManager;

    public LoginCommandExecutor(AdminProtectionPlugin plugin, LoginManager loginManager) {
        this.plugin = plugin;
        this.loginManager = loginManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        if (!player.isOp()) {
            player.sendMessage(ChatColor.RED + "This command is only for server operators.");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Usage: /" + label + " <password>");
            return true;
        }

        String password = args[0];
        if (password.equals(plugin.getAdminPassword())) {
            loginManager.logIn(player);
            player.sendTitle(ChatColor.GREEN + "Login Successful", "", 10, 70, 20);
            player.sendMessage(ChatColor.GREEN + "You have successfully logged in.");
        } else {
            player.kickPlayer(ChatColor.RED + "Incorrect password");
        }
        return true;
    }
}