connection
connectOnSocket: aString 
	self isConnected
		ifTrue: [^ self].
	serial := (UziSerialStream on: SocketSerialPort new)
				 baudRate: 57600;
				 openPort: aString.
	serial
		ifNotNil: [portName := aString.
			self startSteppingProcess; 
				startKeepAliveProcess.
			100 milliSeconds asDelay wait.
			self startReporting;
				sendPinsReporting;
				triggerEvent: #connect]