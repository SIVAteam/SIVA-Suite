/*******************************************************************************
 * Copyright (c) 2011 Tom Seidel, Remus Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *
 * Contributors:
 *     Tom Seidel - initial API and implementation
 *******************************************************************************/
package org.eclipse.mylyn.htmltext.commands.formatting;

import org.eclipse.mylyn.htmltext.commands.Command;
import org.eclipse.mylyn.htmltext.util.ColorConverter;
import org.eclipse.swt.graphics.RGB;

/**
 * @author Tom Seidel <tom.seidel@remus-software.org>
 * @since 0.9
 */
public class SetForegroundColorCommand extends Command {

	private String color;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.mylyn.htmltext.commands.Command#getCommandIdentifier()
	 */
	@Override
	public String getCommandIdentifier() {
		return "setForeground";
	}

	@Override
	public String getCommand() {
		return "new CKEDITOR.style(CKEDITOR.instances.editor1.config['colorButton_foreStyle'], { color : '#"
				 + color + "' } ).apply( CKEDITOR.instances.editor1.document );";
	}

	public void setColor(String hexCode) {
		color = hexCode;
	}

	public void setColor(RGB color) {
		this.color = ColorConverter.convertRgbToHex(color);
	}

	@Override
	protected boolean trackCommand() {
		return false;
	}

}