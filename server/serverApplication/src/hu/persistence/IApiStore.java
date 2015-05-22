package hu.persistence;

import hu.model.api.Client;
import hu.model.api.OauthSession;
import hu.model.api.SivaPlayerLogEntry;
import hu.model.api.SivaPlayerSession;

import java.util.ArrayList;
import java.util.HashMap;

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
}