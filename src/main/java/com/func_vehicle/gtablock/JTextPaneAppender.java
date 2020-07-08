package com.func_vehicle.gtablock;

import java.awt.Color;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.locks.*;

import javax.swing.JTextPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.*;
import org.apache.logging.log4j.core.layout.PatternLayout;

import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;

@Plugin(name="JTextPaneAppender", category="Core", elementType="appender", printObject=true)
public final class JTextPaneAppender extends AbstractAppender {
	
	private static volatile ArrayList<JTextPane> textPaneList = new ArrayList<>();
	
	private static SimpleAttributeSet normalStyle = new SimpleAttributeSet();
    private static SimpleAttributeSet errorStyle = new SimpleAttributeSet();
    
    static {
    	StyleConstants.setForeground(normalStyle, Color.BLACK);
        StyleConstants.setForeground(errorStyle, Color.RED);
    }
	
	private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();
    
	protected JTextPaneAppender(String name, Filter filter,
            Layout<? extends Serializable> layout, final boolean ignoreExceptions) {
        super(name, filter, layout, ignoreExceptions, new Property[0]);
    }
    
    @Override
    public void append(LogEvent event) {
    	readLock.lock();
        try {
        	final byte[] bytes = getLayout().toByteArray(event);
        	String s = new String(bytes, StandardCharsets.UTF_8);
        	for (JTextPane textPane : textPaneList) {
        		StyledDocument doc = textPane.getStyledDocument();
        		if (!"".equals(textPane.getText())) {
        			doc.insertString(doc.getLength(), "\n", null);
        		}
        		SimpleAttributeSet style;
        		if (event.getLevel().isMoreSpecificThan(Level.ERROR)) {
        			style = errorStyle;
        		}
        		else {
        			style = normalStyle;
        		}
        		doc.insertString(doc.getLength(), s, style);
            	textPane.setCaretPosition(textPane.getDocument().getLength());
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
    
    public static void addTextPane(JTextPane textPane) {
    	textPaneList.add(textPane);
    }
    
    @PluginFactory
    public static JTextPaneAppender createAppender(
            @PluginAttribute("name") String name,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginElement("Filter") final Filter filter,
            @PluginAttribute("otherAttribute") String otherAttribute) {
        if (name == null) {
            LOGGER.error("No name provided for JTextPaneAppender");
            return null;
        }
        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }
        return new JTextPaneAppender(name, filter, layout, true);
    }
	
}
