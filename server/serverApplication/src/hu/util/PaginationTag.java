package hu.util;

import java.io.IOException;

import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

/**
 * This class creates a HTML pagination for a JSF application.
 */

@FacesComponent("hu.util.PaginationTag")
public class PaginationTag extends UIComponentBase {

    @Override
    public String getFamily() {
        return "my.custom.component";
    }

    @Override
    public void encodeBegin(FacesContext context) throws IOException {

        // Get tag attributes
        String url = (String) getAttributes().get("url");
        Integer currentPage = (Integer) getAttributes().get("currentPage");
        String currentClass = (String) getAttributes().get("currentClass");
        Integer pages = (Integer) getAttributes().get("pages");

        // Just use an empty URL if none is specified
        if (url == null) {
            url = "";
        }

        // Set pages to 0 if amount of pages not specified
        if (pages == null) {
            pages = new Integer(0);
        }

        // Generate correct HTML ampersands
        url = url.replace("&", "&amp;");
        
        // Add pagination parameter to url
        url = url + ((url.indexOf('?') != -1) ? "&amp;" : "?") + "page=";

        // Create pagination
        StringBuilder pagination = new StringBuilder("");
        ResponseWriter writer = context.getResponseWriter();
        String styleClass;
        for (int i = 0; i < pages; i++) {

            styleClass = "";
            pagination.append("<a href=\"" + url + i + "\"");

            // Add stylesheet class for current pagination link
            if (currentPage != null && currentClass != null && i == currentPage) {
                styleClass = currentClass;
            }

            // Add stylesheet class attribute to link if not empty
            if (!styleClass.equals("")) {
                pagination.append(" class=\"" + styleClass.trim() + "\"");
            }

            pagination.append(">" + (i + 1) + "</a>\n");
        }
        
        // Write pagination to JSF page
        writer.write(pagination.toString());
    }
}
