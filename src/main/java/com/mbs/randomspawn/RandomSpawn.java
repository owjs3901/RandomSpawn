package com.mbs.randomspawn;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class RandomSpawn extends JavaPlugin implements Listener {

	private Map<String, Location> spawnMap = new HashMap<>();
	private List<Location> spawnList = new ArrayList<>();
	private final Random random = new Random();
	private final String TAG = "[RandomSpawn] ";
	private boolean rsJoin = true, rsRespawn = true;

	@Override
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(this, this);
		loadConfig();
	}

	private void help(CommandSender sender) {
		sender.sendMessage(new String[]{
				"RandomSpawn 1.0.0",
				"/rs list - 스폰 목록",
				"/rs reload - 정보 갱신",
				"/rs add <Name> - 스폰 추가",
				"/rs remove <Name> - 스폰 삭제",
				"/rs tp <Name> - 스폰으로 이동",
				"MBS Group",
		});
	}

	/*
	저장할때 데이터도 같이 IO
	 */
	@Override
	public void saveConfig() {
		getConfig().set("respawn", rsRespawn);
		getConfig().set("join", rsJoin);
		for (String s : spawnMap.keySet())
			getConfig().set("spawns." + s, spawnMap.get(s));
		super.saveConfig();
	}

	public void loadConfig() {
		if (getConfig().isBoolean("join"))
			rsJoin = getConfig().getBoolean("join");
		else saveConfig();
		if (getConfig().isBoolean("respawn"))
			rsRespawn = getConfig().getBoolean("respawn");
		else saveConfig();
		for (String spawns : getConfig().getConfigurationSection("spawns").getKeys(false))
			spawnList.add(spawnMap.put(spawns, getConfig().getLocation("spawns." + spawns)));
	}

	@Override
	public void reloadConfig() {
		super.reloadConfig();
		spawnList.clear();
		spawnMap.clear();
		loadConfig();
	}

	@Override
	public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
		switch (args.length) {
			case 1:
				switch (args[0]) {
					case "reload":
					case "재시작":
						reloadConfig();
						sender.sendMessage(TAG + "§aConfig 파일을 재시작 했습니다");
						break;
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
					case "tp":
					case "이동":
						if (!spawnMap.containsKey(args[1]))
							sender.sendMessage(String.format(TAG + "§c%s 스폰지점을 찾을 수 없습니다", args[1]));
						else if (sender instanceof Player) {
							((Player) sender).teleport(spawnMap.get(args[1]));
							sender.sendMessage(String.format(TAG + "§a%s 스폰지점으로 이동했습니다", args[1]));
						} else
							sender.sendMessage(TAG + "§c플레이어만 사용할 수 있습니다.");
						break;
					case "추가":
					case "add":
						if (spawnMap.containsKey(args[1]))
							sender.sendMessage(TAG + "§c이미 존재하는 스폰지점입니다");
						else if (sender instanceof Player) {
							Location loc = ((Player) sender).getLocation();
							spawnMap.put(args[1], loc);
							spawnList.add(loc);
							saveConfig();
							sender.sendMessage(String.format(TAG + "§a%s 스폰지점을 추가했습니다", args[1]));
						} else
							sender.sendMessage(TAG + "§c플레이어만 사용할 수 있습니다.");
						break;
					case "삭제":
					case "remove":
						if (!spawnMap.containsKey(args[1]))
							sender.sendMessage(String.format(TAG + "§c%s 스폰지점을 찾을 수 없습니다", args[1]));
						else {
							spawnList.remove(spawnMap.remove(args[1]));
							saveConfig();
							sender.sendMessage(String.format(TAG + "§a%s 스폰지점을 삭제했습니다", args[1]));
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
		return true;
	}

	@Override
	public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
		if(args.length>1)
			return new ArrayList<>(spawnMap.keySet());
		else
			return Arrays.asList("reload","list","add","remove","tp","재시작","목록","추가","삭제","이동");
	}

	@Override
	public void onDisable() {
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		if (rsJoin && !spawnMap.isEmpty())
			Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> e.getPlayer().teleport(spawnList.get(random.nextInt(spawnList.size()))));
	}

	@EventHandler
	public void onSpawn(PlayerRespawnEvent e) {
		if (rsRespawn && !spawnMap.isEmpty())
			e.setRespawnLocation(spawnList.get(random.nextInt(spawnList.size())));
	}
}
