open/close
openPort: aString 
	"Keeps the SerialPort behaviour of returning nil when the port didn't
	open, but returns self instead of the port "
	^ (port openPort: aString)
		ifNotNil: [self]