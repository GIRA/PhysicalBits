accessing
portName: aSymbol 
	self isConnected
		ifTrue: [^ self].
	portName := aSymbol = #'other...'
				ifTrue: [self choosePort]
				ifFalse: [aSymbol].
	self class lastPortName: portName