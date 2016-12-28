instruction creation
stop: arg
	self addInstruction: (UziStopScriptInstruction arg: arg)