debugging
sendContinue
	debugger update: nil pc: nil fp: nil stack: nil.
	self triggerEvent: UziProtocolEvents debuggerUpdate.
	^ self send: { 
		self class msgOutDebugContinue
	}