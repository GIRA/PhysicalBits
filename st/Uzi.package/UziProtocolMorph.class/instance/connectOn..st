connection
connectOn: portName
	(portName beginsWith: 'COM')
		ifTrue: [self connectOnPort: portName]
		ifFalse: [self connectOnSocket: portName]