package teampg199.playerconnection;

import teampg199.server.PageChanges;
import teampg199.world.board.BoardInfo;

/**
 * Holds most recent Changes for entire game
 *
 * @author JWill
 */
public class GameStateTracker {
	private final PageChanges currGame;
	private final BoardInfo boardInfo;

	public GameStateTracker(BoardInfo boardInfo) {
		currGame = new PageChanges();
		this.boardInfo = boardInfo;
	}

	public void addTurnChanges(PageChanges toAddCh) {
		currGame.merge(toAddCh);
	}

	public PageChanges getCurrentGameState() {
		return (PageChanges) currGame.clone();
	}

	public BoardInfo getBoardInfo() {
		return boardInfo;
	}
}
