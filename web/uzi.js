(function () {
	
	function nop () { /* Do nothing */ }
	
	function errorHandler (err) {
		console.log(err);
		Alert.danger(err.responseText || "Connection error");
	}

	function getSelectedPort() {
		return port.innerText;
	}
	
	function getUziState(wait, callbacks) {
		var success = callbacks.success || nop;
		var complete = callbacks.complete || nop;
		var error = callbacks.error || nop;
		ajax.request({ 
			type: 'GET', 
			url: "/uzi",
			data: {
				wait: wait
			},
			success: success,
			complete: complete,
			error: error
		}, 2);
	}

	function connect () {
		ajax.request({ 
			type: 'POST', 
			url: "/uzi/actions/connect",
			data: {
				port: getSelectedPort()
			},
			success: function (uzi) {
				updateUI(uzi);
				if (uzi.isConnected) {
					Alert.success("Arduino connected on port: " + uzi.portName);
				} else {
					Alert.danger("Arduino not found");
				}
			},
			error: errorHandler
		}, 2);
	}

	function disconnect () {
		ajax.request({ 
			type: 'POST', 
			url: "/uzi/actions/disconnect",
			data: {
				port: getSelectedPort()
			},
			success: function (uzi) {
				updateUI(uzi);
				if (uzi.isConnected) {
					Alert.danger("Arduino connected on port: " + uzi.portName);
				} else {
					Alert.success("Arduino disconnected");
				}
			},
			error: errorHandler
		}, 2);
	}

	function compile() {
		ajax.request({ 
			type: 'POST', 
			url: "/uzi/actions/compile",
			data: {
				src: editor.getValue()
			},
			success: function (bytecodes) {
				console.log(bytecodes);
				Alert.success("Compilation successful");
			},
			error: errorHandler
		}, 2);
	}

	function install() {
		ajax.request({ 
			type: 'POST', 
			url: "/uzi/actions/install",
			data: {
				src: editor.getValue()
			},
			success: function (bytecodes) {
				console.log(bytecodes);
				Alert.success("Installation successful");
			},
			error: errorHandler
		}, 2);
	}

	function run() {
		ajax.request({ 
			type: 'POST', 
			url: "/uzi/actions/run",
			data: {
				src: editor.getValue()
			},
			success: function (bytecodes) {
				console.log(bytecodes);
			},
			error: errorHandler
		}, 2);
	}

	function bindEvents() {
		//Port list
		$("#portlist.dropdown-menu li a")
			.on("click", function () {
				var selectedPort;
				if (this.id) {
					selectedPort = prompt("Port name:", getSelectedPort());
				} else {
					selectedPort = this.innerText;
				}
				$("#port")
					.text(selectedPort)
					.append("\n<span class='caret'></span>");
			});
		
		// Buttons
		$("#connect").on("click", connect);
		$("#disconnect").on("click", disconnect);
		$("#compile").on("click", compile);
		$("#install").on("click", install);
		$("#run").on("click", run);
	}
	
	function updateUI(uzi) {
		if (uzi.isConnected) {
			$("#connect").attr("disabled", "disabled");
			$("#port")
				.attr("disabled", "disabled")
				.text(uzi.portName)
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
	}

	function updateLoop(first) {
		getUziState(first ? 0 : 45, {
			success: updateUI,
			complete: function () { updateLoop(false); },
			error: errorHandler
		});
	}
	
	bindEvents();
	updateLoop(true);
})();