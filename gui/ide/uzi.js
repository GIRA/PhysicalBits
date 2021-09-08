let Uzi = (function () {

  let id = Math.floor(Math.random() * (2**64));
  let host = "";
  let apiURL = "";
  let wsURL = "";
  let observers = {
    "update" : [],
    "server-disconnect" : []
  };

  // HACK(Richo): If the middleware is loaded we run locally
  let localFlag = window.middleware != undefined;


  let Uzi = {
    /*
    NOTE(Richo): Normally the state can be an empty object because once we connect
    to the server the initial value is supplied. However, since we now work fully locally
    we might not have the initial state from the server so I'm initializing it with default
    values. This way if we don't connect to the server the IDE doesn't break.
    */
    state: {
      connection: {
        availablePorts: [],
      },
      output: [],
      pins: {
        available: [
          {name: "D2", reporting: false},
          {name: "D3", reporting: false},
          {name: "D4", reporting: false},
          {name: "D5", reporting: false},
          {name: "D6", reporting: false},
          {name: "D7", reporting: false},
          {name: "D8", reporting: false},
          {name: "D9", reporting: false},
          {name: "D10", reporting: false},
          {name: "D11", reporting: false},
          {name: "D12", reporting: false},
          {name: "D13", reporting: false},
          {name: "A0", reporting: false},
          {name: "A1", reporting: false},
          {name: "A2", reporting: false},
          {name: "A3", reporting: false},
          {name: "A4", reporting: false},
          {name: "A5", reporting: false},
        ],
        elements: []
      },
      globals: {available: [], elements: []},
      "pseudo-vars": {available: [], elements: []},
      program: {src: null, compiled: null, ast: null}
    },
    serverAvailable: true,
    socket: null,

    start: function (preferredHost) {
      if (localFlag) {
        // NOTE(Richo): Early exit, don't attempt to connect at all
        return Promise.resolve();
      }

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
      if (localFlag) {
        // NOTE(Richo): Instead of going to the server to compile, we do it locally
        return compileLocal(src, type, silent);
      }

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
    if (localFlag) {
      update({
        output: [
          {text: ""},
          {type: "info", text: (new Date()).toLocaleString()},
          {type: "error", text: "Not implemented in the DEMO version"}
        ]
      });
      return Promise.resolve();
    }
    return ajax.POST(url, data);
  }

  function log(data) {
    console.log(data);
  }

  function errorHandler (err) {
    console.log(err);
  }

  function compileLocal(src, type, silent) {
    return new Promise((resolve, reject) => {
      try {
        var result = middleware.core.compile(src, type);
        var program = {
          type: type,
          src: result.src,
          compiled: result.compiled,
          ast: result["original-ast"]
        };
        resolve(program);
        var bytecodes = middleware.core.encode(program.compiled);
        var output = [];
        if (!silent) {
          output = [
            {text: ""},
            {type: "info", text: (new Date()).toLocaleString()},
            {type: "info", text: "Program size (bytes): %1", args: [bytecodes.length]},
            {type: "info", text: "[%1]", args: [bytecodes.join(" ")]},
            {type: "success", text: "Compilation successful!", args: []}
          ]
        }
        update({
          program: program,
          output: output
        });
      } catch (err) {
        if (!silent) {
          update({
            output: [
              {text: ""},
              {type: "info", text: (new Date()).toLocaleString()},
              {type: "error", text: err.toString()}
            ]
          });
        }
        reject(err);
      }
    });
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
    let previousState = deepClone(Uzi.state);
    Object.keys(data).forEach(key => Uzi.state[key] = data[key]);

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
