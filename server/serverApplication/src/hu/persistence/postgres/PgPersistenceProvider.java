package hu.persistence.postgres;

import hu.persistence.IApiStore;
import hu.persistence.IGroupStore;
import hu.persistence.IPersistenceProvider;
import hu.persistence.IPersistenceSetup;
import hu.persistence.ITokenStore;
import hu.persistence.IUserStore;
import hu.persistence.IVideoStore;
import hu.util.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;

/**
 * This class implements a {@link IPersistenceProvider} using a PostgreSQL
 * backend. 
 */
@ManagedBean(name = "PersistenceProvider", eager = true)
@ApplicationScoped
public class PgPersistenceProvider implements IPersistenceProvider {

    @ManagedProperty("#{configuration}")
    private Configuration configuration;
    private PgConnectionPool pool;
    private PgApiStore apiStore;
    private PgGroupStore groupStore;
    private PgUserStore userStore;
    private PgVideoStore videoStore;
    private PgTokenStore tokenStore;
    private IPersistenceSetup setup;
    private boolean postConstructFired;

    /**
     * Set {@link Configuration} using injection to retrieve database connection
     * information.
     * 
     * @param configuration
     *            to set.
     */
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Create a new {@link PgPersistenceProvider}. Initialization of all
     * provided datastores as well as the database connection takes place when
     * the PostConstruct group is triggered.
     */
    public PgPersistenceProvider() {
        this.postConstructFired = false;
    }

    /**
     * Initialize a new {@link PgPersistenceProvider}, this includes creating
     * instances of all provided datastores as well as establishing database
     * connectivity.
     * 
     * @param configuration
     *            to retrieve database connection information from.
     */
    public PgPersistenceProvider(Configuration configuration) {
        this.configuration = configuration;
        this.postConstructFired = true;
        if (this.testConnectivity()) {
            this.init();
        }
    }

    @PostConstruct
    private void init() {
        if (!this.postConstructFired
                && !this.configuration.getBoolean("is_installed")) {
            this.postConstructFired = true;
            return;
        }

        this.pool = new PgConnectionPool(String.format(
                "jdbc:postgresql://%s:%d/%s",
                this.configuration.getString("database_host"),
                this.configuration.getInteger("database_port"),
                this.configuration.getString("database_name")),
                this.configuration.getString("database_user"),
                this.configuration.getString("database_password"),
                this.configuration.getInteger("database_connections"));

        this.apiStore = new PgApiStore(this.pool);
        this.groupStore = new PgGroupStore(this.pool);
        this.userStore = new PgUserStore(this.pool);
        this.videoStore = new PgVideoStore(this.pool);
        this.tokenStore = new PgTokenStore(this.pool);
        this.setup = new PgSchemaSetup(this.pool);
    }

    @PreDestroy
    private void destroy() {
        this.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        this.groupStore = null;
        this.userStore = null;
        this.videoStore = null;
        this.tokenStore = null;
        if (this.pool != null) {
            this.pool.close();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void restart() {
        this.close();
        this.init();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean testConnectivity() {
        return PgConnectionPool.testCredentials(String.format(
                "jdbc:postgresql://%s:%d/%s",
                this.configuration.getString("database_host"),
                this.configuration.getInteger("database_port"),
                this.configuration.getString("database_name")),
                this.configuration.getString("database_user"),
                this.configuration.getString("database_password"));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IApiStore getApiStore() {
        return this.apiStore;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IGroupStore getGroupStore() {
        return this.groupStore;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IVideoStore getVideoStore() {
        return this.videoStore;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ITokenStore getTokenStore() {
        return this.tokenStore;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IUserStore getUserStore() {
        return this.userStore;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IPersistenceSetup getSetup() {
        return this.setup;
    }
}
