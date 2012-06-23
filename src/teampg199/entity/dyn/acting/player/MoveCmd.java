package teampg199.entity.dyn.acting.player;

import teampg.grid2d.point.RelPos;


public class MoveCmd extends ActionCommand {
private RelPos moveDir;
	
	
	public MoveCmd(RelPos moveDir) {
		this.moveDir = moveDir;
	}

	public RelPos getRelativePosition(){
		return moveDir;
	}
	

}
