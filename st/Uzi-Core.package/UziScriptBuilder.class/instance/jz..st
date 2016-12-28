instruction creation
jz: arg
	self addInstruction: (UziJZInstruction arg: arg)