let Uzi = (function () {

  // TODO(Richo): Make a proper UUID?
  let id = Math.floor(Math.random() * (2**64));
  let host = "";
  let apiURL = "";
  let wsURL = "";
  let observers = {
    "update" : [],
    "server-disconnect" : []
  };


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
      tasks: [],
      memory: {
        arduino: null,
        uzi: null,
      },
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
      program: {src: null, compiled: null, ast: null},
      debugger: {isHalted: false},
      features: {
        "closable-panels?": true,
        "persistent-layout?": true,
        "options-button?": true,
        "blocks?": true,
        "code?": true,
        "monitoring?": true,
        "interactivity?": true,
        "debugging?": true,
        "concurrency?": true,
      }
    },
    serverAvailable: true,
    socket: null,

    start: function (preferredHost) {
      middleware.simulator.on_update(update);

      // HACK(Richo): Early exit, if on DEMO mode we don't bother connecting to the server
      if (DEMO) return Promise.resolve();

      host = preferredHost || "";
      apiURL = host ? "http://" + host : "";
      wsURL = "ws://" + (host || location.host);

      return new Promise((resolve, reject) => {
        let i = 0;
        let begin = +new Date();
        function connect() {
          let elapsedTime = (+new Date()) - begin;
          if (elapsedTime >= 30000) {
            console.error("Server not found! Giving up...");
            return reject();
          }
          console.log("ATTEMPT: " + (++i));
          console.log("Elapsed time: " + elapsedTime);
          updateLoop().then(resolve).catch(err => {
            setTimeout(connect, 1000);
          });
        }
        connect();
      });
    },

    on: function (evt, callback) {
      observers[evt].push(callback);
    },

    connect: function (port) {
      if (port == "simulator") {
        return middleware.simulator.connect(update);
      }

      let url = apiURL + "/uzi/connect";
      let data = { id: id, port: port };
      return POST(url, data);
    },

    disconnect: function () {
      if (Uzi.state.connection.portName == "simulator") {
        return middleware.simulator.disconnect();
      }

      let url = apiURL + "/uzi/disconnect";
      let data = { id: id };
      return POST(url, data);
    },

		compile: function (src, type, silent) {
      if (DEMO) {
        // NOTE(Richo): Instead of going to the server to compile, we do it locally
        return middleware.simulator.compile(src, type, silent);
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
      if (Uzi.state.connection.portName == "simulator") {
        return middleware.simulator.run(src, type, silent);
      }

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
      if (Uzi.state.connection.portName == "simulator") {
        return middleware.simulator.install(src, type);
      }

      let url = apiURL + "/uzi/install";
      let data = {
        id: id,
        src: src,
        type: type
      };
      return POST(url, data);
    },

    setPinReport: function (pins, report) {
      if (Uzi.state.connection.portName == "simulator") {
        return middleware.simulator.set_pin_report(pins, report);
      }

      let url = apiURL + "/uzi/pin-report";
      let data = {
        id: id,
        pins: Array.from(pins).join(","),
        report: Array.from(report).join(",")
      };
      return POST(url, data);
    },
    setPinValues: function (pins, values) {
      if (Uzi.state.connection.portName == "simulator") {
        return middleware.simulator.set_pin_values(pins, values);
      }

      let url = apiURL + "/uzi/pin-values";
      let data = {
        id: id,
        pins: Array.from(pins).join(","),
        values: Array.from(values).join(",")
      };
      return POST(url, data);
    },

    setGlobalReport: function (globals, report) {
      if (Uzi.state.connection.portName == "simulator") {
        return middleware.simulator.set_global_report(globals, report);
      }

      let url = apiURL + "/uzi/global-report";
      let data = {
        id: id,
        globals: Array.from(globals).join(","),
        report: Array.from(report).join(",")
      };
      return POST(url, data);
    },
    setGlobalValues: function (globals, values) {
      if (Uzi.state.connection.portName == "simulator") {
        return middleware.simulator.set_global_values(globals, values);
      }

      let url = apiURL + "/uzi/global-values";
      let data = {
        id: id,
        globals: Array.from(globals).join(","),
        values: Array.from(values).join(",")
      };
      return POST(url, data);
    },

    setProfile: function (enabled) {
      if (Uzi.state.connection.portName == "simulator") {
        return middleware.simulator.set_profile(enabled);
      }

      let url = apiURL + "/uzi/profile";
      let data = {
        id: id,
        enabled: enabled
      };
      return POST(url, data);
    },

    elog: function (evtType, evtData) {      
      let url = apiURL + "/uzi/elog";
      let data = {
        type: evtType,
        data: JSONX.stringify(evtData),
      };
      //console.log("ELOG! >>> " + evtType + " -> ", evtData);
      return POST(url, data);
    },

    debugger: {
      setBreakpoints: function (breakpoints) {
        if (Uzi.state.connection.portName == "simulator") {
          return middleware.simulator.debugger_set_breakpoints(breakpoints);
        }

        let url = apiURL + "/uzi/debugger/set-breakpoints";
        let data = {
          id: id,
          breakpoints: breakpoints.join(","),
        };
        return POST(url, data);
      },

      break: function () {
        if (Uzi.state.connection.portName == "simulator") {
          return middleware.simulator.debugger_break();
        }

        let url = apiURL + "/uzi/debugger/break";
        let data = {
          id: id
        };
        return POST(url, data);
      },

      continue: function () {
        if (Uzi.state.connection.portName == "simulator") {
          return middleware.simulator.debugger_continue();
        }

        let url = apiURL + "/uzi/debugger/continue";
        let data = {
          id: id
        };
        return POST(url, data);
      },

      stepOver: function () {
        if (Uzi.state.connection.portName == "simulator") {
          return middleware.simulator.debugger_step_over();
        }

        let url = apiURL + "/uzi/debugger/step-over";
        let data = {
          id: id
        };
        return POST(url, data);
      },

      stepInto: function () {
        if (Uzi.state.connection.portName == "simulator") {
          return middleware.simulator.debugger_step_into();
        }

        let url = apiURL + "/uzi/debugger/step-into";
        let data = {
          id: id
        };
        return POST(url, data);
      },

      stepOut: function () {
        if (Uzi.state.connection.portName == "simulator") {
          return middleware.simulator.debugger_step_out();
        }

        let url = apiURL + "/uzi/debugger/step-out";
        let data = {
          id: id
        };
        return POST(url, data);
      },

      stepNext: function () {
        if (Uzi.state.connection.portName == "simulator") {
          return middleware.simulator.debugger_step_next();
        }

        let url = apiURL + "/uzi/debugger/step-next";
        let data = {
          id: id
        };
        return POST(url, data);
      }
    }
  };

  function POST(url, data) {
    if (DEMO) return Promise.resolve();
    return ajax.POST(url, data);
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
    let keys = new Set(Object.keys(data));
    keys.forEach(key => Uzi.state[key] = data[key]);

    observers["update"].forEach(function (fn) {
      try {
        fn(Uzi.state, previousState, keys);
      } catch (err) {
        console.error(err);
      }
    });
  }

  function updateLoop() {
    return new Promise((resolve, reject) => {
      try {
        let socket = new WebSocket(wsURL + "/uzi");
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
              resolve();
            } catch (e) {
              console.error(e);
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
