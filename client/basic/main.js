var displayMap =
{
	"empty" : "&nbsp;",
	"player" : "<span style=\"color:red;font-weight:bold\">P</span>",
	"enemy" : "<span style=\"color:green\">G</span>",
	"wall" : "#",
	"spawn_shrine" : "<span style=\"color:yellow\">S</span>",
	"bullet" : "<span style=\"color:white;font-weight:bold\">*</span>",
	"pushable_block" : "<span style=\"color:brown\">+</span>",
	"wanderer" : "<span style=\"color:navy\">W</span>",
	"corpse" : "<span style=\"color:gray\">~</span>",
	"enemy_spawner" : "<span style=\"color:gray\">E</span>"
};

$(document).keypress(function(event) {
	switch (event.keyCode) {
	case 119: // w
	case 97: // a
	case 115: // s
	case 100: // d
		doMove(event.keyCode);
		break;
	case 32:
		doShoot();
	default:
	}
});

function Entity(inType, inID) {
	this._type = inType;
	this._ID = inID;

	this.getType = function() {
		return this._type;
	};

	this.getID = function() {
		return this._ID;
	};
}

function Board(inWidth, inHeight, inEmptyID) {
	// setup
	this._width = inWidth;
	this._height = inHeight;
	this._grid = new Array(inHeight);
	for ( var _y = 0; _y < inHeight; _y++) {
		this._grid[_y] = new Array(inWidth);
	}
	// populate with empty
	for ( var _y = 0; _y < inHeight; _y++) {
		for ( var x = 0; x < inWidth; x++) {
			this._grid[_y][x] = new Entity("empty", inEmptyID);
		}
	}

	// methods
	this.put = function(inX, inY, inEnt) {
		this._grid[inY][inX] = inEnt;
	};
	this.get = function(inX, inY) {
		return this._grid[inY][inX];
	};

	// getters
	this.getWidth = function() {
		return this._width;
	};
	this.getHeight = function() {
		return this._height;
	};

	this.toString = function() {
		var ret = "<table>";

		for ( var y = 0; y < this._height; y++) {
			ret += "<tr>";
			for ( var x = 0; x < this._width; x++) {
				var anEnt = this._grid[y][x];
				var name = displayMap[anEnt.getType()];// + anEnt.getID() +
				// "&nbsp;";
				if (anEnt.getType() === "empty") {
					name = "&nbsp;";
				}

				ret += "<td>" + name + "</td>";
			}
			ret += "</tr>";
		}

		return ret + "</table>";
	};
}

var ws;
var map = null;
var ents =
{
	"_list" : [],
	"getByID" : function(inID) {
		for ( var i = 0; i < this._list.length; i++) {
			var anEnt = this._list[i];
			if (anEnt.getID() === inID) {
				return anEnt;
			}
		}
		return null;
	},
	"put" : function(inEnt) {
		this._list.push(inEnt);
	},
	"remove" : function(inID) {
		for ( var i = 0; i < this._list.length; i++) {
			if (this._list[i].getID() === inID) {
				this._list.splice(i, 1);
				return;
			}
		}

		console.log("REMOVE ENT: Could not find " + inEnt.getID()
				+ " to remove from entList");
	}
};

function sendTextToServer(text) {
	ws.send(text);
}

var placeholderIDCount = 2;
var dir = 100;
function doMove(pressedKeyCode) {
	var x = 0;
	var y = 0;

	switch (pressedKeyCode) {
	case 119: // w
		y = 1;
		dir = 119;
		break;
	case 97: // a
		x = -1;
		dir = 97;
		break;
	case 115: // s
		y = -1;
		dir = 115;
		break;
	case 100: // d
		x = 1;
		dir = 100;
		break;
	default:
		return;
	}

	sendTextToServer("{\"type\":\"move\",\"act_id\":" + (placeholderIDCount++)
			+ ",\"params\":{\"x_vector\":" + x + ",\"y_vector\":" + y + "}}");
	console.log("Moved: x" + x + " y" + y);
}

function doShoot() {
	var x = 0;
	var y = 0;

	switch (dir) {
	case 119: // up
		y = 1;
		break;
	case 97: // down
		x = -1;
		break;
	case 115: // left
		y = -1;
		break;
	case 100: // right
		x = 1;
		break;
	default:
		return;
	}

	sendTextToServer("{\"type\":\"shoot\",\"act_id\":" + (placeholderIDCount++)
			+ ",\"params\":{\"x_vector\":" + x + ",\"y_vector\":" + y + "}}");
	console.log("Shooting: x" + x + " y" + y);
}

var entities = [];

window.onload = function() {
	// ws = new WebSocket("ws://204.174.60.164:9252"); //school
	// ws = new WebSocket("ws://24.68.55.144:9252"); //jackson
	ws = new WebSocket("ws://localhost:9252");

	ws.onopen = function(event) {
		console.log('Connected to server.');
		sendTextToServer("{\"type\":\"join\",\"act_id\":1,\"params\":{\"name\":\"bob\"}}");
	};

	ws.onmessage = function(event) {
		var msg = JSON.parse(event.data);

		// initial map load
		if (map === null) {
			var emptyID = null;
			for ( var i = 0; i < msg.add_entity_changes.length; i++) {
				if (msg.add_entity_changes[i].entity_type === "empty") {
					emptyID = msg.add_entity_changes[i].id;
					break;
				}
			}
			if (emptyID === null) {
				console.log("didn't find empty in initial message :(");
				return;
			}

			map = new Board(msg.new_board.width, msg.new_board.height, emptyID);
		}

		// put added entities
		for ( var i = 0; i < msg.add_entity_changes.length; i++) {
			var addEntCh = msg.add_entity_changes[i];
			ents.put(new Entity(addEntCh.entity_type, parseInt(addEntCh.id)));
		}

		// remove removed entities
		for ( var i = 0; i < msg.remove_entity_changes.length; i++) {
			var remEntCh = msg.remove_entity_changes[i];
			ents.remove(remEntCh.id);
		}

		// load board updates
		for ( var i = 0; i < msg.board_changes.length; i++) {
			var boardCh = msg.board_changes[i];

			var theEnt = ents.getByID(boardCh.entity_id);
			map.put(boardCh.x, boardCh.y, theEnt);
		}

		// get received messages
		for ( var i = 0; i < msg.received_messages.length; i++) {
			var message = msg.received_messages[i];

			var from_id = message.from;
			var text = message.text;

			console.log("FROM " + from_id + ": " + text);
		}

		document.getElementById("board").innerHTML = map.toString();
	};

	ws.onerror = function(error) {
		console.log(error.text);
		Chat.out('Error. ' + error, 'Console');
	};

	ws.close = function(event) {
		Chat.out('Disconnected.', 'Console.');
	};
};
