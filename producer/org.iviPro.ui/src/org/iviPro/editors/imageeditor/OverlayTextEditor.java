package org.iviPro.editors.imageeditor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.iviPro.model.imageeditor.IText;

public class OverlayTextEditor {

	private StyledText textfield;
	private final int NORMAL = 0;
	private final int BOLD = 1;
	private final int ITALIC = 2;
	private final int UNDERLINE = 3;

	public OverlayTextEditor(final ImageEditWidget editor, final IText text,
			final OverlayWidget wgt, final boolean newText) {
		final Shell shell = new Shell(Display.getCurrent());
		shell.setText(Messages.OverlayTextEditor_Editor_Title);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		shell.setLayoutData(gd);
		GridLayout layout = new GridLayout();
		shell.setLayout(layout);

		final Composite toolbar = new Composite(shell, SWT.None);
		GridLayout tbLayout = new GridLayout(4, false);
		toolbar.setLayout(tbLayout);

		Button bBold = new Button(toolbar, SWT.PUSH);
		bBold.setSize(20, 20);
		bBold.setText("B"); //$NON-NLS-1$
		bBold.addListener(SWT.MouseDown, new Listener() {

			@Override
			public void handleEvent(Event event) {
				setStyle(BOLD);
				text.bold = !text.bold;
			}
		});

		Button bItalic = new Button(toolbar, SWT.PUSH);
		bItalic.setSize(20, 20);
		bItalic.setText("I"); //$NON-NLS-1$
		bItalic.addListener(SWT.MouseDown, new Listener() {

			@Override
			public void handleEvent(Event event) {
				setStyle(ITALIC);
				text.italic = !text.italic;
			}
		});

		Button bUnderline = new Button(toolbar, SWT.PUSH);
		bUnderline.setSize(20, 20);
		bUnderline.setText("U"); //$NON-NLS-1$
		bUnderline.addListener(SWT.MouseDown, new Listener() {

			@Override
			public void handleEvent(Event event) {
				setStyle(UNDERLINE);
				text.underline = !text.underline;
			}
		});

		final Combo bFontsize = new Combo(toolbar, SWT.DROP_DOWN);
		for (int i = 8; i <= 28; i = i + 2) {
			bFontsize.add(String.valueOf(i));
		}
		int sel = bFontsize.indexOf(String.valueOf(editor
				.getHorizontalFtI(text.fontsize)));
		bFontsize.select(sel);
		bFontsize.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				textfield.setFont(new Font(Display.getCurrent(), "Arial", //$NON-NLS-1$
						Integer.parseInt(bFontsize.getText()), SWT.NORMAL));
				text.fontsize = editor.getHorizontalItF(Integer
						.parseInt(bFontsize.getText()));

			}
		});

		// -- TEXTFIELD --//
		textfield = new StyledText(shell, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL
				| SWT.H_SCROLL);
		textfield.setLayoutData(new GridData(GridData.FILL_BOTH
				| GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
		// Set text and attributes
		textfield.setFont(new Font(Display.getCurrent(), "Arial", editor //$NON-NLS-1$
				.getHorizontalFtI(text.fontsize), SWT.NORMAL));
		textfield.setText(text.text);
		if (text.bold)
			setStyle(BOLD);
		if (text.italic)
			setStyle(ITALIC);
		if (text.underline)
			setStyle(UNDERLINE);
		textfield.addListener(SWT.KeyDown, new Listener() {

			@Override
			public void handleEvent(Event arg0) {
				setStyle(NORMAL);
			}
		});
		textfield.setFocus();
		Composite tbComp = new Composite(shell, SWT.None);
		GridData tbCompGD = new GridData(SWT.CENTER, SWT.CENTER);
		tbCompGD.widthHint = 200;
		tbCompGD.heightHint = 30;
		tbComp.setLayoutData(tbCompGD);

		GridLayout bLayout = new GridLayout(2, false);
		tbComp.setLayout(bLayout);
		Button ok = new Button(tbComp, SWT.PUSH);
		ok.setSize(50, 50);
		ok.setText(Messages.OverlayTextEditor_OK_Button);
		ok.addListener(SWT.MouseDown, new Listener() {

			@Override
			public void handleEvent(Event event) {
				text.text = textfield.getText();
				if (wgt != null) {
					wgt.redraw();
				}
				editor.redraw();
				editor.dirty = true;
				shell.close();
				shell.dispose();
			}
		});
		Button cancel = new Button(tbComp, SWT.PUSH);
		cancel.setSize(100, 50);
		cancel.setText(Messages.OverlayTextEditor_Cancel_Button);
		cancel.addListener(SWT.MouseDown, new Listener() {

			@Override
			public void handleEvent(Event arg0) {
				if (newText) {
					editor.overlays.remove(wgt);
					wgt.dispose();
				}
				shell.close();
				shell.dispose();
			}
		});
		shell.setSize(250, 250);
		shell.setVisible(true);
		shell.open();

	}

	/**
	 * Set the style for a text
	 * 
	 * @param newStyle
	 */
	protected void setStyle(int newStyle) {
		if (textfield.getCharCount() > 0) {
			StyleRange range = textfield.getStyleRangeAtOffset(0);
			if (range == null) {
				range = new StyleRange(0, textfield.getCharCount(), null, null,
						SWT.NORMAL);
			}
			range.length = textfield.getCharCount();
			if (newStyle == BOLD) {
				range.fontStyle ^= SWT.BOLD;
			} else if (newStyle == ITALIC) {
				range.fontStyle ^= SWT.ITALIC;
			} else if (newStyle == UNDERLINE) {
				range.underline = !range.underline;
			}
			textfield.setStyleRange(range);
		}
	}
}
