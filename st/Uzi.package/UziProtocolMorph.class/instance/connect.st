connection
connect
	| portName |
	portName := UIManager 
		request: 'Port:' 
		initialAnswer: '127.0.0.1:4242'.
	self connectOn: portName