connection
startProcesses
	self fork: [
			self triggerEvent: #connect;
				startSteppingProcess; 
				startKeepAliveProcess.
			3000 milliSeconds asDelay wait.
			self startReporting;
				sendPinsReporting]
		named: 'Starting...'