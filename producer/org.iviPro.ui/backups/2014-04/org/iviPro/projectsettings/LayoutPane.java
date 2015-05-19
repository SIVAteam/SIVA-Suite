package org.iviPro.projectsettings;

import java.math.BigDecimal;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.MessageBox;

public class LayoutPane extends Composite {

	private static Logger logger = Logger.getLogger(LayoutPane.class);

	private static final int MAX_PANE_WIDTH = 500;
	private static final int MAX_PANE_HEIGHT = 375;
	private int paneWidth = MAX_PANE_WIDTH;
	private int paneHeight = MAX_PANE_HEIGHT;
	private LayoutField left;
	private LayoutField top;
	private LayoutField center;
	private LayoutField bottom;
	private LayoutField right;
	private LayoutEditor editor;
	
	public LayoutPane(Composite parent, int style, LayoutEditor editor) {
		super(parent, style);
		this.editor = editor;
		setSize(paneWidth, paneHeight);
		setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
		left = new LayoutField(this, LayoutField.LEFT);
		top = new LayoutField(this, LayoutField.TOP);
		center = new LayoutField(this, LayoutField.CENTER);
		bottom = new LayoutField(this, LayoutField.BOTTOM);
		right = new LayoutField(this, LayoutField.RIGHT);		
		setInitialValues();				
	}

	protected void setInitialValues() {
		left.setBounds(0.0f, 0.0f, 0.2f, 1.0f);
		top.setBounds(0.2f, 0.0f, 0.6f, 0.2f);
		center.setBounds(0.2f, 0.2f, 0.6f, 0.6f);
		bottom.setBounds(0.2f, 0.8f, 0.6f, 0.2f);
		right.setBounds(0.8f, 0.0f, 0.2f, 1.0f);
	}
	
	/**
	 * prüft die Feldgrößen und gibt eine Warnung aus, dass ab einer best. Größe die Elemente 
	 * nicht mehr richtig angezeigt werden
	 */
	public void checkFieldSizes() {	
		// Unter 3 Prozent wird eine Warnung ausgegeben
		float minWidth = 0.03f;
		float minHeight = 0.03f;
		MessageBox mb = new MessageBox(Display.getCurrent().getActiveShell(), SWT.ICON_WARNING);
		mb.setMessage(Messages.FieldSizeWarningMessage);
		mb.setText(Messages.FileSizeWarningText);
		if (left.getWidth() < minWidth || top.getWidth() < minWidth || right.getWidth() < minWidth || bottom.getWidth() < minWidth
			|| center.getWidth() < minWidth) {
			mb.open();
		} else
		if (left.getHeight() < minHeight || top.getHeight() < minHeight || right.getHeight() < minHeight || bottom.getHeight() < minHeight
				|| center.getHeight() < minHeight) {
			mb.open();
		}
	}

	public boolean arrange(int id, int position, float width, float height) {
		boolean state = true;
		logger.debug("width - height: " + width + " - " + height); //$NON-NLS-1$ //$NON-NLS-2$
		if (width < 0 || height < 0) {
			width = 0.0f;
			state = false;
		}
		if (height < 0) {
			height = 0.0f;
			state = false;
		}
		// Neue Größen aller Felder berechnen und setzen.
		if (id == LayoutField.CENTER && position == LayoutField.BORDERTOP) { // Center
																				// nach
																				// oben
			float completeHeight = top.getHeight() + center.getHeight();
			if (height > completeHeight) {
				height = completeHeight;
				state = false;
			}
			top.setHeight(0, completeHeight - height);
			center.setHeight((completeHeight - height), height);
		} else if (id == LayoutField.CENTER
				&& position == LayoutField.BORDERBOTTOM) { // Center nach unten
			float completeHeight = center.getHeight() + bottom.getHeight();
			if (height > completeHeight) {
				height = completeHeight;
				state = false;
			}
			center.setHeight(-1, height);
			bottom.setHeight(1.0f - (completeHeight - height), completeHeight
					- height);
		} else if (id == LayoutField.CENTER
				&& position == LayoutField.BORDERLEFT) { // Center nach links
			float completeWidth = left.getWidth() + top.getWidth();
			if (width > completeWidth) {
				width = completeWidth;
				state = false;
			}
			left.setWidth(-1, completeWidth - width);
			float newCenterX = completeWidth - width;
			top.setWidth(newCenterX, width);
			center.setWidth(newCenterX, width);
			bottom.setWidth(newCenterX, width);
		} else if (id == LayoutField.CENTER
				&& position == LayoutField.BORDERRIGHT) { // Center nach rechts
			float completeWidth = center.getWidth() + right.getWidth();
			if (width > completeWidth) {
				width = completeWidth;
				state = false;
			}
			right.setWidth(left.getWidth() + width, 1.0f - left.getWidth()
					- width);
			top.setWidth(-1, width);
			center.setWidth(-1, width);
			bottom.setWidth(-1, width);
		} else if (id == LayoutField.LEFT
				&& position == LayoutField.BORDERRIGHT) { // Left nach rechts
			float completeWidth = left.getWidth() + top.getWidth();
			if (width > completeWidth) {
				width = completeWidth;
				state = false;
			}
			float newWidth = completeWidth - width;
			left.setWidth(-1, width);
			top.setWidth(width, newWidth);
			center.setWidth(width, newWidth);
			bottom.setWidth(width, newWidth);
		} else if (id == LayoutField.TOP
				&& position == LayoutField.BORDERBOTTOM) { // Top nach unten
			float completeHeight = top.getHeight() + center.getHeight();
			if (height > completeHeight) {
				height = completeHeight;
				state = false;
			}
			top.setHeight(-1, height);
			center.setHeight(height, completeHeight - height);
		} else if (id == LayoutField.BOTTOM
				&& position == LayoutField.BORDERTOP) { // Bottom nach oben
			float completeHeight = center.getHeight() + bottom.getHeight();
			if (height > completeHeight) {
				height = completeHeight;
				state = false;
			}
			center.setHeight(-1, completeHeight - height);
			bottom.setHeight(1.0f - height, height);
		} else if (id == LayoutField.RIGHT
				&& position == LayoutField.BORDERLEFT) { // Right nach links
			float completeWidth = top.getWidth() + right.getWidth();
			if (width > completeWidth) {
				width = completeWidth;
				state = false;
			}
			float newWidth = completeWidth - width;
			right.setWidth(1.0f - width, width);
			top.setWidth(-1, newWidth);
			center.setWidth(-1, newWidth);
			bottom.setWidth(-1, newWidth);
		} else if (id == LayoutField.CENTER && position == -1) { // Center Höhe
																	// in beide
																	// Richtungen
			float heightDif = center.getHeight() - height;
			float topHeight = top.getHeight() + (heightDif / 2);
			float bottomHeight = bottom.getHeight() + (heightDif / 2);
			top.setHeight(-1, topHeight);
			center.setHeight(topHeight, height);
			bottom.setHeight(1.0f - bottomHeight, bottomHeight);
		} else if (id == LayoutField.CENTER && position == -2) { // Center
																	// Breite in
																	// beide
																	// Richtungen
			float widthDif = center.getWidth() - width;
			float leftWidth = left.getWidth() + (widthDif / 2);
			float rigthWidth = right.getWidth() + (widthDif / 2);
			left.setWidth(-1, leftWidth);
			top.setWidth(leftWidth, width);
			center.setWidth(leftWidth, width);
			bottom.setWidth(leftWidth, width);
			right.setWidth(leftWidth + width, rigthWidth);
		}
		// Editorfelder aktualisiseren
		editor.notifyListeners(SWT.Modify, new Event());
		editor.refreshFields();
		return state;
	}
	
	public void refreshSize(int width, int height) {

		/*
		 * Sets the width and height of LayoutPane according to the 
		 * aspect ratio of the entered width and height in LayoutEditor.
		 * Afterwards, the size of the different LayoutFields is 
		 * recalculated.
		 */
		if (width > 0 && height > 0) {
			if (width > height) {
				float ratio = (float) height / (float) width;
				paneWidth = MAX_PANE_WIDTH;
				paneHeight = (int) (this.paneWidth * ratio);
				if (paneHeight > MAX_PANE_HEIGHT) {
					ratio = (float) width / (float) height;
					paneHeight = MAX_PANE_HEIGHT;
					paneWidth = (int) (this.paneHeight * ratio);
				}
				logger.debug("Ratio: " + ratio); //$NON-NLS-1$
				logger
						.debug("Width: " + paneWidth + " - Height: " //$NON-NLS-1$ //$NON-NLS-2$
								+ paneHeight);
			} else {
				float ratio = (float) width / (float) height;
				paneHeight = MAX_PANE_HEIGHT;
				paneWidth = (int) (this.paneHeight * ratio);
				if (paneWidth > MAX_PANE_WIDTH) {
					ratio = (float) height / (float) width;
					paneWidth = MAX_PANE_WIDTH;
					paneHeight = (int) (this.paneWidth * ratio);
				}
				logger.debug("Ratio: " + ratio); //$NON-NLS-1$
				logger
						.debug("Width: " + paneWidth + " - Height: " //$NON-NLS-1$ //$NON-NLS-2$
								+ paneHeight);
			}

			setSize(this.paneWidth, this.paneHeight);
			Rectangle myBounds = this.getBounds();
			setBounds(myBounds.x, myBounds.y, paneWidth, paneHeight);
			left.resetSize();
			top.resetSize();
			center.resetSize();
			bottom.resetSize();
			right.resetSize();
			myBounds = this.getBounds();
			logger.debug("Bounds width * height: " + myBounds.width + " * " + myBounds.height); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public void setLoadedValues(float leftWidth, float topHeight,
			float bottomHeight, float rightWidth) {
		left.setBounds(0.0f, 0.0f, leftWidth, 1.0f);
		float centerWidth = 1.0f - leftWidth - rightWidth;
		top.setBounds(leftWidth, 0.0f, centerWidth, topHeight);
		center.setBounds(leftWidth, topHeight, centerWidth, 1.0f - bottomHeight
				- topHeight);
		bottom.setBounds(leftWidth, 1.0f - bottomHeight, centerWidth,
				bottomHeight);
		right.setBounds(leftWidth + centerWidth, 0, rightWidth, 1.0f);
		editor.refreshFields();
	}

	public HashMap<Integer, Float> getValues() {
		HashMap<Integer, Float> map = new HashMap<Integer, Float>();
		map.put(0, left.getWidth());
		map.put(1, top.getHeight());
		map.put(2, center.getHeight());
		map.put(3, center.getWidth());
		map.put(4, bottom.getHeight());
		map.put(5, right.getWidth());
		return map;
	}

	protected int horizontalFtI(float x) {
		return Math.round(x * paneWidth);
	}

	protected int verticalFtI(float y) {
		return Math.round(y * paneHeight);
	}

	protected float horizontalItF(int x) {
		return new BigDecimal(x).divide(new BigDecimal(paneWidth), 10000,
				BigDecimal.ROUND_DOWN).floatValue();
	}

	protected float verticalItF(int y) {
		return new BigDecimal(y).divide(new BigDecimal(paneHeight), 10000,
				BigDecimal.ROUND_DOWN).floatValue();
	}

	public String toString() {
		return left.toString()
				+ "\n" + top.toString() + "\n" + center.toString() + "\n" + bottom.toString() + "\n" + right.toString(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}
}
