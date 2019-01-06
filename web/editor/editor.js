var UziEditor = (function () {	

	var breakpoints = [];
	var markers = [];
	
	function getValidLineForBreakpoint(line) {
		let valid = Uzi.program.validBreakpoints;
		for (let i = line; i < valid.length; i++) {
			if (valid[i] != null) return i;
		}
		return null;
	}
	
	function init() {
		
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
			UziDebugger.sendBreak();
		});
		
		$(".ace_gutter").on("click", function (e) { 
			var line = getValidLineForBreakpoint(Number.parseInt(e.target.innerText) - 1);
			
			if (breakpoints.includes(line)) {
				var index = breakpoints.indexOf(line);
				if (index > -1) { breakpoints.splice(index, 1); }
				editor.session.clearBreakpoint(line);
			} else {
				breakpoints.push(line);
				editor.session.setBreakpoint(line, "breakpoint");
			}
			editor.gotoLine(line + 1);
			sendBreakpoints();
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
			
			/*
			TODO(Richo): Here we should update the validBreakpoints list to insert
			null in every inserted line. Otherwise everything gets out of sync...
			*/
			
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
				breakpoints = [];
				editor.session.clearBreakpoints();
				markers.forEach(function (each) { editor.session.removeMarker(each); });
			}
		});
		
		$("#debugger_closeButton").on("click", function () {
			$("#debugger").hide();
			UziDebugger.sendContinue();
		});
		
		Uzi.onDebuggerUpdate(debuggerUpdate);
	}
	
	function debuggerUpdate() {
		if (Uzi.debugger.isHalted) { $("#debugger").show(); }
		
		editor.setReadOnly(Uzi.debugger.isHalted);
		
		markers.forEach(function (each) { editor.session.removeMarker(each); });
		let interval = UziDebugger.getCurrentInterval();
		if (interval == null) {
			markers = [];
		} else {
			let doc = editor.session.getDocument();
			let start = doc.indexToPosition(interval[0] - 1);
			let end = doc.indexToPosition(interval[1]);
			let range = new ace.Range(start.row, start.column, end.row, end.column);
			markers = [];
			markers.push(editor.session.addMarker(range, "debugger_ActiveLine", "line", true));
			markers.push(editor.session.addMarker(range, "debugger_ActiveInterval", "line", true));
		}
	}
	
	function sendBreakpoints() {
		let actualBreakpoints = breakpoints.map(function (line) {
			return Uzi.program.validBreakpoints[line];
		}).filter(function (bp) { return bp != null; });
		UziDebugger.setBreakpoints(actualBreakpoints);
	}
	
	return {
		init: init,
		debuggerUpdate: debuggerUpdate
	};	
})();