connection
connectOnPort: portName 
	self isConnected
		ifTrue: [^ self].
	serial := UziSerialStream new baudRate: 57600;
				 openPort: portName.
	serial
		ifNotNil: [self startSteppingProcess; startReporting]