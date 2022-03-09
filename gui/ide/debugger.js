let Debugger = (function () {

  let selectedStackFrame = 0;

  function init() {
    Split(['#debugger-call-stack', '#debugger-locals', '#debugger-raw-stack'], {
      gutterSize: 5,
    });

    $("#debugger-break-button").on("click", sendBreak);
    $("#debugger-continue-button").on("click", sendContinue);
    $("#debugger-step-over-button").on("click", stepOver);
    $("#debugger-step-into-button").on("click", stepInto);
    $("#debugger-step-out-button").on("click", stepOut);
    $("#debugger-step-next-button").on("click", stepNext);
  }

  function sendBreak() {
    disableButtons();
    Uzi.debugger.break();
  }

  function sendContinue() {
    disableButtons();
    Uzi.debugger.continue();
  }

  function stepOver() {
    disableButtons();
    Uzi.debugger.stepOver();
  }

  function stepInto() {
    disableButtons();
    Uzi.debugger.stepInto();
  }

  function stepOut() {
    disableButtons();
    Uzi.debugger.stepOut();
  }

  function stepNext() {
    disableButtons();
    Uzi.debugger.stepNext();
  }

  function enableButtons() {
    $("#debugger-buttons button").attr("disabled", null);
  }

  function disableButtons() {
    $("#debugger-buttons button").attr("disabled", "disabled");
  }

  function update(state, previous, keys) {
    if (!keys.has("debugger")) return;

    selectedStackFrame = 0;
    updateButtons(state);
    updateDebugger(state);
    if (state.debugger.isHalted) {
      LayoutManager.showDebugger();
    }
    $("#debugger-output").text(JSON.stringify(state.debugger, null, 2));
  }

  function updateButtons(state) {
    if (state.debugger.isHalted) {
      enableButtons();
      $("#debugger-break-button").attr("disabled", "disabled");
    } else {
      disableButtons();
      $("#debugger-break-button").attr("disabled", null);
    }
  }

  function updateDebugger(state) {
    updateCallStack(state);
    updateLocals(state);
    updateRawStack(state);
  }

  function updateCallStack(state) {
    $("#debugger-call-stack-table").html("");
    if (!state.debugger.isHalted) return;

    let $body = $("<tbody>");
    state.debugger.stackFrames.forEach((stackFrame, i) => {
      let $tr = $("<tr>");
      if (i == selectedStackFrame) { $tr.addClass("bg-primary"); }
      let name = stackFrame.scriptName + "(" + stackFrame.arguments.map(arg => arg.name + ": " + arg.value).join(", ") + ")";
      let $td = $("<td>").addClass("px-4").text(name);
      $td.click(() => {
        selectedStackFrame = i;
        UziCode.handleDebuggerUpdate(state, selectedStackFrame); // HACK(Richo): We probably shoulnd't be calling this directly!
        UziBlock.handleDebuggerUpdate(state, selectedStackFrame); // HACK(Richo): We probably shoulnd't be calling this directly!
        updateDebugger(state);
      });
      $tr.append($td);
      $body.append($tr);
    });
    $("#debugger-call-stack-table").append($body);
  }

  function updateLocals(state) {
    $("#debugger-locals-table").html("");
    if (!state.debugger.isHalted) return;

    let stackFrame = state.debugger.stackFrames[selectedStackFrame];
    if (!stackFrame) return;
    let $body = $("<tbody>");
    stackFrame.arguments.forEach(v => {
      let $tr = $("<tr>");
      $tr.append($("<td>").addClass("px-4").text(v.name));
      $tr.append($("<td>").addClass("px-4 text-right").text(v.value));
      $body.append($tr);
    });
    stackFrame.locals.forEach(v => {
      let $tr = $("<tr>");
      $tr.append($("<td>").addClass("px-4").text(v.name));
      $tr.append($("<td>").addClass("px-4 text-right").text(v.value));
      $body.append($tr);
    });
    $("#debugger-locals-table").append($body);
  }

  function updateRawStack(state) {
    $("#debugger-raw-stack-table").html("");
    if (!state.debugger.isHalted) return;

    let $head = $("<thead>");
    $head.append($("<tr>")
      .append($("<th>").addClass("text-right").text("#"))
      .append($("<th>").addClass("text-right").text("HEX"))
      .append($("<th>").addClass("text-right").text("Value")));
    $("#debugger-raw-stack-table").append($head);

    let $body = $("<tbody>");
    let stackIndex = state.debugger.stackFrames.slice(selectedStackFrame).reduce((acc, sf) => acc + sf.stack.length, 0);
    for (let j = selectedStackFrame; j < state.debugger.stackFrames.length; j++) {
      let stack = state.debugger.stackFrames[j].stack;
      for (let i = stack.length - 1; i >= 0; i--) {
        let raw = stack[i]["raw-value"].map(n => n.toString(16).padStart(2, '0').toUpperCase()).join(" ");
        let description = stack[i]["description"];

        let $tr = $("<tr>");
        $tr.append($("<th>").addClass("text-right").text(--stackIndex));
        $tr.append($("<td>").addClass("text-right").text(raw));
        $tr.append($("<td>").addClass("text-right").text(description));
        $body.append($tr);
      }
    }
    $("#debugger-raw-stack-table").append($body);
  }

  return {
    init: init,
    update: update,

    getSelectedStackFrameIndex: () => selectedStackFrame,
  }
})();
