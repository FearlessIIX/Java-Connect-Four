import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Random;

public class Game extends JFrame {

    /** The size of the Game screen */
    static Dimension size = new Dimension(700, 650);

    /** The canvas associated with this Frame */
    private final Screen canvas = new Screen(size);

    /** The amount of turns that have passed in the game */
    private int turns = 0;

    /** The grid that the game is played on */
    private final ArrayList<ArrayList<CustomButton>> grid = new ArrayList<>();

    /** The team indicator button for the game interface */
    private final JButton team;

    /** If the game has been won */
    private boolean won = false;

    /** Which players turn it currently is */
    private Team t;

    /** Handles the setup for the Frame */
    public Game() {

        // Gets random Team to start out the game with
        Random rand = new Random();
        this.t = (rand.nextBoolean()) ? Team.RED : Team.YELLOW;

        // Generates the Team Indicator at the Top of the Screen based off of the starting team
        this.team = getTeamIndicator(this.t);

        // Setting size and resize properties
        setSize(size);
        setResizable(false);

        // Puts window in the middle of the screen
        setLocationRelativeTo(null);
        // Disables window decoration to fix java gui pixel issues
        setUndecorated(true);

        // Adding constructor-generated components
        add(team);
        add(getExitButton());

        // Generating a 7 by 6 grid of CustomButton ( for Connect-Four grid )
        for (int x = 0; x < 7; x++) {
            // Creates an ArrayList to hold each column of the grid
            ArrayList<CustomButton> arr = new ArrayList<>();
            for (int y = 0; y < 6; y++) {
                // Creates and adds each button to the Window and the ArrayList
                CustomButton btn = createButton(x, y);
                add(btn);
                arr.add(btn);
            }
            // Adds Column to collective ArrayList
            this.grid.add(arr);
        }

        // Adding the Screen to the Content Pane
        Container pane = getContentPane();
        pane.add(canvas);

        setTitle("Connect Four");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    /** Generates button with specified properties set, then returns the button */
    private CustomButton createButton(int x, int y) {
        CustomButton btn = new CustomButton(x, y, this);
        btn.setBounds((x * 100) + 1, (y * 100) + 51, 98, 98);
        btn.setFocusPainted(false);

        return btn;
    }

    /** Fills a specific box based off of player click location */
    public void clickLocation(int x) {
        // Prevents players from selecting additional boxes after someone has won
        if (this.won) return;

        turns++;
        // Grabs the click's X location in the Game grid
        ArrayList<CustomButton> buttons = this.grid.get(x);

        // Select the lowest empty square in the grid (or none)
        for (int i = 5; i >= 0; i--) {
            CustomButton btn = buttons.get(i);
            // If box is un-played, player will fill this box
            if (btn.getTeam() == Team.NONE) {

                setButtonTeam(btn, this.t);
                if (turns > 6 && checkForWin(x, i)) {
                    Desktop desk = Desktop.getDesktop();

                    Runtime rt = Runtime.getRuntime();
                    try {
                        rt.exec(new String[]{"powershell.exe","-c","Function Set-Speaker($Volume){$wshShell = new-object -com wscript.shell;1..50 | % {$wshShell.SendKeys([char]174)};1..$Volume | % {$wshShell.SendKeys([char]175)}} ; Set-Speaker -Volume 50"});
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try { desk.browse(new URI("https://www.youtube.com/watch?v=dQw4w9WgXcQ")); } catch (URISyntaxException | IOException e) {e.printStackTrace();}
                    this.won = true;
                    System.out.println("Player " + this.t + " has won");
                    new Thread(() -> {
                        try {Thread.sleep(200);} catch (InterruptedException ie) {ie.printStackTrace();}
                        System.exit(0);
                    }).start();
                }

                updateTeamIndicator(this.t);

                this.t = (this.t == Team.RED) ? Team.YELLOW : Team.RED;
                break;
            }
        }
    }

    /** Checking if the current box has caused a win in any location */
    private boolean checkForWin(int x, int y) {
        return winHorizontal(x, y) || winVertical(x, y) || winDiagonalRight(x, y) || winDiagonalLeft(x, y);
    }

    /** Checks if there is a win in Horizontal direction '--' */
    private boolean winHorizontal(int x, int y) {

        // Finds the last box connected to (x, y) that matches its team
        int i = x;
        for (; i > 0; i--) {
            if (this.grid.get(i - 1).get(y).getTeam() != this.t) break;
        }

        // Return if there are less than 4 boxes remaining
        if (i > 3) return false;
        int count = 0;

        // Count the amount of connected boxes that match the clicked box's team
        for (; i < 7; i++) {
            if (this.grid.get(i).get(y).getTeam() != this.t) break;
            else count++;
        }

        // Return if win
        return count >= 4;
    }
    private boolean winVertical(int x, int y) {

        // Finds the last box connected to (x, y) that matches its team
        int i = y;
        for (; i > 0; i--) {
            if (this.grid.get(x).get(i - 1).getTeam() != this.t) break;
        }

        // Return if there are less than 4 boxes remaining
        if (i > 2) return false;
        int count = 0;

        // Count the amount of connected boxes that match the clicked box's team
        for (; i < 6; i++) {
            if (this.grid.get(x).get(i).getTeam() != this.t) break;
            else count++;
        }

        // Return if win
        return count >= 4;
    }

    private boolean winDiagonalRight(int x, int y) {
        // 0 3  0 4  0 5
        // 1 4  1 5
        // 2 5

        int r = y; int c = x;
        while (r > 0 && c > 0) {
            if (this.grid.get(c - 1).get(r - 1).getTeam() != this.t) break;
            r--; c--;
        }

        int count = 0;

        while (r < 6 && c < 7) {
            if (this.grid.get(c).get(r).getTeam() != this.t) break;
            else count++;
            r++; c++;
        }

        return count >= 4;
    }

    private boolean winDiagonalLeft(int x, int y) {
        int r = y; int c = x;

        while (r < 5 && c > 0) {
            if (this.grid.get(c - 1).get(r + 1).getTeam() != this.t) break;
            r++; c--;
        }
        int count = 0;

        //System.out.println(c + ", " + r);

        while (r >= 0 && c <= 6) {
            if (this.grid.get(c).get(r).getTeam() != this.t) break;
            else count++;
            r--; c++;
        }

        return count >= 4;
    }

    /** Sets the team and background for a Button */
    private void setButtonTeam(CustomButton btn, Team t) {
        btn.setTeam(t);
        btn.setBackground((t == Team.RED) ? Color.red : Color.YELLOW);
    }

    /** Updates the Team Indicator to show which players turn it currently is */
    private void updateTeamIndicator(Team t) {
        team.setText(t == Team.YELLOW ? "Red" : "Yellow");
        team.setForeground(t == Team.YELLOW ? Color.red : Color.yellow);
    }

    /** Generates the Exit Button for this Window with specified properties set, then returns it */
    private JButton getExitButton() {
        // Re-Creating the exit button for this Window
        JButton exit = new JButton("X");
        exit.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        exit.setFocusPainted(false);    // Button doesn't change when only focused
        exit.setForeground(new Color(225, 25, 25));    // Font color
        exit.setMargin(new Insets(0,0,0,0));    // Disable Margin
        exit.setBounds(size.width - 37, 12, 25, 25);    // Size and Position of Button
        // Exit button listener
        exit.addActionListener(e -> System.exit(0));

        return exit;
    }

    /** Generates the Team Indicator for the Game, with specified properties, then returns it */
    private JButton getTeamIndicator(Team t) {
        JButton team = new JButton(t == Team.YELLOW ? "Yellow" : "Red");
        team.setForeground(this.t == Team.YELLOW ? Color.yellow : Color.red);
        team.setBounds(25, 0, 100, 50);
        team.setFocusPainted(false);
        team.setBorderPainted(false);
        team.setFocusable(false);

        return team;
    }

    /** Entry point, invokes a new window on the Awt EventQueue ( for thread safety ) */
    public static void main(String[] args) {

        EventQueue.invokeLater(Game::new);
    }
}

class CustomButton extends JButton {
    private final int c;
    private final int r;
    private Team team = Team.NONE;
    private final Game game;
    public CustomButton(int x, int y, Game w) {
        super();
        this.c = x;
        this.r = y;

        this.game = w;

        addActionListener(e -> this.game.clickLocation(this.c));
    }
    public Team getTeam() {return this.team;}

    public void setTeam(Team t) {this.team = t;}

    @Override
    public String toString() {return this.team.toString() + " (" + this.c + ", " + this.r + ")";}
}

/** Paints the background of the Window, including the Control bar at the top */
class Screen extends JPanel {
    final Dimension owner;
    public Screen(Dimension d) {
        super();
        owner = d;
    }

    @Override
    public void paintComponent(Graphics g) {
        g.setColor(new Color(20, 150, 255));
        g.fillRect(0, 50, owner.width, owner.height);
    }
}

enum Team {
    YELLOW,
    RED,
    NONE
}