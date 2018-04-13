connection
privateConnectOn: portString usingSerial: aSerialPort
	self isConnected
		ifTrue: [^ self].
	serial := (UziSerialStream on: aSerialPort)
				baudRate: 57600;
				openPort: portString.
	serial ifNil: [^ self].
	portName := portString.
	self requestConnection.
	serial ifNil: [^ self].
	self startSteppingProcess; 
		startKeepAliveProcess.
	3000 milliSeconds asDelay wait.
	self startReporting;
		sendPinsReporting;
		triggerEvent: #connect