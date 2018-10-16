package com.ruinscraft.halloween2018;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.ruinscraft.halloween2018.traits.NPCCostumeSelectorTrait;
import com.ruinscraft.halloween2018.traits.NPCPowderGiverTrait;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.skin.SkinnableEntity;

public class Halloween2018Command implements CommandExecutor {

	private static final String NPC_SELECTOR_NAME = "Costume Selector";
	private static final String NPC_POWDER_GIVER_NAME = "Trick. Or Treat?";
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return true;
		}

		Player player = (Player) sender;

		if (!player.hasPermission("ruinscraft.command.halloween")) {
			return true;
		}

		if (args.length < 1) {
			player.sendMessage("Enter a powder that you want this npc to give out or make them a selector");
			player.sendMessage("/halloween <powdername>|selector");
			return true;
		}

		String powderName = args[0];
		Location location = player.getLocation();
		NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "");
		
		npc.spawn(location);
		
		Costume npcCostume = Halloween2018Plugin.getInstance().getCostumeHandler().getRandomCostume();
		SkinnableEntity skinnable = npc.getEntity() instanceof SkinnableEntity ? (SkinnableEntity) npc.getEntity() : null;

		if (skinnable != null) {
			skinnable.setSkinPersistent("halloween", npcCostume.texture.getSignature(), npcCostume.texture.getValue());
		}
		
		if (powderName.equalsIgnoreCase("selector")) {
			npc.setName(NPC_SELECTOR_NAME);
			npc.addTrait(new NPCCostumeSelectorTrait());
			player.sendMessage("NPC costume selector added");
			return true;
		}

		npc.setName(NPC_POWDER_GIVER_NAME);
		npc.addTrait(new NPCPowderGiverTrait(powderName));
		player.sendMessage("NPC added id:" + npc.getId() + " giving out powder: " + powderName);

		return true;
	}

}
