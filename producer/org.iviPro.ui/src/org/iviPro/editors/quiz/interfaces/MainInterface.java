package org.iviPro.editors.quiz.interfaces;

import org.iviPro.editors.quiz.std.NodeModel;
import org.iviPro.editors.quiz.std.TestManagementModel;
import org.iviPro.editors.quiz.std.TestModel;

/**
 * Das Interface wird von der View und dem Controller benutzt.
 * 
 * @author Sabine Gattermann
 * 
 */
public interface MainInterface {

    /**
     * Erstellt einen neuen Test.
     */
    void newTest(int testId);

    /**
     * Oeffnet den privaten Test-Pool.
     */
    void openOwenTests();

    /**
     * Oeffnet den oeffentlichen Test-Pool.
     */
    void openTestPool();

    /**
     * Beendet das Programm.
     */
    void exit();

    /**
     * Oeffnet die Profileinstellungen.
     */
    void openProfil();

    /**
     * Oeffnet die Hilfe.
     */
    void openHelp();

    /**
     * Methode zum Ueberpruefen, ob eine entsprechende Instanz vorhanden (bzw.
     * gesetzt) ist.
     * 
     * @return TestManagementModel-Instanz oder null.
     */
    TestManagementModel getTestManagementModelInstance();

    /**
     * Methode zum Ueberpruefen, ob eine entsprechende Instanz vorhanden (bzw.
     * gesetzt) ist.
     * 
     * @return TesttModel-Instanz oder null.
     */
    TestModel getTestModelInstance();

    /**
     * Methode zum Ueberpruefen, ob eine entsprechende Instanz vorhanden (bzw.
     * gesetzt) ist.
     * 
     * @return NodeModel-Instanz oder null.
     */
    NodeModel getNodeModelInstance();

    /**
     * Methode zum Setzen einer NodeModel-Instanz.
     * 
     * @param nodeModel Die Instanz.
     */
    void setNodeModelInstance(NodeModel nodeModel);

    /**
     * Methode zum Setzen einer TestManagementModel-Instanz.
     * 
     * @param testManagementModelInstance Die Instanz.
     */
    void setTestManagementModelInstance(
	    TestManagementModel testManagementModelInstance);

    /**
     * Methode zum Setzen einer TestModel-Instanz.
     * 
     * @param testModelInstance Die Instanz.
     */
    void setTestModelInstance(TestModel testModelInstance);

    /**
     * Uebergeben einer anzuzeigenden Nachricht.
     * 
     * @return String Die Nachricht.
     */
    String getInfoMsg();

    /**
     * Oeffnet die Login-View
     * */
    public void close();
}
