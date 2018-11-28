connection
connectOnSocket: aString
	UziTranscript cr; log: 'Connecting on socket...'.
	^ self privateConnectOn: aString
		usingSerial: SocketSerialPort new