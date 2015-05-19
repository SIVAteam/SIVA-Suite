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
import org.eclipse.mylyn.htmltext.HtmlComposer;
import org.eclipse.mylyn.htmltext.commands.Command;
import org.eclipse.mylyn.htmltext.commands.GetHtmlCommand;
import org.eclipse.swt.widgets.Text;

/**
 * @author Tom Seidel <tom.seidel@remus-software.org>
 */
public class GetHtmlAction extends AbstractCommandWrapper {

	private final Text target;

	public GetHtmlAction(HtmlComposer composer, Text target) {
		super("Get html", IAction.AS_PUSH_BUTTON, composer);
		this.target = target;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.mylyn.htmltext.example.actions.AbstractCommandWrapper#
	 * getWrappedCommand()
	 */
	@Override
	protected Command getWrappedCommand() {
		return new GetHtmlCommand();
	}

	@Override
	public void run() {
		target.setText(String.valueOf(wrappedCommand.executeWithReturn()));
	}
}
