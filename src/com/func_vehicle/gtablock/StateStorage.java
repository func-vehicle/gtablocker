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
import java.util.Collection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class StateStorage {
	
	void save(java.util.Collection<Player> playerList, File file) throws IOException {	
		Writer writer = new FileWriter(file);
	    Gson gson = new GsonBuilder().create();
	    gson.toJson(playerList, writer);
	    
	    writer.close();
	}
	
	void save(java.util.Collection<Player> playerList, String fileName) throws IOException {
		File file = new File(fileName);
		save(playerList, file);
	}
	
	java.util.Collection<Player> fetch(File file) throws IOException {
		// Java really doesn't have this natively?
		String extension = "";
		String fileName = file.getName();
		
		int i = fileName.lastIndexOf('.');
		if (i >= 0) {
		    extension = fileName.substring(i+1);
		}
		
		java.util.Collection<Player> playerList = null;
		
		if ("json".equals(extension)) {
			playerList = loadJson(file);
		}
		else if ("bin".equals(extension)) {
			playerList = loadBin(file);
		}
		else {
			throw new IOException();
		}
		
		return playerList;
	}
	
	Collection<Player> loadJson(File file) throws IOException {
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
	    	// TODO: print error in FuncLog
	    	System.out.println("Error loading JSON file...");
	    	b_replace = true;
	    }
	    
	    if (b_replace) {
	    	playerList = new ArrayList<Player>();
	    }
	    
	    reader.close();
		return playerList;
	}
	
	@SuppressWarnings("unchecked")
	Collection<Player> loadBin(File file) throws IOException {
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
		Collection<Player> playerList = null;
		
		try {
			playerList = (Collection<Player>) in.readObject();
		}
		catch (ClassNotFoundException e) {
			// TODO: print error in FuncLog
			System.out.println("Error loading BIN file...");
			playerList = new ArrayList<Player>();
		}
		
	    in.close();
		return playerList;
	}
	
}
