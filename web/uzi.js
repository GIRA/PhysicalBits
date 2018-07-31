var Uzi = (function () {
	
	var id = Math.floor(Math.random() * (2**64));
	var eventList = {
		update: [],
		error: []
	}
			
	var Uzi = {
		baseUrl: "",
		isConnected: false,
		portName: undefined,
		
		onUpdate: function (callback) {
			eventList.update.push(callback);
		},
		onError: function (callback) {
			eventList.error.push(callback);
		},
		
		connect: connect,
		disconnect: disconnect,
		compile: compile,
		install: install,
		run: run,
		
		start: function () {
			updateLoop(true);
		}
	};
	
	function nop () { /* Do nothing */ }
	
	function errorHandler (err) {
		console.log(err);
		eventList.error.forEach(function (each){
			each(err);
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
				callback(bytecodes);
			},
			error: errorHandler
		}, 2);
	}
	
	function update(uzi) {
		Uzi.portName = uzi.portName;
		Uzi.isConnected = uzi.isConnected;
		
		eventList.update.forEach(function (each) { 
			each(); 
		});
	}

	function updateLoop(first) {
		getUziState(first ? 0 : 45, {
			success: update,
			complete: function () { updateLoop(false); },
			error: errorHandler
		});
	}
	
	return Uzi;
})();