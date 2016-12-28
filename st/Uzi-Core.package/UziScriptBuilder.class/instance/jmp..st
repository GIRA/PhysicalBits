instruction creation
jmp: arg
	self addInstruction: (UziJMPInstruction arg: arg)