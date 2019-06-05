connection
connectOnSocket: aString
	UziOutput cr; log: 'Connecting on socket...'; flush.
	^ self privateConnectOn: aString
		usingSerial: SocketSerialPort new