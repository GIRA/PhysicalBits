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
      let data = { id: id, port: port };
      return POST(url, data);
    },

    disconnect: function () {
      let url = baseUrl + Uzi.state.actions.disconnect.href;
      let data = { id: id };
      return POST(url, data);
    },

		compile: function (src, type, silent) {
      let url = baseUrl + Uzi.state.actions.compile.href;
      let data = {
        id: id,
        src: src,
        type: type,
        silent: silent == true
      };
      return POST(url, data);
  	},

    run: function (src, type, silent) {
      let url = baseUrl + Uzi.state.actions.run.href;
      let data = {
        id: id,
        src: src,
        type: type,
        silent: silent == true
      };
      return POST(url, data);
  	},

    install: function (src, type) {
      let url = baseUrl + Uzi.state.actions.install.href;
      let data = {
        id: id,
        src: src,
        type: type
      };
      return POST(url, data);
    },

    setPinReport: function (pins, report) {
      let url = baseUrl + Uzi.state.actions.pinReport.href;
      let data = {
        id: id,
        pins: Array.from(pins).join(","),
        report: Array.from(report).join(",")
      };
      return POST(url, data);
    },

    setGlobalReport: function (globals, report) {
      let url = baseUrl + Uzi.state.actions.globalReport.href;
      let data = {
        id: id,
        globals: Array.from(globals).join(","),
        report: Array.from(report).join(",")
      };
      return POST(url, data);
    }
  };

  function POST(url, data) {
    return ajax.POST(url, data);
  }

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
    setTimeout(function () {
      getUziState(immediate ? 0 : 45)
        .then(function (data) {
          Uzi.serverAvailable = true;
          update(fixInvalidJSONFloats(data));
          updateLoop(false);
        })
        .catch(function (err) {
          Uzi.serverAvailable = false;
          serverDisconnect(err);
          updateLoop(false);
        });
    }, 100);
  }

	/*
	HACK(Richo): This function will fix occurrences of Infinity, -Infinity, and NaN
	in the JSON object resulting from a server response. Since JSON	doesn't handle
  these values correctly I'm encoding them in a special way.
	*/
  function fixInvalidJSONFloats(obj) {
    if (obj instanceof Array) return obj.map(fixInvalidJSONFloats);
    if (typeof obj != "object") return obj;
    if (obj === null) return null;
    if (obj === undefined) return undefined;

    if (obj["___INF___"] !== undefined) {
      return Infinity * obj["___INF___"];
    } else if (obj["___NAN___"] !== undefined) {
      return NaN;
    }

    let value = {};
    for (let m in obj) {
      value[m] = fixInvalidJSONFloats(obj[m]);
    }
    return value;
  }

  Uzi.fixJSON = fixInvalidJSONFloats;

  return Uzi;
})();
