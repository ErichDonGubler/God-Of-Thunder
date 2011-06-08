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
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.CreatureType;
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
 * "LoftJump" for Bukkit
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
		//getServer().getPluginManager().registerEvent(Event.Type.PLAYER_EGG_THROW, playerListener, Event.Priority.Normal, this);
		
		//populate nonStrikableBlocks
		nonStrikableBlocks.add((byte)Material.AIR.getId() );
		nonStrikableBlocks.add((byte)Material.FENCE.getId());
		nonStrikableBlocks.add((byte)Material.GLASS.getId());
		nonStrikableBlocks.add((byte)Material.LADDER.getId());
		nonStrikableBlocks.add((byte)Material.TORCH.getId());
		nonStrikableBlocks.add((byte)Material.REDSTONE_TORCH_ON.getId());
		nonStrikableBlocks.add((byte)Material.REDSTONE_TORCH_OFF.getId());
		nonStrikableBlocks.add((byte)Material.REDSTONE_WIRE.getId());
		nonStrikableBlocks.add((byte)Material.STATIONARY_WATER.getId());
		nonStrikableBlocks.add((byte)Material.STONE_PLATE.getId());
		nonStrikableBlocks.add((byte)Material.WATER.getId());
		nonStrikableBlocks.add((byte)Material.YELLOW_FLOWER.getId());
		nonStrikableBlocks.add((byte)Material.RED_ROSE.getId());
		nonStrikableBlocks.add((byte)Material.BED_BLOCK.getId());
		nonStrikableBlocks.add((byte)Material.FIRE.getId());
		nonStrikableBlocks.add((byte)Material.LEVER.getId());
		
		reload();
	}
	private void reload() 
	{
		playerConfigs.clear();

		config = getConfiguration();
		File configFile = new File("plugins\\GodOfThunder", "config.yml");
		if(!configFile.exists())
		{
			config.save();
			config.load();
		}


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
							if (args[0].equalsIgnoreCase("unbind"))
							{
								playerConfigs.get(player.getName()).unbind(player.getItemInHand().getType());
								return true;
							}
							else if(args[0].equalsIgnoreCase("check")) //TODO Check other worlds soon?
							{
								//if(hasPermission(player, "got.check"))
									sendWorldConfig(player, player.getWorld());
								//else player.sendMessage(ChatColor.RED + "You don't have permission to do that.");
								return true;
							}
							else if(args[0].equalsIgnoreCase("reload"))
							{
								if(hasPermission(player, "got.reload")) reload();
								else player.sendMessage(ChatColor.RED + "You don't have permission to do that.");
								return true;
							}
						}
						else if(args.length == 2)
						{
							boolean didSomething = false;
							if(args[0].equalsIgnoreCase("unbind"))
							{
								if(args[1].equalsIgnoreCase("all"))
									playerConfigs.get(player.getName()).unbindAll();
								else 
								{
									for(LightningType lightningType : LightningType.values())
										if(args[0].equalsIgnoreCase(lightningType.getTypeString()))
										{
											playerConfigs.get(player.getName()).unbind(lightningType);
											didSomething = true;
										}
									if(!didSomething) player.sendMessage("[GoT] Error: Invalid lightning type \"" + args[1] + "\"");
								}
							}
							else if(args[0].equalsIgnoreCase("bind"))
							{
								for(LightningType lightningType : LightningType.values())
									if(args[1].equalsIgnoreCase(lightningType.getTypeString()))
									{
										playerConfigs.get(player.getName()).bindMaterialToLightningType(player.getItemInHand().getType(), lightningType);
										didSomething = true;
									}
								if(!didSomething) player.sendMessage("[GoT] Error: Invalid lightning type \"" + args[1] + "\"");
							}
							return true;
						}
						else if (args.length == 3 && args[0].equalsIgnoreCase("set"))
						{
							boolean setSomething = true;
							for(LightningType lightningType : LightningType.values())
								if(args[1].equalsIgnoreCase(lightningType.getTypeString()))
								{
									try{ playerConfigs.get(player.getName()).setAttribute(lightningType, Integer.parseInt(args[2]));}
									catch(Exception e)
									{
										player.sendMessage(ChatColor.RED + "[GoT] Error: expected integer input");
									}
									setSomething = true;
									break;
								}
							if(!setSomething) player.sendMessage(ChatColor.RED + "No matching lightning type!");
							return true;
						}
						sendUsage(player);
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
			else sendUsage(player);
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
		    	if(!lightningType.equals(LightningType.NORMAL))
		    	{
		    		boolean lolwut = hasPermission(player, "got.use." + lightningType.getTypeString());
		    		player.sendMessage((lolwut?ChatColor.GREEN:ChatColor.AQUA) + lightningType.getTypeString() 
		    				+ ChatColor.BLUE + " (Limit " + Integer.toString(limitMap.get(lightningType)) + ")"
		    				+ (lolwut
		    						?(ChatColor.GREEN + " Player settings: " 
		    							+ Integer.toString(playerConfigs.get(player.getName()).getTypeAttribute(lightningType)))
		    						:""));
		    	}
		}
		    
		else 
		{
			log.info("[God Of Thunder] Limit settings for world " + world.getName() + ":\n");
		    for(LightningType lightningType : LightningType.values())
		    	if(!lightningType.equals(LightningType.NORMAL))
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
				player.sendMessage(ChatColor.LIGHT_PURPLE + "/got bind (alias b) (lightningType) - bind current item to the specified type of lightning");
				player.sendMessage(ChatColor.LIGHT_PURPLE + "/got unbind (alias u) [lightningType] - unbind current item [or lightningType]");
				player.sendMessage(ChatColor.LIGHT_PURPLE + "/got set (lightningType) (#value) - set lightning type attribute (see below)");
			}
			else player.sendMessage(ChatColor.RED + "No permissions. :(");
		}
		else log.info("[God Of Thunder] Error: must be a player to use GoT commands."); //TODO Stop being lame here.
	}
	  
//////////////////// GOT EXECUTION /////////////////////////
	  
	public void tryStrike(PlayerInteractEvent event)
	{
		Player player = event.getPlayer(); 
		World world = player.getWorld();
			if(playerConfigs.get(player.getName()).containsKey(player.getItemInHand().getType()))
			{
				LightningType lightningType = playerConfigs.get(player.getName()).getBoundLightningType(player.getItemInHand().getType());
				if(hasPermission(player, "got.type." + lightningType.getTypeString()))
					strikeLightning(player, lightningType);
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
	
	private boolean strikeLightning(Player player, LightningType lightningType)
	{
		int commandModifier = playerConfigs.get(player.getName()).getTypeAttribute(lightningType);
		Location thunderLoc = findStrikeArea(player);
		switch(lightningType)
		{
			case NORMAL: 
				player.getWorld().strikeLightning(thunderLoc);
				break;
				
			case EXPLOSIVE: 
				player.getWorld().strikeLightning(thunderLoc);
				if(commandModifier > 0)
					((CraftWorld)player.getWorld()).getHandle().createExplosion(null, thunderLoc.getX(), thunderLoc.getY(), thunderLoc.getZ(), commandModifier, true);
				break;
				
			case DIFFUSIVE: 
				if(player.getWorld().strikeLightningEffect(thunderLoc).isEffect())
					player.sendMessage("IMA EFFECT SON"); //TODO REMOVE ME
				//if(commandModifier > 0)
					//generateFire(thunderLoc, commandModifier);
				break;
				
			case SUMMON_CREEPER:
				strikeAndSummon(player, CreatureType.CREEPER);
				break;
				
			case SUMMON_PIGZOMBIE:
				strikeAndSummon(player, CreatureType.PIG_ZOMBIE);
				break;
		}
		degradeWeapon(player, lightningType);
		return true;
	}
	
	private void degradeWeapon(Player player, LightningType lightningType) //TODO refer to world configs
	{
		if(durabilityCosts.get(player.getWorld()).containsKey(lightningType))
			player.getItemInHand().setDurability((short)(player.getItemInHand().getDurability() 
					- durabilityCosts.get(player.getWorld()).get(lightningType)));
	}
	
	/*private void generateFire(Location location, int radius) 
	{
		
	}*/

	private void strikeAndSummon(Player player, CreatureType creatureType) 
	{
		Location thunderLoc = findStrikeArea(player);
		Location creatureLoc = thunderLoc;
		creatureLoc.setY(thunderLoc.getY() + 1);
		
		player.getWorld().strikeLightning(thunderLoc);
		player.getWorld().spawnCreature(creatureLoc, creatureType);
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
