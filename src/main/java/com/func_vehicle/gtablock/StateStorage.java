package com.func_vehicle.gtablock;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import javax.swing.DefaultListModel;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class StateStorage {
	
	private Collection<Player> playerList;
	private DefaultListModel<Player> model;
	private Logger logger;

	public StateStorage() {
		playerList = new ArrayList<Player>();
		logger = LogManager.getRootLogger();
	}
	
	public Collection<Player> getPlayerList() {
		return playerList;
	}
	
	public void setPlayerList(Collection<Player> playerList) {
		this.playerList = playerList;
	}
	
	public DefaultListModel<Player> getModel() {
		return model;
	}

	public void setModel(DefaultListModel<Player> model) {
		this.model = model;
	}
	
	public void save(File file) throws IOException {	
		Writer writer = new FileWriter(file);
	    Gson gson = new GsonBuilder().create();
	    gson.toJson(playerList, writer);
	    writer.close();
	}
	
	public void save(String fileName) throws IOException {
		File file = new File(fileName);
		save(file);
	}
	
	public void load(File file) throws IOException {
		String extension = FilenameUtils.getExtension(file.getName());
		if ("json".equals(extension)) {
			playerList = loadJson(file);
		}
		else if ("bin".equals(extension)) {
			playerList = loadBin(file);
		}
		else {
			throw new IOException();
		}
	}
	
	private Collection<Player> loadJson(File file) throws IOException {
		Reader reader = new FileReader(file);
		Collection<Player> playerList = null;
	    Gson gson = new GsonBuilder().create();
	    Type listType = new TypeToken<ArrayList<Player>>() {}.getType();
	    
	    boolean b_replace = false;
	    try {
	    	playerList = gson.fromJson(reader, listType);
	    	if (playerList == null) {
	    		b_replace = true;
	    	}
	    }
	    catch (JsonSyntaxException e) {
	    	logger.error("Error loading JSON file...");
	    	b_replace = true;
	    }
	    
	    if (b_replace) {
	    	playerList = new ArrayList<Player>();
	    }
	    
	    reader.close();
		return playerList;
	}
	
	@SuppressWarnings("unchecked")
	private Collection<Player> loadBin(File file) throws IOException {
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
		Collection<Player> playerList = null;
		
		try {
			playerList = (Collection<Player>) in.readObject();
		}
		catch (ClassNotFoundException e) {
			logger.error("Error loading BIN file...");
			playerList = new ArrayList<Player>();
		}
		
	    in.close();
		return playerList;
	}
	
	public void updateFirewallRules() {
		// Set lower and upper bounds
		List<Long> ipNumList = new ArrayList<Long>(Arrays.asList(-1L, 4294967296L));
		List<String> rangeList = new ArrayList<String>();
		String formattedRanges;
		IPAddress localStart = new IPAddress(192, 168, 0, 0);
		IPAddress localEnd = new IPAddress(192, 168, 255, 255);
		
		// Add IP number equivalents to working list and sort it
		for (Player p : playerList) {
			IPAddress ip = p.getIP();
			// Ignore local IPs (they are automatically added)
			if (localStart.compareTo(ip) > 0 || localEnd.compareTo(ip) < 0) {
				ipNumList.add(ip.ipToNum());
			}
		}
		ipNumList.add(localStart.ipToNum());
		ipNumList.add(localEnd.ipToNum());
		ipNumList = new ArrayList<>(new LinkedHashSet<>(ipNumList));
		Collections.sort(ipNumList);
		
		// Create list of Windows Firewall IP ranges to block, format to final string
		for (int i = 1; i < ipNumList.size(); i++) {
			Long lower = ipNumList.get(i - 1) + 1;
			Long upper = ipNumList.get(i) - 1;
			// Skip over local IPs
			if (lower - 1 == localStart.ipToNum()) {
				continue;
			}
			if (lower < upper) {
				IPAddress lowerIP = IPAddress.numToIP(lower);
				IPAddress upperIP = IPAddress.numToIP(upper);
				rangeList.add(lowerIP+"-"+upperIP);
			}
			else if (lower == upper) {
				IPAddress solo = IPAddress.numToIP(lower);
				rangeList.add(solo.toString());
			}
		}
		formattedRanges = String.join(",", rangeList);
		logger.debug(formattedRanges);
		
		// Try modifying the firewall rule
		try {
			String command = "netsh advfirewall firewall set rule name=\"GTA V Block\" new remoteip="+formattedRanges;
			new ProcessBuilder("cmd", "/c", command).start().waitFor();
		}
		catch (IOException | InterruptedException e) {
			logger.error("An error occurred while modifying the firewall");
		}
	}
	
	public void updateFirewallRules(Collection<Player> newList) {
		Collection<Player> bkup = playerList;
		playerList = newList;
		updateFirewallRules();
		playerList = bkup;
	}
	
	public void updateModel() {
		DefaultListModel<Player> newModel = new DefaultListModel<Player>();
		for (Player p : playerList) {
			model.addElement(p);
		}
		model = newModel;
	}
	
}
