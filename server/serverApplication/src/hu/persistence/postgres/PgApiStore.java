package hu.persistence.postgres;

import hu.model.api.Client;
import hu.model.api.OauthSession;
import hu.model.api.SivaPlayerLogEntry;
import hu.model.api.SivaPlayerSession;
import hu.persistence.IApiStore;
import hu.persistence.InconsistencyException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

/**
 * This class implements a {@link IApiStore} using a PostgreSQL backend. 
 */
public class PgApiStore implements IApiStore {

    private static final String SELECT_CLIENT_BY_NAME_AND_SECRET = "SELECT * FROM \"client\" WHERE \"name\" = ? AND \"secret\" = ?";
    private static final String DELETE_EXPIRED_OAUTH_SESSIONS = "DELETE FROM \"oauthSession\" WHERE \"expireDate\" < NOW()";
    private static final String SELECT_OAUTH_SESSION_BY_TOKEN = "SELECT * FROM \"oauthSession\" WHERE \"token\" = ? AND \"expireDate\" >= NOW()";
    private static final String INSERT_OAUTH_SESSION = "INSERT INTO \"oauthSession\" (\"token\", \"client\", \"user\", \"scope\", \"expireDate\") VALUES (?, ?, ?, ?, NOW() + INTERVAL '1 YEAR')";
    private static final String SELECT_SIVA_PLAYER_SESSION_BY_TOKEN = "SELECT *, (SELECT \"time\" FROM \"sivaPlayerLog\" WHERE \"session\" = ? ORDER BY \"time\" DESC OFFSET 0 LIMIT 1) AS \"end\" FROM \"sivaPlayerSession\" WHERE \"id\" = ? AND \"token\" = ? AND \"expireDate\" >= NOW()";
    private static final String SELECT_SIVA_PLAYER_SESSION_BY_TOKEN_ALSO_EXPIRED = "SELECT *, (SELECT \"time\" FROM \"sivaPlayerLog\" WHERE \"session\" = ? ORDER BY \"time\" DESC OFFSET 0 LIMIT 1) AS \"end\" FROM \"sivaPlayerSession\" WHERE \"id\" = ? AND \"token\" = ?";
    private static final String SELECT_SIVA_PLAYER_SESSION_BY_SECONDARY_TOKEN = "SELECT *, NULL as \"end\" FROM \"sivaPlayerSession\" WHERE \"secondaryToken\" = ?";
    private static final String INSERT_SIVA_PLAYER_SESSION = "INSERT INTO \"sivaPlayerSession\" (\"token\", \"secondaryToken\", \"user\", \"video\", \"videoVersion\", \"expireDate\") VALUES (?, ?, ?, ?, ?, NOW() + INTERVAL '1 DAY')";
    private static final String INSERT_SIVA_PLAYER_LOG_ENTRIES = "INSERT INTO \"sivaPlayerLog\" (\"session\", \"time\", \"sceneTimeOffset\", \"type\", \"element\", \"additionalInformation\", \"playerSequenceId\", \"clientTime\") VALUES";
    private static final String INSERT_SIVA_PLAYER_LOG_ENTRY = "(?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String DELETE_DUPLICATE_SIVA_PLAYER_LOG_ENTRIES = "DELETE FROM \"sivaPlayerLog\" USING \"sivaPlayerLog\" l2 WHERE \"sivaPlayerLog\".\"session\" = ? AND \"sivaPlayerLog\".\"session\" = l2.\"session\" AND \"sivaPlayerLog\".\"playerSequenceId\" = l2.\"playerSequenceId\" AND \"sivaPlayerLog\".\"id\" > l2.\"id\"";
    private static final String COUNT_SIVA_PLAYER_LOG_ENTRY_WITH_PLAYER_SEQUENCE_ID = "SELECT count(\"id\") as num FROM \"sivaPlayerLog\" WHERE \"session\" = ? AND \"playerSequenceId\" = ?";
    private static final String SELECT_SIVA_PLAYER_SESSION_DURATION_BY_DAY_AND_USER = "SELECT * FROM \"sivaPlayerSessionDurationByDayAndUser\" WHERE \"user\" = ?";
    
    private PgConnectionPool pool;

    /**
     * Initialize a new PgGroupStore.
     * 
     * @param pool
     *            for database access.
     */
    PgApiStore(PgConnectionPool pool) {
        this.pool = pool;
    }

    private Client deserializeClient(ResultSet client) throws SQLException {
        Client result = new Client(client.getInt("id"));
        result.setName(client.getString("name"));
        result.setSecret(client.getString("secret"));
        return result;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Client findClientByNamenAndPassword(final String name, final String secret){
    	final Client[] client = new Client[1];

        new SQLRunner(this.pool) {
            @Override
            public void run() throws SQLException {
                stmt = conn.prepareStatement(SELECT_CLIENT_BY_NAME_AND_SECRET);
                stmt.setString(1, name);
                stmt.setString(2, secret);
                ResultSet rset = stmt.executeQuery();
                if (rset.next()) {
                    client[0] = deserializeClient(rset);
                }
            }
        }.execute();

        return client[0];
    }
    
    private OauthSession deserializeOauthSession(ResultSet session) throws SQLException {
    	OauthSession result = new OauthSession(session.getString("token"));
    	result.setClientId(session.getInt("client"));
    	result.setUserId(session.getInt("user"));
        result.setScope(session.getString("scope"));
        
        // Just return date as the session's expire date is limited to days
        result.setExpireDate(session.getDate("expireDate"));
        return result;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public OauthSession createOauthSession(final OauthSession session)  throws InconsistencyException {
    	new SQLRunner(this.pool) {
            @Override
            public void runI() throws SQLException {

                // Insert new video
                stmt = conn.prepareStatement(INSERT_OAUTH_SESSION);
                stmt.setString(1, session.getToken());
                stmt.setInt(2, session.getClientId());
                stmt.setInt(3, session.getUserId());
                stmt.setString(4, session.getScope());
                stmt.executeUpdate();

                stmt.close();
            }
        }.executeTI();
        
        OauthSession newSession = this.findOauthSessionByToken(session.getToken());
        return newSession;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteExpiredOauthSessions(){
    	new SQLRunner(this.pool) {
            @Override
            public void run() throws SQLException {
                stmt = conn.prepareStatement(DELETE_EXPIRED_OAUTH_SESSIONS);
                stmt.executeUpdate();
            }
        }.execute();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public OauthSession findOauthSessionByToken(final String token){
    	final OauthSession[] session = new OauthSession[1];
    	
    	new SQLRunner(this.pool) {
            @Override
            public void run() throws SQLException {
                stmt = conn.prepareStatement(SELECT_OAUTH_SESSION_BY_TOKEN);
                stmt.setString(1, token);
                ResultSet rset = stmt.executeQuery();
                if (rset.next()) {
                	session[0] = deserializeOauthSession(rset);
                }
            }
        }.execute();
    	
    	return session[0];
    }
    
    private SivaPlayerSession deserializeSivaPlayerSession(ResultSet session) throws SQLException {
    	SivaPlayerSession result = new SivaPlayerSession(session.getInt("id"), session.getString("token"));
    	result.setSecondaryToken(session.getString("secondaryToken"));
    	result.setUserId(session.getInt("user"));
    	result.setVideoId(session.getInt("video"));
    	result.setVideoVersion(session.getInt("videoVersion"));
    	result.setStart(new Date(session.getTimestamp("start").getTime()));
    	if(session.getTimestamp("end") == null){
    		result.setEnd(new Date(session.getTimestamp("start").getTime()));
    	}
    	else{
    		result.setEnd(new Date(session.getTimestamp("end").getTime()));
    	}
        
        // Just return date as the session's expire date is limited to days
        result.setExpireDate(session.getDate("expireDate"));
        return result;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public SivaPlayerSession createSivaPlayerSession(final SivaPlayerSession session)  throws InconsistencyException {
    	final Integer[] sessionId = new Integer[1];
    	new SQLRunner(this.pool) {
            @Override
            public void runI() throws SQLException {

                // Insert new video
                stmt = conn.prepareStatement(INSERT_SIVA_PLAYER_SESSION,
                        Statement.RETURN_GENERATED_KEYS);
                stmt.setString(1, session.getToken());
                if(session.getSecondaryToken() != null){
            		stmt.setString(2, session.getSecondaryToken());
                }
                else{
            		stmt.setNull(2, Types.VARCHAR);
                }
                if(session.getUserId() != null){
                	stmt.setInt(3, session.getUserId());
                }
                else{
                	stmt.setNull(3, Types.INTEGER);
                }
                stmt.setInt(4, session.getVideoId());
                stmt.setInt(5, session.getVideoVersion());
                stmt.executeUpdate();

                ResultSet rset = stmt.getGeneratedKeys();
                if (rset.next()) {
                	sessionId[0] = rset.getInt(1);
                }
                rset.close();
                stmt.close();
            }
        }.executeTI();
        
        SivaPlayerSession newSession = this.findSivaPlayerSessionByToken(sessionId[0], session.getToken(), false);
        return newSession;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public SivaPlayerSession findSivaPlayerSessionByToken(final int id, final String token, final boolean canBeExpired){
    	final SivaPlayerSession[] session = new SivaPlayerSession[1];
    	
    	new SQLRunner(this.pool) {
            @Override
            public void run() throws SQLException {
                if(canBeExpired){
                	stmt = conn.prepareStatement(SELECT_SIVA_PLAYER_SESSION_BY_TOKEN_ALSO_EXPIRED);
                }
                else{
                	stmt = conn.prepareStatement(SELECT_SIVA_PLAYER_SESSION_BY_TOKEN);
                }
                stmt.setInt(1, id);
                stmt.setInt(2, id);
                stmt.setString(3, token);
                ResultSet rset = stmt.executeQuery();
                if (rset.next()) {
                	session[0] = deserializeSivaPlayerSession(rset);
                }
            }
        }.execute();
    	
    	return session[0];
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public SivaPlayerSession findSivaPlayerSessionBySecondaryToken(final String secondaryToken){
    	final SivaPlayerSession[] session = new SivaPlayerSession[1];
    	
    	new SQLRunner(this.pool) {
            @Override
            public void run() throws SQLException {
                stmt = conn.prepareStatement(SELECT_SIVA_PLAYER_SESSION_BY_SECONDARY_TOKEN);
                stmt.setString(1, secondaryToken);
                ResultSet rset = stmt.executeQuery();
                if (rset.next()) {
                	session[0] = deserializeSivaPlayerSession(rset);
                }
            }
        }.execute();
    	
    	return session[0];
    }
    
    /**
     * {@inheritDoc}
     * @throws InconsistencyException 
     */
    @Override
    public void createSivaPlayerLogEntries(final ArrayList<SivaPlayerLogEntry> entries) throws InconsistencyException{
    	new SQLRunner(this.pool) {
            @Override
            public void runI() throws SQLException {

        	int i = 0;
        	StringBuilder query = new StringBuilder(INSERT_SIVA_PLAYER_LOG_ENTRIES);
        	for(Iterator<SivaPlayerLogEntry> it = entries.iterator(); it.hasNext(); ){
        	    it.next();
        	    if(i > 0){
        		query.append(",");
        	    }
        	    query.append(" ");
        	    query.append(INSERT_SIVA_PLAYER_LOG_ENTRY);
        	    i++;
        	}
        	
                // Insert new entries
                stmt = conn.prepareStatement(query.toString());
                
                i = 0;
                for(Iterator<SivaPlayerLogEntry> it = entries.iterator(); it.hasNext(); ){
                    SivaPlayerLogEntry entry = it.next();
                    stmt.setInt(i + 1, entry.getSessionId());
                    stmt.setTimestamp(i + 2, new java.sql.Timestamp(entry.getTime().getTime()));
                    stmt.setFloat(i + 3, entry.getSceneTimeOffset());
                    stmt.setString(i + 4, entry.getType());
                    stmt.setString(i + 5, entry.getElement());
                    stmt.setString(i + 6, entry.getAdditionalInformation());
                    stmt.setInt(i + 7, entry.getPlayerSequenceId());
                    stmt.setLong(i + 8, entry.getClientTime());
        	    i += 8;
        	}
                stmt.executeUpdate();
                stmt.close();
                
                stmt = conn.prepareStatement(DELETE_DUPLICATE_SIVA_PLAYER_LOG_ENTRIES);
                stmt.setInt(1, entries.get(0).getSessionId());
                stmt.executeUpdate();
            }
        }.executeTI();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsLogEntryPlayerSequenceId(final int sessionId, final int playerSequenceId){
    	final boolean[] result = new boolean[1];
    	
    	if(playerSequenceId < 0){
    		return true;
    	}
    	
    	new SQLRunner(this.pool) {
            @Override
            public void run() throws SQLException {
            	stmt = conn.prepareStatement(COUNT_SIVA_PLAYER_LOG_ENTRY_WITH_PLAYER_SEQUENCE_ID);
                stmt.setInt(1, sessionId);
                stmt.setInt(2, playerSequenceId);
                ResultSet rset = stmt.executeQuery();
                if (rset.next()) {
                	result[0] = (rset.getInt("num") > 0);
                }
            }
        }.execute();
    	
    	return result[0];
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public HashMap<String, Integer> getSivaPlayerSessionDurationByDay(final Integer userId){
    	final HashMap<String, Integer> durations = new HashMap<String, Integer>();
    	
    	new SQLRunner(this.pool) {
            @Override
            public void run() throws SQLException {
                stmt = conn.prepareStatement(SELECT_SIVA_PLAYER_SESSION_DURATION_BY_DAY_AND_USER);
                stmt.setInt(1, userId);
                ResultSet rset = stmt.executeQuery();
                while(rset.next()) {
                    String date = rset.getString("day");
                    Integer minutes = rset.getInt("duration") / 1000 / 60;
                durations.put(date, minutes);
                }
            }
        }.execute();
    	
    	return durations;
    }
}