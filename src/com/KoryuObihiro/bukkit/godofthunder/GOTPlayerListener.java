package com.KoryuObihiro.bukkit.godofthunder;



import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.ChatColor;


public class GOTPlayerListener extends PlayerListener
{
//Members
	private GodOfThunder plugin;
	//Constructors	
	public GOTPlayerListener(GodOfThunder plugin) 
	{
		this.plugin = plugin;
	}
	
//Functions
	@Override
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		plugin.tryPower(player, event);
	}
	
	@Override
	public void onPlayerEggThrow(PlayerEggThrowEvent event)
	{
		Player player = event.getPlayer();
		player.sendMessage(ChatColor.GREEN + "asdf");
		plugin.egg_Change(player, event);
	}
}
