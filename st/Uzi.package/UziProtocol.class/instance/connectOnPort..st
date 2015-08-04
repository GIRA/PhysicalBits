connection
connectOnPort: aString 
	self isConnected
		ifTrue: [^ self].
	serial := UziSerialStream new baudRate: 57600;
				 openPort: aString.
	serial
		ifNotNil: [portName := aString.
			self startSteppingProcess; startReporting]