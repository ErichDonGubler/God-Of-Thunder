package com.KoryuObihiro.bukkit.godofthunder;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public enum LightningType 
{
	NORMAL(false, null, "No config.", 0, false),
	CHAIN(false, null, "target radius", 8, true),
	EXPLOSIVE(false, null, "explosive power", 6, true),
	DIFFUSIVE(false, null, "fire spread", 5, true),
	FAKE(false, null, "No config.", 0, false),
	SUMMON_CREEPER(true, CreatureType.CREEPER, "summon Creeper(s)", 1, true),
	SUMMON_PIGZOMBIE(true, CreatureType.PIG_ZOMBIE, "summon PigZombie(s)", 1, true);
	
	private boolean isSummon;
	private CreatureType summonType;
	private String attributeString;
	private int defaultAttribute;
	private boolean shouldBeConfigured;
	
	LightningType(boolean summonsSomething, CreatureType summonType, String attributeString, int defaultAttribute, boolean shouldBeConfigured)
	{
		this.isSummon = summonsSomething;
		this.summonType = summonType;
		this.attributeString = attributeString;
		this.defaultAttribute = defaultAttribute;
		this.shouldBeConfigured = shouldBeConfigured;
	}
	
	public String getTypeString(){ return name().toLowerCase();}
	public boolean summonsCreature(){ return isSummon;}
	public CreatureType getSummonType(){ return summonType;}
	public String getAttributeString(){ return attributeString;}
	public int getDefaultAttribute(){ return defaultAttribute;}
	public boolean shouldBeConfigured(){ return shouldBeConfigured;}
	
	public void strikeLightning(Location strikeLocation, int commandModifier)
	{
		switch(this)
		{
			case NORMAL:
				strikeLocation.getWorld().strikeLightning(strikeLocation);
				break;
				
			case CHAIN:
				int radius = commandModifier;
				for(Entity entity : strikeLocation.getWorld().getEntities())
				    if(entity instanceof LivingEntity)
				    {
				        Location entityBlockLocation = entity.getLocation();
				        int distance = Math.abs(strikeLocation.getBlockX() - entityBlockLocation.getBlockX())
				                        + Math.abs(strikeLocation.getBlockY() - entityBlockLocation.getBlockY())
				                        + Math.abs(strikeLocation.getBlockZ() - entityBlockLocation.getBlockZ());
				        if(distance <= radius)
				        	entity.getWorld().strikeLightning(entity.getLocation());
				    }
						
				break;
				
			case EXPLOSIVE:
				strikeLocation.getWorld().strikeLightning(strikeLocation);
				if(commandModifier > 0)
					strikeLocation.getWorld().createExplosion(strikeLocation.getX(), strikeLocation.getY(), strikeLocation.getZ(), commandModifier);
				break;
				
			case DIFFUSIVE:
				strikeLocation.getWorld().strikeLightningEffect(strikeLocation);
				generateFire(strikeLocation, commandModifier);
				break;
				
			case FAKE:
				strikeLocation.getWorld().strikeLightningEffect(strikeLocation);
				break;
				
			case SUMMON_CREEPER:
				strikeEffectandSummon(strikeLocation, CreatureType.CREEPER);
				break;
				
			case SUMMON_PIGZOMBIE:
				strikeEffectandSummon(strikeLocation, CreatureType.PIG_ZOMBIE);
				break;
				
				//TODO WARP! :D
		}
	}
	
	private void generateFire(Location strikeLocation, int commandModifier) 
	{
		World world = strikeLocation.getWorld();
		int origin_x = strikeLocation.getBlockX(), origin_y = strikeLocation.getBlockY(), origin_z = strikeLocation.getBlockZ();
		for(int i = 0; i < commandModifier; i++)
			for(int j = 0; j < commandModifier; j++)
				for(int k = 0; k < commandModifier; k++)
				{
					if(i + j + k > commandModifier) continue;
					Block[] blockArea = {world.getBlockAt(origin_x + i, origin_y + j, origin_z + k),
											world.getBlockAt(origin_x + i, origin_y + j, origin_z - k),
											world.getBlockAt(origin_x - i, origin_y + j, origin_z + k),
											world.getBlockAt(origin_x + i, origin_y - j, origin_z + k),
											world.getBlockAt(origin_x + i, origin_y - j, origin_z - k),
											world.getBlockAt(origin_x - i, origin_y - j, origin_z + k),
											world.getBlockAt(origin_x - i, origin_y - j, origin_z - k)};
					for(Block block : blockArea)
						switch(block.getType())
						{
							case AIR:
							case CAKE_BLOCK:
							case LEAVES:
							case LONG_GRASS:
							case RED_ROSE:
							case SNOW:
							case SUGAR_CANE_BLOCK:
							case WEB:
							case YELLOW_FLOWER:
								block.setType(Material.FIRE);								
						}
					
				}
	}

	private void strikeEffectandSummon(Location strikeLocation, CreatureType creatureType) 
	{
		Location creatureLocation = strikeLocation;
		creatureLocation.setY(strikeLocation.getY() + 1);
		
		strikeLocation.getWorld().strikeLightningEffect(strikeLocation);
		strikeLocation.getWorld().spawnCreature(creatureLocation, creatureType);
	}
	
	public static LightningType matchType(String key)
	{
		for(LightningType lightningType : LightningType.values())
			if(lightningType.name().equalsIgnoreCase(key))
				return lightningType;
		return null;
	}
}
