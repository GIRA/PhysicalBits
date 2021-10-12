let Debugger = (function () {

  function init() {
    // TOOD(Richo)
  }

  function update(state) {
    $("#debugger-output").text(JSON.stringify(state.debugger, null, 2));
  }

  return {
    init: init,
    update: update,
  }
})();
