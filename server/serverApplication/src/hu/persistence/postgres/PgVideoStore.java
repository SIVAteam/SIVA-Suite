package hu.persistence.postgres;

import hu.model.EParticipationRestriction;
import hu.model.EVideoType;
import hu.model.ESortColumnVideo;
import hu.model.ESortDirection;
import hu.model.Group;
import hu.model.Video;
import hu.model.Token;
import hu.model.users.EUserType;
import hu.model.users.User;
import hu.persistence.InconsistencyException;
import hu.persistence.NotFoundException;
import hu.persistence.IVideoStore;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * This class implements a {@link IVideoStore} using a PostgreSQL
 * backend.
 */
public class PgVideoStore implements IVideoStore {

    private static final String NOT_FOUND_Q_BY_TOKEN = "Video for token %s does not exist.";
    private static final String NOT_FOUND_VIDEO = "Video with id %d does not exist.";
    private static final String SELECT_BY_ID = "SELECT * FROM \"video\" WHERE \"id\" = ?";
    private static final String SAVE_VIDEO = "UPDATE \"video\" SET \"title\" = ?, \"description\" = ?, \"start\" = ?, \"stop\" = ?, \"participationRestriction\" = ?::\"enumVideoRestriction\", \"password\" = ?, \"size\" = ?, \"zipDownloadEnabled\" = ?, \"chromeAppURL\" = ? WHERE \"id\" = ?";
    private static final String INSERT_VIDEO = "INSERT INTO \"video\" (\"group\", \"title\", \"description\", \"start\", \"stop\", \"participationRestriction\", \"password\", \"directory\", \"author\") VALUES (?, ?, ?, ?, ?, ?::\"enumVideoRestriction\", ?, ?, ?);";
    private static final String UPDATE_GROUP_VIDEO = "UPDATE \"video\" SET \"group\" = ? WHERE \"id\" = ?";
    private static final String INCREASE_VERSION = "UPDATE \"video\" SET \"version\" = \"version\" + 1, \"lastUpdated\" = NOW() WHERE \"id\" = ?";
    private static final String INCREASE_VIEWS = "UPDATE \"video\" SET \"views\" = \"views\" + 1 WHERE \"id\" = ?";
    private static final String SELECT_ALL = "SELECT q.* FROM \"video\" q, \"group\" e WHERE q.\"group\" = e.\"id\"";
    private static final String SELECT_ALL_BY_GROUP = "SELECT q.* FROM \"video\" q, \"group\" e WHERE q.\"group\" = e.\"id\" AND q.\"group\" = ?";
    private static final String SELECT_ALL_FOR_USER = "SELECT q.* FROM \"video\" q, \"group\" e, \"userToGroup\" t WHERE q.\"group\" = e.\"id\" AND e.\"id\" = t.\"group\"";
    private static final String SELECT_ALL_BY_GROUP_FOR_USER = "SELECT q.* FROM \"video\" q, \"group\" e, \"userToGroup\" t WHERE q.\"group\" = e.\"id\" AND e.\"id\" = t.\"group\" AND q.\"group\" = ?";
    private static final String SELECT_ALL_FOR_GROUP = "SELECT q.* FROM \"video\" q WHERE q.\"group\" = ?";
    private static final String SELECT_ALL_BY_TOKEN = "SELECT q.* FROM \"video\" q, \"token\" t WHERE t.\"video\" = q.\"id\" AND t.\"token\" = ?";
    private static final String SELECT_BY_DIRECTORY = "SELECT q.* FROM \"video\" q WHERE q.\"directory\" = ?";
    private static final String SELECT_FOR_API_BASIC = "SELECT q.* FROM \"video\" q WHERE q.\"start\" is not null AND q.\"start\" <= NOW() AND (q.\"stop\" is null OR q.\"stop\" > NOW()) AND q.size > 0 AND (q.\"ratingPoints\" / (CASE q.\"ratings\" WHEN 0 THEN 1 ELSE q.\"ratings\" END) >= ?) AND q.\"views\" >= ? AND q.\"size\" <= ?";
    private static final String SELECT_FOR_API_BASIC_FOR_USER = "SELECT q.* FROM \"video\" q, \"group\" e, \"userToGroup\" t WHERE q.\"group\" = e.\"id\" AND e.\"id\" = t.\"group\" AND q.\"start\" is not null AND q.\"start\" <= NOW() AND (q.\"stop\" is null OR q.\"stop\" > NOW()) AND q.size > 0 AND (q.\"ratingPoints\" / (CASE q.\"ratings\" WHEN 0 THEN 1 ELSE q.\"ratings\" END) >= ?) AND q.\"views\" >= ? AND q.\"size\" <= ?";
    private static final String SELECT_FOR_API_WITH_AUTHOR = "SELECT q.* FROM \"video\" q, \"user\" u WHERE q.\"start\" is not null AND q.\"start\" <= NOW() AND (q.\"stop\" is null OR q.\"stop\" > NOW()) AND q.size > 0 AND (q.\"ratingPoints\" / (CASE q.\"ratings\" WHEN 0 THEN 1 ELSE q.\"ratings\" END) >= ?) AND q.\"views\" >= ? AND q.\"size\" <= ? AND q.\"author\" = u.\"id\" AND u.\"email\" = ?";
    private static final String SELECT_FOR_API_WITH_AUTHOR_FOR_USER = "SELECT q.* FROM \"video\" q, \"group\" e, \"userToGroup\" t, \"user\" u WHERE q.\"group\" = e.\"id\" AND e.\"id\" = t.\"group\" AND q.\"start\" is not null AND q.\"start\" <= NOW() AND (q.\"stop\" is null OR q.\"stop\" > NOW()) AND q.size > 0 AND (q.\"ratingPoints\" / (CASE q.\"ratings\" WHEN 0 THEN 1 ELSE q.\"ratings\" END) >= ?) AND q.\"views\" >= ? AND q.\"size\" <= ? AND q.\"author\" = u.\"id\" AND u.\"email\" = ?";
    private static final String COUNT_ALL = "SELECT COUNT(q.\"id\") FROM \"video\" q WHERE 1 = 1";
    private static final String COUNT_ALL_BY_GROUP = "SELECT COUNT(q.\"id\") FROM \"video\" q WHERE q.\"group\" = ?";
    private static final String COUNT_ALL_FOR_USER = "SELECT COUNT(distinct q.\"id\") FROM \"video\" q, \"group\" e, \"userToGroup\" t WHERE q.\"group\" = e.\"id\" AND e.\"id\" = t.\"group\"";
    private static final String COUNT_ALL_BY_GROUP_FOR_USER = "SELECT COUNT(distinct q.\"id\") FROM \"video\" q, \"group\" e, \"userToGroup\" t WHERE q.\"group\" = e.\"id\" AND e.\"id\" = t.\"group\" AND q.\"group\" = ?";
    private static final String DELETE_VIDEO = "DELETE FROM \"video\" where id = ?";

    private PgConnectionPool pool;

    /**
     * Initialize a new PgVideoStore.
     * 
     * @param pool
     *            for database access.
     */
    PgVideoStore(PgConnectionPool pool) {
        this.pool = pool;
    }

    /**
     * Get a {@link Video} for the specified result set.
     * 
     * @param video
     *            result set to set.
     */
    private Video deserialize(ResultSet video)
            throws SQLException {
        Video resultVideo = new Video(
                video.getInt("id"));

        resultVideo.setTitle(video.getString("title"));
        resultVideo.setDescription(video
                .getString("description"));

        Timestamp start = video.getTimestamp("start");
        if (start != null) {
            resultVideo.setStart(new Date(start.getTime()));
        }

        // Set option published to yes if start date is set
        resultVideo.setPublished(!video.wasNull());

        Timestamp stop = video.getTimestamp("stop");
        if (stop != null) {
            resultVideo.setStop(new Date(stop.getTime()));
        }
        resultVideo.setTitle(video.getString("title"));

        // Get participation restriction an transform it to Enum
        String participationRestriction = video
                .getString("participationRestriction");
        for (EParticipationRestriction e : EParticipationRestriction.values()) {
            if (participationRestriction.equals(e.toString().substring(0, 1)
                    .toLowerCase()
                    + e.toString().substring(1))) {
                resultVideo.setParticipationRestriction(e);
            }
        }

        resultVideo.setPassword(video.getString("password"));
        resultVideo.setDirectory(video.getString("directory"));
        resultVideo.setRatingPoints(video.getInt("ratingPoints"));
        resultVideo.setRatings(video.getInt("ratings"));
        resultVideo.setViews(video.getInt("views"));
        resultVideo.setDownloads(video.getInt("downloads"));
        resultVideo.setSize(video.getLong("size"));
        resultVideo.setAuthorId(video.getInt("author"));
        resultVideo.setCreated(new Date(video.getTimestamp("created").getTime()));
        resultVideo.setLastUpdated(new Date(video.getTimestamp("lastUpdated").getTime()));
        resultVideo.setVersion(video.getInt("version"));
        resultVideo.setZipDownloadEnabled(video.getBoolean("zipDownloadEnabled"));
        resultVideo.setChromeAppURL(video.getString("chromeAppURL"));
        return resultVideo;
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public Video create(final Video video, Group group)
            throws InconsistencyException {
        return create(video, group.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Video create(final Video video,
            final int groupId) throws InconsistencyException {
        final Integer[] videoId = new Integer[1];

        new SQLRunner(this.pool) {
            @Override
            public void runI() throws SQLException {

                // Insert new video
                stmt = conn.prepareStatement(INSERT_VIDEO,
                        Statement.RETURN_GENERATED_KEYS);
                stmt.setInt(1, groupId);
                stmt.setString(2, video.getTitle());
                stmt.setString(3, video.getDescription());
                stmt.setDate(4, video.getStart() == null ? null
                        : new java.sql.Date(video.getStart().getTime()));
                stmt.setDate(5, video.getStop() == null ? null
                        : new java.sql.Date(video.getStop().getTime()));

                // Special handling because of case sensitive postgre enum
                if (video.getParticipationRestriction() == EParticipationRestriction.GroupAttendants) {
                    stmt.setString(6, "groupAttendants");
                } else {
                    stmt.setString(6, video
                            .getParticipationRestriction().toString()
                            .toLowerCase());
                }

                stmt.setString(7, video.getPassword());
                stmt.setString(8, video.getDirectory());
                stmt.setInt(9, video.getAuthorId());
                stmt.executeUpdate();

                ResultSet rset = stmt.getGeneratedKeys();
                if (rset.next()) {
                    videoId[0] = rset.getInt(1);
                }
                rset.close();         
                stmt.close();
            }
        }.executeTI();
        return findById(videoId[0]);
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public Video save(final Video video)
            throws InconsistencyException {
        new SQLRunner(this.pool) {
            @Override
            public void runI() throws SQLException, InconsistencyException {
                stmt = conn.prepareStatement(SAVE_VIDEO);

                stmt.setString(1, video.getTitle());
                stmt.setString(2, video.getDescription());
                stmt.setTimestamp(
                        3,
                        (video.getStart() != null) ? new java.sql.Timestamp(
                                video.getStart().getTime()) : null);
                stmt.setTimestamp(
                        4,
                        (video.getStop() != null) ? new java.sql.Timestamp(
                                video.getStop().getTime()) : null);
               
                // Participation restriction.
                if (video.getParticipationRestriction() != null) {
                    String partRest = video
                            .getParticipationRestriction().toString();
                    partRest = partRest.substring(0, 1).toLowerCase()
                            + partRest.substring(1);
                    stmt.setString(5, partRest);
                } else {
                    stmt.setString(5, null);
                }

                stmt.setString(6, video.getPassword());
                stmt.setLong(7, video.getSize());
                stmt.setBoolean(8, video.isZipDownloadEnabled());
                stmt.setString(9, video.getChromeAppURL());
                stmt.setInt(10, video.getId());
                stmt.executeUpdate();
            }
        }.executeI();
        return video;
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public void delete(final int videoId) throws InconsistencyException {
        
        new SQLRunner(this.pool) {
            @Override
            public void runI() throws SQLException, InconsistencyException {
                stmt = conn.prepareStatement(DELETE_VIDEO);

                // Populate video id
                stmt.setInt(1, videoId);

                // Execute query
                if (stmt.executeUpdate() != 1) {

                    // Throw exception if specified video did not exist
                    throw new InconsistencyException(String.format(
                            NOT_FOUND_VIDEO, videoId));
                }
            }
        }.executeTI();
        return;
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public void delete(Video video)
            throws InconsistencyException {
        
        this.delete(video.getId());
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public Video getById(int id) {
        Video q = this.findById(id);
        if (q == null) {
            throw new NotFoundException(String.format(
                    NOT_FOUND_VIDEO, id));
        }
        return q;
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public Video getByToken(Token token) {
        return this.getByToken(token.getToken());
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public Video getByToken(final String token) {
    	Video video = this.findByToken(token);
    	if(video == null){
    		throw new NotFoundException(String.format(
                NOT_FOUND_Q_BY_TOKEN, token));
    	}
    	return video;
    }
    
    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public Video findByToken(final String token){
        final Video[] video = new Video[1];

        new SQLRunner(this.pool) {
            @Override
            public void run() throws SQLException {
                stmt = conn.prepareStatement(SELECT_ALL_BY_TOKEN);
                stmt.setString(1, token);
                ResultSet rset = stmt.executeQuery();
                if (rset.next()) {
                    video[0] = deserialize(rset);
                } else {
                    video[0] = null;
                }
            }
        }.execute();

        return video[0];        
    }
    
    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public Video findByDirectory(final String directory){
        final Video[] video = new Video[1];

        new SQLRunner(this.pool) {
            @Override
            public void run() throws SQLException {
                stmt = conn.prepareStatement(SELECT_BY_DIRECTORY);
                stmt.setString(1, directory);
                ResultSet rset = stmt.executeQuery();
                if (rset.next()) {
                    video[0] = deserialize(rset);
                } else {
                    video[0] = null;
                }
            }
        }.execute();

        return video[0];        
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public List<Video> getForGroup(Group group) {
        return getForGroup(group.getId());
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public List<Video> getForGroup(final int groupId) {
        final List<Video> videos = new LinkedList<Video>();

        final String query = SELECT_ALL_FOR_GROUP;

        new SQLRunner(this.pool) {
            @Override
            public void run() throws SQLException {
                stmt = conn.prepareStatement(query.toString());
                stmt.setInt(1, groupId);

                // Execute SQL query and add fetched videos to list
                ResultSet rset = stmt.executeQuery();
                while (rset.next()) {
                    videos.add(deserialize(rset));
                }
            }
        }.execute();

        return videos;
    }
    
    /**
     * {@inheritDoc}
     * 
     */
    public List<Video> getForApi(final float minimumRating, final int minimumViews,
			final long maxSize, final String author, final User user) {
    	final List<Video> videos = new LinkedList<Video>();

        new SQLRunner(this.pool) {
            @Override
            public void run() throws SQLException {
        	
        	// Check if an author was set
        	StringBuilder query;
            	if(author == null) {
            	    
            	    // Check if user is administrator and get all videos
                    if (user == null){
                	query = new StringBuilder(SELECT_FOR_API_BASIC);
                        query.append(getVisibiltityRestriction(user));
                    } else if(user.getUserType() == EUserType.Administrator) {
                        query = new StringBuilder(SELECT_FOR_API_BASIC);
                    } else {
                        query = new StringBuilder(SELECT_FOR_API_BASIC_FOR_USER);
                        query.append(getVisibiltityRestriction(user));
                    }
            	}
            	else {
            	    
            	    // Check if user is administrator and get all videos
                    if (user == null){
                	query = new StringBuilder(SELECT_FOR_API_WITH_AUTHOR);
                        query.append(getVisibiltityRestriction(user));
                    } else if(user.getUserType() == EUserType.Administrator) {
                        query = new StringBuilder(SELECT_FOR_API_WITH_AUTHOR);
                    } else {
                        query = new StringBuilder(SELECT_FOR_API_WITH_AUTHOR_FOR_USER);
                        query.append(getVisibiltityRestriction(user));
                    }
            	}
            	
            	// Append GROUP BY user is not a administrator because JOINS are needed
                if (user == null || user.getUserType() != EUserType.Administrator) {
                    query.append(" GROUP BY q.\"id\", e.\"id\"");
                }
                
                // Append ORDER BY
                query.append(" ORDER BY q.\"id\" ASC");
            	
            	stmt = conn.prepareStatement(query.toString());
                stmt.setFloat(1, minimumRating);
                stmt.setInt(2, minimumViews);
                stmt.setLong(3, maxSize);
                if(author != null) {
                	stmt.setString(4, author);
                }
                // Execute SQL query and add fetched videos to list
                ResultSet rset = stmt.executeQuery();
                while (rset.next()) {
                    videos.add(deserialize(rset));
                }
            }
        }.execute();

        return videos;
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public List<Video> getAll(User user, final Group group,
            ESortColumnVideo sortColumn, ESortDirection sortDirection,
            EVideoType type, int firstRow, int rows) {
        
        final List<Video> videos = 
                new LinkedList<Video>();

        final StringBuilder query;

        // Check if group is set and get just group videos or all
        if (group != null) {

            // Check if user is administrator and get all videos
            if (user == null){
        	query = new StringBuilder(SELECT_ALL_BY_GROUP);
                query.append(getVisibiltityRestriction(user));
            } else if(user.getUserType() == EUserType.Administrator) {
                query = new StringBuilder(SELECT_ALL_BY_GROUP);
            } else {
                query = new StringBuilder(SELECT_ALL_BY_GROUP_FOR_USER);
                query.append(getVisibiltityRestriction(user));
            }
        } else {

            // Check if user is administrator and get all videos
            if (user == null){
        	query = new StringBuilder(SELECT_ALL);
        	query.append(getVisibiltityRestriction(user));
            } else if(user.getUserType() == EUserType.Administrator) {
                query = new StringBuilder(SELECT_ALL);
            } else {
                query = new StringBuilder(SELECT_ALL_FOR_USER);
                query.append(getVisibiltityRestriction(user));
            }
        }

        query.append(getTypeRestriction(type));

        // Append GROUP BY user is not a administrator because JOINS are needed
        if (user == null || user.getUserType() != EUserType.Administrator) {
            query.append(" GROUP BY q.\"id\", e.\"id\"");
        }

        // Append ORDER BY to SQL query using the specified column and order
        if (sortColumn.equals(ESortColumnVideo.Title)) {
            query.append(" ORDER BY q.\"title\" " + sortDirection);
        } else if (sortColumn.equals(ESortColumnVideo.Group)) {
            query.append(" ORDER BY e.\"title\" " + sortDirection);
        }

        // Append OFFSET to SQL query if it is bigger than 0
        if(firstRow > 0) {
            query.append(" OFFSET " + firstRow);
        }

        // Append LIMIT to SQL query if greater than 0
        if (rows > 0) {
            query.append(" LIMIT " + rows);
        }

        new SQLRunner(this.pool) {
            @Override
            public void run() throws SQLException {
                stmt = conn.prepareStatement(query.toString());
                
                // Populate groupId if group is set
                if (group != null) {
                    stmt.setInt(1, group.getId());
                }
                
                // Execute SQL query and add fetched videos to list
                ResultSet rset = stmt.executeQuery();
                while (rset.next()) {
                    videos.add(deserialize(rset));
                }
            }
        }.execute();

        return videos;
    }

    /**
     * {@inheritDoc}
     * 
     */
    public int getCountOfAll(User user, final Group group,
            EVideoType type) {
        
        final int[] count = new int[1];

        StringBuilder query;

        // Check if group is set and get just group videos or all
        if (group != null) {

            // Check if user is administrator and get all videos
            if (user == null){
        	query = new StringBuilder(COUNT_ALL_BY_GROUP);
                query.append(getVisibiltityRestriction(user));
            } else if(user.getUserType() == EUserType.Administrator) {
                query = new StringBuilder(COUNT_ALL_BY_GROUP);
            } else {
                query = new StringBuilder(COUNT_ALL_BY_GROUP_FOR_USER);
                query.append(getVisibiltityRestriction(user));
            }
        } else {

            // Check if user is administrator and get all videos
            if (user == null){
        	query = new StringBuilder(COUNT_ALL);
                query.append(getVisibiltityRestriction(user));
            } else if(user.getUserType() == EUserType.Administrator) {
                query = new StringBuilder(COUNT_ALL);
            } else {
                query = new StringBuilder(COUNT_ALL_FOR_USER);
                query.append(getVisibiltityRestriction(user));
            }
        }

        query.append(getTypeRestriction(type));

        final String countQuery = query.toString();
        new SQLRunner(this.pool) {
            @Override
            public void run() throws SQLException {
                stmt = conn.prepareStatement(countQuery);

                // Populate groupId if group is set
                if (group != null) {
                    stmt.setInt(1, group.getId());
                }
                
                // Execute SQL query and set fetched amount of users
                ResultSet rset = stmt.executeQuery();
                rset.next();
                count[0] = rset.getInt(1);
            }
        }.execute();

        return count[0];
    }

    /**
     * Get visibility restrictions for SQL query for the specified {@link User}
     * to display only {@link Video}s the {@link User} is allowed to
     * see.
     * 
     * @param user
     *            to set.
     * @return the SQL query required for getting just the
     *         {@link Videos} viewable for the user. Query starts with a
     *         AND if a restriction is set.
     */
    private StringBuilder getVisibiltityRestriction(User user) {

        StringBuilder query = new StringBuilder("");

        // Get just started videos
        query.append(" AND ( (q.\"start\" < NOW() "
                + "AND q.\"start\" IS NOT NULL) ");

        // Check if user is logged in and get also videos of groups he
        // owns if so
        if (user != null) {
            query.append(" OR ( t.\"role\" = 'owner' " + "AND t.\"user\" = '"
                    + user.getId() + "' ) ");
        }
        query.append(" ) ");

        // Get always public videos and videos with password
        query.append(" AND ( q.\"participationRestriction\" = 'public' "
                + "OR q.\"participationRestriction\" = 'password' "
                + "OR q.\"participationRestriction\" = 'token' ");

        // Get videos for registered users and group attendants if
        // user is logged in
        if (user != null) {
            query.append(" OR q.\"participationRestriction\" = 'registered' "
                    + " OR ( q.\"participationRestriction\" = 'groupAttendants'"
                    + " AND t.\"user\" = '" + user.getId() + "'"
                    + "AND t.\"role\" = 'attendant' ) ");
        }

        // Get all owned videos for tutors
        if (user != null && user.getUserType() == EUserType.Tutor) {
            query.append(" OR ( t.\"user\" = '" + user.getId()
                    + "' AND t.\"role\" = 'owner' ) ");
        }

        query.append(" ) ");

        return query;
    }

    /**
     * Get start and end time restrictions for SQL query if just new, soon
     * ending or ended {@link Video}s should be displayed.
     * 
     * @param type
     *            of videos to set.
     * @return the SQL query required for getting just the specified
     *         {@link Videos}. Query starts with a AND if a restriction
     *         is set.
     */
    private StringBuilder getTypeRestriction(EVideoType type) {

        StringBuilder query = new StringBuilder("");
        if (type != null) {

            if (type == EVideoType.Active) {

            	// Get just videos that are started and still active
                query.append(" AND (q.\"start\" IS NOT NULL AND q.\"start\" < NOW() AND ("
                        + "q.\"stop\" > NOW() " 
                        + "OR q.\"stop\" IS NULL ) ) ");
            } else {

                // Get just videos that are not started yet or have already been ended
                query.append(" AND (q.\"start\" IS NULL OR q.\"start\" > NOW() OR ("
                        + "q.\"stop\" < NOW() " 
                        + "AND q.\"stop\" IS NOT NULL ) ) ");
            }
        }
        return query;
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public Video findById(final int id) {
        final Video[] q = new Video[1];

        new SQLRunner(this.pool) {
            @Override
            public void run() throws SQLException {
                stmt = conn.prepareStatement(SELECT_BY_ID);
                stmt.setInt(1, id);
                ResultSet rset = stmt.executeQuery();
                if (rset.next()) {
                    q[0] = deserialize(rset);
                }
            }
        }.execute();

        return q[0];
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public void moveVideo(final int videoId, int oldGroupId,
            final int newGroupId) throws InconsistencyException {
        new SQLRunner(this.pool) {
            @Override
            public void runI() throws SQLException {

                // Move video
                stmt = conn.prepareStatement(UPDATE_GROUP_VIDEO);
                stmt.setInt(1, newGroupId);
                stmt.setInt(2, videoId);
                stmt.executeUpdate();
            }
        }.executeI();
    }
    
    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public void increaseVersion(final int videoId) {
        new SQLRunner(this.pool) {
            @Override
            public void run() throws SQLException {

                // Move video
                stmt = conn.prepareStatement(INCREASE_VERSION);
                stmt.setInt(1, videoId);
                stmt.executeUpdate();
            }
        }.execute();
    }
    
    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public void increaseViews(final int videoId) {
        new SQLRunner(this.pool) {
            @Override
            public void run() throws SQLException {

                // Move video
                stmt = conn.prepareStatement(INCREASE_VIEWS);
                stmt.setInt(1, videoId);
                stmt.executeUpdate();
            }
        }.execute();
    }
}