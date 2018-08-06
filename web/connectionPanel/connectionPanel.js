$(document).ready(function () {
	var portList = $("#connectionPanel_portlist.dropdown-menu li a");
	var port = $("#connectionPanel_port");
	var connectButton = $("#connectionPanel_connect");
	var disconnectButton = $("#connectionPanel_disconnect");
	
	function getSelectedPort() {
		return port.text().trim();
	}
	
	portList.on("click", function () {
		var selectedPort;
		if (this.id) {
			selectedPort = prompt("Port name:", getSelectedPort());
		} else {
			selectedPort = $(this).text();
		}
		port.text(selectedPort)
			.append("\n<span class='caret'></span>");
	});

	connectButton.on("click", function () {
		Uzi.connect(getSelectedPort(), function () {			
			if (Uzi.isConnected) {
				Alert.success("Arduino connected on port: " + Uzi.portName);
			} else {
				Alert.danger("Arduino not found");
			}
		});
	});
	
	disconnectButton.on("click", function () {
		Uzi.disconnect(function () {			
			if (Uzi.isConnected) {
				Alert.success("Arduino connected on port: " + Uzi.portName);
			} else {
				Alert.danger("Arduino disconnected");
			}
		});
	});

	Uzi.onError(function (err) {
		Alert.danger(err.responseText || "Connection error");
	});

	Uzi.onConnectionUpdate(function () {
		if (Uzi.isConnected) {
			connectButton.attr("disabled", "disabled");
			port.attr("disabled", "disabled")
				.text(Uzi.portName)
				.append("\n<span class='caret'></span>");
			disconnectButton.removeAttr("disabled");
		} else {
			connectButton.removeAttr("disabled");
			port.removeAttr("disabled");
			disconnectButton.attr("disabled", "disabled");
		}
	});

});