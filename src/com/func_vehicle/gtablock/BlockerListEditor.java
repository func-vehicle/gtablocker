package com.func_vehicle.gtablock;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

public class BlockerListEditor {
	
	private static boolean unsavedChanges = false;
	
	public static ArrayList<Player> loadPlayerList(File file) {
		StateStorage storage = new StateStorage();
		
		try {
			return (ArrayList<Player>) storage.fetch(file);
		}
		catch (IOException e) {
			return new ArrayList<Player>();
		}
	}
	
	public static void savePlayerList(ArrayList<Player> playerList, File file) throws IOException {
		StateStorage storage = new StateStorage();
		storage.save(playerList, file);
	}
	
	// TODO: why not check if new file would be different to old file?
	public static boolean warnUnsavedChanges() {
		if (unsavedChanges) {
			int dialogButton = JOptionPane.YES_NO_OPTION;
			int dialogResult = JOptionPane.showConfirmDialog (null, "You have unsaved changes, are you sure you want to discard them?", "Warning", dialogButton);
			if (dialogResult == JOptionPane.YES_OPTION) {
				return true;
			}
			return false;
		}
		return true;
	}
	
	public static void updateFirewallRules(ArrayList<Player> playerList) {
		List<Long> ipNumList = new ArrayList<Long>(Arrays.asList(-1L, 4294967296L));
		List<String> rangeList = new ArrayList<String>();
		String formattedRanges;
		for (Player p : playerList) {
			ipNumList.add(p.getIP().ipToNum());
		}
		ipNumList = new ArrayList<>(new LinkedHashSet<>(ipNumList));
		Collections.sort(ipNumList);
		
		for (int i = 1; i < ipNumList.size(); i++) {
			Long lower = ipNumList.get(i - 1) + 1;
			Long upper = ipNumList.get(i) - 1;
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
		
		try {
			String command = "netsh advfirewall firewall set rule name=\"GTA V Block\" new remoteip="+formattedRanges;
			new ProcessBuilder("cmd", "/c", command).start().waitFor();
		}
		catch (IOException | InterruptedException e) {
			System.out.println("An error occurred while modifying the firewall.");
		}
	}

	public static void main(String[] args) {
		// Version
		String versionNum = "2.2.2.1";
		
		// Create frame
		JFrame frame = new JFrame("GTA V Port Blocker");
		JFrame aboutFrame = new JFrame("About");
		
		// Set theme to the system theme
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			SwingUtilities.updateComponentTreeUI(frame);
		}
		catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		
		// Define all of the objects to be used
		ArrayList<Player> playerList = new ArrayList<Player>();
		JFileChooser fileSelect = new JFileChooser();
		File workingDirectory = new File(System.getProperty("user.dir"));
		FileNameExtensionFilter binFilter = new FileNameExtensionFilter("*.bin", "bin");
		FileNameExtensionFilter jsonFilter = new FileNameExtensionFilter("*.json", "json");
		
		File defaultFile = new File("info.json");
		
		GridBagConstraints gbc;
		
		JPanel mainPanel = (JPanel) frame.getContentPane();
		JPanel playerSideBar = new JPanel();
		JPanel noPlayerMain = new JPanel();
		JPanel playerMain = new JPanel();
		JPanel playerButtons = new JPanel();
		JPanel aboutMain = (JPanel) aboutFrame.getContentPane();
		JMenuBar menuBar = new JMenuBar();
		
		JMenu fileMenu = new JMenu("File");
		JMenuItem openItem = new JMenuItem("Open...");
		JMenuItem saveItem = new JMenuItem("Save...");
		JMenuItem saveAsItem = new JMenuItem("Save as...");
		JMenuItem exitItem = new JMenuItem("Exit");
		
		JMenu viewMenu = new JMenu("View");
		JMenuItem firewallItem = new JMenuItem("Windows Firewall");
		
		JMenu helpMenu = new JMenu("Help");
		JMenuItem aboutItem = new JMenuItem("About");
		
		JList<Player> playerJList = new JList<Player>();
		JScrollPane playerJListScroll = new JScrollPane(playerJList);
		JButton addPlayerButton = new JButton("Add...");
		
		JTextArea infoTextArea = new JTextArea();
		JScrollPane infoTextScroll = new JScrollPane(infoTextArea);
		FuncLog funcLog = new FuncLog(infoTextArea);
		
		JButton unblockButton = new JButton("Unblock");
		JButton blockButton = new JButton("Block");
		JButton blockAllButton = new JButton("Block All");
		
		JLabel nameLabel = new JLabel("Name: ");
		JTextField nameField = new JTextField("", 30);
		JLabel ipLabel = new JLabel("IP: ");
		JTextField ipField = new JTextField("", 15);
		JButton applyPlayerButton = new JButton("Apply");
		JButton deletePlayerButton = new JButton("Delete");
		JButton cancelPlayerButton = new JButton("Cancel");
		
		JLabel aboutLabel = new JLabel("GTA V Port Blocker v" + versionNum);
		JLabel about2Label = new JLabel("Copyright func_vehicle 2019. All rights reserved.");
		
		// Initial output
		funcLog.log("func_vehicle's GTA V Port Blocker");
		
		// Set file directory and filter
		fileSelect.setCurrentDirectory(workingDirectory);
		fileSelect.addChoosableFileFilter(binFilter);
		fileSelect.addChoosableFileFilter(jsonFilter);
		fileSelect.setSelectedFile(defaultFile);
		
		// Open default file on startup
		DefaultListModel<Player> model = new DefaultListModel<Player>();
		if (fileSelect.getSelectedFile().exists()) {
			ArrayList<Player> loadedPlayerList = loadPlayerList(fileSelect.getSelectedFile());
			playerList.clear();
			for (Player player : loadedPlayerList) {
				playerList.add(player);
			    model.addElement(player);
			}
			funcLog.log("Loaded default file "+fileSelect.getSelectedFile());
		}
		
		// Frame properties
		frame.setLocation(200, 400);
		frame.setSize(600, 300);
		frame.setMinimumSize(new Dimension(600, 300));
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.setJMenuBar(menuBar);
		
		aboutFrame.setSize(300, 200);
		aboutFrame.setResizable(false);
		aboutFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		// Add menu items to menu
		fileMenu.add(openItem);
		fileMenu.add(saveItem);
		fileMenu.add(saveAsItem);
		fileMenu.add(new JSeparator());
		fileMenu.add(exitItem);
		
		viewMenu.add(firewallItem);
		
		helpMenu.add(aboutItem);
		
		// Add menus to menu bar
		fileMenu.setPreferredSize(new Dimension(40, 20));
		viewMenu.setPreferredSize(new Dimension(40, 20));
		helpMenu.setPreferredSize(new Dimension(40, 20));
		menuBar.add(fileMenu);
		menuBar.add(viewMenu);
		menuBar.add(helpMenu);
		
		// Mnemonics and shortcuts
		fileMenu.setMnemonic('F');
		openItem.setMnemonic('O');
		saveItem.setMnemonic('S');
		exitItem.setMnemonic('X');
		openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		
		viewMenu.setMnemonic('V');
		firewallItem.setMnemonic('W');
		
		helpMenu.setMnemonic('H');
		aboutItem.setMnemonic('A');
		
		// Main panel layout and content
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(playerSideBar, BorderLayout.LINE_START);
		mainPanel.add(noPlayerMain, BorderLayout.CENTER);
		
		// Sub-panel layouts
		noPlayerMain.setLayout(new GridBagLayout());
		playerMain.setLayout(new GridBagLayout());
		playerSideBar.setLayout(new GridBagLayout());
		
		aboutMain.setLayout(new GridBagLayout());
		
		// Scroll bar for player list
		playerJListScroll.setPreferredSize(new Dimension(145, 100));
		playerJListScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		// Player selector
		playerJList.setModel(model);
		playerJList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		playerJList.setLayoutOrientation(JList.VERTICAL);
		playerJList.setVisibleRowCount(-1);
		playerJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		playerJList.setSelectedIndex(0);
		playerJList.setCellRenderer(new IndentedRenderer());
		playerJList.clearSelection();
		
		// Scroll bar for info text
		infoTextScroll.setPreferredSize(new Dimension(145, 100));
		infoTextScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		// Set up info text area
		infoTextArea.setOpaque(true);
		infoTextArea.setBorder(BorderFactory.createCompoundBorder(null, BorderFactory.createEmptyBorder(1, 4, 1, 4)));
		infoTextArea.setEditable(false);
		infoTextArea.setLineWrap(true);
		infoTextArea.setWrapStyleWord(true);
		infoTextArea.setFont(ipLabel.getFont());
		
		// Player side bar
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.BASELINE_LEADING;
		gbc.fill = GridBagConstraints.BOTH;
		
		gbc.insets = new Insets(4, 4, 4, 4);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.weighty = 1;
		playerSideBar.add(playerJListScroll, gbc);
		
		gbc.insets = new Insets(0, 4, 4, 4);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1;
		gbc.weighty = 0;
		playerSideBar.add(addPlayerButton, gbc);
		
		// No player main
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.BASELINE_LEADING;
		gbc.fill = GridBagConstraints.BOTH;
		
		gbc.gridwidth = 3;
		
		gbc.insets = new Insets(4, 0, 0, 4);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.weighty = 1;
		noPlayerMain.add(infoTextScroll, gbc);
		
		gbc.gridwidth = 1;
		
		gbc.insets = new Insets(4, 0, 4, 4);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1;
		gbc.weighty = 0;
		noPlayerMain.add(unblockButton, gbc);
		
		gbc.insets = new Insets(4, 0, 4, 4);
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.weightx = 1;
		gbc.weighty = 0;
		noPlayerMain.add(blockButton, gbc);
		
		gbc.insets = new Insets(4, 0, 4, 4);
		gbc.gridx = 2;
		gbc.gridy = 1;
		gbc.weightx = 1;
		gbc.weighty = 0;
		noPlayerMain.add(blockAllButton, gbc);
		
		// Player main
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.BASELINE_LEADING;
		gbc.insets = new Insets(4, 4, 4, 4);
		
		nameLabel.setLabelFor(nameField);
		gbc.gridwidth = 1;
		playerMain.add(nameLabel, gbc);
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		playerMain.add(nameField, gbc);
		
		ipLabel.setLabelFor(ipField);
		gbc.gridwidth = 1;
		playerMain.add(ipLabel, gbc);
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		playerMain.add(ipField, gbc);
		
		gbc = new GridBagConstraints();
		gbc.insets = new Insets(0, 4, 4, 0);
		gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
		gbc.gridy = 2;
		gbc.gridx = 1;
		playerMain.add(playerButtons, gbc);
		
		// Player buttons
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.BASELINE_LEADING;
		gbc.insets = new Insets(0, 0, 0, 0);
		
		playerButtons.add(cancelPlayerButton);
		playerButtons.add(deletePlayerButton);
		playerButtons.add(applyPlayerButton);
		
		applyPlayerButton.setEnabled(false);
		
		// About frame
		gbc = new GridBagConstraints();
		gbc.insets = new Insets(4, 4, 4, 4);
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		
		aboutLabel.setFont(aboutLabel.getFont().deriveFont(Font.BOLD));
		aboutMain.add(aboutLabel, gbc);
		aboutMain.add(about2Label, gbc);
		
		// Make the open file menu item work
		openItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (!warnUnsavedChanges()) {
					return;
				}
				
				int result = fileSelect.showOpenDialog(frame);
				if (result == JFileChooser.APPROVE_OPTION) {
					ArrayList<Player> loadedPlayerList = loadPlayerList(fileSelect.getSelectedFile());
					DefaultListModel<Player> model = new DefaultListModel<Player>();
					playerList.clear();
					for (Player player : loadedPlayerList) {
						playerList.add(player);
					    model.addElement(player);
					}
					playerJList.setModel(model);
					playerJList.clearSelection();
					mainPanel.remove(playerMain);
	        		mainPanel.add(noPlayerMain, BorderLayout.CENTER);
	        		
	        		frame.repaint();
	        		frame.validate();
	        		funcLog.log("Loaded file "+fileSelect.getSelectedFile());
	        		unsavedChanges = false;
				}
			}
		});
		
		// Make the save file menu item work
		saveItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (!fileSelect.getSelectedFile().exists()) {
					saveAsItem.doClick();
					return;
				}
				else {
					if (playerJList.getSelectedIndex() != -1) {
						applyPlayerButton.doClick();
					}
					
					try {
						savePlayerList(playerList, fileSelect.getSelectedFile());
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					updateFirewallRules(playerList);
					funcLog.log("Saved file "+fileSelect.getSelectedFile());
					unsavedChanges = false;
				}
			}
		});
		
		// Make the save as file menu item work
		saveAsItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (playerJList.getSelectedIndex() != -1) {
					applyPlayerButton.doClick();
				}
				
				int result = fileSelect.showSaveDialog(frame);
				if (result == JFileChooser.APPROVE_OPTION) {
					try {
						if (!fileSelect.getSelectedFile().toString().endsWith(".json")) {
							File fileWithExt = new File(fileSelect.getSelectedFile().toString() + ".json");
							fileSelect.setSelectedFile(fileWithExt);
						}
						savePlayerList(playerList, fileSelect.getSelectedFile());
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					updateFirewallRules(playerList);
					funcLog.log("Saved file "+fileSelect.getSelectedFile());
					unsavedChanges = false;
				}
			}
		});
		
		// Make the exit menu item work
		exitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (!warnUnsavedChanges()) {
					return;
				}
				System.exit(0);
			}
		});
		
		// Make the exit button work
		frame.addWindowListener(new WindowAdapter() {
		    public void windowClosing(WindowEvent e) {
		    	if (!warnUnsavedChanges()) {
					return;
				}
				System.exit(0);
		    }
		});
		
		// Make the firewall menu item work
		firewallItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					String command = 
							"wf.msc";
					new ProcessBuilder("cmd", "/c", command).start();
					funcLog.log("Opened Windows Firewall");
				}
				catch (IOException e) {
					funcLog.log("An error occurred while opening the firewall.");
				}
			}
		});
		
		// Make the about menu item work
		aboutItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				Point loc = frame.getLocation();
				aboutFrame.setLocation(loc.x + 112, loc.y + 50);
				aboutFrame.setVisible(true);
			}
		});
		
		// Make the add player button work
		addPlayerButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				Player currentPlayer = new Player();
				currentPlayer.setName("New Player");
				currentPlayer.setIP(new IPAddress(0, 0, 0, 0));
				
				playerList.add(currentPlayer);
				((DefaultListModel<Player>) playerJList.getModel()).addElement(currentPlayer);
				playerJList.setSelectedIndex(playerJList.getModel().getSize()-1);
				
				unsavedChanges = true;
			}
		});
		
		// Make clicking a user on the left show details on right
		playerJList.addListSelectionListener(new ListSelectionListener() {
	        @Override
	        public void valueChanged(ListSelectionEvent e) {
	        	if (playerJList.getSelectedValue() != null) { // User selected on left
	        		Player selectedPlayer = playerJList.getSelectedValue();
	        		nameField.setText(selectedPlayer.getName());
	        		ipField.setText(selectedPlayer.getIP().toString());
	        		mainPanel.remove(noPlayerMain);
	        		mainPanel.add(playerMain, BorderLayout.CENTER);
	        		frame.repaint();
	        		frame.validate();
	        	}
	        	else {
	        		mainPanel.remove(playerMain);
	        		mainPanel.add(noPlayerMain, BorderLayout.CENTER);
	        		frame.repaint();
	        		frame.validate();
	        	}
	        }
		});
		
		// Make the unblock button work
		unblockButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					String command = 
							"netsh advfirewall firewall set rule name=\"GTA V Block\" new enable=no && " +
							"netsh advfirewall firewall set rule name=\"GTA V Open\" new enable=yes";
					new ProcessBuilder("cmd", "/c", command).start().waitFor();
					funcLog.log("Unblocked all");
				}
				catch (IOException | InterruptedException e) {
					funcLog.log("An error occurred while modifying the firewall.");
				}
			}
		});
		
		// Make the block button work
		blockButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				updateFirewallRules(playerList);
				try {
					String command = 
							"netsh advfirewall firewall set rule name=\"GTA V Block\" new enable=yes && " +
							"netsh advfirewall firewall set rule name=\"GTA V Open\" new enable=no";
					new ProcessBuilder("cmd", "/c", command).start().waitFor();
					funcLog.log("Blocked all but friends");
				}
				catch (IOException | InterruptedException e) {
					funcLog.log("An error occurred while modifying the firewall.");
				}
			}
		});
		
		// Make the block all button work
		blockAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				updateFirewallRules(new ArrayList<Player>());
				try {
					String command = 
							"netsh advfirewall firewall set rule name=\"GTA V Block\" new enable=yes && " +
							"netsh advfirewall firewall set rule name=\"GTA V Open\" new enable=no";
					new ProcessBuilder("cmd", "/c", command).start().waitFor();
					funcLog.log("Blocked all");
				}
				catch (IOException | InterruptedException e) {
					funcLog.log("An error occurred while modifying the firewall.");
				}
			}
		});
		
		// Make the player apply button work
		DocumentListener fieldCheck = new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent arg0) {
				fieldsChanged();
			}

			@Override
			public void insertUpdate(DocumentEvent arg0) {
				fieldsChanged();
			}

			@Override
			public void removeUpdate(DocumentEvent arg0) {
				fieldsChanged();
			}
			
			public void fieldsChanged() {
				Player selectedPlayer = playerJList.getSelectedValue();
				if (nameField.getText().equals(selectedPlayer.getName()) && ipField.getText().equals(selectedPlayer.getIP().toString())) {
					applyPlayerButton.setEnabled(false);
				}
				else {
					applyPlayerButton.setEnabled(true);
				}
			}
		};
		
		nameField.getDocument().addDocumentListener(fieldCheck);
		ipField.getDocument().addDocumentListener(fieldCheck);
		
		applyPlayerButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				Player selectedPlayer = playerJList.getSelectedValue();
				selectedPlayer.setName(nameField.getText());
				try {
					IPAddress newIP = new IPAddress(ipField.getText());
					selectedPlayer.setIP(newIP);
				}
				catch (IllegalArgumentException e) {
					JOptionPane.showMessageDialog(null, "The provided IP address is invalid.");
					return;
				}
				frame.repaint();
        		frame.validate();
        		
        		applyPlayerButton.setEnabled(false);
        		unsavedChanges = true;
			}
		});
		
		// Make the player delete button work
		deletePlayerButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				Player selectedPlayer = playerJList.getSelectedValue();
				int dialogButton = JOptionPane.YES_NO_OPTION;
				int dialogResult = JOptionPane.showConfirmDialog (null, "Are you sure you want to delete '"+selectedPlayer.getName()+"'?", "Warning", dialogButton);
				if (dialogResult == JOptionPane.YES_OPTION) {
					DefaultListModel<Player> model = (DefaultListModel<Player>) playerJList.getModel();
					model.remove(playerJList.getSelectedIndex());
					playerList.remove(selectedPlayer);
					frame.repaint();
	        		frame.validate();
	        		
	        		unsavedChanges = true;
				}
			}
		});
		
		// Make the player cancel button work
		cancelPlayerButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				playerJList.clearSelection();
				mainPanel.remove(playerMain);
				mainPanel.add(noPlayerMain, BorderLayout.CENTER);
				frame.repaint();
        		frame.validate();
			}
		});
		
		// Draw the main frame
		frame.setVisible(true);
	}

}
