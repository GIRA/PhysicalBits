connection
startProcesses
	self fork: [
			self startSteppingProcess; 
				startKeepAliveProcess.
			3000 milliSeconds asDelay wait.
			self startReporting;
				sendPinsReporting;
				triggerEvent: #connect]
		named: 'Starting...'