package com.func_vehicle.gtablock;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;

public class StateStorage {
	
	void save(java.util.Collection<Player> playerList, File file) throws IOException {
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
		out.writeObject(playerList);
		out.close();
	}
	
	void save(java.util.Collection<Player> playerList, String fileName) throws IOException {
		File file = new File(fileName);
		save(playerList, file);
	}
	
	@SuppressWarnings("unchecked")
	java.util.Collection<Player> fetch(File file) throws IOException {
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
		java.util.Collection<Player> playerList = null;
		try {
			playerList = (Collection<Player>) in.readObject();
		}
		catch (ClassNotFoundException e) {
			System.out.println("Error");
		}
	    in.close();
		return playerList;
	}
	
}
