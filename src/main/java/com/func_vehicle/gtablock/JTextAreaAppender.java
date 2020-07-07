package com.func_vehicle.gtablock;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.locks.*;

import javax.swing.JTextArea;

import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.*;
import org.apache.logging.log4j.core.layout.PatternLayout;

import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;

@Plugin(name="JTextAreaAppender", category="Core", elementType="appender", printObject=true)
public final class JTextAreaAppender extends AbstractAppender {
	
	private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();
    private static volatile ArrayList<JTextArea> textAreaList = new ArrayList<>();
    
	protected JTextAreaAppender(String name, Filter filter,
            Layout<? extends Serializable> layout, final boolean ignoreExceptions) {
        super(name, filter, layout, ignoreExceptions, new Property[0]);
    }
    
    @Override
    public void append(LogEvent event) {
    	readLock.lock();
        try {
        	final byte[] bytes = getLayout().toByteArray(event);
        	String s = new String(bytes, StandardCharsets.UTF_8);
        	for (JTextArea textArea : textAreaList) {
        		if (!"".equals(textArea.getText()))
        			textArea.append("\n");
            	textArea.append(s);
            	textArea.setCaretPosition(textArea.getDocument().getLength());
        	}
		} catch (Exception e) {
			if (!ignoreExceptions()) {
                throw new AppenderLoggingException(e);
            }
		}
        finally {
        	readLock.unlock();
        }
    }
    
    public static void addTextArea(JTextArea textArea) {
    	textAreaList.add(textArea);
    }
    
    @PluginFactory
    public static JTextAreaAppender createAppender(
            @PluginAttribute("name") String name,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginElement("Filter") final Filter filter,
            @PluginAttribute("otherAttribute") String otherAttribute) {
        if (name == null) {
            LOGGER.error("No name provided for JTextAreaAppender");
            return null;
        }
        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }
        return new JTextAreaAppender(name, filter, layout, true);
    }
	
}
