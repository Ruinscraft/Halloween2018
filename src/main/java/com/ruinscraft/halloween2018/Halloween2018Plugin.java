package com.ruinscraft.halloween2018;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.ruinscraft.halloween2018.traits.NPCCostumeSelectorTrait;
import com.ruinscraft.halloween2018.traits.NPCPowderGiverTrait;

import me.lucko.luckperms.api.LuckPermsApi;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.TraitInfo;

public class Halloween2018Plugin extends JavaPlugin {
	
	private static Halloween2018Plugin instance;
	
	private Halloween2018Database database;
	private CostumeHandler costumeHandler;
	private LuckPermsApi luckPermsApi;
	
	@Override
	public void onEnable() {
		instance = this;
		
		getDataFolder().mkdirs();
		
		saveDefaultConfig();
		
		RegisteredServiceProvider<LuckPermsApi> provider = Bukkit.getServicesManager().getRegistration(LuckPermsApi.class);
		luckPermsApi = provider.getProvider();
		
		CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(NPCCostumeSelectorTrait.class));
		CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(NPCPowderGiverTrait.class));
		
		costumeHandler = new CostumeHandler();
		
		getCommand("halloween").setExecutor(new Halloween2018Command());
		
		try {
			database = new Halloween2018Database(new File(getDataFolder(), "npcdb.db"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		getServer().getPluginManager().registerEvents(new NPCListener(), this);
		
		getServer().getScheduler().runTaskTimer(this, new PumpkinLightTask(), 5L, 20 * 1L);
	}
	
	@Override
	public void onDisable() {
		database.close();
		costumeHandler.clear();
		instance = null;
	}
	
	public Halloween2018Database getDatabase() {
		return database;
	}
	
	public CostumeHandler getCostumeHandler() {
		return costumeHandler;
	}
	
	public LuckPermsApi getLuckPermsApi() {
		return luckPermsApi;
	}
	
	public static Halloween2018Plugin getInstance() {
		return instance;
	}
	
}
