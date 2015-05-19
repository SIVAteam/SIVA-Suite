package org.iviPro.editors.scenegraph;

import org.apache.log4j.Logger;
import org.eclipse.gef.requests.SimpleFactory;
import org.iviPro.application.Application;
import org.iviPro.model.graph.NodeCondSelection;
import org.iviPro.model.graph.NodeCondSelectionControl;
import org.iviPro.model.graph.NodeQuiz;
import org.iviPro.model.graph.NodeQuizControl;
import org.iviPro.model.graph.NodeRandomSelection;
import org.iviPro.model.graph.NodeResume;
import org.iviPro.model.graph.NodeSelection;
import org.iviPro.model.graph.NodeSelectionControl;

public class ModelObjectFactory extends SimpleFactory {
	private static Logger logger = Logger.getLogger(ModelObjectFactory.class);

	public ModelObjectFactory(Class aClass) {
		super(aClass);
	}

	@Override
	public Object getNewObject() {
		if (getObjectType() == NodeSelection.class) {
			return new NodeSelection(Messages.ModelObjectFactory_NodeSelection_DefaultTitle, Application.getCurrentProject());
		} else if (getObjectType() == NodeSelectionControl.class) {
			return new NodeSelectionControl(Messages.ModelObjectFactory_NodeSelectionControl_DefaultTitle, Application
					.getCurrentProject());
		} else if (getObjectType() == NodeCondSelection.class) {
			return new NodeCondSelection(Messages.ModelObjectFactory_NodeConditionalSelection_DefaultTitle, Application.getCurrentProject());
		} else if (getObjectType() == NodeCondSelectionControl.class) {
			return new NodeCondSelectionControl(Messages.ModelObjectFactory_NodeConditionalSelectionControl_DefaultTitle, Application
					.getCurrentProject());
		} else if (getObjectType() == NodeQuiz.class) {
			return new NodeQuiz(Messages.ModelObjectFactory_NodeQuiz_DefaultTitle, Application.getCurrentProject());
		} else if (getObjectType() == NodeQuizControl.class) {
			return new NodeQuizControl(Messages.ModelObjectFactory_NodeQuizControl_DefaultTitle, Application.getCurrentProject());
		} else if (getObjectType() == NodeRandomSelection.class) {
			return new NodeRandomSelection(Messages.ModelObjectFactory_NodeRandomSelection_DefaultTitle, Application.getCurrentProject());	
		} else if (getObjectType() == NodeResume.class) {
			return new NodeResume(Messages.ModelObjectFactory_NodeResume_DefaultTitle, Application.getCurrentProject());		
		} else {
			logger.error("Don't know how to create object of type: " //$NON-NLS-1$
					+ getObjectType());
			return null;
		}
	}

}
