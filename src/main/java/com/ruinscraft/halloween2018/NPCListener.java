package com.ruinscraft.halloween2018;

import java.lang.reflect.Field;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import com.mojang.authlib.GameProfile;
import com.ruinscraft.halloween2018.traits.NPCCostumeSelectorTrait;
import com.ruinscraft.halloween2018.traits.NPCPowderGiverTrait;

import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.md_5.bungee.api.ChatColor;

public class NPCListener implements Listener {

	private static final String INVENTORY_NAME = "Costume Selector";
	
	private final Inventory costumeMenu;
	
	public NPCListener() {
		costumeMenu = Bukkit.createInventory(null, 9, INVENTORY_NAME);
		int index = 0;
		for (Costume costume : Halloween2018Plugin.getInstance().getCostumeHandler().getRegisteredCostumes()) {
			if (index >= 9) break;
			ItemStack costumeSkull = getSkull(costume);
			costumeMenu.addItem(costumeSkull);
			index++;
		}
	}
	
	@EventHandler
	public void onDeath(PlayerDeathEvent event) {
		event.setDeathMessage(null);
	}
	
	@EventHandler
	public void onNPCClick(NPCRightClickEvent event) {
		Player player = event.getClicker();
		NPC npc = event.getNPC();
		if (npc.hasTrait(NPCCostumeSelectorTrait.class)) {
			player.openInventory(costumeMenu);
		}
		
		if (npc.hasTrait(NPCPowderGiverTrait.class)) {
			CostumeHandler costumeHandler = Halloween2018Plugin.getInstance().getCostumeHandler();
			if (!costumeHandler.hasCostume(player.getUniqueId())) {
				new BukkitRunnable() {
					@Override
					public void run() {
						player.sendMessage(ChatColor.GOLD + "Go get a costume first from the Costume Selector");
					}
				}.runTaskLater(Halloween2018Plugin.getInstance(), 2);
				return;
			}
			Halloween2018Plugin.getInstance().getDatabase().handleNPCInteract(player.getUniqueId(), npc.getId());
		}
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (!(event.getWhoClicked() instanceof Player)) {
			return;
		}
		
		Player player = (Player) event.getWhoClicked();
		
		if (event.getInventory().getName().equals(INVENTORY_NAME)) {
			event.setResult(Result.DENY);
			event.setCancelled(true);
			
			ItemStack clicked = event.getCurrentItem();
			
			if (clicked == null || clicked.getType() == Material.AIR) {
				return;
			}
			
			if (clicked.hasItemMeta() && clicked.getItemMeta().hasDisplayName()) {
				String costumeName = clicked.getItemMeta().getDisplayName();
				CostumeHandler costumeHandler = Halloween2018Plugin.getInstance().getCostumeHandler();
				Costume costume = costumeHandler.getByName(costumeName);
				costumeHandler.setCostumeForPlayer(player.getUniqueId(), costume);
				
				player.closeInventory();
				player.sendMessage(ChatColor.GOLD + "Switched costume to " + ChatColor.RED + costume.name);
			}
		}
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		Halloween2018Plugin.getInstance().getCostumeHandler().removeCostumeForPlayer(event.getPlayer().getUniqueId());
	}
	
	private static ItemStack getSkull(Costume costume) {
		ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
		SkullMeta headMeta = (SkullMeta) head.getItemMeta();
		headMeta.setDisplayName(costume.name);
		GameProfile profile = new GameProfile(UUID.randomUUID(), null);
		profile.getProperties().put("textures", costume.texture);
		Field profileField = null;
		try {
			profileField = headMeta.getClass().getDeclaredField("profile");
			profileField.setAccessible(true);
			profileField.set(headMeta, profile);
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		head.setItemMeta(headMeta);
		return head;
	}
	
}
