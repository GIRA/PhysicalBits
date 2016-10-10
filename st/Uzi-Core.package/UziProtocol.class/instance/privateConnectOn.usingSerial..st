connection
privateConnectOn: portString usingSerial: aSerialPort
	self isConnected
		ifTrue: [^ self].
	serial := (UziSerialStream on: aSerialPort)
				baudRate: 57600;
				openPort: portString.
	serial
		ifNotNil: [portName := portString.
			self startSteppingProcess; 
				startKeepAliveProcess.
			100 milliSeconds asDelay wait.
			self startReporting;
				sendPinsReporting;
				triggerEvent: #connect]