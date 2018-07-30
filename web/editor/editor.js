
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

Uzi.onUpdate(function () {
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