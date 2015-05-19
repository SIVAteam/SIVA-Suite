package org.iviPro.projectsettings;

import java.math.BigDecimal;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class LayoutEditor extends Composite {

	private static Logger logger = Logger.getLogger(LayoutEditor.class);
	
	private LayoutPane pane;
	private int sizeHeight;
	private int sizeWidth;
	private Input input;

	private float areaLeftWidth;
	private float areaTopHeight;
	private float areaBottomHeight;
	private float areaRightWidth;
	
	private Group prGroup;

	public LayoutEditor(Composite parent, int style) {
		super(parent, style);

		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginLeft = 10;
		gridLayout.marginTop = 10;
		gridLayout.marginRight = 30;
		setLayout(gridLayout);
		input = new Input(this, SWT.None);
		pane = new LayoutPane(prGroup, SWT.None, this);
	}

	public void refreshFields() {
		input.setFields();
	}

	public float getWHRatio() {
		return sizeWidth / sizeHeight;
	}

	public void setLoadedValues(int sizeWidth, int sizeHeight,
			float areaLeftWidth, float areaTopHeight, float areaBottomHeight,
			float areaRightWidth) {
		this.sizeWidth = sizeWidth;
		this.sizeHeight = sizeHeight;
		this.areaLeftWidth = areaLeftWidth;
		this.areaTopHeight = areaTopHeight;
		this.areaBottomHeight = areaBottomHeight;
		this.areaRightWidth = areaRightWidth;
		input.setWH(sizeWidth, sizeHeight);
		pane.setLoadedValues(areaLeftWidth, areaTopHeight, areaBottomHeight,
				areaRightWidth);
		
		pane.refreshSize(sizeWidth, sizeHeight);
	}

	public void init() {
		this.sizeWidth = 800;
		this.sizeHeight = 600;
		input.setWH(sizeWidth, sizeHeight);
		input.currentEdit = null;
		input.calculateValues();
		if (pane != null) {
			pane.setInitialValues();
		}
		input.setFields();
	}

	private class Input extends Composite {

		private VerifyListener inputVerify = verifyInputs();
		private ModifyListener inputModify = modifyValues();
		private boolean settingFields = false;
		private Text currentEdit = null;

		private Text leftWidth;
		private Text topHeight;
		private Text bottomHeight;
		private Text rightWidth;
		private Text completeWidthText;
		private Text completeHeightText;

		public Input(Composite parent, int style) {
			super(parent, style);
			setSize(500, 100);
			GridLayout layout = new GridLayout(1, true);
			setLayout(layout);
			GridData data = new GridData();
			data.widthHint = 50;
			
			GridData labelData = new GridData();
			labelData.widthHint = 100;
			
			GridData secondInputFieldsGridData = new GridData();
			secondInputFieldsGridData.horizontalIndent = 50;
			secondInputFieldsGridData.widthHint = labelData.widthHint;
			
			Group whGroup = new Group(this, SWT.LEFT);
			GridData whgGD = new GridData(SWT.FILL, SWT.FILL, true, true);
			whGroup.setLayoutData(whgGD);
			whGroup.setText(Messages.LayoutEditor_WHGroup);
			whGroup.setLayout(new GridLayout(1, false));
			
			Composite whFields = new Composite(whGroup, SWT.LEFT);
			whFields.setLayout(new GridLayout(4, false));
						
			Label completeWidthLabel = new Label(whFields, SWT.NULL);
			completeWidthLabel.setText(Messages.LayoutEditor_WidthText);
			completeWidthLabel.setLayoutData(labelData);
			completeWidthText = new Text(whFields, SWT.BORDER);
			completeWidthText.addVerifyListener(inputVerify);
			completeWidthText.setLayoutData(data);

			Label completeHeightLabel = new Label(whFields, SWT.NULL);
			completeHeightLabel.setText(Messages.LayoutEditor_HeightText);
			completeHeightLabel.setLayoutData(secondInputFieldsGridData);
			completeHeightText = new Text(whFields, SWT.BORDER);
			completeHeightText.addVerifyListener(inputVerify);
			completeHeightText.setLayoutData(data);
			
			prGroup = new Group(this, SWT.LEFT);
			GridData prgGD = new GridData(SWT.FILL, SWT.FILL, true, true);
			prGroup.setLayoutData(prgGD);
			prGroup.setText(Messages.LayoutEditor_PRGroup);
			prGroup.setLayout(new GridLayout(1, false));
			prGroup.setLayoutData(new GridData(516, 420));
			
			Composite inputFields = new Composite(prGroup, SWT.LEFT);
			inputFields.setLayout(new GridLayout(4, false));

			KeyListener checkFieldSizesKeyListener = new KeyListener() {
				@Override
				public void keyPressed(KeyEvent paramKeyEvent) {
				}

				@Override
				public void keyReleased(KeyEvent paramKeyEvent) {
					pane.checkFieldSizes();
				}				
			};
			
			Label leftWidthLabel = new Label(inputFields, SWT.NULL);
			leftWidthLabel.setText(Messages.LayoutEditor_WidthLeftText);
			leftWidthLabel.setLayoutData(labelData);
			leftWidth = new Text(inputFields, SWT.BORDER);
			leftWidth.setLayoutData(data);
			leftWidth.addKeyListener(checkFieldSizesKeyListener);

			Label topHeightLabel = new Label(inputFields, SWT.NULL);
			topHeightLabel.setText(Messages.LayoutEditor_HeightTopText);
			topHeightLabel.setLayoutData(secondInputFieldsGridData);
			topHeight = new Text(inputFields, SWT.BORDER);
			topHeight.setLayoutData(data);
			topHeight.addKeyListener(checkFieldSizesKeyListener);

			Label rightWidthLabel = new Label(inputFields, SWT.NULL);
			rightWidthLabel.setText(Messages.LayoutEditor_WidthRightText);
			rightWidthLabel.setLayoutData(labelData);
			rightWidth = new Text(inputFields, SWT.BORDER);
			rightWidth.setLayoutData(data);
			rightWidth.addKeyListener(checkFieldSizesKeyListener);

			Label bottomHeightLabel = new Label(inputFields, SWT.NULL);
			bottomHeightLabel.setText(Messages.LayoutEditor_HeightBottomText);
			bottomHeightLabel.setLayoutData(secondInputFieldsGridData);
			bottomHeight = new Text(inputFields, SWT.BORDER);
			bottomHeight.setLayoutData(data);
			bottomHeight.addKeyListener(checkFieldSizesKeyListener);			

			Listener calculateValues = new Listener() {

				@Override
				public void handleEvent(Event event) {
					if (settingFields) {
						return;
					}
					String h = completeHeightText.getText();
					String w = completeWidthText.getText();
					sizeHeight = 0;
					sizeWidth = 0;
					if (h != null && w != null) {
						try {
							sizeHeight = Integer.parseInt(h);
							sizeWidth = Integer.parseInt(w);
							logger.debug("Width: " + sizeWidth + " - Height" + sizeHeight); //$NON-NLS-1$ //$NON-NLS-2$
							pane.refreshSize(sizeWidth, sizeHeight);
							calculateValues();
						} catch (NumberFormatException e) {

						}
					}
				}
			};

			completeHeightText.addListener(SWT.Modify, calculateValues);
			completeWidthText.addListener(SWT.Modify, calculateValues);
			enableListeners();

		}

		protected void calculateValues() {

			LayoutEditor.this.notifyListeners(SWT.Modify, new Event());
			setFields();
		}

		public void setFields() {
			settingFields = true;
			HashMap<Integer, Float> map = pane.getValues();
			if (currentEdit != leftWidth)
//				leftWidth.setText("" + horizontalFtI(map.get(0))); //$NON-NLS-1$
				leftWidth.setText("" + (int)(map.get(0) * 100)); //$NON-NLS-1$
			if (currentEdit != topHeight)
				topHeight.setText("" + (int)(map.get(1) * 100)); //$NON-NLS-1$
//				topHeight.setText("" + verticalFtI(map.get(1))); //$NON-NLS-1$
//			if (currentEdit != centerHeight)
//				centerHeight.setText("" + verticalFtI(map.get(2))); //$NON-NLS-1$
//			if (currentEdit != centerWidth)
//				centerWidth.setText("" + horizontalFtI(map.get(3))); //$NON-NLS-1$
			if (currentEdit != bottomHeight)
				bottomHeight.setText("" + (int)(map.get(4) * 100)); //$NON-NLS-1$
//			bottomHeight.setText("" + verticalFtI(map.get(4))); //$NON-NLS-1$
			if (currentEdit != rightWidth)
				rightWidth.setText("" + (int)(map.get(5) * 100)); //$NON-NLS-1$
//			rightWidth.setText("" + horizontalFtI(map.get(5))); //$NON-NLS-1$

			settingFields = false;
			currentEdit = null;
		}

		private void enableListeners() {
			leftWidth.addVerifyListener(inputVerify);
			leftWidth.addModifyListener(inputModify);
			topHeight.addVerifyListener(inputVerify);
			topHeight.addModifyListener(inputModify);
//			centerHeight.addVerifyListener(inputVerify);
//			centerHeight.addModifyListener(inputModify);
//			centerWidth.addVerifyListener(inputVerify);
//			centerWidth.addModifyListener(inputModify);
			bottomHeight.addModifyListener(inputModify);
			bottomHeight.addModifyListener(inputModify);
			rightWidth.addModifyListener(inputModify);
		}

		private ModifyListener modifyValues() {
			return new ModifyListener() {

				@Override
				public void modifyText(ModifyEvent e) {
					if (settingFields) {
						return;
					}
					if (pane == null) {
						return;
					}
					Text eventText = (Text) e.widget;
					if (eventText.equals(leftWidth)) {
						try {
							int width = Integer.parseInt(leftWidth.getText());
							currentEdit = leftWidth;
							if (!pane.arrange(LayoutField.LEFT,
									LayoutField.BORDERRIGHT,
									(width / 100.0f), 0)) {
								setFields();
							}
						} catch (NumberFormatException ex) {

						}
					} else if (eventText.equals(topHeight)) {
						try {
							int height = Integer.parseInt(topHeight.getText());
							currentEdit = topHeight;
							if (!pane.arrange(LayoutField.TOP,
									LayoutField.BORDERBOTTOM, 0,
									(height / 100.0f))) {
								setFields();
							}
						} catch (NumberFormatException e2) {

						}
					} else if (eventText.equals(bottomHeight)) {
						try {
							int height = Integer.parseInt(bottomHeight
									.getText());
							currentEdit = bottomHeight;
							if (!pane.arrange(LayoutField.BOTTOM,
									LayoutField.BORDERTOP, 0,
									(height / 100.0f))) {
								setFields();
							}
						} catch (NumberFormatException e2) {
						}
					} else if (eventText.equals(rightWidth)) {
						try {
							int width = Integer.parseInt(rightWidth.getText());
							currentEdit = rightWidth;
							if (!pane.arrange(LayoutField.RIGHT,
									LayoutField.BORDERLEFT,
									(width / 100.0f), 0)) {
								setFields();
							}
						} catch (NumberFormatException e2) {
						}
//					} else if (eventText.equals(centerHeight)) {
//						try {
//							int height = Integer.parseInt(centerHeight
//									.getText());
//							currentEdit = centerHeight;
//							if (!pane.arrange(LayoutField.CENTER, -1, 0,
//									verticalItF(height))) {
//								setFields();
//							}
//						} catch (NumberFormatException e2) {
//						}
//					} else if (eventText.equals(centerWidth)) {
//						try {
//							int width = Integer.parseInt(centerWidth.getText());
//							currentEdit = centerWidth;
//							if (!pane.arrange(LayoutField.CENTER, -2,
//									horizontalItF(width), 0)) {
//								setFields();
//							}
//						} catch (NumberFormatException e2) {
//						}
					}

				}
			};
		}

		public void setWH(int width, int height) {
			settingFields = true;
			completeWidthText.setText("" + width); //$NON-NLS-1$
			completeHeightText.setText("" + height); //$NON-NLS-1$
			settingFields = false;
		}

		public void storeValues() {
			HashMap<Integer, Float> map = pane.getValues();
			areaLeftWidth = map.get(0);
			areaTopHeight = map.get(1);
			areaBottomHeight = map.get(4);
			areaRightWidth = map.get(5);
		}

		private VerifyListener verifyInputs() {
			return new VerifyListener() {

				@Override
				public void verifyText(VerifyEvent e) {
					if (settingFields) {
						e.doit = true;
						return;
					}
					e.doit = false;
					char myChar = e.character;
					// Allow 0-9
					if (Character.isLetter(myChar))
						e.doit = false;
					if (Character.isDigit(myChar))
						e.doit = true;

					// Allow backspace
					if (myChar == '\b' || myChar == SWT.DEL)
						e.doit = true;
					
					if (e.doit) {
						try {
							int i = Integer.parseInt(e.text);
							if (i >= 0 && i <= 100) {
								e.doit = true;
							} else {
								e.doit = false;
							}
						} catch (NumberFormatException e1) {
						}
					}
				}
			};
		}
	}

	protected int horizontalFtI(float x) {
		return Math.round(x * sizeWidth);
	}

	protected int verticalFtI(float y) {
		return Math.round(y * sizeHeight);
	}

	protected float horizontalItF(int x) {
		return new BigDecimal(x).divide(new BigDecimal(sizeWidth), 10000,
				BigDecimal.ROUND_DOWN).floatValue();
	}

	protected float verticalItF(int y) {
		return new BigDecimal(y).divide(new BigDecimal(sizeHeight), 10000,
				BigDecimal.ROUND_DOWN).floatValue();
	}

	public int getSizeHeight() {
		input.storeValues();
		return sizeHeight;
	}

	public int getSizeWidth() {
		input.storeValues();
		return sizeWidth;
	}

	public float getAreaLeftWidth() {
		input.storeValues();
		return areaLeftWidth;
	}

	public float getAreaTopHeight() {
		input.storeValues();
		return areaTopHeight;
	}

	public float getAreaBottomHeight() {
		input.storeValues();
		return areaBottomHeight;
	}

	public float getAreaRightWidth() {
		input.storeValues();
		return areaRightWidth;
	}
}
