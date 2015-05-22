package hu.persistence.postgres;

import hu.persistence.IPersistenceSetup;
import hu.persistence.PersistenceSetupException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

/**
 * This class implements the {@link IPersistenceSetup} for a PostgreSQL
 * database.
 */
public class PgSchemaSetup implements IPersistenceSetup {
    private static final String END_OF_QUERY_MARKER = "/*eoq*/";
    private static final String UPDATE_SCHEMA_SQL = "hu/persistence/postgres/schema/updateSchema.sql";
    private static final String EMPTY_SCHEMA_SQL = "hu/persistence/postgres/schema/emptySchema.sql";
    private static final String CREATE_SCHEMA_SQL = "hu/persistence/postgres/schema/createSchema.sql";
    private static final String REMOVE_SCHEMA_SQL = "hu/persistence/postgres/schema/removeSchema.sql";
    private PgConnectionPool pool;

    /**
     * Create a new {@link PgSchemaSetup}.
     * 
     * @param pool
     *            for database access.
     */
    public PgSchemaSetup(PgConnectionPool pool) {
        this.pool = pool;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void uninstall() throws PersistenceSetupException {
        this.runFileInDatabase(REMOVE_SCHEMA_SQL);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void install() throws PersistenceSetupException {
        this.runFileInDatabase(CREATE_SCHEMA_SQL);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void purge() throws PersistenceSetupException {
        this.runFileInDatabase(EMPTY_SCHEMA_SQL);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update() throws PersistenceSetupException {
    	this.runFileInDatabase(UPDATE_SCHEMA_SQL);
    }

    private void runFileInDatabase(String filename)
            throws PersistenceSetupException {
        Connection conn = this.pool.fetchConnection();
        Statement st = null;
        try {
            st = conn.createStatement();
            List<String> queries = this.loadQueries(filename);
            for (String query : queries) {
                st.execute(query);
            }
            st.close();

        } catch (SQLException e) {
        	e.printStackTrace();
            throw new PersistenceSetupException(
                    "Failed to execute queries from file: " + filename, e.getNextException());

        } finally {
            try {
                if (st != null) {
                    st.close();
                }
            } catch (SQLException ignored) {
            } finally {
                this.pool.returnConnection(conn);
            }
        }
    }

    private List<String> loadQueries(String name)
            throws PersistenceSetupException {
        // Open specified file.
        InputStream is = this.getClass().getClassLoader()
                .getResourceAsStream(name);
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);

        List<String> result = new LinkedList<String>();
        StringBuilder queryBuffer = new StringBuilder();
        try {
            for (String line = ""; line != null; line = br.readLine()) {
                if (line.trim().length() == 0) {
                    continue;
                }
                queryBuffer.append(line);
                queryBuffer.append("\n");
                if (line.contains(END_OF_QUERY_MARKER)) {
                    result.add(queryBuffer.toString());
                    queryBuffer = new StringBuilder();
                }
            }
        } catch (IOException e) {
        	throw new PersistenceSetupException(
                    "Failed to load queries from file: " + name, e);
        } finally {
            try {
                is.close();
            } catch (IOException ignored) {
            }
        }

        return result;
    }
}
