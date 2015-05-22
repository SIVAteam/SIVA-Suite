package hu.persistence;

/**
 * This interface specifies methods to setup a persistence layer, i.e. create a
 * schema in the implemented datastore.
 */
public interface IPersistenceSetup {

    /**
     * Uninstall the persistence layer.
     * 
     * @throws PersistenceSetupException
     *             if the operation fails.
     */
    public void uninstall() throws PersistenceSetupException;

    /**
     * Install the persistence layer.
     * 
     * @throws PersistenceSetupException
     *             if the operation fails.
     */
    public void install() throws PersistenceSetupException;

    /**
     * Remove all data kept by the persistence layer.
     * 
     * @throws PersistenceSetupException
     *             if the operation fails.
     */
    public void purge() throws PersistenceSetupException;

    /**
     * Updates existing database.
     * 
     * @throws PersistenceSetupException
     *             if the operation fails.
     */
    public void update() throws PersistenceSetupException;
}