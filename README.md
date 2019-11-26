# GTA V Helper

This application will enable the user to automatically block ports for Grand Theft Auto Online, which allows
the user to play in a 'public' session by themselves or with friends only. A public session has more benefits
than the in-game friends-only session.

## Getting Started

### Prerequisites

Windows 7 or later and Java SE 8 or later are required for the program to function correctly.
Additionally, administrator privileges are required to modify the Windows Firewall.  

### Installing

This is a standalone application, so no installation is required. It is recommended to extract the contents
of the zip to an empty folder.  

However, first time firewall setup is required.  
The batch file setup.bat will remove any default firewall rules for Grand Theft Auto V, and replace them
with its own. Read the rest of this section before continuing.  

You are advised to create a backup of the Windows Firewall before using setup.bat. To do so, open Windows
Firewall with Advanced Security (Run -> wf.msc), and right click on on the main tab on the left
(Windows Firewall with Advanced Security on Local Computer). Then export policy and save the backup.  

You also must set the location of GTA5.exe (the game) before running setup.bat. The location of GTA5.exe
is in the main game folder. Edit setup.bat, and replace the given location (on line 3) with your location.
Make sure that you keep the double quotes around the location.  

Then run setup.bat as administrator.  

### Using the Application

You can add friends on the left panel with the 'Add...' button. You then need to provide their name and
their public IP address. To edit an existing friend, simply select them on the left.

The firewall rules created previously are automatically updated when you save in the application. It is
recommended to save the information file with the default name (info.bin) as this file gets automatically
loaded when the application starts.

When a friend is not selected, you are able to enable / disable the blocker with the buttons below the
output console. Remember, you cannot join another player while your blocker is active, and non-friends
cannot join you.

The blocking state is remembered between sessions. This means if you leave the blocker on when you close
the application, it will continue blocking while not open.

### Troubleshooting

Public IPs are often not static, and can change seemingly randomly. If a friend cannot join you, get them
to send you their IP again. Chances are their IP has changed automatically.

If you get randomly disconnected from GTA Online, it is likely because your friend was the host of the
session instead of you, and somebody not on your allowed IP list (a random) was matchmade into the
session. This results in your game not being able to communicate with the random player, and you get
disconnected. To ensure that you are the host, use the 'block all' feature to get into a session with
only yourself, swap to the regular block all but friends, and then invite your friends.

## Built With

* [Launch4J](https://sourceforge.net/projects/launch4j/) - Conversion from .jar to .exe

## Versioning

2.2.2.1 - Minor fixes
2.2.2.0 - Fixed block / block all sometimes not changing the firewall
2.2.1.0 - Automated firewall rule creation, view firewall shortcut, numerous bug fixes
2.2.0.2 - Bug fixes
2.2.0.1 - Bug fixes
2.2.0.0 - Console log feature
2.1.0.0 - Multiple save feature, bug fixes
2.0.0.0 - Initial Java GUI implementation
1.0.0.0 - Initial Python command line implementation

## Authors
**func_vehicle**
