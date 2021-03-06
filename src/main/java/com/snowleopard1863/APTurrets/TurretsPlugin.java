package com.snowleopard1863.APTurrets;

import com.snowleopard1863.APTurrets.integration.*;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class TurretsPlugin extends JavaPlugin implements IntegrationManager, TurretManager {
	private static final String META_TURRET_OCCUPANT = "TurretUser";
	private static final String[] TURRET_SIGN_TEXT = new String[]{
			"Mounted", "Gun", "", ""
	};

	private HashMap<String, Integration> integrations = new HashMap<>();
	private HashMap<Player, Turret> turrets = new HashMap<>();

	private Config config;
	private I18n lang;

	@Override
	public void onEnable() {
		config = new Config(new File(getDataFolder(), "config.yml").toPath());
		try {
			config.loadAndSave();
		} catch(IOException e) {
			e.printStackTrace();
			getPluginLoader().disablePlugin(this);
		}
		lang = I18n.getInstance();
		lang.init(config);

		enableIntegrations();

		EventListener listener = new EventListener(this, this, config);
		getServer().getPluginManager().registerEvents(listener, this);

		getServer().getScheduler().scheduleSyncRepeatingTask(this, () ->
			turrets.forEach(((player, turret) -> {
				for(Iterator<Arrow> i = turret.shotArrows.iterator(); i.hasNext(); ) {
					Arrow arrow = i.next();

					if (!arrow.getCustomName().equals("TurretRound")) continue;

					if (!arrow.isOnGround()) {
						if(isAreaLoaded(arrow.getLocation())) {
							player.getWorld().spawnParticle(Particle.CRIT,
									arrow.getLocation(), 3, 0.0, 0.0, 0.0, 0);

							if(arrow.getVelocity().distance(new Vector()) > config.disintegrateVelocity) {
								continue;
							}
						}
					}

					// default condition
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
		player.teleport(block.getLocation().add(0.5, 0, 0.5));
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

	private boolean isAreaLoaded(Location location) {
		World world = location.getWorld();
		int chunkX = location.getBlockX() >> 4;
		int chunkZ = location.getBlockZ() >> 4;

		final int offsets[][] = {{0, 0}, {0, 1}, {0, -1}, {1, 0}, {-1, 0}};
		for(int[] offset : offsets) {
			if(!world.isChunkLoaded(chunkX + offset[0], chunkZ + offset[1])) {
				return false;
			}
		}

		return true;
	}

	@Override
	public void onDisable() {
		for(Iterator<Player> i = turrets.keySet().iterator(); i.hasNext(); ) {
			Player p = i.next();
			dismountTurret(p, false);
			i.remove();
		}

		getServer().getWorlds().stream()
				.map(World::getEntities)
				.forEach(entities -> entities.stream()
						.filter(entity ->
								entity.getType().equals(EntityType.ARROW) &&
								entity.getCustomName().equals("TurretRound"))
						.forEach(Entity::remove)); // don't leak turret rounds
	}

	private void enableIntegrations() {
		ArrayList<String> names = new ArrayList<>();
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
				.forEach(names::add);

		getLogger().info(lang.sprintf("plugin.enabled_integrations",
				String.join(",", names.toArray(new String[]{}))));
	}

	@Override
	public void register(Integration integration) {
		integrations.put(integration.getClass().getName(), integration);
	}

	public <T extends Integration> Optional<T> getIntegration(Class<T> clazz) {
		return Optional.ofNullable(clazz.cast(integrations.get(clazz.getName())));
	}
}
