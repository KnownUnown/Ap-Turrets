package com.snowleopard1863.APTurrets.integration;

import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.utils.MovecraftLocation;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Optional;

public class MovecraftIntegration implements Integration {
	private CraftManager manager;

	public void initialize() {
		manager = CraftManager.getInstance();
	}

	private Location movecraftToBukkitLoc(World world, MovecraftLocation location) {
		return new Location(world, location.getX(), location.getY(), location.getZ());
	}

	public Optional<Inventory> inventoryContainingItemForPlayerOnCraft(Player player, ItemStack stack) {
		Craft craft = manager.getCraftByPlayer(player);
		if(craft == null)
			return Optional.empty();

		return Arrays.stream(craft.getBlockList())
				.map(location -> movecraftToBukkitLoc(player.getWorld(), location))
				.map(Location::getBlock)
				.filter(block ->
						block.getType() == Material.CHEST ||
						block.getType() == Material.TRAPPED_CHEST)
				.map(block -> ((InventoryHolder) block))
				.filter(inventoryHolder -> inventoryHolder.getInventory().containsAtLeast(stack, 1))
				.map(InventoryHolder::getInventory)
				.findFirst();
	}

	public String getRequiredPlugin() {
		return "Movecraft";
	}
}
