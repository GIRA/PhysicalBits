var Uzi = (function () {
	
	var id = Math.floor(Math.random() * (2**64));
	var eventList = {
		error: [],
		connectionUpdate: [],
		programUpdate: [],
		monitorUpdate: [],
		debuggerUpdate: []
	};
			
	var Uzi = {
		baseUrl: "",
		isConnected: false,
		portName: undefined,
		program: {
			id: undefined,
			src: undefined,
			ast: undefined,
			bytecodes: undefined,
		},
		pinsReporting: new Set(),
		pins: [],
		availableGlobals: [],
		globalsReporting: new Set(),
		globals: [],
		
		onError: function (callback) {
			eventList.error.push(callback);
		},
		onConnectionUpdate: function (callback) {
			eventList.connectionUpdate.push(callback);
		},
		onProgramUpdate: function (callback) {
			eventList.programUpdate.push(callback);
		},
		onMonitorUpdate: function (callback) {
			eventList.monitorUpdate.push(callback);
		},
		onDebuggerUpdate: function (callback) {
			eventList.debuggerUpdate.push(callback);
		},
		
		connect: connect,
		disconnect: disconnect,
		compile: compile,
		install: install,
		run: run,
		
		activatePinReport: function (pinNumber) {
			Uzi.pinsReporting.add(pinNumber);
		},
		deactivatePinReport: function (pinNumber) {
			Uzi.pinsReporting.delete(pinNumber);
		},
		activateGlobalReport: function (globalNumber) {
			Uzi.globalsReporting.add(globalNumber);
		},
		deactivateGlobalReport: function (globalNumber) {
			Uzi.globalsReporting.delete(globalNumber);
		},
				
		start: function () {
			updateLoop(true);
			startPinMonitor();
			startGlobalMonitor();
		}
	};
	
	function nop () { /* Do nothing */ }
	
	function errorHandler (err) {
		console.log(err);
		triggerEvent(eventList.error, err);
	}
	
	function triggerEvent(evt, args) {
		evt.forEach(function (each) { 
			each(args); 
		});
	}
		
	function getUziState(wait, callbacks) {
		var success = callbacks.success || nop;
		var complete = callbacks.complete || nop;
		var error = callbacks.error || nop;
		ajax.request({ 
			type: 'GET', 
			url: Uzi.baseUrl + "/uzi",
			data: {
				id: id,
				wait: wait
			},
			success: success,
			complete: complete,
			error: error
		}, 1);
	}

	function connect (port, callback) {
		ajax.request({ 
			type: 'POST', 
			url: Uzi.baseUrl + "/uzi/actions/connect",
			data: {
				id: id,
				port: port
			},
			success: function (uzi) {
				update(uzi);
				callback();
			},
			error: errorHandler
		}, 0);
	}

	function disconnect (callback) {
		ajax.request({ 
			type: 'POST', 
			url: Uzi.baseUrl + "/uzi/actions/disconnect",
			data: {
				id: id
			},
			success: function (uzi) {
				update(uzi);
				callback();
			},
			error: errorHandler
		}, 0);
	}

	function compile(src, type, callback) {
		ajax.request({ 
			type: 'POST', 
			url: Uzi.baseUrl + "/uzi/actions/compile",
			data: {
				id: id,
				src: src,
				type: type
			},
			success: function (bytecodes) {
				callback(bytecodes);				
			},
			error: errorHandler
		}, 0);
	}

	function install(src, type, callback) {
		ajax.request({ 
			type: 'POST', 
			url: Uzi.baseUrl + "/uzi/actions/install",
			data: {
				id: id,
				src: src,
				type: type
			},
			success: function (bytecodes) {
				callback(bytecodes);
			},
			error: errorHandler
		}, 0);
	}

	function run(src, type, callback) {
		var program = {
			id: undefined,
			compiled: false			
		};
		if (type === "json") {
			program.ast = src;
		} else {
			program.src = src;
		}
		Uzi.program = program;
		ajax.request({ 
			type: 'POST', 
			url: Uzi.baseUrl + "/uzi/actions/run",
			data: {
				id: id,
				src: src,
				type: type
			},
			success: function (bytecodes) {
				callback(bytecodes);
			},
			error: function (err) {
				Uzi.program = {
					id: undefined,
					src: undefined,
					ast: undefined,
					bytecodes: undefined,
				};
				errorHandler(err);
			}
		}, 0);
	}

	function getPins(start, pins, callback) {
		ajax.request({ 
			type: 'GET', 
			url: Uzi.baseUrl + "/uzi/pins",
			data: {
				id: id,
				start: start,
				pins: Array.from(pins).join(",")
			},
			success: callback,
			error: errorHandler
		}, 2);
	}

	function getGlobals(start, globals, callback) {
		ajax.request({ 
			type: 'GET', 
			url: Uzi.baseUrl + "/uzi/globals",
			data: {
				id: id,
				start: start,
				globals: Array.from(globals).join(",")
			},
			success: callback,
			error: errorHandler
		}, 2);
	}
	
	function update(uzi) {
		Uzi.portName = uzi.portName;
		Uzi.isConnected = uzi.isConnected;
		Uzi.availableGlobals = uzi.globals.available;		
		triggerEvent(eventList.connectionUpdate);
		
		if (Uzi.program.id !== uzi.program.id) {
			Uzi.program = {
				id: uzi.program.id,
				src: uzi.program.src,
				ast: uzi.program.ast,
				bytecodes: uzi.program.bytecodes,
				compiled: true
			};
			triggerEvent(eventList.programUpdate);
		}
		
		Uzi.debugger = uzi.debugger;
		triggerEvent(eventList.debuggerUpdate);
	}

	function updateLoop(first) {
		getUziState(first ? 0 : 45, {
			success: update,
			complete: function () { updateLoop(false); },
			error: errorHandler
		});
	}
	
	function startPinMonitor() {
		var waiting = false;
		var last = 0;
		setInterval(function () {
			if (waiting || Uzi.pinsReporting.size === 0) return;
			waiting = true;
			getPins(last, Uzi.pinsReporting, function (pins) {
				Uzi.pins = fixedInvalidJSONFloats(pins.elements);
				if (Uzi.pins.length > 0) {
					last = Math.max.apply(Math, Uzi.pins.map(function (each) {
						return each.history.length > 0 ?
							each.history[each.history.length-1].timestamp :
							0;
					}));
				}
				triggerEvent(eventList.monitorUpdate);
				waiting = false;
			});
		}, 200);
	}
	
	function startGlobalMonitor() {
		var waiting = false;
		var last = 0;
		setInterval(function () {
			if (waiting || Uzi.globalsReporting.size === 0) return;
			waiting = true;
			getGlobals(last, Uzi.globalsReporting, function (globals) {
				Uzi.globals = fixedInvalidJSONFloats(globals.reporting);
				if (Uzi.globals.length > 0) {
					last = Math.max.apply(Math, Uzi.globals.map(function (each) {
						return each.history.length > 0 ?
							each.history[each.history.length-1].timestamp :
							0;
					}));
				}
				triggerEvent(eventList.monitorUpdate);
				waiting = false;
			});
		}, 200);
	}
	
	/*
	HACK(Richo): This function will fix occurrences of Infinity, -Infinity, and NaN 
	in the JSON object resulting from either the pin or global reports. Since JSON
	doesn't handle these values correctly I'm encoding them in a special way.
	*/
	function fixedInvalidJSONFloats(reporting) {
		return reporting.map(function (r) { 
			return {
				name: r.name, 
				number: r.number, 
				history: r.history.map(function (h) {
					let value = h.value;
					if (value["___INF___"] !== undefined) {
						value = Infinity * value["___INF___"];
					} else if (value["___NAN___"] !== undefined) {
						value = NaN;
					}
					return { timestamp: h.timestamp, value: value };
				})
			};
		});
	}
	
	return Uzi;
})();