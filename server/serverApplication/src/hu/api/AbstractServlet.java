package hu.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

/**
 * Abstract servlet class for functions thata have to be available in all
 * servlets.
 */
public abstract class AbstractServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	protected void sendError(HttpServletResponse response, Integer errorCode, String errorDescription) throws IOException{
		this.sendError(response, errorCode, errorDescription, null);
	}
	
	protected void sendError(HttpServletResponse response, Integer errorCode, String errorDescription, String additionalInformation) throws IOException{			
		response.setStatus(errorCode);
		response.setContentType("application/json");
		response.setCharacterEncoding("utf-8");
		response.addHeader("WWW-Authenticate", "OAuth realm=\"/token\"");
		
		String message;
		if(errorCode == HttpServletResponse.SC_BAD_REQUEST){
			message = "Bad request";
		}
		else if(errorCode == HttpServletResponse.SC_UNAUTHORIZED){
			message = "Unauthorized";
		} 
		else if(errorCode == HttpServletResponse.SC_FORBIDDEN){
			message = "Forbidden";
		}
		else if(errorCode == HttpServletResponse.SC_NOT_FOUND){
			message = "Not Found";
		}
		else{
			message ="Uknown Error";
		}
		
		Map<String,String> jsonMap = new HashMap<String,String>();
		jsonMap.put("code", errorCode.toString());
		jsonMap.put("message", message);
		jsonMap.put("description", errorDescription);
		if(additionalInformation != null){
			jsonMap.put("additionalInformation", additionalInformation);
		}
		response.getWriter().write((new JSONObject(jsonMap)).toString());
		response.getWriter().flush();
		response.getWriter().close();
	}	
}