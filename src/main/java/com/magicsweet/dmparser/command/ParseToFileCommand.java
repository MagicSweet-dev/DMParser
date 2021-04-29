package com.magicsweet.dmparser.command;

import com.destroystokyo.paper.block.TargetBlockInfo;
import com.magicsweet.dmparser.util.LocationUtil;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.LocationType;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import org.bukkit.Location;
import org.bukkit.block.Chest;

public class ParseToFileCommand {
	
	public ParseToFileCommand() {
		new CommandAPICommand("dmparser")
		.withArguments(
			new MultiLiteralArgument("parse"),
			new LocationArgument("block", LocationType.BLOCK_POSITION),
			new LocationArgument("block2", LocationType.BLOCK_POSITION)
		).executesPlayer((sender, args) -> {
			var block = sender.getWorld().getBlockAt((Location) args[1]);
			var block2 = sender.getWorld().getBlockAt((Location) args[2]);
			
			if (block.getState() instanceof Chest) {
				var chest = (Chest) block.getState();
				var chest2 = (Chest) block2.getState();
				
				new DMParseHandler(sender, chest, chest2);
				
				return;
			} else {
				sender.sendMessage("Block at " + LocationUtil.locationToString(block.getLocation()) + " isn't a chest!");
			}
			
		}).register();
		
		new CommandAPICommand("dmparser")
		.withArguments(
			new MultiLiteralArgument("parse"),
			new LocationArgument("block", LocationType.BLOCK_POSITION)
		).executesPlayer((sender, args) -> {
			var block = sender.getWorld().getBlockAt((Location) args[1]);
			
			if (block.getState() instanceof Chest) {
				var chest = (Chest) block.getState();
				
				new DMParseHandler(sender, chest, null);
				
				return;
			} else {
				sender.sendMessage("Block at " + LocationUtil.locationToString(block.getLocation()) + " isn't a chest!");
			}
			
		}).register();
	}
}
