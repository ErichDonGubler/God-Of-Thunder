package com.KoryuObihiro.bukkit.godofthunder;

import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;


public class GOTPlayerListener extends PlayerListener
{
	//TODO add file persistence
//Members
	private GodOfThunder plugin;
	//Constructors	
	public GOTPlayerListener(GodOfThunder plugin) 
	{
		this.plugin = plugin;
	}
	
//Functions
	@Override
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		if(GodOfThunder.hasPermission(event.getPlayer(), "got.use"))
		{
			String playerName = event.getPlayer().getName();
			plugin.playerConfigs.put(playerName, new GOTPlayerConfiguration(plugin, event.getPlayer()));
			if(!plugin.playerConfigs.get(playerName).isLoaded())
				plugin.playerConfigs.remove(playerName);
		}
	}
	
	@Override
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();
		if(plugin.playerConfigs.containsKey(player.getName())) //TODO Does the player hash evaluation change when the world changes?
		{
			plugin.playerConfigs.get(player.getName()).save();
			plugin.playerConfigs.remove(player.getName());
		}
	}
	
	@Override
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		if((event.getAction() == Action.RIGHT_CLICK_AIR) || event.getAction() == Action.RIGHT_CLICK_BLOCK)
		{
			if(plugin.playerConfigs.containsKey(event.getPlayer().getName()))
			{
				if(GodOfThunder.hasPermission(event.getPlayer(), "got.use"))
					plugin.tryStrike(event);
			}
		}
	}
	
	@Override
	public void onPlayerTeleport(PlayerTeleportEvent event)
	{
		if(!event.getFrom().getWorld().equals(event.getTo().getWorld()) && plugin.playerConfigs.containsKey(event.getPlayer()))
			plugin.playerConfigs.get(event.getPlayer().getName()).load();
	}
}
