/*
 * net/balusc/webapp/FileServlet.java
 *
 * Copyright (C) 2009 BalusC
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package hu.api;

import hu.model.EParticipationRestriction;
import hu.model.Video;
import hu.model.api.CollaborationMedia;
import hu.model.api.CollaborationPost;
import hu.model.api.CollaborationThread;
import hu.model.api.ECollaborationThreadVisibility;
import hu.model.api.SivaPlayerLogEntry;
import hu.model.api.SivaPlayerSession;
import hu.model.users.EUserType;
import hu.model.users.User;
import hu.persistence.IApiStore;
import hu.persistence.IPersistenceProvider;
import hu.persistence.IUserStore;
import hu.persistence.IVideoStore;
import hu.persistence.InconsistencyException;
import hu.util.BrandingConfiguration;
import hu.util.CommonUtils;
import hu.util.MailService;
import hu.util.SecurityUtils;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.zip.GZIPOutputStream;

import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A file servlet supporting resume of downloads and client-side caching and
 * GZIP of text content. This servlet can also be used for images, client-side
 * caching would become more efficient. This servlet can also be used for text
 * files, GZIP would decrease network bandwidth.
 * 
 * @author BalusC
 * @link 
 *       http://balusc.blogspot.com/2009/02/fileservlet-supporting-resume-and.html
 */
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 10, maxFileSize = 1024 * 1024 * 12, maxRequestSize = 1024 * 1024 * 101)
public class SivaPlayerVideoServlet extends AbstractServlet {

    private static final long serialVersionUID = 1L;
    private static final int DEFAULT_BUFFER_SIZE = 10240; // 10KB.
    private static final long DEFAULT_EXPIRE_TIME = 86400000L; // ..ms = 1 week.
    private static final String MULTIPART_BOUNDARY = "MULTIPART_BYTERANGES";
    private static final ArrayList<String> ALLOWED_FILE_TYPES = new ArrayList<String>() {
	
	private static final long serialVersionUID = 1L;

	{
	    add("jpg");
	    add("jpeg");
	    add("png");
	    add("gif");
	    add("pdf");
	}
    };

    private boolean isAJAXRequest = false;
    private String videoPath;
    private SivaPlayerSession session = null;
    private IPersistenceProvider persistenceProvider;
    private MailService mailService;
    private BrandingConfiguration brandingConfiguration;
    private User currentUser = null;

    public void init() throws ServletException {

	// Define base path somehow. You can define it as init-param of the
	// servlet.
	this.videoPath = System.getProperty("user.home") + "/.sivaServer/videos";
    }

    protected void doHead(HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException {
	this.doAction(request, response, "HEAD");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException {
	this.doAction(request, response, "GET");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException {
	this.doAction(request, response, "POST");
    }

    private void doAction(HttpServletRequest request, HttpServletResponse response,
	    String requestType) throws ServletException, IOException {

	// Check if it's an AJAX request
	this.isAJAXRequest = (request.getParameter("ajax") != null && request.getParameter("ajax")
		.equals("true"));

	// Allow Cross-Origin-Requests
	response.setHeader("Access-Control-Allow-Origin", "*");
	response.setHeader("Access-Control-Allow-Methods", "GET, PUT, POST, DELETE, OPTIONS");
	response.setHeader("Access-Control-Max-Age", "1000");
	response.setHeader("Access-Control-Allow-Headers",
		"Content-Type, Authorization, X-Requested-With");

	// URL pattern: /videoId
	// Get request parameter from URL and check if it has been set.
	// Show 400 if less or more parameters than allowed.
	String requestedVideo = request.getPathInfo();
	if (requestedVideo == null || requestedVideo.split("/").length < 2
		|| requestedVideo.split("/")[1].equals("")) {
	    this.sendError(response, HttpServletResponse.SC_BAD_REQUEST,
		    "The video folder has to be specified for using this web service.");
	    return;
	}

	this.persistenceProvider = (IPersistenceProvider) getServletContext().getAttribute(
		"PersistenceProvider");
	this.mailService = (MailService) getServletContext().getAttribute("mailService");
	this.brandingConfiguration = (BrandingConfiguration) getServletContext().getAttribute(
		"brandingConfiguration");

	// Check if it's a watching request
	if (request.getPathInfo().endsWith("/watch.html")) {
	    this.providePlayer(request, response);
	    return;
	}
	
	// Check if it's a log request and perform logging if so
	if (request.getPathInfo().endsWith("/log") && requestType.equals("POST")) {
	    this.doLogging(request, response);
	    return;
	}

	// Check if it's a checkSession request and provide session status if so
	if (requestedVideo.endsWith("/getStats.js")) {
	    this.getStats(request, response);
	    return;
	}

	// Check if user requests user secret and perform login
	if (request.getPathInfo().endsWith("/getSecret.js") && requestType.equals("POST")) {
	    this.provideUserSecret(request, response, requestType);
	    return;
	}

	// Check if current session exists and if it is allowed to access this
	// video, stop further execution, if so.
	boolean result = handleAccess(request, response, requestType);
	if (!result) {
	    return;
	}

	// Check if it's collaboration request and provide data
	if (request.getPathInfo().endsWith("/getCollaboration.js")) {
	    this.provideCollaboration(request, response);
	    return;
	}

	// Check if it's a thread creation request
	if (request.getPathInfo().endsWith("/createCollaborationThread.js")) {
	    this.createCollaborationThread(request, response);
	    return;
	}

	// Check if it's a post creation request
	if (request.getPathInfo().endsWith("/createCollaborationPost.js")) {
	    this.createCollaborationPost(request, response);
	    return;
	}
	
	// Check if it's a post activation request
	if (request.getPathInfo().endsWith("/activateCollaborationPost.js")) {
	    this.activateCollaborationPost(request, response);
	    return;
	}

	// Check if it's a post creation request
	if (request.getPathInfo().endsWith("/deleteCollaborationThread.js")) {
	    this.deleteCollaborationThread(request, response);
	    return;
	}

	// Check if it's a post creation request
	if (request.getPathInfo().endsWith("/deleteCollaborationPost.js")) {
	    this.deleteCollaborationPost(request, response);
	    return;
	}

	// Check if it's a checkSession request and provide session status if so
	if (requestedVideo.endsWith("/checkSession.js")) {
	    this.provideSessionStatus(request, response);
	    return;
	}

	// Decode the file name from the URL and check if file actually exists
	// in
	// file system, send 404 if not
	File file = new File(videoPath, URLDecoder.decode(requestedVideo, "UTF-8"));
	if (!file.exists()) {
	    this.sendError(response, HttpServletResponse.SC_NOT_FOUND, "File not found");
	    return;
	}

	// Create log entry for file request
	this.logFileRequest(requestedVideo);

	// Check if configuration is requested and do needed preparing and
	// stop standard file preparation
	if (file.getName().equals("export.js")) {
	    this.provideConfigFile(request, response, file);
	    return;
	}

	// Prepare some variables. The ETag is an unique identifier of the file.
	String fileName = file.getName();
	long length = file.length();
	long lastModified = file.lastModified();
	String eTag = fileName + "_" + length + "_" + lastModified;
	long expires = System.currentTimeMillis() + DEFAULT_EXPIRE_TIME;

	// Validate request headers for caching
	// ---------------------------------------------------

	// If-None-Match header should contain "*" or ETag. If so, then return
	// 304.
	String ifNoneMatch = request.getHeader("If-None-Match");
	if (ifNoneMatch != null && matches(ifNoneMatch, eTag)) {
	    response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
	    response.setHeader("ETag", eTag); // Required in 304.
	    response.setDateHeader("Expires", expires); // Postpone cache with 1
							// week.
	    return;
	}

	// If-Modified-Since header should be greater than LastModified. If so,
	// then return 304.
	// This header is ignored if any If-None-Match header is specified.
	long ifModifiedSince = request.getDateHeader("If-Modified-Since");
	if (ifNoneMatch == null && ifModifiedSince != -1 && ifModifiedSince + 1000 > lastModified) {
	    response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
	    response.setHeader("ETag", eTag); // Required in 304.
	    response.setDateHeader("Expires", expires); // Postpone cache with 1
							// week.
	    return;
	}

	// Validate request headers for resume
	// ----------------------------------------------------

	// If-Match header should contain "*" or ETag. If not, then return 412.
	String ifMatch = request.getHeader("If-Match");
	if (ifMatch != null && !matches(ifMatch, eTag)) {
	    response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
	    return;
	}

	// If-Unmodified-Since header should be greater than LastModified. If
	// not, then return 412.
	long ifUnmodifiedSince = request.getDateHeader("If-Unmodified-Since");
	if (ifUnmodifiedSince != -1 && ifUnmodifiedSince + 1000 <= lastModified) {
	    response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
	    return;
	}

	// Validate and process range
	// -------------------------------------------------------------

	// Prepare some variables. The full Range represents the complete file.
	Range full = new Range(0, length - 1, length);
	List<Range> ranges = new ArrayList<Range>();

	// Validate and process Range and If-Range headers.
	String range = request.getHeader("Range");
	if (range != null) {

	    // Range header should match format "bytes=n-n,n-n,n-n...". If not,
	    // then return 416.
	    if (!range.matches("^bytes=\\d*-\\d*(,\\d*-\\d*)*$")) {
		response.setHeader("Content-Range", "bytes */" + length); // Required
									  // in
									  // 416.
		response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
		return;
	    }

	    // If-Range header should either match ETag or be greater then
	    // LastModified. If not,
	    // then return full file.
	    String ifRange = request.getHeader("If-Range");
	    if (ifRange != null && !ifRange.equals(eTag)) {
		try {
		    long ifRangeTime = request.getDateHeader("If-Range"); // Throws
									  // IAE
									  // if
									  // invalid.
		    if (ifRangeTime != -1 && ifRangeTime + 1000 < lastModified) {
			ranges.add(full);
		    }
		} catch (IllegalArgumentException ignore) {
		    ranges.add(full);
		}
	    }

	    // If any valid If-Range header, then process each part of byte
	    // range.
	    if (ranges.isEmpty()) {
		for (String part : range.substring(6).split(",")) {
		    // Assuming a file with length of 100, the following
		    // examples returns bytes at:
		    // 50-80 (50 to 80), 40- (40 to length=100), -20
		    // (length-20=80 to length=100).
		    long start = sublong(part, 0, part.indexOf("-"));
		    long end = sublong(part, part.indexOf("-") + 1, part.length());

		    if (start == -1) {
			start = length - end;
			end = length - 1;
		    } else if (end == -1 || end > length - 1) {
			end = length - 1;
		    }

		    // Check if Range is syntactically valid. If not, then
		    // return 416.
		    if (start > end) {
			response.setHeader("Content-Range", "bytes */" + length); // Required
										  // in
										  // 416.
			response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
			return;
		    }

		    // Add range.
		    ranges.add(new Range(start, end, length));
		}
	    }
	}

	// Prepare and initialize response
	// --------------------------------------------------------

	// Get content type by file name and set default GZIP support and
	// content disposition.
	String contentType = getServletContext().getMimeType(fileName);
	boolean acceptsGzip = false;
	String disposition = "inline";

	// If content type is unknown, then set the default value.
	// For all content types, see:
	// http://www.w3schools.com/media/media_mimeref.asp
	// To add new content types, add new mime-mapping entry in web.xml.
	if (contentType == null) {
	    contentType = "application/octet-stream";
	}

	// If content type is text, then determine whether GZIP content encoding
	// is supported by
	// the browser and expand content type with the one and right character
	// encoding.
	if (contentType.startsWith("text")) {
	    String acceptEncoding = request.getHeader("Accept-Encoding");
	    acceptsGzip = acceptEncoding != null && accepts(acceptEncoding, "gzip");
	    contentType += ";charset=UTF-8";
	}

	// Else, expect for images, determine content disposition. If content
	// type is supported by
	// the browser, then set to inline, else attachment which will pop a
	// 'save as' dialogue.
	else if (!contentType.startsWith("image")) {
	    String accept = request.getHeader("Accept");
	    disposition = accept != null && accepts(accept, contentType) ? "inline" : "attachment";
	}

	// Initialize response.
	response.reset();
	response.setBufferSize(DEFAULT_BUFFER_SIZE);
	response.setHeader("Content-Disposition", disposition + ";filename=\"" + fileName + "\"");
	response.setHeader("Accept-Ranges", "bytes");
	response.setHeader("ETag", eTag);
	response.setDateHeader("Last-Modified", lastModified);
	response.setDateHeader("Expires", expires);

	// Send requested file (part(s)) to client
	// ------------------------------------------------

	// Prepare streams.
	RandomAccessFile input = null;
	OutputStream output = null;

	try {
	    // Open streams.
	    input = new RandomAccessFile(file, "r");
	    output = response.getOutputStream();

	    if (ranges.isEmpty() || ranges.get(0) == full) {

		// Return full file.
		Range r = full;
		response.setContentType(contentType);
		response.setHeader("Content-Range", "bytes " + r.start + "-" + r.end + "/"
			+ r.total);

		if (requestType.equals("GET")) {
		    if (acceptsGzip) {
			// The browser accepts GZIP, so GZIP the content.
			response.setHeader("Content-Encoding", "gzip");
			output = new GZIPOutputStream(output, DEFAULT_BUFFER_SIZE);
		    } else {
			// Content length is not directly predictable in case of
			// GZIP.
			// So only add it if there is no means of GZIP, else
			// browser will hang.
			response.setHeader("Content-Length", String.valueOf(r.length));
		    }

		    // Copy full range.
		    copy(input, output, r.start, r.length);
		}

	    } else if (ranges.size() == 1) {

		// Return single part of file.
		Range r = ranges.get(0);
		response.setContentType(contentType);
		response.setHeader("Content-Range", "bytes " + r.start + "-" + r.end + "/"
			+ r.total);
		response.setHeader("Content-Length", String.valueOf(r.length));
		response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT); // 206.

		if (requestType.equals("GET")) {
		    // Copy single part range.
		    copy(input, output, r.start, r.length);
		}

	    } else {

		// Return multiple parts of file.
		response.setContentType("multipart/byteranges; boundary=" + MULTIPART_BOUNDARY);
		response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT); // 206.

		if (requestType.equals("GET")) {
		    // Cast back to ServletOutputStream to get the easy println
		    // methods.
		    ServletOutputStream sos = (ServletOutputStream) output;

		    // Copy multi part range.
		    for (Range r : ranges) {
			// Add multipart boundary and header fields for every
			// range.
			sos.println();
			sos.println("--" + MULTIPART_BOUNDARY);
			sos.println("Content-Type: " + contentType);
			sos.println("Content-Range: bytes " + r.start + "-" + r.end + "/" + r.total);

			// Copy single part range of multi part range.
			copy(input, output, r.start, r.length);
		    }

		    // End with multipart boundary.
		    sos.println();
		    sos.println("--" + MULTIPART_BOUNDARY + "--");
		}
	    }
	} finally {
	    // Gently close streams.
	    close(output);
	    close(input);
	}
    }

    /**
     * Write prepared configuration file to response.
     * 
     * @param request
     *            servlet.
     * @param response
     *            servlet.
     * @param file
     *            containing the configuration.
     * @throws IOException
     */
    private void provideConfigFile(HttpServletRequest request, HttpServletResponse response,
	    File file) throws IOException {
	// Check if it's an AJAX request or a normal JavaScript file request
	// and provide therefore prepared configuration.
	FileInputStream in = new FileInputStream(file);
	if (this.isAJAXRequest) {
	    this.writeJSON(
		    response,
		    "{\"accessRestriction\":{\"accessToken\":\""
			    + this.session.getSessionToken()
			    + "\",\"expiresAt\":\""
			    + this.session.getExpireDate().toString()
			    + "\",\"passed\":true},"
			    + IOUtils.toString(in, Charset.defaultCharset()).split("\\.src,")[1]
				    .replaceFirst("\\);$", ""));
	} else {
	    String[] config = IOUtils.toString(in, Charset.defaultCharset()).split("\\.push\\(\\{");
	    this.isAJAXRequest = true;
	    this.writeJSON(response, config[0] + ".push({\"accessRestriction\":{\"accessToken\":\""
		    + this.session.getSessionToken() + "\",\"expiresAt\":\""
		    + this.session.getExpireDate().toString() + "\",\"passed\":true}," + config[1]);
	}
    }

    /**
     * Write current file as loadFile entry to log
     * 
     * @param requestedVideo
     *            file to log.
     */
    private void logFileRequest(String requestedVideo) {
	ArrayList<SivaPlayerLogEntry> logEntries = new ArrayList<SivaPlayerLogEntry>();
	SivaPlayerLogEntry logEntry = new SivaPlayerLogEntry();
	logEntry.setSessionId(this.session.getId());
	logEntry.setType("loadFile");
	logEntry.setElement(requestedVideo);
	logEntry.setTime(new Date());
	logEntries.add(logEntry);
	try {
	    this.persistenceProvider.getApiStore().createSivaPlayerLogEntries(logEntries);
	} catch (InconsistencyException ignore) {
	    // ignore
	}
    }

    /**
     * Perform login and return user secret to response.
     * 
     * @param request
     *            servlet.
     * @param response
     *            servlet.
     * @param requestType
     *            of this request.
     * @throws IOException
     */
    private void provideUserSecret(HttpServletRequest request, HttpServletResponse response,
	    String requestType) throws IOException {

	// Check if it is a GET request and provide information about
	// the access
	// restriction form if so
	if (!requestType.equals("POST")) {
	    this.sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "loginRequiredError",
		    "login");
	    return;
	}

	IVideoStore videoStore = this.persistenceProvider.getVideoStore();
	IUserStore userStore = this.persistenceProvider.getUserStore();

	// Get video from database and show 404 if video does not exist
	String videoDirectory = request.getPathInfo().split("/")[1];
	Video video = videoStore.findByDirectory(videoDirectory);
	if (video == null) {
	    this.sendError(response, HttpServletResponse.SC_NOT_FOUND, "videoNotExistingError");
	    return;
	}

	// Check if credentials where transmitted via HTTP POST and show
	// 400 if not
	String userName = request.getParameter("username");
	String userPassword = request.getParameter("password");
	if (userName == null || userPassword == null) {
	    this.sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "userNotFoundError");
	    return;
	}

	// Check if a user with these credentials exists and if it is
	// not banned, show 401 if so
	User user = userStore.findByEmail(userName);
	String passwordHash = SecurityUtils.hash(userPassword);
	if (user == null || !passwordHash.equals(user.getPasswordHash())) {
	    this.sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "userNotFoundError");
	    return;
	} else if (user.isBanned()) {
	    this.sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "userBannedError");
	    return;
	}

	// Check if video is active or user is administrator or owner of the
	// video
	Date currentDate = new Date();
	if ((user == null || (!user.getUserType().equals(EUserType.Administrator) && !userStore
		.isUserOwnerOfVideo(user.getId(), video.getId())))
		&& (video.getStart() == null || video.getStart().compareTo(currentDate) > 0 || (video
			.getStop() != null && video.getStop().compareTo(currentDate) < 0))) {
	    this.sendError(response, HttpServletResponse.SC_NOT_FOUND, "videoNotActiveError");
	    return;
	}

	// Check if user is a group attendant if the video is group
	// restricted
	if (video.getParticipationRestriction() == EParticipationRestriction.GroupAttendants) {
	    if (!userStore.isUserAttendantOfGroup(user.getId(), video.getId())
		    && !userStore.isUserOwnerOfVideo(user.getId(), video.getId())
		    && user.getUserType() != EUserType.Administrator) {
		this.sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "noAttendantError");
		return;
	    }
	}

	// Send always positive session status as earlier session checks
	// allowed to continue processing.
	Map<String, String> jsonMap = new HashMap<String, String>();
	jsonMap.put("userEmail", user.getEmail());
	jsonMap.put("userSecret", user.getSecretKey());
	this.isAJAXRequest = true;
	response.setStatus(HttpServletResponse.SC_OK);
	this.writeJSON(response, (new JSONObject(jsonMap)).toString());
    }

    /**
     * Write collaboration information for the given video and scene as JSON.
     * 
     * @param request
     *            servlet.
     * @param response
     *            servlet.
     * @throws IOException
     */
    private void provideCollaboration(HttpServletRequest request, HttpServletResponse response)
	    throws IOException {

	IVideoStore videoStore = this.persistenceProvider.getVideoStore();
	IApiStore apiStore = this.persistenceProvider.getApiStore();
	IUserStore userStore = this.persistenceProvider.getUserStore();

	// Get video from database and show 404 if video does not exist
	String videoDirectory = request.getPathInfo().split("/")[1];
	Video video = videoStore.findByDirectory(videoDirectory);
	if (video == null) {
	    this.sendError(response, HttpServletResponse.SC_NOT_FOUND, "videoNotExistingError");
	    return;
	}

	// Get scene and show 401 if no scene is given
	String scene = request.getParameter("scene");
	if (scene == null || scene.equals("")) {
	    this.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "malformedDataError");
	    return;
	}

	// Get threads, posts and media from database and write it to JSON
	List<CollaborationThread> threads = apiStore.listCollaborationThreads(video.getId(), scene);
	JSONArray json = new JSONArray();
	CollaborationThreadLoop: for (CollaborationThread thread : threads) {
	    JSONObject jsonThread = new JSONObject();
	    try {
		jsonThread.put("threadId", thread.getId());
		jsonThread.put("title", thread.getTitle());
		JSONArray jsonPosts = new JSONArray();
		int i = 0;
		List<CollaborationPost> posts = apiStore.listCollaborationPosts(thread.getId());
		for (CollaborationPost post : posts) {
		    boolean isOwner = userStore.isUserOwnerOfVideo(this.currentUser.getId(),
			    video.getId());

		    // Skip thread if visibility settings are not met
		    if (i == 0
			    && !(thread.getVisibility() == ECollaborationThreadVisibility.All
				    || post.getUserId() == this.currentUser.getId() || (thread
				    .getVisibility() == ECollaborationThreadVisibility.Administrator && (isOwner || this.currentUser
				    .getUserType() == EUserType.Administrator)))) {
			continue CollaborationThreadLoop;
		    }

		    // Skip post if not active and not post owner, video owner
		    // or administrator
		    if (!post.isActive() && !isOwner
			    && this.currentUser.getUserType() != EUserType.Administrator
			    && post.getUserId() != this.currentUser.getId()) {

			// Skip whole thread if first post is not active yet
			if (i == 0) {
			    continue CollaborationThreadLoop;
			}
			continue;
		    }

		    JSONObject jsonPost = new JSONObject();
		    jsonPost.put("id", post.getId());
		    User user = this.persistenceProvider.getUserStore().findById(post.getUserId());
		    if (user != null) {
			jsonPost.put("user", ((user.getTitle() != null) ? user.getTitle() + " "
				: "") + user.getFirstName() + " " + user.getLastName());
		    } else {
			jsonPost.put("user", "Anonymous");
		    }
		    jsonPost.put("date", post.getDate().getTime());
		    jsonPost.put("post", post.getPost());
		    jsonPost.put("active", (post.isActive() || (this.currentUser.getUserType() != EUserType.Administrator && !isOwner)));
		    jsonPost.put(
			    "manageable",
			    ((user != null && this.currentUser.getId() == user.getId())
				    || this.currentUser.getUserType() == EUserType.Administrator || isOwner));
		    JSONArray jsonMedia = new JSONArray();
		    List<CollaborationMedia> media = apiStore.listCollaborationMedia(post.getId());
		    for (CollaborationMedia mediaFile : media) {
			JSONObject jsonMediaFile = new JSONObject();
			jsonMediaFile.put("id", mediaFile.getId());
			jsonMediaFile.put("filename", "collaboration/" + mediaFile.getId() + "-"
				+ mediaFile.getFilename());
			jsonMedia.put(jsonMediaFile);
		    }
		    jsonPost.put("media", jsonMedia);
		    jsonPosts.put(jsonPost);
		    i++;
		}
		if(i == 0){
		    continue;
		}
		jsonThread.put("posts", jsonPosts);
		jsonThread.put("start", thread.getDurationFrom());
		jsonThread.put("end", thread.getDurationTo());
	    } catch (JSONException e) {
		this.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "malformedDataError");
	    }
	    json.put(jsonThread);
	}

	this.isAJAXRequest = true;
	response.setStatus(HttpServletResponse.SC_OK);
	this.writeJSON(response, json.toString());
    }

    /**
     * Create a new {@link CollaborationThread} based on the user input.
     * 
     * @param request
     *            servlet.
     * @param response
     *            servlet.
     * @throws IOException
     * @throws ServletException
     * @throws IllegalStateException
     */
    private void createCollaborationThread(HttpServletRequest request, HttpServletResponse response)
	    throws IOException, IllegalStateException, ServletException {

	IVideoStore videoStore = this.persistenceProvider.getVideoStore();
	IApiStore apiStore = this.persistenceProvider.getApiStore();

	// Get video from database and show 404 if video does not exist
	String videoDirectory = request.getPathInfo().split("/")[1];
	Video video = videoStore.findByDirectory(videoDirectory);
	if (video == null) {
	    this.sendError(response, HttpServletResponse.SC_NOT_FOUND, "videoNotExistingError");
	    return;
	}

	// Get scene and show 401 if no scene is given
	String scene = request.getParameter("scene");
	if (scene == null || scene.equals("")) {
	    this.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "malformedDataError");
	    return;
	}

	// Create and save thread based on input
	CollaborationThread thread = new CollaborationThread(null);
	thread.setVideoId(video.getId());
	thread.setScene(request.getParameter("scene"));
	thread.setTitle(new String(request.getParameter("title").getBytes("iso-8859-1"), "UTF-8"));
	String[] durationFrom = request.getParameter("durationFrom").split(":");
	thread.setDurationFrom((Integer.parseInt(durationFrom[0]) * 60 + Integer
		.parseInt(durationFrom[1])));
	String[] durationTo = request.getParameter("durationTo").split(":");
	thread.setDurationTo((Integer.parseInt(durationTo[0]) * 60 + Integer
		.parseInt(durationTo[01])));
	thread.setVisibility(ECollaborationThreadVisibility.All);
	for (ECollaborationThreadVisibility visibility : ECollaborationThreadVisibility.values()) {
	    if (request.getParameter("visibility").equals(visibility.toString())) {
		thread.setVisibility(visibility);
	    }
	}

	try {
	    thread = apiStore.createCollaborationThread(thread);
	} catch (InconsistencyException e) {
	    this.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "malformedDataError");
	    return;
	}

	// Create post and media and delete thread if not successful
	if (!this.savePost(thread, video, request, response)) {
	    try {
		apiStore.deleteCollaborationThread(thread.getId());
	    } catch (InconsistencyException e) {
	    }
	}
    }

    /**
     * Create a new {@link CollaborationThread} based on the user input.
     * 
     * @param request
     *            servlet.
     * @param response
     *            servlet.
     * @throws IOException
     * @throws ServletException
     * @throws IllegalStateException
     */
    private void createCollaborationPost(HttpServletRequest request, HttpServletResponse response)
	    throws IOException, IllegalStateException, ServletException {

	IVideoStore videoStore = this.persistenceProvider.getVideoStore();
	IApiStore apiStore = this.persistenceProvider.getApiStore();

	// Get video from database and show 404 if video does not exist
	String videoDirectory = request.getPathInfo().split("/")[1];
	Video video = videoStore.findByDirectory(videoDirectory);
	if (video == null) {
	    this.sendError(response, HttpServletResponse.SC_NOT_FOUND, "videoNotExistingError");
	    return;
	}

	// Get scene and show 401 if no scene is given
	String scene = request.getParameter("scene");
	if (scene == null || scene.equals("")) {
	    this.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "malformedDataError");
	    return;
	}

	// Get thread from database
	if (request.getParameter("thread") == null) {
	    this.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "malformedDataError");
	    return;
	}
	CollaborationThread thread = apiStore.findCollaborationThreadById(Integer.parseInt(request
		.getParameter("thread")));
	if (thread == null) {
	    this.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "threadNotExisting");
	    return;
	}

	// Create post and media
	this.savePost(thread, video, request, response);
    }

    /**
     * Write current session status to response.
     * 
     * @param request
     *            servlet.
     * @param response
     *            servlet.
     * @throws IOException
     * @throws ServletException
     * @throws IllegalStateException
     */
    private boolean savePost(CollaborationThread thread, Video video, HttpServletRequest request,
	    HttpServletResponse response) throws IOException, IllegalStateException,
	    ServletException {
	IApiStore apiStore = this.persistenceProvider.getApiStore();
	IUserStore userStore = this.persistenceProvider.getUserStore();

	// Create and save post based on input
	CollaborationPost post = new CollaborationPost(null);
	post.setThreadId(thread.getId());
	post.setUserId(this.currentUser.getId());
	post.setPost(new String(request.getParameter("post").getBytes("iso-8859-1"), "UTF-8"));
	post.setActive(userStore.isUserOwnerOfVideo(this.currentUser.getId(), video.getId())
		|| this.currentUser.getUserType() == EUserType.Administrator
		|| thread.getVisibility() == ECollaborationThreadVisibility.Me);

	try {
	    post = apiStore.createCollaborationPost(post);
	} catch (InconsistencyException e) {
	    this.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "malformedDataError");
	    return false;
	}

	// Create and save media based on input
	for (Part part : request.getParts()) {
	    if (part.getName().equals("media[]")) {
		String filename = this.getFileName(part).replaceAll("[^a-zA-Z0-9.-]", "");
		String[] extension = filename.split("\\.");
		if (filename.equals("") || extension.length <= 1
			|| !ALLOWED_FILE_TYPES.contains(extension[extension.length - 1])) {
		    continue;
		}
		CollaborationMedia media = new CollaborationMedia(null);
		media.setPostId(post.getId());
		media.setFilename(filename);
		try {
		    media = apiStore.createCollaborationMedia(media);
		} catch (InconsistencyException e) {
		    try {
			apiStore.deleteCollaborationPost(post.getId());
		    } catch (InconsistencyException e1) {
		    }
		    this.sendError(response, HttpServletResponse.SC_BAD_REQUEST,
			    "malformedDataError");
		    return false;
		}
		File directory = new File(this.videoPath + "/" + video.getDirectory()
			+ "/collaboration");
		if (!directory.exists()) {
		    directory.mkdir();
		}
		part.write(this.videoPath + "/" + video.getDirectory() + "/collaboration/"
			+ media.getId() + "-" + media.getFilename());
	    }
	}

	// Write positive result and information about publishing state
	Map<String, String> jsonMap = new HashMap<String, String>();
	jsonMap.put("saved", "true");
	if (userStore.isUserOwnerOfVideo(this.currentUser.getId(), video.getId())
		|| this.currentUser.getUserType() == EUserType.Administrator
		|| thread.getVisibility() == ECollaborationThreadVisibility.Me
		|| (thread.getVisibility() == ECollaborationThreadVisibility.Administrator && this.currentUser
			.getUserType() == EUserType.Administrator)) {
	    jsonMap.put("message", "collaborationPublished");
	} else {
	    jsonMap.put("message", "collaborationActivationNeeded");
	    int[] videoIds = { video.getId() };

	    // Initialize JSF to get property files
	    this.getFacesContext(request, response);

	    Map<Integer, List<User>> owners = userStore.getUsersOwningGroupsOfVideos(videoIds);
	    for (User owner : owners.get(video.getId())) {
		try {
		    this.mailService.sendMail(owner.getEmail(), String.format(
			    this.getCommonMessage("send_mail_new_collaboration_subject"),
			    video.getTitle()), String.format(this
			    .getCommonMessage("send_mail_new_collaboration"), thread.getTitle(), post.getPost(),
			    CommonUtils.buildContextPath(
				    "/sivaPlayerVideos/" + video.getDirectory() + "/watch.html", null) + "#0="
					    + thread.getScene() + "%2C" + thread.getDurationFrom(),
				    this.brandingConfiguration
				    .getBrandingText("project_name")));
		} catch (URISyntaxException e) {}
		catch (IllegalArgumentException e){}
	    }
	}
	this.isAJAXRequest = true;
	response.setStatus(HttpServletResponse.SC_OK);
	this.writeJSON(response, (new JSONObject(jsonMap)).toString());
	return true;
    }
    
    /**
     * Activate the specified {@link CollaborationPost}.
     * 
     * @param request
     *            servlet.
     * @param response
     *            servlet.
     * @throws IOException
     * @throws ServletException
     * @throws IllegalStateException
     */
    private void activateCollaborationPost(HttpServletRequest request, HttpServletResponse response)
	    throws IOException, IllegalStateException, ServletException {

	IVideoStore videoStore = this.persistenceProvider.getVideoStore();
	IApiStore apiStore = this.persistenceProvider.getApiStore();
	IUserStore userStore = this.persistenceProvider.getUserStore();

	// Get video from database and show 404 if video does not exist
	String videoDirectory = request.getPathInfo().split("/")[1];
	Video video = videoStore.findByDirectory(videoDirectory);
	if (video == null) {
	    this.sendError(response, HttpServletResponse.SC_NOT_FOUND, "videoNotExistingError");
	    return;
	}

	// Get postId and show 401 if no scene is given
	if (request.getParameter("postId") == null || request.getParameter("postId").equals("")) {
	    this.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "malformedDataError");
	    return;
	}

	int postId = Integer.parseInt(request.getParameter("postId"));
	CollaborationPost post = apiStore.findCollaborationPostById(postId);
	
	if(post != null){
	    post.setActive(true);
	    if (this.currentUser.getUserType() == EUserType.Administrator
		    || userStore.isUserOwnerOfVideo(this.currentUser.getId(), video.getId())) {
		try {
		    apiStore.saveCollaborationPost(post);
		} catch (InconsistencyException e) {
		    this.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
			    "databaseError");
		    return;
		}
	    }
	}
	
	Map<String, String> jsonMap = new HashMap<String, String>();
	jsonMap.put("saved", "true");
	this.isAJAXRequest = true;
	response.setStatus(HttpServletResponse.SC_OK);
	this.writeJSON(response, (new JSONObject(jsonMap)).toString());
    }

    /**
     * Delete the specified {@link CollaborationThread}.
     * 
     * @param request
     *            servlet.
     * @param response
     *            servlet.
     * @throws IOException
     * @throws ServletException
     * @throws IllegalStateException
     */
    private void deleteCollaborationThread(HttpServletRequest request, HttpServletResponse response)
	    throws IOException, IllegalStateException, ServletException {

	IVideoStore videoStore = this.persistenceProvider.getVideoStore();
	IApiStore apiStore = this.persistenceProvider.getApiStore();
	IUserStore userStore = this.persistenceProvider.getUserStore();

	// Get video from database and show 404 if video does not exist
	String videoDirectory = request.getPathInfo().split("/")[1];
	Video video = videoStore.findByDirectory(videoDirectory);
	if (video == null) {
	    this.sendError(response, HttpServletResponse.SC_NOT_FOUND, "videoNotExistingError");
	    return;
	}

	// Get threadId and show 401 if no scene is given
	if (request.getParameter("threadId") == null || request.getParameter("threadId").equals("")) {
	    this.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "malformedDataError");
	    return;
	}
	
	int threadId = Integer.parseInt(request.getParameter("threadId")); 
	List<CollaborationPost> posts = apiStore.listCollaborationPosts(threadId);
	
	if (posts.size() > 0) {
	    Integer threadPosterId = posts.get(0).getUserId();
	    if ((threadPosterId != null && this.currentUser.getId() == threadPosterId)
		    || this.currentUser.getUserType() == EUserType.Administrator
		    || userStore.isUserOwnerOfVideo(this.currentUser.getId(), video.getId())) {
		try {
		    apiStore.deleteCollaborationThread(threadId);
		} catch (InconsistencyException e) {
		    this.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
			    "databaseError");
		    return;
		}
	    }
	}
	
	Map<String, String> jsonMap = new HashMap<String, String>();
	jsonMap.put("saved", "true");
	this.isAJAXRequest = true;
	response.setStatus(HttpServletResponse.SC_OK);
	this.writeJSON(response, (new JSONObject(jsonMap)).toString());
    }
    
    /**
     * Delete the specified {@link CollaborationPost}.
     * 
     * @param request
     *            servlet.
     * @param response
     *            servlet.
     * @throws IOException
     * @throws ServletException
     * @throws IllegalStateException
     */
    private void deleteCollaborationPost(HttpServletRequest request, HttpServletResponse response)
	    throws IOException, IllegalStateException, ServletException {

	IVideoStore videoStore = this.persistenceProvider.getVideoStore();
	IApiStore apiStore = this.persistenceProvider.getApiStore();
	IUserStore userStore = this.persistenceProvider.getUserStore();

	// Get video from database and show 404 if video does not exist
	String videoDirectory = request.getPathInfo().split("/")[1];
	Video video = videoStore.findByDirectory(videoDirectory);
	if (video == null) {
	    this.sendError(response, HttpServletResponse.SC_NOT_FOUND, "videoNotExistingError");
	    return;
	}

	// Get postId and show 401 if no scene is given
	if (request.getParameter("postId") == null || request.getParameter("postId").equals("")) {
	    this.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "malformedDataError");
	    return;
	}
	
	int postId = Integer.parseInt(request.getParameter("postId")); 
	CollaborationPost post = apiStore.findCollaborationPostById(postId);
	
	if(post != null){
	    Integer posterId = post.getUserId();
	    if ((posterId != null && this.currentUser.getId() == posterId)
		    || this.currentUser.getUserType() == EUserType.Administrator
		    || userStore.isUserOwnerOfVideo(this.currentUser.getId(), video.getId())) {
		try {
		    apiStore.deleteCollaborationPost(postId);
		} catch (InconsistencyException e) {
		    this.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
			    "databaseError");
		    return;
		}
	    }
	}
	
	Map<String, String> jsonMap = new HashMap<String, String>();
	jsonMap.put("saved", "true");
	this.isAJAXRequest = true;
	response.setStatus(HttpServletResponse.SC_OK);
	this.writeJSON(response, (new JSONObject(jsonMap)).toString());
    }

    /**
     * Extracts the filename from the {@link Part} header.
     * 
     * @param part
     *            to parse
     * @return the filename
     */
    private String getFileName(Part part) {
	String contentDisp = part.getHeader("content-disposition");
	String[] tokens = contentDisp.split(";");
	for (String token : tokens) {
	    if (token.trim().startsWith("filename")) {
		return token.substring(token.indexOf("=") + 2, token.length() - 1);
	    }
	}
	return "";
    }

    /**
     * Write current session status to response.
     * 
     * @param request
     *            servlet.
     * @param response
     *            servlet.
     * @throws IOException
     */
    private void provideSessionStatus(HttpServletRequest request, HttpServletResponse response)
	    throws IOException {

	// Send always positive session status as earlier session checks
	// allowed to continue processing.
	Map<String, String> jsonMap = new HashMap<String, String>();
	jsonMap.put("isStillActive", "true");
	this.isAJAXRequest = true;
	response.setStatus(HttpServletResponse.SC_OK);
	this.writeJSON(response, (new JSONObject(jsonMap)).toString());
    }

    /**
     * Check if the video exists, the user is allowed to access it and create a
     * new session if provided authentication information is sufficient.
     * 
     * @param request
     *            servlet.
     * @param response
     *            servlet.
     * @param requestType
     *            of this request.
     * @return true if all restrictions are passed, false if checks failed.
     * @throws IOException
     */
    private boolean handleAccess(HttpServletRequest request, HttpServletResponse response,
	    String requestType) throws IOException {

	IVideoStore videoStore = this.persistenceProvider.getVideoStore();
	IUserStore userStore = this.persistenceProvider.getUserStore();

	// Get video from database and show 404 if video does not exist
	String videoDirectory = request.getPathInfo().split("/")[1];
	Video video = videoStore.findByDirectory(videoDirectory);
	if (video == null) {
	    this.sendError(response, HttpServletResponse.SC_NOT_FOUND, "videoNotExistingError");
	    return false;
	}

	// Check if there already is a session and whether the associated user
	// is an administrator
	// or owner of the video
	IApiStore apiStore = this.persistenceProvider.getApiStore();
	this.session = ((request.getParameter("token") == null || request.getParameter("token")
		.split("-").length != 2) ? null : apiStore.findSivaPlayerSessionByToken(Integer
		.parseInt(request.getParameter("token").split("-")[0]),
		request.getParameter("token").split("-")[1], false));
	if (this.session != null && this.session.getId() != null) {
	    this.currentUser = userStore.findById(this.session.getUserId());
	}

	// Check if video is active or user is administrator or owner of the
	// video
	Date currentDate = new Date();
	if ((this.currentUser == null || (!this.currentUser.getUserType().equals(
		EUserType.Administrator) && !userStore.isUserOwnerOfVideo(this.currentUser.getId(),
		video.getId())))
		&& (video.getStart() == null || video.getStart().compareTo(currentDate) > 0 || (video
			.getStop() != null && video.getStop().compareTo(currentDate) < 0))) {
	    this.sendError(response, HttpServletResponse.SC_NOT_FOUND, "videoNotActiveError");
	    return false;
	}

	// Check if there has been a SivaPlayer session token submitted and if
	// it hasn't been expired, generate a new token otherwise
	if (this.session == null || this.session.getId() == null) {

	    // Check if user has sufficient rights for video and show 401 if not
	    if (video.getParticipationRestriction() == EParticipationRestriction.GroupAttendants
		    || video.getParticipationRestriction() == EParticipationRestriction.Registered) {

		// Check if notice for session expiration is needed
		String sessionNotice = "loginRequiredError";
		if (request.getParameter("token") != null) {
		    sessionNotice = "sessionExpiredLoginError";
		}

		// Check if it is a GET request and provide information about
		// the access
		// restriction form if so
		if (!requestType.equals("POST")) {
		    this.sendError(response, HttpServletResponse.SC_UNAUTHORIZED, sessionNotice,
			    "login");
		    return false;
		}

		// Check if credentials where transmitted via HTTP POST and show
		// 400 if not
		String userName = request.getParameter("username");
		String userPassword = request.getParameter("password");
		String userSecret = request.getParameter("secret");
		if (userName == null || (userPassword == null && userSecret == null)) {
		    this.sendError(response, HttpServletResponse.SC_UNAUTHORIZED,
			    "userNotFoundError");
		    return false;
		}

		// Check if a user with these credentials exists
		this.currentUser = userStore.findByEmail(userName);
		String passwordHash = ((userPassword != null) ? SecurityUtils.hash(userPassword)
			: "");
		if (this.currentUser == null
			|| (!passwordHash.equals(this.currentUser.getPasswordHash()) && !userSecret
				.equals(this.currentUser.getSecretKey()))) {
		    this.sendError(response, HttpServletResponse.SC_UNAUTHORIZED,
			    "userNotFoundError");
		    return false;
		}

		// Check if user is banned and show 401 if so
		if (this.currentUser.isBanned()) {
		    this.sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "userBannedError");
		    return false;
		}

		// Check if user is a group attendant if the video is group
		// restricted
		if (video.getParticipationRestriction() == EParticipationRestriction.GroupAttendants) {
		    if (!userStore.isUserAttendantOfGroup(this.currentUser.getId(), video.getId())
			    && !userStore.isUserOwnerOfVideo(this.currentUser.getId(),
				    video.getId())
			    && this.currentUser.getUserType() != EUserType.Administrator) {
			this.sendError(response, HttpServletResponse.SC_UNAUTHORIZED,
				"noAttendantError");
			return false;
		    }
		}
	    } else if (video.getParticipationRestriction() == EParticipationRestriction.Password) {

		// Check if notice for session expiration is needed
		String sessionNotice = "passwordRequiredError";
		if (request.getParameter("token") != null) {
		    sessionNotice = "sessionExpiredPasswordError";
		}

		// Check if it is a GET request and provide information about
		// the access
		// restriction form if so
		if (!requestType.equals("POST")) {
		    this.sendError(response, HttpServletResponse.SC_UNAUTHORIZED, sessionNotice,
			    "password");
		    return false;
		}

		// Check if encrypted video password is equal to the password in
		// the URL
		String password = request.getParameter("password");
		if (password == null || !video.getPassword().equals(password)) {
		    this.sendError(response, HttpServletResponse.SC_UNAUTHORIZED,
			    "wrongPasswordError");
		    return false;
		}
	    } else if (video.getParticipationRestriction() == EParticipationRestriction.Token) {

		// Check if notice for session expiration is needed
		String sessionNotice = "tokenRequiredError";
		if (request.getParameter("token") != null) {
		    sessionNotice = "sessionExpiredTokenError";
		}

		// Check if it is a GET request and provide information about
		// the access
		// restriction form if so
		if (!requestType.equals("POST")) {
		    this.sendError(response, HttpServletResponse.SC_UNAUTHORIZED, sessionNotice,
			    "token");
		    return false;
		}

		// Check if a token was provided and if it's related to the
		// video
		String token = request.getParameter("token");
		if (token == null) {
		    this.sendError(response, HttpServletResponse.SC_UNAUTHORIZED,
			    "unknownTokenError");
		    return false;
		}

		Video tokenVideo = videoStore.findByToken(token);
		if (tokenVideo == null || !video.getId().equals(tokenVideo.getId())) {
		    this.sendError(response, HttpServletResponse.SC_UNAUTHORIZED,
			    "unknownTokenError");
		    return false;
		}
	    }

	    // Create new session and write it to database
	    if (!this.createSession(request, response, video, this.currentUser)) {
		return false;
	    }

	    // Return session information as JSON output
	    Map<String, String> jsonMap = new HashMap<String, String>();
	    jsonMap.put("accessToken", session.getSessionToken());
	    jsonMap.put("expiresAt", session.getExpireDate().toString());
	    response.setStatus(HttpServletResponse.SC_OK);
	    this.writeJSON(response, (new JSONObject(jsonMap)).toString());
	    return false;
	}
	return true;
    }

    /**
     * Creates a new {@link SivaPlayerSession}.
     * 
     * @param video
     *            of the session.
     * @param user
     *            of the session.
     * @return true if session could be created, false otherwise.
     * @throws IOException
     */
    private boolean createSession(HttpServletRequest request, HttpServletResponse response,
	    Video video, User user) throws IOException {
	IApiStore apiStore = this.persistenceProvider.getApiStore();
	try {
	    while (this.session == null || this.session.getId() == null) {
		this.session = new SivaPlayerSession(null, SecurityUtils.randomString(70));
		if (request.getParameter("token2") != null
			&& request.getParameter("token2").length() == 40) {
		    String ip = request.getRemoteAddr().replaceAll("\\D+", "");
		    if (request.getHeader("X-Forwarded-For") != null) {
			ip = request.getHeader("X-Forwarded-For").replaceAll("\\D+", "");
		    }
		    this.session.setSecondaryToken(ip + "-" + request.getParameter("token2"));
		}
		if (user != null) {
		    this.session.setUserId(user.getId());
		}
		this.session.setVideoId(video.getId());
		this.session.setVideoVersion(video.getVersion());
		this.session = apiStore.createSivaPlayerSession(this.session);
	    }
	} catch (InconsistencyException e) {
	    this.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
		    "sessionCreationFailedError");
	    return false;
	}

	// Log Client IP address
	/*
	 * ArrayList<SivaPlayerLogEntry> entries = new
	 * ArrayList<SivaPlayerLogEntry>(); SivaPlayerLogEntry entry = new
	 * SivaPlayerLogEntry(); entry.setSessionId(this.session.getId());
	 * entry.setType("getClientInformation"); entry.setElement("ip");
	 * entry.setAdditionalInformation(request.getRemoteAddr());
	 * entry.setTime(new Date()); entries.add(entry); try {
	 * apiStore.createSivaPlayerLogEntries(entries); } catch
	 * (InconsistencyException ignore) { this.sendError(response,
	 * HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "savingError"); return
	 * false; }
	 */
	return true;
    }
    
    /**
     * Provide a player for playing the current video.
     * 
     * @param request
     *            servlet.
     * @param response
     *            servlet.
     * @throws IOException
     */
    private void providePlayer(HttpServletRequest request, HttpServletResponse response)
	    throws IOException {
	
	IVideoStore videoStore = this.persistenceProvider.getVideoStore();
	IUserStore userStore = this.persistenceProvider.getUserStore();

	// Get video from database and show 404 if video does not exist
	String videoDirectory = request.getPathInfo().split("/")[1];
	Video video = videoStore.findByDirectory(videoDirectory);
	if (video == null) {
	    this.sendError(response, HttpServletResponse.SC_NOT_FOUND, "videoNotExistingError");
	    return;
	}
	
	String token = "";
	String externUserId = request.getParameter("externUserId");
	String code = request.getParameter("code");
	String firstName = request.getParameter("firstName");
	String lastName = request.getParameter("lastName");
	if(externUserId != null && !externUserId.equals("") && code != null){
	    String secret = SecurityUtils.hash(video.getParticipationRestriction().toString().toLowerCase() + video.getDirectory()).substring(10, 40);
	    if(code.equals(SecurityUtils.hash(secret + externUserId))){
		User user = userStore.findByExternUserId(externUserId);
		if(user == null){
		    user = new User(null);
		    user.setFirstName(((firstName != null) ? firstName : "External User"));
		    user.setLastName(((lastName != null) ? lastName : "Anoymous"));
		    user.setExternUserId(externUserId);
		    user.setUserType(EUserType.Participant);
		    user.setPassword(SecurityUtils.randomString(20));
		    user.setEmail(secret + externUserId + "@anonymous.org");
		    try {
			user = userStore.create(user);
		    } catch (InconsistencyException e) {
			user = null;
		    }
		}
		if(user != null){
		    	
		    	// generate new SivaPlayerSession and provide session token for direct
			// video access
			token = SecurityUtils.randomString(70);
			SivaPlayerSession session = new SivaPlayerSession(null, token);
			session.setUserId(user.getId());
			session.setVideoId(video.getId());
			session.setVideoVersion(video.getVersion());
			try {
			    session = this.persistenceProvider.getApiStore().createSivaPlayerSession(session);
			    token = session.getSessionToken();
			} catch (InconsistencyException e) {
			    token = null;
			}

			// Log Client IP address
			ArrayList<SivaPlayerLogEntry> entries = new ArrayList<SivaPlayerLogEntry>();
			SivaPlayerLogEntry entry = new SivaPlayerLogEntry();
			entry.setSessionId(session.getId());
			entry.setType("getClientInformation");
			entry.setElement("embeddedAccess");
			/*
			 * entry.setElement("ip");
			 * entry.setAdditionalInformation(((HttpServletRequest
			 * )this.getCurrentFcInstance
			 * ().getExternalContext().getRequest()).getRemoteAddr());
			 */
			entry.setTime(new Date());
			entries.add(entry);
			try {
			    this.persistenceProvider.getApiStore().createSivaPlayerLogEntries(entries);
			} catch (InconsistencyException ignore) {	    
			}
		}
	    }
	}
		
	PrintWriter out = response.getWriter();
	out.print("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"
		+ "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"de\">"
		+ "<head>"
		+ "<title>" + video.getTitle() + "</title>"
		+ "<meta name=\"HandheldFriendly\" content=\"True\" />"
		+ "<meta name=\"MobileOptimized\" content=\"400\" />"
		+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1, maximum-scale=1.0, user-scalable=no\" />"
		+ "<meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\" />"
		+ "<style>body,html{height:100%;margin:0;padding:0;}</style>"
		+ "</head>"
		+ "<body>"
		+ "<div class=\"sivaPlayer\" style=\"display:none;\">"
		+ "<div class=\"sivaPlayer_configuration\">"
		+ "<span class=\"common_log\">true</span>"
		+ "<span class=\"common_useSecretLogin\">false</span>"
		+ "</div>"
		+ "</div>"
		+ "<script src=\"./XML/export.js?token=" + token + "\" type=\"text/javascript\"></script>"
		+ "<script src=\"../../resources/SivaPlayer/js/initSivaPlayer.js?lang=en,de\" type=\"text/javascript\"></script>"
		+ "</body>"
		+ "</html>");
    }

    /**
     * Save logged player data to database.
     * 
     * @param request
     *            servlet.
     * @param response
     *            servlet.
     * @throws IOException
     */
    private void doLogging(HttpServletRequest request, HttpServletResponse response)
	    throws IOException {

	// Check if there is a set of logged data and try to parse it as JSON
	// Array,
	// display error if something goes wrong
	if (request.getParameter("data") == null || request.getParameter("data").equals("")) {
	    this.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "nothingToLogError");
	    return;
	}

	JSONArray json;
	try {
	    json = new JSONArray(request.getParameter("data"));
	} catch (JSONException e) {
	    this.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "malformedDataError");
	    return;
	}

	String videoDirectory = request.getPathInfo().split("/")[1];
	IVideoStore videoStore = this.persistenceProvider.getVideoStore();
	Video video = videoStore.findByDirectory(videoDirectory);
	if (video == null) {
	    this.sendError(response, HttpServletResponse.SC_NOT_FOUND, "videoNotExistingError");
	    return;
	}

	// Create new session if no one exists get related session otherwise
	IApiStore apiStore = this.persistenceProvider.getApiStore();
	if (request.getParameter("token") == null || request.getParameter("token").equals("")
		|| request.getParameter("token").equals("undefined")) {

	    // Check if there is a user specified for logging
	    User user = null;
	    if (request.getParameter("email") != null && !request.getParameter("email").equals("")) {
		user = this.persistenceProvider.getUserStore().findByEmail(
			request.getParameter("email"));
		if (user == null || request.getParameter("secret") == null
			|| !user.getSecretKey().equals(request.getParameter("secret"))) {
		    this.sendError(response, HttpServletResponse.SC_FORBIDDEN,
			    "unknownUserCredentials");
		    return;
		}
	    }

	    // Try to find an existing session for the provided secondary token
	    // to add the logged data to this session, create a new session
	    // otherwise
	    String ip = request.getRemoteAddr().replaceAll("\\D+", "");
	    if (request.getHeader("X-Forwarded-For") != null) {
		ip = request.getHeader("X-Forwarded-For").replaceAll("\\D+", "");
	    }
	    this.session = apiStore.findSivaPlayerSessionBySecondaryToken(ip + "-"
		    + request.getParameter("token2"));
	    if (this.session == null || this.session.getId() == null) {

		// Create new session and write it to database
		if (!this.createSession(request, response, video, user)) {
		    return;
		}
	    }
	} else {

	    // Get related session and show error if session does not exist
	    this.session = ((request.getParameter("token").split("-").length != 2) ? null
		    : apiStore.findSivaPlayerSessionByToken(Integer.parseInt(request.getParameter(
			    "token").split("-")[0]), request.getParameter("token").split("-")[1],
			    true));
	    if (this.session == null || this.session.getId() == null) {
		this.sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "tokenError");
		return;
	    }
	}

	// Write each log entry to database if it has not already been written
	ArrayList<SivaPlayerLogEntry> entries = new ArrayList<SivaPlayerLogEntry>();
	try {
	    for (int i = 0; i < json.length(); i++) {
		JSONObject logEntry = json.getJSONObject(i);
		SivaPlayerLogEntry entry = new SivaPlayerLogEntry();
		entry.setSessionId(this.session.getId());
		entry.setPlayerSequenceId(logEntry.getInt("id"));
		entry.setSceneTimeOffset((float) logEntry.getDouble("sceneOffset"));
		entry.setTime(new Date(this.session.getStart().getTime()
			+ logEntry.getLong("timeOffset")));
		entry.setType(logEntry.getString("type"));
		entry.setElement(logEntry.getString("element"));
		entry.setAdditionalInformation(logEntry.getString("extraInfo"));
		entry.setClientTime(logEntry.getLong("clientTime"));
		entries.add(entry);
	    }
	} catch (JSONException e) {
	    this.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "malformedDataError");
	    return;
	} catch (NullPointerException e) {
	    e.printStackTrace();
	    this.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "savingError");
	    return;
	}

	try {
	    apiStore.createSivaPlayerLogEntries(entries);
	} catch (InconsistencyException e) {
	    this.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "savingError");
	    return;
	}

	// Return positive JSON output as result of the logging
	response.setStatus(HttpServletResponse.SC_OK);
	response.setContentType("application/json");
	response.setCharacterEncoding("utf-8");

	Map<String, String> jsonMap = new HashMap<String, String>();
	jsonMap.put("logged", "true");
	// if(video.getId() == 1 || video.getId() == 2){
	// jsonMap.put("disabledTitle",
	// "Deaktivierung der aktuellen Beckenbodentrainerversion");
	// jsonMap.put("disabledText",
	// "Es ist eine neue Version des Beckenbodentrainers verfügbar. Diese Testversion wurde aus diesem Grund deaktiviert. Bitte laden Sie die neue Version hier herunter: <br /><a href=\"http://www.klinik-prof-schedel.de/bb\">http://www.klinik-prof-schedel.de/bb</a><br /><br /><b>Bitte beachten Sie,</b> dass Sie für die Nutzung der neuen Version einen Lizenschlüssel benötigen. Diesen erhalten Sie auf Anfrage von der Klinik Prof. Schedel.");
	// }
	this.isAJAXRequest = true;
	this.writeJSON(response, (new JSONObject(jsonMap)).toString());
    }

    /**
     * Return available stats for user.
     * 
     * @param request
     *            servlet.
     * @param response
     *            servlet.
     * @throws IOException
     */
    private void getStats(HttpServletRequest request, HttpServletResponse response)
	    throws IOException {

	// Check if there is a user specified for getting the stats and abort if
	// not
	User user = null;
	IApiStore apiStore = this.persistenceProvider.getApiStore();
	if (request.getParameter("token") != null && !request.getParameter("token").equals("")
		&& !request.getParameter("token").equals("undefined")) {
	    SivaPlayerSession session = apiStore.findSivaPlayerSessionByToken(Integer
		    .parseInt(request.getParameter("token").split("-")[0]),
		    request.getParameter("token").split("-")[1], true);
	    if (session.getUserId() == null) {
		this.sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "loginRequiredTitle");
		return;
	    }
	    user = new User(session.getUserId());
	} else if (request.getParameter("email") == null
		&& !request.getParameter("email").equals("")) {
	    this.sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "loginRequiredTitle");
	    return;
	} else {
	    user = this.persistenceProvider.getUserStore().findByEmail(
		    request.getParameter("email"));
	    if (user == null || request.getParameter("secret") == null
		    || !user.getSecretKey().equals(request.getParameter("secret"))) {
		this.sendError(response, HttpServletResponse.SC_FORBIDDEN, "unknownUserCredentials");
		return;
	    }
	}

	// Get stats and write them to HTTP response
	HashMap<String, Integer> stats = apiStore.getSivaPlayerSessionDurationByDay(user.getId());
	if (stats.isEmpty()) {
	    this.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "notSufficentData");
	} else {
	    this.isAJAXRequest = true;
	    this.writeJSON(response, (new JSONObject(stats).toString()));
	}
    }

    /**
     * Returns true if the given accept header accepts the given value.
     * 
     * @param acceptHeader
     *            The accept header.
     * @param toAccept
     *            The value to be accepted.
     * @return True if the given accept header accepts the given value.
     */
    private static boolean accepts(String acceptHeader, String toAccept) {
	String[] acceptValues = acceptHeader.split("\\s*(,|;)\\s*");
	Arrays.sort(acceptValues);
	return Arrays.binarySearch(acceptValues, toAccept) > -1
		|| Arrays.binarySearch(acceptValues, toAccept.replaceAll("/.*$", "/*")) > -1
		|| Arrays.binarySearch(acceptValues, "*/*") > -1;
    }

    /**
     * Returns true if the given match header matches the given value.
     * 
     * @param matchHeader
     *            The match header.
     * @param toMatch
     *            The value to be matched.
     * @return True if the given match header matches the given value.
     */
    private static boolean matches(String matchHeader, String toMatch) {
	String[] matchValues = matchHeader.split("\\s*,\\s*");
	Arrays.sort(matchValues);
	return Arrays.binarySearch(matchValues, toMatch) > -1
		|| Arrays.binarySearch(matchValues, "*") > -1;
    }

    /**
     * Returns a substring of the given string value from the given begin index
     * to the given end index as a long. If the substring is empty, then -1 will
     * be returned
     * 
     * @param value
     *            The string value to return a substring as long for.
     * @param beginIndex
     *            The begin index of the substring to be returned as long.
     * @param endIndex
     *            The end index of the substring to be returned as long.
     * @return A substring of the given string value as long or -1 if substring
     *         is empty.
     */
    private static long sublong(String value, int beginIndex, int endIndex) {
	String substring = value.substring(beginIndex, endIndex);
	return (substring.length() > 0) ? Long.parseLong(substring) : -1;
    }

    /**
     * Copy the given byte range of the given input to the given output.
     * 
     * @param input
     *            The input to copy the given range to the given output for.
     * @param output
     *            The output to copy the given range from the given input for.
     * @param start
     *            Start of the byte range.
     * @param length
     *            Length of the byte range.
     * @throws IOException
     *             If something fails at I/O level.
     */
    private static void copy(RandomAccessFile input, OutputStream output, long start, long length)
	    throws IOException {
	byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
	int read;

	if (input.length() == length) {
	    // Write full range.
	    while ((read = input.read(buffer)) > 0) {
		output.write(buffer, 0, read);
	    }
	} else {
	    // Write partial range.
	    input.seek(start);
	    long toRead = length;

	    while ((read = input.read(buffer)) > 0) {
		if ((toRead -= read) > 0) {
		    output.write(buffer, 0, read);
		} else {
		    output.write(buffer, 0, (int) toRead + read);
		    break;
		}
	    }
	}
    }

    /**
     * Get a message from the language file by using the key of the pair of
     * values.
     * 
     * @param key
     *            to which the corresponding message should be fetched.
     * @return the message that is defined in the language file for a certain
     *         key.
     */
    private String getCommonMessage(String key) {
	ResourceBundle bundle = ResourceBundle.getBundle("hu.configuration.CommonMessages",
		FacesContext.getCurrentInstance().getViewRoot().getLocale());

	return bundle.getString(key);
    }

    /**
     * Close closable resource.
     * 
     * @param resource
     *            to close.
     */
    private static void close(Closeable resource) {

	// Check if specified file is not null and try to close it if so
	if (resource != null) {
	    try {
		resource.close();
	    } catch (IOException ignore) {
		// ignore
	    }
	}
    }

    /**
     * Write JSON output to HTTP response.
     * 
     * @param response
     *            servlet.
     * @param output
     *            to write to HTTP response.
     * @throws IOException
     */
    private void writeJSON(HttpServletResponse response, String output) throws IOException {
	response.setContentType("application/json");
	response.setCharacterEncoding("utf-8");
	if (!this.isAJAXRequest) {
	    response.getWriter()
		    .write("if(!sivaVideoConfiguration){var sivaVideoConfiguration=[];};sivaVideoConfiguration.push({\"configPath\": document.getElementsByTagName('script')[document.getElementsByTagName('script').length - 1].src, \"accessRestriction\": ");
	}
	response.getWriter().write(output);
	if (!this.isAJAXRequest) {
	    response.getWriter().write("});");
	}
	response.getWriter().flush();
	response.getWriter().close();
    }

    @Override
    protected void sendError(HttpServletResponse response, Integer errorCode,
	    String errorDescription) throws IOException {
	this.sendError(response, errorCode, errorDescription, null);
    }

    @Override
    protected void sendError(HttpServletResponse response, Integer errorCode,
	    String errorDescription, String additionalInformation) throws IOException {
	String message;
	if (errorCode == HttpServletResponse.SC_BAD_REQUEST) {
	    message = "Bad request";
	} else if (errorCode == HttpServletResponse.SC_UNAUTHORIZED) {
	    message = "Unauthorized";
	} else if (errorCode == HttpServletResponse.SC_FORBIDDEN) {
	    message = "Forbidden";
	} else if (errorCode == HttpServletResponse.SC_NOT_FOUND) {
	    message = "Not Found";
	} else {
	    message = "Uknown Error";
	}

	// Create log entry if a session exists
	if (this.session != null && this.session.getId() != null) {
	    ArrayList<SivaPlayerLogEntry> logEntries = new ArrayList<SivaPlayerLogEntry>();
	    SivaPlayerLogEntry logEntry = new SivaPlayerLogEntry();
	    logEntry.setSessionId(session.getId());
	    logEntry.setType("HTTPError");
	    logEntry.setElement(message);
	    logEntry.setAdditionalInformation(errorDescription);
	    logEntry.setTime(new Date());
	    logEntries.add(logEntry);
	    try {
		this.persistenceProvider.getApiStore().createSivaPlayerLogEntries(logEntries);
	    } catch (InconsistencyException ignore) {
		// ignore
	    }
	}

	Map<String, String> jsonMap = new HashMap<String, String>();
	jsonMap.put("code", errorCode.toString());
	jsonMap.put("message", message);
	jsonMap.put("description", errorDescription);
	if (additionalInformation != null) {
	    jsonMap.put("additionalInformation", additionalInformation);
	}

	this.writeJSON(response, (new JSONObject(jsonMap)).toString());
    }

    /**
     * This class represents a byte range.
     */
    protected class Range {
	long start;
	long end;
	long length;
	long total;

	/**
	 * Construct a byte range.
	 * 
	 * @param start
	 *            Start of the byte range.
	 * @param end
	 *            End of the byte range.
	 * @param total
	 *            Total length of the byte source.
	 */
	public Range(long start, long end, long total) {
	    this.start = start;
	    this.end = end;
	    this.length = end - start + 1;
	    this.total = total;
	}
    }

    protected FacesContext getFacesContext(HttpServletRequest request, HttpServletResponse response) {
	FacesContext facesContext = FacesContext.getCurrentInstance();
	if (facesContext == null) {

	    FacesContextFactory contextFactory = (FacesContextFactory) FactoryFinder
		    .getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
	    LifecycleFactory lifecycleFactory = (LifecycleFactory) FactoryFinder
		    .getFactory(FactoryFinder.LIFECYCLE_FACTORY);
	    Lifecycle lifecycle = lifecycleFactory.getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);

	    facesContext = contextFactory.getFacesContext(request.getSession().getServletContext(),
		    request, response, lifecycle);

	    // Set using our inner class
	    InnerFacesContext.setFacesContextAsCurrentInstance(facesContext);

	    // set a new viewRoot, otherwise context.getViewRoot returns null
	    UIViewRoot view = facesContext.getApplication().getViewHandler()
		    .createView(facesContext, "");
	    facesContext.setViewRoot(view);
	}
	return facesContext;
    }

    public void removeFacesContext() {
	InnerFacesContext.setFacesContextAsCurrentInstance(null);
    }

    protected Application getApplication(FacesContext facesContext) {
	return facesContext.getApplication();
    }

    @SuppressWarnings("deprecation")
    protected Object getManagedBean(String beanName, FacesContext facesContext) {
	return getApplication(facesContext).getVariableResolver().resolveVariable(facesContext,
		beanName);
    }

    private abstract static class InnerFacesContext extends FacesContext {
	protected static void setFacesContextAsCurrentInstance(FacesContext facesContext) {
	    FacesContext.setCurrentInstance(facesContext);
	}
    }
}