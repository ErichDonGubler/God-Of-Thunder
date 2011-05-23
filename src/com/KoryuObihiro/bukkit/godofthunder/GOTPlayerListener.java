package com.KoryuObihiro.bukkit.godofthunder;



import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.ChatColor;


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
			plugin.playerConfigs.put(event.getPlayer(), new GOTPlayerConfiguration(plugin, event.getPlayer()));
	}
	
	@Override
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();
		if(plugin.playerConfigs.containsKey(player)) //TODO Does the player hash evaluation change when the world changes?
		{
			plugin.playerConfigs.get(player).save();
			plugin.playerConfigs.remove(player);
		}
	}
	@Override
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		plugin.tryStrike(event);
	}
}
