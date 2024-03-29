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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
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
import javax.swing.JTextField;
import javax.swing.JTextPane;
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

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BlockerListEditor {
	
	private static boolean unsavedChanges = false;
	
	public static boolean warnUnsavedChanges() {
		if (unsavedChanges) {
			int dialogButton = JOptionPane.YES_NO_OPTION;
			int dialogResult = JOptionPane.showConfirmDialog(null, "You have unsaved changes, are you sure you want to discard them?", "Warning", dialogButton);
			if (dialogResult == JOptionPane.YES_OPTION) {
				return true;
			}
			return false;
		}
		return true;
	}

	public static void main(String[] args) {
		// Version
		String versionNum = "2.4.0";
		
		// Logging
		Logger logger = LogManager.getRootLogger();
		
		// Create frame
		JFrame frame = new JFrame("GTA V Port Blocker");
		JFrame aboutFrame = new JFrame("About");
		
		// Set theme to the system theme
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			SwingUtilities.updateComponentTreeUI(frame);
		}
		catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			logger.error("Could not use the system theme");
			e.printStackTrace();
		}
		
		// Define all of the objects to be used
		StateStorage storage = new StateStorage();
		JFileChooser fileSelect = new JFileChooser();
		File workingDirectory = new File(System.getProperty("user.dir"));
		FileNameExtensionFilter binFilter = new FileNameExtensionFilter("*.bin", "bin");
		FileNameExtensionFilter jsonFilter = new FileNameExtensionFilter("*.json", "json");
		
		GridBagConstraints gbc;
		
		JPanel mainPanel = (JPanel) frame.getContentPane();
		JPanel playerSideBar = new JPanel();
		JPanel consoleMain = new JPanel();
		JPanel playerMain = new JPanel();
		JPanel playerButtons = new JPanel();
		JPanel aboutMain = (JPanel) aboutFrame.getContentPane();
		JMenuBar menuBar = new JMenuBar();
		
		JMenu fileMenu = new JMenu("File");
		JMenuItem openItem = new JMenuItem("Open...");
		JMenuItem saveItem = new JMenuItem("Save...");
		JMenuItem saveAsItem = new JMenuItem("Save as...");
		JCheckBoxMenuItem watchFileItem = new JCheckBoxMenuItem("Watch for changes");
		JMenuItem exitItem = new JMenuItem("Exit");
		
		JMenu viewMenu = new JMenu("View");
		JMenuItem firewallItem = new JMenuItem("Windows Firewall");
		
		JMenu helpMenu = new JMenu("Help");
		JMenuItem aboutItem = new JMenuItem("About");
		
		JList<Player> playerJList = new JList<Player>();
		playerJList.setModel(new DefaultListModel<Player>());
		JScrollPane playerJListScroll = new JScrollPane(playerJList);
		JButton addPlayerButton = new JButton("Add...");
		
		JTextPane infoTextPane = new JTextPane();
		JScrollPane infoTextScroll = new JScrollPane(infoTextPane);
		JTextPaneAppender.addTextPane(infoTextPane);
		
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
		JLabel about2Label = new JLabel("Copyright func_vehicle 2020. All rights reserved.");
		
		// Fix console wrapping (Must be done before any logging can be done)
		infoTextPane.setEditorKit(new WrapEditorKit());
		
		// Initial output
		logger.trace("New instance");
		logger.info("func_vehicle's GTA V Port Blocker");
		
		// Set file directory and filter
		fileSelect.setCurrentDirectory(workingDirectory);
		fileSelect.addChoosableFileFilter(binFilter);
		fileSelect.addChoosableFileFilter(jsonFilter);
		String[] defaultFiles = {"info.json", "info.bin"};
		
		// Open default file on startup
		storage.setJList(playerJList);
		boolean fileLoaded = false;
		for (String file : defaultFiles) {
			fileSelect.setSelectedFile(new File(file));
			if (fileSelect.getSelectedFile().exists()) {
				try {
					storage.load(fileSelect.getSelectedFile());
				}
				catch (IOException e) {
					logger.error("Could not load file "+fileSelect.getSelectedFile());
					e.printStackTrace();
				}
				storage.updateModel();
				if ("info.json".equals(file)) {
					logger.info("Loaded default file "+fileSelect.getSelectedFile());
					fileLoaded = true;
				}
				else {
					logger.info("Loaded legacy default file "+fileSelect.getSelectedFile());
					fileLoaded = true;
				}
				break;
			}
		}
		if (!fileLoaded) {
			logger.info("Default file not found");
		}
		fileSelect.setSelectedFile(new File("info.json"));
		
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
		fileMenu.add(watchFileItem);
		fileMenu.add(new JSeparator());
		fileMenu.add(exitItem);
		
		viewMenu.add(firewallItem);
		
		helpMenu.add(aboutItem);
		
		// Set menu item images
		// TODO: find better, add more
		openItem.setIcon(UIManager.getIcon("FileView.hardDriveIcon"));
		saveItem.setIcon(UIManager.getIcon("FileView.floppyDriveIcon"));
		
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
		mainPanel.add(consoleMain, BorderLayout.CENTER);
		
		// Sub-panel layouts
		consoleMain.setLayout(new GridBagLayout());
		playerMain.setLayout(new GridBagLayout());
		playerSideBar.setLayout(new GridBagLayout());
		
		aboutMain.setLayout(new GridBagLayout());
		
		// Scroll bar for player list
		playerJListScroll.setPreferredSize(new Dimension(145, 100));
		playerJListScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		// Player selector
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
		infoTextPane.setOpaque(true);
		infoTextPane.setBorder(BorderFactory.createCompoundBorder(null, BorderFactory.createEmptyBorder(1, 4, 1, 4)));
		infoTextPane.setEditable(false);
		infoTextPane.setFont(ipLabel.getFont());
		
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
		
		// Console main
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.BASELINE_LEADING;
		gbc.fill = GridBagConstraints.BOTH;
		
		gbc.gridwidth = 3;
		
		gbc.insets = new Insets(4, 0, 0, 4);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.weighty = 1;
		consoleMain.add(infoTextScroll, gbc);
		
		gbc.gridwidth = 1;
		
		gbc.insets = new Insets(4, 0, 4, 4);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1;
		gbc.weighty = 0;
		consoleMain.add(unblockButton, gbc);
		
		gbc.insets = new Insets(4, 0, 4, 4);
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.weightx = 1;
		gbc.weighty = 0;
		consoleMain.add(blockButton, gbc);
		
		gbc.insets = new Insets(4, 0, 4, 4);
		gbc.gridx = 2;
		gbc.gridy = 1;
		gbc.weightx = 1;
		gbc.weighty = 0;
		consoleMain.add(blockAllButton, gbc);
		
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
					
					try {
						storage.load(fileSelect.getSelectedFile());
					} catch (IOException e) {
						logger.error("Could not open file "+fileSelect.getSelectedFile());
						e.printStackTrace();
						return;
					}
					storage.updateModel();
					playerJList.clearSelection();
					mainPanel.remove(playerMain);
	        		mainPanel.add(consoleMain, BorderLayout.CENTER);
	        		
	        		frame.repaint();
	        		frame.validate();
	        		
	        		String extension = FilenameUtils.getExtension(fileSelect.getSelectedFile().getName());
	        		if ("json".equals(extension)) {
	        			logger.info("Loaded file "+fileSelect.getSelectedFile());
	        		}
	        		else {
	        			logger.info("Loaded legacy file "+fileSelect.getSelectedFile());
	        			String newName = FilenameUtils.removeExtension(fileSelect.getSelectedFile().getName()) + ".json";
	        			fileSelect.setSelectedFile(new File(newName));
	        		}
	        		unsavedChanges = false;
	        		watchFileItem.setState(false);
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
						storage.save(fileSelect.getSelectedFile());
					}
					catch (IOException e) {
						logger.error("Could not save file as "+fileSelect.getSelectedFile());
						e.printStackTrace();
						return;
					}
					
					storage.updateFirewallRules();
					logger.info("Saved file "+fileSelect.getSelectedFile());
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
						storage.save(fileSelect.getSelectedFile());
					}
					catch (IOException e) {
						logger.error("Could not save file as "+fileSelect.getSelectedFile());
						e.printStackTrace();
						return;
					}
					
					storage.updateFirewallRules();
					logger.info("Saved file "+fileSelect.getSelectedFile());
					unsavedChanges = false;
					watchFileItem.setState(false);
				}
			}
		});
		
		// Create file listener thread
		FileListener fl = new FileListener(fileSelect.getSelectedFile(), storage);
	    Thread t1 = new Thread(fl, "File Listener");
	    Thread.UncaughtExceptionHandler h = new Thread.UncaughtExceptionHandler() {
	    	// By default, thread is t1
	    	private Thread t = t1;
	    	
	        @Override
	        public void uncaughtException(Thread th, Throwable ex) {
	            logger.error("An exception occurred while watching file");
	            
	            // Create new thread, reset service
	            t = new Thread(fl, "File Listener");
	            t.setUncaughtExceptionHandler(this);
	            watchFileItem.setSelected(false);
	            fl.unwatch();
	            t.start();
	        }
	    };
	    t1.setUncaughtExceptionHandler(h);
	    t1.start();
	    
	    // Make the watch file menu item work
		watchFileItem.addItemListener(new ItemListener() {
			File watchedFile = null;
			
			@Override
	        public void itemStateChanged(ItemEvent e) {
				if (watchFileItem.getState()) {
					// Watch
					watchedFile = fileSelect.getSelectedFile();
					logger.info("Watching "+watchedFile+" for changes");
					fl.watchFile(watchedFile);
	            }
				else {
					// Remove watch
					if (watchedFile != null) {
						logger.info("Stopped watching "+watchedFile);
					}
					fl.unwatch();
				}
			}
		});
		
		// Make the exit menu item work
		exitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (!warnUnsavedChanges()) {
					return;
				}
				logger.trace("Closed instance\n");
				System.exit(0);
			}
		});
		
		// Make the close window button work
		frame.addWindowListener(new WindowAdapter() {
		    public void windowClosing(WindowEvent e) {
		    	if (!warnUnsavedChanges()) {
					return;
				}
		    	logger.trace("Closed instance\n");
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
					logger.info("Opened Windows Firewall");
				}
				catch (IOException e) {
					logger.info("An error occurred while opening Windows Firewall");
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
				
				Collection<Player> playerList = storage.getPlayerList();
				playerList.add(currentPlayer);
				storage.setPlayerList(playerList);
				
				((DefaultListModel<Player>) playerJList.getModel()).addElement(currentPlayer);
				playerJList.setSelectedIndex(playerJList.getModel().getSize() - 1);
				
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
	        		mainPanel.remove(consoleMain);
	        		mainPanel.add(playerMain, BorderLayout.CENTER);
	        		frame.repaint();
	        		frame.validate();
	        	}
	        	else {
	        		mainPanel.remove(playerMain);
	        		mainPanel.add(consoleMain, BorderLayout.CENTER);
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
							"netsh advfirewall firewall set rule name=\"GTA V Block\" new enable=no";
					new ProcessBuilder("cmd", "/c", command).start().waitFor();
					logger.info("Unblocked all");
				}
				catch (IOException | InterruptedException e) {
					logger.info("An error occurred while modifying the firewall");
				}
			}
		});
		
		// Make the block button work
		blockButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				storage.updateFirewallRules();
				try {
					String command = 
							"netsh advfirewall firewall set rule name=\"GTA V Block\" new enable=yes";
					new ProcessBuilder("cmd", "/c", command).start().waitFor();
					logger.info("Blocked all but friends");
				}
				catch (IOException | InterruptedException e) {
					logger.info("An error occurred while modifying the firewall");
				}
			}
		});
		
		// Make the block all button work
		blockAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				storage.updateFirewallRules(new ArrayList<Player>());
				try {
					String command = 
							"netsh advfirewall firewall set rule name=\"GTA V Block\" new enable=yes";
					new ProcessBuilder("cmd", "/c", command).start().waitFor();
					logger.info("Blocked all");
				}
				catch (IOException | InterruptedException e) {
					logger.info("An error occurred while modifying the firewall");
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
					
					Collection<Player> playerList = storage.getPlayerList();
					playerList.remove(selectedPlayer);
					storage.setPlayerList(playerList);
					
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
				mainPanel.add(consoleMain, BorderLayout.CENTER);
				frame.repaint();
        		frame.validate();
			}
		});
		
		// Draw the main frame
		frame.setVisible(true);
	}

}
