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

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.mylyn.htmltext.HtmlComposer;
import org.eclipse.mylyn.htmltext.commands.Command;
import org.eclipse.mylyn.htmltext.commands.basicstyle.SuperscriptCommand;
import org.eclipse.swt.graphics.Image;

/**
 * @author Tom Seidel <tom.seidel@remus-software.org>
 */
public class SuperscriptAction extends AbstractCommandWrapper {

	public SuperscriptAction(HtmlComposer composer) {
		super("Superscript", IAction.AS_CHECK_BOX, composer); //$NON-NLS-1$
		setImageDescriptor(ImageDescriptor.createFromImage((Image)images.get(ActionImages.ActionImage_SuperscriptAction)));
	}

	@Override
	protected Command getWrappedCommand() {
		return new SuperscriptCommand();
	}

}
