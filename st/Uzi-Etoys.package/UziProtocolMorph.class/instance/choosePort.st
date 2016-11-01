connection
choosePort
	^ portName := UIManager 
		request: 'Port:' 
		initialAnswer: '127.0.0.1:4242'