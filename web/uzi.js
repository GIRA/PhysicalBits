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
		pinsReporting: [],
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
		
		activatePinReport: function (pin) { setPinReport(pin, true); },
		deactivatePinReport: function (pin) { setPinReport(pin, false); },
				
		start: function () {
			updateLoop(true);
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
		}, 2);
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
		}, 2);
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
		}, 2);
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
		}, 2);
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
		}, 2);
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
		}, 2);
	}

	function getPins(callback) {
		ajax.request({ 
			type: 'GET', 
			url: Uzi.baseUrl + "/uzi/pins",
			data: {
				id: id
			},
			success: callback,
			error: errorHandler
		}, 2);
	}
	
	function setPinReport(pin, value) {
		ajax.request({ 
			type: 'POST', 
			url: Uzi.baseUrl + "/uzi/pins/reporting/" + (value ? "activate" : "deactivate"),
			data: {
				id: id,
				pin: pin
			},
			success: updatePins,
			error: errorHandler
		}, 2);
	}
	
	function update(uzi) {
		Uzi.portName = uzi.portName;
		Uzi.isConnected = uzi.isConnected;
		
		var pinsReporting = uzi.pins.elements.map(function(pin) { 
			return pin.number; 
		});
		var startPinMonitor = Uzi.isConnected && 
			Uzi.pinsReporting.length == 0 && 
			pinsReporting.length > 0;
		Uzi.pinsReporting = pinsReporting;
		
		if (!Uzi.isConnected) {
			Uzi.currentProgram = undefined;
			Uzi.pinsReporting = [];
		}
		if (startPinMonitor) {
			getPins(updatePins);
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
	
	function updatePins(pins) {
		if (!Uzi.isConnected) return;
		
		Uzi.pins = pins.elements;
		Uzi.pinsReporting = pins.elements.map(function (pin) {
			return pin.number;
		});
		triggerEvent(eventList.monitorUpdate);
		
		if (Uzi.pinsReporting.length > 0) {
			setTimeout(function () { getPins(updatePins); }, 100);
		}
	}
	
	return Uzi;
})();