package com.ruinscraft.halloween2018;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Pumpkin;
import org.bukkit.scheduler.BukkitRunnable;

public class PumpkinLightTask implements Runnable {

	private static final int PUMPKIN_RAD = 10;

	private List<Location> pumpkinLocations;

	public PumpkinLightTask() {
		pumpkinLocations = new ArrayList<>();
		for (Chunk chunk : Bukkit.getWorld("world").getLoadedChunks()) {
			checkChunk(chunk);
		}
	}

	// run every like 5 ticks?
	@Override
	public void run() {
		
		boolean lightningStrike = false;
		
		if (Math.random() > 0.96) {
			lightningStrike = true;
		}
		
		if (lightningStrike) {
			for (Location loc : pumpkinLocations) {
				Block block = loc.getBlock();
				BlockState state = block.getState();
				MaterialData data = state.getData();
				BlockFace direction = ((Pumpkin) data).getFacing();
				Pumpkin pumpkin = new Pumpkin(direction);
				state.setType(Material.JACK_O_LANTERN);
				state.setData(pumpkin);
				state.update(true);
			}
			
			new BukkitRunnable() {
				@Override
				public void run() {
					Bukkit.getWorld("world").strikeLightning(new Location(Bukkit.getWorld("world"), -21, 118, -38));
				}
			}.runTaskLater(Halloween2018Plugin.getInstance(), 50L);
			
			return;
		}
		
		for (Location loc : pumpkinLocations) {
			boolean playerNearby = false;

			for (Player player : Bukkit.getOnlinePlayers()) {
				if (player.getLocation().distance(loc) <= PUMPKIN_RAD) {
					playerNearby = true;
				}
			}

			Block block = loc.getBlock();
			BlockState state = block.getState();
			MaterialData data = state.getData();
			BlockFace direction = ((Pumpkin) data).getFacing();
			Pumpkin pumpkin = new Pumpkin(direction);
			Material material = playerNearby ? Material.JACK_O_LANTERN : Material.PUMPKIN;

			state.setType(material);
			state.setData(pumpkin);
			state.update(true);
		}
	}

	private void checkChunk(Chunk chunk) {
		int x = chunk.getX() << 4;
		int z = chunk.getZ() << 4;

		World world = chunk.getWorld();

		for(int xx = x; xx < x + 16; xx++) {
			for(int zz = z; zz < z + 16; zz++) {
				for(int yy = 0; yy < 256; yy++) {
					Block block = world.getBlockAt(xx, yy, zz);
					if (block.getType() == Material.PUMPKIN || block.getType() == Material.JACK_O_LANTERN) {
						Block rel = block.getRelative(BlockFace.DOWN);
						if (rel.getType() == Material.ANVIL) {
							pumpkinLocations.add(block.getLocation());
						}
					}
				}
			}
		}
	}

}
