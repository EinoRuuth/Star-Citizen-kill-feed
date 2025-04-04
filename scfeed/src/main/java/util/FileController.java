package util;

import java.util.prefs.Preferences;

public class FileController {
    private Preferences preferences;

    public FileController() {
        preferences = Preferences.userNodeForPackage(FileController.class);
        initializeDefaults();
    }

    private void initializeDefaults() {
        if (preferences.get("SCLGfileLocation", null) == null) {
            preferences.put("SCLGfileLocation", "C:/Program Files/Roberts Space Industries/StarCitizen/LIVE/game.log");
        }
        if (preferences.get("SCLGalarmSoundLocation", null) == null) {
            preferences.put("SCLGalarmSoundLocation", "scglparser/src/main/resources/alarm.mp3");
        }
        if (preferences.get("SCLGvolume", null) == null) {
            preferences.put("SCLGvolume", "0.15");
        }
        if (preferences.get("SCLGusername", null) == null) {
            preferences.put("SCLGusername", "");
        }
        if (preferences.get("SCLGalarmToggle", null) == null) {
            preferences.put("SCLGalarmToggle", "false");
        }
        if (preferences.get("SCLGparty", null) == null) {
            preferences.put("SCLGparty", "");
        }
    }

    public String getFileLocation() {
        System.out.println("Fetched file location");
        return preferences.get("SCLGfileLocation", "defaultFileLocation");
    }

    public void setFileLocation(String newLocation) {
        preferences.put("SCLGfileLocation", newLocation);
        System.out.println("File location updated to: " + newLocation);
    }


    public String getAudioFileLocation() {
        System.out.println("Fetched audio file location");
        return preferences.get("SCLGalarmSoundLocation", "defaultAlarmSoundLocation");
    }

    public void setAudioFileLocation(String newLocation) {
        preferences.put("SCLGalarmSoundLocation", newLocation);
        System.out.println("Audio file location updated to: " + newLocation);
    }


    public String getVolume() {
        System.out.println("Fetched volume");
        return preferences.get("SCLGfvolume", "50");
    }

    public void setVolume(String newVolume) {
        preferences.put("SCLGfvolume", newVolume);
        System.out.println("Volume updated to: " + newVolume);
    }


    public String getUsername() {
        System.out.println("Fetched username");
        return preferences.get("SCLGusername", "");
    }

    public void setUsername(String newUsername) {
        preferences.put("SCLGusername", newUsername);
        System.out.println("Username updated to: " + newUsername);
    }


    public String getAlarmToggle() {
        System.out.println("Fetched alarm toggle");
        return preferences.get("SCLGalarmToggle", "");
    }

    public void setAlarmToggle(String newUsername) {
        preferences.put("SCLGalarmToggle", newUsername);
        System.out.println("alarm toggle updated to: " + newUsername);
    }


    public String getParty() {
        System.out.println("Fetched alarm toggle");
        return preferences.get("SCLGparty", "");
    }

    public void setParty(String newUsername) {
        preferences.put("SCLGparty", newUsername);
        System.out.println("party updated to: " + newUsername);
    }
}