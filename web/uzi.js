(function () {

	function getSelectedPort() {
		return port.innerText;
	}

	function connect () {
		ajax.request({ 
			type: 'POST', 
			url: "/uzi/actions/connect",
			data: {
				port: getSelectedPort()
			},
			success: function (uzi) {
				if (uzi.isConnected) {
					Alert.success("Arduino connected on port: " + uzi.portName);
					$("#connect").attr("disabled", "disabled");
					$("#port").attr("disabled", "disabled");
					$("#disconnect").removeAttr("disabled");					
					//$("#install").removeAttr("disabled");
					$("#run").removeAttr("disabled");
					$("#more").removeAttr("disabled");
				} else {
					Alert.danger("Arduino not found");
				}
			},
			error: function (err) {
				console.log(err);
				Alert.danger(err.responseText);
			}
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
				if (uzi.isConnected) {
					Alert.danger("Arduino connected on port: " + uzi.portName);
				} else {
					Alert.success("Arduino disconnected");
					$("#connect").removeAttr("disabled");
					$("#port").removeAttr("disabled");
					$("#disconnect").attr("disabled", "disabled");
					$("#install").attr("disabled", "disabled");
					$("#run").attr("disabled", "disabled");
					$("#more").attr("disabled", "disabled");
				}
			},
			error: function (err) {
				console.log(err);
				Alert.danger(err.responseText);
			}
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
			error: function (err) {
				console.log(err);
				Alert.danger(err.responseText);
			}
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
			error: function (err) {
				console.log(err);
				Alert.danger(err.responseText);
			}
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
			error: function (err) {
				console.log(err);
				Alert.danger(err.responseText);
			}
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
				port.innerHTML = selectedPort + "\n<span class='caret'></span>";
			});
		
		// Buttons
		$("#connect").on("click", connect);
		$("#disconnect").on("click", disconnect);
		$("#compile").on("click", compile);
		$("#install").on("click", install);
		$("#run").on("click", run);
		
	}

	bindEvents();

})();