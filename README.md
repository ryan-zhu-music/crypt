# CRYPT

### A grade 11 computer science project.

---

## FEATURES

Logistics:

- 10 levels
- Clues placed around the map
- For each level after the first, the player will need to perform some sort of transformation/encryption/decryption to the previous level’s password.
- Obstacles such as lava and moving walls will trigger restarts.
- Each level features a different cryptosystem and step in the decryption process.

Input:

- Left/Right arrow keys to move left and right
- Up to jump
- TAB to interact with hints/doors
- Letters and numbers to enter passwords

New features:

- Save/Load games (various game info will be saved/loaded, such as location, level, and number of hints collected)
- Portals will teleport the player across the map
- Moving platforms

Known bugs:

- When transitioning between levels, sometimes paintComponent() is called before initializeLevel(), which results in the player being able to pass through walls, portals not working, lava not triggering restarts, etc.
- Clicking too many buttons too quickly may cause the program to be stuck on a single button.
- Sometimes on level 9, if the player is crushed by the moving platform the are simply pushed to the side rather than restarting.
- Sometimes when interacting with hints, doors or the level info, more than one dialog will open at once.
- In rare occasions, certain sound effects (usually portal sounds) will not play.

Developer mode:
Enter the settings page to toggle on/off developer mode. When developer mode is activated, the following cheats are enabled:

- Click the mouse on the screen to immediately teleport the player to that location
- Press any of the number keys to immediately change to the corresponding level (e.g. 0 => level 0, 9 => level 9)
- None of the hints in a level are required to open the door
- Enter "pass" to open a door

---
