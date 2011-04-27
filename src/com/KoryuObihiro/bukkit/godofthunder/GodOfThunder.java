package com.KoryuObihiro.bukkit.godofthunder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import net.minecraft.server.Explosion;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

import java.util.logging.Logger;

/**
 * "LoftJump" for Bukkit
 * 
 * @author Erich Gubler
 *
 */
public class GodOfThunder extends JavaPlugin{
	private final GOTPlayerListener playerListener = new GOTPlayerListener(this);
	public final HashMap<Player, ArrayList<Block>> GoT = new HashMap<Player, ArrayList<Block>>();
	public HashSet<Byte> nonStrikableBlocks = new HashSet<Byte>();
	public static Logger log = Logger.getLogger("Minecraft");
	public static PermissionHandler Permissions = null;
	public HashMap<Integer, CreatureType> eggu = new HashMap<Integer, CreatureType>();
	
	
	//TODO There's a reason to question the use of the HashMap for players right now, with Permissions used - IDK
	//DEFAULT SETTINGS
	
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
		getServer().getPluginManager().registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Event.Priority.Normal, this);
		getServer().getPluginManager().registerEvent(Event.Type.PLAYER_EGG_THROW, playerListener, Event.Priority.Normal, this);
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
	}
	
	@Override
	public void onDisable() 
	{
		//TODO Deregister when Bukkit supports
		log.info("["+getDescription().getName()+"] disabled.");	
		//configs.clear();
	}
	  
//////////////////// LEGIONAIRE EXECUTION /////////////////////////
	  
	public void tryPower(Player player, PlayerInteractEvent event)
	{
		World world = player.getWorld();
		
		if((event.getAction() == Action.RIGHT_CLICK_AIR))
		{
			//weather-related stuff
			if(player.getItemInHand().getType() == Material.WATER_BUCKET)
			{
				player.setItemInHand(new ItemStack(Material.BUCKET));
				world.setStorm(true);
				world.setWeatherDuration(100);
			}
			else if(player.getItemInHand().getType() == Material.LAVA_BUCKET)
			{
				player.setItemInHand(new ItemStack(Material.BUCKET));
				//world.setStorm(false);
				world.setWeatherDuration(0);
			}
			//egg stuff
			else if(player.getItemInHand().getType() == Material.SLIME_BALL)
				egg_Toss(player, Material.SLIME_BALL, CreatureType.SLIME);
			else if(player.getItemInHand().getType() == Material.ARROW)
				egg_Toss(player, Material.ARROW, CreatureType.SKELETON);
			
			//weapon-based stuff
			else if(player.getItemInHand().getType() == Material.DIAMOND_SWORD)
			{
				strikeLightning(player, 6);
				degradeWeapon(player);
			}
			else if(player.getItemInHand().getType() == Material.DIAMOND_SPADE)
			{
				strikeAndSummon(player, CreatureType.CREEPER);
				degradeWeapon(player);
			}
			else if(player.getItemInHand().getType() == Material.DIAMOND_HOE)
			{
				strikeAndSummon(player, CreatureType.PIG);
				degradeWeapon(player);
			}
		}
		
		else if(event.getAction() == Action.RIGHT_CLICK_BLOCK)
		{
			if(player.getItemInHand().getType() == Material.DIAMOND_SWORD)
			{
				strikeLightning(player, 6);
				degradeWeapon(player);
			}
			else if(player.getItemInHand().getType() == Material.DIAMOND_SPADE)
			{
				strikeAndSummon(player, CreatureType.CREEPER);
				degradeWeapon(player);
			}
			else if(player.getItemInHand().getType() == Material.DIAMOND_HOE)
			{
				strikeAndSummon(player, CreatureType.PIG);
				degradeWeapon(player);
			}
		}	
	}

	private void degradeWeapon(Player player) 
	{
		player.getItemInHand().setDurability((short)(player.getItemInHand().getDurability() - 1));
	}

	private void egg_Toss(Player player, Material itemType, CreatureType creatureType) 
	{
		player.setItemInHand(new ItemStack(player.getItemInHand().getType(), player.getItemInHand().getAmount() - 1));
		int eggID = player.throwEgg().getEntityId();
		eggu.put(eggID, creatureType);
		//log.info("eggID: " + eggID);
	}

	public void egg_Change(Player player, PlayerEggThrowEvent event) 
	{
		int eggID = event.getEgg().getEntityId();
		//log.info("Checking eggID " + eggID);
		if(eggu.containsKey(eggID))
		{
			//log.info("EggID matched! Creature: " + eggu.get(eggID).toString());
			event.setHatchType(eggu.get(eggID));
			event.setHatching(true);
			eggu.remove(eggID);
		}		
	}  


	private boolean strikeLightning(Player player) {return strikeLightning(player, 0);}
	private boolean strikeLightning(Player player, int explosionSize)
	{
		Location thunderLoc = findStrikeArea(player);
		player.getWorld().strikeLightning(thunderLoc);
		if(explosionSize > 0)
		{
			((CraftWorld)player.getWorld()).getHandle().createExplosion(null, thunderLoc.getX(), thunderLoc.getY(), thunderLoc.getZ(), explosionSize, true);
		}
		return true;
	}
	
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
