
//Port list
$("#portlist.dropdown-menu li a")
	.on("click", function () {
		var selectedPort;
		if (this.id) {
			selectedPort = prompt("Port name:", port.innerText);
		} else {
			selectedPort = this.innerText;
		}
		$("#port")
			.text(selectedPort)
			.append("\n<span class='caret'></span>");
	});

// Buttons
$("#connect").on("click", function () {
	Uzi.connect(port.innerText, function () {			
		if (Uzi.isConnected) {
			Alert.success("Arduino connected on port: " + Uzi.portName);
		} else {
			Alert.danger("Arduino not found");
		}
	});
});
$("#disconnect").on("click", function () {
	Uzi.disconnect(function () {			
		if (Uzi.isConnected) {
			Alert.success("Arduino connected on port: " + Uzi.portName);
		} else {
			Alert.danger("Arduino disconnected");
		}
	});
});
$("#compile").on("click", function () {
	Uzi.compile(editor.getValue(), function (bytecodes) {			
		console.log(bytecodes);
		Alert.success("Compilation successful");
	});
});
$("#install").on("click", function () {
	Uzi.install(editor.getValue(), function (bytecodes) {			
		console.log(bytecodes);
		Alert.success("Installation successful");
	});
});
$("#run").on("click", function () {
	Uzi.run(editor.getValue(), function (bytecodes) {			
		console.log(bytecodes);
	});
});

Uzi.onError = function (err) {
	Alert.danger(err.responseText || "Connection error");
}

Uzi.onUpdate = function () {
	if (Uzi.isConnected) {
		$("#connect").attr("disabled", "disabled");
		$("#port")
			.attr("disabled", "disabled")
			.text(Uzi.portName)
			.append("\n<span class='caret'></span>");
		$("#disconnect").removeAttr("disabled");					
		$("#install").removeAttr("disabled");
		$("#run").removeAttr("disabled");
		$("#more").removeAttr("disabled");
	} else {
		$("#connect").removeAttr("disabled");
		$("#port").removeAttr("disabled");
		$("#disconnect").attr("disabled", "disabled");
		$("#install").attr("disabled", "disabled");
		$("#run").attr("disabled", "disabled");
		$("#more").attr("disabled", "disabled");
	}
};