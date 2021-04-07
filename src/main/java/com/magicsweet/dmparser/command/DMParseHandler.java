package com.magicsweet.dmparser.command;

import com.magicsweet.dmparser.DMParser;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationNodeFactory;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

public class DMParseHandler implements Listener {
	final Player player;
	Chest block;
	InputAwaitMode inputAwaitMode;
	
	String fileName;
	
	ConfigurationNode master;
	
	public enum InputAwaitMode {
		FILE_NAME("parsed.yml"),
		OPEN_COMMAND("openparsed"),
		CONFIRM("confirm");
		
		@Getter String defaultValue;
		
		InputAwaitMode(String defaultValue) {
			this.defaultValue = defaultValue;
		}
		
		public void message(Player player) {
			String message = null;
			switch (this) {
				case FILE_NAME:
					message = "Enter file name";
					break;
				case OPEN_COMMAND:
					message = "Enter open command";
					break;
				case CONFIRM:
					message = "Summary";
			}
			
			if (message != null) {
				message = message + " (default: {default}):";
				player.sendMessage(message.replace("{default}", getDefaultValue()));
			} else {
				player.sendMessage("Message is null :/" + "\nState: " + this.toString().toLowerCase());
			}
			
		}
		
	}
	
	public DMParseHandler(Player player, Chest block) {
		this.player = player;
		this.block = block;
		this.inputAwaitMode = InputAwaitMode.FILE_NAME;
		
		master = BasicConfigurationNode.root(ConfigurationOptions.defaults());
		
		Bukkit.getPluginManager().registerEvents(this, DMParser.getInstance());
		player.sendMessage("Enter \"def\" for default field value");
		inputAwaitMode.message(player);
	}
	
	@EventHandler @SneakyThrows
	public void chatEvent(AsyncPlayerChatEvent event) {
		
		var isDefault = event.getMessage().equals("def");
		var input = event.getMessage();
		
		if (isDefault) {
			input = inputAwaitMode.getDefaultValue();
		} else {
			input = event.getMessage();
		}
		
		if (inputAwaitMode.equals(InputAwaitMode.FILE_NAME)) {
			fileName = input;
			inputAwaitMode = InputAwaitMode.OPEN_COMMAND;
		} else if (inputAwaitMode.equals(InputAwaitMode.OPEN_COMMAND)) {
			master.node("open_command").set(String.class, input);
			master.node("register_command").set(boolean.class, true);
			// TODO check if this bullshit saves
			inputAwaitMode = InputAwaitMode.CONFIRM;
		} else if (inputAwaitMode.equals(InputAwaitMode.CONFIRM)) {
			HandlerList.unregisterAll(this);
		}
	}
	
	public void saveToFile() {
	
	}
	
}
