package com.KoryuObihiro.bukkit.godofthunder;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;
import org.bukkit.util.config.ConfigurationNode;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

/**
 * "God of Thunder" for Bukkit
 * 
 * @author Erich Gubler
 * 
 */
public class GodOfThunder extends JavaPlugin{
	private final GOTPlayerListener playerListener = new GOTPlayerListener(this);
	public static Logger log = Logger.getLogger("Minecraft");
	public static PermissionHandler Permissions = null;
	Configuration config;
	
	public final HashMap<World, HashMap<LightningType, Short>> durabilityCosts = new HashMap<World, HashMap<LightningType, Short>>();
	public final HashMap<World, HashMap<LightningType, Integer>> typeLimits = new HashMap<World, HashMap<LightningType, Integer>>();
	public final HashMap<String, GOTPlayerConfiguration> playerConfigs = new HashMap<String, GOTPlayerConfiguration>();
	
	public HashSet<Byte> nonStrikableBlocks = new HashSet<Byte>();
	

	//TODO Deregister when Bukkit supports
////////////////////////// INITIALIZATION ///////////////////////////////
	@Override
	public void onEnable() 
	{
		//attempt to find permissions
		Plugin test = getServer().getPluginManager().getPlugin("Permissions");
		if (test != null)
		{
			GodOfThunder.Permissions = ((Permissions)test).getHandler();
			log.info("["+getDescription().getName()+"] " + this.getDescription().getVersion() 
					+ " enabled [Permissions v" + test.getDescription().getVersion() + " active]");
		}
		else
			log.info("["+getDescription().getName()+"] " + this.getDescription().getVersion() 
					+ " enabled [Permissions not found]");
		
		//register plugin-related stuff with the server's plugin manager
		getServer().getPluginManager().registerEvent(Event.Type.PLAYER_JOIN, playerListener, Event.Priority.Normal, this);
		getServer().getPluginManager().registerEvent(Event.Type.PLAYER_QUIT, playerListener, Event.Priority.Normal, this);
		getServer().getPluginManager().registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Event.Priority.Normal, this);
		getServer().getPluginManager().registerEvent(Event.Type.PLAYER_TELEPORT, playerListener, Event.Priority.Normal, this);
		
		//populate nonStrikableBlocks
		nonStrikableBlocks.add((byte)Material.AIR.getId() );
		nonStrikableBlocks.add((byte)Material.GLASS.getId());
		nonStrikableBlocks.add((byte)Material.LADDER.getId());
		nonStrikableBlocks.add((byte)Material.TORCH.getId());
		nonStrikableBlocks.add((byte)Material.REDSTONE_TORCH_ON.getId());
		nonStrikableBlocks.add((byte)Material.REDSTONE_TORCH_OFF.getId());
		nonStrikableBlocks.add((byte)Material.REDSTONE_WIRE.getId());
		nonStrikableBlocks.add((byte)Material.STATIONARY_WATER.getId());
		nonStrikableBlocks.add((byte)Material.STATIONARY_LAVA.getId());
		nonStrikableBlocks.add((byte)Material.STONE_PLATE.getId());
		nonStrikableBlocks.add((byte)Material.WATER.getId());
		nonStrikableBlocks.add((byte)Material.YELLOW_FLOWER.getId());
		nonStrikableBlocks.add((byte)Material.RED_ROSE.getId());
		nonStrikableBlocks.add((byte)Material.BED_BLOCK.getId());
		nonStrikableBlocks.add((byte)Material.FIRE.getId());
		nonStrikableBlocks.add((byte)Material.LEVER.getId());

		config = getConfiguration();
		reload();
	}
	private void reload() 
	{
		for(GOTPlayerConfiguration playerConfig : playerConfigs.values())
			playerConfig.save();
		playerConfigs.clear();

		if(!(new File("plugins\\GodOfThunder", "config.yml")).exists())
			config.save();
		config.load();

		HashMap<LightningType, Integer> limitMap = null;
		HashMap<LightningType, Short> durabilityMap = null;
		//TODO Remove when the ghettoness is done.
		for(World world : getServer().getWorlds())
		{
			limitMap = new HashMap<LightningType, Integer>();
			durabilityMap = new HashMap<LightningType, Short>();
			
			//load world settings
			ConfigurationNode worldNode = config.getNode(world.getName());
			if(worldNode == null)
				worldNode = writeDefaults(world);
			
			//get settings of the world
			for(LightningType lightningType : LightningType.values())
			{
				try
				{
					limitMap.put(lightningType, worldNode.getInt(lightningType.getTypeString() + ".limit", lightningType.getDefaultAttribute()));
				}
				catch(Exception e)
				{
					e.printStackTrace();
					log.severe("");
				}
				try
				{
					durabilityMap.put(lightningType, (short)worldNode.getInt(lightningType.getTypeString() + ".durability", lightningType.getDefaultAttribute()));
				}
				catch(Exception e)
				{
					e.printStackTrace();
					log.severe("Could not load durability cost for " + world.getName() + "." + lightningType.getTypeString());
				}
			}
			
			typeLimits.put(world, limitMap);
			durabilityCosts.put(world, durabilityMap);
			//load settings of players on the server}
			for(Player player : world.getPlayers())
				playerConfigs.put(player.getName(), new GOTPlayerConfiguration(this, player));
		}
		
	}

	private ConfigurationNode writeDefaults(World world) 
	{
		String worldName = world.getName();
		log.info("World \"" + worldName + "\" not found in config, generating a default one...");
		for(LightningType lightningType : LightningType.values())
		{
			config.setProperty(worldName + "." + lightningType.getTypeString() + ".durability", 0); //FIXME Figure out durability stuffs, put 'er in the enum.
			if(lightningType.equals(LightningType.NORMAL)) continue;
			config.setProperty(worldName + "." + lightningType.getTypeString() + ".limit", lightningType.getDefaultAttribute());
		}
		config.save();
		config.load();
		return config.getNode(worldName);
	}
	
	@Override
	public void onDisable() 
	{
		log.info("["+getDescription().getName()+"] disabled.");	
		//configs.clear();
		for(GOTPlayerConfiguration playerConfig : playerConfigs.values())
			playerConfig.save();
	}
	
///////////////////// COMMAND HANDLING //////////////////////////////
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		//debugging		
		if ((label.equalsIgnoreCase("GodOfThunder") || label.equalsIgnoreCase("got")) && sender instanceof Player)
		{
			Player player = (Player)sender;
			if(args.length >= 0)
			{
				if(playerConfigs.get(player.getName()).isLoaded())
				{
					if(hasPermission(player, "got.use"))
					{
						if(args.length == 1)
						{
							if (args[0].equalsIgnoreCase("unbind") || args[0].equalsIgnoreCase("u"))
								playerConfigs.get(player.getName()).unbind(player.getItemInHand().getType());
							else if(args[0].equalsIgnoreCase("check") || args[0].equalsIgnoreCase("c")) //TODO Check other worlds soon?
							{
								sendWorldConfig(player, player.getWorld());
							}
							else if(args[0].equalsIgnoreCase("reload"))
							{
								if(hasPermission(player, "got.reload"))
								{
									reload();
									player.sendMessage(ChatColor.GREEN + "[GoT] Reloaded!");
								}
								else player.sendMessage(ChatColor.RED + "You don't have permission to do that.");
							}
							else if(args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("s") || args[0].equalsIgnoreCase("bind")  || args[0].equalsIgnoreCase("b"))
								player.sendMessage(ChatColor.RED + "[GoT] Error: expected lightning type.");
							return true;
						}
						else if(args.length == 2)
						{
							boolean didSomething = false;
							if(args[0].equalsIgnoreCase("unbind") || args[0].equalsIgnoreCase("u"))
							{
								if(args[1].equalsIgnoreCase("all"))
									playerConfigs.get(player.getName()).unbindAll();
								else 
								{
									for(LightningType lightningType : LightningType.values())
										if(args[1].equalsIgnoreCase(lightningType.getTypeString()))
										{
											playerConfigs.get(player.getName()).unbind(lightningType);
											didSomething = true;
										}
									if(!didSomething) player.sendMessage(ChatColor.RED + "[GoT] Error: Invalid lightning type \"" + args[1] + "\"");
								}
								return true;
							}
							else if(args[0].equalsIgnoreCase("bind") || args[0].equalsIgnoreCase("b"))
							{
								for(LightningType lightningType : LightningType.values())
									if(args[1].equalsIgnoreCase(lightningType.getTypeString()))
									{
										playerConfigs.get(player.getName()).bindMaterialToLightningType(player.getItemInHand().getType(), lightningType);
										didSomething = true;
									}
								if(!didSomething) player.sendMessage(ChatColor.RED + "[GoT] Error: Invalid lightning type \"" + args[1] + "\"");
								return true;
							}
							else if(args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("s"))
							{
								for(LightningType lightningType : LightningType.values())
									if(args[1].equalsIgnoreCase(lightningType.getTypeString()))
									{
										player.sendMessage(ChatColor.RED + "[GoT] Error: expected integer for set");
										return true;
									}
								player.sendMessage(ChatColor.RED + "[GoT] Error: Type \"" + args[1] + "\" invalid.");
								return true;
							}
						}
						else if(args.length == 3)
						{
							boolean didSomething = false;
							if(args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("s"))
							{
								for(LightningType lightningType : LightningType.values())
									if(args[1].equalsIgnoreCase(lightningType.getTypeString()))
									{
										try{ didSomething = playerConfigs.get(player.getName()).setAttribute(lightningType, Integer.parseInt(args[2]), false, true);}
										catch(Exception e){ player.sendMessage(ChatColor.RED + "[GoT] Error: expected integer input");}
										break;
									}
								if(!didSomething) player.sendMessage(ChatColor.RED + "[GoT] Error: Type \"" + args[1] + "\" invalid.");	
								return true;
							}
							else if(args[0].equalsIgnoreCase("bind") || args[0].equalsIgnoreCase("b"))
							{
								try
								{
									int attribute = Integer.parseInt(args[2]);
									for(LightningType lightningType : LightningType.values())
										if(args[1].equalsIgnoreCase(lightningType.getTypeString()))
										{
											playerConfigs.get(player.getName()).setAttribute(lightningType, attribute, false, true);
											playerConfigs.get(player.getName()).bindMaterialToLightningType(player.getItemInHand().getType(), lightningType);
											return true;
										}
								}
								catch(NumberFormatException e)
								{
									player.sendMessage(ChatColor.RED + "[GoT] Error: expected integer input");
								}
								player.sendMessage("[GoT] Error: Invalid lightning type \"" + args[1] + "\"");
								return true;
							}
						}
					}
					else
					{
						player.sendMessage(ChatColor.RED + "[GoT] You don't have permissions to use GoT.");
						return true;
					}
				}
				else
				{
					player.sendMessage(ChatColor.RED + "[GoT] Not configured in GoT - try rejoining.");
					return true;
				}
			}
			sendUsage(player);
		}
		else sendUsage(null);
		return true;
	}	

	private boolean sendWorldConfig(Player player, World world) 
	{
		HashMap<LightningType, Integer> limitMap = typeLimits.get(world);
		if(player != null)
		{
		    player.sendMessage(ChatColor.GOLD + "[GoT] Settings for world " + ChatColor.DARK_PURPLE + world.getName() 
		    		+ ChatColor.GOLD + ":");
		    player.sendMessage(ChatColor.YELLOW + "(" + ChatColor.GREEN + "Green" + ChatColor.YELLOW + ") indicates usability)");
		    for(LightningType lightningType : LightningType.values())
		    {
		    	String boundMaterialString = "";
		    	if(playerConfigs.get(player.getName()).isBound(lightningType))
		    	{
		    		boundMaterialString = playerConfigs.get(player.getName()).getBoundMaterial(lightningType).name();
		    	}
	    		boolean hasPermission = hasPermission(player, "got.use." + lightningType.getTypeString());
		    	if(lightningType.shouldBeConfigured())
		    		player.sendMessage((hasPermission?ChatColor.GREEN:ChatColor.AQUA) + lightningType.getTypeString() 
		    				+ ChatColor.BLUE + " (Limit " + Integer.toString(limitMap.get(lightningType)) + ")"
		    				+ (hasPermission
		    						?(ChatColor.GREEN + " Player settings: " 
		    							+ Integer.toString(playerConfigs.get(player.getName()).getTypeAttribute(lightningType))
		    							+ ChatColor.GOLD + "(" + boundMaterialString + ")")
		    						:""));
		    	else player.sendMessage((hasPermission
							    			?ChatColor.GREEN + lightningType.getTypeString() + ChatColor.GOLD + "(" + boundMaterialString + ")"
							    			:ChatColor.AQUA + lightningType.getTypeString()));
		    }
		}
		    
		else 
		{
			log.info("[God Of Thunder] Limit settings for world " + world.getName() + ":\n");
		    for(LightningType lightningType : LightningType.values())
				if(!lightningType.shouldBeConfigured())
		    		log.info(lightningType.getTypeString() + ": " + limitMap.get(lightningType));
		}
	    
	    return true;
	}

	private void sendUsage(Player player) 
	{
		if(player != null)
		{
			player.sendMessage(ChatColor.LIGHT_PURPLE + "God Of Thunder commands: ");
			player.sendMessage(ChatColor.LIGHT_PURPLE + "/godofthunder (alias /got) - brings up this help message");
			if(hasPermission(player, "got.use"))
			{
				player.sendMessage(ChatColor.LIGHT_PURPLE + "/got check (alias c) - see world configuration");
				player.sendMessage(ChatColor.LIGHT_PURPLE + "/got bind (alias b) (lightningType) [#value] - bind current item [and set attribute]");
				player.sendMessage(ChatColor.LIGHT_PURPLE + "/got unbind (alias u) [all | lightningType] - unbind current item [or lightningType]");
				player.sendMessage(ChatColor.LIGHT_PURPLE + "/got set (lightningType) (#value) - set lightning type attribute (refer to \"check\")");
			}
			if(hasPermission(player, "got.reload")) 
				player.sendMessage(ChatColor.LIGHT_PURPLE + "/got reload - reload plugin from configuration file");
			else player.sendMessage(ChatColor.RED + "No permissions. :(");
		}
		else log.info("[God Of Thunder] Error: must be a player to use GoT commands."); //TODO Stop being lame here.
	}
	  
//////////////////// GOT EXECUTION /////////////////////////
	  
	public void tryStrike(PlayerInteractEvent event)
	{
		Player player = event.getPlayer(); 
		World world = player.getWorld();
			if(playerConfigs.get(player.getName()).isBound(player.getItemInHand().getType()))
			{
				LightningType lightningType = playerConfigs.get(player.getName()).getBoundLightningType(player.getItemInHand().getType());
				if(hasPermission(player, "got.type." + lightningType.getTypeString()))
				{
					int commandModifier = playerConfigs.get(player.getName()).getTypeAttribute(lightningType);
					Location thunderLoc = findStrikeArea(player);
					lightningType.strikeLightning(player, thunderLoc, commandModifier);
					degradeWeapon(player, lightningType);
				}
			}
			else if(hasPermission(player, "got.bucket"))
			{
				//TODO Refactor this...but definitely not high-priority.
				if(player.getItemInHand().getType().equals(Material.WATER_BUCKET)
						&& !world.isThundering())
				{
					player.setItemInHand(new ItemStack(Material.BUCKET, 1));
					world.setStorm(true);
					world.setThundering(true);
				}
				else if(player.getItemInHand().getType().equals(Material.BUCKET)
							&& world.isThundering())
				{
					player.setItemInHand(new ItemStack(Material.WATER_BUCKET, 1));
					world.setStorm(false);
					world.setThundering(false);
				}
			}
			
	}

	private Location findStrikeArea(Player player) 
	{
		Location strikeLocation = player.getTargetBlock(this.nonStrikableBlocks, 0).getLocation();
		Location tryHere = new Location(player.getWorld(), strikeLocation.getBlockX(), 0, strikeLocation.getBlockZ());
		
		for(int i = 127; i > 0; i--)
		{
			tryHere.setY(i);
			if(!nonStrikableBlocks.contains((byte)tryHere.getBlock().getTypeId()))
			{
				strikeLocation.setY(i + 1);
				break;
			}
		}
		return strikeLocation;
	}
	private void degradeWeapon(Player player, LightningType lightningType) //TODO refer to world configs
	{
		if(durabilityCosts.get(player.getWorld()).containsKey(lightningType))
			player.getItemInHand().setDurability((short)(player.getItemInHand().getDurability() 
					- durabilityCosts.get(player.getWorld()).get(lightningType)));
	}

/////////////////// HELPER FUNCTIONS ////////////////////////////
	//check for Permissions
	public static boolean hasPermission(Player player, String permission)
	{
		if (GodOfThunder.Permissions != null)
		{
			if (GodOfThunder.Permissions.has(player, permission)) 
				return true;
			return false;
		}
		return player.isOp();
	}
}
