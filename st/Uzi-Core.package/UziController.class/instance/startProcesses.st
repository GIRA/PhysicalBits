connection
startProcesses
	self fork: [
			self triggerEvent: UziProtocolEvents connect;
				startSteppingProcess; 
				startKeepAliveProcess.
			3000 milliSeconds asDelay wait.
			self startReporting;
				sendPinsReporting]
		named: 'Starting...'