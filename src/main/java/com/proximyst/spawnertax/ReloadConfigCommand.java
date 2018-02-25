package com.proximyst.spawnertax;

import lombok.AllArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@AllArgsConstructor
public class ReloadConfigCommand
      implements CommandExecutor {
  private final Main main;

  @Override
  public boolean onCommand(
        CommandSender sender,
        Command command,
        String label,
        String[] args
  ) {
    if (sender instanceof BlockCommandSender) {
      sender.sendMessage("SpawnerTax > Only players and console can perform this command for security measures.");
      return false;
    }
    if (sender instanceof Player
          && !sender.hasPermission(Main.PERMISSION_RELOAD)) {
      sender.sendMessage(
            ChatColor.RED.toString()
                  + "SpawnerTax > "
                  + ChatColor.YELLOW
                  + "You cannot perform this command!");
      return true;
    }
    main.reloadConfig();
    main.reloadPermissions();
    main.reloadTaxes();
    sender.sendMessage(
          ChatColor.GREEN.toString()
                + "SpawnerTax > "
                + ChatColor.YELLOW
                + "The config has been successfully reloaded.");
    return true;
  }
}
