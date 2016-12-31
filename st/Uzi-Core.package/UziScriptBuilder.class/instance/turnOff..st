instruction creation
turnOff: arg
	^ self addInstruction: (UziTurnOffInstruction arg: arg) 