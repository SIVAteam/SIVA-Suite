package org.iviPro.editors.scenegraph;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.CompoundCommand;
import org.iviPro.editors.scenegraph.commands.CommandWrapper;
import org.iviPro.operations.CompoundOperation;
import org.iviPro.operations.IAbstractOperation;

/**
 * Diese Klasse ist eine Erweiterung des normalen GEF-CommandStacks, der alle
 * Operationen in zusammengesetzten Commands in einer CompoundOperation
 * zusammenfasst, bevor er sie ausfuehrt.<br>
 * <br>
 * Dadurch lassen sich diese dann mit einem einzigen Undo/Redo rueckgaengig
 * machen oder wiederholen. Wuerde man das nicht tun, dann wuerden fuer jedes
 * Unterkommando eine eigene Operation in die die OperationHistory eingefuegt.
 * So wird daraus eine einzige Operation, z.B. beim Verschieben mehrerer Knoten
 * auf einmal oder beim löschen mehrerer Dinge.<br>
 * <br>
 * Dieser CommandStack muss in der EditDomain des GEF-Editors verwendet werden,
 * wenn obiges Verhalten erfolgen soll. Ansonsten wuerde jedes Unterkommando
 * einzeln in der OperationHistory landen, d.h. beim Verschieben von 20 Knoten
 * muesste man auch 20x undo machen.
 * 
 * @author dellwo
 * 
 */
public class CommandStackWithOperationSupport extends CommandStack {

	@Override
	public void execute(Command command) {
		// Fasse alle Operationen zusammen, bevor das Command ausgefuehrt wird.
		Command mergedCommand = mergeOperations(command);
		super.execute(mergedCommand);
	}

	/**
	 * Fasst alle Operationen in einem zusammengesetzten Command in einer
	 * einzigen CompoundOperation zusammen. Dadurch lassen sich diese dann mit
	 * einem einzigen Undo/Redo rueckgaengig machen oder wiederholen. Wuerde man
	 * das nicht tun, dann wuerden fuer jedes Unterkommando eine eigene
	 * Operation in die die OperationHistory eingefuegt. So wird daraus eine
	 * einzige Operation, z.B. beim Verschieben mehrerer Knoten auf einmal oder
	 * beim löschen mehrerer Dinge.
	 * 
	 * @param cmd
	 *            Das Command.
	 * @return Ein Command in dem alle Operationen in einer CompoundOperation
	 *         zusammengefasst sind.
	 */
	private Command mergeOperations(Command cmd) {
		// Falls das Command ein zusammengesetztes Command ist, splitten wir
		// es auf in seine Bestandteile und fassen spaeter alle Operationen
		// darin in einer CompoundOperation zusammen. Dies bewirkt, dass
		// sich dann alles auf einmal per Undo/Redo rueckgaengig machen
		// laesst. Wuerde man dies nicht tun, wuerde fuer jedes
		// Einzel-Command ein eigener Undo/Redo noetig, d.h. verschieben man
		// z.B. 20 Objekte auf einmal, dann muesste man 20x Undo machen, um
		// die Aktion rueckgaengig machen zu koennen.
		List<IAbstractOperation> operations = new ArrayList<IAbstractOperation>();
		List<Command> commands = new ArrayList<Command>();

		if (cmd instanceof CompoundCommand) {
			// Aufsplitten, falls Command zusammengesetzt ist.
			CompoundCommand compoundCmd = (CompoundCommand) cmd;
			for (Object o : compoundCmd.getCommands()) {
				if (o instanceof CommandWrapper) {
					operations.add(((CommandWrapper) o).getOperation());
				} else if (o instanceof Command) {
					commands.add((Command) o);
				}
			}
		} else {
			// Kein zusammengesetztes Command
			return cmd;
		}

		// Nur wenn zusammengesetztes Command Operationen enthalten hat,
		// tritt obiges Problem auf. Dann koennen wir einfach nur das
		// urspruengliche Command zurueck geben, da ohnehin nichts in die
		// Undo/Redo Liste kommen wuerde.
		if (operations.size() == 0) {
			return cmd;

		} else {
			// Es sind Operationen vorhanden, dann kapseln wir diese
			// gemeinsam in einer Compound-Operation und packen das ganze
			// wieder in einem Command zusammen.
			String opLabel = operations.get(0).getLabel();
			CompoundOperation<IAbstractOperation> compoundOperation = new CompoundOperation<IAbstractOperation>(
					opLabel);
			for (IAbstractOperation operation : operations) {
				compoundOperation.addOperation(operation);
			}
			if (commands.size() > 0) {
				// Mischung auf Operationen und Commands: Ein neues
				// Compound-Command erstellen, dass einen Wrapper fuer alle
				// Operationen enthaelt + alle normalen GEF-Commands.
				CompoundCommand mixedCommand = new CompoundCommand();
				mixedCommand.add(new CommandWrapper(compoundOperation));
				for (Command singleCmd : commands) {
					mixedCommand.add(singleCmd);
				}
				return mixedCommand;
			} else {
				// Operationen only: Es reicht, die CompoundOperation zu
				// wrappen.
				return new CommandWrapper(compoundOperation);
			}
		}
	}

}
