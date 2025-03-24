package util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class FileController {
    private Properties properties = new Properties();

    public FileController() {
        loadProperties();
    }

    private void loadProperties() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find config.properties");
                return;
            }
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public String getFileLocation() {
        return properties.getProperty("fileLocation");
    }

    public void setFileLocation(String newLocation) {
        properties.setProperty("fileLocation", newLocation);
        saveProperties();
    }

    private void saveProperties() {
        try (FileOutputStream output = new FileOutputStream(getClass().getClassLoader().getResource("config.properties").getFile())) {
            properties.store(output, null);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}