let UziCode = (function () {

  let editor;
  let focus = false;
	let breakpoints = [];
	let markers = [];
  let observers = {
    "change": []
  };

  function init() {

    editor = ace.edit("code-editor");
    editor.setTheme("ace/theme/ambiance");
    editor.getSession().setMode("ace/mode/uzi");

    editor.on("focus", function () { focus = true; });
    editor.on("blur", function () { focus = false; });
    editor.on("change", function (e) {
      trigger("change", focus);

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

    Uzi.on("update", function (state, previousState) {
      handleDebuggerUpdate(state);
      handleProgramUpdate(state, previousState);
    });
  }

  function handleProgramUpdate(state, previousState) {
    if (focus) return; // Don't change the code while the user is editing!
    if (state.program.type == "uzi") return; // Ignore textual programs
    if (editor.getValue() !== "" &&
        state.program.src == previousState.program.src) return;

    let src = state.program.src;
    if (src == undefined) return;
    if (editor.getValue() !== src) {
      editor.setValue(src, 1);

      // TODO(Richo): How do we preserve the breakpoints after a program update?
      breakpoints = [];
      editor.session.clearBreakpoints();
      markers.forEach(function (each) { editor.session.removeMarker(each); });
    }
  }

  function handleDebuggerUpdate(state) {
    try {
      let interval = null;
      if (state.debugger.stackFrames.length > 0) {
        interval = state.debugger.stackFrames[0].interval;
      }
      highlight(interval);
    } catch (err) {
      console.log(err);
    }
  }

	function getValidLineForBreakpoint(line) {
    return line;
    // TODO(Richo)
		let valid = Uzi.program.validBreakpoints;
		for (let i = line; i < valid.length; i++) {
			if (valid[i] != null) return i;
		}
		return null;
	}

	function sendBreakpoints() {
    console.log("SEND BREAKPOINTS!");
    return;

    // TODO(Richo)
		let actualBreakpoints = breakpoints.map(function (line) {
			return Uzi.program.validBreakpoints[line];
		}).filter(function (bp) { return bp != null; });
		UziDebugger.setBreakpoints(actualBreakpoints);
	}

  function highlight(interval) {
		markers.forEach((each) => { editor.session.removeMarker(each); });
		if (interval == null) {
			markers = [];
		} else {
			let doc = editor.session.getDocument();
			let start = doc.indexToPosition(interval[0]);
			let end = doc.indexToPosition(interval[1]);
			let range = new ace.Range(start.row, start.column, end.row, end.column);
			markers = [];
			markers.push(editor.session.addMarker(range, "debugger_ActiveLine", "line", true));
			markers.push(editor.session.addMarker(range, "debugger_ActiveInterval", "line", true));
		}
  }

  function resizeEditor() {
    if (editor) {// TODO(Richo): Is this condition necessary??
      editor.resize(true);
    }
  }

  function setProgram(code) {
    editor.setValue(code);
    return true;
  }

  function getProgram() {
    return editor.getValue();
  }

  function clearEditor() {
    editor.setValue("");
  }

  function on (evt, callback) {
    observers[evt].push(callback);
  }

  function trigger(evt, args) {
    observers[evt].forEach(function (fn) {
      try {
        fn(args);
      } catch (err) {
        console.error(err);
      }
    });
  }

  return {
    init: init,
    on: on,
    resizeEditor: resizeEditor,
    setProgram: setProgram,
    getProgram: getProgram,
    clearEditor: clearEditor,

    getBreakpoints: () => breakpoints,
  }
})();
