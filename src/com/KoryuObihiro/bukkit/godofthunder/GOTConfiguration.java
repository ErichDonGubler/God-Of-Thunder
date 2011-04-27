package com.KoryuObihiro.bukkit.godofthunder;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.util.config.Configuration;
import org.bukkit.util.config.ConfigurationNode;



public class GOTConfiguration
{
//// MEMBERS ////
	private GodOfThunder plugin;
	private Logger log;
	public World world;
	
	public boolean settings_use_onByDefault = true;
	public boolean settings_free_onByDefault = false;
	public boolean settings_HoldMaterial_use = true;
	public Material settings_ConsumeMaterial = Material.FEATHER;
	public Material settings_HoldMaterial = Material.FEATHER;
	public int settings_costPerDamage = 1;	
	
	
//// CONSTRUCTOR ////
	public GOTConfiguration(World world, GodOfThunder plugin) 
	{
		this.world = world;
		this.plugin = plugin;
		this.log = GodOfThunder.log;
	}
	
	
//// CLASS-SPECIFIC FUNCTIONS ////
	//getters
	public boolean get_use_onByDefault()	{return settings_use_onByDefault;}
	public boolean get_free_onByDefault()	{return settings_free_onByDefault;}
	public boolean get_HoldMaterial_use()	{return settings_HoldMaterial_use;}
	public Material get_HoldMaterial() 		{return settings_HoldMaterial;}
	public Material get_ConsumeMaterial() 	{return settings_ConsumeMaterial;}
	public int get_Cost() 					{return settings_costPerDamage;}
	
	//setters
	public void set_use_onByDefault(boolean input) { settings_use_onByDefault = input;}
	public void set_free_onByDefault(boolean input){ settings_free_onByDefault = input;}
	public void set_HoldMaterial_use(boolean input){ settings_HoldMaterial_use = input;}
	public void set_HoldMaterial(Material input)   { settings_HoldMaterial = input;}
	public void set_ConsumeMaterial(Material input){ settings_ConsumeMaterial = input;}
	public void set_Cost(int input)				   { settings_costPerDamage = input;}
	
	
	public void loadSettings(Configuration config)
	{
		/* Stuff to hopefully implement in a later version?
		   	File yml = null;
		 	log.info("Checking at location " + plugin.getDataFolder().toString() + "\\" + world.getName() + ".yml");
			try{yml = new File(plugin.getDataFolder() + "\\" + world.getName() + ".yml");}
			catch(Exception e)
			{
				log.severe("Loftjump configuration for world " + world.getName() + " not found!");
				return;
			}
			
			if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
			if (!yml.exists()) 
			{
				try { yml.createNewFile();} 
				catch (IOException e) {e.printStackTrace();}
			}
		*/
		
		//if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
		
		config.load();
		
		ConfigurationNode thisConfig = config.getNode(world.getName()); //hopefully this will not be necessary as a better multiworld is added
	//grab the settings from the YAML file
		
		try
		{
		//settings_HoldMaterial
			try
			{settings_HoldMaterial = Material.getMaterial(thisConfig.getInt("holdMaterial", Material.FEATHER.getId()));}
			catch(Exception e)
			{
				settings_HoldMaterial = Material.getMaterial(thisConfig.getString("holdMaterial", "" + Material.FEATHER.toString()));
			}
			
		//settings_HoldMaterial_use
			settings_HoldMaterial_use = thisConfig.getBoolean("holdMaterial_use", true);
	
		//settings_ConsumeMaterial
			try
			{settings_ConsumeMaterial = Material.getMaterial(thisConfig.getInt("consumeMaterial", Material.FEATHER.getId()));}
			catch(Exception e)
			{
				settings_ConsumeMaterial = Material.getMaterial(thisConfig.getString("consumeMaterial", "" + Material.FEATHER.toString()));
			}
			
		}
		catch(Exception e)
		{
			log.severe("[Legionaire] Invalid file configuration for world " + world.getName() + ";\nusing default settings");
			useDefaults();
		}
		
		return;
	}
	
	public void useDefaults()
	{
		settings_use_onByDefault = true;
		settings_free_onByDefault = false;
		settings_HoldMaterial_use = true;
		settings_ConsumeMaterial = Material.FEATHER;
		settings_HoldMaterial = Material.FEATHER;
		settings_costPerDamage = 1;
	}
}

	