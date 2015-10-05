package hu.persistence;

import hu.model.api.Client;
import hu.model.api.CollaborationMedia;
import hu.model.api.CollaborationPost;
import hu.model.api.CollaborationThread;
import hu.model.api.OauthSession;
import hu.model.api.SivaPlayerLogEntry;
import hu.model.api.SivaPlayerSession;
import hu.model.users.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This interface describes how API elements are stored in and retrieved from
 * the persistence layer.
 */
public interface IApiStore {

	/**
	 * Retrieve the {@link Client} with given credentials.
	 * 
	 * @param name
	 *            of the {@link Client} to fetch.
	 * @param passwort
	 *            of the {@link Client} to fetch.
	 * @return the {@link Client} with the specified id.
	 */
	public Client findClientByNamenAndPassword(final String name,
			final String secret);
	
	/**
	 * Create a new {@link OauthSession} for the current client.
	 * 
	 * @param session
	 *           to create.
	 * @return the {@link OauthSession} with its expire date.
	 */
	public OauthSession createOauthSession(final OauthSession session) throws InconsistencyException ;

	/**
	 * Delete all expired oauth sessions.
	 */
	public void deleteExpiredOauthSessions();
	
	/**
	 * Retrieve the {@link OauthSession} with the given token.
	 * 
	 * @param token
	 *            of the {@link OauthSession} to fetch.
	 * @return the {@link OauthSession} with the specified token.
	 */
	public OauthSession findOauthSessionByToken(final String token);
	
	/**
	 * Create a new {@link SivaPlayerSession}.
	 * 
	 * @param session
	 *           to create.
	 * @return the {@link SivaPlayerSession} with its expire date.
	 */
	public SivaPlayerSession createSivaPlayerSession(final SivaPlayerSession session) throws InconsistencyException ;
	
	/**
	 * Retrieve the {@link SivaPlayerSession} with the given id and token.
	 * 
	 * @param id of the {@link SivaPlayerSession} to fetch
	 * @param token
	 *            of the {@link SivaPlayerSession} to fetch.
	 * @param canBeExpired true if the {@link SivaPlayerSession} can already be expired.
	 * @return the {@link SivaPlayerSession} with the specified id and token.
	 */
	public SivaPlayerSession findSivaPlayerSessionByToken(final int id, final String token, final boolean canBeExpired);
	
	/**
	 * Retrieve the {@link SivaPlayerSession} with the given secondary token. Returns both
	 * expired and not expired sessions. Session end date is always NULL.
	 * 
	 * @param secondaryToken
	 *            of the {@link SivaPlayerSession} to fetch.
	 * @return the {@link SivaPlayerSession} with the specified secondary token.
	 */
	public SivaPlayerSession findSivaPlayerSessionBySecondaryToken(final String secondaryToken);
	
	/**
	 * Creates new {@link SivaPlayerLogEntry}s
	 * 
	 * @param logEntries to create
	 */
	public void createSivaPlayerLogEntries(final ArrayList<SivaPlayerLogEntry> entries) throws InconsistencyException;
	
	/**
	 * Check if the given sequence id from the player exists in the specified {@link SivaPlayerSession}.
	 * 
	 * @param id of the {@link SivaPlayerSession} to fetch
	 * @param playerSequenceId of the player.
	 * @return true if a log entry with this playerSequenceId already exists, false otherwise. 
	 * 		For the playerSequenceId -1 it returns always false as this id is used for server side logging.
	 */
	public boolean existsLogEntryPlayerSequenceId(final int sessionId, final int playerSequenceId);

	/**
	 * Retrieve the daily usage duration for a given user in minutes.
	 * 
	 * @param userId
	 *            of the {@link User} the data should be fetched.
	 * @return a {@link HashMap} containing the dates and the amounts.
	 */
	public HashMap<String, Integer> getSivaPlayerSessionDurationByDay(Integer userId);
	
	/**
	 * Create a new {@link CollaborationThread}.
	 * 
	 * @param thread
	 *           to create.
	 * @return the created {@link CollaborationThread}.
	 */
	public CollaborationThread createCollaborationThread(final CollaborationThread thread) throws InconsistencyException;
	
        /**
         * Delete the {@link CollaborationThread} and all related
         * {@link CollaborationPost}s and {@link CollaborationMedia} elements.
         * 
         * @param id
         *            to be deleted.
         */
        public void deleteCollaborationThread(final int id) throws InconsistencyException;
	
	/**
	 * Retrieve the {@link CollaborationThread} with the given id.
	 * 
	 * @param id of the {@link CollaborationThread} to fetch
	 * @return the {@link CollaborationThread} with the specified id.
	 */
	public CollaborationThread findCollaborationThreadById(final int id);
	
	/**
	 * Retrieve all {@link CollaborationThread}s for the given video and scene.
	 * 
	 * @param videoId of the {@link CollaborationThread}s to fetch
	 * @param scene of the {@link CollaborationThread}s to fetch
	 * @return the {@link CollaborationThread}s.
	 */
	public List<CollaborationThread> listCollaborationThreads(final int videoId, final String scene);
	
	/**
	 * Create a new {@link CollaborationPost}.
	 * 
	 * @param post
	 *           to create.
	 * @return the created {@link CollaborationPost}.
	 */
	public CollaborationPost createCollaborationPost(final CollaborationPost post) throws InconsistencyException;
	
	/**
	 * Update a {@link CollaborationPost}.
	 * 
	 * @param post
	 *           to update
	 * @return updated {@link CollaborationPost}.
	 */
	public CollaborationPost saveCollaborationPost(final CollaborationPost post) throws InconsistencyException;
	
	/**
         * Delete the {@link CollaborationPost} and all related
         * {@link CollaborationMedia} elements.
         * 
         * @param id
         *            to be deleted.
         */
        public void deleteCollaborationPost(final int id) throws InconsistencyException;
	
	/**
	 * Retrieve the {@link CollaborationPost} with the given id.
	 * 
	 * @param id of the {@link CollaborationPost} to fetch
	 * @return the {@link CollaborationPost} with the specified id.
	 */
	public CollaborationPost findCollaborationPostById(final int id);
	
	/**
	 * Retrieve all {@link CollaborationPost}s for the given thread id.
	 * 
	 * @param threadId of the {@link CollaborationPost}s to fetch
	 * @return the {@link CollaborationPost}s.
	 */
	public List<CollaborationPost> listCollaborationPosts(final int threadId);
	
	/**
	 * Create a new {@link CollaborationMedia}.
	 * 
	 * @param media
	 *           to create.
	 * @return the created {@link CollaborationMedia}.
	 */
	public CollaborationMedia createCollaborationMedia(final CollaborationMedia media) throws InconsistencyException;
	
	/**
	 * Retrieve the {@link CollaborationMedia} with the given id.
	 * 
	 * @param id of the {@link CollaborationMedia} to fetch
	 * @return the {@link CollaborationMedia} with the specified id.
	 */
	public CollaborationMedia findCollaborationMediaById(final int id);
	
	/**
	 * Retrieve all {@link CollaborationMedia}s for the given post id.
	 * 
	 * @param threadId of the {@link CollaborationMedia}s to fetch
	 * @return the {@link CollaborationMedia}s.
	 */
	public List<CollaborationMedia> listCollaborationMedia(final int postId);
}