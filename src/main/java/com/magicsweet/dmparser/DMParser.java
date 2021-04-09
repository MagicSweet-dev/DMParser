package com.magicsweet.dmparser;

import com.magicsweet.dmparser.command.ParseToFileCommand;
import com.magicsweet.dmparser.editmode.LoreEdit;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ChatComponentArgument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

public final class DMParser extends JavaPlugin {
	@Getter static DMParser instance;
	
	public DMParser() {
		instance = this;
	}
	
	@Override
	public void onLoad() {
		new CommandAPICommand("dmparser")
		.withArguments(
			new MultiLiteralArgument("edit").withPermission("dmparser.command.dmparser"),
			new MultiLiteralArgument("lore")
		).executesPlayer((sender, args) -> {
			new LoreEdit(sender);
		}).register();
		
		new CommandAPICommand("dmparser")
		.withArguments(
				new MultiLiteralArgument("edit").withPermission("dmparser.command.dmparser"),
				new MultiLiteralArgument("name"),
				new ChatComponentArgument("item name (json component)")
		).executesPlayer((sender, args) -> {
			var item = sender.getInventory().getItemInMainHand();
			
			if (item.getType().equals(Material.AIR)) {
				sender.sendMessage(Component.text("You're not holding any item to edit its lore!").color(NamedTextColor.RED));
				return;
			}
			
			var meta = item.getItemMeta();
			
			meta.setDisplayNameComponent((BaseComponent[]) args[2]);
			
			item.setItemMeta(meta);
		}).register();
		
		new CommandAPICommand("dmparser")
		.withArguments(
			new MultiLiteralArgument("edit").withPermission("dmparser.command.dmparser"),
			new MultiLiteralArgument("name"),
			new GreedyStringArgument("item name")
		).executesPlayer((sender, args) -> {
			var item = sender.getInventory().getItemInMainHand();
			
			if (item.getType().equals(Material.AIR)) {
				sender.sendMessage(Component.text("You're not holding any item to edit its lore!").color(NamedTextColor.RED));
				return;
			}
			
			var meta = item.getItemMeta();
			
			meta.displayName(LoreEdit.parseInput((String)args[2]));
			
			item.setItemMeta(meta);
		}).register();
		
		new ParseToFileCommand();
	}
	
	@Override
	public void onEnable() {
		// Plugin startup logic
		saveDefaultConfig();
	}
	
	@Override
	public void onDisable() {
		// Plugin shutdown logic
	}
}
