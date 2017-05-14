var Uzi = (function () {
			
	var Uzi = {
		isConnected: false,
		portName: undefined,
		
		onUpdate: nop,
		onError: nop,
		
		connect: connect,
		disconnect: disconnect,
		compile: compile,
		install: install,
		run: run
	};
	
	function nop () { /* Do nothing */ }
	
	function errorHandler (err) {
		console.log(err);
		Uzi.onError(err);
	}
		
	function getUziState(wait, callbacks) {
		var success = callbacks.success || nop;
		var complete = callbacks.complete || nop;
		var error = callbacks.error || nop;
		ajax.request({ 
			type: 'GET', 
			url: "/uzi",
			data: {
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
			url: "/uzi/actions/connect",
			data: {
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
			url: "/uzi/actions/disconnect",
			data: {},
			success: function (uzi) {
				update(uzi);
				callback();
			},
			error: errorHandler
		}, 2);
	}

	function compile(src, callback) {
		ajax.request({ 
			type: 'POST', 
			url: "/uzi/actions/compile",
			data: {
				src: src
			},
			success: function (bytecodes) {
				callback(bytecodes);
			},
			error: errorHandler
		}, 2);
	}

	function install(src, callback) {
		ajax.request({ 
			type: 'POST', 
			url: "/uzi/actions/install",
			data: {
				src: src
			},
			success: function (bytecodes) {
				callback(bytecodes);
			},
			error: errorHandler
		}, 2);
	}

	function run(src, callback) {
		ajax.request({ 
			type: 'POST', 
			url: "/uzi/actions/run",
			data: {
				src: src
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
		
		Uzi.onUpdate();
	}

	function updateLoop(first) {
		getUziState(first ? 0 : 45, {
			success: update,
			complete: function () { updateLoop(false); },
			error: errorHandler
		});
	}
	
	updateLoop(true);
	
	return Uzi;
})();