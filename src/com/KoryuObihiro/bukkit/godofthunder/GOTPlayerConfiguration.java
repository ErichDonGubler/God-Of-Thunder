package com.KoryuObihiro.bukkit.godofthunder;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.util.config.ConfigurationNode;

import org.bukkit.ChatColor;
import org.bukkit.Material;

public class GOTPlayerConfiguration 
{	
	private GodOfThunder plugin;
	private Player player;

	private HashMap<Material, LightningType> bindList = new HashMap<Material, LightningType>();
	private HashMap<LightningType, Integer> typeAttributes = new HashMap<LightningType, Integer>();
	
	public GOTPlayerConfiguration(GodOfThunder plugin, Player player, ConfigurationNode worldNode)
	{
		//TODO toolbinds by passing string
		this.plugin = plugin;
		//TODO check for file, create new one if no filename for the same player is found
		
		ConfigurationNode playerNode = worldNode.getNode(world.getName());
		if(playerNode.equals(null))
			playerNode = writeDefaults(world);
	}
	
	public boolean setAttribute(LightningType lightningType, int input)
	{
		if(input < 0)
		{
			player.sendMessage(ChatColor.RED + "[GoT] Error: negative attribute.");
			return false;
		}
		else if(input > plugin.typeLimits.get(player.getWorld()).get(lightningType))
		{
			player.sendMessage(ChatColor.RED + "[GoT] Error: Input greater than configured limit.");
			return false;
		}
		if(typeAttributes.containsKey(lightningType))
			typeAttributes.remove(lightningType);
		typeAttributes.put(lightningType, input);
		player.sendMessage(ChatColor.GREEN + "[GoT] \"" + lightningType.getTypeString() + "\" attribute set to " + input);
		return true;
	}


	public boolean containsKey(Material material){ return bindList.containsKey(material);}

	public void bindMaterialToLightningType(Material material, LightningType lightningType)
	{
		if(bindList.containsValue(lightningType))
			player.sendMessage(ChatColor.YELLOW + "[GoT] Warning: bind of type " +
					lightningType.getTypeString() +  " already exists.");
		if(bindList.containsKey(material))
		{
			player.sendMessage(ChatColor.YELLOW + "[GoT] Warning: overriding existing bind of type \"" 
					+ bindList.get(material).getTypeString() + "\"");
			bindList.remove(material);
		}
		bindList.put(material, lightningType);
		
	}
	
	public boolean unbindAll()
	{
		boolean unboundSomething = false;
		for(Material material : bindList.keySet())
		{
			bindList.remove(material);
			unboundSomething = true;
		}
		if(unboundSomething) player.sendMessage(ChatColor.GREEN + "Removed all binds.");
		else player.sendMessage(ChatColor.RED + "[GoT] Error: no binds to remove!");
		return unboundSomething;
	}
	public void unbind(Material material)
	{
		if(bindList.containsKey(material))
		{
			player.sendMessage(ChatColor.GREEN + "[GoT] Removed bind for type \"" + bindList.get(material).getTypeString() + "\"");
			bindList.remove(material);
			return;
		}
		player.sendMessage(ChatColor.RED + "[GoT] No bind found for material " + material.name());
	}
	
	public void unbind(LightningType lightningType)
	{
		if(bindList.containsValue(lightningType))
		{
			for(Material material : bindList.keySet())
				if(bindList.get(material).equals(lightningType))
					bindList.remove(material);
			player.sendMessage(ChatColor.GREEN + "[GoT] Removed all binds for type \"" + lightningType.getTypeString() + "\"");
		}
		player.sendMessage(ChatColor.RED + "[GoT] No bind found for type \"" + lightningType.getTypeString() + "\"");
	}
	
	public LightningType getBoundLightningType(Material material){ return bindList.get(material);}

	public int getTypeAttribute(LightningType lightningType){ return typeAttributes.get(lightningType);}
	
	public void save()
	{
		
	}

}
