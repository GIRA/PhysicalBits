instruction creation
call: arg
	^ self addInstruction: (UziScriptCallInstruction arg: arg)