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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class FileListener implements Runnable {
	
//	private volatile boolean running = false;
	private File file;
	private Path dir;
	private StateStorage storage;
	private WatchService watcher;
	private WatchKey key;
	private Logger logger;
	
	public FileListener(File f, StateStorage ss) {
		storage = ss;
		logger = LogManager.getRootLogger();
		try {
			watcher = FileSystems.getDefault().newWatchService();
		}
		catch (IOException e) {
			logger.error("Failed to register watch service");
			e.printStackTrace();
			return;
		}
	}
	
    public void run() {
    	Long lastModified = 0L;
    	
    	while (true) {
    		 // Wait for key to be signaled
    	    try {
    	        key = watcher.take();
    	    }
    	    catch (InterruptedException e) {
    	    	logger.error("Could not continue watching file");
    	    	e.printStackTrace();
    	        return;
    	    }

    	    for (WatchEvent<?> event: key.pollEvents()) {
    	        WatchEvent.Kind<?> kind = event.kind();
    	        
    	        // This key is registered only for ENTRY_MODIFY events, but an OVERFLOW event can
    	        // occur regardless if events are lost or discarded.
    	        if (kind == StandardWatchEventKinds.OVERFLOW) {
    	        	logger.debug("Skipping overflow event");
    	            continue;
    	        }
    	        
    	        // The filename is the context of the event.
    	        @SuppressWarnings("unchecked")
				WatchEvent<Path> ev = (WatchEvent<Path>)event;
    	        Path filename = ev.context();
    	        
    	        Long newLastModified = dir.resolve(filename).toFile().lastModified();
    	        System.out.println(newLastModified);
    	        
    	        // Check if right file and not a duplicate event (Allow up to 5ms when discarding duplicates)
    	        if (file.getName().equals(filename.toString()) && newLastModified > lastModified + 5) {
    	        	try {
    					storage.load(file);
    					storage.updateModel();
    					storage.updateFirewallRules();
    					lastModified = newLastModified;
    					logger.info("Successfully reloaded file "+filename);
    				}
        	        catch (IOException e) {
    					logger.error("Could not automatically reload file");
    					e.printStackTrace();
    				}
    	        }
    	    }

    	    // Reset the key -- this step is critical if you want to receive further watch events.
    	    // If the key is no longer valid, the directory is inaccessible so exit the loop.
    	    boolean valid = key.reset();
    	    if (!valid) {
    	    	logger.error("Directory inaccessible, unable to watch file");
    	        break;
    	    }
    	}
    }
    
    public void watchFile(File f) {
    	file = f;
		dir = Paths.get(file.getAbsolutePath()).getParent();
		
		try {
		    key = dir.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
		}
		catch (IOException e) {
			logger.error("Failed to register watcher on file");
			e.printStackTrace();
		    return;
		}
    	
//    	synchronized (key) {
//    		running = true;
//        	key.notify();
//    	}
    }
    
    public void unwatch() {
//    	running = false;
    	key.cancel();
    }
    
}