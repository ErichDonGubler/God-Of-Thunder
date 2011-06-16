package com.KoryuObihiro.bukkit.godofthunder;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public enum LightningType 
{
	NORMAL("normal", false, null, "No config.", 0, false),
	CHAIN("chain", false, null, "target radius", 8, true),
	EXPLOSIVE("explosive", false, null, "explosive power", 6, true),
	DIFFUSIVE("diffusive", false, null, "fire spread", 5, true),
	FAKE("fake", false, null, "No config.", 0, false),
	SUMMON_CREEPER("summon_creeper", true, CreatureType.CREEPER, "summon Creeper(s)", 1, true),
	SUMMON_PIGZOMBIE("summon_pigzombie", true, CreatureType.PIG_ZOMBIE, "summon PigZombie(s)", 1, true);
	
	private String typeString;
	private boolean isSummon;
	private CreatureType summonType;
	private String attributeString;
	private int defaultAttribute;
	private boolean shouldBeConfigured;
	
	LightningType(String typeString, boolean summonsSomething, CreatureType summonType, String attributeString, int defaultAttribute, boolean shouldBeConfigured)
	{
		this.typeString = typeString;
		this.isSummon = summonsSomething;
		this.summonType = summonType;
		this.attributeString = attributeString;
		this.defaultAttribute = defaultAttribute;
		this.shouldBeConfigured = shouldBeConfigured;
	}
	
	public String getTypeString(){ return typeString;}
	public boolean summonsCreature(){ return isSummon;}
	public CreatureType getSummonType(){ return summonType;}
	public String getAttributeString(){ return attributeString;}
	public int getDefaultAttribute(){ return defaultAttribute;}
	public boolean shouldBeConfigured(){ return shouldBeConfigured;}
	
	public void strikeLightning(Player player, Location strikeLocation, int commandModifier)
	{
		switch(this)
		{
			case NORMAL:
				player.getWorld().strikeLightning(strikeLocation);
				break;
				
			case CHAIN:
				for(Entity entity : player.getWorld().strikeLightningEffect(strikeLocation).getNearbyEntities(commandModifier, commandModifier, commandModifier))
					if(entity instanceof LivingEntity && !entity.equals(player))
						entity.getWorld().strikeLightning(entity.getLocation());
				break;
				
			case EXPLOSIVE:
				player.getWorld().strikeLightning(strikeLocation);
				if(commandModifier > 0)
					player.getWorld().createExplosion(strikeLocation.getX(), strikeLocation.getY(), strikeLocation.getZ(), commandModifier);
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
						if(block.getType().equals(Material.AIR) || block.getType().equals(Material.LEAVES))
							block.setType(Material.FIRE);
					
				}
	}

	private void strikeEffectandSummon(Location strikeLocation, CreatureType creatureType) 
	{
		Location creatureLocation = strikeLocation;
		creatureLocation.setY(strikeLocation.getY() + 1);
		
		strikeLocation.getWorld().strikeLightningEffect(strikeLocation);
		strikeLocation.getWorld().spawnCreature(creatureLocation, creatureType);
	}
}
