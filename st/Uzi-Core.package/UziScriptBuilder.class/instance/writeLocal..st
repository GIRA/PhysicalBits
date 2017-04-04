instruction creation
writeLocal: arg
	^ self addInstruction: (UziWriteLocalInstruction arg: arg)