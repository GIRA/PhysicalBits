connection
connectOnSocket: aString
	UziTranscript cr; log: 'Connecting on socket...'; flush.
	^ self privateConnectOn: aString
		usingSerial: SocketSerialPort new