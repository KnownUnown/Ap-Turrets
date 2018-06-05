package com.snowleopard1863.APTurrets;

import com.snowleopard1863.APTurrets.integration.IntegrationManager;
import com.snowleopard1863.APTurrets.integration.MovecraftIntegration;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Optional;

public class Turret {
	private static final ItemStack ammunition = new ItemStack(Material.ARROW, 1);

	private IntegrationManager manager;
	private Config config;
	private Player player;

	private long lastShot;

	ArrayList<Arrow> shotArrows = new ArrayList<>();

	public enum ShotStatus {
		SUCCESS, FAIL_NO_AMMO, FAIL_COOLDOWN
	}

	public Turret(IntegrationManager manager, Config config, Player player) {
		this.manager = manager;

		this.config = config;
		this.player = player;
	}

	public ShotStatus shoot() { // TODO: knownunown: particles and sfx
		if(!config.infiniteAmmo && !loadAmmo())
			return ShotStatus.FAIL_NO_AMMO;

		if(System.nanoTime() - lastShot < config.fireInterval * Math.pow(10, 6))
			return ShotStatus.FAIL_COOLDOWN;

		Arrow arrow = NmsUtil.shootArrow(player);
		arrow.setCustomName("TurretRound");
		Vector skewDirection = skew(player.getLocation().getDirection());
		arrow.setVelocity(skewDirection.multiply(config.velocity));
		arrow.setShooter(player);
		arrow.setCritical(false);
		arrow.setBounce(false);

		if(config.tracers) {
			NmsUtil.hideArrowClientside(arrow);
			shotArrows.add(arrow);
		}

		lastShot = System.nanoTime();
		return ShotStatus.SUCCESS;
	}

	private Vector skew(Vector v) {
		return v.add(new Vector(
				Math.random() * 2 * config.skew - config.skew,
				Math.random() * 2 * config.skew - config.skew,
				Math.random() * 2 * config.skew - config.skew
		));
	}

	private boolean loadAmmo() {
		if(config.sources.chest) {
			Optional<MovecraftIntegration> integration =
					manager.getIntegration(MovecraftIntegration.class);
			if(integration.isPresent()) {
				Optional<Inventory> inv = integration.get().inventoryContainingItemForPlayerOnCraft(player, ammunition);
				if(inv.isPresent()) {
					inv.get().removeItem(ammunition);
					return true;
				}
			}
		}

		if(config.sources.inventory) {
			if(player.getInventory().containsAtLeast(ammunition, 1)) {
				player.getInventory().removeItem(ammunition);
				player.updateInventory();
				return true;
			}
		}

		return false;
	}
}
