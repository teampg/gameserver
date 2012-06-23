package teampg199.world.board;

import java.awt.Dimension;

public class BoardInfo {
	private final Dimension size;

	public BoardInfo(Dimension size) {
		this.size = size;
	}

	public Dimension getSize() {
		return (Dimension) size.clone();
	}
}
