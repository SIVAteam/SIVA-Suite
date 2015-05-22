package hu.persistence.postgres;

import hu.model.Token;
import hu.persistence.ITokenStore;
import hu.persistence.InconsistencyException;
import hu.persistence.NotFoundException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * This class implements a {@link ITokenStore} using a PostgreSQL backend.
 */
public class PgTokenStore implements ITokenStore {

    private static final String SELECT_VIDEO_BY_ID = "SELECT * FROM \"video\" WHERE \"id\" = ?";
    private static final String NOT_FOUND_TOKEN = "Token with String %s does not exist.";
    private static final String TOKENTYPE_PARTICIPATION = "participation";
    private static final String TOKENTYPE_EVALUATION = "evaluation";
    private static final String SELECT_BY_TOKEN = "SELECT * FROM \"token\" WHERE token = ?";
    private static final String CREATE_TOKEN = "INSERT INTO \"token\" (\"token\", \"type\", \"video\") VALUES (?, ?::\"enumTokenType\", ?)";
    private static final String DELETE_TOKEN = "DELETE FROM \"token\" WHERE token = ?";
    private static final String SELECT_BY_VIDEO = "SELECT \"token\", \"type\", \"video\" FROM \"token\" WHERE \"video\" = ?";

    private PgConnectionPool pool;
    
    /**
     * Initialize a new PgTokenStore.
     * 
     * @param pool
     *            for database access.
     */
    PgTokenStore(PgConnectionPool pool) {
        this.pool = pool;
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public Token create(final Token token, final int videoId) throws InconsistencyException {
        
        new SQLRunner(this.pool) {
            @Override
            public void runI() throws SQLException, InconsistencyException {
                stmt = conn.prepareStatement(CREATE_TOKEN);
                stmt.setString(1, token.getToken());

                if (token.isForParticipation()) {
                    stmt.setString(2, TOKENTYPE_PARTICIPATION);
                } else {
                    stmt.setString(2, TOKENTYPE_EVALUATION);
                }
                stmt.setInt(3, videoId);
                stmt.executeUpdate();
                stmt.close();
            }
        }.executeTI();
        return token;
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public void delete(final String token) throws InconsistencyException {
        new SQLRunner(this.pool) {
            @Override
            public void runI() throws SQLException, InconsistencyException {
                stmt = conn.prepareStatement(DELETE_TOKEN);
                
                // Populate token string
                stmt.setString(1, token);
                
                // Execute query
                if (stmt.executeUpdate() != 1) {

                    // Throw exception if specified token did not exist
                    throw new InconsistencyException(String.format(
                            NOT_FOUND_TOKEN, token));
                }
            }
        }.executeI();
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public void delete(Token token) throws InconsistencyException {
        delete(token.getToken());
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public List<Token> getForVideo(final int videoId) {
        final List<Token> tokens = new LinkedList<Token>();
        new SQLRunner(this.pool) {
            @Override
            public void run() throws SQLException {
                stmt = conn.prepareStatement(SELECT_VIDEO_BY_ID);
                stmt.setInt(1, videoId);
                if (!stmt.executeQuery().next()) {
                    throw new NotFoundException(String.format(
                            "Video with id %d does not exist!",
                            videoId));
                }
                
                stmt = conn.prepareStatement(SELECT_BY_VIDEO);
                stmt.setInt(1, videoId);
                ResultSet rset = stmt.executeQuery();

                while (rset.next()) {
                    tokens.add(deserialize(rset));
                }
            }
        }.executeT();
        return tokens;
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public Token find(final String tokenIdentifier) {

        final Token[] tokenFound = new Token[1];

        new SQLRunner(this.pool) {
            @Override
            public void run() throws SQLException {
                stmt = conn.prepareStatement(SELECT_BY_TOKEN);
                stmt.setString(1, tokenIdentifier);
                ResultSet rset = stmt.executeQuery();
                if (rset.next()) {
                    tokenFound[0] = deserialize(rset);
                }
            }
        }.execute();

        return tokenFound[0];
    }
    
    /**
     * Convert an entry in a {@link ResultSet} to a {@link Token} object.
     * 
     * @param rset
     *            to deserialize {@link Token} from.
     * @return the {@link Token} at the current position in the
     *         {@link ResultSet}.
     * @throws SQLException
     *             if a database failure occurs.
     */
    private Token deserialize(ResultSet rset) throws SQLException {
        Token token = new Token(rset.getString("token"));
        
        if (rset.getString("type").toString().toLowerCase().equals(TOKENTYPE_EVALUATION)) {
            token.setForEvalutation(true);
        } else {
            token.setForParticipation(true);
        }
        return token;
    }
}