instruction creation
read: arg
	^ self addInstruction: (UziReadInstruction arg: arg)