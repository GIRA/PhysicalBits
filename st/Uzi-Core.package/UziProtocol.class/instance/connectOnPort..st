connection
connectOnPort: aString
	UziTranscript cr; log: 'Connecting on serial...'.
	^ self privateConnectOn: aString 
		usingSerial: SerialPort new