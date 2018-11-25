(function () {	
	$("#compile").on("click", function () {
		Uzi.compile(editor.getValue(), "text", function (bytecodes) {			
			console.log(bytecodes);
			Alert.success("Compilation successful");
		});
	});

	$("#install").on("click", function () {
		Uzi.install(editor.getValue(), "text", function (bytecodes) {			
			console.log(bytecodes);
			Alert.success("Installation successful");
		});
	});

	$("#run").on("click", function () {
		Uzi.run(editor.getValue(), "text", function (bytecodes) {
			console.log(bytecodes);
		});
	});
	
	$("#debug").on("click", function() {
		Alert.danger("Debugger not implemented yet");
	});

	Uzi.onConnectionUpdate(function () {
		if (Uzi.isConnected) {				
			$("#install").removeAttr("disabled");
			$("#run").removeAttr("disabled");
			$("#more").removeAttr("disabled");
		} else {
			$("#install").attr("disabled", "disabled");
			$("#run").attr("disabled", "disabled");
			$("#more").attr("disabled", "disabled");
		}
	});
	
	Uzi.onProgramUpdate(function () {
		if (editor.getValue() !== Uzi.program.src) {
			editor.setValue(Uzi.program.src);
		}
	});
})();