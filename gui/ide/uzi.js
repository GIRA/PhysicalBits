let Uzi = (function () {

  let id = Math.floor(Math.random() * (2**64));
  let host = "";
  let apiURL = "";
  let wsURL = "";
  let observers = {
    "update" : [],
    "server-disconnect" : []
  };


  let Uzi = {
    state: null,
    serverAvailable: true,
    socket: null,

    start: function (preferredHost) {
      host = preferredHost || "";
      apiURL = host ? "http://" + host : "";
      wsURL = "ws://" + (host || location.host);

      return new Promise((resolve, reject) => {
        let i = 0;
        let begin = +new Date();
        function connect() {
          console.log("ATTEMPT: " + (++i));
          console.log("Elapsed time: " + ((+new Date()) - begin));
          updateLoop().then(resolve).catch(err => {
            setTimeout(connect, 500);
          });
        }
        connect();
      });
    },

    on: function (evt, callback) {
      observers[evt].push(callback);
    },

    connect: function (port) {
      let url = apiURL + "/uzi/connect";
      let data = { id: id, port: port };
      return POST(url, data);
    },

    disconnect: function () {
      let url = apiURL + "/uzi/disconnect";
      let data = { id: id };
      return POST(url, data);
    },

		compile: function (src, type, silent) {
      let url = apiURL + "/uzi/compile";
      let data = {
        id: id,
        src: src,
        type: type,
        silent: silent == true
      };
      return POST(url, data);
  	},

    run: function (src, type, silent) {
      let url = apiURL + "/uzi/run";
      let data = {
        id: id,
        src: src,
        type: type,
        silent: silent == true
      };
      return POST(url, data);
  	},

    install: function (src, type) {
      let url = apiURL + "/uzi/install";
      let data = {
        id: id,
        src: src,
        type: type
      };
      return POST(url, data);
    },

    setPinReport: function (pins, report) {
      let url = apiURL + "/uzi/pin-report";
      let data = {
        id: id,
        pins: Array.from(pins).join(","),
        report: Array.from(report).join(",")
      };
      return POST(url, data);
    },

    setGlobalReport: function (globals, report) {
      let url = apiURL + "/uzi/global-report";
      let data = {
        id: id,
        globals: Array.from(globals).join(","),
        report: Array.from(report).join(",")
      };
      return POST(url, data);
    },

    setProfile: function (enabled) {
      let url = apiURL + "/uzi/profile";
      let data = {
        id: id,
        enabled: enabled
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

  function reconnect() {
    updateLoop().catch(err => {
      setTimeout(reconnect, 1000);
    });
  }

  function serverDisconnect(error) {
    observers["server-disconnect"].forEach(function (fn) {
      try {
        fn(error);
      } catch (err) {
        console.log(err);
      }
    });
    reconnect();
  }

  function update(data) {
    /*
    TODO(Richo): Using JSONX stringify/parse is probably slower than deep cloning
    the object manually. Benchmark and refactor!
    */
    let previousState = JSONX.parse(JSONX.stringify(Uzi.state));
    if (Uzi.state == null) {
      Uzi.state = data;
    } else {
      Object.keys(data).forEach(key => Uzi.state[key] = data[key]);
    }
    observers["update"].forEach(function (fn) {
      try {
        fn(Uzi.state, previousState);
      } catch (err) {
        console.log(err);
      }
    });
  }

  function updateLoop() {
    return new Promise((resolve, reject) => {
      try {
        let socket = new WebSocket(wsURL + "/uzi");
        let msgReceived = false;
        socket.onerror = function (err) {
          reject(err);
        };
        socket.onopen = function () {
          Uzi.serverAvailable = true;
          socket.onmessage = function (evt) {
            try {
              let msg = evt.data;
              let data = JSONX.parse(msg);
              update(data);
              if (!msgReceived) {
                msgReceived = true;
                resolve();
              }
            } catch (e) {
              console.log(e);
            }
          };
          socket.onclose = function(evt) {
            Uzi.serverAvailable = false;
            serverDisconnect(evt);
          };
        };
        Uzi.socket = socket;
      } catch (ex) {
        reject(ex);
      }
    });
  }

  return Uzi;
})();
