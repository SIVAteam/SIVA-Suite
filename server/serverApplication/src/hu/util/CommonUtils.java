package hu.util;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.faces.context.FacesContext;

public class CommonUtils {
	
    /**
     * Generates a valid path of the actual application.
     * 
     * @param facelet which you want to access.
     * @param parameters to add to the url(GET).
     * @return the full URL as {@link String} 
     * @throws URISyntaxException
     * @throws MalformedURLException
     */
    public static String buildContextPath(String facelet, String parameters)
            throws URISyntaxException, MalformedURLException {

        //if junit test is running return is not used
        if (FacesContext.getCurrentInstance() != null && FacesContext.getCurrentInstance().getExternalContext() != null) {
            FacesContext ctx = FacesContext.getCurrentInstance();
            String path = ctx.getExternalContext().getRequestContextPath();
            String host = ctx.getExternalContext().getRequestServerName();      
            String protocol = ctx.getExternalContext().getRequestScheme();
            Integer port = ctx.getExternalContext().getRequestServerPort();
            URI uri;
            URL url;
            uri = new URI(
                    protocol,
                    null,
                    host,
                    port,
                    path+facelet,
                    parameters,
                    null);
            url = uri.toURL();
            
            // decode to an URL specific string
            String decodedURL = url.toString().replace("@", "%40");
            return decodedURL;
        }
        
        return "ignoreForTest";
    }
    
}