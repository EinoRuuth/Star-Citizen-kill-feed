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
                    if (line.contains("CActor::Kill:")) {
                        System.out.println("New kill");
                        List<String> listified = listifyKill(line);
                        kills.add(listified);
                        Platform.runLater(() -> this.gui.addKill(listified));
                    } else if (line.contains("CVehicle::OnAdvanceDestroyLevel:")) {
                        System.out.println("New destruction");
                        List<String> listified = listifyDestuction(line);
                        destroys.add(listified);
                        Platform.runLater(() -> this.gui.addDestruction(listified));
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
            if (line.contains("CActor::Kill:")) {
                kills.add(listifyKill(line));
            } else if (line.contains("CVehicle::OnAdvanceDestroyLevel:")) {
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
        if (location.contains("_")){
            location = location.substring(0, location.lastIndexOf('_'));
            location = location.replaceAll("_", " ");
        }
        return Arrays.asList(parts[0], cleanNpc(parts[5]), " died in ", location, " killed by ", parts[12]);
    }

    private List<String> listifyDestuction(String line) {
        String[] parts = line.split(" ");
        for (int x = 0; x < parts.length; x++) {
            parts[x] = parts[x].replaceAll("'", "");
        }
        String ship = parts[6];
        ship = ship.substring(0, ship.lastIndexOf('_'));
        ship = ship.replaceAll("_", " ");
        String location = parts[10];
        if (location.contains("_")){
            location = location.substring(0, location.lastIndexOf('_'));
            location = location.replaceAll("_", " ");
        }
        System.out.println(parts[35]);
        if (parts[35].equals("1")) {
            return Arrays.asList(parts[0], "1", ship, " got destroyed in ", location, " destroyed by ", cleanKiller(parts[38]), " piloted by ", parts[27], " with a ", parts[41]);
        } else if (parts[35].equals("2")) {
            return Arrays.asList(parts[0], "2", ship, " got destroyed in ", location, " destroyed by ", cleanKiller(parts[38]), " piloted by ", parts[27], " with a ", parts[41]);
        } else {
            return Arrays.asList(parts[0], "3", ship, " got destroyed in ", location, " destroyed by ", cleanKiller(parts[38]), " piloted by ", parts[27], " with a ", parts[41]);
        }
    }

    private String cleanNpc(String input){
        if (input.contains("PU_Human_Enemy_GroundCombat")) {
            input = input.replace("PU_Human_Enemy_GroundCombat_", "");
        }
        if (input.lastIndexOf('_') == -1){
            return input;
        } else {
            String result = input.substring(0, input.lastIndexOf('_'));
            result = result.replaceAll("_", " ");
            return result.trim();
        }
    }

    private String cleanKiller(String input) {
        if (input.startsWith("U")) {
            return input;
        }
        if (input.lastIndexOf('_') == -1){
            return input;
        } else {
            String result = input.substring(0, input.lastIndexOf('_'));
            result = result.replaceAll("_", " ");
            return result.trim();
        }
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
}