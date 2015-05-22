package hu.tests.facelets.configuration;

/**
 * This class provides the contants used for the web tests.
 * 
 */
public class Globals {

    // BASE_URL
     private static final String BASE_URL = "http://localhost:8080/MTT";

    // Admin credentials you chose while the installation process
    private final static String admin_username = "admin@mailinator.com";
    private final static String admin_password = "Admin123";

    /**
     * 
     * @return the BASE_URL to use for the test
     */
    public static String getBaseUrl() {
        return BASE_URL;
    }

    /**
     * 
     * @return the administrators username
     */
    public static String getAdminUsername() {
        return admin_username;
    }

    /**
     * 
     * @return the administrators password
     */
    public static String getAdminPassword() {
        return admin_password;
    }
}