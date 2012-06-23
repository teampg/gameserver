package teampg199.world.board;

import teampg.grid2d.point.BoundedPos;
import teampg199.changeout.Change;
import teampg199.entity.Entity;
import teampg199.entity.stat.Empty;

import com.google.common.base.Objects;

/**
 * Indicates new value of some cell.
 *
 * @author JWill <Jackson.Williams at camosun.ca>
 */
public final class BoardChange extends Change<Board> {
	private final BoundedPos pos;
	private final Entity newOccupant;

	public BoardChange(Board b, BoundedPos pos, Entity newOccupant) {
		super(b);
		this.pos = pos;
		this.newOccupant = newOccupant;
	}

	public BoundedPos getPos() {
		return pos;
	}

	public Entity getNewOccupant() {
		return newOccupant;
	}

	@Override
	public String toString() {
		return "BoardChange [pos=" + pos + ", newOccupant=" + newOccupant + "]";
	}

	@Override
	public boolean equals(Object other) {
		Change otherChange = (Change) other;
		if (! (otherChange instanceof BoardChange)) {
			return false;
		}

		BoardChange o = (BoardChange) other;

		if (super.affected != o.affected) {
			return false;
		}

		return pos.equals(o.pos);
	}

	@Override
	public Change<Board> merge(Change<Board> newer) {
		BoardChange n = (BoardChange) newer;
		assert (equals(n));

		Board theBrd = super.affected;
		Entity newestOcc = n.newOccupant;

		// empty is default
		if (newestOcc instanceof Empty) {
			return null;
		}

		return new BoardChange(theBrd, pos, newestOcc);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(pos);
	}
}
