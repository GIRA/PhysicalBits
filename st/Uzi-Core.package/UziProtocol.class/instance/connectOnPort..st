connection
connectOnPort: aString
	^ self privateConnectOn: aString 
		usingSerial: SerialPort new