package util;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import javafx.application.Platform;
import view.GUI;

public class GameLogParser {
    private String filename;
    private List<List<String>> kills;
    private List<List<String>> destroys;
    private volatile boolean stopThread;
    private BufferedReader fileReader;
    private GUI gui;

    public GameLogParser(String filename, GUI gui) {
        this.filename = filename;
        this.gui = gui;
        this.kills = new ArrayList<>();
        this.destroys = new ArrayList<>();
        this.stopThread = false;

        List<String> gameLogData = openFileAndGetData();
        getKillsAndDestroys(gameLogData);
        
        // Start a new thread for watching for changes
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(this::lookForChanges);
    }

    private List<String> openFileAndGetData() {
        List<String> gameLogData = new ArrayList<>();
        try {
            fileReader = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = fileReader.readLine()) != null) {
                gameLogData.add(line);
            }
        } catch (IOException e) {
            System.out.println("Error: File not found.");
        }
        return gameLogData;
    }

    private void lookForChanges() {
        try {
            while (!stopThread) {
                String line = fileReader.readLine();
                if (line != null) {
                    if (line.contains("kill")) {
                        System.out.println("New kill");
                        kills.add(listifyKill(line));
                        Platform.runLater(() -> this.gui.refresh());
                    } else if (line.contains("Destruction")) {
                        System.out.println("New destruction");
                        destroys.add(listifyDestuction(line));
                        Platform.runLater(() -> this.gui.refresh());
                    }
                } else {
                    Thread.sleep(1000);
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void getKillsAndDestroys(List<String> gameLogData) {
        for (String line : gameLogData) {
            if (line.contains("kill")) {
                kills.add(listifyKill(line));
            } else if (line.contains("Destruction")) {
                destroys.add(listifyDestuction(line));
            }
        }
    }

    private List<String> listifyKill(String line) {
        String[] parts = line.split(" ");
        for (int x = 0; x < parts.length; x++) {
            parts[x] = parts[x].replaceAll("'", "");
        }
        String location = parts[9];
        location = location.substring(0, location.lastIndexOf('_'));
        location = location.replaceAll("_", " ");
        return Arrays.asList(parts[5], " died in ", location, " killed by ", parts[12]);
    }

    private String prettyfiKill(String line) {
        String[] parts = line.split(" ");
        for (int x = 0; x < parts.length; x++) {
            parts[x] = parts[x].replaceAll("'", "");
        }
        String location = parts[9];
        location = location.substring(0, location.lastIndexOf('_'));
        location = location.replaceAll("_", " ");
        return parts[5] + " died in " + location + " killed by " + parts[12];
    }

    private List<String> listifyDestuction(String line) {
        String[] parts = line.split(" ");
        for (int x = 0; x < parts.length; x++) {
            parts[x] = parts[x].replaceAll("'", "");
        }
        String ship = parts[6];
        ship = ship.substring(0, ship.lastIndexOf('_'));
        ship = ship.replaceAll("_", " ");
        return Arrays.asList(ship, " got destroyed in ", parts[10], " destroyed by ", cleanKiller(parts[38]), " piloted by ", parts[27], " with a ", parts[41]);
    }

    private String prettyfiDestuction(String line) {
        String[] parts = line.split(" ");
        for (int x = 0; x < parts.length; x++) {
            parts[x] = parts[x].replaceAll("'", "");
        }
        String ship = parts[6];
        ship = ship.substring(0, ship.lastIndexOf('_'));
        ship = ship.replaceAll("_", " ");

        return ship + " got destroyed in " + parts[10] + " destroyed by " + cleanKiller(parts[38]) + " piloted by " + parts[27] + " with a " + parts[41];
    }

    private String cleanKiller(String input) {
        if (input.startsWith("U")) {
            return input;
        }
        String result = input.replaceAll("_", " ");
        result = result.replaceAll("\\d", "");
        return result.trim();
    }

    public List<List<String>> getPlayerKills() {
        return kills;
    }

    public void printShipKill(List<String> killes) {
        System.out.println("\033[0;32mPlayer Kills:\033[0m");
        for (String kill : killes) {
            System.out.println(kill);
        }
    }

    public List<List<String>> getShipKills() {
        return destroys;
    }

    public void printPlayerKill(List<String> Desctrucions) {
        System.out.println("\033[0;32mShip kills:\033[0m");
        for (String kill : Desctrucions) {
            System.out.println(kill);
        }
    }

    public void stop() {
        System.out.println("\033[0;31mExiting...\033[0m");
        stopThread = true;
        try {
            fileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    private void printExitMessage() {
        System.out.print(" Press Enter to exit\r");
    }

    public void exitMessage() {
        printExitMessage();
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
        stop();
        scanner.close();
    }
}