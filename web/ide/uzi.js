let Uzi = (function () {

  let id = Math.floor(Math.random() * (2**64));
  let baseUrl = "";
  let observers = {
    "update" : [],
    "server-disconnect" : []
  };

  let Uzi = {
    state: null,
    serverAvailable: true,

    start: function (url) {
      baseUrl = url || "";
      updateLoop(true);
    },

    on: function (evt, callback) {
      observers[evt].push(callback);
    },

    connect: function (port) {
      let url = baseUrl + Uzi.state.actions.connect.href;
      ajax.POST(url, { id: id, port: port })
        .then(update)
        .catch(errorHandler);
    },

    disconnect: function () {
      let url = baseUrl + Uzi.state.actions.disconnect.href;
      ajax.POST(url, { id: id })
        .then(update)
        .catch(errorHandler);
    },

		compile: function (src, type, silent) {
      let url = baseUrl + Uzi.state.actions.compile.href;
      let data = {
        id: id,
        src: src,
        type: type,
        silent: silent == true
      };
  		ajax.POST(url, data)
        .then(log)
  			.catch(errorHandler);
  	},

    run: function (src, type, silent) {
      let url = baseUrl + Uzi.state.actions.run.href;
      let data = {
        id: id,
        src: src,
        type: type,
        silent: silent == true
      };
  		ajax.POST(url, data)
        .then(log)
        .catch(errorHandler);
  	},

    install: function (src, type) {
      let url = baseUrl + Uzi.state.actions.install.href;
      let data = {
        id: id,
        src: src,
        type: type
      };
      ajax.POST(url, data)
        .then(log)
        .catch(errorHandler);
    },

    setPinReport: function (pinNumber, reportEnabled) {
      let url = baseUrl + Uzi.state.actions.setPinReport.href;
      let data = {
        id: id,
        pinNumber: pinNumber,
        reportEnabled: reportEnabled
      };
      ajax.POST(url, data)
        .then(log)
        .catch(errorHandler);
    }
  };

  function log(data) {
    console.log(data);
  }

  function errorHandler (err) {
    console.log(err);
  }

  function serverDisconnect(error) {
    observers["server-disconnect"].forEach(function (fn) {
      try {
        fn(error);
      } catch (err) {
        console.log(err);
      }
    });
  }

  function update(data) {
    let previousState = Uzi.state;
    Uzi.state = data;
    observers["update"].forEach(function (fn) {
      try {
        fn(Uzi.state, previousState);
      } catch (err) {
        console.log(err);
      }
    });
  }

  function getUziState(wait) {
    let url = baseUrl + "/uzi";
    let data = { id: id, wait: wait };
    let priority = 1;
    return ajax.GET(url, data, priority);
  }

  function updateLoop(immediate) {
    getUziState(immediate ? 0 : 45)
      .then(function (data) {
        Uzi.serverAvailable = true;
        update(data);
        updateLoop(false);
      })
      .catch(function (err) {
        Uzi.serverAvailable = false;
        serverDisconnect(err);
        updateLoop(true);
      });
  }

  return Uzi;
})();
