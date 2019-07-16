connection
privateConnectOn: portString usingSerial: aSerialPort
	self isConnected ifTrue: [^ self].
	serial := UziSerialStream on: aSerialPort.
	self openPort: portString.
	serial ifNil: [^ self].
	portName := portString.
	self requestConnection.
	serial ifNil: [^ self].
	self startProcesses