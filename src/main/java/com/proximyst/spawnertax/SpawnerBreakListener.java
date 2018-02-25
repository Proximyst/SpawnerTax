package com.proximyst.spawnertax;

import lombok.AllArgsConstructor;
import lombok.experimental.var;
import lombok.val;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.List;

@AllArgsConstructor
public class SpawnerBreakListener
      implements Listener {
  private final Main main;

  @SuppressWarnings("Duplicates")
  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST) // Only handle this if required
  public void onBreak(final BlockBreakEvent event) {
    if (event.getPlayer().hasPermission(Main.PERMISSION_FREE)
          || event.getBlock().getType() != Material.MOB_SPAWNER
          || !event.getPlayer().hasPermission(Main.PERMISSION_MINE)) {
      return;
    }
    val state = (CreatureSpawner) event.getBlock().getState();
    var tax = main.getTaxes().get(state.getSpawnedType());
    if (tax == null) {
      tax = main.getTaxes().get(null);
      if (tax == null) {
        return; // no tax for the spawner
      }
    }
    if (tax <= 0) {
      return; // no tax
    }

    val discount = main.getDiscounts()
                       .entrySet()
                       .parallelStream()
                       .sorted((o1, o2) -> (int) (Math.ceil(o2.getKey()) - Math.floor(o1.getKey())))
                       .filter(it -> event.getPlayer().hasPermission(it.getValue()))
                       .findFirst();
    if (discount.isPresent()) {
      tax = (int) Math.ceil(tax * (1 - discount.get().getKey()));
    }

    if (main.getEconomy().has(
          event.getPlayer(),
          tax
    )) {
      main.getEconomy().withdrawPlayer(
            event.getPlayer(),
            tax
      );
      val message = main.getConfig().get("mine-success-message");
      if (message != null) {
        if (message instanceof String) {
          event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes(
                '&',
                ((String) message).replace(
                      "%amount%",
                      tax.toString()
                )
          ));
        } else if (message instanceof List) {
          val finalTax = tax; // effectively and statically final
          ((List<?>) message).forEach(it -> event.getPlayer().sendMessage(
                ChatColor.translateAlternateColorCodes(
                      '&',
                      it.toString().replace(
                            "%amount%",
                            finalTax.toString()
                      )
                )
          ));
        }
      }
      return;
    }
    // not enough money

    val message = main.getConfig().get("mine-failure-message");
    if (message != null) {
      if (message instanceof String) {
        event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes(
              '&',
              ((String) message).replace(
                    "%amount%",
                    tax.toString()
              )
        ));
      } else if (message instanceof List) {
        val finalTax = tax; // effectively and statically final
        ((List<?>) message).forEach(it -> event.getPlayer().sendMessage(
              ChatColor.translateAlternateColorCodes(
                    '&',
                    it.toString().replace(
                          "%amount%",
                          finalTax.toString()
                    )
              )
        ));
      }
    }
  }
}