package com.KoryuObihiro.bukkit.godofthunder;

import org.bukkit.entity.CreatureType;

public enum LightningType 
{
	NORMAL("normal", false, null, "No config.", 0),
	EXPLOSIVE("explosive", false, null, "explosive power", 6),
	DIFFUSIVE("diffusive", false, null, "fire spread", 5),
	SUMMON_CREEPER("summon.creeper", true, CreatureType.CREEPER, "summons Creeper(s)", 1),
	SUMMON_PIGZOMBIE("summon.pigzombie", true, CreatureType.PIG_ZOMBIE, "summons PigZombie(s)", 1);
	
	private String typeString;
	private boolean isSummon;
	private CreatureType summonType;
	private String attributeString;
	private int defaultAttribute;
	
	LightningType(String typeString, boolean summonsSomething, CreatureType summonType, String attributeString, int defaultAttribute)
	{
		this.typeString = typeString;
		this.isSummon = summonsSomething;
		this.summonType = summonType;
		this.attributeString = attributeString;
		this.defaultAttribute = defaultAttribute;
	}
	
	public String getTypeString(){ return typeString;}
	public boolean summonsCreature(){ return isSummon;}
	public CreatureType getSummonType(){ return summonType;}
	public String getAttributeString(){ return attributeString;}
	public int getDefaultAttribute(){ return defaultAttribute;}
}
