connection
connectOnPort: aString
	Uzi logger cr; log: 'Connecting on serial...'; flush.
	^ self privateConnectOn: aString 
		usingSerial: SerialPort new