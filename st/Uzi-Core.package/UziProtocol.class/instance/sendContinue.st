debugging
sendContinue
	^ self send: { 
		self class msgOutDebugContinue
	}