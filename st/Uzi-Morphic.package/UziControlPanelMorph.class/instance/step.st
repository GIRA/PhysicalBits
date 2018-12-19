stepping
step
	rebuildFlag 
		ifTrue: [self rebuild.
			rebuildFlag := false].
	self borderColor: (self isConnected 
		ifTrue: [Color green] 
		ifFalse: [Color red]).
	connectButton label: (self isConnected 
		ifTrue: ['Disconnect']
		ifFalse: ['Connect...']).
	serverButton label: (self isServerStarted
		ifTrue: ['Stop server']
		ifFalse: ['Start server']).
	connectedText contents: self isConnected asString.
	portText contents: self portName.
	freeRAMText contents: self freeRAM asString.
	pinTexts keysAndValuesDo: [:pin :text |
		text privateValue: (self getPinValue: pin)].
	globalTexts keysAndValuesDo: [:var :text |
		text privateValue: (self getGlobalValue: var)].
	scriptTexts keysAndValuesDo: [:script :text |
		text contents: (((self isTicking: script)
				ifTrue: [' {1} -> running']
				ifFalse: [' {1}']) 
			format: {script name})]