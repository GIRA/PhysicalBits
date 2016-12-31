instruction creation
write: arg
	^ self addInstruction: (UziWriteInstruction arg: arg)