accessing
portName
	^ self isConnected 
		ifTrue: [protocol portName asString]
		ifFalse: [portName asString]