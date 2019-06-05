connection
connectOnSocket: aString
	Uzi out cr; log: 'Connecting on socket...'; flush.
	^ self privateConnectOn: aString
		usingSerial: SocketSerialPort new