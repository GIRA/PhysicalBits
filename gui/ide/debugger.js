let Debugger = (function () {

  function init() {
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

  function update(state) {
    if (state.debugger.isHalted) {
      enableButtons();
      $("#debugger-break-button").attr("disabled", "disabled");
    } else {
      disableButtons();
      $("#debugger-break-button").attr("disabled", null);
    }
    $("#debugger-output").text(JSON.stringify(state.debugger, null, 2));
  }

  return {
    init: init,
    update: update,
  }
})();
