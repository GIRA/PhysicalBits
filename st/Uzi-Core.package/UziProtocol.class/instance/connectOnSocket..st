connection
connectOnSocket: aString
	^ self privateConnectOn: aString
		usingSerial: SocketSerialPort new