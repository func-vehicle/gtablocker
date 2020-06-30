package com.func_vehicle.gtablock;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

class IndentedRenderer extends DefaultListCellRenderer {
	
	private static final long serialVersionUID = -9211204681472166456L;

	public Component getListCellRendererComponent(JList<?> list,Object value, int index,boolean isSelected,boolean cellHasFocus) {
		JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		lbl.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));  // Indent on left of 5
		return lbl;
	}
	
}