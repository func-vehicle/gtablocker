package com.func_vehicle.gtablock;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

class FileListener implements Runnable {
	
	volatile boolean isRunning = true;
	File file;
	Path dir;
	
	public FileListener(File f) {
		file = f;
		dir = Paths.get(file.getAbsolutePath()).getParent();
		System.out.println(dir);
	}
	
    public void run() {
    	WatchService watcher;
		try {
			watcher = FileSystems.getDefault().newWatchService();
		}
		catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		WatchKey key;
		try {
		    key = dir.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
		}
		catch (IOException x) {
			System.out.println("Failed to register!");
			System.out.println(x);
		    return;
		}
    	
    	while (isRunning) {
    		 // Wait for key to be signaled
    	    try {
    	        key = watcher.take();
    	    }
    	    catch (InterruptedException e) {
    	    	System.out.println("Failed to take key!");
    	    	System.out.println(e);
    	        return;
    	    }

    	    for (WatchEvent<?> event: key.pollEvents()) {
    	        WatchEvent.Kind<?> kind = event.kind();
    	        System.out.println(kind);
    	        
    	        // This key is registered only for ENTRY_MODIFY events, but an OVERFLOW event can
    	        // occur regardless if events are lost or discarded.
    	        if (kind == StandardWatchEventKinds.OVERFLOW) {
    	        	System.out.println("Skipping error");
    	            continue;
    	        }
    	        
    	        // The filename is the context of the event.
    	        @SuppressWarnings("unchecked")
				WatchEvent<Path> ev = (WatchEvent<Path>)event;
    	        Path filename = ev.context();
    	        
    	        if (file.getName().equals(filename.toString())) {
    	        	System.out.println("Match!");
    	        }
    	        
    	        // TODO: update the application somehow
    	        
	            //Path child = dir.resolve(filename);
	            
    	        // Email the file to the specified email alias.
    	        System.out.println("Modified file: "+filename);
    	    }

    	    // Reset the key -- this step is critical if you want to receive further watch events.
    	    // If the key is no longer valid, the directory is inaccessible so exit the loop.
    	    boolean valid = key.reset();
    	    if (!valid) {
    	        break;
    	    }
    	}
    }
    
    public void watch() {
    	isRunning = true;
    }
    
    public void unwatch() {
    	isRunning = false;
    }
    
}