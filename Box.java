package de.matthias;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Skull;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import de.matthias.api.HoloAPI;
import de.matthias.api.LoadBox;

public class Box {

	Location pos;
	int rotation;
	Location spawn;
	Location head;
	Location holo;
	Location oldPosUser;
	Location oldPosOwner;
	boolean empty;
	boolean isUserInside;
	int ID;
	HoloAPI hologramm;
	LoadBox boxSchematics;
	
	
	public Box(YamlConfiguration config, int id) {
		pos = new Location(Bukkit.getWorld(config.getString("pos.world")), config.getDouble("pos.x"), config.getDouble("pos.y"), config.getDouble("pos.z"));
		rotation = config.getInt("rotation");
		spawn = new Location(Bukkit.getWorld(config.getString("pos.world")), config.getDouble("spawn.x"), config.getDouble("spawn.y"), config.getDouble("spawn.z"), config.getInt("spawn.yaw"), config.getInt("spawn.pitch"));
		head = new Location(Bukkit.getWorld(config.getString("pos.world")), config.getDouble("head.x"), config.getDouble("head.y"), config.getDouble("head.z"));
		holo = new Location(Bukkit.getWorld(config.getString("pos.world")), config.getDouble("holo.x"), config.getDouble("holo.y"), config.getDouble("holo.z"));
		empty = true;
		isUserInside = false;
		ID = id;
		hologramm = new HoloAPI(holo, Arrays.asList(new String[] { "§7Derzeit in der Box:",
				"§e"}));
		resetHolo();
		boxSchematics = new LoadBox();
		boxSchematics.loadStandart(pos.getWorld(), pos, rotation);
	}

	
	private void destroyHolo(){
		for(Player all : Bukkit.getOnlinePlayers()){
			hologramm.destroy(all);
		}
	}
	
	private void showHolo(){
		for(Player all : Bukkit.getOnlinePlayers()){
			hologramm.display(all);
		}
	}
	
	private void resetHolo(){
		destroyHolo();
		hologramm = new HoloAPI(holo, Arrays.asList(new String[] { "§7Derzeit in der Box:",
		"§4Keiner"}));
		showHolo();
	}
	
	private void updateHolo(String Playername){
		destroyHolo();
		hologramm = new HoloAPI(holo, Arrays.asList(new String[] { "§7Derzeit in der Box:",
		"§e" + Playername}));
		showHolo();
	}
	
	public boolean isHead(Location l) {
		if(head.getBlockX() == l.getBlockX()){
			if(head.getBlockY() == l.getBlockY()){
				if(head.getBlockZ() == l.getBlockZ()){
					return true;
				}
			}
		}
		return false;
	}


	public int getID() {
		return ID;
	}


	public boolean isEmpty() {
		return empty;
	}


	public boolean isUserInside() {
		return isUserInside;
	}


	public void setOwnerIn(Player p) {
		boxSchematics.loadBox(p, pos.getWorld(), pos, rotation);
		empty = false;
		Skull s = (Skull) head.getBlock().getState();
		s.setOwner(p.getName());
		s.update();
		oldPosOwner = p.getLocation();
		p.teleport(spawn);
		p.sendMessage("§3Du hast die §eYT-Box §3betreten!");
		updateHolo(p.getName());
	}


	public void setUserIn(Player p) {
		isUserInside = true;
		oldPosUser = p.getLocation();
		p.teleport(spawn);
	}


	public void setUserOut(Player p) {
		isUserInside = false;
		p.teleport(oldPosUser);
		boxSchematics.loadStandart(pos.getWorld(), pos, rotation);
		p.sendMessage("§3Du hast die §eYT-Box §3verlassen!");
	}


	public void setOwnerOut(Player p) {
		empty = true;
		Skull s = (Skull) head.getBlock().getState();
		s.setOwner("MHF_Skeleton");
		s.update();
		p.teleport(oldPosOwner);
		p.sendMessage("§3Du hast die §eYT-Box §3verlassen!");
		resetHolo();
	}

}
