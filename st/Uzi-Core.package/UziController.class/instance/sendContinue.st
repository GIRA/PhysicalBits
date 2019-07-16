debugging
sendContinue
	debugger update: nil pc: nil fp: nil stack: nil.
	self triggerEvent: UziEvents debuggerUpdate.
	^ self send: { 
		self class msgOutDebugContinue
	}