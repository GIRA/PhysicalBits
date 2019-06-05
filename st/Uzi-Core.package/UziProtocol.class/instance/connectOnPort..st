connection
connectOnPort: aString
	UziTranscript cr; log: 'Connecting on serial...'; flush.
	^ self privateConnectOn: aString 
		usingSerial: SerialPort new