package com.mbs.randomspawn;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.util.*;

public final class RandomSpawn extends JavaPlugin implements Listener {

	private Map<String, Location> spawnMap = new HashMap<>();
	private List<Location> spawnList = new ArrayList<>();
	private final Random random = new Random();
	private final String TAG = "[RandomSpawn] ";

	@Override
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(this, this);
		for (String spawns : getConfig().getConfigurationSection("spawns").getKeys(false)) {
			Location loc = getConfig().getLocation("spawns." + spawns);
			spawnMap.put(spawns, loc);
			spawnList.add(loc);
		}
	}

	private void help(CommandSender sender) {
		sender.sendMessage(new String[]{
				"RandomSpawn 1.0.0",
				"/rs list - 스폰 목록",
				"/rs add <Name> - 스폰 추가",
				"/rs remove <Name> - 스폰 삭제",
				"MBS Group",
		});
	}

	/*
	저장할때 데이터도 같이 IO
	 */
	@Override
	public void saveConfig() {
		for (String s : spawnMap.keySet())
			getConfig().set("spawns." + s, spawnMap.get(s));
		super.saveConfig();
	}

	@Override
	public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
		switch (args.length) {
			case 1:
				switch (args[0]) {
					case "목록":
					case "list":
						sender.sendMessage(String.format("RandomSpawn List(%d)", spawnMap.size()));
						for (String s : spawnMap.keySet())
							sender.sendMessage("- " + s);
						break;
					default:
						help(sender);
						break;
				}
				break;
			case 2:
				switch (args[0]) {
					case "추가":
					case "add":
						if (spawnMap.containsKey(args[1]))
							sender.sendMessage(TAG + "§c이미 존재하는 스폰지점입니다");
						else if (sender instanceof Player) {
							Location loc = ((Player) sender).getLocation();
							spawnMap.put(args[1], loc);
							spawnList.add(loc);
							saveConfig();
							sender.sendMessage(TAG + "스폰지점을 추가했습니다");
						} else
							sender.sendMessage(TAG + "§c플레이어만 사용할 수 있습니다.");
						break;
					case "삭제":
					case "remove":
						if (!spawnMap.containsKey(args[1]))
							sender.sendMessage(TAG + "§c해당 스폰지점을 찾을 수 없습니다");
						else {
							spawnList.remove(spawnMap.remove(args[1]));
							saveConfig();
							sender.sendMessage(TAG + "스폰지점을 삭제했습니다");
						}
						break;
					default:
						help(sender);
						break;
				}
				break;
			default:
				help(sender);
				break;
		}
		return super.onCommand(sender, command, label, args);
	}

	@Override
	public void onDisable() {
	}

	@EventHandler
	public void onSpawn(PlayerSpawnLocationEvent e) {
		if (!spawnMap.isEmpty()){
			int r=random.nextInt(spawnList.size());
			System.out.println(r+" "+spawnList.get(r).toString());
			e.setSpawnLocation(spawnList.get(r));
		}
	}
}
