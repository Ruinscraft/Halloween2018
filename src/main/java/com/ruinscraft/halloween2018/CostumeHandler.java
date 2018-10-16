package com.ruinscraft.halloween2018;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_12_R1.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_12_R1.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_12_R1.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;

public class CostumeHandler {

	private Map<UUID, Costume> costumes;

	public CostumeHandler() {
		costumes = new HashMap<>();
	}

	public void clear() {
		costumes.clear();
	}
	
	public Costume getCostumeForPlayer(UUID uuid) {
		return costumes.get(uuid);
	}

	public void setCostumeForPlayer(UUID uuid, Costume costume) {
		costumes.put(uuid, costume);
		apply(uuid);
	}

	public void removeCostumeForPlayer(UUID uuid) {
		costumes.remove(uuid);
	}
	
	public boolean hasCostume(UUID uuid) {
		return costumes.containsKey(uuid);
	}

	public Costume getByName(String costumeName) {
		for (Costume costume : getRegisteredCostumes()) {
			if (costumeName.equalsIgnoreCase(costume.name)) {
				return costume;
			}
		}
		return null;
	}

	public List<Costume> getRegisteredCostumes() {
		List<Costume> costumes = new ArrayList<>();
		FileConfiguration config = Halloween2018Plugin.getInstance().getConfig();
		ConfigurationSection skinsSection = config.getConfigurationSection("costumes");
		for (String key : skinsSection.getKeys(false)) {
			Property texture = new Property("textures", skinsSection.getString(key + ".base64"), skinsSection.getString(key + ".signature"));
			costumes.add(new Costume(key, texture));
		}
		return costumes;
	}

	public Costume getRandomCostume() {
		List<Costume> costumes = getRegisteredCostumes();
		Random random = new Random();
		int index = random.nextInt(costumes.size());
		return costumes.get(index);
	}

	private void apply(UUID uuid) {
		Player player = Bukkit.getPlayer(uuid);
		CraftPlayer craftPlayer = (CraftPlayer) player;
		EntityPlayer entityPlayer = craftPlayer.getHandle();

		GameProfile profile = craftPlayer.getProfile();
		profile.getProperties().removeAll("textures");
		profile.getProperties().put("textures", getCostumeForPlayer(uuid).texture);
		
		for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
			CraftPlayer onlineCraftPlayer = (CraftPlayer) onlinePlayer;
			EntityPlayer onlineEntityPlayer = onlineCraftPlayer.getHandle();
			
			onlineEntityPlayer.playerConnection.sendPacket(new PacketPlayOutEntityDestroy(craftPlayer.getEntityId()));
			onlineEntityPlayer.playerConnection.sendPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, entityPlayer));
		}
		
		new BukkitRunnable() {
			@Override
			public void run() {
				for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
					CraftPlayer onlineCraftPlayer = (CraftPlayer) onlinePlayer;
					EntityPlayer onlineEntityPlayer = onlineCraftPlayer.getHandle();
					
					onlineEntityPlayer.playerConnection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entityPlayer));
					
					if (onlinePlayer != player) {
						onlineEntityPlayer.playerConnection.sendPacket(new PacketPlayOutNamedEntitySpawn(entityPlayer));
					}
				}
				player.getInventory().clear();
				player.setHealth(0);
			}
		 }.runTaskLater(Halloween2018Plugin.getInstance(), 4);
	}

}
