instruction creation
dup: arg
	^ self addInstruction: (UziDupInstruction arg: arg)