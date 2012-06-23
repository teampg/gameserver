package teampg199.server.translator;

import java.util.HashMap;
import java.util.Map;

import teampg199.entity.dyn.acting.player.ActionCommand;

class ActionIDManager {
	private final Map<ActionCommand, Integer> loggedActions;

	public ActionIDManager(){
		loggedActions = new HashMap<>();
	}

	public int getID(ActionCommand action) {
		assert loggedActions.containsKey(action);
		return loggedActions.get(action);
	}

	public void setID(ActionCommand action, int id){
		loggedActions.put(action, id);
	}
}
