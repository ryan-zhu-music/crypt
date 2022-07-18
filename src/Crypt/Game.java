/* Name: Ryan Zhu
 * Date: June 21st, 2022
 * Course: ICS3U1
 * Assignment: ISU
 */

package Crypt;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.sound.sampled.*;

// Game class
class Game extends JPanel implements Runnable, MouseListener {
  Thread thread;
  final int FPS = 60;
  final int WIDTH = 1200;
  final int HEIGHT = 615;

  private Color PLAYER_COLOR = Color.decode("#5E478F");
  private Color WALL_COLOR = Color.decode("#B45E5E");
  private Color HINT_COLOR = Color.decode("#D6B82F");
  private Color DOOR_COLOR = Color.decode("#422E15");
  private Color PORTAL_COLOR = Color.decode("#9FBDE0");
  private Color LAVA_COLOR = Color.decode("#D9362B");

  int level = 0; // can change this for debugging
  private String name = "Unnamed Player";
  private String THEME = "Castle";
  private boolean DEVELOPER_MODE = true; // enables "cheat" features
  private boolean SOUND_ON = true;
  Map<String, Clip> soundEffects = new HashMap<String, Clip>();

  private int restarts = 0;
  long startTime;
  private long totalTime = 0;
  long time = 0;
  private boolean gameOver = false;

  Rectangle player = new Rectangle(585, 535, 30, 30);
  Rectangle[][] walls = new Rectangle[11][];
  Rectangle[][] hints = new Rectangle[11][];
  Rectangle[][] lava = new Rectangle[11][];
  Rectangle[] door = new Rectangle[11];

  // for levels 4-8 (moving platforms)
  int[][] wallSpeeds = new int[5][];
  char[][] wallDirections = new char[5][];
  int[][][] wallBoundaries = new int[5][][];

  // for level 8-9 (portals)
  Rectangle[][] portals = new Rectangle[2][];
  int[][] nodes = {
      { 2, 3, 0, 1 },
      { 3, 17, 11, 0, 20, 16, 10, 14, 12, 24, 14, 19, 8, 18, 7, 23, 5, 1, 13, 1, 4, 10, 19, 15, 9, 19, 10, 10 }
  };
  String[] locations = { "rldd", "lllddrdrrlrdldrrldldrddldddd" };
  int[][] portalBrightnesses = new int[2][];
  boolean[][] portalIncreasing = new boolean[2][];

  // for level 5 (lava)
  boolean rising = true;
  int lavaFrames = 0;

  // player movement
  boolean jump = false;
  boolean left, right;
  boolean up = false;

  double jumpSpeed = 11;
  double jumpCap = 0;
  double xVelocity = 0;
  double yVelocity = 0;
  final double ACCELERATION = 0.2;
  final double MAX_SPEED = 7;
  final double GRAVITY = 0.8;

  int wallCollisionIndex = -1;
  boolean hitBottom = false;
  boolean onTop = false;
  boolean interact = false;
  boolean show = false;

  boolean changingLevel = false;
  int opacity = 255;

  String hintsRevealed = "";

  // hints for each level
  final String HINT_MESSAGES[][] = {
      { "635635817835759", "91140695620031", "2490736001390167", "130164967747870751819077" },
      { ".reverse()", "0x", "lmbg...ovnm" },
      { "\"1\" + \"1\"", "Et tu, Brute?", "Try forwards and backwards." },
      { "A <=> Z", "M <=> N", "Surely I don't have to list out all the substitutions for you..." },
      { "PLAYFAIRNOCHEATING",
          "Append the rest of the alphabet to the key, and follow the rules. No V, though.",
          "Remove the last letter from your decrypted plaintext. It should be 'x'." },
      { "This cipher is indechiffrable!", "Here's a key: <",
          "Surely a key can't be a single character. I wonder what that symbol is..." },
      { "This level is very basic.", "The password will be a base-10 string...hmmm...", "a = 0, b = 1 ... z = 25" },
      { "D0_3><C1u51\\/30r",
          "You will need two numbers to perform the operation. You already have one...what's the other?" },
      { "What makes this Really Secure Algorithm secure?", "Your answer should be in the same format as the clues.",
          "N = C0F87BD824FEC5D271121B4570EC6CCB", "E = 3" },
      { "Look up!", "Lost? You may want to try draw out the graph :)" },
      { "These ciphers were waaay too easy. Maybe I'll try some AES cracking next time...", "CRYPT 2 for 2023.... ;)" }
  };

  // no lookey here
  final String PASSWORDS[] = {
      "18782029301900430529808328724994339846446411540427210086582652781411",
      "lmbgnfvlmuezdmhjerybhoubovnm",
      "abqvcukabjtosbwytgnqwdjqdkcb",
      "zyjexfpzyqglhydbgtmjdwqjwpxy",
      "xferzyfsfmeygpqhtekdksmdsam",
      "voazxusprknuonmuqqimgakzfxy",
      "132602421809299633443962044849570667050",
      "53096815195807772403100423577508276824",
      "4f41243847da693a4f356c0486114bc6",
      "deadbeef",
  };

  // level info
  final String LEVEL_MESSAGES[] = {
      "Welcome to the first level!\nFamiliarize yourself with the controls, because things will pick up fairly quickly ;)",
      "Not bad. Here's a tip: Sometimes hints are hidden in the layout of the map itself.",
      "Red = lava = bad!\nDon't believe me? Try it yourself :)\n\nLevel designed by Ishaan Dey.",
      "Use this level to practice your timing and accuracy.",
      "Woah, moving platforms!",
      "Time is of the essence.",
      "You're pretty good at this. Good luck with this one, though.",
      "Take this moment to relax a little. Before you lose your sanity with the next two levels.",
      "This is the hardest level. I promise. Good luck ;)",
      "Well, well, well. You've made it to the final level. As a prize, enjoy this very easy level :)",
      "Congratulations...didn't think you'd make it this far. Well, there's the door. \nNothing else to see here. Until next time..."
  };

  // constructor
  public Game() {
    // setup audio
    try {
      String sounds[] = { "Click", "Correct", "Incorrect", "Hint", "Door", "Error", "Portal", "Lava", "Crush" };
      for (int i = 0; i < sounds.length; i++) {
        AudioInputStream sound = AudioSystem.getAudioInputStream(new File("./assets/audio/" + sounds[i] + ".wav"));
        soundEffects.put(sounds[i], AudioSystem.getClip());
        soundEffects.get(sounds[i]).open(sound);
      }
    } catch (Exception e) {
      // e.printStackTrace();
    }

    setPreferredSize(new Dimension(WIDTH, HEIGHT));
    setVisible(true);
    setLayout(null);

    // handle key presses
    KeyboardFocusManager.getCurrentKeyboardFocusManager()
        .addKeyEventDispatcher(new KeyEventDispatcher() {
          @Override
          public boolean dispatchKeyEvent(KeyEvent e) {
            boolean pressed = true;
            int key = e.getKeyCode();
            if (!show) {
              if (e.getID() == KeyEvent.KEY_RELEASED) {
                pressed = false;
              }
              if (key == KeyEvent.VK_UP) {
                jump = pressed;
              }
              if (key == KeyEvent.VK_RIGHT) {
                right = pressed;
                if (right && !left && xVelocity < 0 || jump) {
                  xVelocity = 2;
                }
              }
              if (key == KeyEvent.VK_LEFT) {
                left = pressed;
                if (left && !right && xVelocity > 0 || jump) {
                  xVelocity = -2;
                }
              }
              if (key == KeyEvent.VK_TAB) {
                if (interact && !show)
                  show = true;
              }

              if (left && right) {
                left = false;
                right = false;
              }
              if (DEVELOPER_MODE) {
                // number key pressed
                if (key >= KeyEvent.VK_0 && key <= KeyEvent.VK_9 && !show) {
                  // show message dialog to confirm
                  level = key - KeyEvent.VK_0;
                  jumpSpeed = 11;
                  initializeLevel();
                }
                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                  initializeLevel();
                }
              }
            }
            return false;
          }
        });

    addMouseListener(this);

    thread = new Thread(this);
    thread.start();

    startTime = System.currentTimeMillis();
  }

  // setter for game colors
  // argument: array of colors
  public void setColors(Color[] colors) {
    this.PLAYER_COLOR = colors[0];
    this.WALL_COLOR = colors[1];
    this.HINT_COLOR = colors[2];
    this.DOOR_COLOR = colors[3];
    this.PORTAL_COLOR = colors[4];
    this.LAVA_COLOR = colors[5];
  }

  // setter for developer mode
  // argument: boolean for on/off
  public void setDeveloperMode(boolean developerMode) {
    this.DEVELOPER_MODE = developerMode;
  }

  // setter for sound
  // argument: boolean for on/off
  public void setSound(boolean sound) {
    this.SOUND_ON = sound;
  }

  // setter for theme
  // argument: string corresponding to the theme colors
  public void setTheme(String theme) {
    this.THEME = theme;
  }

  // setter for player name
  // argument: string player name
  public void setName(String name) {
    this.name = name;
  }

  // getter for gameOver state
  // return: boolean gameOver or not
  public boolean getGameOver() {
    return this.gameOver;
  }

  // getter for game info
  // returns a string
  public String getData() {
    String data = String.format(
        "Name:%s\nLevel:%d\nTime:%d\nRestarts:%d\nX:%d\nY:%d\nHints:%s", name, level, totalTime + time,
        restarts, player.x, player.y, hintsRevealed);

    return data.trim();
  }

  // loads a player's game info
  // arguments: String player name
  public void loadData(String name) {
    try {
      Scanner in = new Scanner(new File("./assets/data.txt"));
      String line = in.nextLine();
      while (line.indexOf(name) == -1 && in.hasNextLine()) {
        line = in.nextLine();
      }
      line = in.nextLine();
      level = Integer.parseInt(line.substring(line.indexOf(":") + 1));
      line = in.nextLine();
      totalTime = Long.parseLong(line.substring(line.indexOf(":") + 1));
      line = in.nextLine();
      restarts = Integer.parseInt(line.substring(line.indexOf(":") + 1));

      initializeLevel();
      line = in.nextLine();
      player.x = Integer.parseInt(line.substring(line.indexOf(":") + 1));
      line = in.nextLine();
      player.y = Integer.parseInt(line.substring(line.indexOf(":") + 1));
      line = in.nextLine();
      hintsRevealed = line.substring(line.indexOf(":") + 1);
      this.repaint();
      in.close();
    } catch (FileNotFoundException e) {
      // e.printStackTrace();
    }
  }

  // draw frames
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g;

    // try catch because paintComponent is buggy as explained in the README.txt
    try {
      // background
      Image background = Toolkit.getDefaultToolkit().getImage("./assets/images/" + THEME + "/Background.png");
      g.drawImage(background, 0, 0, this);

      // hints[level]
      for (int i = 0; i < hints[level].length; i++) {
        if (hintsRevealed.indexOf(Integer.toString(i)) != -1) {
          g2.setColor(Color.decode("#A3A681"));
        } else {
          g2.setColor(HINT_COLOR);
        }
        g2.fill(hints[level][i]);
      }

      // door[level]
      g2.setColor(DOOR_COLOR);
      g2.fill(door[level]);

      // player
      g2.setColor(PLAYER_COLOR);
      g2.fill(player);

      // floor
      Image floor = Toolkit.getDefaultToolkit().getImage("./assets/images/" + THEME + "/Floor.png");
      g.drawImage(floor, 0, HEIGHT - 50, this);

      // walls[level]
      g2.setColor(WALL_COLOR);
      for (int i = 0; i < walls[level].length; i++) {
        if (!(level == 0 && i >= 3 && i <= 4)) {
          g2.fill(walls[level][i]);
        }
      }

      // lava
      g2.setColor(LAVA_COLOR);
      for (int i = 0; i < lava[level].length; i++)
        g2.fill(lava[level][i]);

      switch (level) {
        case 0:
          g2.setColor(new Color(0, 0, 0, 0));
          g2.fill(walls[level][3]);
          g2.fill(walls[level][4]);
          g2.setColor(WALL_COLOR);
          g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 340));
          g2.drawString("*", 493, 450);
          break;
        case 1:
          break;
        case 2:
          break;
        case 3:
          break;
        case 4:
          break;
        case 5:
          lavaFrames++;
          if (lavaFrames % 5 == 0) {
            if (rising)
              lava[level][0].y -= 1;
            else
              lava[level][0].y += 1;
          }
          if (lava[level][0].y < 115)
            rising = false;
          else if (lava[level][0].y > 565)
            rising = true;
          break;
        case 6:
          break;
        case 7:
          break;
        case 8:
          animatePortals(g2);
          break;
        case 9:
          animatePortals(g2);
          break;
        case 10:
          break;
      }

      g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, 18));

      // level info

      g2.setColor(Color.WHITE);
      g2.fillRect(12, 577, 25, 25);
      g2.setColor(Color.BLACK);
      g2.drawString("i", 18, 596);

      // restarts
      g2.setColor(Color.WHITE);
      g2.drawString("Restarts: " + Integer.toString(restarts), 170, 596);

      // timer
      g2.drawString(getTime(), 60, 596);

      // (TAB) tip
      if (interact) {
        g2.drawString("(TAB)", 1090, 596);
      }

      // fade transition between levels
      g2.setColor(new Color(0, 0, 0, opacity));
      g2.fillRect(0, 0, 1200, 615);
    } catch (Exception e) {
      // e.printStackTrace();
    }
  }

  // fade in/out animation for portals
  // arguments: Graphics2D object
  public void animatePortals(Graphics2D g2) {
    for (int i = 0; i < portals[level - 8].length; i++) {
      if (portalBrightnesses[level - 8][i] > 220) {
        portalIncreasing[level - 8][i] = false;
        portalBrightnesses[level - 8][i] = 220;
      } else if (portalBrightnesses[level - 8][i] < 60) {
        portalIncreasing[level - 8][i] = true;
        portalBrightnesses[level - 8][i] = 60;
      }
      if (portalIncreasing[level - 8][i]) {
        portalBrightnesses[level - 8][i]++;
        PORTAL_COLOR = new Color(PORTAL_COLOR.getRed(), PORTAL_COLOR.getGreen(),
            PORTAL_COLOR.getBlue(), portalBrightnesses[level - 8][i]);
      } else {
        portalBrightnesses[level - 8][i]--;
        PORTAL_COLOR = new Color(PORTAL_COLOR.getRed(), PORTAL_COLOR.getGreen(),
            PORTAL_COLOR.getBlue(), portalBrightnesses[level - 8][i]);
      }
      g2.setColor(PORTAL_COLOR);
      g2.fill(portals[level - 8][i]);
    }
  }

  // main loop
  @Override
  public void run() {
    initializeLevel();
    while (true) {
      try {
        update();
      } catch (Exception e) {
        // e.printStackTrace();
        // sometimes update throws an error when paintcomponent is called before
        // initializelevel is finished; not a big deal though
      }
      if (soundEffects.get("Incorrect").getMicrosecondLength() == soundEffects.get("Incorrect")
          .getMicrosecondPosition()) {
        soundEffects.get("Incorrect").stop();
      }
      this.repaint();
      sleep();
    }
  }

  // delay
  public void sleep() {
    try {
      Thread.sleep(1000 / FPS);
    } catch (Exception e) {
      System.out.println(("Thread error"));
    }
  }

  // update the walls, hints, and door for each level, resets variables
  public void initializeLevel() {
    player.x = 585;
    player.y = 535;
    interact = false;
    show = false;
    jump = false;
    left = false;
    right = false;
    opacity = 255;
    hintsRevealed = "";
    wallCollisionIndex = -1;

    switch (level) {
      case 0:
        player.x = 557;
        int[][] dimensions0 = {
            { 1061, 93, 139, 30 },
            { 222, 178, 139, 30 },
            { 869, 151, 122, 30 },
            { 504, 230, 102, 86 },
            { 542, 202, 30, 28 },
            { 647, 218, 152, 30 },
            { 647, 278, 152, 30 },
            { 0, 307, 139, 30 },
            { 1041, 347, 159, 30 },
            { 118, 463, 227, 30 },
            { 617, 343, 30, 222 },
            { 799, 414, 159, 30 }
        };

        walls[level] = new Rectangle[dimensions0.length];
        for (int i = 0; i < dimensions0.length; i++) {
          walls[level][i] = new Rectangle(dimensions0[i][0], dimensions0[i][1], dimensions0[i][2], dimensions0[i][3]);
        }

        hints[level] = new Rectangle[4];
        hints[level][0] = new Rectangle(23, 288, 20, 20);
        hints[level][1] = new Rectangle(713, 258, 20, 20);
        hints[level][2] = new Rectangle(1159, 327, 20, 20);
        hints[level][3] = new Rectangle(1149, 545, 20, 20);

        lava[level] = new Rectangle[0];

        door[level] = new Rectangle(1131, 23, 50, 70);
        break;
      case 1:
        player.x = 177;
        player.y = 205;
        int[][] dimensions1 = {
            { 0, 0, 310, 30 },
            { 0, 30, 30, 390 },
            { 71, 235, 209, 30 },
            { 0, 420, 239, 30 },
            { 280, 235, 30, 215 },
            { 384, 60, 216, 30 },
            { 354, 60, 30, 420 },
            { 384, 450, 216, 30 },
            { 354, 60, 30, 420 },
            { 384, 450, 216, 30 },
            { 636, 245, 163, 30 },
            { 636, 310, 163, 30 },
            { 889, 0, 30, 137 },
            { 889, 172, 30, 393 },
            { 919, 535, 81, 30 }
        };

        walls[level] = new Rectangle[dimensions1.length];
        for (int i = 0; i < dimensions1.length; i++) {
          walls[level][i] = new Rectangle(dimensions1[i][0], dimensions1[i][1], dimensions1[i][2],
              dimensions1[i][3]);
        }

        hints[level] = new Rectangle[3];
        hints[level][0] = new Rectangle(157, 400, 20, 20);
        hints[level][1] = new Rectangle(472, 40, 20, 20);
        hints[level][2] = new Rectangle(708, 290, 20, 20);

        lava[level] = new Rectangle[1];
        lava[level][0] = new Rectangle(1000, 550, 200, 15);

        door[level] = new Rectangle(935, 465, 50, 70);
        break;
      case 2:
        player.x = 183;
        player.y = 235;
        int[][] dimensions2 = {
            { 175, 90, 25, 25 },
            { 275, 90, 25, 25 },
            { 0, 465, 85, 30 },
            { 90, 365, 55, 30 },
            { 479, 178, 30, 387 },
            { 145, 265, 105, 30 },
            { 343, 216, 30, 349 },
            { 553, 0, 30, 502 },
            { 583, 90, 30, 30 },
            { 684, 135, 30, 30 },
            { 804, 175, 30, 30 },
            { 669, 465, 100, 30 },
            { 779, 365, 100, 30 },
            { 889, 265, 100, 30 },
            { 1070, 207, 130, 30 }
        };
        walls[level] = new Rectangle[dimensions2.length];
        for (int i = 0; i < dimensions2.length; i++) {
          walls[level][i] = new Rectangle(dimensions2[i][0], dimensions2[i][1], dimensions2[i][2], dimensions2[i][3]);
        }

        hints[level] = new Rectangle[3];
        hints[level][0] = new Rectangle(178, 70, 20, 20);
        hints[level][1] = new Rectangle(293, 545, 20, 20);
        hints[level][2] = new Rectangle(590, 70, 20, 20);

        lava[level] = new Rectangle[2];
        lava[level][0] = new Rectangle(373, 564, 106, 15);
        lava[level][1] = new Rectangle(804, 564, 396, 15);

        door[level] = new Rectangle(1120, 137, 50, 70);

        break;
      case 3:
        player.x = 5;

        int[][] dimensions3 = {
            { 0, 107, 81, 30 },
            { 111, 107, 96, 30 },
            { 276, 107, 924, 30 },
            { 0, 219, 113, 30 },
            { 143, 219, 1007, 30 },
            { 0, 334, 39, 30 },
            { 69, 334, 199, 30 },
            { 349, 334, 851, 30 },
            { 0, 449, 113, 30 },
            { 143, 449, 900, 30 },
            { 207, 107, 30, 458 }
        };

        walls[level] = new Rectangle[dimensions3.length];
        for (int i = 0; i < dimensions3.length; i++) {
          walls[level][i] = new Rectangle(dimensions3[i][0], dimensions3[i][1], dimensions3[i][2], dimensions3[i][3]);
        }

        hints[level] = new Rectangle[3];
        hints[level][0] = new Rectangle(3, 87, 20, 20);
        hints[level][1] = new Rectangle(237, 314, 20, 20);
        hints[level][2] = new Rectangle(247, 545, 20, 20);

        lava[level] = new Rectangle[4];
        lava[level][0] = new Rectangle(640, 218, 40, 15);
        lava[level][1] = new Rectangle(623, 333, 40, 15);
        lava[level][2] = new Rectangle(520, 448, 40, 15);
        lava[level][3] = new Rectangle(922, 448, 30, 15);

        door[level] = new Rectangle(1130, 37, 50, 70);
        break;
      case 4:
        player.x = 130;
        player.y = 263;

        int[][] dimensions4 = {
            { 782, 400, 61, 30 },
            { 916, 143, 286, 30 },
            { 0, 293, 680, 30 },
            { 1050, 310, 100, 30 },
            { 1050, 475, 100, 30 },
        };
        wallSpeeds[0] = new int[1];
        wallSpeeds[0][0] = -2;

        wallDirections[0] = new char[1];
        wallDirections[0][0] = 'x';

        wallBoundaries[0] = new int[1][2];
        wallBoundaries[0][0][0] = 30;
        wallBoundaries[0][0][1] = 890;

        walls[level] = new Rectangle[dimensions4.length];
        for (int i = 0; i < dimensions4.length; i++) {
          walls[level][i] = new Rectangle(dimensions4[i][0], dimensions4[i][1], dimensions4[i][2], dimensions4[i][3]);
        }

        hints[level] = new Rectangle[3];
        hints[level][0] = new Rectangle(16, 273, 20, 20);
        hints[level][1] = new Rectangle(1090, 290, 20, 20);
        hints[level][2] = new Rectangle(1090, 455, 20, 20);

        lava[level] = new Rectangle[1];
        lava[level][0] = new Rectangle(0, 564, 1200, 51);

        door[level] = new Rectangle(1130, 73, 50, 70);

        break;
      case 5:
        player.x = 585;
        player.y = 460;

        int[][] dimensions5 = {
            { 11, 118, 30, 57 },
            { 1160, 216, 30, 63 },
            { 11, 320, 30, 63 },
            { 1160, 422, 30, 63 },
            { 0, 86, 59, 30 },
            { 311, 86, 59, 30 },
            { 570, 86, 59, 30 },
            { 829, 86, 59, 30 },
            { 1141, 86, 59, 30 },
            { 151, 179, 104, 30 },
            { 413, 179, 104, 30 },
            { 681, 179, 104, 30 },
            { 940, 179, 104, 30 },
            { 0, 286, 164, 30 },
            { 259, 286, 164, 30 },
            { 518, 286, 164, 30 },
            { 777, 286, 164, 30 },
            { 1036, 286, 164, 30 },
            { 59, 388, 290, 30 },
            { 455, 388, 290, 30 },
            { 851, 388, 290, 30 },
            { 0, 490, 349, 30 },
            { 425, 490, 349, 30 },
            { 850, 490, 349, 30 },
        };
        wallSpeeds[1] = new int[4];
        wallSpeeds[1][0] = 7;
        wallSpeeds[1][1] = -7;
        wallSpeeds[1][2] = 7;
        wallSpeeds[1][3] = -7;

        wallDirections[1] = new char[4];
        wallDirections[1][0] = 'x';
        wallDirections[1][1] = 'x';
        wallDirections[1][2] = 'x';
        wallDirections[1][3] = 'x';

        wallBoundaries[1] = new int[4][2];
        for (int i = 0; i < 4; i++) {
          wallBoundaries[1][i][0] = 10;
          wallBoundaries[1][i][1] = 1161;
        }

        walls[level] = new Rectangle[dimensions5.length];
        for (int i = 0; i < dimensions5.length; i++) {
          walls[level][i] = new Rectangle(dimensions5[i][0], dimensions5[i][1], dimensions5[i][2], dimensions5[i][3]);
        }

        hints[level] = new Rectangle[3];
        hints[level][0] = new Rectangle(10, 470, 20, 20);
        hints[level][1] = new Rectangle(10, 66, 20, 20);
        hints[level][2] = new Rectangle(1160, 66, 20, 20);

        lava[level] = new Rectangle[1];
        lava[level][0] = new Rectangle(0, 564, 1200, 615);

        door[level] = new Rectangle(575, 16, 50, 70);

        break;
      case 6:
        player.x = 1150;
        player.y = 113;
        int dimensions6[][] = {
            { 818, 237, 50, 30 },
            { 611, 355, 50, 30 },
            { 6, 500, 30, 60 },
            { 990, 143, 220, 30 },
            { 92, 73, 30, 388 },
            { 92, 461, 1108, 30 },
            { 324, 431, 30, 30 },
            { 1140, 237, 50, 30 },
        };
        wallSpeeds[2] = new int[3];
        wallSpeeds[2][0] = 2;
        wallSpeeds[2][1] = -3;
        wallSpeeds[2][2] = 4;

        wallDirections[2] = new char[3];
        for (int i = 0; i < 3; i++) {
          wallDirections[2][i] = 'x';
        }

        wallBoundaries[2] = new int[3][2];
        wallBoundaries[2][0][0] = 139;
        wallBoundaries[2][0][1] = 1013;
        wallBoundaries[2][1][0] = 139;
        wallBoundaries[2][1][1] = 1141;
        wallBoundaries[2][2][0] = 5;
        wallBoundaries[2][2][1] = 1070;

        walls[6] = new Rectangle[dimensions6.length];
        for (int i = 0; i < dimensions6.length; i++) {
          walls[6][i] = new Rectangle(dimensions6[i][0], dimensions6[i][1], dimensions6[i][2], dimensions6[i][3]);
        }

        hints[6] = new Rectangle[3];
        hints[6][0] = new Rectangle(97, 53, 20, 20);
        hints[6][1] = new Rectangle(1150, 217, 20, 20);
        hints[6][2] = new Rectangle(329, 411, 20, 20);

        lava[level] = new Rectangle[2];
        lava[level][0] = new Rectangle(940, 143, 50, 30);
        lava[level][1] = new Rectangle(122, 446, 1110, 15);

        door[6] = new Rectangle(1141, 495, 50, 70);
        break;
      case 7:
        player.x = 7;
        player.y = 255;
        int dimensions7[][] = {
            { 202, 285 },
            { 432, 315 },
            { 624, 278 },
            { 836, 248 },
            { 1014, 330 },
            { 0, 92 },
            { 527, 92 },
            { 1127, 92 },
            { 0, 285 },
        };
        wallSpeeds[3] = new int[5];
        wallSpeeds[3][0] = -1;
        wallSpeeds[3][1] = 1;
        wallSpeeds[3][2] = -1;
        wallSpeeds[3][3] = 1;
        wallSpeeds[3][4] = -1;

        wallDirections[3] = new char[5];
        for (int i = 0; i < 5; i++) {
          wallDirections[3][i] = 'y';
        }

        wallBoundaries[3] = new int[5][2];

        for (int i = 0; i < 5; i++) {
          wallBoundaries[3][i][0] = 92;
          wallBoundaries[3][i][1] = 523;
        }

        walls[level] = new Rectangle[dimensions7.length];
        for (int i = 0; i < dimensions7.length; i++) {
          walls[level][i] = new Rectangle(dimensions7[i][0], dimensions7[i][1], 73, 30);
        }

        hints[level] = new Rectangle[2];
        hints[level][0] = new Rectangle(12, 72, 20, 20);
        hints[level][1] = new Rectangle(554, 72, 20, 20);

        lava[level] = new Rectangle[1];
        lava[level][0] = new Rectangle(0, 403, 1200, 212);

        door[level] = new Rectangle(1139, 22, 50, 70);

        break;
      case 8:
        player.x = 510;
        player.y = 65;
        int dimensions8[][] = {
            { 692, 258, 75, 30 },
            { 617, 419, 75, 30 },
            { 9, 165, 44, 30 },
            { 40, 500, 44, 30 },
            { 232, 430, 44, 30 },
            { 319, 380, 44, 30 },
            { 1058, 376, 30, 160 },
            { 382, 0, 30, 565 },
            { 788, 0, 30, 565 },
            { 62, 95, 648, 30 },
            { 96, 125, 30, 343 },
            { 126, 186, 75, 10 },
            { 126, 226, 59, 10 },
            { 818, 95, 303, 30 },
            { 950, 38, 30, 103 },
            { 1021, 125, 30, 61 },
            { 950, 171, 30, 47 },
            { 1091, 157, 30, 61 },
            { 897, 218, 303, 30 },
            { 818, 325, 303, 30 },
            { 897, 438, 303, 30 },
            { 0, 31, 242, 30 },
        };
        wallSpeeds[4] = new int[7];
        wallSpeeds[4][0] = -1;
        wallSpeeds[4][1] = 1;
        wallSpeeds[4][2] = 2;
        wallSpeeds[4][3] = 1;
        wallSpeeds[4][4] = -1;
        wallSpeeds[4][5] = -1;
        wallSpeeds[4][6] = 1;

        wallDirections[4] = new char[7];
        wallDirections[4][0] = 'x';
        wallDirections[4][1] = 'x';
        wallDirections[4][2] = 'y';
        wallDirections[4][3] = 'x';
        wallDirections[4][4] = 'y';
        wallDirections[4][5] = 'y';
        wallDirections[4][6] = 'y';

        wallBoundaries[4] = new int[7][2];
        wallBoundaries[4][0][0] = 421;
        wallBoundaries[4][0][1] = 703;
        wallBoundaries[4][1][0] = 421;
        wallBoundaries[4][1][1] = 703;
        wallBoundaries[4][2][0] = 145;
        wallBoundaries[4][2][1] = 441;
        wallBoundaries[4][3][0] = 9;
        wallBoundaries[4][3][1] = 319;
        wallBoundaries[4][4][0] = 225;
        wallBoundaries[4][4][1] = 440;
        wallBoundaries[4][5][0] = 225;
        wallBoundaries[4][5][1] = 440;
        wallBoundaries[4][6][0] = 355;
        wallBoundaries[4][6][1] = 405;

        walls[level] = new Rectangle[dimensions8.length];
        for (int i = 0; i < dimensions8.length; i++) {
          walls[level][i] = new Rectangle(dimensions8[i][0], dimensions8[i][1], dimensions8[i][2], dimensions8[i][3]);
        }

        hints[level] = new Rectangle[4];
        hints[level][0] = new Rectangle(11, 10, 20, 20);
        hints[level][1] = new Rectangle(130, 206, 20, 20);
        hints[level][2] = new Rectangle(995, 75, 20, 20);
        hints[level][3] = new Rectangle(1164, 545, 20, 20);

        lava[level] = new Rectangle[5];
        lava[level][0] = new Rectangle(126, 125, 256, 61);
        lava[level][1] = new Rectangle(0, 550, 382, 15);
        lava[level][2] = new Rectangle(412, 456, 168, 30);
        lava[level][3] = new Rectangle(618, 456, 170, 30);
        lava[level][4] = new Rectangle(970, 324, 20, 15);

        door[level] = new Rectangle(430, 25, 50, 70);

        int[][] portalDimensions8 = {
            { 284, 0, 70, 30 },
            { 846, 0, 70, 30 },
            { 412, 495, 30, 70 },
            { 758, 495, 30, 70 }
        };

        portals[0] = new Rectangle[portalDimensions8.length];
        portalBrightnesses[0] = new int[portalDimensions8.length];
        portalIncreasing[0] = new boolean[portalDimensions8.length];
        for (int i = 0; i < portalDimensions8.length; i++) {
          portals[0][i] = new Rectangle(portalDimensions8[i][0], portalDimensions8[i][1], portalDimensions8[i][2],
              portalDimensions8[i][3]);
          portalBrightnesses[0][i] = PORTAL_COLOR.getAlpha() + (i - 2) * 50 % 255;
          portalIncreasing[0][i] = true;
        }

        break;
      case 9:
        player.x = 706;
        player.y = 340;
        jumpSpeed = 7;

        int[][] dimensions9 = {
            { 0, 168, 1200, 30 },
            { 0, 370, 1200, 30 },
            { 390, 0, 30, 565 },
            { 781, 0, 30, 565 },
            { 0, 465, 50, 30 },
            { 1140, 465, 50, 30 }
        };

        walls[level] = new Rectangle[dimensions9.length];
        for (int i = 0; i < dimensions9.length; i++) {
          walls[level][i] = new Rectangle(dimensions9[i][0], dimensions9[i][1], dimensions9[i][2], dimensions9[i][3]);
        }

        hints[level] = new Rectangle[2];
        hints[level][0] = new Rectangle(0, 445, 20, 20);
        hints[level][1] = new Rectangle(1165, 445, 20, 20);

        lava[level] = new Rectangle[1];
        lava[level][0] = new Rectangle(512, 369, 176, 15);

        door[level] = new Rectangle(455, 300, 50, 70);

        int[][] portalDimensions9 = {
            { 161, 0, 97, 29 },
            { 551, 0, 97, 29 },
            { 942, 0, 97, 29 },
            { 1156, 0, 29, 70 },
            { 0, 98, 29, 70 },
            { 361, 98, 29, 70 },
            { 420, 98, 29, 70 },
            { 752, 98, 29, 70 },
            { 1156, 98, 29, 70 },
            { 161, 198, 97, 29 },
            { 550, 198, 97, 29 },
            { 1156, 300, 29, 70 },
            { 0, 300, 29, 70 },
            { 361, 300, 29, 70 },
            { 420, 300, 29, 70 },
            { 752, 300, 29, 70 },
            { 811, 300, 29, 70 },
            { 1156, 198, 29, 70 },
            { 161, 400, 97, 29 },
            { 550, 400, 97, 29 },
            { 947, 400, 97, 29 },
            { 0, 495, 29, 70 },
            { 361, 495, 29, 70 },
            { 420, 495, 29, 70 },
            { 752, 495, 29, 70 },
            { 811, 495, 29, 70 },
            { 1156, 495, 29, 70 },
            { 811, 98, 29, 70 },
        };

        portals[1] = new Rectangle[portalDimensions9.length];
        portalBrightnesses[1] = new int[portalDimensions9.length];
        portalIncreasing[1] = new boolean[portalDimensions9.length];
        for (int i = 0; i < portalDimensions9.length; i++) {
          portals[1][i] = new Rectangle(portalDimensions9[i][0], portalDimensions9[i][1], portalDimensions9[i][2],
              portalDimensions9[i][3]);
          portalIncreasing[1][i] = true;
          int alpha = (PORTAL_COLOR.getAlpha() + (i * 20)) % 160 + 60;
          portalBrightnesses[1][i] = alpha;
        }
        break;
      case 10:
        player.x = 38;
        player.y = 535;
        jumpSpeed = 11;

        int[][] dimensions10 = {
            { 412, 143, 165, 30 },
            { 382, 143, 30, 291 },
            { 442, 278, 135, 30 },
            { 547, 308, 30, 126 },
            { 412, 404, 135, 30 },
            { 652, 143, 165, 30 },
            { 622, 143, 30, 291 },
            { 682, 278, 135, 30 },
            { 787, 308, 30, 126 },
            { 652, 404, 135, 30 }
        };

        walls[level] = new Rectangle[dimensions10.length];
        for (int i = 0; i < dimensions10.length; i++) {
          walls[level][i] = new Rectangle(dimensions10[i][0], dimensions10[i][1], dimensions10[i][2],
              dimensions10[i][3]);
        }

        hints[level] = new Rectangle[2];
        hints[level][0] = new Rectangle(519, 384, 20, 20);
        hints[level][1] = new Rectangle(760, 384, 20, 20);

        lava[level] = new Rectangle[0];

        door[level] = new Rectangle(1099, 495, 50, 70);

      default:
        break;
    }

    while (opacity > 5) {
      opacity -= 5;
      this.repaint();
      sleep();
    }
    opacity = 0;
    try {
      if (soundEffects.get("Correct").getMicrosecondLength() == soundEffects.get("Correct").getMicrosecondPosition()) {
        soundEffects.get("Correct").stop();
      }
    } catch (Exception e) {
    }

  }

  // various methods called for each frame
  public void update() {
    this.repaint();
    move();
    moveWalls();
    keepInBound();
    checkLava();
    for (int i = 0; i < walls[level].length; i++) {
      checkCollision(walls[level][i], i);
    }
    checkInteraction();
    if (level == 8 || level == 9) {
      for (int i = 0; i < portals[level - 8].length; i++)
        checkPortal(i);
    }
  }

  // moves the player based on arrow key input
  void move() {
    // left-right movement
    if (left) {
      if (Math.abs(xVelocity) < MAX_SPEED && yVelocity == 0) {
        xVelocity -= ACCELERATION;
      }
    } else if (right) {
      if (xVelocity < MAX_SPEED && yVelocity == 0) {
        xVelocity += ACCELERATION;
      }
    } else {
      if (Math.round(xVelocity) == 0) {
        xVelocity = 0;
        xVelocity = 0;
      } else if (xVelocity < 0) {
        xVelocity += ACCELERATION + 0.4;
      } else {
        xVelocity -= ACCELERATION + 0.4;
      }
    }

    // jump
    if (yVelocity < 0) {
      jump = false;
    }

    if (jump && jumpCap < 1) {
      jumpCap += 0.1;
    } else {
      jumpCap = 1;
    }

    if (up) {
      if (jumpCap >= 1)
        yVelocity -= GRAVITY;
    } else {
      if (jump) {
        up = true;
        yVelocity = jumpSpeed;
      } else {
        jumpCap = 0;
      }
    }

    player.x += xVelocity;
    player.y -= yVelocity;
  }

  // moves the walls for levels 4 to 8
  void moveWalls() {
    if (level >= 4 && level <= 8) {
      for (int i = 0; i < walls[level].length; i++) {
        if (i >= wallSpeeds[level - 4].length) {
          break;
        }
        if (wallDirections[level - 4][i] == 'x') {
          if (walls[level][i].x < wallBoundaries[level - 4][i][0]
              || walls[level][i].x > wallBoundaries[level - 4][i][1]) {
            wallSpeeds[level - 4][i] *= -1;
          }
          walls[level][i].x += wallSpeeds[level - 4][i];
        } else {
          if (walls[level][i].y < wallBoundaries[level - 4][i][0]
              || walls[level][i].y > wallBoundaries[level - 4][i][1]) {
            wallSpeeds[level - 4][i] *= -1;
          }
          walls[level][i].y += wallSpeeds[level - 4][i];
        }
      }
    }
  }

  // ensures that the player does not leave the boundaries of the screen
  void keepInBound() {
    if (player.x < 0) {
      player.x = 0;
    } else if (player.x > WIDTH - player.width - 15) {
      player.x = WIDTH - player.width - 15;
    }

    int floorHeight = 50;
    if (player.y < 0) {
      player.y = 0;
      yVelocity = 0;
    } else if (player.y > HEIGHT - player.height - floorHeight) {
      player.y = HEIGHT - player.height - floorHeight;
      up = false;
      yVelocity = 0;
    }
    this.repaint();
  }

  // checks if the player has collided with a wall to restrict movement
  // arguments: the wall to check against and its index
  void checkCollision(Rectangle wall, int index) {
    boolean intersects = player.intersects(wall);
    double left1 = player.getX();
    double right1 = player.getX() + player.getWidth();
    double top1 = player.getY();
    double bottom1 = player.getY() + player.getHeight();
    double left2 = wall.getX();
    double right2 = wall.getX() + wall.getWidth();
    double top2 = wall.getY();
    double bottom2 = wall.getY() + wall.getHeight();

    if (intersects || bottom1 >= top2 - 1 && top1 < top2 && left1 < right2
        && right1 > left2) {
      this.repaint();
      wallCollisionIndex = index;
      if (right1 > left2 &&
          left1 < left2 &&
          right1 - left2 < bottom1 - top2 &&
          right1 - left2 < bottom2 - top1) {
        // left
        player.x = wall.x - player.width;
        up = true;
        if (level == 6 && index == 2 && left1 <= 0 && top1 >= 484) {
          playSound("Crush");
          restarts++;
          initializeLevel();
          return;
        } else if (level == 5 && left1 <= 0) {
          if (index == 1 && top1 >= 200 && top1 <= 256 || index == 3 && top1 >= 403 && top1 <= 460) {
            playSound("Crush");
            restarts++;
            initializeLevel();
            return;
          }
        }
      } else if (left1 < right2 &&
          right1 > right2 &&
          right2 - left1 < bottom1 - top2 &&
          right2 - left1 < bottom2 - top1) {
        // right
        player.x = wall.x + wall.width;
        up = true;
        if (level == 5 && right1 >= 1175) {
          if (index == 1 && top1 >= 200 && top1 <= 256 || index == 3 && top1 <= 460 && top1 >= 403) {
            playSound("Crush");
            restarts++;
            initializeLevel();
            return;
          }
        }
      } else if (bottom1 >= top2 - 1 && top1 < top2) {
        // top
        if (level == 8 && index == 6 && top1 <= 355 && left1 < 1081 && left1 > 1034) {
          playSound("Crush");
          restarts++;
          initializeLevel();
          return;
        }
        onTop = true;
        if (!jump) {
          player.y = wall.y - player.height;
          yVelocity = 0;
          if (level >= 4 && level <= 8 && wallDirections[level - 4].length > index) {
            if (wallDirections[level - 4][index] == 'x') {
              player.x += wallSpeeds[level - 4][index];
            } else if (wallSpeeds[level - 4][index] > 0) {
              player.y += wallSpeeds[level - 4][index];
            }
          }
          this.repaint();
        }
        up = false;
      } else if (top1 < bottom2 && bottom1 > bottom2 && !hitBottom) {
        // bottom
        if (level == 8 && index == 6 && bottom1 >= 565 && left1 < 1081 && left1 > 1031) {
          playSound("Crush");
          restarts++;
          initializeLevel();
          return;
        }
        hitBottom = true;
        player.y = wall.y + wall.height;
        jump = false;
        up = true;
        yVelocity = -2;
        this.repaint();
      }
    } else if (player.y > 0 && player.y < HEIGHT - 80) {
      if (wallCollisionIndex != -1) {
        if (player.x < walls[level][wallCollisionIndex].x - player.width
            || player.x > walls[level][wallCollisionIndex].x + walls[level][wallCollisionIndex].width
            || walls[level][wallCollisionIndex].y > player.y + player.height) {
          wallCollisionIndex = -1;
          onTop = false;
        }
        hitBottom = false;
      }
      if (!onTop) {
        up = true;
      }
    }
  }

  // checks if player has interated with a hint or a door
  void checkInteraction() {
    // door
    if (player.intersects(door[level]) && yVelocity == 0) {
      interact = true;
      if (level == 10) {
        gameOver = true;
        return;
      }
      if (show) {
        if (hintsRevealed.length() != hints[level].length && !DEVELOPER_MODE) {
          // not enough hints collected
          playSound("Error");
          JOptionPane.showMessageDialog(this,
              String.format("You must collect %s more hints.", hints[level].length - hintsRevealed.length()),
              "Locked", JOptionPane.WARNING_MESSAGE);
          show = false;
        } else {
          String password = PASSWORDS[level];
          playSound("Door");
          String input = JOptionPane.showInputDialog(this,
              String.format("Enter the %s-length password:", PASSWORDS[level].length()));
          show = false;
          if (input != null && input.length() != 0) {
            if (input.equalsIgnoreCase(password) || DEVELOPER_MODE && input.equalsIgnoreCase("pass")) {
              // success
              playSound("Correct");
              while (opacity < 254) {
                opacity += 2;
                this.repaint();
                sleep();
              }
              opacity = 255;
              level++;
              wallCollisionIndex = -1;
              initializeLevel();
            } else {
              // fail
              playSound("Incorrect");
              JOptionPane.showMessageDialog(this, "Incorrect password!",
                  "ERROR", JOptionPane.ERROR_MESSAGE);
              interact = false;
              show = false;
            }
          }
        }
      }
    } else { // hints
      for (int i = 0; i < hints[level].length; i++) {
        this.repaint();
        if (player.intersects(hints[level][i]) && yVelocity == 0) {
          interact = true;
          if (show) {
            playSound("Hint");
            JOptionPane.showMessageDialog(this, HINT_MESSAGES[level][i],
                "HINT", JOptionPane.INFORMATION_MESSAGE);
            show = false;
            if (hintsRevealed.indexOf(Integer.toString(i)) == -1) {
              hintsRevealed += Integer.toString(i);
            }
          }
          break;
        } else {
          interact = false;
        }
      }
      gameOver = false;
    }
  }

  // check if player has touched lava; if so, restart the level
  void checkLava() {
    for (int i = 0; i < lava[level].length; i++) {
      if (player.intersects(lava[level][i])) {
        playSound("Lava");
        initializeLevel();
        restarts++;
        break;
      }
    }
  }

  // for level 8-9: checks if the player has entered a portal; if so, teleports
  // the player to the corresponding portal
  // argument: the index of the portal to check for
  void checkPortal(int index) {
    int l = level - 8;
    if (player.intersects(portals[l][index])) {
      playSound("Portal");
      // teleport player to left of, right of, or under portal
      if (locations[l].charAt(index) == 'l') {
        player.x = portals[l][nodes[l][index]].x - 43;
        player.y = portals[l][nodes[l][index]].y + 40;
        yVelocity = 0;
      } else if (locations[l].charAt(index) == 'r') {
        player.x = portals[l][nodes[l][index]].x + 43;
        player.y = portals[l][nodes[l][index]].y + 40;
        yVelocity = 0;
      } else {
        player.x = portals[l][nodes[l][index]].x + 34;
        player.y = portals[l][nodes[l][index]].y + 30;
        yVelocity = -10;
      }
      jump = false;
      left = false;
      right = false;
      xVelocity = 0;
      up = false;
    }
  }

  // returns the total time elapsed as a hh:mm:ss formated string
  public String getTime() {
    time = System.currentTimeMillis() - startTime;
    int hours = (int) ((totalTime + time) / 3600000 % 24);
    int minutes = (int) ((totalTime + time) / 60000 % 60);
    int seconds = (int) ((totalTime + time) / 1000 % 60);
    return String.format("%02d:%02d:%02d", hours, minutes, seconds);
  }

  // plays a sound effect
  // argument: String sound name to play
  public void playSound(String sound) {
    if (SOUND_ON) {
      if (soundEffects.get(sound).getMicrosecondPosition() != 0) {
        soundEffects.get(sound).setMicrosecondPosition(0);
      }
      soundEffects.get(sound).start();
    }
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    int x = e.getX();
    int y = e.getY();

    // show level info
    if (x <= 37 && x >= 12 && y <= 602 && y >= 577) {
      JOptionPane.showMessageDialog(this,
          LEVEL_MESSAGES[level] + ((level > 0 && level < 10) ? "\n\nPrevious passcode: " + PASSWORDS[level - 1] : ""),
          String.format("Level %s", level + 1),
          JOptionPane.INFORMATION_MESSAGE);
    }

    // teleport player to mouse position
    if (DEVELOPER_MODE && y < 535) {
      player.x = x;
      player.y = y;
      up = true;
      wallCollisionIndex = -1;
      yVelocity = 0;
    }
  }

  @Override
  public void mouseReleased(MouseEvent e) {
  }

  @Override
  public void mouseEntered(MouseEvent e) {
  }

  @Override
  public void mouseExited(MouseEvent e) {
  }

  @Override
  public void mousePressed(MouseEvent e) {
  }

}
