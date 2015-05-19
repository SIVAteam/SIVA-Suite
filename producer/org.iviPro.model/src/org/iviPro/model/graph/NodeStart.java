/**
 * 
 */
package org.iviPro.model.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.eclipse.draw2d.geometry.Point;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.LocalizedString;
import org.iviPro.model.Project;
import org.iviPro.model.resources.IResource;

/**
 * @author dellwo
 */
public class NodeStart extends INodeAnnotationAction {

	/** Default-Position wo der Startkonten am Anfang liegen soll */
	private static final Point DEFAULT_POSITION = new Point(300, 100);

	/** Definition der moeglichen und erforderlichen Verbindungen */
	private static final ConnectionConstraints CONNECTION_CONSTRAINTS = //
	new ConnectionConstraints(1, 1, new ConnectionTargetDefinition[] {
			new ConnectionTargetDefinition(NodeScene.class, 0, 1)
	});

	public NodeStart(Project project) {
		super(
				new LocalizedString("START", Locale.ROOT), project, CONNECTION_CONSTRAINTS); //$NON-NLS-1$
		setPosition(new Point(DEFAULT_POSITION));
	}

	@Override
	public boolean isDependentOn(IAbstractBean object) {
		return false;
	}	
}
