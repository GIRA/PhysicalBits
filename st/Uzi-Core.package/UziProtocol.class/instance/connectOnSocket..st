connection
connectOnSocket: aString
	Uzi logger cr; log: 'Connecting on socket...'; flush.
	^ self privateConnectOn: aString
		usingSerial: SocketSerialPort new