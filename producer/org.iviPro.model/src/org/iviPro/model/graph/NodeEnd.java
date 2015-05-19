/**
 * 
 */
package org.iviPro.model.graph;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.Point;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.Project;
import org.iviPro.model.resources.IResource;

/**
 * @author dellwo
 */
public class NodeEnd extends INodeAnnotationAction {

	/** Default-Position wo der Endkonten am Anfang liegen soll */
	private static final Point DEFAULT_POSITION = new Point(300, 400);

	/** Definition der moeglichen und erforderlichen Verbindungen */
	private static final ConnectionConstraints CONNECTION_CONSTRAINTS = //
	new ConnectionConstraints(0, 0, new ConnectionTargetDefinition[] {});

	public NodeEnd(Project project) {
		super("End", project, CONNECTION_CONSTRAINTS); //$NON-NLS-1$
		setPosition(new Point(DEFAULT_POSITION));

	}

	@Override
	public boolean isDependentOn(IAbstractBean object) {
		return false;
	}
}
