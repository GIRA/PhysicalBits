connection
connectOnPort: portName
	self isConnected ifTrue: [^ self].
	port := SerialPort new baudRate: 57600;
			openPort: portName