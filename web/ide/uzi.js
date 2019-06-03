let Uzi = (function () {

  let id = Math.floor(Math.random() * (2**64));
  let baseUrl = "";
  let observers = [];

  let Uzi = {
    state: null,

    start: function (url) {
      baseUrl = url || "";
      updateLoop(true);
    },
    addObserver: function (fn) {
      observers.push(fn);
    },
    connect: function (port) {
      ajax.request({
  			type: 'POST',
  			url: baseUrl + Uzi.state.actions.connect.href,
  			data: {
  				id: id,
  				port: port
  			},
  			success: function (data) {
  				update(data);
  			},
  			error: errorHandler
  		}, 0);
    },
    disconnect: function () {
      ajax.request({
  			type: 'POST',
  			url: baseUrl + Uzi.state.actions.disconnect.href,
  			data: {
  				id: id,
  			},
  			success: function (data) {
  				update(data);
  			},
  			error: errorHandler
  		}, 0);
    },
  };

  function nop () { /* Do nothing */ }

  function errorHandler (err) {
    console.log(err);
  }

  function update(data) {
    Uzi.state = data;
    observers.forEach(function (fn) { fn(); });
  }

  function getUziState(wait, callbacks) {
    let success = callbacks.success || nop;
    let complete = callbacks.complete || nop;
    let error = callbacks.error || nop;
    ajax.request({
      type: 'GET',
      url: baseUrl + "/uzi",
      data: {
        id: id,
        wait: wait
      },
      success: success,
      complete: complete,
      error: error
    }, 1);
  }

  function updateLoop(first) {
    getUziState(first ? 0 : 45, {
      success: update,
      complete: function () { updateLoop(false); },
      error: errorHandler
    });
  }

  function start() {
    updateLoop(true);
  }

  return Uzi;
})();
