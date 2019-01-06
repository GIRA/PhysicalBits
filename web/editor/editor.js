(function () {	

	var breakpoints = [];

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
	
	$(".ace_gutter").on("click", function (e) { 
		var line = Number.parseInt(e.target.innerText) - 1;
		if (breakpoints.includes(line)) {
			var index = breakpoints.indexOf(line);
			if (index > -1) { breakpoints.splice(index, 1); }
			editor.session.clearBreakpoint(line);
		} else {
			breakpoints.push(line);
			editor.session.setBreakpoint(line, "breakpoint");
		}
	});
	
	editor.on("change", function (e) { 
		let start = e.start.row;
		let delta = e.lines.length - 1;
		if (e.action == "insert") {
			delta *= 1;
		} else if (e.action == "remove") {
			delta *= -1;
		} else {
			debugger;
		}
		let bpts = breakpoints.filter(function (bp) { return bp > start; });
		breakpoints = breakpoints.filter(function (bp) { return bp <= start; });
		bpts.forEach(function (bp) { breakpoints.push(bp + delta); });
		editor.session.clearBreakpoints();
		breakpoints.forEach(function (line) {
			editor.session.setBreakpoint(line, "breakpoint");
		});
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