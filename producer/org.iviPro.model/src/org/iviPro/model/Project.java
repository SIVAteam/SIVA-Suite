/**
 * 
 */
package org.iviPro.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.log4j.Logger;
import org.iviPro.model.graph.Graph;
import org.iviPro.model.graph.INodeAnnotationLeaf;
import org.iviPro.model.graph.NodeIDGenerator;
import org.iviPro.model.quiz.AdditionalInfo;
import org.iviPro.model.quiz.AdditionalInfoManager;
import org.iviPro.model.quiz.Answer;
import org.iviPro.model.quiz.AnswerManager;
import org.iviPro.model.quiz.Category;
import org.iviPro.model.quiz.CategoryManager;
import org.iviPro.model.quiz.Condition;
import org.iviPro.model.quiz.ConditionManager;
import org.iviPro.model.quiz.Edge;
import org.iviPro.model.quiz.EdgeManager;
import org.iviPro.model.quiz.Node;
import org.iviPro.model.quiz.NodeManager;
import org.iviPro.model.quiz.Question;
import org.iviPro.model.quiz.QuestionManager;
import org.iviPro.model.quiz.Test;
import org.iviPro.model.quiz.TestManager;
import org.iviPro.model.quiz.User;
import org.iviPro.model.quiz.UserManager;
import org.iviPro.model.resources.Scene;
import org.iviPro.model.resources.Video;

/**
 * @author dellwo
 * @uml.dependency supplier="org.iviPro.model.TocItem"
 * @uml.dependency supplier="org.iviPro.model.graph.Graph"
 * @uml.dependency supplier="org.iviPro.model.IMediaObject"
 */
public class Project extends IFileBasedObject {

	public static final String PROJECT_FILE_ENCODING = "UTF-8"; //$NON-NLS-1$
	public static final String PROJECT_FILE_EXTENSION = "siva"; //$NON-NLS-1$
	public static final String PROJECT_BACKUP_FILE_EXTENSION = "sivaBackup"; //$NON-NLS-1$
	public static final String PROP_LANGUAGES = "languages"; //$NON-NLS-1$

	public static final String SUBDIRECTORY_MEDIA = "media";//$NON-NLS-1$
	public static final String SUBDIRECTORY_RICHTEXT = "richtext";//$NON-NLS-1$
	public static final String SUBDIRECTORY_VIDEO = "video";//$NON-NLS-1$
	public static final String SUBDIRECTORY_AUDIO = "audio";//$NON-NLS-1$
	public static final String SUBDIRECTORY_PICTURE = "picture";//$NON-NLS-1$

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(Project.class);

	/**
	 * Generator for the creation of unique identifiers for nodes of the scene graph.
	 */
	private final NodeIDGenerator nodeIdGen = new NodeIDGenerator();
	
	/**
	 * @uml.property name="sceneGraph"
	 */
	private Graph sceneGraph;
	
	/**
	 * Aktuelle verwendete Sprache des Projekts
	 */
	private Locale currentLanguage;
	/**
	 * Standard-Sprache des Projekts
	 * 
	 * @uml.property name="defaultLanguage"
	 */
	private Locale defaultLanguage;
	/**
	 * TODO DOK
	 */
	private int lastMediaObjectId = 0;
	/**
	 * @uml.property name="mediaObjects"
	 */
	private BeanList<IAbstractBean> mediaObjects;
	/**
	 * @uml.property name="globalAnnotations"
	 */
	private BeanList<INodeAnnotationLeaf> globalAnnotations;
	/**
	 * @uml.property name="languages" readOnly="true"
	 */
	private List<Locale> languages;
	/**
	 * @uml.property name="tableOfContents"
	 */
	private TocItem tableOfContents;
	/**
	 * @uml.property name="settings"
	 */
	private ProjectSettings settings;

	/**
	 * @uml.property name="QuizBeans"
	 */
	private LinkedList<BeanList<? extends IQuizBean>> quizBeans;

	/**
	 * @uml.property name="QuizKeys"
	 */
	private LinkedList<Integer> quizKeys;
	
	/**
	 * Used to store handles to unused files until either those files are
	 * used again or a ProjectSaveAction deletes the unused files.
	 */
	private transient HashSet<File> unusedFiles = new HashSet<File>();

	/**
	 * Erstellt eine neues Projekt-Objekt. Als Standard-Sprache des Projekts
	 * wird die Sprache des Titels verwendet.
	 * 
	 * @param title
	 *            Der Titel des Projekts.
	 * @param projectFile
	 *            Projekt-Datei fuer das Projekt.
	 */
	public Project(LocalizedString title, File projectFile) {
		super(projectFile, title, null);
		setProject(this);
		this.languages = new ArrayList<Locale>();
		this.defaultLanguage = title.getLanguage();
		this.currentLanguage = defaultLanguage;
		this.languages.add(defaultLanguage);

		// Root-Knoten vom Inhaltsverzeichnis wird eh nicht angezeigt, Text
		// daher wurscht.
		this.tableOfContents = new TocItem("ROOT", this); //$NON-NLS-1$
		this.sceneGraph = new Graph(title, this);
		this.mediaObjects = new BeanList<IAbstractBean>(this);
		this.globalAnnotations = new BeanList<INodeAnnotationLeaf>(this);

		// Eintraege fuer alle QuizContainer
		createQuizBeanEntries();

		openProjectSetData();
		setDefaultQuizUser();
	}
	
	/**
	 * Returns the <code>NodeIDGenerator</code> associated with this project.
	 * This generator has to be used to create unique identifiers for the scene
	 * graph nodes of the current project.
	 * @return generator for unique node IDs
	 */
	public NodeIDGenerator getNodeIDGen() {
		return nodeIdGen;
	}

	private void createQuizBeanEntries() {
		this.quizKeys = new LinkedList<Integer>();
		this.quizBeans = new LinkedList<BeanList<? extends IQuizBean>>();
		quizBeans.add(new BeanList<AdditionalInfo>(this));
		quizBeans.add(new BeanList<Answer>(this));
		quizBeans.add(new BeanList<Category>(this));
		quizBeans.add(new BeanList<Condition>(this));
		quizBeans.add(new BeanList<Edge>(this));
		quizBeans.add(new BeanList<Node>(this));
		quizBeans.add(new BeanList<Question>(this));
		quizBeans.add(new BeanList<Test>(this));
		quizBeans.add(new BeanList<User>(this));

		for (int i = 0; i <= 8; i++) {
			quizKeys.add(new Integer(0));
		}
	}

	private void setDefaultQuizUser() {
		@SuppressWarnings("unchecked")
		BeanList<User> usrLst = (BeanList<User>) quizBeans.get(8);

		User usr = new User(project, "admin", "admin", "admin", "admin",
				"admin");
		usrLst.add(usr);
		UserManager.getInstance().setBeanList(usrLst);
	}

	@SuppressWarnings("unchecked")
	public void openProjectSetData() {

		// !!!BUGFIX!!!
		//
		// Überprüft ob eine Liste bzw. Manager nicht erzeugt wurde. Ist nur
		// dann der Fall
		// wenn ein altes Projekt geladen wurde, bei dem noch keine Quizdaten
		// gespeichert wurden!
		//
		boolean createDefaultUser = false;
		if (quizBeans == null) {
			createQuizBeanEntries();
			createDefaultUser = true;
		}

		AdditionalInfoManager.createInstance(this);
		BeanList<AdditionalInfo> lst0 = (BeanList<AdditionalInfo>) quizBeans
				.get(0);
		AdditionalInfoManager.getInstance().setBeanList(lst0);
		AdditionalInfoManager.getInstance().setKey(quizKeys.get(0));

		AnswerManager.createInstance(this);
		BeanList<Answer> lst1 = (BeanList<Answer>) quizBeans.get(1);
		AnswerManager.getInstance().setBeanList(lst1);
		// AnswerManager.getInstance().setKey(quizKeys.get(1));

		CategoryManager.createInstance(this);
		BeanList<Category> lst2 = (BeanList<Category>) quizBeans.get(2);
		CategoryManager.getInstance().setBeanList(lst2);
		CategoryManager.getInstance().setKey(quizKeys.get(2));

		ConditionManager.createInstance(this);
		BeanList<Condition> lst3 = (BeanList<Condition>) quizBeans.get(3);
		ConditionManager.getInstance().setBeanList(lst3);
		ConditionManager.getInstance().setKey(quizKeys.get(3));

		EdgeManager.createInstance(this);
		BeanList<Edge> lst4 = (BeanList<Edge>) quizBeans.get(4);
		EdgeManager.getInstance().setBeanList(lst4);
		// EdgeManager.getInstance().setKey(quizKeys.get(4));

		NodeManager.createInstance(this);
		BeanList<Node> lst5 = (BeanList<Node>) quizBeans.get(5);
		NodeManager.getInstance().setBeanList(lst5);
		// NodeManager.getInstance().setKey(quizKeys.get(5));

		QuestionManager.createInstance(this);
		BeanList<Question> lst6 = (BeanList<Question>) quizBeans.get(6);
		QuestionManager.getInstance().setBeanList(lst6);
		// QuestionManager.getInstance().setKey(quizKeys.get(6));

		TestManager.createInstance(this);
		BeanList<Test> lst7 = (BeanList<Test>) quizBeans.get(7);
		TestManager.getInstance().setBeanList(lst7);
		// TestManager.getInstance().setKey(quizKeys.get(7));

		UserManager.createInstance(this);
		BeanList<User> lst8 = (BeanList<User>) quizBeans.get(8);
		UserManager.getInstance().setBeanList(lst8);
		UserManager.getInstance().setKey(quizKeys.get(8));

		if (createDefaultUser) {
			setDefaultQuizUser();
		}
	}

	/**
	 * Fuegt eine Sprache zum Projekt hinzu.
	 * 
	 * @param locale
	 *            Die Sprache
	 */
	public void addLanguage(Locale locale) {
		if (!languages.contains(locale)) {
			languages.add(locale);
			firePropertyChange(PROP_LANGUAGES, null, locale);
		}
	}

	/**
	 * Gibt eine Szene mit einem bestimmten Titel zurueck. Die Szene wird in
	 * allen Videos dieses Projekts gesucht.
	 * 
	 * @param title
	 *            Titel der Szene
	 * @param language
	 *            Sprache in der Titel vorhanden sein soll.
	 * @return Gefundene Szene oder null, falls keine Szene mit entsprechendem
	 *         Titel existiert.
	 */
	public Scene getScene(String title, Locale language) {
		Iterator<IAbstractBean> it = mediaObjects.iterator();
		while (it.hasNext()) {
			IAbstractBean mediaObj = it.next();
			if (mediaObj instanceof Video) {
				Video video = (Video) mediaObj;
				Scene scene = video.getScene(title, language);
				if (scene != null) {
					return scene;
				}
			}
		}
		return null;
	}

	/**
	 * @param langCode
	 */
	public void removeLanguage(String langCode) {
		// TODO: Vetoable machen?
		if (languages.remove(langCode)) {
			firePropertyChange(PROP_LANGUAGES, langCode, null);
		}
	}

	/**
	 * Getter of the property <tt>mediaObjects</tt>
	 * 
	 * @return Returns the mediaObjects.
	 * @uml.property name="mediaObjects"
	 */
	public BeanList<IAbstractBean> getMediaObjects() {
		return mediaObjects;
	}

	// ################################################################
	// TODO: Project - PropertyChanges etc
	// ################################################################

	/**
	 * Gibt die aktuelle verwendete Sprache des Projekts zurueck
	 */
	public Locale getCurrentLanguage() {
		return currentLanguage;
	}

	/**
	 * Setzt die aktuell zu verwendende Sprache dieses Projekts. Die Sprache
	 * muss dabei dem Projekt bereits hinzugefuegt worden sein.
	 * 
	 * @see #addLanguage(String)
	 * 
	 * @param currentLanguage
	 */
	public void setCurrentLanguage(Locale currentLanguage) {
		// TODO: Informiere Listener ueber PropertyChanges
		if (languages.contains(currentLanguage)) {
			this.currentLanguage = currentLanguage;
		}
	}

	/**
	 * Gibt die Standard-Sprache des Projekts zurueck
	 * 
	 * @return Returns the defaultLanguage.
	 * @uml.property name="defaultLanguage"
	 */
	public Locale getDefaultLanguage() {
		return defaultLanguage;
	}

	/**
	 * Setzt die Standard-Sprache des Projekts. oDie Sprache muss dabei dem
	 * Projekt bereits hinzugefuegt worden sein.
	 * 
	 * @see #addLanguage(String)
	 * 
	 * @param defaultLanguage
	 *            Die Standard-Sprache des Projekts.
	 * @uml.property name="defaultLanguage"
	 */
	public void setDefaultLanguage(Locale defaultLanguage) {
		// TODO: Informiere Listener ueber PropertyChanges
		if (languages.contains(currentLanguage)) {
			this.defaultLanguage = defaultLanguage;
		}
	}

	/**
	 * Getter of the property <tt>globalAnnotations</tt>
	 * 
	 * @return Returns the globalAnnotations.
	 * @uml.property name="globalAnnotations"
	 */
	public BeanList<INodeAnnotationLeaf> getGlobalAnnotations() {
		// TODO:Sollte hier ueberhaupt die Original-Liste zurueckgegeben werden?
		return globalAnnotations;
	}

	/**
	 * Getter of the property <tt>languages</tt>
	 * 
	 * @return Returns the languages.
	 * @uml.property name="languages"
	 */
	public List<Locale> getLanguages() {
		// TODO:Sollte hier ueberhaupt die Original-Liste zurueckgegeben werden?
		return languages;
	}

	/**
	 * Getter of the property <tt>tableOfContents</tt>
	 * 
	 * @return Returns the tableOfContents.
	 * @uml.property name="tableOfContents"
	 */
	public TocItem getTableOfContents() {
		return tableOfContents;
	}

	/**
	 * Getter of the property <tt>sceneGraph</tt>
	 * 
	 * @return Returns the sceneGraph.
	 * @uml.property name="sceneGraph"
	 */
	public Graph getSceneGraph() {
		return sceneGraph;
	}

	/**
	 * Gibt das Projekt-Unterverzeichnis fuer die Medien-Dateien zurueck
	 * 
	 * @return
	 */
	private File getSubdirectoryMedia() {
		File projectFile = getFile().getValue();
		File mediaDir = new File(projectFile.getParentFile(),
				SUBDIRECTORY_MEDIA);
		return mediaDir;
	}

	/**
	 * Gibt das Projekt-Unterverzeichnis fuer die Richtext-Dateien zurueck
	 * 
	 * @return
	 */
	public File getSubdirectoryRichtext() {
		File mediaDir = getSubdirectoryMedia();
		File richtextDir = new File(mediaDir, SUBDIRECTORY_RICHTEXT);
		return richtextDir;
	}

	/**
	 * Erstellt die Projekt-Verzeichnisstruktur.
	 * 
	 * @throws IOException
	 *             Falls ein Verzeichnis nicht erstellt werden konnte, dann wird
	 *             eine IOException geworfen. Die Message der Exception ist der
	 *             Pfad des Verzeichnisses das nicht erstellt werden konnte.
	 * 
	 */
	public void createProjectDirectoryStructure() throws IOException {
		File projectDir = getFile().getValue().getParentFile();
		File mediaDir = new File(projectDir, SUBDIRECTORY_MEDIA);
		File richTextDir = new File(mediaDir, SUBDIRECTORY_RICHTEXT);
		File audioDir = new File(mediaDir, SUBDIRECTORY_AUDIO);
		File videoDir = new File(mediaDir, SUBDIRECTORY_VIDEO);
		File pictureDir = new File(mediaDir, SUBDIRECTORY_PICTURE);
		if (!projectDir.mkdirs()) {
			throw new IOException(projectDir.getAbsolutePath());
		}
		if (!mediaDir.mkdir()) {
			throw new IOException(mediaDir.getAbsolutePath());
		}
		if (!richTextDir.mkdir()) {
			throw new IOException(richTextDir.getAbsolutePath());
		}
		if (!audioDir.mkdir()) {
			throw new IOException(audioDir.getAbsolutePath());
		}
		if (!videoDir.mkdir()) {
			throw new IOException(videoDir.getAbsolutePath());
		}
		if (!pictureDir.mkdir()) {
			throw new IOException(pictureDir.getAbsolutePath());
		}

	}

	public ProjectSettings getSettings() {
		return this.settings;
	}

	public void setSettings(ProjectSettings settings) {
		this.settings = settings;
	}

	/**
	 * Returns the list of files which have been created on the disk but are
	 * currently not used in the project. Those files will be deleted on the
	 * next project save operation.
	 * @return list of unused files
	 */
	public Set<File> getUnusedFiles() {
		return unusedFiles;
	}
	
	/**
	 * Deserializes the project and initializes transient fields as needed.
	 * @param in input stream containing the project's data
	 * @throws IOException if an I/O error occurs
	 * @throws ClassNotFoundException if a class of s serialized object could
	 * not be found
	 */
	private void readObject(java.io.ObjectInputStream in)
		     throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		unusedFiles = new HashSet<File>();
	}
	
	/**
	 * Returns an {@link File} object referencing the backup file
	 * for the current project which is written after each project change to
	 * to prevent data loss in case of crashes.
	 * @return the {@link File} object referencing the project backup file.
	 * @throws IOException  if an I/O error occurs
	 */
	public File getBackupFile() throws IOException{
		return new File(project.getFile().getValue().getCanonicalPath() + "." + Project.PROJECT_BACKUP_FILE_EXTENSION);
	}
}
