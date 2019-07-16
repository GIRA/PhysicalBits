accessing
portName
	^ self isConnected 
		ifTrue: [controller portName asString]
		ifFalse: [portName asString]