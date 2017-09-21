package de.matthias;

import java.io.File;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class BoxManager implements Listener {

	HashMap<Integer, Box> boxes;
	HashMap<Player, Integer> BoxOwner;
	HashMap<Player, Integer> BoxUser;
	HashMap<Player, BukkitRunnable> delay;
	JavaPlugin plugin;
	int secInBox;

	public BoxManager(JavaPlugin p) {
		boxes = new HashMap<>();
		BoxOwner = new HashMap<>();
		BoxUser = new HashMap<>();
		delay = new HashMap<>();
		plugin = p;
		secInBox = 15;
	}

	public void loadBoxes() {

		File boxOrdner = ScreenBox.boxesFiles;
		File[] boxFiles = boxOrdner.listFiles();

		System.out.println("Es wurden " + boxFiles.length + "Boxen geladen!");

		int i = 0;
		for (File f : boxFiles) {
			YamlConfiguration config = YamlConfiguration.loadConfiguration(f);
			if (config.getBoolean("enabled")) {
				boxes.put(i, new Box(config, i));
				i++;
			}
		}

		System.out.println("Es wurden " + i + "Boxen Aktiviert!");
	}

	private Box isHead(Location l) {
		for (Entry<Integer, Box> ent : boxes.entrySet()) {
			if (ent.getValue().isHead(l)) {
				return ent.getValue();
			}
		}
		return null;
	}

	private void UserleaveBox(int ID, Player p) {
		Box b = boxes.get(ID);
		b.setUserOut(p);
		boxes.put(ID, b);
		BoxUser.remove(p);
		if(delay.containsKey(p)){
			delay.get(p).cancel();
			delay.remove(p);
		}
	}

	private void OwnerleaveBox(int ID, Player p) {
		Box b = boxes.get(ID);
		b.setOwnerOut(p);
		boxes.put(ID, b);
		BoxOwner.remove(p);
		for(Entry<Player, Integer> ent : BoxUser.entrySet()){
			if(ent.getValue() == ID){
				UserleaveBox(ID, ent.getKey());
			}
		}
	}
	
	private void leaveBox(Player p){
		if(BoxUser.containsKey(p)){
			UserleaveBox(BoxUser.get(p), p);
		}else if(BoxOwner.containsKey(p)){
			OwnerleaveBox(BoxOwner.get(p), p);
		}
	}

	@EventHandler
	public void onHeadClick(PlayerInteractEvent e) {
		if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (e.hasBlock() && e.getClickedBlock().getType() == Material.SKULL) {
				Box b = isHead(e.getClickedBlock().getLocation());
				if (b != null) {
					final int ID = b.getID();
					final Player p = e.getPlayer();
					if (boxes.get(ID).isEmpty()) {
						if (p.hasPermission("box.owner")) {
							/** Owner Joint Box **/
							BoxOwner.put(p, ID);
							b.setOwnerIn(p);
							boxes.put(ID, b);
							return;
						} else {
							p.sendMessage("§3Warten auf Besitzer der Box!");
						}
					}

					if ((!boxes.get(ID).isEmpty()) && (!boxes.get(ID).isUserInside())) {

						/** User Joint Box **/
						BoxUser.put(p, ID);
						b.setUserIn(p);
						boxes.put(ID, b);
						p.sendMessage("§3Du hast nun §e" + secInBox + "§3 Sekunden Zeit Screens zu machen!");
						delay.put(p, 
							new BukkitRunnable() {
	
								@Override
								public void run() {
									UserleaveBox(ID, p);
									delay.remove(p);
								}
							}
						);
						delay.get(p).runTaskLater(plugin, 20 * secInBox);
					} else {
						p.sendMessage("§3Die Box ist zurzeit voll!!");
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onSignClick(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (e.hasBlock() && (e.getClickedBlock().getType() == Material.SIGN
					|| e.getClickedBlock().getType() == Material.WALL_SIGN
					|| e.getClickedBlock().getType() == Material.SIGN_POST)) {

				Sign s = (Sign) e.getClickedBlock().getState();
				if(s.getLine(1).equalsIgnoreCase("§4YT-Box") && s.getLine(2).equalsIgnoreCase("§4Verlassen")){
					leaveBox(p);
				}
			}
		}
	}
	
	
	@EventHandler
	public void onQuitBox(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		leaveBox(p);
	}
	
	
	@EventHandler
	public void onKick(PlayerKickEvent e) {
		Player p = e.getPlayer();
		leaveBox(p);
	}
	
	
	@EventHandler
	public void onSignChange(SignChangeEvent e){
		Player p = e.getPlayer();
		if(p.hasPermission("box.admin")){
			if(e.getLine(0).equalsIgnoreCase("boxleave")) {
				e.setLine(0, "§7§m------------");
				e.setLine(1, "§4YT-Box");
				e.setLine(2, "§4Verlassen");
				e.setLine(3, "§7§m------------");
			}
		}
	}

}
