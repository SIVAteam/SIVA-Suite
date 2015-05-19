/*******************************************************************************
 * Copyright (c) 2010 Tom Seidel, Remus Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *
 * Contributors:
 *     Tom Seidel - initial API and implementation
 *******************************************************************************/

package org.iviPro.editors.annotationeditor.components.contenteditors.richtext.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.mylyn.htmltext.HtmlComposer;
import org.eclipse.mylyn.htmltext.commands.Command;
import org.eclipse.mylyn.htmltext.model.TriState;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;

/**
 * @author Tom Seidel <tom.seidel@remus-software.org>
 */
public abstract class AbstractCommandWrapper extends Action implements
		PropertyChangeListener {
	protected static List<Image> images;
	protected final Command wrappedCommand;
	protected final HtmlComposer composer;

	static {
		images = new ArrayList<Image>();

		ImageDescriptor createFromURL = ImageDescriptor
				.createFromURL(FileLocator.find(
						Platform.getBundle("org.eclipse.mylyn.htmltext"),
						new Path("ckeditor/skins/office2003/icons.png"),
						Collections.EMPTY_MAP));
		Image image = createFromURL.createImage();

		int i = 1;
		for (int n = 75; i < n; i++) {
			Image img = new Image(null, 16, 16);
			GC gc = new GC(img);

			gc.drawImage(image, 0, i * 16, 16, 16, 0, 0, 16, 16);
			gc.dispose();
			images.add(img);
		}
		image.dispose();
	}

	public AbstractCommandWrapper(String text, int style, HtmlComposer composer) {
		super(text, style);
		this.composer = composer;
		this.wrappedCommand = getWrappedCommand();
		assert (this.wrappedCommand != null);
		this.wrappedCommand.setComposer(composer);
		this.wrappedCommand.addPropertyChangeListener(this);
	}

	public void propertyChange(PropertyChangeEvent evt) {
		if ("state".equals(evt.getPropertyName()))
			switch ((TriState) evt.getNewValue()) {
			case OFF:
				setChecked(false);
				setEnabled(true);
				break;
			case ON:
				setChecked(true);
				setEnabled(true);
				break;
			case DISABLED:
				setEnabled(false);
				setChecked(false);
			}
	}

	public void run() {
		this.wrappedCommand.execute();
	}

	protected abstract Command getWrappedCommand();
}
