package com.magicsweet.dmparser.editmode;

import com.magicsweet.dmparser.DMParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


public class LoreEdit implements Listener {
	private Player player;
	private double random;
	private ItemStack item;
	
	private static final List<LoreEdit> editing = new ArrayList<>();
	
	
	public LoreEdit(Player player) {
		
		if (editing.stream().anyMatch(editing -> editing.player.equals(player))) {
			player.sendMessage(Component.text("You are already editing lore of item! Finish it to start a new one").color(NamedTextColor.RED));
			return;
		}
		
		var item = player.getInventory().getItemInMainHand();
		this.item = item;
		
		if (item.getType().equals(Material.AIR)) {
			player.sendMessage(Component.text("You're not holding any item to edit its lore!").color(NamedTextColor.RED));
			return;
		}
		
		if (item.getItemMeta().lore() != null && !item.getItemMeta().lore().isEmpty()) {
			lines = item.getItemMeta().lore();
		}
		
		this.player = player;
		Bukkit.getPluginManager().registerEvents(this, DMParser.getInstance());
		
		random = ThreadLocalRandom.current().nextDouble();
		
		editing.add(this);
		
		showFAQ();
	}
	
	private void showFAQ() {
		var component = Component.text("Edit Mode: Edit Lore").color(NamedTextColor.LIGHT_PURPLE).append(Component.newline())
		.append(Component.text("FAQ:").color(NamedTextColor.AQUA)).append(Component.newline())
		.append(LegacyComponentSerializer.legacyAmpersand().deserialize("&f- Click on line to get suggestion of it")).append(Component.newline())
		.append(LegacyComponentSerializer.legacyAmpersand().deserialize("&f- Enter &a\\{line number} {lore entry} &fto edit specific line")).append(Component.newline())
		.append(LegacyComponentSerializer.legacyAmpersand().deserialize("&f- Enter &a\\remline {index} &fto remove specific line by index")).append(Component.newline())
		.append(LegacyComponentSerializer.legacyAmpersand().deserialize("&f- Enter &a\\faq &fto show this FAQ")).append(Component.newline())
		.append(LegacyComponentSerializer.legacyAmpersand().deserialize("&f- Enter &aany other message starts with \\ &fto show current input"));

		player.sendMessage(component);
	}
	
	private void unregister() {
		HandlerList.unregisterAll(this);
		editing.remove(this);
	}
	
	private void message() {
		var component = Component.text("Edit Lore | ")
		.append(
			Component.text("Clear ").color(TextColor.fromHexString("#ff1a42")).clickEvent(ClickEvent.runCommand("/" + random + "clear"))
		)
		.append(
				Component.text("Save ").color(TextColor.fromHexString("#33cdff")).clickEvent(ClickEvent.runCommand("/" + random + "save"))
		)
		.append(
			Component.text("Save & Exit").color(TextColor.fromHexString("#37ff76")).clickEvent(ClickEvent.runCommand("/" + random + "confirm"))
		)
		.append(Component.newline());
		
		for (int i = 0; i < lines.size(); i++) {
			var line = lines.get(i);
			component = component.append(Component.text(i + " ").clickEvent(ClickEvent.suggestCommand("\\" + i)).color(TextColor.fromHexString("#b2b2b2"))).append(line.clickEvent(ClickEvent.suggestCommand("\\" + i + " " + LegacyComponentSerializer.legacyAmpersand().serialize(line))));
			if (lines.size() - 1 != i) {
				component = component.append(Component.newline());
			}
		}
		
		player.sendMessage(component);
	}
	
	List<Component> lines = new ArrayList<>();
	
	@EventHandler(priority = EventPriority.LOW)
	public void typeEvent(AsyncPlayerChatEvent event) {
		event.setCancelled(true);
		var text = event.getMessage();
		
		if (text.startsWith("\\")) {
			text = text.substring(1);
			var command = text.split("\\s+");
			
			// \{num} text
			try {
				var num = Integer.parseInt(command[0]);
				var builder = new StringBuilder();
				for (int i = 1; i < command.length; i++) {
					builder.append(command[i]);
					if (command.length - 1 != i) {
						builder.append(" ");
					}
				}
				
				if (num > lines.size() - 1) {
					event.getPlayer().sendMessage(
						Component.text(num + " is not matching last index of total lines (" + (lines.size() - 1) + ")").color(NamedTextColor.RED)
					);
					return;
				}
				
				lines.set(num, parseInput(builder.toString()));
				
			} catch (Exception ignored) {}
			
			if (command[0].equals("faq")) {
				showFAQ();
			}
			
			if (command[0].equals("remline")) {
				try {
					var numLine = Integer.parseInt(command[1]);
					
					if (numLine > lines.size() - 1) {
						event.getPlayer().sendMessage(
							Component.text(numLine + " is not matching last index of total lines (" + (lines.size() - 1) + ")").color(NamedTextColor.RED)
						);
						return;
					}
					
					lines.remove(numLine);
					
				} catch (Exception ignored) {}
			}
			
		} else {
			lines.add(parseInput(text));
		}
		
		message();
	}
	
	public static Component parseInput(String text) {
		var component = LegacyComponentSerializer.legacyAmpersand().deserialize(text);
		
		if (!component.hasStyling()) {
			component = component.style(Style.style(NamedTextColor.WHITE));
		}
		
		if (!component.hasDecoration(TextDecoration.ITALIC)) {
			component = component.decoration(TextDecoration.ITALIC, false);
		}
		return component;
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void commandEvent(PlayerCommandPreprocessEvent event) {
		boolean work = false;
		var msg = event.getMessage();
		
		if (msg.equals("/" + random + "clear")) {
			lines.clear();
			player.sendMessage(Component.text("Input cleared!").color(NamedTextColor.GREEN));
			message();
			work = true;
		} else if (msg.equals("/" + random + "confirm")) {
			var meta = item.getItemMeta();
			meta.lore(lines);
			item.setItemMeta(meta);
			unregister();
			player.sendMessage(Component.text("Input confirmed!").color(NamedTextColor.GREEN));
			work = true;
		} else if (msg.equals("/" + random + "save")) {
			var meta = item.getItemMeta();
			meta.lore(lines);
			item.setItemMeta(meta);
			player.sendMessage(Component.text("Input saved!").color(NamedTextColor.GREEN));
			work = true;
		}
		
		
		if (work) event.setCancelled(true);
	}
	
}
