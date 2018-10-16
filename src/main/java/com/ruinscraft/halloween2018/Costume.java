package com.ruinscraft.halloween2018;

import com.mojang.authlib.properties.Property;

public class Costume {

	public final String name;
	public final Property texture;
	
	public Costume(String name, Property texture) {
		this.name = name;
		this.texture = texture;
	}
	
}
