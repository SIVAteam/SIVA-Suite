package org.iviPro.editors.toc;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.iviPro.application.Application;
import org.iviPro.editors.common.SivaComposite;
import org.iviPro.operations.OperationHistory;
import org.iviPro.operations.global.ChangeTitleOperation;
/**
 * Panel mit Steuerelementen zur Manipulation des Inhaltsverzeichnisses
 * @author langa
 *
 */
public class ControlPanel extends SivaComposite implements PropertyChangeListener {

	private TreePanel tc;
	private TitleComposite title;
	
	public ControlPanel(Composite parent, int style) {
		super(parent, style);
		setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		GridLayout layout = new GridLayout(1, false);
		setLayout(layout);

		title = new TitleComposite(this, SWT.NONE);
		initToolbar(this);
	}

	public void setTC(TreePanel comp) {
		tc = comp;
		title.setTitle();
		tc.getRoot().addPropertyChangeListener(this);
	}

	private Composite initToolbar(Composite parent) {
		Composite toolbar = new Composite(parent, SWT.None);
		toolbar.setLayout(new RowLayout());
		toolbar.setBackground(Display.getCurrent().getSystemColor(
				SWT.COLOR_WHITE));

		// Text und Button für neuen Punkt im Inhaltsverzeichnis
		final Text addText = new Text(toolbar, SWT.BORDER);
		addText.setLayoutData(new RowData(150, SWT.DEFAULT));
		addText.setText(Messages.TreePanel_DefaultNewNodeTitle);
		addText.addMouseListener(new MouseListener() {

			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
			}

			@Override
			public void mouseDown(MouseEvent arg0) {
				if (addText.getText().equals(Messages.TreePanel_DefaultNewNodeTitle)) {
					addText.setText(""); //$NON-NLS-1$
				} else {
					//addText.selectAll();
				}
			}
			

			@Override
			public void mouseUp(MouseEvent arg0) {
			}
			
		});
		
		addText.addKeyListener(new KeyListener() {
			
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == SWT.CR || e.keyCode == SWT.LF) {
					String fieldText = addText.getText();
					if (fieldText != null && !fieldText.equals("")) {
						addItem(fieldText);
					}
					addText.setText("");
				}
				
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		
		Button add = new Button(toolbar, SWT.PUSH);
		add.setToolTipText(Messages.TreePanel_ButtonAdd);
		add.setText(Messages.TreePanel_ButtonAdd);
		add.setToolTipText(Messages.ControlPanel_Tooltip_AddNewItem);
		add.addListener(SWT.MouseDown, new Listener() {

			@Override
			public void handleEvent(Event event) {
				addItem(addText.getText());
				addText.setText("");
				//addText.setText(Messages.TreePanel_DefaultNewNodeTitle); // Feld leeren //$NON-NLS-1$
			}
		});

		// Button um einen Knoten nach unten zu schieben
		Button moveDown = new Button(toolbar, SWT.PUSH);
		moveDown.setToolTipText(Messages.TreePanel_ButtonDown);
		moveDown.setText(Messages.TreePanel_ButtonDown);
		moveDown.setToolTipText(Messages.ControlPanel_Tooltip_MoveDown);
		moveDown.addListener(SWT.MouseDown, new Listener() {

			@Override
			public void handleEvent(Event event) {
				moveDown();
			}
		});

		// Button um einen Knoten nach oben zu schieben
		Button moveUp = new Button(toolbar, SWT.PUSH);
		moveUp.setToolTipText(Messages.TreePanel_ButtonUp);
		moveUp.setText(Messages.TreePanel_ButtonUp);
		moveUp.setToolTipText(Messages.ControlPanel_Tooltip_MoveUp);
		moveUp.addListener(SWT.MouseDown, new Listener() {

			@Override
			public void handleEvent(Event event) {
				moveUp();
			}
		});

		return toolbar;
	}

	private void addItem(String text) {
		if (tc == null) {
			return;
		}
		tc.addItem(text);
	}

	private void moveDown() {
		if (tc == null) {
			return;
		}
		tc.moveDown();
	}

	private void moveUp() {
		if (tc == null) {
			return;
		}
		tc.moveUp();
	}
	
	private class TitleComposite extends SivaComposite {
		
		Text toctitle;
		private boolean init;
		private boolean isTyping;
		
		public TitleComposite(Composite parent, int style) {
			super(parent, style);

			setLayout(new RowLayout());
			setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));

			Label label = new Label(this, SWT.NONE);
			label.setText(Messages.TreePanel_LabelTitleOfTableOfCotent);
			label.setBackground(Display.getCurrent().getSystemColor(
					SWT.COLOR_WHITE));
			toctitle = new Text(this, SWT.BORDER);
			setTitle();
			toctitle.setLayoutData(new RowData(200, SWT.DEFAULT));

			toctitle.addModifyListener(new ModifyListener() {

				@Override
				public void modifyText(ModifyEvent e) {
					if (init) {
						return;
					}
					if (tc != null) {
						ChangeTitleOperation operation = new ChangeTitleOperation(
								tc.getRoot(), toctitle.getText());
						try {
							isTyping = true;
							OperationHistory.execute(operation);
							isTyping = false;
						} catch (ExecutionException e1) {
							operation.getErrorMessage(e1);
						}
					}

				}
			});
		}
		
		void setTitle() {
			if (isTyping) {
				return;
			}
			init = true;
			if (tc != null) {
				String title = tc.getRoot().getTitle();
				if (title == null || title.equals("")) {
					title = Application.getCurrentProject().getTitle();
				}
				toctitle.setText(title);
			} 
			init = false;
		}

	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// Werte neu setzten bei PropertyChange (z.b. Redo/Undo)
		title.setTitle();		
	}
}
