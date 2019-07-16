connection
connectOn: port
	^ ((port beginsWith: 'COM') or: [port beginsWith: '/'])
		ifTrue: [self connectOnPort: port]
		ifFalse: [self connectOnSocket: port].