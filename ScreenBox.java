package de.matthias;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import de.matthias.commands.CommandSetHead;
import de.matthias.commands.CommandSetPos;
import de.matthias.commands.CommandDisable;
import de.matthias.commands.CommandEnable;
import de.matthias.commands.CommandSetHolo;
import de.matthias.commands.CommandSetSpawn;

public class ScreenBox extends JavaPlugin implements Listener{
	
	public static JavaPlugin plugin;
	public static File boxesFiles;
	
	BoxManager BXManager;
	
	@Override
	public void onDisable() {
		
	}
	
	@Override
	public void onEnable() {
		PluginManager pm = Bukkit.getPluginManager();
		plugin = this;
		
		File dataFile = new File(getDataFolder() + "/");
		if(!dataFile.exists()){
			dataFile.mkdir();
		}
		
		File boxesFile = new File(getDataFolder() + "/boxes/");
		if(!boxesFile.exists()){
			boxesFile.mkdir();
		}
		boxesFiles = boxesFile;
		
		getCommand("boxsetpos").setExecutor(new CommandSetPos());
		getCommand("boxsetspawn").setExecutor(new CommandSetSpawn());
		getCommand("boxsethead").setExecutor(new CommandSetHead());;
		getCommand("boxsetholo").setExecutor(new CommandSetHolo());;
		getCommand("boxenable").setExecutor(new CommandEnable());;
		getCommand("boxdisable").setExecutor(new CommandDisable());;
		
		pm.registerEvents(this, this);
		
		BXManager = new BoxManager(this);
		BXManager.loadBoxes();
		pm.registerEvents(BXManager, this);
		
	}

	
}
