package com.KoryuObihiro.bukkit.godofthunder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.entity.Player;

import com.KoryuObihiro.bukkit.ModDamage.Backend.EntityReference;
import com.KoryuObihiro.bukkit.ModDamage.Backend.IntegerMatching.IntegerMatch;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.CalculationRoutine;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Calculation.PlayerCalculationRoutine;

public class GOTRoutine extends PlayerCalculationRoutine
{
	protected final LightningType lightningType;
	
	protected GOTRoutine(String configString, EntityReference entityReference, IntegerMatch match, LightningType lightningType)
	{
		super(configString, entityReference, match);
		this.lightningType = lightningType;
	}

	@Override
	protected void applyEffect(Player player, int input) 
	{
		lightningType.strikeLightning(player.getLocation(), input);
	}
	
	public static void register()
	{
		CalculationRoutine.registerCalculation(GOTRoutine.class, Pattern.compile("(\\w+)effect\\.got.(\\w+)", Pattern.CASE_INSENSITIVE));
	}
	
	public static GOTRoutine getNew(Matcher matcher, IntegerMatch match)
	{
		if(matcher != null && match != null)
		{
			LightningType lightningType = LightningType.matchType(matcher.group(2));
			if(EntityReference.isValid(matcher.group(1)) & lightningType != null)
				return new GOTRoutine(matcher.group(), EntityReference.match(matcher.group(1)), match, lightningType);
		}
		return null;
	}

}
