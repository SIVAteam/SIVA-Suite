package org.iviPro.editors.shotoverview;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.iviPro.actions.nondestructive.OpenInsertShotEditorAction;
import org.iviPro.editors.IAbstractEditor;
import org.iviPro.editors.sceneeditor.components.Messages;
import org.iviPro.scenedetection.sd_main.MiscOperations;
import org.iviPro.scenedetection.sd_main.Shot;
import org.iviPro.theme.Icons;
import org.iviPro.utils.ImageHelper;
import org.iviPro.scenedetection.shd_algorithm.Cut;
import org.iviPro.scenedetection.sd_main.CutTypes;
import org.iviPro.actions.undoable.NewSceneDetectionAction;

public class ShotOverviewEditor extends IAbstractEditor implements
		PropertyChangeListener {

	private static Logger logger = Logger.getLogger(ShotOverviewEditor.class);

	public static final String ID = ShotOverviewEditor.class.getName();

	private ShotOverviewEditorInput input;

	private static final int PADDING_OVERALL = 8;

	private static final int CANVAS_MARGING = 12;

	private static final int PADDING_LINE = 12;

	private static final int BUTTON_SIZE = 24;

	private final List<Image> startImgs;

	private final List<Image> endImgs;

	private Image mergeImage;

	private Image insertImage;

	private List<Shot> shotList;

	private List<Canvas> compList;

	private ScrolledComposite timeLineScrollPane;

	private Composite mainParent;

	private Slider slider;

	private int currentPage;

	private boolean openPage;

	private final ShotOverviewEditor ed;

	public ShotOverviewEditor() {
		super();
		this.mergeImage = Icons.MERGE_SHOT.getImage();
		this.insertImage = Icons.INSERT_SHOT.getImage();
		this.shotList = new LinkedList<Shot>();
		this.startImgs = new LinkedList<Image>();
		this.endImgs = new LinkedList<Image>();
		this.currentPage = 0;
		this.ed = this;
		this.openPage = false;
		logger.debug("Creating shotoverview editor.");
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
	}

	private void createTimeLineScrollPane(Composite parent) {
		timeLineScrollPane = new ScrolledComposite(parent, SWT.V_SCROLL
				| SWT.DOUBLE_BUFFERED);
		GridData grid = new GridData();
		grid.grabExcessHorizontalSpace = true;
		grid.grabExcessVerticalSpace = true;
		grid.horizontalAlignment = SWT.CENTER;
		grid.verticalAlignment = SWT.TOP;
		grid.minimumWidth = 840;
		grid.minimumHeight = 620;
		timeLineScrollPane.setLayoutData(grid);
		timeLineScrollPane.setLayout(new GridLayout(1, false));
		compList = generateCanvasList(shotList, timeLineScrollPane);

		timeLineScrollPane.setContent(compList.get(0));
		timeLineScrollPane.setExpandHorizontal(true);
		timeLineScrollPane.setExpandVertical(true);
	}

	public void createStartSceneDetectionButton(Composite parent) {
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
		startSceneDetection.setText("Szenenerkennung starten");
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
				if (openPage) {
					MessageDialog.openInformation(mainParent.getShell(),
							"Information",
							"Bitte alle Shoteditor Fenster schließen.");
				} else {
					ShotOverviewEditorInput input = (ShotOverviewEditorInput) getEditorInput();
					new NewSceneDetectionAction(ed, input.getWindow(), input
							.getSelectedVideo(), shotList, input.isParallel(),
							input.isMpeg7()).run();
				}
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
				if (event.detail == SWT.DRAG || event.detail == SWT.ARROW_DOWN
						|| event.detail == SWT.ARROW_UP
						|| event.detail == SWT.PAGE_UP
						|| event.detail == SWT.PAGE_DOWN) {
					int amountPages = (int) Math.ceil((double) (shotList.size() / 16.0));
					double jumper = slider.getMaximum() / (double) amountPages;
					int toPage = (int) Math.floor((double) slider
							.getSelection() / jumper);
					if (currentPage != toPage) {
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

		input = (ShotOverviewEditorInput) getEditorInput();
		List<Shot> shotList = input.getShotList();

		// Removes memory leaks
		this.shotList = new LinkedList<Shot>();
		for (int i = 0; i < shotList.size(); i++) {
			this.shotList.add(shotList.get(i).clone());
		}
		input.shotGarbageCollection();

		for (int i = 0; i < this.shotList.size(); i++) {
			ImageData data = MiscOperations.convertToSWT(this.shotList.get(i)
					.getStartImage());
			Image img = new Image(Display.getCurrent(), data.scaledTo(80, 80));
			startImgs.add(img);
			data = MiscOperations.convertToSWT(this.shotList.get(i)
					.getEndImage());
			img = new Image(Display.getCurrent(), data.scaledTo(80, 80));
			endImgs.add(img);
		}
		createStartSceneDetectionButton(comp);
		createTimeLineScrollPane(comp);
		createSlider();
	}

	private List<Canvas> generateCanvasList(final List<Shot> shots,
			Composite parent) {
		List<Canvas> compList = new LinkedList<Canvas>();
		int amountPages = (int) Math.ceil((double) (shots.size() / 16.0));
		for (int i = 0; i < amountPages; i++) {
			final int pageNr = i;
			final List<Shot> sub = new LinkedList<Shot>();
			if (shots.size() >= i * 16 + 16) {
				for (int j = i * 16; j < i * 16 + 16; j++) {
					sub.add(shots.get(j));
				}
			} else {
				for (int j = i * 16; j < shots.size(); j++) {
					sub.add(shots.get(j));
				}
			}

			ShotPage page = new ShotPage(parent, SWT.NONE, sub, i);
			for (int j = 0; j < sub.size(); j++) {
				final int position = j;
				Button button = new Button(page, SWT.PUSH);
				int widthm = (j % 4);
				int heightm = ((int) Math.floor(j / 4));
				button.setBounds(widthm
						* (2 * (80 + 2 * PADDING_OVERALL + BUTTON_SIZE / 4))
						+ CANVAS_MARGING + 77, heightm
						* (80 + 4 * PADDING_OVERALL + (PADDING_OVERALL / 2)
								+ PADDING_LINE + BUTTON_SIZE) + CANVAS_MARGING
						- PADDING_OVERALL + 20, BUTTON_SIZE, BUTTON_SIZE);
				button.setSize(29, 30);
				button.setImage(insertImage);
				button.addSelectionListener(new SelectionListener() {
					public void widgetSelected(SelectionEvent event) {
						if (!openPage) {
							openPage = true;
							new OpenInsertShotEditorAction(input.getWindow(),
									ed, input.getSelectedVideo(), sub.get(
											position).getStartTimeNano(), sub
											.get(position).getEndTimeNano(),
									sub.get(position).getStartFrame() - 1, sub
											.get(position).getShotId()).run();
						} else {
							MessageDialog.openInformation(
									mainParent.getShell(), "Information",
									"Das Fenster des zugehörigen Shots ist bereits offen!");
						}
					}

					public void widgetDefaultSelected(SelectionEvent event) {
					}
				});
			}
			for (int j = 0; j < sub.size(); j++) {
				if (sub.get(j).getShotId() != (shots.size() - 1)) {
					final int position = j;
					Button button = new Button(page, SWT.TOGGLE);
					int widthm = (j % 4);
					int heightm = ((int) Math.floor(j / 4));
					button.setBounds(
							widthm
									* (2 * (80 + 2 * PADDING_OVERALL + BUTTON_SIZE / 4))
									+ CANVAS_MARGING + 149, heightm
									* (80 + 4 * PADDING_OVERALL
											+ (PADDING_OVERALL / 2)
											+ PADDING_LINE + BUTTON_SIZE)
									+ CANVAS_MARGING - PADDING_OVERALL + 20,
							BUTTON_SIZE, BUTTON_SIZE);
					button.setSize(29, 30);
					button.setImage(mergeImage);
					button.addSelectionListener(new SelectionListener() {
						public void widgetSelected(SelectionEvent event) {
							if (!openPage) {
								int id = sub.get(position).getShotId();
								mergeShots(shots.get(id), shots.get(id + 1));
								int toPage = 0;
								if (sub.size() > 1) {
									toPage = pageNr;
								} else {
									toPage = pageNr - 1;
								}
								setPreviousPage(toPage);
							} else {
								MessageDialog.openInformation(
										mainParent.getShell(), "Information",
										"Bitte zuerst die Fenster der zu bearbeitenden Shots schließen!");
							}
						}

						public void widgetDefaultSelected(SelectionEvent event) {
						}
					});
				}
			}
			for (int j = 0; j < sub.size(); j++) {
				Label label = new Label(page, SWT.NONE);
				int widthm = (j % 4);
				int heightm = ((int) Math.floor(j / 4));
				label.setText("Shot " + (sub.get(j).getShotId() + 1));
				label.setFont(new Font(Display.getCurrent(), "Arial", 13,
						SWT.NORMAL));
				label.setBounds(widthm
						* (2 * (80 + 2 * PADDING_OVERALL + BUTTON_SIZE / 4))
						+ CANVAS_MARGING + 10, heightm
						* (80 + 4 * PADDING_OVERALL + (PADDING_OVERALL / 2)
								+ PADDING_LINE + BUTTON_SIZE) + CANVAS_MARGING
						+ 22 - PADDING_OVERALL,
						label.computeSize(SWT.DEFAULT, SWT.DEFAULT).x,
						label.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
			}
			compList.add(page);
		}
		return compList;
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	@Override
	public Image getDefaultImage() {
		return Icons.SHOT_EDITOR.getImage();
	}

	private void setPreviousPage(int index) {
		this.slider.setMaximum(10 * compList.size());
		slider.setSelection(10 * index);
		for (int i = 0; i < compList.size(); i++) {
			timeLineScrollPane.setContent(compList.get(i));
		}
		timeLineScrollPane.setContent(compList.get(index));
	}

	private void mergeShots(Shot shot1, Shot shot2) {
		for (int i = 0; i < compList.size(); i++) {
			compList.get(i).dispose();
		}
		shot1.setEndImage(shot2.getEndImage());
		shot1.setEndFrame(shot2.getEndFrame());
		shot1.setEndTimeNano(shot2.getEndTimeNano());
		startImgs.remove(shot2.getShotId());
		endImgs.remove(shot1.getShotId());
		shotList.remove(shot2.getShotId());
		// Update IDs
		for (int i = 0; i < shotList.size(); i++) {
			shotList.get(i).setShotID(i);
		}
		// Dispose every canvas!
		for (int i = 0; i < compList.size(); i++) {
			compList.get(i).dispose();
		}
		compList = generateCanvasList(shotList, timeLineScrollPane);
	}

	public void setCut(Cut cut) {
		for (int i = 0; i < compList.size(); i++) {
			compList.get(i).dispose();
		}
		int imagePosition = 0;
		for (int i = 0; i < shotList.size(); i++) {
			if (cut.getCutFrameNr() > shotList.get(i).getStartFrame()
					&& cut.getCutFrameNr() < shotList.get(i).getEndFrame()) {
				Shot newshot = new Shot(
						CutTypes.values()[cut.getCutCategory()], shotList
								.get(i).getCut2Type(), cut.getCutFrameNr(),
						shotList.get(i).getEndFrame(), cut.getSecondTime(),
						shotList.get(i).getEndTimeObject());
				shotList.get(i).setEndFrame(cut.getCutFrameNr() - 1);
				shotList.get(i).setEndImage(cut.getImage(false));
				shotList.get(i).setEndTimeNano(
						cut.getFirstTime().getNanoseconds());
				shotList.add(i + 1, newshot);
				imagePosition = i;
				break;
			}
		}
		ImageData data = MiscOperations.convertToSWT(cut.getImage(false));
		Image img = new Image(Display.getCurrent(), data.scaledTo(80, 80));
		startImgs.add(imagePosition + 1, img);
		data = MiscOperations.convertToSWT(cut.getImage(true));
		img = new Image(Display.getCurrent(), data.scaledTo(80, 80));
		endImgs.add(imagePosition, img);

		// Update IDs
		for (int i = 0; i < shotList.size(); i++) {
			shotList.get(i).setShotID(i);
		}
		// Dispose every canvas!
		for (int i = 0; i < compList.size(); i++) {
			compList.get(i).dispose();
		}
		compList = generateCanvasList(shotList, timeLineScrollPane);
		setPreviousPage(0);
	}

	public void closeWindow() {
		openPage = false;
	}

	class ShotPage extends Canvas implements PaintListener {

		private List<Shot> sub;

		private int pageNumber;

		ShotPage(Composite parent, int style, List<Shot> sub,
				int pageNumber) {
			super(parent, style);
			this.sub = sub;
			this.pageNumber = pageNumber;
			GridData grid = new GridData();
			grid.horizontalAlignment = SWT.CENTER;
			grid.grabExcessHorizontalSpace = true;
			grid.grabExcessVerticalSpace = true;
			grid.minimumWidth = 840;
			grid.minimumHeight = 620;
			this.setSize(840, 620);
			this.setLayoutData(grid);
			this.addPaintListener(this);
		}

		@Override
		public void paintControl(PaintEvent e) {
			System.out.println("Ich zeichne eins neu! " +pageNumber);
			for (int j = 0; j < sub.size(); j++) {
				int widthm = (j % 4);
				int heightm = ((int) Math.floor(j / 4));
				int x = widthm
						* (2 * (80 + 2 * PADDING_OVERALL + BUTTON_SIZE / 4))
						+ PADDING_OVERALL + CANVAS_MARGING;
				int y = heightm
						* (80 + 4 * PADDING_OVERALL + (PADDING_OVERALL / 2)
								+ PADDING_LINE + BUTTON_SIZE) + 2
						* PADDING_OVERALL + (PADDING_OVERALL / 2) + BUTTON_SIZE
						+ CANVAS_MARGING;
				Rectangle rect = new Rectangle(widthm
						* (2 * (80 + 2 * PADDING_OVERALL + BUTTON_SIZE / 4))
						+ CANVAS_MARGING, heightm
						* (80 + 4 * PADDING_OVERALL + (PADDING_OVERALL / 2)
								+ PADDING_LINE + BUTTON_SIZE) + CANVAS_MARGING,
						160 + 3 * PADDING_OVERALL, 80 + 3 * PADDING_OVERALL
								+ (PADDING_OVERALL / 2) + BUTTON_SIZE);
				e.gc.setForeground(new Color(Display.getCurrent(), new RGB(185,
						185, 185)));
				e.gc.drawRectangle(rect);
				e.gc.drawImage(startImgs.get(pageNumber * 16 + j), x, y);
				e.gc.drawImage(endImgs.get(pageNumber * 16 + j), x + 80
						+ PADDING_OVERALL, y);
			}
		}
	}
}
