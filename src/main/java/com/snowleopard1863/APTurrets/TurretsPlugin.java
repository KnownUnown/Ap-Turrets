package com.snowleopard1863.APTurrets;

import com.snowleopard1863.APTurrets.integration.*;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Optional;

public class TurretsPlugin extends JavaPlugin implements IntegrationManager, TurretManager {
	private static final String META_TURRET_OCCUPANT = "TurretUser";
	private static final String[] TURRET_SIGN_TEXT = new String[]{
			"Mounted", "Gun", "", ""
	};

	private HashMap<String, Integration> integrations = new HashMap<>();
	private HashMap<Player, Turret> turrets = new HashMap<>();

	private Config config;
	private EventListener listener;

	@Override
	public void onEnable() {
		config = new Config(new File(getDataFolder(), "config.yml").toPath());
		try {
			config.loadAndSave();
		} catch(IOException e) {
			e.printStackTrace();
			getPluginLoader().disablePlugin(this);
		}
		enableIntegrations();

		listener = new EventListener(this, this, config);
		getServer().getPluginManager().registerEvents(listener, this);

		getServer().getScheduler().scheduleSyncRepeatingTask(this, () ->
			turrets.forEach(((player, turret) -> {
				for(Iterator<Arrow> i = turret.shotArrows.iterator(); i.hasNext(); ) {
					Arrow arrow = i.next();

					if (!arrow.getCustomName().equals("TurretRound")) return;
					if (!arrow.isOnGround()) {
						player.getWorld().spawnParticle(Particle.CRIT, arrow.getLocation(), 3);
						return;
					}

					arrow.remove();
					i.remove();
				}
			}))
		, 0, config.traceInterval);
	}

	public Optional<Turret> getTurret(Player player) {
		return Optional.ofNullable(turrets.get(player));
	}

	public void mountTurret(Player player, Block block) {
		block.setMetadata(META_TURRET_OCCUPANT,
				new FixedMetadataValue(this, player.getUniqueId()));
		turrets.put(player, new Turret(this, config, player));
		player.teleport(block.getLocation());
		player.setWalkSpeed(0);
	}

	public void dismountTurret(Player player) {
		dismountTurret(player, true);
	}

	private void dismountTurret(Player player, boolean remove) {
		Block block = player.getWorld().getBlockAt(player.getLocation());
		if(isTurret(block))
			block.removeMetadata(META_TURRET_OCCUPANT, this);
		if(remove)
			turrets.remove(player);
		player.setWalkSpeed(0.2f);
	}

	public boolean isTurret(Block block) {
		BlockState state = block.getState();

		return state instanceof Sign &&
				isTurretArray(((Sign) state).getLines());
	}

	public boolean isTurretArray(String[] arr) {
		return Arrays.equals(arr, TURRET_SIGN_TEXT);
	}

	public boolean isOnTurret(Player player) {
		return turrets.containsKey(player);
	}

	public boolean isTurretOccupied(Block block) {
		return block.hasMetadata(META_TURRET_OCCUPANT);
	}

	@Override
	public void onDisable() {
		for(Iterator<Player> i = turrets.keySet().iterator(); i.hasNext(); ) {
			Player p = i.next();
			dismountTurret(p, false);
			i.remove();
		}
	}

	private void enableIntegrations() {
		StringBuilder str = new StringBuilder("Enabled integrations:");
		Arrays.stream(new Integration[]{
				// Taking arrows from craft chests
				new MovecraftIntegration(),
				// Placing of turrets only allowed in regions
				new WorldGuardIntegration(),
				// Deducting balance to place turrets
				new VaultIntegration(),
		})
				.filter(integration -> getServer().getPluginManager()
						.getPlugin(integration.getRequiredPlugin()) != null)
				.filter(integration -> {
					try {
						integration.initialize();
					} catch(Exception e) {
						return false;
					}
					return true;
				})
				.peek(this::register)
				.map(integration -> " " + integration.getClass().getSimpleName())
				.forEach(str::append);

		getLogger().info(str.toString());
	}
	@Override
	public void register(Integration integration) {
		integrations.put(integration.getClass().getName(), integration);
	}

	public <T extends Integration> Optional<T> getIntegration(Class<T> clazz) {
		return Optional.ofNullable(clazz.cast(integrations.get(clazz.getName())));
	}
}
