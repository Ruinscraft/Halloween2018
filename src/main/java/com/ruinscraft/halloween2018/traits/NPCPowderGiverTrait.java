package com.ruinscraft.halloween2018.traits;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

@TraitName("powdergiver")
public class NPCPowderGiverTrait extends Trait {

	@Persist
	private String powder;
	
	public NPCPowderGiverTrait() {
		super("powdergiver");
	}
	
	public NPCPowderGiverTrait(String powder) {
		super("powdergiver");
		this.powder = powder;
	}
	
	public String getPowder() {
		return powder;
	}
	
}
