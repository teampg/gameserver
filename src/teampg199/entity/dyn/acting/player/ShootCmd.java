package teampg199.entity.dyn.acting.player;

import teampg.grid2d.point.RelPos;

public class ShootCmd extends ActionCommand{
	
private RelPos direction;
	
	
	public ShootCmd(RelPos dir) {
		direction = dir;
	}

	public RelPos getDirection(){
		return direction;
	}
}
