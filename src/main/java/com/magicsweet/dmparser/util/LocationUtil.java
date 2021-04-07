package com.magicsweet.dmparser.util;

import lombok.experimental.UtilityClass;
import org.bukkit.Location;

@UtilityClass
public class LocationUtil {
	
	public String locationToString(Location location) {
		return ((int) location.getX()) + " " + ((int) location.getY()) + " " + ((int) location.getZ());
	}
}
