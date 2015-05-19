package org.iviPro.editors.shotoverview;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.LineAttributes;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.iviPro.actions.undoable.SaveSceneDetectionResults;
import org.iviPro.editors.IAbstractEditor;
import org.iviPro.editors.sceneeditor.components.Messages;
import org.iviPro.scenedetection.sd_main.MiscOperations;
import org.iviPro.scenedetection.sd_main.Scene;
import org.iviPro.scenedetection.sd_main.Shot;
import org.iviPro.theme.Icons;
import org.iviPro.utils.ImageHelper;

public class SceneOverviewEditor extends IAbstractEditor implements
		PropertyChangeListener {

	private static Logger logger = Logger.getLogger(SceneOverviewEditor.class);

	public static final String ID = SceneOverviewEditor.class.getName();

	private static final int BUTTON_SIZE = 24;

	private static final int PADDING_OVERALL = 8;

	private static final int CANVAS_MARGING = 12;

	private static final int PADDING_LINE = 12;

	private static int BORDERDIST = 15;

	private SceneOverviewEditorInput input;

	private Composite mainParent;

	private List<Scene> sceneList;

	private List<Canvas> compList;

	private ScrolledComposite timeLineScrollPane;

	private Slider slider;

	private int currentPage;

	private Image mergeImage;

	private Image insertImage;

	private List<Text> textList;

	private final SceneOverviewEditor ed;

	public SceneOverviewEditor() {
		super();
		this.ed = this;
		this.mergeImage = Icons.MERGE_SHOT.getImage();
		this.insertImage = Icons.INSERT_SHOT.getImage();
		this.textList = new LinkedList<Text>();
		currentPage = 0;
		logger.debug("Creating sceneverview editor.");
	}

	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		// TODO Auto-generated method stub

	}

	private void createTimeLineScrollPane(Composite parent) {
		timeLineScrollPane = new ScrolledComposite(parent, SWT.V_SCROLL
				| SWT.DOUBLE_BUFFERED);
		GridData grid = new GridData();
		grid.grabExcessHorizontalSpace = true;
		grid.grabExcessVerticalSpace = true;
		grid.horizontalAlignment = SWT.CENTER;
		grid.verticalAlignment = SWT.TOP;
		grid.minimumWidth = 880;
		grid.minimumHeight = 620;
		timeLineScrollPane.setLayoutData(grid);
		timeLineScrollPane.setLayout(new GridLayout(1, false));
		compList = generateCanvasList(sceneList, timeLineScrollPane);

		timeLineScrollPane.setContent(compList.get(0));
		timeLineScrollPane.setExpandHorizontal(true);
		timeLineScrollPane.setExpandVertical(true);
	}

	public void createSaveButton(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(1, false));
		GridData grid = new GridData();
		grid.grabExcessHorizontalSpace = true;
		grid.grabExcessVerticalSpace = true;
		grid.horizontalAlignment = SWT.CENTER;
		grid.verticalAlignment = SWT.TOP;
		comp.setLayoutData(grid);
		final Button startSceneDetection = new Button(comp, SWT.CENTER);
		grid = new GridData();
		grid.grabExcessHorizontalSpace = true;
		grid.grabExcessVerticalSpace = true;
		grid.horizontalAlignment = SWT.CENTER;
		grid.verticalAlignment = SWT.TOP;
		startSceneDetection.setLayoutData(grid);
		startSceneDetection.setText("Szenen speichern");
		startSceneDetection
				.setToolTipText(Messages.SceneDefineWidget_Tooltip_SaveCreateButton);
		GridData savecreButtonGD = new GridData();
		savecreButtonGD.horizontalSpan = 3;
		startSceneDetection.setLayoutData(savecreButtonGD);
		// Bild für den Button
		ImageHelper.setButtonImage(startSceneDetection, Icons.ACTION_SCENE_NEW);
		startSceneDetection
				.setToolTipText(Messages.SceneDefineWidget_SaveCreateButton);

		startSceneDetection.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event e) {
				for (int i = 0; i < sceneList.size(); i++) {
					sceneList.get(i).setName(textList.get(i).getText());
				}
				// Step Seven: Mpeg 7 export
				if (input.getMpeg7()) {
					input.getMpeg7Exporter().setScenes(sceneList);
					input.getMpeg7Exporter().writeXmlFile(generateXMLFileName(input.getMediaLocator()));
				}
				new SaveSceneDetectionResults(ed, input.getWindow(), input
						.getVideo(), sceneList).run();
			}
		});
	}

	private void createSlider() {
		Composite slidercomp = new Composite(mainParent, SWT.RIGHT);
		slidercomp.setLayout(new GridLayout(1, false));
		slider = new Slider(slidercomp, SWT.VERTICAL);
		slider.setMaximum(10 * compList.size());
		slider.setIncrement(10);
		GridData slidercompgrid = new GridData();
		slidercompgrid.grabExcessVerticalSpace = true;
		slidercompgrid.horizontalAlignment = SWT.RIGHT;
		slidercompgrid.verticalAlignment = SWT.FILL;
		slidercomp.setLayoutData(slidercompgrid);

		GridData slidergrid = new GridData();
		slidergrid.verticalAlignment = SWT.FILL;
		slidergrid.grabExcessVerticalSpace = true;
		slider.setLayoutData(slidergrid);
		slider.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				final int amountPages = compList.size();
				if (event.detail == SWT.DRAG || event.detail == SWT.ARROW_DOWN
						|| event.detail == SWT.ARROW_UP
						|| event.detail == SWT.PAGE_UP
						|| event.detail == SWT.PAGE_DOWN) {
					double jumper = slider.getMaximum() / (double) amountPages;

					int toPage = (int) Math.floor((double) slider
							.getSelection() / jumper);
					if (currentPage != toPage) {
						// System.out
						// .println("HIER BIN ICH AUF JEDENFALL DRINNEN MOTHAFUCKA!!!!");
						currentPage = toPage;
						timeLineScrollPane.setContent(compList.get(toPage));
						timeLineScrollPane.redraw();
					}
				}
			}
		});
	}

	@Override
	protected void createPartControlImpl(Composite parent) {
		input = (SceneOverviewEditorInput) getEditorInput();
		sceneList = input.getSceneList();
		mainParent = parent;
		Composite comp = new Composite(mainParent, SWT.NONE);
		GridLayout gridL = new GridLayout();
		gridL.numColumns = 1;
		GridData grid = new GridData();
		grid.grabExcessHorizontalSpace = true;
		grid.grabExcessVerticalSpace = true;
		grid.horizontalAlignment = SWT.CENTER;
		grid.verticalAlignment = SWT.TOP;
		comp.setLayout(gridL);
		comp.setLayoutData(grid);
		GridLayout mainParentGridLayout = new GridLayout();
		mainParentGridLayout.numColumns = 2;
		mainParent.setLayout(mainParentGridLayout);

		for (int i = 0; i < this.sceneList.size(); i++) {
			int shotNr = sceneList.get(i).getNumberofShots();
			for (int j = 0; j < shotNr; j++) {
				Shot shot = sceneList.get(i).getShotWithNr(j);
				ImageData data = MiscOperations.convertToSWT(shot
						.getStartImage());
				Image img = new Image(Display.getCurrent(), data.scaledTo(80,
						80));
				sceneList.get(i).setAddSwtStartImage(img);
				data = MiscOperations.convertToSWT(shot.getEndImage());
				img = new Image(Display.getCurrent(), data.scaledTo(80, 80));
				sceneList.get(i).setAddSwtEndImage(img);
			}
		}
		createSaveButton(comp);
		createTimeLineScrollPane(comp);
		createSlider();
	}

	private List<Canvas> generateCanvasList(final List<Scene> scenes,
			Composite parent) {
		List<Canvas> compList = new LinkedList<Canvas>();

		int line = 1;
		final List<SceneObjectContainer> sub = new LinkedList<SceneObjectContainer>();
		ScenePage page = null;
		boolean isLastOverlap = false;
		int startShot = 0;
		int endShot = 0;
		int shotWorkedOff = 0;

		for (int i = 0; i < sceneList.size(); i++) {
			int amountShotsInScene = sceneList.get(i).getNumberofShots();
			int neededLines = ((int) Math.ceil((double) amountShotsInScene
					/ (double) 4));

			if (line == 5) {
				page = new ScenePage(parent, SWT.NONE, sub, compList.size());
				createInsertButton(page, compList.size());
				compList.add(page);
				page = null;
				line = 1;
				sub.clear();
			}

			if ((line + neededLines) <= 5) {
				sub.add(new SceneObjectContainer(0, amountShotsInScene,
						sceneList.get(i)));
				line += neededLines;
				isLastOverlap = false;
			} else {
				startShot = 0;
				endShot = 0;
				while (line + neededLines > 5) {
					endShot = 16 - 4 * line + 4;
					shotWorkedOff += endShot;
					sub.add(new SceneObjectContainer(startShot, shotWorkedOff,
							sceneList.get(i)));
					page = new ScenePage(parent, SWT.NONE, sub, compList.size());
					createInsertButton(page, compList.size());
					sub.clear();
					compList.add(page);
					page = null;
					line = 1;
					neededLines -= (shotWorkedOff - startShot) / 4;
					startShot = shotWorkedOff;
					isLastOverlap = true;
				}
				sub.add(new SceneObjectContainer(startShot, sceneList.get(i)
						.getNumberofShots(), sceneList.get(i)));
				line = (int) Math.ceil((double) (sceneList.get(i)
						.getNumberofShots() - startShot) / 4) + 1;
				// System.out.println("startShot: " + startShot + " endshot: "
				// + (sceneList.get(i).getNumberofShots()) + "LINE: "
				// + line);
				shotWorkedOff = 0;
			}
		}
		if (isLastOverlap) {
			sub.clear();
			sub.add(new SceneObjectContainer(startShot, sceneList.get(
					sceneList.size() - 1).getNumberofShots(), sceneList
					.get(sceneList.size() - 1)));
		}
		page = new ScenePage(parent, SWT.NONE, sub, compList.size());
		createInsertButton(page, compList.size());

		compList.add(page);
		return compList;
	}

	private void createInsertButton(ScenePage parent, final int pageNumber) {
		final List<SceneObjectContainer> tempsub = parent.sub;
		for (int j = 0; j < tempsub.size(); j++) {
			final int sceneIt = j;
			int line = 0;
			int diff = 0;
			for (int i = 0; i < j; i++) {
				diff = tempsub.get(i).endPosition
						- tempsub.get(i).startPosition;
				if (diff >= 4) {
					int temp = (int) Math.ceil((double) diff / (double) 4);
					line += (temp - 1);
				}
			}
			int addSceneHeight = (j * 155) + (line * 130);
			int start = tempsub.get(j).startPosition;
			int end = tempsub.get(j).endPosition;
			int x1 = 207;
			for (int k = start; k < end; k++) {
				if (k < tempsub.get(j).getScene().getNumberofShots() - 1) {
					final int shotPos = k;
					int multiplier = shotPos % 4;
					int heightm = ((int) Math.floor((k - start) / 4));
					int y1 = addSceneHeight
							+ heightm
							* (80 + 4 * PADDING_OVERALL + (PADDING_OVERALL / 2) + PADDING_LINE);
					Button button = new Button(parent, SWT.PUSH);
					button.setBounds(x1 + multiplier * 204, y1 + 8,
							BUTTON_SIZE, BUTTON_SIZE);
					button.setSize(29, 30);
					button.setImage(insertImage);
					button.addSelectionListener(new SelectionListener() {
						public void widgetSelected(SelectionEvent event) {
							insertScene(tempsub.get(sceneIt).getScene()
									.getSceneId(), shotPos);
							setPreviousPage(pageNumber);
						}

						public void widgetDefaultSelected(SelectionEvent event) {
						}
					});
				}
				// MergeButton
				if (k + 1 == end
						&& k + 1 == tempsub.get(j).getScene()
								.getNumberofShots()) {
					final int shotPos = k;
					int heightm = ((int) Math.floor((k - start) / 4));
					int multiplier = shotPos % 4;
					int y1 = addSceneHeight
							+ heightm
							* (80 + 4 * PADDING_OVERALL + (PADDING_OVERALL / 2) + PADDING_LINE);
					Button button = new Button(parent, SWT.PUSH);
					button.setBounds(x1 + multiplier * 204 + 15, y1 + 8 + 60,
							BUTTON_SIZE, BUTTON_SIZE);
					button.setSize(29, 30);
					button.setImage(mergeImage);
					button.addSelectionListener(new SelectionListener() {
						public void widgetSelected(SelectionEvent event) {
							mergeScenes(tempsub.get(sceneIt).getScene()
									.getSceneId());
							setPreviousPage(pageNumber);
						}

						public void widgetDefaultSelected(SelectionEvent event) {
						}
					});
				}
			}
			// Text Input
			Text sceneName = new Text(parent, SWT.SINGLE | SWT.BORDER);
			textList.add(tempsub.get(j).getScene().getSceneId(), sceneName);
			sceneName
					.setText("Szene " + tempsub.get(j).getScene().getSceneId());
			sceneName.setBounds(27, addSceneHeight + 12,
					sceneName.computeSize(SWT.DEFAULT, SWT.DEFAULT).x + 5,
					sceneName.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		}
	}

	private void insertScene(int sceneId, int shotPosition) {
		for (int i = 0; i < compList.size(); i++) {
			compList.get(i).dispose();
		}
		Scene newScene = new Scene(sceneId + 1);
		int start = shotPosition + 1;
		int end = sceneList.get(sceneId).getNumberofShots();
		sceneList.get(sceneId).test();
		for (int i = start; i < end; i++) {
			newScene.addShots(sceneList.get(sceneId).getShotWithNr(i), false);
			newScene.setAddSwtStartImage(sceneList.get(sceneId)
					.getStartSwtImage(i));
			newScene.setAddSwtEndImage(sceneList.get(sceneId).getEndSwtImage(i));
		}

		for (int i = start; i < end; i++) {
			sceneList.get(sceneId).removeShot(shotPosition + 1);
		}

		sceneList.add(sceneId + 1, newScene);
		// Update scene ids
		for (int i = sceneId + 2; i < sceneList.size(); i++) {
			sceneList.get(i).incrementId();
		}
		compList = generateCanvasList(sceneList, timeLineScrollPane);
	}

	private void setPreviousPage(int index) {
		slider.setMaximum(10 * compList.size());
		slider.setSelection(10 * index);
		for (int i = 0; i < compList.size(); i++) {
			timeLineScrollPane.setContent(compList.get(i));
		}
		timeLineScrollPane.setContent(compList.get(index));
	}

	private void mergeScenes(int id) {
		for (int i = 0; i < compList.size(); i++) {
			compList.get(i).dispose();
		}
		Scene toAdd = sceneList.get(id + 1);
		int numberOfShots = toAdd.getNumberofShots();
		for (int i = 0; i < numberOfShots; i++) {
			sceneList.get(id).addShots(toAdd.getShotWithNr(i), false);
			sceneList.get(id).setAddSwtStartImage(toAdd.getStartSwtImage(i));
			sceneList.get(id).setAddSwtEndImage(toAdd.getEndSwtImage(i));
		}
		sceneList.remove(id + 1);

		// Update scene ids
		for (int i = id + 1; i < sceneList.size(); i++) {
			sceneList.get(i).decrementId();
		}
		compList = generateCanvasList(sceneList, timeLineScrollPane);
	}

	@Override
	public void doSave(IProgressMonitor arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
	}

	@Override
	public boolean isDirty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	class ScenePage extends Canvas implements PaintListener {

		private List<SceneObjectContainer> sub;

		ScenePage(Composite parent, int style, List<SceneObjectContainer> sub,
				int page) {
			super(parent, style);
			this.sub = new LinkedList<SceneObjectContainer>();
			for (int i = 0; i < sub.size(); i++) {
				this.sub.add(sub.get(i).clone());
				for (int j = 0; j < sub.get(i).getScene().getNumberofShots(); j++) {
					this.sub.get(this.sub.size() - 1)
							.getScene()
							.setAddSwtStartImage(
									sub.get(i).getScene().getStartSwtImage(j));
					this.sub.get(this.sub.size() - 1)
							.getScene()
							.setAddSwtEndImage(
									sub.get(i).getScene().getEndSwtImage(j));
				}
			}
			GridData grid = new GridData();
			grid.horizontalAlignment = SWT.CENTER;
			grid.grabExcessHorizontalSpace = true;
			grid.grabExcessVerticalSpace = true;
			grid.minimumWidth = 880;
			grid.minimumHeight = 620;
			this.setSize(880, 620);
			this.setLayoutData(grid);
			this.addPaintListener(this);
		}

		@Override
		public void paintControl(PaintEvent e) {
			for (int j = 0; j < sub.size(); j++) {
				int line = 0;
				int diff = 0;
				for (int i = 0; i < j; i++) {
					diff = sub.get(i).endPosition - sub.get(i).startPosition;
					if (diff >= 4) {
						int temp = (int) Math.ceil((double) diff / (double) 4);
						line += (temp - 1);
					}
				}
				int addSceneHeight = (j * 155) + (line * 130);
				int start = sub.get(j).startPosition;
				int end = sub.get(j).endPosition;

				int x1 = 10;
				int y1 = addSceneHeight;
				int difference = sub.get(j).endPosition
						- sub.get(j).startPosition;
				int additionalSpace = (int) Math.ceil((double) difference
						/ (double) 4);

				// Fill Color
				if (sub.get(j).getScene().getSceneId() % 2 == 0) {
					e.gc.setBackground(new Color(Display.getCurrent(), 210,
							210, 210));
					e.gc.fillRectangle(x1, y1, 870, additionalSpace * 128 + 15);
				} else {
					e.gc.setBackground(new Color(Display.getCurrent(), 230,
							230, 230));
					e.gc.fillRectangle(x1, y1, 870, additionalSpace * 128 + 15);
				}

				// LINES
				LineAttributes standard = e.gc.getLineAttributes();

				if (sub.get(j).startPosition > 0) {
					e.gc.setLineAttributes(new LineAttributes(1, SWT.CAP_FLAT,
							SWT.JOIN_MITER, SWT.LINE_DASHDOTDOT, null, 1, 0));
				}

				e.gc.drawLine(x1, y1, x1 + 869, y1);

				e.gc.setLineAttributes(standard);

				e.gc.drawLine(x1 + 869, y1, x1 + 869, y1 + additionalSpace
						* 128 + 15);

				if (sub.get(j).getScene().getNumberofShots() > sub.get(j).endPosition) {
					e.gc.setLineAttributes(new LineAttributes(1, SWT.CAP_FLAT,
							SWT.JOIN_MITER, SWT.LINE_DASHDOTDOT, null, 1, 0));
				}
				e.gc.drawLine(x1, y1 + additionalSpace * 128 + 15, x1 + 869, y1
						+ additionalSpace * 128 + 15);
				e.gc.setLineAttributes(standard);

				e.gc.drawLine(x1, y1, x1, y1 + additionalSpace * 128 + 15);

				for (int k = start; k < end; k++) {
					int widthm = ((k - start) % 4);
					int heightm = ((int) Math.floor((k - start) / 4));
					int x = BORDERDIST
							+ widthm
							* (2 * (80 + 2 * PADDING_OVERALL + BUTTON_SIZE / 4))
							+ PADDING_OVERALL + CANVAS_MARGING;
					int y = addSceneHeight
							+ 2
							* BORDERDIST
							+ heightm
							* (80 + 4 * PADDING_OVERALL + (PADDING_OVERALL / 2) + PADDING_LINE)
							+ 2 * PADDING_OVERALL + (PADDING_OVERALL / 2)
							+ CANVAS_MARGING;
					Rectangle rect = new Rectangle(
							BORDERDIST
									+ widthm
									* (2 * (80 + 2 * PADDING_OVERALL + BUTTON_SIZE / 4))
									+ CANVAS_MARGING,
							addSceneHeight
									+ 2
									* BORDERDIST
									+ heightm
									* (80 + 4 * PADDING_OVERALL
											+ (PADDING_OVERALL / 2) + PADDING_LINE)
									+ CANVAS_MARGING,
							160 + 3 * PADDING_OVERALL, 80 + PADDING_OVERALL
									+ (PADDING_OVERALL / 2));

					e.gc.drawRectangle(rect);

					e.gc.drawImage(sub.get(j).getScene().getStartSwtImage(k),
							x, y - 14);
					e.gc.drawImage(sub.get(j).getScene().getEndSwtImage(k), x
							+ 80 + PADDING_OVERALL, y - 14);
				}
			}
		}
	}

	private class SceneObjectContainer implements Cloneable {

		private int startPosition;

		private int endPosition;

		private Scene scene;

		private SceneObjectContainer(int startPosition, int endPosition,
				Scene scene) {
			this.startPosition = startPosition;
			this.endPosition = endPosition;
			this.scene = scene;
		}

		public SceneObjectContainer clone() {
			SceneObjectContainer clone = new SceneObjectContainer(
					startPosition, endPosition, scene.clone());
			return clone;
		}

		Scene getScene() {
			return scene;
		}
	}

	private File generateXMLFileName(String ml) {
		File file;
		Calendar c = Calendar.getInstance();
		int day = c.get(Calendar.DAY_OF_MONTH);
		int month = c.get(Calendar.MONTH) + 1;
		int year = c.get(Calendar.YEAR);
		int h = c.get(Calendar.HOUR_OF_DAY);
		int m = c.get(Calendar.MINUTE);
		int s = c.get(Calendar.SECOND);

		String dd = (day < 10) ? "0" + day : "" + day;
		String mm = (month < 10) ? "0" + month : "" + month;
		String hh = (h < 10) ? "0" + h : "" + h;
		String minmin = (m < 10) ? "0" + m : "" + m;
		String ss = (s < 10) ? "0" + s : "" + s;

		String tmp = ml;
		String dateiName = tmp.substring(tmp.lastIndexOf("\\") + 1,
				tmp.lastIndexOf("."));
		System.out.println("Dateiname: " + dateiName);
		file = new File("MP7_" + dateiName + "_" + dd + "_" + mm + "_" + year
				+ "_" + hh + "_" + minmin + "_" + ss + ".xml");
		return file;
	}
}
