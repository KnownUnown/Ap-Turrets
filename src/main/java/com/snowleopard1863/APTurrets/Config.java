package com.snowleopard1863.APTurrets;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import de.exlll.configlib.Version;

import java.nio.file.Path;

@SuppressWarnings("unused")
@Version(version = "1.0.0")
public class Config extends Configuration {

	@Comment("Cost to place turret (requires Vault integration.)")
	public double costToPlace = 15000.00;

	@Comment("Only allow turrets to be placed in WorldGuard regions (requires WorldGuard integration.)")
	public boolean placeInRegionOnly = true;

	@Comment("Damage dealt per ammo round.")
	public double damage = 2.5;

	@Comment("Chance that a given round will be an incendiary round.")
	public double incendiaryChance = 0.10;

	@Comment("Knockback multiplier on hit.")
	public double knockback = 2.0;

	@Comment("Velocity multiplier for rounds.")
	public double velocity = 4.0;

	@Comment("Minimum round velocity: rounds below this velocity will be despawned.")
	public double disintegrateVelocity = 2.0;

	@Comment("Hide arrows.")
	public boolean tracers = true;

	@Comment("Interval (ticks) in which to spawn the trace particle.")
	public int traceInterval = 10;

	@Comment("Fire interval in milliseconds.")
	public long fireInterval = 2;

	@Comment("Maximum skew in arrow pitch/yaw.")
	public double skew = 0.02;

	@Comment("Infinite ammo.")
	public boolean infiniteAmmo = false;

	@Comment({
			"Sources to take ammo from.",
			"Chest source requires Movecraft integration."
			})
	public AmmoSources sources = new AmmoSources();

	public class AmmoSources {
		public boolean inventory = true;
		public boolean chest = true;
		public boolean craft = true;
	}

	@Comment("Language for plugin messages.")
	public Locale locale = new Locale();

	public class Locale {
		public String language = "en";
		public String region = "US";

		public java.util.Locale toJavaLocale() {
			return new java.util.Locale(language, region);
		}
	}

	public Config(Path path) {
		super(path);
	}
}
