instruction creation
turnOn: arg
	^ self addInstruction: (UziTurnOnInstruction arg: arg) 