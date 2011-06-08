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
		succeededLoading = reload();
	}
	
	public boolean isLoaded(){ return succeededLoading;}
	
	public boolean setAttribute(LightningType lightningType, int input)
	{
		//TODO Handle properties that aren't there.
		boolean withinLimits = true;
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
		
		if(input < 0)
		{
			player.sendMessage(ChatColor.RED + "[GoT] Error: negative attribute.");
			return false;
		}
		
		else if(input > limit)
		{
			withinLimits = false;
			input = limit;
			player.sendMessage(ChatColor.RED + "[GoT] Error: Input greater than configured limit.");
		}
		if(typeAttributes.containsKey(lightningType))
			typeAttributes.remove(lightningType);
		typeAttributes.put(lightningType, input);
		player.sendMessage((withinLimits?ChatColor.GREEN:ChatColor.RED) 
				+ "[GoT] \"" + lightningType.getTypeString() + "\" attribute set to " + input);
		return true;
	}


	public boolean containsKey(Material material){ return bindList.containsKey(material);}

	public void bindMaterialToLightningType(Material material, LightningType lightningType)
	{
		boolean alreadyBoundMaterial = false;
		if(bindList.containsValue(lightningType))
			player.sendMessage(ChatColor.YELLOW + "Warning: bind of type " +
					lightningType.getTypeString() +  " already exists.");
		if(bindList.containsKey(material))
		{
			alreadyBoundMaterial = true;
			player.sendMessage(ChatColor.YELLOW + "Warning: overriding existing bind of type \"" 
					+ bindList.get(material).getTypeString() + "\"");
			bindList.remove(material);
		}
		bindList.put(material, lightningType);
		if(!alreadyBoundMaterial)
			player.sendMessage(ChatColor.GREEN + "\"" + lightningType.getTypeString() 
					+ "\"-type lightning bound to " + material.name());
		
	}
	
	public boolean unbindAll()
	{
		boolean unboundSomething = false;
		for(Material material : bindList.keySet())
		{
			plugin.config.setProperty(player.getWorld().getName() + ".players." + player.getName() 
					+ ".binds." + bindList.get(material).getTypeString(), "");
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
			plugin.config.setProperty(player.getWorld().getName() + ".players." + player.getName() 
										+ ".binds." + bindList.get(material).getTypeString(), "");
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
				{
					bindList.remove(material);
					plugin.config.setProperty(player.getWorld().getName() + ".players." + player.getName() 
							+ ".binds." + lightningType.getTypeString(), "");
				}
			player.sendMessage(ChatColor.GREEN + "[GoT] Removed all binds for type \"" + lightningType.getTypeString() + "\"");
			return;
		}
		player.sendMessage(ChatColor.RED + "[GoT] No bind found for type \"" + lightningType.getTypeString() + "\"");
	}
	
	public LightningType getBoundLightningType(Material material){ return bindList.get(material);}

	public int getTypeAttribute(LightningType lightningType)
	{ 
		if(lightningType.equals(LightningType.NORMAL)) return 0;
		return typeAttributes.get(lightningType);
	}
	
	public boolean reload()
	{
		if(player == null) 
		{
			GodOfThunder.log.severe("[GoT] Cannot load player. Friiiiick"); //TODO Remove me
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
			try
			{
				typeAttributes.put(lightningType, playerNode.getInt(lightningType.getTypeString(), lightningType.getDefaultAttribute()));
				bindList.put(Material.matchMaterial(playerNode.getString("binds." + lightningType.getTypeString(), "")), lightningType);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				GodOfThunder.log.severe("LOLWUT");//FIXME
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
			if(lightningType.equals(LightningType.NORMAL)) continue;
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
				for(Object material : bindList.keySet().toArray())
						plugin.config.setProperty(playerReference + ".binds." + lightningType.getTypeString(), ((Material)material).toString());
			if(lightningType.equals(LightningType.NORMAL)) continue;
			if(typeAttributes.containsKey(lightningType))
				plugin.config.setProperty(playerReference + "." + lightningType.getTypeString(), lightningType.getDefaultAttribute());
		}
		plugin.config.save();
	}

}
