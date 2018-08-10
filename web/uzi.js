var Uzi = (function () {
	
	var id = Math.floor(Math.random() * (2**64));
	var eventList = {
		error: [],
		connectionUpdate: [],
		monitorUpdate: [],
	};
			
	var Uzi = {
		baseUrl: "",
		isConnected: false,
		portName: undefined,
		currentProgram: undefined,
		pinsReporting: new Set(),
		pins: [],
		
		onError: function (callback) {
			eventList.error.push(callback);
		},
		onConnectionUpdate: function (callback) {
			eventList.connectionUpdate.push(callback);
		},
		onMonitorUpdate: function (callback) {
			eventList.monitorUpdate.push(callback);
		},
		
		connect: connect,
		disconnect: disconnect,
		compile: compile,
		install: install,
		run: run,
		
		activatePinReport: function (pin) { 
			Uzi.pinsReporting.add(pin); 
		},
		deactivatePinReport: function (pin) { 
			Uzi.pinsReporting.delete(pin); 			
		},
				
		start: function () {
			updateLoop(true);
			startMonitor();
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
		ajax.request({ 
			type: 'POST', 
			url: Uzi.baseUrl + "/uzi/actions/run",
			data: {
				id: id,
				src: src,
				type: type
			},
			success: function (bytecodes) {
				Uzi.currentProgram = {
					src: src,
					type: type
				};
				callback(bytecodes);
			},
			error: errorHandler
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
	
	function update(uzi) {
		Uzi.portName = uzi.portName;
		Uzi.isConnected = uzi.isConnected;
				
		if (!Uzi.isConnected) {
			Uzi.currentProgram = undefined;
		}		
		triggerEvent(eventList.connectionUpdate);
	}

	function updateLoop(first) {
		getUziState(first ? 0 : 45, {
			success: update,
			complete: function () { updateLoop(false); },
			error: errorHandler
		});
	}
	
	function startMonitor() {
		var waiting = false;
		var last = 0;
		setInterval(function () {
			if (waiting || Uzi.pinsReporting.size === 0) return;
			waiting = true;
			getPins(last, Uzi.pinsReporting, function (pins) {
				Uzi.pins = pins.elements;
				last = Math.max.apply(Math, pins.elements
					.filter(function (each) {
						return each.history.length > 0;
					})
					.map(function (each) {					
						return each.history[each.history.length-1].timestamp;
					}));
				triggerEvent(eventList.monitorUpdate);
				waiting = false;
			});
		}, 200);
	}
	
	return Uzi;
})();