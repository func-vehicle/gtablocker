package com.func_vehicle.gtablock;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextArea;

public class FuncLog {
	
	private List<String> logList;
	private JTextArea outputLoc;
	
	public FuncLog(JTextArea textArea) {
		logList = new ArrayList<String>();
		outputLoc = textArea;
	}
	
	public void log(String newEntry) {
		logList.add(newEntry);
		if (logList.size() == 1) {
			outputLoc.setText(newEntry);
		}
		else {
			outputLoc.append("\n" + newEntry);
		}
		outputLoc.setCaretPosition(outputLoc.getDocument().getLength());
	}
}
