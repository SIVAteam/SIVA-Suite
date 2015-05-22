package hu.persistence;

import hu.persistence.postgres.PgPersistenceProvider;

/**
 * This interface describes a central provider which gives easy access to all
 * other objects in the persistence layer.
 */
public interface IPersistenceProvider {
	/**
     * @return a concrete implementation of an {@link IApiStore}.
     */
    public IApiStore getApiStore();
	
    /**
     * @return a concrete implementation of an {@link IGroupStore}.
     */
    public IGroupStore getGroupStore();

    /**
     * @return a concrete implementation of an {@link IVideoStore}.
     */
    public IVideoStore getVideoStore();

    /**
     * @return a concrete implementation of an {@link ITokenStore}.
     */
    public ITokenStore getTokenStore();

    /**
     * @return a concrete implementation of an {@link IUserStore}.
     */
    public IUserStore getUserStore();

    /**
     * @return a concrete implementation of an {@link IPersistenceSetup}.
     */
    public IPersistenceSetup getSetup();

    /**
     * Close all resources used by the {@link PgPersistenceProvider}, this
     * includes instances of all provided datastores as well as database
     * connections.
     */
    public void close();

    /**
     * Restart the {@link PgPersistenceProvider}, this includes instances of all
     * provided datastores as well as database connections.
     */
    public void restart();

    /**
     * @return true if the {@link PgPersistenceProvider} is able to interact
     *         with its backend.
     */
    public boolean testConnectivity();
}