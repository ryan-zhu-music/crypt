package Crypt;

/* Name: Ryan Zhu
 * Date: June 21st, 2022
 * Course: ICS3U1
 * Assignment: ISU
 */

/**********************************************************
 ________  ________      ___    ___ ________  _________   
|\   ____\|\   __  \    |\  \  /  /|\   __  \|\___   ___\ 
\ \  \___|\ \  \|\  \   \ \  \/  / | \  \|\  \|___ \  \_| 
 \ \  \    \ \   _  _\   \ \    / / \ \   ____\   \ \  \  
  \ \  \____\ \  \\  \|   \/  /  /   \ \  \___|    \ \  \ 
   \ \_______\ \__\\ _\ __/  / /      \ \__\        \ \__\
    \|_______|\|__|\|__|\___/ /        \|__|         \|__|
                       \|___|/                            
                                                          

Combine your platforming skills with puzzle-solving skills
in this cryptography-themed escape room! 

Navigate through the map, get all the hints, and undo the 
multi-step decryption one level at a time.

Explore some common cryptosystems such as [REDACTED] and
[REDACTED]!! [REDACTED] is a really fun one ;)

***********************************************************/

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.sound.sampled.*;

public class Main extends JPanel implements ActionListener {
	JFrame frame;
	JPanel home, game, about, instructions, settings;
	JLabel currentTheme;
	JButton developerOption, musicOption, soundOption;

	static final int WIDTH = 1200;
	static final int HEIGHT = 650;

	final Font BUTTON_FONT = new Font(Font.MONOSPACED, Font.BOLD, 20);

	final Color RED = Color.decode("#B74D4D");
	final Color GREEN = Color.decode("#9CB682");
	final Color WHITE = Color.decode("#C7C5C9");
	final Color GREY = Color.decode("#6E6E6E");

	final Color BACKGROUND = Color.decode("#121212");

	// game settings
	int theme = 0;
	String[] themes = { "Castle", "Cave", "Temple", "Space", "Ocean" };
	boolean developerMode = false;
	boolean musicOn = true;
	boolean soundOn = true;

	// game stats
	static String name = "Unnamed Player";
	int level = 0;
	int time = 0;
	int restarts = 0;

	Clip menuMusic, gameMusic, startSound, clickSound;

	// constructor
	public Main() {
		frame = new JFrame("Game");
		frame.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setLocation(0, 0);
		frame.setBackground(BACKGROUND);

		// keyboard listener
		KeyboardFocusManager.getCurrentKeyboardFocusManager()
				.addKeyEventDispatcher(new KeyEventDispatcher() {
					@Override
					public boolean dispatchKeyEvent(KeyEvent e) {
						if (e.getID() == KeyEvent.KEY_PRESSED) {
							if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
								int input = JOptionPane.showConfirmDialog(null, "Exit game?", "Exit",
										JOptionPane.YES_NO_OPTION);
								if (input == JOptionPane.YES_OPTION) {
									if (!developerMode) {
										saveData();
									}
									gameMusic.close();
									frame.setVisible(false);
									frame.getContentPane().removeAll();
									frame.add(home);
									frame.validate();
									frame.setVisible(true);
								}
							} else if (e.getKeyCode() == KeyEvent.VK_TAB) {
								if (((Game) game).getGameOver()) {
									if (!developerMode) {
										saveData();
									}
									gameMusic.close();
									frame.setVisible(false);
									frame.getContentPane().removeAll();
									frame.add(home);
									frame.validate();
									frame.setVisible(true);
								}
							}
						}
						return false;
					}
				});

		// setup sounds
		try {
			AudioInputStream sound = AudioSystem.getAudioInputStream(new File("./assets/audio/Ryan Zhu - Fallen.wav"));
			menuMusic = AudioSystem.getClip();
			menuMusic.open(sound);
			menuMusic.loop(Clip.LOOP_CONTINUOUSLY);

			sound = AudioSystem.getAudioInputStream(new File("./assets/audio/Background.wav"));
			gameMusic = AudioSystem.getClip();
			gameMusic.open(sound);

			sound = AudioSystem.getAudioInputStream(new File("./assets/audio/Correct.wav"));
			startSound = AudioSystem.getClip();
			startSound.open(sound);

			sound = AudioSystem.getAudioInputStream(new File("./assets/audio/Click.wav"));
			clickSound = AudioSystem.getClip();
			clickSound.open(sound);
		} catch (Exception ex) {
		}

		// home panel
		home = new JPanel();
		home.setLayout(new FlowLayout());
		home.setBackground(BACKGROUND);
		JLabel title = new JLabel(
				new ImageIcon(new ImageIcon("./assets/images/Title.png").getImage().getScaledInstance(400, 200,
						java.awt.Image.SCALE_SMOOTH)));
		title.setAlignmentX(Component.CENTER_ALIGNMENT);

		// menu options
		JButton playButton = new JButton("New Game");
		setupButton(playButton, "Play", BUTTON_FONT, GREEN, 200, 50);
		JButton loadButton = new JButton("Load Game");
		setupButton(loadButton, "Load", BUTTON_FONT, Color.decode("#BCB579"), 200, 50);
		JButton settingsButton = new JButton("Settings");
		setupButton(settingsButton, "Settings", BUTTON_FONT, Color.decode("#C19562"), 200, 50);
		JButton aboutButton = new JButton("About");
		setupButton(aboutButton, "About", BUTTON_FONT, Color.decode("#C77B44"), 200, 50);
		JButton instructionsButton = new JButton("Instructions");
		setupButton(instructionsButton, "Instructions", BUTTON_FONT, RED, 200, 50);

		home.add(title);
		home.add(Box.createRigidArea(new Dimension(1200, 0)));
		home.add(playButton);
		home.add(Box.createRigidArea(new Dimension(1200, 10)));
		home.add(loadButton);
		home.add(Box.createRigidArea(new Dimension(1200, 10)));
		home.add(settingsButton);
		home.add(Box.createRigidArea(new Dimension(1200, 10)));
		home.add(aboutButton);
		home.add(Box.createRigidArea(new Dimension(1200, 10)));
		home.add(instructionsButton);

		// about panel
		about = new JPanel();
		about.setBackground(BACKGROUND);
		JLabel aboutTitle = new JLabel(
				new ImageIcon(new ImageIcon("./assets/images/About Title.png").getImage().getScaledInstance(400, 200,
						java.awt.Image.SCALE_SMOOTH)));
		aboutTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
		JButton aboutBack = new JButton("<");
		setupButton(aboutBack, "aboutBack", new Font(Font.MONOSPACED, Font.BOLD, 70), BACKGROUND, 60, 50);
		aboutBack.setAlignmentX(Component.LEFT_ALIGNMENT);
		JLabel aboutImage = new JLabel(new ImageIcon("./assets/images/About.png"));
		about.add(aboutBack);
		about.add(Box.createRigidArea(new Dimension(200, 0)));
		about.add(aboutTitle);
		about.add(Box.createRigidArea(new Dimension(250, 0)));
		about.add(aboutImage);

		// instructions panel
		instructions = new JPanel();
		instructions.setBackground(BACKGROUND);
		JLabel instructionsTitle = new JLabel(
				new ImageIcon(new ImageIcon("./assets/images/Instructions Title.png").getImage().getScaledInstance(500, 150,
						java.awt.Image.SCALE_SMOOTH)));
		JButton instructionsBack = new JButton("<");
		setupButton(instructionsBack, "instructionsBack", new Font(Font.MONOSPACED, Font.BOLD, 70), BACKGROUND, 60, 80);
		JLabel instructionsImage = new JLabel(new ImageIcon("./assets/images/Instructions.png"));
		instructions.add(instructionsBack);
		instructions.add(Box.createRigidArea(new Dimension(150, 0)));
		instructions.add(instructionsTitle);
		instructions.add(Box.createRigidArea(new Dimension(200, 0)));
		instructions.add(instructionsImage);

		// settings panel
		settings = new JPanel();
		settings.setLayout(new FlowLayout());
		settings.setBackground(BACKGROUND);
		JLabel settingsTitle = new JLabel(
				new ImageIcon(new ImageIcon("./assets/images/Settings Title.png").getImage().getScaledInstance(400, 200,
						java.awt.Image.SCALE_SMOOTH)));
		JButton settingsBack = new JButton("<");
		setupButton(settingsBack, "settingsBack", new Font(Font.MONOSPACED, Font.BOLD, 70), BACKGROUND, 60, 80);
		settingsBack.setBounds(200, 200, 200, 200);
		settings.add(settingsBack);
		settings.add(Box.createRigidArea(new Dimension(190, 0)));
		settings.add(settingsTitle);
		settings.add(Box.createRigidArea(new Dimension(230, 0)));

		// developer mode
		developerOption = new JButton("Developer Mode");
		setupButton(developerOption, "developerMode", BUTTON_FONT, RED, 200, 50);

		// music on/off
		musicOption = new JButton("Music");
		setupButton(musicOption, "music", BUTTON_FONT, GREEN, 200, 50);

		// sound on/off
		soundOption = new JButton("SFX");
		setupButton(soundOption, "sound", BUTTON_FONT, GREEN, 200, 50);

		JLabel settingsTheme = new JLabel("Theme");
		setupText(settingsTheme, new Font(Font.MONOSPACED, Font.BOLD, 30), WHITE);
		JButton themeBack = new JButton("<");
		currentTheme = new JLabel(themes[theme]);
		setupText(currentTheme, new Font(Font.MONOSPACED, Font.BOLD, 20), WHITE);
		JButton themeForward = new JButton(">");
		setupButton(themeBack, "themeBack", BUTTON_FONT, BACKGROUND, 50, 50);
		setupButton(themeForward, "themeForward", BUTTON_FONT, BACKGROUND, 50, 50);
		settings.add(Box.createRigidArea(new Dimension(1200, 0)));
		settings.add(settingsTheme);
		settings.add(Box.createRigidArea(new Dimension(1200, 0)));
		settings.add(themeBack);
		settings.add(currentTheme);
		settings.add(themeForward);
		settings.add(Box.createRigidArea(new Dimension(1200, 20)));
		developerOption.setBounds(400, 400, 200, 200);
		settings.add(developerOption);
		settings.add(Box.createRigidArea(new Dimension(1200, 20)));
		settings.add(musicOption);
		settings.add(Box.createRigidArea(new Dimension(1200, 20)));
		settings.add(soundOption);

		frame.add(home);
		frame.pack();
		frame.setVisible(true);
	}

	// saves game data on game close by writing to /assets/data.txt
	public void saveData() {
		try {
			Scanner in = new Scanner(new File("./assets/data.txt"));
			String saved = "";
			while (in.hasNextLine()) {
				saved += in.nextLine() + "\n";
			}
			saved = saved.trim();

			ArrayList<String> players = new ArrayList<>(
					Arrays.asList(saved.split("\n-----------------------------------------------------\n")));
			String data = ((Game) game).getData();
			boolean replace = false;
			for (int i = 0; i < players.size(); i++) {
				if (players.get(i).indexOf(name) != -1) {
					players.set(i, data);
					replace = true;
					break;
				}
			}

			if (!replace) {
				players.add(data);
			}

			String result = String.join("\n-----------------------------------------------------\n", players).trim();

			PrintWriter out = new PrintWriter(new FileWriter("./assets/data.txt"));
			out.write(result);
			out.close();
		} catch (IOException e) {
			// e.printStackTrace();
		}
	}

	// sets up JButtons (actions, styling, etc.)
	// arguments: JButton to setup, String actionCommand for actionPerformed(),
	// Font, Color, width, and height for styling
	public void setupButton(JButton button, String actionCommand, Font font, Color color, int width,
			int height) {
		button.setActionCommand(actionCommand);
		button.addActionListener(this);
		button.setFont(font);
		button.setBackground(color);
		button.setForeground(Color.WHITE);
		button.setPreferredSize(new Dimension(width, height));
		button.setBorder(BorderFactory.createEmptyBorder());
		button.setFocusPainted(false);
		button.setAlignmentX(Component.CENTER_ALIGNMENT);
	}

	// sets up JLabel texts
	// arguments: JLabel to setup, font and color for styling
	public void setupText(JLabel label, Font font, Color color) {
		label.setFont(font);
		label.setForeground(color);
		label.setAlignmentX(Component.CENTER_ALIGNMENT);
	}

	public void actionPerformed(ActionEvent e) {
		// themes
		Color[] colors = new Color[6];
		switch (themes[theme]) {
			case "Castle":
				colors[0] = Color.decode("#5AC549");
				colors[1] = Color.decode("#544B47");
				colors[2] = Color.decode("#D34B2D");
				colors[3] = Color.decode("#74593F");
				colors[4] = new Color(222, 64, 140, 190);
				colors[5] = Color.decode("#C02626");
				break;
			case "Cave":
				colors[0] = Color.decode("#C9B673");
				colors[1] = Color.decode("#636363");
				colors[2] = Color.decode("#A874C0");
				colors[3] = Color.decode("#BE8964");
				colors[4] = new Color(167, 198, 172, 190);
				colors[5] = Color.decode("#D9362B");
				break;
			case "Temple":
				colors[0] = Color.decode("#EFEFEF");
				colors[1] = Color.decode("#959173");
				colors[2] = Color.decode("#F4C25F");
				colors[3] = Color.decode("#FFF7AF");
				colors[4] = new Color(87, 72, 255, 190);
				colors[5] = Color.decode("#AA1D14");
				break;
			case "Space":
				colors[0] = Color.decode("#BAA4EA");
				colors[1] = Color.decode("#5F67AF");
				colors[2] = Color.decode("#D0BC61");
				colors[3] = Color.decode("#CFA7CB");
				colors[4] = new Color(159, 189, 224, 190);
				colors[5] = Color.decode("#6AD645");
				break;
			case "Ocean":
				colors[0] = Color.decode("#83E799");
				colors[1] = Color.decode("#4B5D8B");
				colors[2] = Color.decode("#BFA35B");
				colors[3] = Color.decode("#524327");
				colors[4] = new Color(121, 191, 195, 190);
				colors[5] = Color.decode("#EAA994");
				break;
			default:
				break;
		}
		boolean cancel = false;
		// click sound
		if (e.getID() == ActionEvent.ACTION_PERFORMED && soundOn) {
			clickSound.setMicrosecondPosition(0);
			clickSound.start();
		}
		// switch for buttons
		switch (e.getActionCommand()) {
			case "Play":
				if (!developerMode) {
					name = JOptionPane.showInputDialog(game, "Please enter a name:", "Name", JOptionPane.PLAIN_MESSAGE);
					if (name == null || name.equals("")) {
						break;
					}
					while (checkName()) {
						name = JOptionPane.showInputDialog(game, "That name has been taken already. Please enter a new name:",
								"Name",
								JOptionPane.ERROR_MESSAGE);
						if (name == null || name.equals("")) {
							cancel = true;
							break;
						}
					}
					if (cancel) {
						break;
					}
				}

				frame.remove(home);
				game = new Game();
				((Game) game).setColors(colors);
				((Game) game).setDeveloperMode(developerMode);
				((Game) game).setSound(soundOn);
				((Game) game).setTheme(themes[theme]);
				((Game) game).setName(name);

				if (musicOn) {
					menuMusic.stop();
					gameMusic.loop(Clip.LOOP_CONTINUOUSLY);
				}
				if (soundOn)
					startSound.start();

				frame.add(game);
				frame.validate();
				break;
			case "Load":
				name = JOptionPane.showInputDialog(game, "Please enter a name:", "Name", JOptionPane.PLAIN_MESSAGE);
				if (name == null || name.equals("")) {
					break;
				}
				cancel = false;
				while (!checkName()) {
					name = JOptionPane.showInputDialog(game, "That player does not exist. Please enter an existing player name:",
							"Name", JOptionPane.ERROR_MESSAGE);
					if (name == null || name.equals("")) {
						cancel = true;
						break;
					}
				}
				if (cancel) {
					break;
				}

				frame.remove(home);
				game = new Game();
				((Game) game).setColors(colors);
				((Game) game).setDeveloperMode(developerMode);
				((Game) game).setSound(soundOn);
				((Game) game).setTheme(themes[theme]);
				((Game) game).setName(name);
				((Game) game).loadData(name);

				if (musicOn) {
					menuMusic.stop();
					gameMusic.loop(Clip.LOOP_CONTINUOUSLY);
				}
				if (soundOn)
					startSound.start();

				frame.add(game);
				frame.validate();
				break;
			case "About":
				frame.getContentPane().removeAll();
				frame.setVisible(false);
				frame.add(about);
				frame.pack();
				frame.setVisible(true);
				break;
			case "Instructions":
				frame.getContentPane().removeAll();
				frame.setVisible(false);
				frame.add(instructions);
				frame.pack();
				frame.setVisible(true);
				break;
			case "Settings":
				frame.getContentPane().removeAll();
				frame.setVisible(false);
				frame.add(settings);
				frame.pack();
				frame.setVisible(true);
				break;
			case "aboutBack":
				frame.getContentPane().removeAll();
				frame.setVisible(false);
				frame.add(home);
				frame.pack();
				frame.setVisible(true);
				break;
			case "instructionsBack":
				frame.getContentPane().removeAll();
				frame.setVisible(false);
				frame.add(home);
				frame.pack();
				frame.setVisible(true);
				break;
			case "settingsBack":
				frame.getContentPane().removeAll();
				frame.setVisible(false);
				frame.add(home);
				frame.pack();
				frame.setVisible(true);
				break;
			case "themeBack":
				theme--;
				if (theme < 0) {
					theme = themes.length - 1;
				}
				currentTheme.setText(themes[theme]);
				break;
			case "themeForward":
				theme++;
				if (theme >= themes.length) {
					theme = 0;
				}
				currentTheme.setText(themes[theme]);
				break;
			case "developerMode":
				developerMode = !developerMode;
				developerOption.setBackground(developerMode ? GREEN : RED);
				break;
			case "music":
				musicOn = !musicOn;
				musicOption.setBackground(musicOn ? GREEN : RED);
				if (musicOn)
					menuMusic.loop(Clip.LOOP_CONTINUOUSLY);
				else
					menuMusic.stop();
				break;
			case "sound":
				soundOn = !soundOn;
				soundOption.setBackground(soundOn ? GREEN : RED);
				break;
			default:
				break;
		}
	}

	// checks if name exists already
	public static boolean checkName() {
		boolean exists = false;
		try {
			Scanner in = new Scanner(new File("./assets/data.txt"));
			String line = in.nextLine();
			while (in.hasNextLine()) {
				if (line.equals("Name:" + name)) {
					exists = true;
					break;
				}
				line = in.nextLine();
			}
			in.close();
		} catch (Exception e) {
			// e.printStackTrace();
		}
		return exists;
	}

	public static void main(String[] args) {
		new Main();
	}

}
