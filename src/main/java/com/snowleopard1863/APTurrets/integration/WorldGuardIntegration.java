package com.snowleopard1863.APTurrets.integration;

import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.managers.RegionManager;
import org.bukkit.entity.Player;

public class WorldGuardIntegration implements Integration {

	@Override
	public void initialize() {
	}

	public boolean isInARegion(Player player) {
		RegionManager rm = WGBukkit.getRegionManager(player.getWorld());
		return rm.getApplicableRegions(player.getLocation()).size() > 0;
	}

	@Override
	public String getRequiredPlugin() {
		return "WorldGuard";
	}
}
