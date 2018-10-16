package com.ruinscraft.halloween2018;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.ruinscraft.halloween2018.traits.NPCPowderGiverTrait;

import me.lucko.luckperms.api.LuckPermsApi;
import me.lucko.luckperms.api.Node;
import me.lucko.luckperms.api.User;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.md_5.bungee.api.ChatColor;

public class Halloween2018Database {

	private File dbFile;
	private Connection connection;

	public Halloween2018Database(File dbFile) throws IOException {
		if (!dbFile.exists()) {
			dbFile.createNewFile();
		}

		this.dbFile = dbFile;
	}

	public Connection getConnection() {
		try {
			if (connection == null || connection.isClosed()) {
				Class.forName("org.sqlite.JDBC");
				connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
			}
			// check table
			PreparedStatement insert = connection.prepareStatement("create table if not exists npc_interact (player_uuid VARCHAR(36), npc_id int);");
			insert.execute();
			insert.close();
			return connection;
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void handleNPCInteract(final UUID playerUUID, final int npcID) {
		Halloween2018Plugin.getInstance().getServer().getScheduler().runTaskAsynchronously(Halloween2018Plugin.getInstance(), () -> {
			boolean hasClickedBefore = false;
			
			try {
				PreparedStatement select = getConnection().prepareStatement("select * from npc_interact where player_uuid = ? and npc_id = ?;");
				select.setString(1, playerUUID.toString());
				select.setInt(2, npcID);
				ResultSet rs = select.executeQuery();
				if (rs.next()) {
					hasClickedBefore = true;
				}
				rs.close();
				select.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}


			Player player = Bukkit.getPlayer(playerUUID);

			if (player == null) return; // this is run async, just to double check

			if (hasClickedBefore) {
				player.sendMessage(ChatColor.GOLD + "You have already collected your Powder from me! Get lost!");
				return;
			}
			
			player.sendMessage(ChatColor.GOLD + "I have a Powder for you...");
			
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			
			if (!player.isOnline()) { // remember we waited for 2 seconds...
				return;
			}
			
			NPC npc = CitizensAPI.getNPCRegistry().getById(npcID);
			NPCPowderGiverTrait powderTrait = npc.getTrait(NPCPowderGiverTrait.class);
			String powderName = powderTrait.getPowder();
			LuckPermsApi lpapi = Halloween2018Plugin.getInstance().getLuckPermsApi();
			User user = lpapi.getUser(player.getName());
			Node node = lpapi.buildNode("powder.powder." + powderName.toLowerCase()).setValue(true).build();
			
			if (user.setPermission(node).asBoolean()) {
				lpapi.getUserManager().saveUser(user);
			}

			player.sendMessage(ChatColor.GOLD + "Received Powder: " + ChatColor.RED + powderName);
			player.sendMessage(ChatColor.GOLD + "Use it with " + ChatColor.RED + "/powder " + powderName);
			player.sendTitle(ChatColor.GOLD + "POWDER UP!", "You got the " + ChatColor.RED + powderName + ChatColor.WHITE + " Powder!", 5, 80, 20);
			player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5F, 1.0F);
			
			try {
				PreparedStatement insert = getConnection().prepareStatement("insert into npc_interact (player_uuid, npc_id) values (?, ?);");
				insert.setString(1, playerUUID.toString());
				insert.setInt(2, npcID);
				insert.execute();
				insert.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}

	public void close() {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
