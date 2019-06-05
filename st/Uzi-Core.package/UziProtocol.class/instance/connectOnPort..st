connection
connectOnPort: aString
	UziOutput cr; log: 'Connecting on serial...'; flush.
	^ self privateConnectOn: aString 
		usingSerial: SerialPort new