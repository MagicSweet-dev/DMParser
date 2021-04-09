package com.magicsweet.dmparser.command;

import com.magicsweet.dmparser.DMParser;
import lombok.Getter;
import lombok.SneakyThrows;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.nio.file.Paths;
import java.util.stream.Collectors;

public class DMParseHandler implements Listener {
	final Player player;
	Chest block;
	InputAwaitMode inputAwaitMode;
	
	String fileName;
	
	ConfigurationNode master;
	
	public enum InputAwaitMode {
		FILE_NAME("parsed.yml"),
		OPEN_COMMAND("openparsed"),
		CONFIRM("confirm"),
		ELSE(null);
		
		@Getter String defaultValue;
		
		InputAwaitMode(String defaultValue) {
			this.defaultValue = defaultValue;
		}
		
		public void message(Player player) {
			String message = null;
			boolean def = true;
			switch (this) {
				case FILE_NAME:
					message = "Enter file name";
					break;
				case OPEN_COMMAND:
					message = "Enter open command";
					break;
				case CONFIRM:
					message = "// TODO Summary, type any to confirm";
					def = false;
					break;
				case ELSE:
					def = false;
					message = "File saved!";
					break;
			}
			if (message != null) {
				if (def) message = (message + " (default: {default}):").replace("{default}", getDefaultValue());
				player.sendMessage(message);
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
	
	@EventHandler(priority = EventPriority.LOW) @SneakyThrows
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
			saveToFile();
			inputAwaitMode = InputAwaitMode.ELSE;
		}
		inputAwaitMode.message(event.getPlayer());
		event.setCancelled(true);
	}
	
	@SneakyThrows
	public void saveToFile() {
		buildItemList();
		
		var file = Paths.get(DMParser.getInstance().getDataFolder().getAbsolutePath(), fileName).toFile();
		file.createNewFile();
		YamlConfigurationLoader.builder().file(file).nodeStyle(NodeStyle.BLOCK).build().save(master);
	}
	
	@SneakyThrows
	private void buildItemList() {
		var items = master.node("items");
		
		var i = 0;
		var slot = 0;
		for (var itemStack: block.getInventory().getContents()) {
			if (itemStack != null) {
				var item = items.node("" + i);
				
				var meta = itemStack.getItemMeta();
				
				item.node("material").set(String.class, itemStack.getType().toString().toLowerCase());
				item.node("slot").set(int.class, slot);
				item.node("amount").set(int.class, itemStack.getAmount());
				if (meta.hasDisplayName()) item.node("display_name").set(String.class, LegacyComponentSerializer.legacyAmpersand().serialize(meta.displayName()));
				if (meta.hasLore()) item.node("lore").set(String[].class, meta.lore().stream().map(LegacyComponentSerializer.legacyAmpersand()::serialize).collect(Collectors.toList()).toArray(String[]::new));
				
				i = i + 1;
			}
			slot = slot + 1;
		}
		
	}
}
