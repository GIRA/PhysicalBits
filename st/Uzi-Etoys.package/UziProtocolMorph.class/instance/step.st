stepping
step
	self borderColor: (self isConnected 
		ifTrue: [Color green] 
		ifFalse: [Color red]).
	connectButton label: (self isConnected 
		ifTrue: ['Disconnect']
		ifFalse: ['Connect...']).
	connectedText contents: self isConnected asString.
	portText contents: self portName.
	pinTexts keysAndValuesDo: [:pin :text |
		text privateValue: (self getPinValue: pin)].
	globalTexts keysAndValuesDo: [:var :text |
		text privateValue: (self getGlobal: var)].
	scriptTexts keysAndValuesDo: [:script :text |
		text contents: (((self isTicking: script)
				ifTrue: [' @{1} -> ticking']
				ifFalse: [' @{1}']) 
			format: {script name})]