package hu.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import org.apache.commons.lang.StringUtils;

/**
 * 
 * This class provides access to the application's configuration file. All the
 * different settings are stored as key-value-pairs.
 */
@ManagedBean
@ApplicationScoped
public class Configuration {

    private Properties properties = new Properties();
    private Properties updateProperties = new Properties();

    private final String directory = System.getProperty("user.home")
            + "/.sivaServer/";
    private final String photoDirectory = "photos/";
    private final String videoDirectory = "videos/";
    private final String brandingDirectory = "branding/";
    private final String configFileName = "Configuration.properties";
    private final String configFilePath = directory + configFileName;
    private final String brandingConfigFileName = "BrandingConfiguration.properties";
    private final String brandingConfigFilePath = directory + brandingConfigFileName;
    private final String configInWarFile = "hu/configuration/Configuration.properties";
    private final String configUpdateInWarFile = "hu/configuration/ConfigurationUpdate.properties";
    private final String brandingConfigInWarFile = "hu/configuration/BrandingConfiguration.properties";

    public Configuration() {
	this.initDirectories();
	this.createBrandingConfigFile();
        this.openConfigFile();
    }

    /**
     * Get a configuration entry as a {@link String}.
     * 
     * @param key
     *            of the entry.
     * 
     * @return the value of the entry.
     */
    public String getString(String key) {
        return this.properties.getProperty(key);
    }

    /**
     * Set a configuration entry as a {@link String}. Overwrite if the entry
     * already exists.
     * 
     * @param key
     *            of the entry.
     * @param value
     *            for the entry.
     */
    public void setString(String key, String value) {
        this.properties.setProperty(key, value);
        this.storeToConfigFile();
    }

    /**
     * Get a configuration entry as an {@link Integer}.
     * 
     * @param key
     *            of the entry.
     * @return the value of the entry.
     */
    public Integer getInteger(String key) {
    	if(this.properties.getProperty(key) == null)
    		return 0;
        return Integer.parseInt(this.properties.getProperty(key));
    }

    /**
     * Set a configuration entry as an {@link Integer}. Overwrite if the entry
     * already exists.
     * 
     * @param key
     *            of the entry.
     * @param value
     *            for the entry.
     */
    public void setInteger(String key, Integer value) {
        this.properties.setProperty(key, Integer.toString(value));
        this.storeToConfigFile();
    }

    /**
     * Get a configuration entry as a {@link Boolean}.
     * 
     * @param key
     *            of the entry.
     * @return the value of the entry.
     */
    public Boolean getBoolean(String key) {
        return Boolean.parseBoolean(this.properties.getProperty(key));
    }

    /**
     * Set a configuration entry as a {@link Boolean}. Overwrite if the entry
     * already exists.
     * 
     * @param key
     *            of the entry.
     * @param value
     *            for the entry.
     */
    public void setBoolean(String key, Boolean value) {
        this.properties.setProperty(key, Boolean.toString(value));
        this.storeToConfigFile();
    }
    
    /**
     * Get a configuration entry as a {@link String} array.
     * 
     * @param key
     *            of the entry.
     * @return the {@link String} array.
     */
    public String[] getArray(String key) {
	return this.properties.getProperty(key).split(",");
    }

    /**
     * Set a configuration entry as a {@link String} array. Overwrite if the entry
     * already exists.
     * 
     * @param key
     *            of the entry.
     * @param value
     *            of type {@link String} array.
     */
    public void setArray(String key, String[] value) {
        this.properties.setProperty(key, StringUtils.join(value));
        this.storeToConfigFile();
    }
    
    /**
     * Lookup if the specified {@link String} array contains a certain value.
     * 
     * @param key
     *            of the {@link String} array.
     * @param value
     *            to search for.
     * @return true if the value exists in the given array, false otherwise.
     */
    public boolean containsArrayValue(String key, String value){
	String[] array = this.getArray(key);
	for(int i = 0; i < array.length; i++){
	    if(array[i].equals(value)){
		return true;
	    }
	}
	return false;
    }
    
    /**
     * Create required home directory and its subdirectories
     */
    private void initDirectories(){
	File dir = new File(directory);
        if(!dir.exists()){
            dir.mkdir();
        }
	File photoDir = new File(directory + photoDirectory);
	if(!photoDir.exists()){
	    photoDir.mkdir();
        }
	File videoDir = new File(directory + videoDirectory);
	if(!videoDir.exists()){
	    videoDir.mkdir();
        }
	File brandingDir = new File(directory + brandingDirectory);
	if(!brandingDir.exists()){
	    brandingDir.mkdir();
        }
    }

    /**
     * Opens the global Configuration file. If no Configuration file exists an
     * empty File is created.
     * 
     * @return true if Configuration file could be opened successful, false if
     *         Configuration file could not be opened or created
     */
    private boolean openConfigFile() {
        
	boolean hasToBeSaved = false;
	
        // try to load the file of the filesystem
	File file = new File(configFilePath);
        BufferedInputStream stream;
        try {
            stream = new BufferedInputStream(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            hasToBeSaved = true;
            stream = new BufferedInputStream(this.getClass().getClassLoader()
                    .getResourceAsStream(configInWarFile));
        }

        try {
            properties.load(stream);
            stream.close();
        } catch (IOException e) {
            return false;
        }
        
        stream = new BufferedInputStream(this.getClass().getClassLoader()
                    .getResourceAsStream(configUpdateInWarFile));
        try {
            updateProperties.load(stream);
            stream.close();
        } catch (IOException e) {
            return false;
        }
        
        // Look for configuration updates and execute those
        int i = 0;
        for(Iterator<Entry<Object,Object>> it = updateProperties.entrySet().iterator(); it.hasNext(); ){
            Entry<Object,Object> entry = it.next();
            properties.put(entry.getKey(), entry.getValue());
            i++;
        } 
        if(i > 0){
            hasToBeSaved = true;
        }
        
        // Save configuration file to local home directory if needed
        if(hasToBeSaved){
            this.storeToConfigFile();
        }
        
        return true;
    }

    /**
     * Writes changes to configuration file. Creates it if does not exist.
     */
    private void storeToConfigFile() {
        OutputStream output = null;
        try {
            File file = new File(configFilePath);
            output = new FileOutputStream(file);
            properties.store(output, "Configuration File of sivaServer");
        } catch (IOException e) {
            throw new RuntimeException("Failed to write configuration, please ensure " + directory + " is writable.");
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException ignored) {
                }
            }
        }
    }
    
    /**
     * Checks if a BrandingConfiguration file is already stored in the user's.
     * directory. If not it will be created.
     * 
     * @return true if BrandingConfiguration file could created successfully or already
     * 		exists, false otherwise
     */
    private boolean createBrandingConfigFile(){
	File file = new File(brandingConfigFilePath);
	if(!file.exists()){
            BufferedInputStream stream;stream = new BufferedInputStream(this.getClass().getClassLoader()
                    .getResourceAsStream(brandingConfigInWarFile));
            try {
                properties.load(stream);
                stream.close();
            } catch (IOException e) {
                return false;
            }
            
            OutputStream output = null;
            try {
                output = new FileOutputStream(file);
                properties.store(output, "BrandingConfiguration File of sivaServer");
            } catch (IOException e) {
                throw new RuntimeException("Failed to write branding configuration, please ensure " + directory + " is writable.");
            } finally {
                if (output != null) {
                    try {
                        output.close();
                    } catch (IOException ignored) {
                    }
                }
            }
	}
	return true;
    }
}