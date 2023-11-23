let UziCode = (function () {

  let editor;
  let focus = false;
  let updating = false;
	let markers = [];
  let observers = {
    "change": [],
    "cursor": []
  };

  function init() {

    editor = ace.edit("code-editor");
    editor.setTheme("ace/theme/ambiance");
    editor.getSession().setMode("ace/mode/uzi");
    editor.setHighlightSelectedWord(false);
    editor.setShowFoldWidgets(false);
    editor.setShowPrintMargin(false);

    editor.selection.on("changeCursor", handleCursorChange)
    editor.on("focus", function () { 
      focus = true; 
      handleCursorChange();
    });
    editor.on("blur", function () { focus = false; });
    editor.on("change", function (e) {
      trigger("change", focus);
      
      if (updating) return;

      let start = e.start.row;
      let delta = e.lines.length - 1;
      if (e.action == "insert") {
        delta *= 1;
      } else if (e.action == "remove") {
        delta *= -1;
      } else {
        debugger;
      }

      if (focus) {
        Uzi.elog("CODE/CHANGE", e);
      }

      /*
      TODO(Richo): Here we should update the validBreakpoints list to insert
      null in every inserted line. Otherwise everything gets out of sync...
      NOTE(Richo): The reason it's (kinda) working right now is that we're 
      not using the validBreakpoints anymore. IIRC the server used to send us
      a list of lines where it was valid to set a breakpoint, but now I think
      I remove it (I don't remember why, though, probably simplicity)
      */
     
      let breakpoints = new Set();
      editor.session.clearBreakpoints();
      Debugger.getBreakpoints().forEach(bp => {
        // If the breakpoint is after the edit start we add the edit delta, otherwise we
        // leave it as is
        let line = bp <= start ? bp : bp + delta;
        breakpoints.add(line);
        editor.session.setBreakpoint(line, "breakpoint");
      });
    });

		$(".ace_gutter").on("click", function (e) {
      // TODO(Richo): Sometimes the editor and the src get out of sync when they shouldn't
      // I still don't know why! But it bothers when setting breakpoints and in some other 
      // places as well (like when selecting blocks from the code editor)
      if (editor.getValue() !== Uzi.state.program.src) return;
      if (!Uzi.state.features["debugging?"]) return;
      
      var line = Debugger.getValidLineForBreakpoint(Number.parseInt(e.target.innerText) - 1);
      Debugger.toggleBreakpoint(line);
      if (Debugger.getBreakpoints().has(line)) {
        editor.session.setBreakpoint(line, "breakpoint");
      } else {
        editor.session.clearBreakpoint(line);
      }
      editor.gotoLine(line + 1);
		});

    Uzi.on("update", function (state, previousState, keys) {
      updating = true;
      try {
        if (keys.has("program")) {
          handleProgramUpdate(state, previousState);
        }
      } catch (err) {
        console.error(err);
      } finally {
        updating = false;
      }
    });

    Debugger.on("change", handleDebuggerUpdate);
  }

  function handleCursorChange() {
    if (!focus) return;
    let doc = editor.session.getDocument();

    let col = editor.selection.cursor.column;
    let row = editor.selection.cursor.row;
    let idx = doc.positionToIndex({row: row, column: col});
    trigger("cursor", idx);
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

  function handleDebuggerUpdate(state, stackFrameIndex) {
    try {
      editor.setReadOnly(state.debugger.isHalted);

      let interval = null;
      let src = state.program.src;
      if (state.debugger.stackFrames.length > 0) {
        let stackFrame = state.debugger.stackFrames[stackFrameIndex];
        src = state.debugger.sources[stackFrame.source];
        interval = stackFrame.interval;
      }
      
      if (!focus && editor.getValue() !== src) {
        editor.setValue(src, 1);
      }
      highlight(interval);

      breakpoints = state.debugger.breakpoints;
      editor.session.clearBreakpoints();
      if (src == state.program.src) {
        breakpoints.forEach(function (line) {
          editor.session.setBreakpoint(line, "breakpoint");
        });
      }
    } catch (err) {
      console.log(err);
    }
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
      editor.scrollToLine(start.row - 3);
		}
  }

  function select(interval) {
    if (focus) return;
    if (interval == null || interval.length < 2) {
      editor.clearSelection();
    } else {
      let doc = editor.session.getDocument();
      let start = doc.indexToPosition(interval[0]);
      let end = doc.indexToPosition(interval[1]);
      let range = new ace.Range(start.row, start.column, end.row, end.column);
      editor.selection.setSelectionRange(range);
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

    handleDebuggerUpdate: handleDebuggerUpdate,
    select: select,

    getEditor: () => editor,
    isFocused: () => focus,
  }
})();
