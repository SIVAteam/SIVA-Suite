package org.iviPro.scenedetection.shd_algorithm;

public class ShotDetectionSettings {

    /**
     * Anzahl der Merkmalsalgorithmen
     */
    static final int AMOUNTDETECTIONALGORITHM = 3;
    
    /**
     * Wert Trennfläche a
     */
    static final float fuzzySetVariableA = 0.85f;

    /**
     * Wert Trennfläche b
     */
    static final float fuzzySetVariableB = 1.7f;

    /**
     * Wert Trennfläche c
     */
    static final float fuzzySetVariableC = 2.55f;

    /**
     * Größe des Framebuffers bei Hauptalgorithmus
     */
    static final int DETECTIONWINDOWSIZE = 50;

    /**
     * Grenzwert der unterschritten bzw überschritten werden muss, damit ein
     * Fade erkannt wird
     */
    static final int FADESTANDARDVARIANCE = 23;

    /**
     * Mindestlänge eines Fades
     */
    static final int FADEMINLENGTH = 5;

    /**
     * Maximale Fadelänge
     */
    static final int FADEMAXLENGHT = 40;

    /**
     * Parameter für OSF der das benötigte Saitenverhältnis festlegt (Muss
     * nicht geändert werden.)
     */
    static final int ONESIDEDFEATURETHRESHOLD = 4;

    /**
     * Festgelegte Werte um RGB in YUV umzuwandeln
     */
    static final float Y_VALUE_R = 0.299f;
    static final float Y_VALUE_G = 0.587f;
    static final float Y_VALUE_B = 0.114f;

    /**
     * Deklaration von Schwarz und Weißwerte. Wird für Kantenbild benötigt
     */
    static final int BLACK = -16777216;
    static final int WHITE = -1;

    /**
     * Grenzwert der überschritten werden muss wenn die Fadeerkennung mit Hilfe
     * des Edge Change Ratio Algorithmus stattfindet.
     */
    static final float FADETHRESHOLD = 1.0f;

    /**
     * Nur für Edge Change Ratio Algorithmus!!! Minimallänge des Fades
     */
    static final int FADELENGTHCHECK = 5;

    /**
     * Das zentrale Bild (Bildnummer im Buffer), das den Fade repräsentiert.
     */
    static final int FADEPOINTERPOSITION = 25;

    /**
     * Startbildnummer des möglichen Fades innerhalb des Buffers (wird nur bei
     * Fadeerkennung mit Hilfe der Standardabweichung benötigt)
     */
    static final int FVPP = 5;

    /**
     * Grenzwert der überschritten werden muss, damit ein Fade resultiert. Gibt
     * Minimalwert an welcher sich beim Abfall bzw Anstieg der Standardvarianz durch-
     * schnittlich ändern muss.
     */
//    static final float FADERAISINGTHRESHOLD = 0.8f;
    static final float FADERAISINGTHRESHOLD = 0.7f;

    /**
     * Startpunkt des Dissolves innerhalb des Bearbeitungsbuffers
     */
    static final int DISSOLVEPOINTERPOSITION = 0;

    /**
     * Wird bei OSF Edge Change Ratio Algorithmus beötigt und gibt den Grenzwert
     * zur Hardcuterkennung an.
     */
    static final float HARDCUTTHRESHOLD = 2.0f;

    /**
     * Wird bei OSF Edge Change Ratio Algorithmus beötigt und gibt den Grenzwert
     * an, bei welchen ECR Werten ein Hardcut erkannt werden soll
     */
    static final float HARDCUTSECONDTHRESHOLD = 0.325f;

    /**
     * Minimallänge eines Dissolves
     */
    public static final int DISSOLVEMINLENGTH = 4;

    /**
     * Diese Einstellungen können im Optionsmenü des Algorithmus eingestellt
     * werden
     */
    private static boolean onSidedFeature;
    private static boolean enableDissolve;
    private static boolean fastMotionCompensation;
    private static boolean fadingStandardVariance;
    private static String edgeDetection;
    private static String video;

    /**
     * Singleton Pattern. Es soll kein neues Objekt erstellt werden.
     */
    private ShotDetectionSettings() {
    }

    static String getEdgeDetection() {
        return edgeDetection;
    }

    static boolean isFadeWithStandardVariation() {
        return fadingStandardVariance;
    }

    static boolean isFastMotionCompensation() {
        return fastMotionCompensation;
    }

    static boolean isOnSidedFeature() {
        return onSidedFeature;
    }

    static boolean isEnableDissolve() {
        return enableDissolve;
    }

    static String getVideo() {
        return video;
    }

    public static void setEdgeDetection(String edgeDetection) {
        ShotDetectionSettings.edgeDetection = edgeDetection;
    }

    public static void setEnableDissolve(boolean enableDissolve) {
        ShotDetectionSettings.enableDissolve = enableDissolve;
    }

    public static void setFading(boolean fading) {
        ShotDetectionSettings.fadingStandardVariance = fading;
    }

    public static void setFastMotionCompensation(boolean fastMotionCompensation) {
        ShotDetectionSettings.fastMotionCompensation = fastMotionCompensation;
    }

    public static void setOnSidedFeature(boolean onSidedFeature) {
        ShotDetectionSettings.onSidedFeature = onSidedFeature;
    }

    public static void setVideo(String video) {
        ShotDetectionSettings.video = video;
    }
}
