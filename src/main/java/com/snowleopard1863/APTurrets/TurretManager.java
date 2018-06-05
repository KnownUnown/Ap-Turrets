package com.snowleopard1863.APTurrets;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Optional;

public interface TurretManager {

	void mountTurret(Player player, Block block);
	void dismountTurret(Player player);
	Optional<Turret> getTurret(Player player);

	boolean isTurret(Block block);
	boolean isTurretArray(String[] arr);
	boolean isOnTurret(Player player);
	boolean isTurretOccupied(Block block);
}
