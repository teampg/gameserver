package teampg199.server.translator;


import java.io.IOException;
import java.util.List;


import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import teampg.grid2d.point.RelPos;
import teampg199.entity.Entity;
import teampg199.entity.dyn.EntityChange;
import teampg199.entity.dyn.acting.Bullet;
import teampg199.entity.dyn.acting.DrunkMob;
import teampg199.entity.dyn.acting.PushableBlock;
import teampg199.entity.dyn.acting.Remains;
import teampg199.entity.dyn.acting.TrackingMob;
import teampg199.entity.dyn.acting.player.ActionCommand;
import teampg199.entity.dyn.acting.player.ActionUpdate;
import teampg199.entity.dyn.acting.player.JoinCmd;
import teampg199.entity.dyn.acting.player.MoveCmd;
import teampg199.entity.dyn.acting.player.MsgCmd;
import teampg199.entity.dyn.acting.player.JoinCmd.JoinUpdate;
import teampg199.entity.dyn.acting.player.Player;
import teampg199.entity.dyn.acting.player.ShootCmd;
import teampg199.entity.dyn.acting.spawner.EnemySpawner;
import teampg199.entity.dyn.acting.spawner.PlayerSpawnShrine;
import teampg199.entity.stat.Empty;
import teampg199.entity.stat.EntityManager.AddRemEntChange;
import teampg199.entity.stat.Wall;
import teampg199.playerconnection.PlayerConnection.Message;
import teampg199.playerconnection.PlayerConnection.PrivateUpdates;
import teampg199.server.PageChanges;
import teampg199.server.TranslatingBuffer.TurnUpdates;
import teampg199.world.board.BoardChange;

public class Translator {
	private final ObjectMapper mapper;

	private final EntityIDManager entityIDs;
	private final ActionIDManager actionIDs;

	public Translator() {
		mapper = new ObjectMapper();

		entityIDs = new EntityIDManager();
		actionIDs = new ActionIDManager();
	}

	public ActionCommand toAction(String jsonString) throws JsonParseException,
			JsonMappingException, IOException, MalformedPizzaProtocolException {
		JsonNode rootNode = mapper.readValue(jsonString, JsonNode.class);

		if (!rootNode.has("act_id")) {
			throw new MalformedPizzaProtocolException("Missing act_id",
					jsonString);
		}
		if (!rootNode.get("act_id").canConvertToInt()) {
			throw new MalformedPizzaProtocolException("act_id not numeric",
					jsonString);
		}
		if (!(rootNode.get("act_id").asInt() > 0)) {
			throw new MalformedPizzaProtocolException(
					"act_id must be greater than 0", jsonString);
		}

		if (!rootNode.has("type")) {
			throw new MalformedPizzaProtocolException("Missing type",
					jsonString);
		}
		if (!rootNode.has("params")) {
			throw new MalformedPizzaProtocolException("Missing params",
					jsonString);
		}

		JsonNode typeNode = rootNode.path("type");
		JsonNode paramsNode = rootNode.path("params");
		JsonNode actIDNode = rootNode.path("act_id");

		int actID = actIDNode.asInt();

		ActionCommand action = null;

		switch (typeNode.asText()) {
		case "join":
			if (!paramsNode.has("name")) {
				throw new MalformedPizzaProtocolException(
						"Missing player name", jsonString);
			}

			String playerName = paramsNode.get("name").asText();
			action = new JoinCmd(playerName);
			break;
		case "say":
			String text = paramsNode.get("text").asText();
			action = new MsgCmd(text);
			break;
		case "move":
			 int xVector = paramsNode.get("x_vector").asInt();
			 int yVector = paramsNode.get("y_vector").asInt();
			 action = new MoveCmd(RelPos.of(xVector, yVector));
			 break;
		case "shoot":
			 xVector = paramsNode.get("x_vector").asInt();
			 yVector = paramsNode.get("y_vector").asInt();
			 action = new ShootCmd(RelPos.of(xVector, yVector));
			 break;
		default:
			throw new MalformedPizzaProtocolException("type not recognized",
					jsonString);
		}

		actionIDs.setID(action, actID);
		return action;
	}

	public String toJSON(TurnUpdates turnUpdates)
			throws JsonGenerationException, JsonMappingException, IOException {
		PageChanges pageChanges = turnUpdates.getPageChanges();

		// build skeleton json
		ObjectNode root = mapper.createObjectNode();

		// do new board section
		{
			ObjectNode nodeNewBoardInfo = root.putObject("new_board");

			// if new board, put info, otherwise leave section empty
			if (turnUpdates.getBoardInfo() != null) {
				nodeNewBoardInfo.put("width", turnUpdates.getBoardInfo()
						.getSize().width);
				nodeNewBoardInfo.put("height", turnUpdates.getBoardInfo()
						.getSize().height);
			}
		}

		// do Board Changes section
		{
			ArrayNode nodeBoard = root.putArray("board_changes");
			List<BoardChange> boardChanges = pageChanges.getBoardChanges();

			for (BoardChange ch : boardChanges) {
				ObjectNode chRoot = nodeBoard.addObject();

				chRoot.put("x", ch.getPos().x);
				chRoot.put("y", ch.getPos().y);

				Entity ent = ch.getNewOccupant();
				chRoot.put("entity_id", entityIDs.getID(ent));
			}
		}

		// do Entity Changes section
		{
			ArrayNode nodeEntity = root.putArray("entity_changes");
			List<EntityChange> entityChanges = pageChanges.getEntityChanges();

			for (EntityChange ch : entityChanges) {
				// TODO entity changes
				throw new IllegalStateException(
						"Entity Changes not implemented");
			}
		}

		// do Add Entity Changes section
		{
			ArrayNode nodeNewEntity = root.putArray("add_entity_changes");
			List<AddRemEntChange> newEntChanges = pageChanges
					.getNewEntityChanges();

			for (AddRemEntChange ch : newEntChanges) {
				// only do added entity changes
				if (ch.getChangeType() == AddRemEntChange.Type.CREATED) {
				} else if (ch.getChangeType() == AddRemEntChange.Type.DESTROYED) {
					continue;
				} else {
					throw new IllegalStateException(
							"Undefined AddRemEntChange Type");
				}

				ObjectNode chRoot = nodeNewEntity.addObject();

				Entity addedEntity = ch.getEntity();

				chRoot.put("id", entityIDs.getID(addedEntity));
				chRoot.put("entity_type",
						ProtocolNameMap.get(addedEntity.getClass()));

				ObjectNode attrNode = chRoot.putObject("attr");
				if (addedEntity instanceof Player) {
					attrNode.put("name", ((Player) addedEntity).getName());
				} else if (addedEntity instanceof Wall) {
				} else if (addedEntity instanceof PlayerSpawnShrine) {
				} else if (addedEntity instanceof Empty) {
				} else if (addedEntity instanceof TrackingMob) {
				} else if (addedEntity instanceof PushableBlock) {
				} else if (addedEntity instanceof DrunkMob) {
				} else if (addedEntity instanceof Bullet) {
				} else if (addedEntity instanceof Remains) {
				} else if (addedEntity instanceof EnemySpawner) {
				} else {
					throw new IllegalStateException(
							"Cannot add entity. Entity type not implemented");
				}
			}
		}

		// do Remove Entity Changes section
		{
			ArrayNode nodeRemoveEntity = root.putArray("remove_entity_changes");
			List<AddRemEntChange> newEntChanges = pageChanges
					.getNewEntityChanges();

			for (AddRemEntChange ch : newEntChanges) {
				// only do removed entity changes
				if (ch.getChangeType() == AddRemEntChange.Type.DESTROYED) {
				} else if (ch.getChangeType() == AddRemEntChange.Type.CREATED) {
					continue;
				} else {
					throw new IllegalStateException(
							"Undefined AddRemEntChange Type");
				}

				ObjectNode chRoot = nodeRemoveEntity.addObject();

				Entity removedEntity = ch.getEntity();

				chRoot.put("id", entityIDs.getID(removedEntity));
			}
		}

		// get act updates and messages
		PrivateUpdates privateUpdates = turnUpdates.getPrivateUpdates();

		// do Action Updates section
		{
			ArrayNode nodeAction = root.putArray("action_changes");
			List<ActionUpdate> actionUpdates = privateUpdates.getActionUpdates();

			for (ActionUpdate ch : actionUpdates) {
				ObjectNode chRoot = nodeAction.addObject();

				ActionCommand action = ch.getForAction();
				chRoot.put("act_id", actionIDs.getID(action));
				chRoot.put("type", ProtocolNameMap.get(action.getClass()));

				ObjectNode attrNode = chRoot.putObject("attr");
				if (action instanceof JoinCmd) {
					JoinUpdate j = (JoinUpdate) ch;
					Player avatar = j.getAvatar();

					attrNode.put("avatar", entityIDs.getID(avatar));
					attrNode.put("name", avatar.getName());
				} else {
					throw new IllegalStateException(
							"ActionChange type not implemented");
				}
			}
		}

		// do Messages section
		{
			ArrayNode nodeMessages = root.putArray("received_messages");
			List<Message> receivedMessages = privateUpdates.getReceivedMessages();

			for (Message m : receivedMessages) {
				ObjectNode msgRoot = nodeMessages.addObject();

				msgRoot.put("from", entityIDs.getID(m.getFrom()));
				msgRoot.put("text", m.getText());
			}
		}

		//Logger.log("Built change: "
		//		+ mapper.writerWithDefaultPrettyPrinter().writeValueAsString(
		//				root));
		return mapper.writeValueAsString(root);
	}
}
