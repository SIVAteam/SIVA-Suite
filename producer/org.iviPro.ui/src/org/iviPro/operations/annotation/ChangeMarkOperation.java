package org.iviPro.operations.annotation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.iviPro.application.Application;
import org.iviPro.model.annotation.EllipseShape;
import org.iviPro.model.annotation.IMarkShape;
import org.iviPro.model.annotation.PolygonShape;
import org.iviPro.model.annotation.PolygonShape.Position;
import org.iviPro.model.annotation.PositionalShape;
import org.iviPro.model.graph.NodeMark;
import org.iviPro.model.graph.NodeMarkType;
import org.iviPro.operations.IAbstractOperation;

public class ChangeMarkOperation extends IAbstractOperation {

	private final NodeMark target;
	private final LinkedList<IMarkShape> oldItems;
	private final LinkedList<IMarkShape> newItems;
	private final NodeMarkType newType;
	private final NodeMarkType oldType;
	private final long newDuration;
	private final long oldDuration;
	private final String newButtonLabel;
	private final String oldButtonLabel;

	/**
	 * Erstellt eine neue Operation zum Speichern von Mark Shapes bei
	 * Markierungsannotationen
	 * 
	 * @param target
	 *            Das Model-Objekt dessen Titel geaendert werden soll.
	 * @param markShapes
	 *            Die neuen MarkShapes
	 * @throws IllegalArgumentException
	 *             Falls einer der obigen Parameter null ist.
	 */
	public ChangeMarkOperation(NodeMark target, NodeMarkType markType,
			List<IMarkShape> markShapes, long markDuration, String buttonLabel)
			throws IllegalArgumentException {
		super(Messages.ChangeScreenPositionOperation_Label);

		if (target == null || markType == null || markShapes == null) {
			throw new IllegalArgumentException(
					"Neither of the parameters may be null."); //$NON-NLS-1$
		}

		this.newType = markType;
		this.oldType = target.getType();

		this.newDuration = markDuration;
		this.oldDuration = target.getDuration();
		//TODO entfernen oder umbauen fuer neue posi von der beschriftung
		this.newButtonLabel = buttonLabel;
		this.oldButtonLabel = target.getButtonLabel();

		newItems = new LinkedList<IMarkShape>();
		oldItems = new LinkedList<IMarkShape>();
		// falls markShapes gesetzt sind, kopiere sie ansonsten erstelle einen
		// Shape
		if (markShapes.size() > 0) {
			// kopiere neue
			for (IMarkShape item : markShapes) {
				newItems.add(item.clone());
			}
		} else {
			long start = target.getStart();
			switch (markType) {
			case ELLIPSE:
				EllipseShape markEllipse = new EllipseShape("",
						Application.getCurrentProject());
				markEllipse.setTime(start);
				markEllipse.setLengthA(0.3f);
				markEllipse.setLengthB(0.3f);
				markEllipse.setX(0.1f);
				markEllipse.setY(0.1f);
				markShapes.add(markEllipse);
				break;
			case POLYGON:
				PolygonShape markPolygon = new PolygonShape("",
						Application.getCurrentProject());
				markPolygon.setTime(start);
				Position newPosition = markPolygon.new Position();
				newPosition.setX(0.1f);
				newPosition.setY(0.1f);
				Position newPosition2 = markPolygon.new Position();
				newPosition2.setX(0.4f);
				newPosition2.setY(0.4f);
				Position newPosition3 = markPolygon.new Position();
				newPosition3.setX(0.3f);
				newPosition3.setY(0.6f);
				ArrayList<Position> positions = new ArrayList<Position>();
				positions.add(newPosition);
				positions.add(newPosition2);
				positions.add(newPosition3);
				markPolygon.setVertices(positions);
				markShapes.add(markPolygon);
				break;
			case BUTTON:
				PositionalShape markButton = new PositionalShape("",
						Application.getCurrentProject());
				markButton.setTime(start);
				markButton.setX(0.1f);
				markButton.setY(0.1f);
				markShapes.add(markButton);
				break;
			}
		}
		// kopiere alte
		for (IMarkShape item : target.getShapes()) {
			oldItems.add(item.clone());
		}
		this.target = target;
	}

	@Override
	public boolean canExecute() {
		return target != null && newType != null;
	}

	@Override
	public String getErrorMessage(Exception e) {
		return Messages.ChangeMarkOperation_ErrorMsg + e.getMessage();
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		return redo(monitor, info);
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		target.setType(newType);
		target.getShapes().clear();
		target.getShapes().addAll(newItems);
		target.setDuration(newDuration);
		target.setButtonLabel(newButtonLabel);
		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		target.setType(oldType);
		target.getShapes().clear();
		target.getShapes().addAll(oldItems);
		target.setDuration(oldDuration);
		target.setButtonLabel(oldButtonLabel);
		return Status.OK_STATUS;
	}

}
