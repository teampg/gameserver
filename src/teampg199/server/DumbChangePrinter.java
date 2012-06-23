package teampg199.server;

import java.io.PrintStream;
import teampg199.changeout.Change;
import teampg199.changeout.PageChangeSubscriber;

public class DumbChangePrinter implements PageChangeSubscriber {
	private int turnCount = 0;
	PrintStream printTo;

	public DumbChangePrinter(PrintStream printTo) {
		this.printTo = printTo;
	}

	@Override
	public void addChanges(PageChanges changes) {
		if (changes.isEmpty()) {
			return;
		}

		printTo.println();

		turnCount++;
		for (Change change : changes.getBoardChanges()) {
			printTo.println(turnCount + ": " + change);
		}
		for (Change change : changes.getEntityChanges()) {
			printTo.println(turnCount + ": " + change);
		}
		for (Change change : changes.getNewEntityChanges()) {
			printTo.println(turnCount + ": " + change);
		}
	}
}