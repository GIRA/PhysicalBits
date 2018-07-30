connection
connectOn: port
	(port beginsWith: 'COM')
		ifTrue: [self connectOnPort: port]
		ifFalse: [self connectOnSocket: port].
	pinTexts keysDo: [:pin | self setPin: pin report: true]