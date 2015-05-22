package hu.api;

import hu.model.api.OauthSession;
import hu.persistence.IApiStore;
import hu.persistence.IPersistenceProvider;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLDecoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;

/**
 * Video servlet for serving video files from home directory.
 * 
 * @author BalusC
 * @link http://balusc.blogspot.com/2007/04/imageservlet.html
 */
public class VideoServlet extends AbstractServlet {

	private static final long serialVersionUID = 1L;

	private static final int DEFAULT_BUFFER_SIZE = 10240; // 10KB.

	private String videoPath;

	public void init() throws ServletException {

		// Define base path somehow. You can define it as init-param of the
		// servlet.
		this.videoPath = System.getProperty("user.home")
				+ "/.sivaServer/videos";
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		// Check if there was an authorization header sent and show 400 if not
		String authorizationHeader = request.getHeader("Authorization");
		if (authorizationHeader == null) {
			this.sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "No authorization header set");
			return;
		}

		// Check if format of authorization header is correct and show 400 if
		// not
		if (!authorizationHeader.substring(0, 7).equals("Bearer ")) {
			this.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Malformed authorization header");
			return;
		}

		// Decode token and check if there is an oauth session using it
		String token = new String(
				Base64.decodeBase64(authorizationHeader.substring(7).trim()),
				"UTF-8");
		IPersistenceProvider persistenceProvider = (IPersistenceProvider) getServletContext()
				.getAttribute("PersistenceProvider");
		IApiStore apiStore = persistenceProvider.getApiStore();
		OauthSession session = apiStore.findOauthSessionByToken(token);
		if(session == null){
			this.sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token expired or unknown");
			return;
		}
		else if(!session.getScope().equals("public")){
			this.sendError(response, HttpServletResponse.SC_FORBIDDEN, "Token not allowed to access contents of scope 'public'.");
			return;
		}
		
		// Get requested video by path info
		String requestedVideo = request.getPathInfo();

		// Check if file requested in the URI is not empty and send 404 error if it is
		if(requestedVideo == null){
			this.sendError(response, HttpServletResponse.SC_NOT_FOUND, "No file specified");
			return;
		}

		// Decode the file name from the URL and check if file actually exists in
		// file system, send 404 if not
		File video = new File(videoPath, URLDecoder.decode(requestedVideo,
				"UTF-8"));
		
		if(!video.exists()){
			this.sendError(response, HttpServletResponse.SC_NOT_FOUND, "File not found");
			return;
		}

		// Get content type and make sure a content type exists, send 404 otherwise
		String contentType = getServletContext().getMimeType(video.getName());

		// Start reponse with sending required HTTP headers
		response.reset();
		response.setBufferSize(DEFAULT_BUFFER_SIZE);
		if(contentType != null){
			response.setContentType(contentType);
		}
		response.setHeader("Content-Length", String.valueOf(video.length()));
		response.setHeader("Content-Disposition",
				"inline; filename=\"" + video.getName() + "\"");

		
		// Read file and write it's contents into the response
		BufferedInputStream input = null;
		BufferedOutputStream output = null;

		try{
			input = new BufferedInputStream(new FileInputStream(video),
					DEFAULT_BUFFER_SIZE);
			output = new BufferedOutputStream(response.getOutputStream(),
					DEFAULT_BUFFER_SIZE);

			byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
			int length;
			while ((length = input.read(buffer)) > 0) {
				output.write(buffer, 0, length);
			}
		} catch(IOException ignore){
			
			// Send 404 error if an error occurs
			this.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Could not write file contents to output stream");
		}
		finally{
			
			// Close files even if an error occurs
			close(output);
			close(input);
		}
	}

	private static void close(Closeable resource){
		
		// Check if specified file is not null and try to close it if so
		if(resource != null){
			try{
				resource.close();
			} catch(IOException ignore){
				// ignore
			}
		}
	}
}