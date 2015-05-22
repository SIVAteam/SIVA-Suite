package hu.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Properties;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.management.RuntimeErrorException;
import javax.servlet.ServletContext;

/**
 * This class provides access to the configuration of CSS, logo, and favicon.
 * The different settings are accessible through specific keys. Every entry is
 * stored in the file as key/value pairs.
 */
@ManagedBean(name = "brandingConfiguration", eager = true)
@ApplicationScoped
public class BrandingConfiguration {

    private Properties brandingProperties = new Properties();
    private static final String CONFIG_DIRECTORY = System
            .getProperty("user.home") + "/.sivaServer/";
    private static final String CONFIG_DIRECTORY_IN_WAR = "hu/configuration/";
    private static final String ABSOLUTE_APP_URL = getAbsoluteApplicationUrl();
    private static final String FILE_NAME = "BrandingConfiguration.properties";
    private static final String FILE_PATH = CONFIG_DIRECTORY + FILE_NAME;
    private static final String BRANDINGCONFIG_IN_WAR = CONFIG_DIRECTORY_IN_WAR
            + FILE_NAME;

    public BrandingConfiguration() {
        this.openBrandingConfigFile();
    }

    /**
     * Getter for a style property as {@link String}.
     * 
     * @param key
     *            for the specific value.
     * @return the URL to the requested resource.
     */
    public String getStylePoperty(String key) {
	String property = brandingProperties.getProperty(key);
	File file = new File(CONFIG_DIRECTORY + "branding/" + property);
	return ABSOLUTE_APP_URL  + ((file.exists()) ? "branding/" : "") + property;
    }

    /**
     * Getter for a branded text passage.
     * 
     * @return the branding text for the requested key
     * 
     */
    public String getBrandingText(String key) {
        Locale locale = FacesContext.getCurrentInstance().getViewRoot()
                .getLocale();
        if (brandingProperties.containsKey(key + "_de")
                && brandingProperties.containsKey(key + "_en")) {
            if (locale.equals(Locale.GERMAN)) {
                return brandingProperties.getProperty(key + "_de");
            } else if (locale.equals(Locale.ENGLISH)) {
                return brandingProperties.getProperty(key + "_en");
            }
        }
        return "! KEY NOT FOUND IN BrandingConfiguration.properties !";
    }

    /**
     * Opens the styleConfiguration-file. If a configuration file exists outside
     * the WAR-File this file is set for style-configuration, else the
     * style-configuration from WAR-file will be used.
     * 
     * @return true if Configuration-file could be opened successful, false if
     *         Configuration-file could not be opened or created
     */
    private boolean openBrandingConfigFile() {
        File configFile = new File(FILE_PATH);
        BufferedInputStream stream = null;

        try {
            stream = new BufferedInputStream(new FileInputStream(configFile));
        } catch (FileNotFoundException ignored) {
            
        }

        // Load properties from file
        try {
            if (stream != null) {
                brandingProperties.load(stream);
            }
        } catch (IOException e) {
            return false;
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException ignored) {
                
            }
        }
        completeProperties();
        return true;
    }

    /**
     * After generating properties call this method. It will be fill not set
     * properties with the default values and complete the values with absolute
     * URL-path of the different files.
     * 
     */
    private void completeProperties() {
        Properties warProperties = new Properties();
        BufferedInputStream warFileStream = null;

        try {
            warFileStream = new BufferedInputStream(this.getClass()
                    .getClassLoader()
                    .getResourceAsStream(BRANDINGCONFIG_IN_WAR));
            warProperties.load(warFileStream);
        } catch (FileNotFoundException e) {
            throw new RuntimeErrorException(null, "File: '"
                    + BRANDINGCONFIG_IN_WAR + "' not found!\n" + e.toString());
        } catch (IOException e) {
            throw new RuntimeErrorException(null, "IO-Error while reading: '"
                    + BRANDINGCONFIG_IN_WAR + "'!\n" + e.toString());
        } finally {
            try {
                warFileStream.close();
            } catch (IOException ignored) {
                
            }
        }

        // merge/fill properties and add directory-path
        for (Entry<Object, Object> e : warProperties.entrySet()) {
            String key = (String) e.getKey();
            String value = (String) e.getValue();
            if (this.brandingProperties.getProperty(key) == null) {
                this.brandingProperties.setProperty(key, value);
            }
        }
    }

    /**
     * Determined the absolute URL-path of the current application.
     * 
     * @return URL-path of the application.
     */
    private static String getAbsoluteApplicationUrl() {
        ExternalContext ectx = FacesContext.getCurrentInstance()
                .getExternalContext();
        ServletContext servletContext = (ServletContext) ectx.getContext();
        String url = servletContext.getContextPath();
        if (!url.endsWith("/")) {
            url = url + "/";
        }
        return url;
    }
}