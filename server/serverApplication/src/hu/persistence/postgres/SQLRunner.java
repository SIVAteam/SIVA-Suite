package hu.persistence.postgres;

import hu.persistence.InconsistencyException;
import hu.persistence.PersistenceRuntimeException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * This class provides an execution environment for code accessing the database.
 * It takes care of error handling and freeing of resources. This reduced the
 * amount of duplicate boilerplate code when accessing the database.
 */
public abstract class SQLRunner {

    private static final String CONSTR_ERR_PREFIX = "HuConstraint";
    private static final String TRIGGER_ERR_PREFIX = "HuTriggerEx";
    private PgConnectionPool pool;
    protected Connection conn;
    protected PreparedStatement stmt;

    /**
     * Create a new {@link SQLRunner}.
     * 
     * @param pool
     *            to access the database with.
     */
    public SQLRunner(PgConnectionPool pool) {
        this.pool = pool;
        this.conn = null;
        this.stmt = null;
    }

    /**
     * Execute {@link SQLRunner#runI()} in a database transaction which will be
     * rolled back if an error occurs.
     * 
     * @throws InconsistencyException
     *             thrown by {@link SQLRunner#runI()}.
     */
    public void executeTI() throws InconsistencyException {
        this.executeTI(1);
    }
    
    /**
     * Execute {@link SQLRunner#runI()} in a database transaction which will be
     * rolled back if an error occurs.
     * 
     * @param tries
     *            for repeating the operation in case of failure.
     * @throws InconsistencyException
     *             thrown by {@link SQLRunner#runI()}.
     */
    public void executeTI(int tries) throws InconsistencyException {
        this.execute(true, true, tries);
    }

    /**
     * Execute {@link SQLRunner#run()} in a database transaction which will be
     * rolled back if an error occurs.
     */
    public void executeT() {
        this.executeT(1);
    }
    
    /**
     * Execute {@link SQLRunner#run()} in a database transaction which will be
     * rolled back if an error occurs.
     * 
     * @param tries
     *            for repeating the operation in case of failure.
     */
    public void executeT(int tries) {
        try {
            this.execute(true, false, tries);
        } catch (InconsistencyException e) {
            throw new PersistenceRuntimeException(
                    "Unexpected InconsistencyException", e);
        }
    }

    /**
     * Execute {@link SQLRunner#run()}.
     */
    public void execute() {
        try {
            this.execute(false, false, 1);
        } catch (InconsistencyException e) {
            throw new PersistenceRuntimeException(
                    "Unexpected InconsistencyException", e);
        }
    }

    /**
     * Execute {@link SQLRunner#runI()}.
     * 
     * @throws InconsistencyException
     *             thrown by {@link SQLRunner#runI()}.
     */
    public void executeI() throws InconsistencyException {
        this.execute(false, true, 1);
    }

    /**
     * Run database access code that may throw an {@link InconsistencyException}
     * .
     * 
     * @throws SQLException
     *             if the database access fails.
     * @throws InconsistencyException
     *             if the database access causes an inconsistency.
     */
    protected void runI() throws SQLException, InconsistencyException {
    }

    /**
     * Run database access code.
     * 
     * @throws SQLException
     *             in case the database access fails.
     */
    protected void run() throws SQLException {
    }

    private void execute(boolean useTransaction, boolean allowInconsistencyExc, int tries)
            throws InconsistencyException {
        // Loop for retrying execution.
        for (int attempt = tries; attempt > 0; attempt--) {
            // Flag indicating, whether execution was successful.
            boolean success = false;

            // Fetch a connection from the connection pool.
            this.conn = this.pool.fetchConnection();
            try {
                // Start transaction if requested.
                if (useTransaction) {
                    this.conn.setAutoCommit(false);
                }
    
                // Execute implemented functionality.
                if (allowInconsistencyExc) {
                    this.runI();
                } else {
                    this.run();
                }
    
                // End transaction if requested.
                if (useTransaction) {
                    this.conn.commit();
                }

                success = true;

            } catch (SQLException e) {
                success = false;

                // Rollback transaction on failure.
                if (useTransaction) {
                    try {
                        this.conn.rollback();
                    } catch (SQLException ignored) {
                    }
                }
    
                // Convert trigger or constraint errors to inconsistency exceptions.
                if (allowInconsistencyExc) {
                    String msg = e.getMessage();
                    if (msg.contains(TRIGGER_ERR_PREFIX)
                            || msg.contains(CONSTR_ERR_PREFIX)) {
                        if (attempt == 1) {
                            throw new InconsistencyException(e.getMessage(), e);
                        }
                    }
                }
    
                if (attempt == 1) {
                    throw new PersistenceRuntimeException("Database error", e);
                }
        
            } finally {
                try {
                    // Close used statement.
                    if (this.stmt != null) {
                        this.stmt.close();
                    }
        
                } catch (SQLException e) {
                    success = false;

                    if (attempt == 1) {
                        throw new PersistenceRuntimeException("Database error", e);
                    }
        
                } finally {
                    try {
                        // End transaction use if required.
                        if (useTransaction) {
                            this.conn.setAutoCommit(true);
                        }
        
                    } catch (SQLException e) {
                        success = false;

                        if (attempt == 1) {
                            throw new PersistenceRuntimeException("Database error", e);
                        }
        
                    } finally {
                        // Return used connection to the pool.
                        this.pool.returnConnection(this.conn);
                    }
                }
            }

            if (success) {
                break;
            }
        }
    }

}
