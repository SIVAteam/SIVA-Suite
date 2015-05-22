package hu.persistence.postgres;

import hu.persistence.PersistenceRuntimeException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * This class implements a connection pool for managing {@link Connection}s to a
 * PostgreSQL database. The pool will try to keep a constant number of open
 * {@link Connection}s which may be reserved for exclusive use.
 */
class PgConnectionPool {
    private static final int TRANSACTION_ISOLATION = Connection.TRANSACTION_REPEATABLE_READ;
    private static final String JDBC_DRIVER = "org.postgresql.Driver";
    private BlockingQueue<Connection> pool;
    private Set<Connection> connections;
    private int poolSize;
    private String url;
    private String username;
    private String password;

    /**
     * Test if the given credentials can be used to establish a database
     * connection.
     * 
     * @param url
     *            to database.
     * @param username
     *            for database access.
     * @param password
     *            for database access.
     * @return true if the credentials can be used to establish a database
     *         connection.
     */
    public static boolean testCredentials(String url, String username,
            String password) {
        PgConnectionPool.loadDriver();

        Connection c = null;
        try {
            c = DriverManager.getConnection(url, username, password);

        } catch (SQLException e) {
            return false;

        } finally {
            if (c != null) {
                try {
                    c.close();
                } catch (SQLException ignored) {
                }
            }
        }

        return true;
    }

    /**
     * Initialize a new {@link PgConnectionPool}.
     * 
     * @param url
     *            to database.
     * @param username
     *            for database access.
     * @param password
     *            for database access.
     * @param poolSize
     *            of the {@link PgConnectionPool}, i.e. the number of open
     *            {@link Connection}s to keep in the pool.
     */
    public PgConnectionPool(String url, String username, String password,
            int poolSize) {
        PgConnectionPool.loadDriver();
        if(poolSize < 1)
        	poolSize = 2;
        this.poolSize = poolSize;
        this.url = url;
        this.username = username;
        this.password = password;

        // Initialize datastructures to hold connections.
        this.connections = new HashSet<Connection>();
        this.pool = new ArrayBlockingQueue<Connection>(poolSize);

        // Create connections.
        this.replenish();
    }

    private synchronized void replenish() {
        while (this.connections.size() < this.poolSize) {
            try {
                // Establish and configure database connection.
                Connection c = DriverManager.getConnection(
                        this.url,
                        this.username,
                        this.password);
                c.setTransactionIsolation(TRANSACTION_ISOLATION);

                // Add connection to the pool's datastructures.
                this.connections.add(c);
                this.pool.offer(c);

            } catch (SQLException e) {
                this.close();
                throw new PersistenceRuntimeException(
                        "Failed to replenish database connection pool.", e);
            }
        }
    }

    private static void loadDriver() {
        try {
            Class.forName(JDBC_DRIVER);
        } catch (ClassNotFoundException e) {
            throw new PersistenceRuntimeException(
                    "Failed to load postgres driver.", e);
        }
    }

    /**
     * Fetch a {@link Connection} from the {@link PgConnectionPool}. This method
     * will block until a {@link Connection} is available. A fetched
     * {@link Connection} is reserved for exclusive use by the caller.
     * 
     * @return the {@link Connection} from the pool.
     */
    public Connection fetchConnection() {
        Connection c = null;

        while (c == null) {
            // Fetch connection from queue.
            // This call blocks until a connection becomes available.
            try {
                c = this.pool.take();
            } catch (InterruptedException e) {
                throw new PersistenceRuntimeException(
                        "Failed to poll connection.", e);
            }

            // Determine if connection is alive.
            // A connection may die if it is unused for a while.
            boolean closed;
            try {
                closed = c.isClosed();
            } catch (SQLException e) {
                throw new PersistenceRuntimeException(
                        "Failed to determine connection status.", e);
            }

            if (closed) {
                this.connections.remove(c);
                c = null;
                this.replenish();
            }
        }

        return c;
    }

    /**
     * Return a {@link Connection} to the {@link PgConnectionPool} that was
     * previously retrieved using {@link PgConnectionPool#fetchConnection()}.
     * After a {@link Connection} is returned, the caller must not use it
     * anymore and destroy any references to it.
     * 
     * @param connection
     *            to return.
     */
    public void returnConnection(Connection connection) {
        // Ignore connection if not managed by pool.
        if (!this.connections.contains(connection)) {
            return;
        }

        try {
            this.pool.put(connection);
        } catch (InterruptedException e) {
            throw new PersistenceRuntimeException(
                    "Failed to return connection.", e);
        }
    }

    /**
     * Close all {@link Connection}s in the {@link PgConnectionPool}. This
     * method will block until all {@link Connection}s have been returned to the
     * pool.
     */
    public void close() {
        while (!this.connections.isEmpty()) {
            Connection c = this.fetchConnection();
            try {
                c.close();
            } catch (SQLException ignored) {
            }
            this.connections.remove(c);
        }
    }
}
