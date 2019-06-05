connection
connectOnPort: aString
	Uzi out cr; log: 'Connecting on serial...'; flush.
	^ self privateConnectOn: aString 
		usingSerial: SerialPort new