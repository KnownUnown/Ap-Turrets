package com.snowleopard1863.APTurrets;

import net.minecraft.server.v1_10_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;

public class NmsUtil {

	public static Arrow shootArrow(Player bukkitPlayer) {
		EntityPlayer player = ((CraftPlayer) bukkitPlayer).getHandle();
		net.minecraft.server.v1_10_R1.World world = player.getWorld();
		EntityTippedArrow arrow = new EntityTippedArrow(world, player);

		arrow.setNoGravity(true);

		world.addEntity(arrow);
		return (Arrow) arrow.getBukkitEntity();
	}

	public static void hideArrowClientside(Arrow arrow) {
		PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(arrow.getEntityId());

		for(Player player : Bukkit.getOnlinePlayers())
			((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
	}
}
