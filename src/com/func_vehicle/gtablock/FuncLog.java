package com.func_vehicle.gtablock;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextArea;

public class FuncLog {
	
	private List<String> logStack;
	private JTextArea outputLoc;
	
	public FuncLog(JTextArea textArea) {
		logStack = new ArrayList<String>();
		outputLoc = textArea;
	}
	
	public void log(String newEntry) {
		logStack.add(newEntry);
		String infoText = "";
		for (String s : logStack) {
			infoText += s + "\n";
		}
		outputLoc.setText(infoText);
	}
}
