package hu.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLDecoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Image servlet for serving photos from home directory.
 * 
 * @author BalusC
 * @link http://balusc.blogspot.com/2007/04/imageservlet.html
 */
public class PhotoServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final int DEFAULT_BUFFER_SIZE = 10240; // 10KB.

	private String imagePath;

	public void init() throws ServletException {

		// Define base path somehow. You can define it as init-param of the
		// servlet.
		this.imagePath = System.getProperty("user.home")
				+ "/.sivaServer/photos";
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		// Get requested photo by path info
		String requestedImage = request.getPathInfo();

		// Check if file requested in the URI is not empty and send 404 error if it is
		if(requestedImage == null){
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		// Decode the file name from the URL and check if file actually exists in
		// file system, send 404 if not
		File image = new File(imagePath, URLDecoder.decode(requestedImage,
				"UTF-8"));

		if(!image.exists()){
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		// Get content type and make sure a content type exists, send 404 otherwise
		String contentType = getServletContext().getMimeType(image.getName());

		if(contentType == null || !contentType.startsWith("image")){
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		// Start reponse with sending required HTTP headers
		response.reset();
		response.setBufferSize(DEFAULT_BUFFER_SIZE);
		response.setContentType(contentType);
		response.setHeader("Content-Length", String.valueOf(image.length()));
		response.setHeader("Content-Disposition",
				"inline; filename=\"" + image.getName() + "\"");

		// Read file and write it's contents into the response
		BufferedInputStream input = null;
		BufferedOutputStream output = null;

		try{
			input = new BufferedInputStream(new FileInputStream(image),
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
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		} finally {
			
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