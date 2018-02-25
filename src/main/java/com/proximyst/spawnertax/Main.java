package com.proximyst.spawnertax;

import com.google.common.base.Enums;
import lombok.Getter;
import lombok.val;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.EntityType;
import org.bukkit.event.HandlerList;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class Main
      extends JavaPlugin {
  public static final String PERMISSION_RELOAD = "spawnertax.reload";
  public static final String PERMISSION_MINE = "spawnertax.mine";
  public static final String PERMISSION_FREE = "spawnertax.free";

  @Getter private Economy economy;
  @Getter private final Map<EntityType, Integer> taxes = new HashMap<>();
  @Getter private final Map<Double, Permission> discounts = new HashMap<>();

  @Override
  public void onEnable() {
    {
      final Plugin vault = getServer().getPluginManager().getPlugin("Vault");
      if (vault == null || !vault.isEnabled()) {
        getLogger().warning("The plugin cannot enable without Vault!");
        setEnabled(false);
        return;
      }
      val economyService = getServer().getServicesManager().getRegistration(Economy.class);
      if (economyService == null || (economy = economyService.getProvider()) == null) {
        getLogger().warning("The plugin cannot enable without an economy plugin!");
        setEnabled(false);
        return;
      }
    } // economy is now non-null

    saveDefaultConfig();
    reloadTaxes();
    reloadPermissions();

    getServer().getPluginManager().registerEvents(
          new SpawnerBreakListener(this),
          this
    );
    getCommand("spawnertax-reload").setExecutor(new ReloadConfigCommand(this));
  }

  @Override
  public void onDisable() {
    HandlerList.unregisterAll(this);
    discounts.values().forEach(getServer().getPluginManager()::removePermission);
    discounts.clear();
    taxes.clear();
  }

  protected void reloadPermissions() {
    discounts.values().forEach(getServer().getPluginManager()::removePermission);
    discounts.clear();
    val discountSection = getConfig().getConfigurationSection("discounts");
    if (discountSection == null) {
      return;
    }
    for (String key : discountSection.getKeys(false)) {
      if (key == null) {
        continue;
      }
      key = key.toLowerCase().trim().replace(
            ' ',
            '_'
      );
      if (key.isEmpty()) {
        continue;
      }
      val discount = discountSection.getDouble(
            key,
            0d
      );
      if (discount <= 0) {
        continue;
      }
      val permission = new Permission(
            "spawnertax.discount." + key,
            "A " + discount + "% discount on breaking spawners",
            PermissionDefault.FALSE
      );
      getServer().getPluginManager().addPermission(permission);
    }
  }

  protected void reloadTaxes() {
    taxes.clear();
    val taxSection = getConfig().getConfigurationSection("taxes");
    if (taxSection == null) {
      getLogger().warning("There are no taxes defined, therefore the plugin is useless and disabling.");
      setEnabled(false);
      return;
    }
    for (String key : taxSection.getKeys(false)) {
      if (key.equals("_")) {
        taxes.put(
              null,
              taxSection.getInt(
                    key,
                    0
              )
        );
      }
      val type = Enums.getIfPresent(
            EntityType.class,
            key.toUpperCase()
      );
      if (!type.isPresent()) {
        getLogger().warning("Unknown entity type: " + key.toUpperCase());
        continue;
      }
      val price = taxSection.getInt(
            key,
            0
      );

      taxes.put(
            type.get(),
            price
      );
    }
  }
}
