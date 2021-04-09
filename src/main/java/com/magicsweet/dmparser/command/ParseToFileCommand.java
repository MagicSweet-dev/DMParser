package com.magicsweet.dmparser.command;

import com.destroystokyo.paper.block.TargetBlockInfo;
import com.magicsweet.dmparser.util.LocationUtil;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import org.bukkit.block.Chest;

public class ParseToFileCommand {
	
	public ParseToFileCommand() {
		// looking at
		new CommandAPICommand("dmparser")
		.withArguments(new MultiLiteralArgument("parse"))
		.executesPlayer((sender, args) -> {
			var block = sender.getTargetBlock(10, TargetBlockInfo.FluidMode.NEVER);
			
			if (block.getState() instanceof Chest) {
				var chest = (Chest) block.getState();
				
				new DMParseHandler(sender, chest);
				
				return;
			} else {
				sender.sendMessage("Block at " + LocationUtil.locationToString(block.getLocation()) + " isn't a chest!");
			}
			
		}).register();
	}
}
