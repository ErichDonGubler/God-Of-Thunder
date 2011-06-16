package com.KoryuObihiro.bukkit.godofthunder;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.config.ConfigurationNode;

public class GOTPlayerConfiguration 
{	
	private boolean succeededLoading = false;
	private GodOfThunder plugin;
	private Player player;

	private HashMap<Material, LightningType> bindList = new HashMap<Material, LightningType>();
	private HashMap<LightningType, Integer> typeAttributes = new HashMap<LightningType, Integer>();
	
	public GOTPlayerConfiguration(GodOfThunder plugin, Player player)
	{
		this.plugin = plugin;
		this.player = player;
		succeededLoading = load();
	}
	
	public boolean isLoaded(){ return succeededLoading;}
	
	public boolean setAttribute(LightningType lightningType, int input, boolean forLoad, boolean shouldSave)
	{
		int limit = lightningType.getDefaultAttribute();
		
		try
		{
			limit = plugin.typeLimits.get(player.getWorld()).get(lightningType);
		}
		catch(NullPointerException e)
		{
			GodOfThunder.log.warning("No configured limit found for lightning type \"" + lightningType.getTypeString() 
					+ "\" in world " + player.getWorld().getName() + " - defaulting to " 
					+ Integer.toString(limit));
		}
		
		if(lightningType.shouldBeConfigured() && input < 1)
		{
			player.sendMessage(ChatColor.RED + "[GoT] Error: Input must be a positive integer.");
			if(forLoad)
				setAttribute(lightningType, plugin.typeLimits.get(player.getWorld()).get(lightningType), false, shouldSave);
			else return false;
		}
		
		else if(input > limit)
		{
			if(forLoad)
				setAttribute(lightningType, limit, false, shouldSave);
			else player.sendMessage(ChatColor.YELLOW + "[GoT] Input greater than configured limit - defaulting to " + limit);
			return false;
		}
		if(typeAttributes.containsKey(lightningType))
			typeAttributes.remove(lightningType);
		typeAttributes.put(lightningType, input);
		if(!forLoad) player.sendMessage(ChatColor.GREEN + "[GoT] \"" + lightningType.getTypeString() + "\" attribute set to " + input);
		if(shouldSave) save();
		return true;
	}

	public boolean isBound(Material material){ return bindList.containsKey(material);}
	public boolean isBound(LightningType lightningType){ return bindList.containsValue(lightningType);}

	public void bindMaterialToLightningType(Material material, LightningType lightningType)
	{
		if(material.equals(Material.AIR))
		{
			player.sendMessage(ChatColor.RED + "[GoT] Can't bind to your fists. :(");
			return;
		}
		if(bindList.get(material) == lightningType)
		{
			player.sendMessage(ChatColor.RED + "[GoT] This bind already exists!");
			return;
		}
		if(bindList.containsValue(lightningType))
		{
			Material currentlyBound = null;
			for(Material materialKey : bindList.keySet())
				if(materialKey != null && bindList.get(materialKey) == lightningType)
					currentlyBound = materialKey;
			player.sendMessage(ChatColor.YELLOW + "Warning: bind of type " +
					lightningType.getTypeString() +  " already exists for " + currentlyBound.name());
		}
		if(bindList.containsKey(material))
		{
			player.sendMessage(ChatColor.YELLOW + "Warning: overriding existing bind of type \"" 
					+ bindList.get(material).getTypeString() + "\"");
			bindList.remove(material);
		}
		bindList.put(material, lightningType);
		player.sendMessage(ChatColor.GREEN + "\"" + lightningType.getTypeString() 
				+ "\"-type lightning bound to " + material.name());
		save();
	}
	
	public boolean unbindAll()
	{
		boolean unboundSomething = false;
		for(Material material : bindList.keySet())
		{
			plugin.config.setProperty(player.getWorld().getName() + ".players." + player.getName() 
					+ ".binds." + bindList.get(material).getTypeString(), "");
			unboundSomething = true;
		}
		bindList.clear();
		if(unboundSomething)
		{
			player.sendMessage(ChatColor.GREEN + "Removed all binds.");
			save();
		}
		else player.sendMessage(ChatColor.RED + "[GoT] Error: no binds to remove!");
		return unboundSomething;
	}
	public void unbind(Material material)
	{
		if(bindList.containsKey(material))
		{
			plugin.config.setProperty(player.getWorld().getName() + ".players." + player.getName() 
					+ ".binds." + bindList.get(material).getTypeString(), "");
			player.sendMessage(ChatColor.GREEN + "[GoT] Removed bind " + material.name() + " for type \"" + bindList.get(material).getTypeString() + "\"");
			bindList.remove(material);
			return;
		}
		player.sendMessage(ChatColor.RED + "[GoT] No bind found for material " + material.name());
		save();
	}
	
	public void unbind(LightningType lightningType)
	{
		if(bindList.containsValue(lightningType))
			for(Material material : bindList.keySet())
				if(bindList.get(material).equals(lightningType))
				{
					bindList.remove(material);
					plugin.config.setProperty(player.getWorld().getName() + ".players." + player.getName() 
							+ ".binds." + lightningType.getTypeString(), "");
					player.sendMessage(ChatColor.GREEN + "[GoT] Removed all bind " + material.name() + " for type \"" + lightningType.getTypeString() + "\"");
					return;
				}
		player.sendMessage(ChatColor.RED + "[GoT] No bind found for type \"" + lightningType.getTypeString() + "\"");
	}
	
	public LightningType getBoundLightningType(Material material){ return bindList.get(material);}

	public int getTypeAttribute(LightningType lightningType)
	{ 
		if(!lightningType.shouldBeConfigured()) return 0;
		return typeAttributes.get(lightningType);
	}
	
	public boolean load()
	{
		if(player == null) 
		{
			GodOfThunder.log.severe("[GoT] Null player passed! What did you DO?");
			return false;
		}
		if(plugin.config == null) 
		{
			GodOfThunder.log.severe("[GoT] Couldn't get config! Cannot load player.");
			return false;
		}
		ConfigurationNode playerNode = plugin.config.getNode(player.getWorld().getName() + ".players." + player.getName());
		if(playerNode == null)
			playerNode = writeDefaults();

		//get settings of the world
		for(LightningType lightningType : LightningType.values())
		{
			int attribute;
			String materialString = "";
			try
			{	
				materialString = (playerNode.getProperty("binds." + lightningType.getTypeString()) != null?(String)playerNode.getProperty("binds." + lightningType.getTypeString()):"");
				if(!materialString.isEmpty()) 
				{
					Material material = Material.matchMaterial(materialString);
					if(material != null) bindList.put(material, lightningType);
					else throw(new NumberFormatException());
				}
			}
			catch(NumberFormatException e)
			{
				
			}
			catch(Exception e)
			{
				GodOfThunder.log.severe("[GoT] Couldn't read player's bind \"" + materialString + "\"");
			}
			if(!lightningType.shouldBeConfigured()) continue;
			try
			{
				attribute = (Integer)playerNode.getProperty(lightningType.getTypeString());
				setAttribute(lightningType, attribute, true, false);
			}
			catch(Exception e)
			{
				GodOfThunder.log.severe("[GoT] Couldn't read player \"" + player.getName() + "\"'s attribute for type \"" + lightningType.getTypeString() + "\"");
				typeAttributes.put(lightningType, lightningType.getDefaultAttribute());//Necessary?
			}
		}
		return true;
	}
	
	private ConfigurationNode writeDefaults() 
	{
		String configReference = player.getWorld().getName() + ".players." + player.getName();
		GodOfThunder.log.info("Player \"" + player.getName() + "\" not found in config for world \"" + player.getWorld().getName() + ", generating a default one ...");
		for(LightningType lightningType : LightningType.values())
		{
			plugin.config.setProperty(configReference + ".binds." + lightningType.getTypeString(), "");
			if(!lightningType.shouldBeConfigured()) continue;
			plugin.config.setProperty(configReference + "." + lightningType.getTypeString(), plugin.typeLimits.get(player.getWorld()).get(lightningType));
		}
		plugin.config.save();
		plugin.config.load();
		return plugin.config.getNode(configReference);
	}
	
	public void save() //FIXME
	{
		String playerReference = player.getWorld().getName() + ".players." + player.getName();
		for(LightningType lightningType : LightningType.values())
		{
			if(bindList.containsValue(lightningType))
			{
				Material material = getBoundMaterial(lightningType);
				if(material != null && bindList.get(material) == lightningType)
					plugin.config.setProperty(playerReference + ".binds." + lightningType.getTypeString(), material.name());
			}
			else plugin.config.setProperty(playerReference + ".binds." + lightningType.getTypeString(), null);

			if(!lightningType.shouldBeConfigured()) continue;
			if(typeAttributes.containsKey(lightningType))
				plugin.config.setProperty(playerReference + "." + lightningType.getTypeString(), typeAttributes.get(lightningType));
		}
		plugin.config.save();
	}

	public Material getBoundMaterial(LightningType lightningType) 
	{
		for(Material material : bindList.keySet())
			if(material != null && bindList.get(material) == lightningType)
				return material;
		return null;
	}

}
