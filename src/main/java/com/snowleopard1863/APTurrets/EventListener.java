package com.snowleopard1863.APTurrets;

import com.snowleopard1863.APTurrets.integration.IntegrationManager;
import com.snowleopard1863.APTurrets.integration.VaultIntegration;
import com.snowleopard1863.APTurrets.integration.WorldGuardIntegration;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.*;
import org.bukkit.material.MaterialData;

import java.util.Optional;

public class EventListener implements Listener {
	private IntegrationManager integrations;
	private TurretManager turrets;
	private Config config;
	private I18n lang = I18n.getInstance();
	
	EventListener(IntegrationManager integrations, TurretManager turrets, Config config) {
		this.integrations = integrations;
		this.turrets = turrets;
		this.config = config;
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		Player player = e.getPlayer();
		if(turrets.isOnTurret(player)) { // player is on a turret
			if(e.hasItem()) {
				switch(e.getItem().getType()) {
					case STONE_BUTTON: // Shoot turret
						turrets.getTurret(player).ifPresent(turret -> {
							switch(turret.shoot()) {
								case FAIL_COOLDOWN:
									break;
								case FAIL_NO_AMMO:
									player.getWorld().playSound(player.getLocation(),
											Sound.BLOCK_DISPENSER_FAIL, 1, 2);
									break;
								case SUCCESS:
									player.getWorld().playSound(player.getLocation(),
											Sound.ENTITY_FIREWORK_BLAST, 1, 2);
									player.getWorld().playEffect(player.getLocation(),
											Effect.MOBSPAWNER_FLAMES, 0);
									break;
							}
						});
						e.setCancelled(true);
						break;
				}
			}
		} else { // player isn't currently on a turret
			if(e.getAction() == Action.RIGHT_CLICK_BLOCK) { // attempt to mount turret
				if(!player.hasPermission("ap-turrets.use")) {
					lang.pprintf(e.getPlayer(), "use.insufficient_permissions");
					e.setCancelled(true);
					return;
				}

				Block block = e.getClickedBlock();
				if(turrets.isTurret(block) && !turrets.isTurretOccupied(block)) {
					turrets.mountTurret(player, block);
				}
			}
		}
	}

	@EventHandler
	public void onSignChange(SignChangeEvent e) {
		if(!e.getPlayer().hasPermission("ap-turrets.place")) {
			lang.pprintf(e.getPlayer(), "place.insufficient_permissions");
			e.setCancelled(true);
			return;
		}

		if(turrets.isTurretArray(e.getLines())) {
			if(config.placeInRegionOnly) {
				Optional<WorldGuardIntegration> wg = integrations.getIntegration(WorldGuardIntegration.class);
				wg.ifPresent(integration -> {
					if(!integration.isInARegion(e.getPlayer())) {
						lang.pprintf(e.getPlayer(), "place.no_region");
						e.setCancelled(true);
					}
				});
			}
			if(config.costToPlace > 0.00) {
				Optional<VaultIntegration> vault = integrations.getIntegration(VaultIntegration.class);
				vault.ifPresent(integration -> {
					if(!integration.withdrawFunds(e.getPlayer(), config.costToPlace)) {
						lang.pprintf(e.getPlayer(), "place.insufficient_funds");
						e.setCancelled(true);
					}
				});
			}
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		if(turrets.isOnTurret(e.getPlayer()) &&
				!e.getFrom().toVector().equals(e.getTo().toVector())) // src(x, y, z) == dst(x, y, z)
			e.setCancelled(true);
	}

	@EventHandler
	public void onPickupArrow(PlayerPickupArrowEvent e) {
		if(e.getArrow().getCustomName().equals("TurretRound"))
			e.setCancelled(true);
	}

	@EventHandler
	public void onArrowHit(ProjectileHitEvent e) {
		Arrow arrow = (Arrow) e.getEntity();
		Location loc = arrow.getLocation();
		Block blk = loc.subtract(0, 1, 0).getBlock();

		arrow.getWorld().playEffect(loc, Effect.STEP_SOUND, blk.getType());
		arrow.getWorld().playEffect(loc, Effect.TILE_BREAK, new MaterialData(blk.getType()), 0);
	}

	@EventHandler
	public void onEntityInteract(PlayerInteractEntityEvent e) {
		if(turrets.isOnTurret(e.getPlayer()) &&
				e.getRightClicked() instanceof Vehicle)
			turrets.dismountTurret(e.getPlayer());
	}

	@EventHandler
	public void onDamage(EntityDamageByEntityEvent e) {
		if(e.getDamager() instanceof Arrow &&
				e.getDamager().getCustomName().equals("TurretRound")) {
			e.setDamage(config.damage);
		}

		if(e.getEntity() instanceof Player) {
			Player damaged = (Player) e.getEntity();

			if(turrets.isOnTurret(damaged)) {
				turrets.dismountTurret(damaged);
			} else if(damaged.isGliding()) {
				damaged.setGliding(false);
				damaged.setSprinting(false);
			}
		}
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent e) {
		if(turrets.isOnTurret(e.getEntity().getKiller()))
			e.setDeathMessage(
					e.getEntity().getDisplayName() +
					" was gunned down by " +
					e.getEntity().getKiller().getDisplayName() + ".");
	}

	@EventHandler
	public void onSneak(PlayerToggleSneakEvent e) {
		if(e.isSneaking() && turrets.isOnTurret(e.getPlayer()))
			turrets.dismountTurret(e.getPlayer());
	}
}
