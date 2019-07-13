let Uzi = (function () {

  let id = Math.floor(Math.random() * (2**64));
  let baseUrl = "";
  let observers = [];
  let serverDisconnectHandlers = [];

  let Uzi = {
    state: null,
    serverAvailable: true,

    start: function (url) {
      baseUrl = url || "";
      updateLoop(true);
    },
    addObserver: function (fn) {
      observers.push(fn);
    },
    addServerDisconnectHandler: function (fn) {
      serverDisconnectHandlers.push(fn);
    },
    connect: function (port) {
      ajax.request({
    			type: 'POST',
    			url: baseUrl + Uzi.state.actions.connect.href,
    			data: {
    				id: id,
    				port: port
    			},
          priority: 0
        })
        .then(update)
        .catch(errorHandler);
    },
    disconnect: function () {
      ajax.request({
    			type: 'POST',
    			url: baseUrl + Uzi.state.actions.disconnect.href,
    			data: {
    				id: id,
    			},
          priority: 0
        })
        .then(update)
        .catch(errorHandler);
    },
		compile: function (src, type, silent) {
  		ajax.request({
    			type: 'POST',
    			url: baseUrl + Uzi.state.actions.compile.href,
    			data: {
    				id: id,
    				src: src,
    				type: type,
            silent: silent == true
    			},
          priority: 0
        })
        .then(function (bytecodes) {
          console.log(bytecodes);
  			})
  			.catch(errorHandler);
  	},
    run: function (src, type, silent) {
  		ajax.request({
    			type: 'POST',
    			url: baseUrl + Uzi.state.actions.run.href,
    			data: {
    				id: id,
    				src: src,
    				type: type,
            silent: silent == true
    			},
          priority: 0
        })
        .then(function (bytecodes) {
          console.log(bytecodes);
        })
        .catch(errorHandler);
  	},
    install: function (src, type) {
      ajax.request({
    			type: 'POST',
    			url: baseUrl + Uzi.state.actions.install.href,
    			data: {
    				id: id,
    				src: src,
    				type: type
    			},
          priority: 0
        })
        .then(function (bytecodes) {
          console.log(bytecodes);
        })
        .catch(errorHandler);
    }
  };

  function nop () { /* Do nothing */ }

  function errorHandler (err) {
    console.log(err);
  }

  function serverDisconnect(error) {
    serverDisconnectHandlers.forEach(function (fn) {
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
    observers.forEach(function (fn) {
      try {
        fn(Uzi.state, previousState);
      } catch (err) {
        console.log(err);
      }
    });
  }

  function getUziState(wait) {
    return ajax.request({
      type: 'GET',
      url: baseUrl + "/uzi",
      data: {
        id: id,
        wait: wait
      },
      priority: 1,
    });
  }

  function updateLoop(first) {
    getUziState(first ? 0 : 45)
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

  function start() {
    updateLoop(true);
  }

  return Uzi;
})();
